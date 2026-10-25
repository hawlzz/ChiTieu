# Kết quả kiểm chứng

## Lệnh đã chạy

Bản mã cuối đã chạy thành công:

```text
gradlew.bat assembleDebug testDebugUnitTest lint assembleRelease assembleDebugAndroidTest
BUILD SUCCESSFUL
```

45 unit test, 0 lỗi/0 thất bại:

| Bộ test | Số test |
|---|---:|
| MoneyTest | 3 |
| EmvTest | 17 |
| MessageTest | 13 |
| BackupTest | 9 |
| PinTest | 3 |

Đã build APK debug, APK release unsigned (R8) và APK instrumentation. Có 4 test integration Room đã biên dịch; **chưa chạy** trên Android vì `adb devices -l` không có thiết bị. Không có emulator được cài thêm.

Android Lint qua, không tắt abortOnError và không dùng baseline để bỏ lỗi. Có cảnh báo về targetSdk 35/công cụ mới hơn, các API deprecated; được giữ theo yêu cầu và tổ hợp tương thích. Xem báo cáo HTML trong app/build/reports sau khi build.

## Cổng kiểm tra từng giai đoạn

| Giai đoạn | Build/test/lint |
|---|---|
| 0 — môi trường, wrapper, bản khung | Qua; test NO-SOURCE. Lần đầu AAPT2 crash trong sandbox, người dùng đã chạy bên ngoài thành công trước khi sang giai đoạn 1 |
| 1 — Room, MVVM, thu chi | Qua; 3 unit test |
| 2 — VietQR, CRC, registry, URL | Qua; 20 unit test |
| 3 — scanner, chọn ảnh, ngân hàng, PENDING | Qua; 20 unit test; thiết bị chưa kiểm tra |
| 4 — listener/SMS scaffold, chống trùng, settings | Qua; 33 unit test; sửa khai báo telephony optional theo lint trước khi sang giai đoạn 5 |
| 5 — thống kê/lọc, CSV/backup, PIN/vân tay, tài liệu | Qua; 45 unit test; debug/release/test APK build thành công |

## Kiểm tra riêng tư và dependency

- Manifest release hợp nhất xác nhận minSdk 26, targetSdk 35 và allowBackup=false.
- Không có INTERNET trong manifest hợp nhất.
- Dependency runtime không có Firebase, play-services, Billing, quảng cáo hay analytics SDK.
- WorkManager thêm các quyền tiêu chuẩn WAKE_LOCK, RECEIVE_BOOT_COMPLETED, ACCESS_NETWORK_STATE và FOREGROUND_SERVICE qua thư viện; app không khởi động foreground service để giữ NotificationListener sống.
- Mã app không gọi Log/println để ghi tài khoản, tiền, nội dung giao dịch vào system log.
- ZIP JDK và Gradle đã đối chiếu SHA-256 với metadata chính thức. Gradle Wrapper kiểm checksum bản phân phối.
- File local.properties, build output/cache và keystore bị loại khỏi git. Toolchain nằm ngoài repository tại D:/ChiTieu/tools.

## Phần cần kiểm thử thực tế

Camera/flash, chọn ảnh và xoay máy; giao diện màn nhỏ/bàn phím; tên package bản cài trên Xiaomi; deep link autofill từng app; lifecycle quay lại sau giao dịch; vân tay/PIN và Restricted settings; listener/FCM không GMS; SMS sender/template; HyperOS giữ nền qua đêm; thao tác chọn file/restore. Đây là giới hạn kiểm chứng thực tế, không được coi là đã test pass chỉ nhờ unit test.

Parser ngân hàng thực tế vẫn ở dạng khung chờ mẫu được người dùng cung cấp, đúng yêu cầu không bịa mẫu. Các con số test trên không chứng minh parser sản xuất đã nhận diện thông báo thật.

Log build cục bộ: D:/ChiTieu/evidence/phase1.log, phase2.log, phase3.log, phase4-fixed.log, phase5-final.log và final-validation.log. Log không được đẩy lên repository.
