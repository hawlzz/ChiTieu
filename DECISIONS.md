# Quyết định cho phiên bản 2

## Giao diện

Dùng Backdrop 2.0.1 của Kyant0 (Apache-2.0), API `rememberLayerBackdrop` / `layerBackdrop` / `drawBackdrop`; không dùng Haze vì cấu hình hiện tại biên dịch được. Thành phần kính do dự án tự dựng; có attribution trong Giới thiệu. Hiệu ứng chỉ ở nền và các thành phần nổi, không ở từng giao dịch. `Giảm hiệu ứng` tắt blur/lens; tiết kiệm pin tự chọn lớp phủ đơn giản.

Nâng minSdk từ 26 lên 33 để dùng RuntimeShader cho khúc xạ. compileSdk 37, targetSdk 35; Kotlin 2.4.21 và Compose BOM 2026.09.00 giữ nguyên. Phiên bản tra qua metadata Maven ngày 26/10/2026, không tự động nâng khi build.

Nguồn API và giấy phép: https://github.com/Kyant0/AndroidLiquidGlass

## OCR và quyền riêng tư

Dùng `com.google.mlkit:text-recognition:16.0.1` **bundled**, model trong APK. Không cấu hình tải model động. Manifest loại quyền INTERNET. Không dùng Firebase hoặc gửi ảnh lên dịch vụ. ML Kit vẫn kéo các thư viện trợ giúp `play-services-base`, `play-services-basement` và `play-services-mlkit-text-recognition` theo dependency graph; không thể gọi cấu hình này là “không có thư viện Google”. Artifact khai báo trực tiếp là bundled theo lựa chọn được prompt cho phép.

Nguồn: https://developers.google.com/ml-kit/vision/text-recognition/v2/android

Đã xử lý đủ 30 ảnh trên Xiaomi ngày 28/10/2026. Máy thực tế Android 16, có Google Play Services; **chưa xác minh môi trường không GMS**. Không tắt hay gỡ dịch vụ hệ thống để giả lập. Chưa có nhãn được xác minh nên chưa chọn engine dựa trên độ chính xác, cũng chưa có so sánh Tesseract. Nếu cần điều kiện tuyệt đối không có dependency Google, phải thay engine và đo lại; đây là giới hạn còn mở.

30 ảnh Techcombank chia cố định 21 phát triển / 9 kiểm tra, seed 20261026. OCR không phải ground truth. `scripts/score_receipts.py` chỉ chấm dòng `verified=true`, không ghi số tiền, tên hay mã giao dịch vào báo cáo. Ô trống không được chấm; `<EMPTY>` biểu thị trường đã xác minh không có. Chỉ dùng tập phát triển để sửa quy tắc. Confidence là heuristic, chưa hiệu chuẩn.

Photo Picker không yêu cầu đọc toàn bộ thư viện. Chia sẻ ảnh dùng content URI. Mặc định giữ ảnh tắt; chỉ bitmap trong bộ nhớ được xử lý, giới hạn 20 MB / 60 MP và cạnh dài 2560. Khi hủy tác vụ, chờ ML Kit hoàn tất trước khi giải phóng bitmap. Lưu ảnh tùy chọn trong thư mục riêng; ảnh không có trong backup JSON. Metadata biên lai trong Room vẫn có thể xuất cùng backup JSON.

## Dữ liệu và kiểm thử

Room v1 → v2 chỉ thêm bảng `receipt_evidence`, không xóa bảng cũ. Debug dùng `vn.chitieu.debug` tách khỏi bản chính `vn.chitieu`. Harness và quyền chấp nhận shell nằm trong source set debug; release có implementation từ chối shell.

ADB không chạy chuyển tiền. Deep link chỉ mở app. Các thao tác tạo/xóa kiểm thử chỉ chạm giao dịch `test-*` hoặc package shell trong bản debug. Ảnh thử và kết quả chi tiết đều bị gitignore. Dùng stdin nhị phân qua `adb exec-in run-as` thay vì thư mục ngoài do FUSE trên HyperOS đã từ chối app đọc ảnh shell tạo. Dọn ảnh sau test bằng danh sách alias cố định.

Thông báo ngân hàng thật còn thiếu fixture đã xác minh, nên parser sản xuất vẫn trả null. Tự thêm + hoàn tác đã triển khai nhưng thử shell chỉ chứng minh pipeline mẫu, không chứng minh thông báo ngân hàng thật. Force-stop theo Android sẽ dừng component; kiểm tra khôi phục sau mở lại phải phân biệt với sống nền khi không force-stop.

APK release v2 unsigned: 48.769.012 bytes, so với v1 3.537.607 bytes (tăng 45.231.405 bytes cho toàn bộ thay đổi/dependency v2, không phải phép đo riêng ML Kit). Model đóng gói là đánh đổi dung lượng để không cần tải model lúc sử dụng.
