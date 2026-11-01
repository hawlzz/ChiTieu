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

## Thiết kế giao diện 29/10/2026

- Giữ Backdrop 2.0.1 đã tích hợp; không nâng thư viện kính chỉ để đổi giao diện. Nguồn https://github.com/Kyant0/AndroidLiquidGlass , Apache-2.0 (THIRD_PARTY_BACKDROP_LICENSE.txt).
- Font Inter variable tải từ repository google/fonts/ofl/inter, đóng gói offline; SIL OFL 1.1 đính kèm design/INTER-OFL.txt. Nguồn https://github.com/google/fonts/tree/main/ofl/inter . Không dùng SF Pro hoặc Google Fonts runtime.
- Icon Material Icons Rounded từ dependency androidx.compose.material:material-icons-extended đã có; Apache-2.0, https://github.com/google/material-design-icons/blob/master/LICENSE . Không dùng SF Symbols. Launcher/notification do ChiTieu tự vẽ vector.
- Dấu + đặt ở header của Tổng quan/Giao dịch; thanh tab chỉ có ba điểm đến. Hàng Thêm nhanh trên Tổng quan giúp tiếp cận bằng nhãn rõ ràng.
- Nền nhóm đặc giúp nội dung dễ đọc; kính chỉ ở hero/tab/dialog. Không áp dụng kính cho từng giao dịch. Số thu/chi dùng màu đậm hơn trên nền sáng.
- Giữ cờ onboarded sẵn có trong DataStore; không đổi schema Room hoặc hợp đồng Repository. Người dùng cũ chưa có cờ sẽ thấy chào mừng một lần; xem lại không xoá dữ liệu.
- Splash dùng AndroidX core-splashscreen 1.2.0 từ Google Maven; Apache-2.0. Theo https://developer.android.com/develop/ui/views/launch/splash-screen/migrate .
- Android 17 bỏ InputManager.getInstance; bổ sung trực tiếp Espresso 3.7.0 (chỉ androidTest) để tránh dependency 3.6.1 bắc cầu từ Compose test. Nguồn https://developer.android.com/jetpack/androidx/releases/test ; không thay thư viện nghiệp vụ để sửa hạ tầng test.
- Không thêm GMS/Firebase/network permission. OCR ML Kit bundled từ bản v2 vẫn giữ nguyên; UI không chứng minh toàn bộ OCR chạy không GMS. Giới hạn này được giữ rõ trong báo cáo OCR lưu trữ cục bộ.

## PIN / Cài đặt / Thống kê 2.1
- Giữ nguyên tên DataStore, pinSalt/pinHash, Base64, PBKDF2-HMAC-SHA256 210.000 vòng / 256 bit. Thêm overload CharArray; giao diện mới dùng bộ đệm có thể xoá.
- Có nút Mở khoá; không lưu hoặc tiết lộ độ dài PIN cũ để tự gửi. PIN vẫn từ 6–12 số.
- Bộ đếm sai được ghi đồng bộ trong bộ nhớ riêng trước khi trả kết quả. Dùng elapsedRealtime + BOOT_COUNT; khởi động lại thiết bị thì bắt đầu lại mức chờ đang có, không dùng giờ tường để tránh lách bằng đổi ngày giờ.
- Sau 5 lần sai: 30 giây; lần 6: 1 phút; từ lần 7: 5 phút; chỉ PIN đúng đặt lại bộ đếm.
- Tự khoá mặc định 1 phút; vẫn khoá khi tạo tiến trình mới. Quay lại từ ngân hàng trong khoảng được chọn không làm mất trạng thái điều hướng; khoản PENDING vẫn ở Room và được hỏi lại sau mở khoá.
- Ẩn đa nhiệm và chặn chụp màn hình là hai tuỳ chọn độc lập; FLAG_SECURE mặc định tắt và bị bỏ qua trong debug.
- Giữ ngày bắt đầu tháng là ngày 1. Chưa thêm kỳ lương riêng vì sẽ thay đổi ngữ nghĩa bộ chọn tháng, báo cáo lịch sử và xuất dữ liệu; tránh đưa thêm cấu hình không đồng nhất.
- Biểu đồ tự vẽ bằng Canvas, không thêm thư viện/GMS. Tuần bắt đầu thứ Hai, múi giờ Asia/Ho_Chi_Minh. Kỳ trước của tháng/năm là tháng/năm lịch trước; Tuỳ chọn dùng cùng số ngày liền trước.
- Thống kê dùng SUM/COUNT/GROUP BY trong Room Flow. Migration 2→3 chỉ thêm chỉ mục, không tạo lại bảng. Danh sách chi tiết danh mục tải từng đợt 50 mục.
- App chưa có kiểu giao dịch chuyển ví nội bộ; không tự suy diễn từ tên ví/người nhận để loại khoản thu/chi.
- Chế độ Cả hai hiển thị tổng thu + chi và chênh lệch riêng, không gọi tổng luân chuyển là số dư. Dự báo chỉ hiện cho tháng hiện tại, ghi rõ ước tính.
- Các xác nhận HyperOS được ghi rõ là do người dùng xác nhận; không nhận là quyền đã được tự kiểm tra.

## Rà soát trước khi phát hành 2.1 (30/10/2026)
- Giữ các tính năng đang phát triển và schema Room 3. Không reset toàn bộ về 2.0 vì thiết bị đã nâng dữ liệu lên schema 3 không có migration hạ cấp.
- Khi xác nhận chuyển khoản, theo dõi trạng thái theo ID hiện tại. Một PENDING mới không được đóng sheet của khoản trước.
- Khoảng ngày dài được gộp thành tối đa 120 cột, có ghi số ngày mỗi cột; các tổng tiền vẫn dùng Long. Trục biểu đồ chứa được đường trung bình khi hiển thị cả thu và chi.
- Xoá dữ liệu và nạp lại ví/danh mục mặc định nằm trong một transaction. Chỉ dọn ảnh sau khi transaction thành công.
- README và danh mục APK quay về bản Git 2.0 đã phát hành. APK 2.1 trước rà soát đã được giữ ngoài repo; chỉ tạo lại ứng viên phát hành sau khi duyệt mã nguồn và bản kiểm tra mới.

## Tinh chỉnh UI trước lượt duyệt — 01/11/2026
- Dùng UI UX Pro Max cho touch target Android 48dp, selected semantics và tương phản; giữ Inter offline và hệ thống iOS-like đã có.
- Không áp dụng pattern Contact Sales/video landing page hoặc font web từ kết quả tìm kiếm tự động vì không phù hợp app tài chính Android.
- Không đổi dependency, schema, thuật toán tiền/PIN; không ký lại hoặc cập nhật release trong lượt UI này. Chủ dự án sẽ thử APK debug trước khi cho phép push.


## Android nhiều hãng và sinh trắc học trước PIN — 01/11/2026
- Phạm vi hỗ trợ vẫn là Android 13/API 33 trở lên. Không tuyên bố đã kiểm thử mọi hãng/phiên bản từ một AVD.
- Dùng AndroidX BiometricPrompt do Activity tạo trong onCreate; session trong ViewModel chỉ giữ trạng thái đang xác thực khi xoay màn hình, không ghi nhận quyền mở khoá qua process death. UI gắn lại callback và huỷ khi rời màn khoá.
- Khi người dùng đã bật sinh trắc học, thử BIOMETRIC_STRONG trước, hỗ trợ vân tay/khuôn mặt đủ chuẩn do Android quyết định. Không tự bật tuỳ chọn và không hạ xuống BIOMETRIC_WEAK để nhận mọi camera khuôn mặt.
- Huỷ, chọn Dùng PIN, không đăng ký, lỗi phần cứng hoặc tạm khoá sinh trắc học đều trở về PIN. Không tự bật lại prompt khi resume sau khi đã chọn PIN. Thời gian chờ do nhập sai PIN vẫn áp dụng cho cả hai cách.
- Giữ nguyên hash/salt/PIN hiện có và Room. Không xin quyền mới, không thêm dependency/GMS, không đổi mức bảo vệ của thuật toán PIN.
- Checklist bắt buộc phản ánh quyền và kết nối NotificationListener thực tế. Tối ưu pin/hướng dẫn theo hãng là tuỳ chọn khắc phục khi nhận thông báo bị ngắt; không dùng xác nhận thủ công để giả định kết nối hoạt động.
- Xiaomi/Redmi/POCO mới hiện hướng dẫn tự khởi động/giữ thẻ đa nhiệm; Samsung có hướng dẫn ứng dụng ngủ; hãng khác dùng hướng dẫn Android tổng quát. Không dựa vào intent nội bộ OEM. Thông tin ứng dụng là điểm vào ổn định; người dùng có thể bỏ qua mục không tồn tại.
- API tham khảo: https://developer.android.com/identity/sign-in/biometric-auth ; https://developer.android.com/reference/androidx/biometric/BiometricPrompt ; https://developer.android.com/topic/performance/background-optimization .

- Sửa lỗi yêu cầu điều hướng chạy trước NavHost ở lần cài mới: chờ hết onboarding rồi mới xử lý đường dẫn, ảnh chia sẻ và nút thiết lập; thêm hai test tái hiện khi Activity tạo mới.


## G2 triển khai native — 02/11/2026
- Thực hiện refinement từ preview G2 chủ dự án duyệt, giữ thiết kế/bố cục hiện có theo UI UX Pro Max. Primary vẫn là mực đủ đậm cho link/icon; CTA pastel dùng primaryContainer, tránh chữ nhạt trên nền nhạt.
- Dùng Lucide đã có trong runtime phát triển, trích 56 glyph cần thiết thành ImageVector. Không thêm dependency. ISC notice: design/LUCIDE-LICENSE.txt và res/raw/lucide_license.txt, có thể đọc tại Giới thiệu/giấy phép. Các notice Material/Backdrop cũ giữ nguyên.
- Launcher tĩnh và vector-only: gradient/lớp màu mô phỏng kính, không bê filter SVG Gaussian blur vào VectorDrawable. Độ sâu có thể khác PNG prototype; cần so trên launcher/ảnh native. Icon themed luôn đơn sắc theo Android.
- Bóng mềm xuyên suốt bề mặt; blur/lens chỉ tại các vùng kính hiện có để giữ mục tiêu máy tầm trung trở lên. Low-RAM/tiết kiệm pin và giảm hiệu ứng dùng lớp đơn giản. Chưa tuyên bố FPS hay tốc độ phần cứng khi chưa đo.
- Animation chỉ là UI: press trong graphicsLayer, tab Canvas, route transition. ContentObserver đọc trực tiếp animator_duration_scale để phản ứng khi đổi cài đặt Android; đăng ký/huỷ theo composition.
- Native vector nguồn Lucide: https://lucide.dev/license ; https://github.com/lucide-icons/lucide . API: https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/vector/PathParser .
