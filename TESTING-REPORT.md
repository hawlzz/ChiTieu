# Kiểm thử phiên bản 2 — 28/10/2026

Thiết bị thật: Xiaomi 24129PN74C (dada), Android 16 / SDK 36, màn hình 1200 × 2670; có Google Play Services và Play Store. ADB 37.0.1. Hai kết nối không dây cùng trỏ tới một máy, dùng `-s 192.168.1.13:40995`. Chỉ bản `vn.chitieu.debug` được tạo/xóa dữ liệu mẫu.

| Kịch bản | Kết quả | Bằng chứng / giới hạn |
|---|---|---|
| Cài và mở debug | PASS | `adb install -r`, `am start -W`; không thấy crash của vn.chitieu trong crash buffer được thu |
| Unit test | PASS | 59 test, 0 lỗi: Money 3, PIN 3, QR 17, notification 13, backup 9, receipt parser 14 |
| Android Lint debug | PASS | `assembleDebug testDebugUnitTest lint`, log v2-resume-main.log |
| Cấp CAMERA qua ADB | PASS sau khi bật tùy chọn Xiaomi | Lần đầu SecurityException; kiểm tra lại pm grant trả thành công |
| Quyền listener | PASS | `cmd notification allow_listener`, đối chiếu secure enabled_notification_listeners và nhận mẫu thực tế |
| Thông báo mẫu + chống trùng | PASS | Gửi IN/OUT 12.345 VND hai lần mỗi mẫu, chỉ 2 bản ghi tổng; dữ liệu đánh dấu SIMULATED |
| OCR thật | PASS xử lý / CHƯA CHẤM độ chính xác | 30/30 ảnh, 0 lỗi engine; nhãn verified 0/30. Xem OCR-REPORT.md |
| Dọn ảnh thử | PASS | Thư mục staging nội bộ không còn; 30 alias staging ngoài từ lượt trước cũng đã xóa |
| Giao diện sáng/tối | PASS chụp, đã sửa hai lỗi | Snackbar từng che tab dưới và chữ kính tối từng màu đen; đã sửa và chụp lại. 10 PNG và 10 XML: tổng quan, giao dịch, cài đặt, nâng cao, biên lai. Bóng chat của app khác xuất hiện nên screenshot chỉ lưu cục bộ |
| Chạm/vuốt ADB | PASS sau thiết lập | Lần đầu INJECT_EVENTS bị chặn, lượt sau chạy được; không tự đổi thiết lập bảo mật |
| Hiệu năng danh sách | PASS trong lượt đo mẫu | 200 bản ghi SIMULATED, 6 vuốt mỗi chế độ. Số liệu bên dưới; không phải benchmark dài hạn |
| Force-stop và khôi phục | PASS dừng / cần rebind để khôi phục | Lượt cuối: force-stop state 0, mở lại state 0; `allow_listener` để kết nối lại rồi nhận 1 mẫu. Không chứng minh listener sống lâu khi khóa máy |
| Deep link mở ngân hàng | BỊ CHẶN | Cơ chế duyệt tự động từ chối lệnh mở link thử; chưa xác nhận mở thành công trên thiết bị. Không thực hiện chuyển tiền |
| Instrumentation Compose/Room | PASS | Runner trực tiếp trên Xiaomi: `OK (11 tests)`, 7,854 giây; 8 test dữ liệu + 3 test giao diện. Log v2-foreground-instrument.log |
| Release v2 | PASS | assembleRelease sau sửa theme; v2-final-artifacts.log BUILD SUCCESSFUL. APK unsigned 48.769.012 bytes |
| connectedDebugAndroidTest qua Gradle | ĐÃ CHẠY, BỊ CHẶN ở lượt cuối | Đã chạy các lượt trên máy: 8 test dữ liệu qua; sửa test vòng đời/cuộn. Lượt cuối bị INSTALL_FAILED_USER_RESTRICTED; sau cài tay, chạy cùng APK/runner trực tiếp đủ 11/11 qua |
| Kiểm tra APK release | PASS | aapt2 không có INTERNET; DEX không có HarnessActivity hay chuỗi com.android.shell; zip không chứa ảnh/nhãn testdata |
| Không GMS | CHƯA KIỂM CHỨNG | Máy thực tế có GMS; không vô hiệu hóa dịch vụ để giả lập |

## Hiệu năng thực đo

| Chế độ | Frames | Janky (hiện tại) | Janky legacy | p50 / p90 / p95 / p99 |
|---|---:|---:|---:|---|
| Kính đầy đủ | 594 | 1 (0,17%) | 48 (8,08%) | 6 / 10 / 13 / 17 ms |
| Giảm hiệu ứng | 568 | 0 (0%) | 30 (5,28%) | 6 / 8 / 11 / 13 ms |

Lượt cuối run-20261028115823, lấy từ `dumpsys gfxinfo vn.chitieu.debug` sau reset và vuốt trên cùng danh sách. Hai chỉ số jank dùng định nghĩa khác nhau nên không trộn. Giữ tùy chọn giảm hiệu ứng; chưa đủ dữ liệu để kết luận độ mượt khi OCR/camera chạy đồng thời.

## Chạy lại

```powershell
. D:\ChiTieu\env.ps1
cd D:\ChiTieu\project
.\gradlew.bat assembleDebug testDebugUnitTest lint assembleRelease assembleDebugAndroidTest --max-workers=1
.\scripts\adb_test.ps1 -Serial 'SERIAL_TU_ADB_DEVICES'
$env:ANDROID_SERIAL='SERIAL_TU_ADB_DEVICES'
.\gradlew.bat connectedDebugAndroidTest
python scripts\score_receipts.py --results testdata\receipt-results-RUN.json
```

Script kiểm tra đủ số ảnh OCR, trao đổi nhị phân bằng stdin và chỉ xuất kết quả chi tiết vào testdata bị gitignore. Harness được mở với NEW_TASK/CLEAR_TASK để tránh `am start` chuyển intent tới instance cũ và không chạy lại action. Force-stop làm mất cờ shell trong bộ nhớ; phải bật lại khi test.

Log build nằm tại `D:\ChiTieu\evidence`, bằng chứng máy ở `local-reports/`, ảnh/XML ở `screenshots/`; không commit do có thể chứa thông tin của app khác. Crash buffer có lỗi cũ của app khác: chỉ đối chiếu Process vn.chitieu, không coi mọi FATAL trên điện thoại là lỗi ChiTieu.

## Kiểm tra tay còn lại

- Duyệt nhãn testdata/labels.csv, chỉ đánh verified=true sau đối chiếu ảnh; không lấy OCR điền đáp án.
- Khóa màn hình và dùng bình thường đủ lâu để kiểm tra HyperOS ngắt listener; bật tự khởi động, pin không hạn chế và cửa sổ nền nếu cần.
- Camera QR thật, sinh trắc học, Photo Picker/share từ app ngân hàng, cỡ chữ lớn và luồng sửa/lưu biên lai cần đối chiếu trực tiếp thêm.
- Chỉ mở app ngân hàng để kiểm tra deep link; không bấm xác nhận chuyển tiền. Chưa có fixture thông báo ngân hàng thật nên pipeline mẫu không chứng minh nhận ngân hàng thật.

## Lỗi đã xử lý và lịch sử chạy

- AAPT2 link native lỗi trong môi trường agent; build ngoài PowerShell thành công. Một lượt ngoài trùng khóa cache vì build agent còn chạy; đã dừng daemon rồi chạy lại tuần tự.
- Lượt UI đầu có vấn đề vòng đời do test mở Activity lần nữa; đổi sang yêu cầu điều hướng qua state. Sửa cuộn lazy list bằng performScrollToNode. Máy tự khóa trong một lượt; test thêm KEEP_SCREEN_ON, không đổi thời gian khóa hệ thống.
- HyperOS chặn mở Activity từ runner khi app ở nền: test thêm rule mở debug bằng shell trước ActivityScenario. Cài lại khi người dùng cho phép, runner trực tiếp kết thúc OK (11 tests). Không bỏ qua test lỗi để tạo kết quả đạt.
- Một số lượt chạy lỗi bị chủ động dừng bằng force-stop sau khi đã xác định nguyên nhân; không tính chúng là app tự crash, cũng không tính là pass.

Lệnh xác minh cuối: adb -s SERIAL shell am instrument -w -r vn.chitieu.debug.test/androidx.test.runner.AndroidJUnitRunner. Gradle testDebugUnitTest lint assembleRelease cũng qua sau các sửa mã ứng dụng. Cảnh báo targetSdk cũ và DSL AGP deprecated vẫn còn; không có lỗi lint.


Lượt dọn cuối đã xác nhận xóa giao dịch mẫu. Ảnh nguồn và kết quả OCR chi tiết không có trên điện thoại sau dọn; bản sao riêng trên PC nằm trong testdata bị gitignore. Khôi phục listener sau force-stop đã kiểm tra riêng: final-bg-stopped.json = 0, final-bg-recovered.json = 0, final-bg-rebind.json = 1. Script có bước allow_listener để tái kết nối và ghi cảnh báo nếu HyperOS từ chối.

## Máy ảo và Debug Android Studio — 29/10/2026

- Đã cài Android Emulator và Google Play x86_64 API 37.0 vào SDK D:\ChiTieu\tools\sdk bằng SDK Component Installer của IDE.
- Sau khi người dùng bật tính năng Windows và khởi động lại, máy ảo Medium Phone API 37.0 khởi động được từ IDE; ADB nhận emulator-5554, sys.boot_completed=1.
- Chạy cấu hình app bằng Shift+F9: IDE báo Install successfully finished và Connected to the target VM; package vn.chitieu.debug, MainActivity hiển thị Tổng quan.
- Kết nối debugger đã xác minh; tương tác trong phiên debug chưa ổn định: thao tác Thêm nhanh không mở biểu mẫu, dumpsys activity top có timeout. Chưa kết luận nguyên nhân thuộc app, debugger hay máy ảo.
- Sau force-stop, clear-debug-app và mở lại không gắn debugger, UI Automator xác nhận biểu mẫu Thêm nhanh có Số tiền/Danh mục/Lưu, và điều hướng tới Cài đặt hoạt động. Không thêm giao dịch mẫu hoặc cấp quyền đọc thông báo.
- Crash buffer trống tại thời điểm kiểm tra ban đầu. Chưa coi đây là kiểm thử hồi quy đầy đủ hay đo hiệu năng máy ảo.
- Giữ thay đổi cục bộ, chưa commit/push theo yêu cầu người dùng.
