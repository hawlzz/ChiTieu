# ChiTieu — Android offline

Dự án tại `D:\ChiTieu\project`. Công cụ riêng tại `D:\ChiTieu\tools`.

## Môi trường

Không có JDK/adb/sdkmanager/Gradle trên PATH khi kiểm tra ban đầu. Đã tải JDK Temurin 17.0.20.1+1, Gradle 9.7.0 (chỉ dùng khởi tạo wrapper), command-line tools 23.0 và 19.0. Bản 23 gặp lỗi giải nén CLI trong sandbox; bản 19 dùng SDK Manager Java. Đã chấp nhận license SDK bằng SDK Manager. SDK platform 35, 37.0, build-tools 36.0.0 và platform-tools cài tại tools/sdk. Không cài Android Studio, emulator, Firebase, Play Services, ML Kit, analytics hay công cụ ngoài danh sách yêu cầu.

AGP 9.3.1 và Kotlin 2.4.21 được chọn theo bảng tương thích Kotlin; Gradle 9.7.0 nằm trong phạm vi hỗ trợ. AGP 9.4.1 mới hơn nhưng nằm ngoài phạm vi AGP đã được Kotlin 2.4.21 kiểm chứng. Hai cờ opt-out Kotlin tích hợp/new DSL giữ plugin Kotlin Android/Hilt theo cấu hình yêu cầu; có cảnh báo deprecated, cần di trú trước AGP 10.

Phiên bản đầy đủ: `gradle/libs.versions.toml`; bằng chứng truy vấn Maven tại `../evidence/versions.json`, `agp.xml`, `kotlin.xml`, `hilt.xml`, `ksp.xml`. Dependencies được khóa phiên bản cụ thể, không dùng dấu +.

Nguồn: https://kotlinlang.org/docs/gradle-configure-project.html ; https://developer.android.com/build/releases/gradle-plugin ; https://dl.google.com/android/repository/repository2-1.xml ; https://api.adoptium.net/v3/assets/latest/17/hotspot?architecture=x64&image_type=jdk&os=windows

## Build

PowerShell:

```powershell
. D:\ChiTieu\env.ps1
cd D:\ChiTieu\project
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lint
```

Trên máy khác: cài JDK 17 và Android SDK, đặt JAVA_HOME, ANDROID_HOME, sửa local.properties theo đường dẫn SDK; wrapper tự tải Gradle. Unix: `chmod +x gradlew; ./gradlew assembleDebug testDebugUnitTest lint`.

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Debug key chỉ dùng phát triển.

## Ký release

Tạo khóa riêng; tự nhập mật khẩu tương tác, giữ khóa và mật khẩu ở nơi an toàn. Không đưa vào git.

```powershell
keytool -genkeypair -keystore D:\ChiTieu\chitieu-release.jks -alias chitieu -keyalg RSA -keysize 3072 -validity 10000
.\gradlew.bat assembleRelease
& "$env:ANDROID_HOME\build-tools\36.0.0\zipalign.exe" -p -f 4 app\build\outputs\apk\release\app-release-unsigned.apk D:\ChiTieu\chitieu-aligned.apk
& "$env:ANDROID_HOME\build-tools\36.0.0\apksigner.bat" sign --ks D:\ChiTieu\chitieu-release.jks --ks-key-alias chitieu --out D:\ChiTieu\ChiTieu-release.apk D:\ChiTieu\chitieu-aligned.apk
& "$env:ANDROID_HOME\build-tools\36.0.0\apksigner.bat" verify --verbose D:\ChiTieu\ChiTieu-release.apk
```

## Xiaomi 15

Cài đặt → Giới thiệu điện thoại → chạm phiên bản HyperOS nhiều lần để bật nhà phát triển. Trong Cài đặt bổ sung → Tùy chọn nhà phát triển bật Gỡ lỗi USB, Cài đặt qua USB và Gỡ lỗi USB (Cài đặt bảo mật) nếu ROM cung cấp; yêu cầu tài khoản Xiaomi có thể thay đổi theo ROM. Chỉ cho phép máy tính tin cậy. Kết nối USB, chấp nhận khóa RSA rồi chạy:

```powershell
adb devices -l
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell pm list packages
```

Gỡ lỗi không dây: bật Wireless debugging, dùng `adb pair IP:PORT` (cổng ghép đôi), nhập mã rồi `adb connect IP:PORT` (cổng kết nối riêng). Không mở gỡ lỗi qua mạng công cộng.

Trên Android 13+ sideload, vào Thông tin ứng dụng → ⋮ → Cho phép cài đặt hạn chế nếu hệ thống chặn quyền truy cập thông báo. Chỉ cấp quyền SMS nếu dùng SMS Banking.

Kiểm tra thực tế trên Xiaomi và package ngân hàng vẫn cần thiết; build thành công không xác nhận deep link hay thông báo hoạt động trên ROM này.

## Công cụ bổ sung và phiên bản thực tế

- Temurin JDK 17.0.20.1+1 (portable, không sửa PATH hệ thống).
- Gradle 9.7.0 để khởi tạo wrapper; sau đó build qua wrapper. SHA-256 JDK/Gradle đã đối chiếu metadata chính thức; wrapper có distributionSha256Sum.
- SDK command-line tools 23.0 (thử, CLI không giải nén được trong sandbox); 19.0 (SDK Manager Java sử dụng thực tế).
- SDK platforms android-35 revision 2 và android-37.0 revision 2; compileSdk 37, targetSdk 35.
- Build-tools 36.0.0 cho AGP; 35.0.0 và 37.0.0 tải bổ sung để đối chiếu lỗi AAPT2. Bản final không cấu hình override AAPT2.
- Platform-tools 37.0.1-15733141, adb 1.0.41. Chưa có thiết bị kết nối ở thời điểm kiểm tra cuối.
- Plugin: AGP 9.3.1, Kotlin/Compose/Serialization 2.4.21, KSP 2.3.12, Hilt 2.60.1; các phiên bản thư viện trong version catalog. Phiên bản Maven được tra trực tiếp, không chọn từ trí nhớ.
- Các giai đoạn 1–5 không cài thêm công cụ toàn máy; Gradle tải dependency vào thư mục tools/gradle-home. Không cài emulator/Android Studio, SQLCipher, ML Kit hay SDK không nằm trong yêu cầu.
- Trong quá trình chẩn đoán đã biên dịch một helper chuyển tiếp AAPT2 bằng csc.exe .NET có sẵn của Windows; không cài .NET/công cụ mới. Helper chỉ ở evidence, không dùng trong build final hay đưa lên repository.

Biến môi trường trong env.ps1 chỉ có hiệu lực với phiên PowerShell chạy script. Nếu bị execution policy chặn, dùng các lệnh đặt biến trực tiếp trong README; không cần thay đổi chính sách bảo mật. Android/adb có thể tạo thư mục cấu hình chuẩn C:/Users/Admin/.android, còn project/toolchain/APK được lưu ở D:/ChiTieu.
