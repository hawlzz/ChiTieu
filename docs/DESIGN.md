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

## PIN, Cài đặt và Thống kê — 2.1
- IosSwitch: vùng chạm tối thiểu 48dp, trạng thái xanh lá, nhãn riêng cho TalkBack.
- SettingsRow: icon màu, tiêu đề có thể xuống dòng, chú thích và phần hành động tách riêng.
- SectionHeader/Footer, StatusCard/StatusIcon dùng để gom nhóm và thể hiện kết quả kiểm tra.
- PinDots chỉ giữ số lượng đã gõ; PinKeypad có nút số/xoá với testTag, haptic theo hệ thống.
- StepScaffold dùng trang cuộn toàn màn hình cho từng bước tạo/đổi/tắt PIN. ConfirmSheet dành cho hướng dẫn và xác nhận ngắn.
- Thống kê: bốn tab; biểu đồ Canvas trên thẻ màu đặc, chỉ hero dùng kính. Danh mục luôn có tên, tiền và phần trăm; biểu đồ thời gian có bảng số liệu thay thế.
- Biểu đồ dùng số thực chỉ để tính tọa độ vẽ; tiền và kết quả tính toán vẫn là Long, tỷ lệ/ước tính dùng số nguyên lớn khi nhân để tránh tràn trung gian.

## UI UX Pro Max — 01/11/2026
Master tại `design-system/chitieu/MASTER.md`. Nền nhóm xanh xám nhẹ, màu chữ theo vai trò rõ hơn, nút dùng đúng onPrimary/onError của theme. Các hàng Cài đặt có separator thụt vào; tab/segment có selected semantics và nhãn hỗ trợ chữ lớn. AmountText co theo chiều rộng dựa trên số cuối cùng, giữ cỡ chữ ổn định trong animation; hàng giao dịch xếp tiền xuống dưới ở màn hẹp/chữ lớn. Thanh tab và nội dung có chiều rộng tối đa, padding cuối danh sách tăng theo cỡ chữ. Bàn phím PIN chuyển sang phím tròn, ô sinh trắc không khả dụng được bỏ phần nền. Không đổi PIN hash, Room hoặc nghiệp vụ.

### Hoàn thiện luồng và số tiền
- Luồng tạo/đổi/tắt PIN tạm ẩn thanh tab trong chính NavHost, trả lại khi hoàn tất hoặc quay lại; màn quản lý khoá có padding cuối để không bị che nút sao lưu.
- Số tiền có tracking bằng 0, bố trí đủ chiều rộng và chừa biên cho làm tròn glyph; kiểm tra riêng số âm dài ở font 130% để ký hiệu tiền không xuống dòng lẻ.
- Màu secondary/secondaryContainer và surfaceVariant cũng được ánh xạ theo theme, tránh nền chip/progress tím mặc định lẫn vào giao diện xanh.


## Sinh trắc học trước PIN — 01/11/2026
- Màn khoá bắt đầu bằng icon và lời nhắc sinh trắc học khi đã bật và thiết bị hỗ trợ. Bàn phím PIN chỉ hiện khi người dùng chọn Dùng PIN hoặc sinh trắc học không dùng được.
- Tránh vòng lặp lời nhắc: tự gọi một lần cho mỗi lượt khoá, các lần thử lại do người dùng chọn. Giữ nút PIN dự phòng có nhãn rõ, vùng chạm theo GlassTokens, safeDrawing và bố cục ngang/dọc đã có.
- Đổi nhãn Mở bằng vân tay thành Mở bằng sinh trắc học, chú thích Ưu tiên trước PIN. Cập nhật khả năng đăng ký khi quay lại từ cài đặt Android.
- Kiểm tra thiết lập phân biệt trạng thái kết nối thật và trợ giúp chạy nền tuỳ chọn theo hãng; không buộc mọi Android thực hiện mục riêng HyperOS.


## G2 pastel glass native — 02/11/2026
Master hiện tại: design-system/chitieu/MASTER.md; bảng màu giai đoạn trước phía trên giữ làm lịch sử. Bản G2 dùng nền #EAF1F7/#152735, surface #F8FBFD/#253A4B, chữ #20394E/#EDF5FA và CTA pastel #C1D9EB/#365B77. Nguồn thật là pastelColorScheme/PastelTokens.
Thêm tonalSurface, softPress, FrostedIconBadge và BrandMark; GroupedList, nút, ô tìm kiếm, bàn phím, danh sách và icon dùng bóng mềm thống nhất. Không đặt blur sau từng hàng. AppIcons là 56 native vector Lucide, lazy/cache. Giữ font, cấu trúc, điều hướng, semantics và các luồng cũ; chỉ bổ sung transition nhẹ, tắt theo hệ thống.

G2 kiểm tra native: chip dùng nền tonal kín để bóng không xuyên qua thành xám; từ fontScale 1.5, hai ô Thu/Chi xếp dọc để số tiền không ngắt giữa nhóm chữ số. Cỡ chữ thường giữ hai cột.
