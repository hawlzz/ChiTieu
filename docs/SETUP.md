# ChiTieu — Android offline

Dự án tại `D:\ChiTieu\project`. Công cụ riêng tại `D:\ChiTieu\tools`.

## Môi trường

Không có JDK/adb/sdkmanager/Gradle trên PATH khi kiểm tra ban đầu. Đã tải JDK Temurin 17.0.20.1+1, Gradle 9.7.0 (chỉ dùng khởi tạo wrapper), command-line tools 23.0 và 19.0. Bản 23 gặp lỗi giải nén CLI trong sandbox; bản 19 dùng SDK Manager Java. Đã chấp nhận license SDK bằng SDK Manager. SDK platform 35, 37.0, build-tools 36.0.0 và platform-tools cài tại tools/sdk. Không cài Android Studio hoặc emulator. Phiên bản 2 thêm dependency ML Kit bundled và Backdrop qua Gradle; xem phần bổ sung v2.

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
- Platform-tools 37.0.1-15733141, adb 1.0.41. Đã kết nối Xiaomi qua USB ngày 26/10 và ADB không dây ngày 28/10/2026.
- Plugin: AGP 9.3.1, Kotlin/Compose/Serialization 2.4.21, KSP 2.3.12, Hilt 2.60.1; các phiên bản thư viện trong version catalog. Phiên bản Maven được tra trực tiếp, không chọn từ trí nhớ.
- Các giai đoạn 1–5 không cài thêm công cụ toàn máy; Gradle tải dependency vào thư mục tools/gradle-home. Không cài emulator/Android Studio hoặc SQLCipher. ML Kit được bổ sung trong giai đoạn 2 theo prompt2.
- Trong quá trình chẩn đoán đã biên dịch một helper chuyển tiếp AAPT2 bằng csc.exe .NET có sẵn của Windows; không cài .NET/công cụ mới. Helper chỉ ở evidence, không dùng trong build final hay đưa lên repository.

Biến môi trường trong env.ps1 chỉ có hiệu lực với phiên PowerShell chạy script. Nếu bị execution policy chặn, dùng các lệnh đặt biến trực tiếp trong README; không cần thay đổi chính sách bảo mật. Android/adb có thể tạo thư mục cấu hình chuẩn C:/Users/Admin/.android, còn project/toolchain/APK được lưu ở D:/ChiTieu.

## Bổ sung v2

Backdrop 2.0.1, ML Kit text-recognition bundled 16.0.1; minSdk 33, Room schema 2. Không thêm công cụ toàn máy. Google helper dependencies vẫn có trong graph, xem DECISIONS.md. Bản debug là vn.chitieu.debug; release giữ vn.chitieu.

Chạy PowerShell: .\scripts\adb_test.ps1 -Serial '<serial từ adb devices>'. Script chỉ nhận máy đã được cấp phép; giữ điện thoại mở khóa, bật Cài đặt qua USB và Gỡ lỗi USB (Cài đặt bảo mật) nếu HyperOS yêu cầu. Kết quả/screenshot cục bộ được gitignore. OCR cần testdata/receipts; bỏ qua có chủ đích bằng -SkipOcr.

Chạy instrumentation: đặt ANDROID_SERIAL đúng một kết nối rồi chạy .\gradlew.bat connectedDebugAndroidTest. Không chạy hai kết nối của cùng điện thoại như hai máy riêng. Chấm OCR bằng Python: scripts/score_receipts.py --results testdata/receipt-results-RUN.json; chỉ dùng nhãn verified=true.

## Mở bằng Android Studio trên máy này

Android Studio Rabbit 2 | 2026.2.2 (build 262.10968.63.2622.16730706) được tải từ trang chính thức Google, bản ZIP Windows; SHA-256 đã đối chiếu: `a05e6742d5fd92a090ee6697c3f75706db735decf5a8543f3bbaf2514c0fd123`.

- IDE: `D:\ChiTieu\tools\AndroidStudio\android-studio\bin\studio64.exe`.
- Lối tắt Desktop: **Android Studio - ChiTieu**, mở thẳng `D:\ChiTieu\project`.
- SDK của project: `D:\ChiTieu\tools\sdk` (local.properties, không commit).
- Gradle user home: `D:\ChiTieu\tools\gradle-home`; dùng wrapper 9.7.0 của project.
- Daemon Gradle yêu cầu Java 17, đồng nhất với JVM toolchain của app. `gradle/gradle-daemon-jvm.properties` chỉ định major 17; đường dẫn JDK cục bộ được đăng ký trong gradle.properties riêng của Gradle user home, không ghi đường dẫn máy vào mã nguồn.
- Git trong IDE dùng bản đã có của GitHub Desktop (`app-3.6.9/resources/app/git/cmd/git.exe`), đã nhận diện version 2.56.0.windows.2. Nếu GitHub Desktop đổi thư mục phiên bản khi cập nhật, chỉnh lại Settings → Version Control → Git.
- `.idea/` và `.gradle/` là cấu hình riêng trên máy, được gitignore. Không cần đăng nhập Google/Gemini để mở, sửa và build app.

Lần mở đầu IDE mặc định chọn Java 25 và không tìm thấy toolchain Java 17 của app. Đã đổi daemon criteria về 17 và chỉ rõ JDK portable đang có; không nâng AGP/Kotlin/Gradle hay thêm repository tải JDK chỉ để import. Wizard lần đầu đã cài thêm SDK/emulator mặc định ở `%LOCALAPPDATA%\Android\Sdk`; project ChiTieu sử dụng SDK riêng trên D như cấu hình trên.

Nguồn chính thức: https://developer.android.com/studio ; bảng tương thích AGP: https://developer.android.com/studio/releases ; cơ chế daemon JVM: https://docs.gradle.org/current/userguide/gradle_daemon.html .

Đã xác minh import trong IDE: project nhận tên ChiTieu, module app và run configuration app; Gradle sync kết thúc thành công (IDE log onSuccess RESOLVE_PROJECT:2, màn hình Build ghi BUILD SUCCESSFUL). Còn cảnh báo AGP/DSL cũ như trước; không chạy Upgrade Assistant tự động.

Máy ảo (29/10/2026): Android Emulator và system image Google Play x86_64 API 37.0 đã được cài thêm vào SDK trên D. Medium Phone API 37.0 khởi động được trong IDE sau khi người dùng bật tính năng Windows và khởi động lại. Shift+F9 cài app và kết nối debugger thành công; giới hạn tương tác của phiên Debug được ghi trong báo cáo kiểm thử lưu trữ cục bộ.

## Giao diện mới — công cụ/tài nguyên
Không cài công cụ toàn máy. Dùng Python/Pillow 12.3.0 đã có để render vector icon ra PNG; cairosvg không có nên không sử dụng/cài. Tải Inter variable và giấy phép OFL từ google/fonts; bổ sung core-splashscreen 1.2.0 vào catalog. Espresso 3.7.0 chỉ thuộc APK kiểm thử, để tương thích Android 17. Gradle vẫn wrapper/JDK 17 hiện có.

Bản cập nhật máy ảo dùng debug keystore mà Android Studio đã sử dụng ở C:/Users/Admin/.android/debug.keystore qua android.injected.signing.* ở lệnh build, không commit keystore/password hay cấu hình ký máy cá nhân. Bản build mặc định ở máy khác vẫn dùng debug key chuẩn của máy đó. local.properties dùng sdk.dir=D\:/ChiTieu/tools/sdk để lint không báo PropertyEscape.
