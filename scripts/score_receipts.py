"""Score local OCR against human-verified labels; never print financial values.
Run: python scripts/score_receipts.py --results testdata/receipt-results-RUN.json
Empty CSV cells mean unknown; use <EMPTY> for a verified absent field.
"""
import argparse, csv, json, unicodedata
from collections import defaultdict
from datetime import datetime, timezone, timedelta
from pathlib import Path

FIELDS={'amount':'amount','status':'status','recipient':'recipient','recipient_bank':'recipientBank','account':'account','memo':'memo','time':'time','transaction_id':'transactionId'}
VN=timezone(timedelta(hours=7))
def normalize(value, field):
    if value is None or value=='<EMPTY>': return ''
    if field=='amount': return str(int(str(value).replace(',','').replace('.','').replace(' ','')))
    if field=='time':
        text=str(value).strip()
        if text.isdigit(): return str(int(text))
        stamp=datetime.fromisoformat(text)
        if stamp.tzinfo is None: stamp=stamp.replace(tzinfo=VN)
        return str(int(stamp.timestamp()*1000))
    return ' '.join(unicodedata.normalize('NFC',str(value)).casefold().split())
def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--results',required=True,type=Path)
    parser.add_argument('--labels',type=Path,default=Path('testdata/labels.csv'))
    parser.add_argument('--output',type=Path,default=Path('OCR-REPORT.md'))
    args=parser.parse_args()
    rows=list(csv.DictReader(args.labels.open(encoding='utf-8-sig')))
    result=json.loads(args.results.read_text(encoding='utf-8-sig'))
    items={item['file']:item for item in result['items']}
    if len(items)!=len(result['items']): raise ValueError('Duplicate result aliases')
    counters=defaultdict(lambda:[0,0]); errors=[]; verified=0
    for row in rows:
        if row['verified'].strip().lower()!='true': continue
        verified+=1
        item=items.get(row['file'],{})
        draft=item.get('draft') or {}
        for field,key in FIELDS.items():
            truth=row.get(field,'').strip()
            if not truth: continue
            counter=counters[(row['source_bank'],row['split'],field)]
            counter[1]+=1
            try: correct=normalize(truth,field)==normalize(draft.get(key,{}).get('value'),field)
            except (ValueError,TypeError):
                raise ValueError(f"Invalid verified label format: {row['file']} / {field}") from None
            counter[0]+=int(correct)
            if not correct: errors.append((row['file'],row['split'],field,'engine error' if item.get('error') else 'missing result' if not item else 'extraction mismatch; review locally'))
    lines=['# OCR — kết quả trên thiết bị','',f'Engine: {result.get("engine","mlkit-bundled-16.0.1")}.',f'Ảnh xử lý: {len(items)}. Nhãn được người dùng xác minh: {verified}/{len(rows)}.','',
           'Chia cố định 21 phát triển / 9 kiểm tra (seed 20261026); chỉ ảnh phát triển được dùng chỉnh quy tắc. Tất cả 30 ảnh nguồn Techcombank. Mẫu yêu cầu rút ATM không phải giao dịch chuyển tiền hoàn tất.','',
           'So sánh chính xác sau chuẩn hóa Unicode, khoảng trắng và hoa/thường; giữ dấu tiếng Việt. Thời gian so epoch theo múi giờ Việt Nam. Ô nhãn trống không tính; `<EMPTY>` là trường được xác minh không có. Confidence là heuristic, không phải xác suất đã hiệu chuẩn.','']
    if not counters: lines+=['**Chưa có độ chính xác:** không có trường nhãn đã xác minh để chấm. Không dùng kết quả OCR làm đáp án.','']
    else:
        lines+=['| Ngân hàng | Tập | Trường | Đúng / Có nhãn | Chính xác |','|---|---|---|---:|---:|']
        for (bank,split,field),(ok,total) in sorted(counters.items()): lines.append(f'| {bank} | {split} | {field} | {ok}/{total} | {ok/total:.1%} |')
    lines+=['','## Sai khác cần đối chiếu cục bộ','']
    lines += [f'- `{alias}` ({split}), {field}: {reason}.' for alias,split,field,reason in errors] or ['Không có sai khác được chấm; điều này không chứng minh độ chính xác nếu chưa có nhãn.']
    lines+=['','## Trường trích xuất được (không phải độ chính xác)','','| Trường | Có kết quả / Ảnh |','|---|---:|']
    for field,key in FIELDS.items():
        count=sum(i.get('draft',{}).get(key,{}).get('value') is not None for i in items.values() if i.get('draft'))
        lines.append(f'| {field} | {count}/{len(items)} |')
    failures=sum(bool(i.get('error')) for i in items.values())
    elapsed=[i.get('elapsedMs',0) for i in items.values() if not i.get('error')]
    lines+=['',f'Lỗi engine: {failures}/{len(items)}.',f'Thời gian xử lý trung bình (gồm giải mã + OCR + parser): {sum(elapsed)/len(elapsed):.0f} ms.' if elapsed else 'Chưa có thời gian xử lý hợp lệ.',
            '', 'Thiết bị có Google Play Services nên lần chạy này không xác minh môi trường không GMS. Chưa có so sánh Tesseract; lựa chọn engine còn cần nhãn xác minh và máy không GMS. Không công bố nội dung tài chính trong báo cáo.','']
    args.output.write_text('\n'.join(lines),encoding='utf-8')
    print(f'Scored {verified} verified rows; report saved (no financial values printed).')
if __name__=='__main__': main()
