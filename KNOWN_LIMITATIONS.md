# Hạn chế đã biết

- Chưa có thiết bị Android kết nối (`adb devices -l` rỗng). Chưa xác nhận camera, picker ảnh, vân tay, vòng đời nền, WorkManager và UI trên Xiaomi 15/HyperOS 2. Không coi build/lint là kiểm thử thiết bị.
- Chưa có mẫu thông báo/SMS thật. 28 parser theo ngân hàng trả về null có chủ đích; pipeline nhập, chống trùng và chờ duyệt đã có mã/test, nhưng chưa có tính năng tự ghi thực tế cho một ngân hàng nào. Parser SIMULATED chỉ để thử, không hoạt động trong listener sản xuất.
- Chỉ 5 package app ngân hàng được đối chiếu nguồn phát hành. 71 mục khác chưa có package xác minh; không xuất hiện trong bộ chọn app hoặc danh sách bật đọc thông báo. BIN/mã ngân hàng có nguồn API, không đồng nghĩa app hỗ trợ deep link.
- Chưa kiểm chứng app không có GMS có nhận FCM hay chạy được trên ROM nội địa. Không có thông báo đến máy thì listener không thể tự đọc; dùng ghi thủ công. SMS cũng cần parser/mẫu thực tế trước khi hữu dụng.
- VietQR.io hiện tài liệu hoá thêm tham số `url` bắt buộc trong deep link. Builder giữ mẫu yêu cầu gốc không có callback chưa xác minh. Nhiều app có thể không nhận HTTPS đó với setPackage; khi không resolve sẽ sao chép tài khoản/mở app. Nếu app nhận intent nhưng không tự điền, người dùng nhập tay từ thông tin giao dịch chờ. Không coi callback là bằng chứng thanh toán.
- QRIBFTTC được giải mã nhưng chỉ dùng copy/mở app để chuyển tới thẻ; chưa có deep link tới thẻ được xác minh. Không đổi thẻ thành tài khoản để điền sai.
- Tên người nhận chỉ lấy từ QR; không gọi API xác minh chủ tài khoản. QR có thể bỏ tên, người dùng phải đối chiếu tại ngân hàng.
- Hướng dẫn HyperOS dùng intent hệ thống công khai, fallback Thông tin ứng dụng; Autostart, popup nền, khoá đa nhiệm và Restricted settings cần thao tác thủ công theo ROM. Không dựa foreground service để giữ listener.
- Trạng thái lần thấy thông báo lưu trong RAM theo phiên, không phải nhật ký đầy đủ. Log thô riêng chỉ lưu khi được phép, giới hạn 100, không được đưa vào bản sao lưu.
- Chống trùng theo ngân hàng, chiều giao dịch, số tiền, nội dung chuẩn hoá và phút. Hai giao dịch thật giống hệt nhau trong cùng phút có thể bị coi là trùng. Nếu thông báo/SMS cho cùng giao dịch có nội dung/thời gian khác nhau, có thể cần duyệt thủ công. Nhiều QR cùng tiền/bank trong 10 phút không tự đoán giao dịch nào; đưa bản tin mới vào luồng duyệt.
- PENDING quá 24 giờ được chuyển Cần xem lại khi mở app hoặc worker chạy. HyperOS có thể hoãn worker; không đảm bảo nhắc đúng thời điểm 24 giờ. Không tạo notification nhắc riêng.
- PIN/vân tay khoá giao diện; DB chưa mã hoá. FLAG_SECURE hạn chế screenshot/preview. Throttling PIN 5 lần/30 giây chỉ trong phiên, không thay thế bảo mật hệ điều hành.
- Sao lưu JSON/CSV chưa mã hoá, tối đa 16 MB khi nhập, 50.000 giao dịch. CSV chỉ nhận định dạng ChiTieu v1; không nhập tự động file bất kỳ của ngân hàng. Khôi phục JSON thay dữ liệu sau xác nhận; không khôi phục PIN, quyền hệ thống, log hoặc sở thích giao diện.
- Chưa có quản lý ngân sách theo danh mục; nằm ở giai đoạn sau. Không có đồng bộ cloud, OCR hoá đơn, FCM hay SMS history.
- Cảnh báo deprecated: opt-out Kotlin tích hợp/new DSL để dùng plugin theo cấu hình yêu cầu; cần di trú trước AGP 10. targetSdk 35 được giữ theo yêu cầu nên lint cảnh báo OldTargetApi; một số dependency/công cụ mới hơn nằm ngoài tổ hợp Kotlin đã xác nhận nên không tự nâng.
- Trong môi trường thực thi bị giới hạn, AAPT2 có thể crash native kể cả app tối thiểu. Người dùng đã chạy build ngoài môi trường này thành công. Log thử lỗi được giữ ở `D:\ChiTieu\evidence`, không đưa vào GitHub.

## Giới hạn bổ sung v2

- minSdk 33; máy Android 12 trở xuống không cài bản này.
- OCR đã chạy trên 30 ảnh Techcombank nhưng nhãn chưa xác minh, nên chưa có số liệu accuracy. Các ngân hàng khác chưa có bộ ảnh.
- ML Kit bundled vẫn có dependency Google helper; không có quyền INTERNET, nhưng máy không GMS chưa được kiểm chứng.
- Thời gian, mã giao dịch và tên có thể thiếu/sai; luôn kiểm tra trước khi lưu. Confidence không phải xác suất.
- Ảnh lưu tùy chọn chưa có trình xem riêng trong app; metadata có trên giao dịch. Ảnh không đi theo backup; CSV v1 không xuất metadata biên lai. Khôi phục có thể để lại ảnh cũ không còn tham chiếu trong bộ nhớ riêng.
- Deep link và thông báo ngân hàng thật còn chờ kiểm chứng. Xem TESTING-REPORT.md để biết kết quả thiết bị và các bước bị chặn.
