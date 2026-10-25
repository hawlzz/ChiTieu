# ChiTieu

Ứng dụng Android quản lý thu chi cá nhân bằng tiếng Việt, chạy offline, dành cho APK cài trực tiếp và thiết bị không có Google Play Services.

**Trạng thái:** đã build APK debug, chạy unit test và Android Lint qua từng giai đoạn. Chưa có Xiaomi/thiết bị Android kết nối để kiểm thử camera, vân tay, HyperOS và deep link ngân hàng. Parser thông báo/SMS đang là **khung chờ mẫu thật**, chưa tự ghi giao dịch từ thông báo.

## Tính năng

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

Cần JDK 17 và Android SDK. `minSdk 26`, `targetSdk 35`, compile SDK 37. Tiền dùng số nguyên VND; ngày giờ theo `Asia/Ho_Chi_Minh`.

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
