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
