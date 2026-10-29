# ChiTieu — Hệ thống thiết kế

## Mục tiêu
Giao diện lấy cảm hứng từ bố cục nhóm và thứ bậc chữ của iOS, vẫn dùng hành vi điều hướng, quyền và bộ chọn ảnh Android. Không dùng tài sản Apple. Toàn bộ font/icon nằm trong APK; không có tải font qua mạng.

## Màu theo vai trò
| Vai trò | Sáng | Tối |
|---|---|---|
| background | #F2F2F7 | #000000 |
| card / surface | #FFFFFF | #1C1C1E |
| primary | #007AFF | #0A84FF |
| text | #000000 | #FFFFFF |
| secondary text | #636366 | #AEAEB2 |
| income | #187B36 | #30D158 |
| expense | #C62D24 | #FF453A |
| warning | #945600 chữ, #FF9500 điểm nhấn | #FF9500 |

Thu/chi ở nền sáng được làm đậm hơn màu trang trí để tăng độ tương phản. Trạng thái còn có nhãn Thu/Chi, không chỉ dựa vào màu. Card thường dùng màu đặc; chỉ hero/tab/dialog dùng kính. Tab lấy một lớp nội dung riêng để cuộn xuyên kính; lớp này được bỏ hoàn toàn khi giảm hiệu ứng.

## Chữ và hình khối
Inter variable đóng gói tại res/font/inter.ttf, SIL OFL 1.1 (design/INTER-OFL.txt). Cấu hình trục wght 400/600/700. Thang chữ: 40 số tổng; 34 tiêu đề lớn; 22 tiêu đề; 17 headline/body; 16 callout; 15 subhead; 13 footnote; 12 caption. AmountText dùng fontFeatureSettings tnum và giữ số nguyên Long ở kết quả cuối.

GlassTokens là nguồn chung cho lề 20dp, khoảng 8/12/16dp, góc card 24dp, control 14dp, input 12dp và padding đáy 104dp. Đơn vị sp tôn trọng cỡ chữ hệ thống. Trường dài xuống dòng/ellipsis theo vai trò; danh sách và sheet có cuộn.

## Thành phần
- LargeTitleHeader: tiêu đề 34sp chuyển sang 22sp khi danh sách cuộn; dấu + góc phải.
- GlassBottomBar: biểu tượng Rounded và nhãn, viên thuốc nổi; danh sách cuộn dưới thanh và có padding cuối.
- GlassCard/GlassDialog/GlassBottomSheet: Backdrop 2.0.1; sheet dùng grabber của Material 3.
- GroupedList/GroupedRow: nền nhóm, icon màu và separator thụt vào.
- SegmentedControl, PillButton, SearchBar, EmptyState, AmountText, CategoryIcon và Chip dùng chung.
- QuickAdd: bàn phím số riêng, tách nghìn khi hiển thị, lưới danh mục, ví và ghi chú; không thay Repository/Room.
- EntryDetailsSheet giữ đầy đủ người nhận/nội dung/mã biên lai; dòng gọn mở chi tiết, còn vuốt/giữ lâu để sửa hoặc xoá.
- Giao dịch: nhóm ngày, tổng ngày tính khoản confirmed, vuốt sửa/xoá; giữ lâu mở menu. Xoá vẫn cần xác nhận.

## Chuyển động và khả năng tiếp cận
Nút kính dùng spring nhẹ; đếm số dùng tiến trình 420ms, tính chính xác số nguyên khi hoàn tất. Chào mừng có pager; thiết lập animator_duration_scale=0 tắt các chuyển động tự thêm. Giảm hiệu ứng hoặc tiết kiệm pin bỏ blur/refraction và giảm chuyển động. QR thành công và thao tác chính có haptic theo cài đặt thiết bị.

Không xin camera/thông báo/SMS trong chào mừng. QR xin camera khi người dùng bấm; chọn ảnh vẫn dùng được khi không cấp camera. Chào mừng chỉ trình bày khả năng: chuyển khoản vẫn cần xác nhận kết quả khi quay về.

## Xác minh
Báo cáo UI-REPORT.md và ảnh đo đạc được giữ cục bộ trên máy phát triển. Screenshot trước/sau là dữ liệu cục bộ ở screenshots/, không tự đưa lên GitHub. Chưa có ảnh launcher HyperOS thật cho bộ icon mới.
