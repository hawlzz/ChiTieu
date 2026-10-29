# OCR — kết quả trên thiết bị

Engine: mlkit-bundled-16.0.1.
Ảnh xử lý: 30. Nhãn được người dùng xác minh: 0/30.

Chia cố định 21 phát triển / 9 kiểm tra (seed 20261026); chỉ ảnh phát triển được dùng chỉnh quy tắc. Tất cả 30 ảnh nguồn Techcombank. Mẫu yêu cầu rút ATM không phải giao dịch chuyển tiền hoàn tất.

So sánh chính xác sau chuẩn hóa Unicode, khoảng trắng và hoa/thường; giữ dấu tiếng Việt. Thời gian so epoch theo múi giờ Việt Nam. Ô nhãn trống không tính; `<EMPTY>` là trường được xác minh không có. Confidence là heuristic, không phải xác suất đã hiệu chuẩn.

**Chưa có độ chính xác:** không có trường nhãn đã xác minh để chấm. Không dùng kết quả OCR làm đáp án.


## Sai khác cần đối chiếu cục bộ

Không có sai khác được chấm; điều này không chứng minh độ chính xác nếu chưa có nhãn.

## Trường trích xuất được (không phải độ chính xác)

| Trường | Có kết quả / Ảnh |
|---|---:|
| amount | 28/30 |
| status | 30/30 |
| recipient | 25/30 |
| recipient_bank | 26/30 |
| account | 26/30 |
| memo | 26/30 |
| time | 21/30 |
| transaction_id | 13/30 |

Lỗi engine: 0/30.
Thời gian xử lý trung bình (gồm giải mã + OCR + parser): 159 ms.

Thiết bị có Google Play Services nên lần chạy này không xác minh môi trường không GMS. Chưa có so sánh Tesseract; lựa chọn engine còn cần nhãn xác minh và máy không GMS. Không công bố nội dung tài chính trong báo cáo.
