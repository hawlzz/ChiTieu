# ChiTieu — UI Master G2

Ngày cập nhật 02/11/2026. Nâng cấp G2 pastel glass theo bản preview được chủ dự án chọn, giữ Jetpack Compose, bố cục iOS-like hiện tại, điều hướng và dữ liệu cục bộ.

## Nguồn và phạm vi
UI UX Pro Max: ưu tiên khả năng tiếp cận, vùng chạm Android 48dp, tương phản và sự thống nhất. Search animation/performance trả đúng Compose APIs và UI-only state; search contrast xác nhận mốc 4.5:1. Không chạy lại design-system hoặc ép đổi giao diện theo một mẫu khác.

## Màu
| Vai trò | Sáng | Tối |
|---|---|---|
| Nền | #EAF1F7 | #152735 |
| Bề mặt | #F8FBFD | #253A4B |
| Chữ chính | #20394E | #EDF5FA |
| Chữ phụ | #546D80 | #B9CBD9 |
| Primary — mực cho icon/link | #416B8C | #B7D5E9 |
| Primary container — CTA/tab pastel | #C1D9EB | #365B77 |
| Chữ trên CTA pastel | #254761 | #E8F4FC |
| Thu | #25724E | #8ED6B3 |
| Chi | #B33E50 | #F4A3AC |

Không dùng pastel nhạt làm chữ chính hoặc màu báo lỗi. PillButton chính dùng primaryContainer/onPrimaryContainer; Material Button mặc định vẫn có primary/onPrimary đủ tương phản. Font Inter offline, cỡ chữ và định dạng số tiền hiện tại được giữ.

## Bề mặt và icon
- GroupedList/card danh sách có gradient tĩnh rất nhẹ, viền sáng và bóng mềm; không thêm Backdrop vào từng dòng.
- PastelTokens gom elevation: card 10dp, control 6dp, badge 5dp, vùng nổi 14dp. Giảm hiệu ứng/tiết kiệm pin/thiết bị low-RAM giảm elevation; kính/lens vẫn chỉ ở các vùng GlassCard/tab hiện có.
- 56 glyph Lucide outline, khung 24, nét 2 và round cap/join. Chuyển thành ImageVector và cache lazy; không có runtime JS hoặc dependency icon mới.
- FrostedIconBadge dùng chung cho danh mục/Cài đặt; điều chỉnh màu hiển thị của icon quá nhạt để đạt ≥3:1 trên nền badge, không đổi màu lưu trong dữ liệu.
- Danh mục mặc định map tường minh, xử lý Unicode/các cách viết Hoá/Hóa; danh mục tự đặt dùng Shapes, không đoán theo chuỗi con hoặc gán nhầm ví/xe.
- Launcher ví/dấu ghi nhận G2 bằng vector gradient/lớp màu tĩnh. Monochrome/small notification giữ ngôn ngữ hình, không raster hoặc shader mới. BrandMark hiển thị nhiều màu đúng asset trong khoá/chào mừng.

## Chuyển động và bố cục
- CTA có press scale .97 đọc trong graphicsLayer; tab trượt hiện có, số tiền đếm, sheet native và haptic hiện có được giữ. Route enter 280ms, exit 140ms, dịch nhẹ 12dp; không animate database/VM.
- LocalReduceMotion theo đổi animator_duration_scale ngay khi app mở; Power Saver/low-RAM/giảm hiệu ứng vẫn có đường hiển thị đơn giản. Không tự thêm chuyển động lặp lên danh sách.
- Giữ lề 20dp, card 24dp, touch 48dp, content max720dp/tab max560dp, padding cuối danh sách và xử lý chữ lớn/landscape trước đây. PIN đang tạo/đổi/tắt tiếp tục ẩn tab.
- Không đổi Room/PIN hash/thuật toán tiền/quyền/camera decoder/ngân hàng hoặc xác nhận PENDING. Prototype chỉ là hướng thị giác; dữ liệu giả của prototype không được đưa vào APK sản phẩm.

## Kiểm tra và phát hành
PastelPaletteTest kiểm tra các cặp chữ chính/phụ/link/nút trên sáng/tối ≥4.5:1, icon ≥3:1, mapping và 56 vector parse được. PastelMotionTest kiểm tra cập nhật giảm chuyển động Android khi app đang mở. Việc quan sát PNG và đo máy thật được ghi trong G2-NATIVE-REPORT.md, không suy ra từ token.
Theo yêu cầu bổ sung 02/11/2026, sau kiểm tra đăng pre-release 2.1.0-beta.1 kèm APK ký bằng khoá hiện có; khoá ký giữ riêng trên máy.

G2 kiểm tra native: chip dùng nền tonal kín để bóng không xuyên qua thành xám; từ fontScale 1.5, hai ô Thu/Chi xếp dọc để số tiền không ngắt giữa nhóm chữ số. Cỡ chữ thường giữ hai cột.
