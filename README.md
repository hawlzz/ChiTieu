# ChiTieu

Ứng dụng Android quản lý thu chi cá nhân bằng tiếng Việt, chạy offline, dành cho APK cài trực tiếp. OCR bundled đã chạy trên Xiaomi Android 16; khả năng chạy không GMS còn cần xác minh.

**Phiên bản 2 đang kiểm thử:** giao diện kính, đọc ảnh biên lai, thêm nhanh và cài đặt gọn hơn. Build debug/release, 59 unit test, lint và 11 test thiết bị đã qua. Đã chạy OCR 30 ảnh trên Xiaomi thật, chưa có nhãn được người dùng xác minh để tính độ chính xác. Parser thông báo/SMS ngân hàng thật vẫn chờ mẫu. Kết quả chi tiết ở [OCR-REPORT.md](OCR-REPORT.md) và [TESTING-REPORT.md](TESTING-REPORT.md).

## Tính năng

- Đọc ảnh biên lai bằng Photo Picker hoặc chia sẻ ảnh; sửa kết quả trước khi xác nhận, gợi ý gộp để tránh trùng. Giữ ảnh mặc định tắt.
- Giao diện sáng/tối/theo hệ thống với Backdrop; giảm hiệu ứng theo tùy chọn hoặc tiết kiệm pin.
- Trang chính ba hành động; SMS, log và sao lưu nằm trong Nâng cao.

- Thêm, sửa, xoá thu/chi; nhiều ví; danh mục có biểu tượng, màu và quy tắc từ khoá.
- Tổng quan tháng: tổng thu, tổng chi, chênh lệch; biểu đồ Canvas theo danh mục; tìm kiếm và lọc ngày, tháng, ví, danh mục, trạng thái.
- Quét QR bằng CameraX + ZXing, bật đèn hoặc chọn ảnh; giải mã TLV VietQR/NAPAS, kiểm tra CRC16/CCITT-FALSE.
- Xác nhận người nhận, tiền, nội dung, ví/danh mục; mở app ngân hàng đã cài bằng deep link có đặt package, hoặc sao chép tài khoản và mở app.
- Lưu PENDING trước khi mở ngân hàng. Khi quay lại: Đã chuyển / Chưa chuyển, Huỷ / Hỏi lại sau. Khoản chờ quá 24 giờ vào Cần xem lại.
- NotificationListenerService có danh sách ngân hàng được bật riêng; trạng thái kết nối, nút rebind và ghi nhận lần thấy thông báo trong phiên.
- Pipeline chống trùng và đối chiếu QR trong 10 phút; xác nhận khi chỉ có một giao dịch phù hợp. Chờ duyệt hoặc tự thêm sau khi parser thực tế được bổ sung.
- SMS Banking mặc định tắt. Danh sách đầu số và parser chưa được xác minh nên hiện bỏ qua SMS.
- Log thô chỉ bật sau khi người dùng đồng ý, lưu tối đa 100 thông báo của ngân hàng được bật, có nút xoá. Parser thử dùng cú pháp `[SIMULATED]`, không lưu giao dịch và không gắn vào listener sản xuất.
- CSV theo định dạng ChiTieu v1, sao lưu/khôi phục JSON qua bộ chọn file hệ thống. Khôi phục có bước xác nhận thay dữ liệu.
- Khoá PIN 6–12 số, PBKDF2-HMAC-SHA256 với salt riêng; hỗ trợ sinh trắc học mạnh khi thiết bị cung cấp. Giao diện tối và hướng dẫn HyperOS khi mở lần đầu.

## Bắt đầu

Cần JDK 17 và Android SDK. `minSdk 33`, `targetSdk 35`, compile SDK 37. Tiền dùng số nguyên VND; ngày giờ theo `Asia/Ho_Chi_Minh`.

```powershell
# Trên máy đã được chuẩn bị trong D:\ChiTieu
. D:\ChiTieu\env.ps1
cd D:\ChiTieu\project
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lint
```

Nếu chính sách PowerShell không cho chạy env.ps1, đặt biến trực tiếp, không cần thay chính sách hệ thống:

```powershell
$env:JAVA_HOME='D:\ChiTieu\tools\java\jdk-17.0.20.1+1'
$env:ANDROID_HOME='D:\ChiTieu\tools\sdk'
$env:ANDROID_USER_HOME='D:\ChiTieu\tools\android-user'
$env:GRADLE_USER_HOME='D:\ChiTieu\tools\gradle-home'
cd D:\ChiTieu\project
.\gradlew.bat assembleDebug testDebugUnitTest lint
```

Khi clone trên máy khác, đặt các biến theo môi trường của bạn và tạo `local.properties` chứa `sdk.dir=...`. Không commit file này. Dùng `./gradlew` trên macOS/Linux.

APK debug ở `app/build/outputs/apk/debug/app-debug.apk`:

```powershell
& "$env:ANDROID_HOME\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

Hướng dẫn chi tiết: [SETUP.md](SETUP.md) · [TESTING.md](TESTING.md) · [KNOWN_LIMITATIONS.md](KNOWN_LIMITATIONS.md) · [TODO_VERIFY.md](TODO_VERIFY.md) · [VALIDATION.md](VALIDATION.md).

## Dữ liệu ngân hàng và tính riêng tư

`app/src/main/assets/banks.json` có 76 BIN/mã từ API VietQR. BVBank và Viet Capital Bank được ghi là tên/alias của cùng một mục, không tạo ngân hàng giả trùng BIN. Package của Vietcombank, VietinBank, BIDV, MB và Techcombank đã đối chiếu trang Google Play của bên phát hành; vẫn cần kiểm tra bản cài trên Xiaomi bằng adb. Package khác để `null` và không được dùng để đọc thông báo hoặc mở app.

Tên người nhận trong QR **không phải kết quả xác minh chủ tài khoản**. Hãy đối chiếu trong app ngân hàng. ChiTieu không yêu cầu mật khẩu/OTP ngân hàng, không tự thực hiện hay xác nhận tiền đã chuyển chỉ vì mở app thành công.

Không có quyền INTERNET, Firebase, Play Services, quảng cáo hay analytics. Cơ sở dữ liệu ở bộ nhớ riêng của app, `allowBackup=false`; **chưa mã hoá DB bằng SQLCipher**. Khoá PIN là khoá giao diện, không thay thế mã hoá thiết bị. File CSV/JSON xuất ra chứa dữ liệu tài chính dạng rõ; người dùng tự chọn nơi lưu. Không đưa dữ liệu thật, log thô hoặc keystore vào repository.

Deep link HTTPS được giao cho package ngân hàng đã chọn; ChiTieu không tự gọi mạng. App ngân hàng hoặc dịch vụ deep link bên ngoài có hành vi mạng riêng. Thiếu handler sẽ dùng clipboard/launch intent. Chưa bật deep link riêng chưa được chứng minh.

## Kiến trúc

Kotlin, Compose Material 3, MVVM, Room, Hilt/KSP, Coroutines/Flow, DataStore, WorkManager, Navigation Compose. Không dùng ML Kit; biểu đồ tự vẽ bằng Canvas.

```text
app/src/main/java/vn/chitieu/
  data/          Room, repository, preferences, CSV/backup, worker
  domain/        kiểu giao dịch, tiền VND, thời gian, PIN
  ui/            ViewModel, sổ thu chi, QR, cài đặt, bảo mật
  qr/            TLV, CRC, ZXing, bank registry
  bank/          URL builder và mở app/fallback
  notification/ listener, SMS receiver, parser, chống trùng
```

Phiên bản plugin/thư viện quản lý tại `gradle/libs.versions.toml`; Gradle Wrapper có checksum phân phối. Không cần Gradle cài toàn hệ thống. Xem VALIDATION.md để biết lệnh nào đã chạy và phần nào chưa có thiết bị để kiểm thử.

## Bổ sung parser thực tế

1. Bật ngân hàng và quyền truy cập thông báo; tự đồng ý bật log cục bộ nếu cần thu thập mẫu.
2. Thu thập thông báo thực tế trên thiết bị; che tài khoản, số dư và thông tin cá nhân trước khi chia sẻ mẫu.
3. Thay parser tương ứng trong `notification/parsers/`, thêm fixture đã ẩn danh và unit test thu/chi, số tiền, số dư, thời gian, thông báo không phải giao dịch.
4. Chỉ đăng ký quy tắc SMS sau khi xác minh đầu số/brandname. Không suy diễn OTP hoặc số dư thành số tiền giao dịch.
5. Chạy test + lint, kiểm thử trên Xiaomi, rồi mới bật tự thêm giao dịch.

Ngân sách theo danh mục được để cho giai đoạn tiếp theo như yêu cầu gốc.

## Phát triển và kiểm thử v2

Debug dùng applicationId vn.chitieu.debug, tách khỏi bản chính. Room tự nâng schema 1 → 2. Không chạy thử bằng dữ liệu thật trong app chính.

Script PowerShell: scripts/adb_test.ps1 -Serial '<kết nối từ adb devices>'. Kiểm thử Compose/Room: .\gradlew.bat connectedDebugAndroidTest. Cách chuẩn bị môi trường, giới hạn HyperOS và ký release ở SETUP.md. Quyết định engine, dependency Google và minSdk ở DECISIONS.md; thay đổi giao diện ở CHANGELOG.md.

Ảnh nguồn, nhãn, OCR chi tiết và ảnh chụp màn hình chỉ lưu cục bộ, không đưa vào git hoặc APK. Không dùng OCR làm ground truth. Chấm các nhãn được duyệt bằng scripts/score_receipts.py.

## Giao diện mới
Hệ thống màu/chữ, component và nguyên tắc được mô tả trong [DESIGN.md](DESIGN.md). Xem [design/ICON-OPTIONS.md](design/ICON-OPTIONS.md) cho ba hướng icon. Inter được đóng gói offline (SIL OFL); không cần Google Fonts runtime. Bộ ảnh trước/sau và REVIEW.html dùng đường dẫn cục bộ, không đưa dữ liệu kiểm thử lên GitHub.

## APK release
APK đã ký được lưu tại [releases/ChiTieu-2.0.0-release.apk](releases/ChiTieu-2.0.0-release.apk); mã SHA-256 đi kèm trong releases/SHA256SUMS.txt. Khoá ký riêng được giữ ngoài repository trên máy phát triển.
