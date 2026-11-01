# Thay đổi — 2.1.0

- Tinh chỉnh UI: màu sáng/tối dễ đọc hơn, thẻ nhóm rõ ràng và phím PIN tròn.
- Tab/bộ chọn có trạng thái truy cập; tìm kiếm có nút xoá, số tiền thích ứng với chiều rộng và chữ lớn.
- Bố cục màn nhỏ/ngang/tablet và khoảng trống cuối danh sách thích ứng với cỡ chữ.

- Nâng cấp mở khoá, tạo/đổi/tắt PIN thành các bước riêng với bàn phím số và phản hồi lỗi.
- Thêm thời gian tự khoá, ẩn đa nhiệm và tuỳ chọn chặn chụp màn hình độc lập.
- Gom Cài đặt thành các nhóm và trang con; checklist có trạng thái và hướng dẫn ngắn.
- Thêm tab Thống kê: kỳ tuần/tháng/năm/tuỳ chọn, lọc thu chi/ví, danh mục, dòng thời gian, xu hướng và nhận xét cục bộ.
- Bổ sung dữ liệu giả và kịch bản kiểm tra cho bản debug.

---
# Thay đổi — 2.0.0 (đang kiểm thử)

- Trang chính: Quét QR, Thêm nhanh, Đọc ảnh biên lai; danh sách đầy đủ ở tab Giao dịch.
- Theme sáng/tối/theo hệ thống; thành phần kính Backdrop; giảm hiệu ứng thủ công và theo tiết kiệm pin.
- OCR bundled chạy cục bộ, Photo Picker và chia sẻ ảnh; kiểm tra/sửa trước lưu; gộp giao dịch gần giống, PENDING và ảnh trùng.
- Metadata biên lai, lưu ảnh tùy chọn mặc định tắt; migration Room 1 → 2 giữ giao dịch cũ.
- Ghi nhớ app trả tiền theo ngân hàng nhận, tự chọn ví; tự ghi nhận thông báo kèm hoàn tác; vẫn có chế độ chờ duyệt.
- Checklist thiết lập chỉ hiện bước chưa hoàn thành; trạng thái riêng HyperOS được ghi rõ khi cần tự xác nhận.
- Nâng minSdk 26 → 33 (Android 13) cho khúc xạ. Bản debug có hậu tố `.debug` để tách dữ liệu thử.

## Mục đã ẩn hoặc chuyển

- Bộ lọc trên Tổng quan thu gọn; luôn có ở trang Giao dịch.
- SMS, log thông báo, danh mục/quy tắc, backup/CSV, giảm hiệu ứng chuyển vào Nâng cao.
- Bộ nhập thông báo giả rời giao diện sử dụng; chỉ còn harness debug và test.
- Hộp thoại lỗi chung thay bằng snackbar. PENDING vẫn yêu cầu xác nhận khi chưa có bằng chứng chắc chắn.
- Không xóa chức năng sửa giao dịch, backup, khóa ứng dụng, CSV hay luồng VietQR cũ.

## Còn cần xác minh

Độ chính xác OCR từng trường cần labels.csv được người dùng duyệt; máy không GMS chưa được thử; parser ngân hàng thật chưa có mẫu. Xem OCR-REPORT.md và TESTING-REPORT.md để phân biệt kết quả thực tế với phần bị chặn.

## Giao diện mới — 29/10/2026
- Thêm hệ thống màu/chữ Inter đóng gói offline, icon Rounded, component nhóm và kính có giới hạn.
- Thay thẻ tên app bằng tiêu đề Tổng quan + nút thêm; thêm chọn tháng, swipe tháng, hero Thu/Chi và trạng thái rỗng có hành động.
- Giao dịch nhóm ngày, tìm kiếm, chip/segment và thao tác vuốt/giữ lâu; giữ xác nhận xoá.
- Thêm nhanh có bàn phím số, danh mục, ví và ghi chú. QR có khung quét và điều khiển ảnh/đèn; biên lai có thumbnail và gợi ý kiểm tra.
- Thêm adaptive/round/monochrome launcher, small notification icon, SplashScreen API và ba trang chào mừng có thể xem lại.
- Giữ Room schema 2, parser, Repository và luồng xác nhận thanh toán; không thêm dữ liệu thật hay quyền mạng.

- Giữ tra cứu đầy đủ thông tin người nhận, nội dung và mã biên lai bằng sheet Chi tiết giao dịch; bổ sung test hồi quy cho metadata.


## Chưa phát hành — Android nhiều hãng & sinh trắc học
- Ưu tiên sinh trắc học mạnh trước PIN; có PIN dự phòng, không hỏi lại liên tục sau khi huỷ.
- Gắn vòng đời lời nhắc với Activity, giữ lượt xác thực khi đổi cấu hình, không lưu quyền mở khoá qua khởi động lại tiến trình.
- Checklist thiết lập Android chuẩn, hướng dẫn chạy nền theo hãng; bỏ yêu cầu HyperOS trên các hãng khác.
- Đổi nhãn sinh trắc học và cập nhật khả năng đăng ký sau khi quay lại cài đặt điện thoại.

- Sửa lỗi yêu cầu điều hướng chạy trước NavHost ở lần cài mới: chờ hết onboarding rồi mới xử lý đường dẫn, ảnh chia sẻ và nút thiết lập; thêm hai test tái hiện khi Activity tạo mới.


## Chưa phát hành — G2 pastel glass native
- Màu pastel xanh G2 sáng/tối, bóng mềm và viền kính dùng chung cho bề mặt, nút, input, bàn phím và icon.
- Bộ icon nét Lucide native thống nhất; mapping danh mục rõ, fallback Shapes.
- Icon ứng dụng ví/dấu ghi nhận G2, splash/monochrome/notification đồng bộ; BrandMark ở màn khoá và chào mừng.
- Press feedback, chuyển màn nhẹ và tự cập nhật giảm chuyển động từ Android; giữ đường giảm hiệu ứng cho low-RAM/tiết kiệm pin.
