> Giai đoạn 1: tài liệu dưới đây là kế hoạch kiểm thử ban đầu. Kết quả v2 thực đo nằm trong [TESTING-REPORT.md](TESTING-REPORT.md).

# Kiểm thử ChiTieu trên Xiaomi 15

## Tự động

```powershell
. D:\ChiTieu\env.ps1
cd D:\ChiTieu\project
.\gradlew.bat assembleDebug testDebugUnitTest lint
.\gradlew.bat assembleRelease assembleDebugAndroidTest
```

Unit test: CRC chuẩn `123456789 → 29B1`; QR tĩnh/động, có/không tiền, CRC sai, TLV trùng/cắt ngắn, VND lẻ/tràn, QR lạ, URL encode và tham số không hợp lệ; tiền VND và ranh giới tháng Việt Nam; parser giả, khung không đoán dữ liệu thật, fingerprint, QR duy nhất/mơ hồ/ngoài 10 phút/đã xác nhận; CSV quotes/newlines/công thức/ID trùng/file hỏng; JSON version; PIN/salt.

Báo cáo: `app/build/reports/tests/testDebugUnitTest/index.html`, `app/build/reports/lint-results-debug.html`.

Có 4 instrumentation test Room: vòng đời PENDING/CONFIRMED/REVIEW, đối chiếu thông báo đúng một lần, huỷ PENDING, nhập CSV lỗi giữ dữ liệu. Chỉ mới biên dịch APK test, chưa chạy vì không có thiết bị. Chạy khi có Xiaomi:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Chạy trên dữ liệu thử hoặc thiết bị riêng. Test dùng DB in-memory, tài khoản và nội dung giả.

## Chuẩn bị thiết bị

Làm theo SETUP.md: bật USB debugging, Install via USB và USB debugging (Security settings) nếu ROM yêu cầu. `adb devices -l` phải hiện `device`, không phải `unauthorized`. Cài debug APK bằng `adb install -r ...`. Không có Play Store/GMS là cấu hình mục tiêu; không cần cài thêm chúng để chạy ChiTieu.

## Checklist thu chi

1. Mở lần đầu: hướng dẫn thiết lập xuất hiện; đánh dấu đã xem, chuyển Sổ thu chi.
2. Thêm chi 1.250.000, thu 500.000, chọn danh mục/ví, ngày giờ. Kiểm tra tổng chênh lệch -750.000 trong cùng tháng.
3. Sửa số tiền/ngày; lọc tháng, ngày, danh mục, ví, trạng thái; tìm ghi chú và người nhận. Không có kết quả phải có trạng thái rỗng.
4. Tạo ví và danh mục riêng, đổi icon/màu danh mục cũ; thêm quy tắc từ khoá. Kiểm tra chúng tồn tại sau khi tắt/mở app.
5. Huỷ xoá giữ giao dịch; xác nhận xoá mới bỏ. Khởi động lại app kiểm tra Room giữ dữ liệu.
6. Thử số tiền 0, quá giới hạn, ngày lỗi: không lưu/crash. Kiểm tra ranh giới tháng lúc 00:00 tại Asia/Ho_Chi_Minh.

## QR và app ngân hàng

- Dùng QR thử có CRC đúng, tài khoản thử; không thực hiện chuyển tiền thật chỉ để kiểm thử ứng dụng.
- Cho/từ chối quyền camera; chọn ảnh QR khi từ chối, ảnh không QR, ảnh lớn, QR lạ; thử bật/tắt flash và xoay máy. Camera phải được giải phóng khi rời màn hình.
- QR không có tiền cho nhập; QR động/tĩnh có tiền đều giải mã; CRC sai phải báo QR không hợp lệ. Tên trong QR phải có chú thích chưa đối chiếu ngân hàng.
- Liệt kê package thật:

```powershell
adb shell pm list packages
adb shell pm list packages | Select-String 'VCB|mbmobile|techcombank|bidv|vietinbank'
```

Đối chiếu với `assets/banks.json`; package null không được tự đoán. Khi thêm package đã xác minh, cập nhật `<queries>` trong manifest và test.

- Bộ chọn chỉ có app đã cài trong registry. Thử không có app, app không hỗ trợ HTTPS, app bị chặn mở; fallback phải copy tài khoản và cho biết tiền/nội dung cần nhập tay. QRIBFTTC phải gọi rõ số thẻ.
- Lệnh kiểm tra deep link dưới đây chỉ **mở** app, không xác nhận kết quả chuyển tiền. Thay placeholder bằng tài khoản thử đã được phép. Dùng package thật lấy từ adb:

```powershell
adb shell am start -a android.intent.action.VIEW -d '<url>' -p '<package_da_xac_minh>'
# Ví dụ cấu trúc, KHÔNG phải dữ liệu giao dịch thật:
# https://dl.vietqr.io/pay?app=mb&ba=TAI_KHOAN_THU%40vcb&am=1000&tn=TEST&bn=TEST
```

VietQR.io hiện mô tả tham số `url` bắt buộc; kiểm tra theo tài liệu chính thức khi đánh giá mỗi app. Không thêm callback tuỳ đoán. App nhận URL không đồng nghĩa tự điền đầy đủ hoặc chuyển thành công.

- Trước khi mở ngân hàng, kiểm tra giao dịch PENDING đã lưu. Quay lại: Đã chuyển ghi khoản chi một lần, Chưa chuyển/Huỷ xoá, Hỏi lại sau giữ chờ; tắt/khởi động lại vẫn hỏi. PENDING không được tính vào tổng chi trước xác nhận. Sau 24 giờ/mở app chuyển Cần xem lại.

## Listener, dữ liệu giả và dữ liệu thật

1. Trong Cài đặt, đọc giải thích và cấp Truy cập thông báo. Bật đúng ngân hàng muốn theo dõi. Kiểm tra listener kết nối/mất kết nối và nút kết nối lại.
2. `cmd notification post` tạo thông báo của **com.android.shell**, không thể giả package ngân hàng. Lệnh này dùng để kiểm tra listener không nhập nhầm app lạ:

```powershell
adb shell cmd notification post -t 'ChiTieu TEST GIẢ' chitieu-test '[SIMULATED] OUT 1,250,000 VND MEMO=TEST'
```

Kết quả đúng: **không có giao dịch mới và không lưu raw log**, vì shell không nằm trong allowlist. Không khai báo com.android.shell là app ngân hàng để làm test pass.

3. Vào màn Thử parser, dán `[SIMULATED] OUT 1.250.000đ MEMO=TEST` hoặc `[SIMULATED] IN 500,000 VND BAL=1,250,000 MEMO=TEST`. Kết quả phải ghi DỮ LIỆU GIẢ; không lưu giao dịch. Không dùng cú pháp này làm mẫu ngân hàng thật.
4. Parser sản xuất hiện trả null vì chưa có mẫu thật. Bật raw log chỉ sau khi tự đồng ý trong app; thử thông báo thật của ngân hàng đã bật. Nội dung phải ở app cục bộ, không ghi system log, không chứa thông báo app khác. Tắt log: không lưu bản tin mới. Xoá log: danh sách trống.
5. Trên máy không GMS, kiểm tra thông báo ngân hàng có đến thanh thông báo thực sự hay không. Nếu không đến, không kết luận parser lỗi; dùng thêm thủ công. Không hứa listener thay thế FCM.
6. Sau khi có parser đã xác minh: thử khoản thu/chi, số dư khác số tiền, bản tin quảng cáo/OTP, thông báo nhóm; gửi lại cùng mẫu phải không trùng. Thử thông báo trừ tiền khớp QR trong 10 phút, QR đã được xác nhận bằng tay, nhiều QR cùng tiền, thông báo ngoài cửa sổ. Test Room in-memory có sẵn xác nhận đúng một lần.
7. SMS mặc định tắt; bật phải xin RECEIVE_SMS, từ chối vẫn dùng được thu chi/QR. Allowlist sender hiện rỗng nên mọi SMS bị bỏ qua. Chỉ kiểm thử nhập SMS sau khi bổ sung sender/fixture đã xác minh; không lưu OTP/SMS không liên quan.

## HyperOS

Kiểm tra tên/vị trí mục trên ROM thực tế: Tự khởi động, Pin Không hạn chế, khoá đa nhiệm, Truy cập thông báo, Cho phép cài đặt hạn chế, cửa sổ bật lên khi chạy nền nếu cần. Mỗi nút phải mở hệ thống hoặc fallback Thông tin ứng dụng, không crash. Tắt/bật quyền rồi mở app để xem cảnh báo. Để máy qua đêm, thử khởi động lại và kiểm tra kết nối; WorkManager có thể bị hoãn.

## CSV, sao lưu, bảo mật

- Tạo ghi chú có dấu tiếng Việt, dấu phẩy, dấu nháy, xuống dòng và chuỗi bắt đầu `=`, `+`, `-`, `@`. Xuất CSV; ô văn bản được thêm một dấu nháy bảo vệ công thức. Nhập lại phải giữ nguyên ghi chú, không tạo ID trùng. CSV bên ngoài sai định dạng phải từ chối, không xoá dữ liệu.
- Sao lưu JSON, thêm giao dịch thử, chọn khôi phục rồi Huỷ: dữ liệu giữ nguyên. Xác nhận khôi phục: dữ liệu trở về bản sao lưu. File JSON sai phiên bản, file lỗi hoặc lớn hơn 16 MB phải từ chối trước khi thay DB. PIN, quyền hệ thống và log không được nằm trong file.
- Đặt PIN, khoá/mở lại, thử PIN sai và chờ sau 5 lần; đổi/tắt PIN phải có PIN cũ. Không hiển thị giao dịch trước khi đọc xong cài đặt khoá. Đưa app xuống nền hoặc mở ngân hàng rồi quay lại phải khoá lại.
- Bật vân tay sau khi có PIN; thử không có cảm biến/chưa đăng ký, thành công, huỷ và dùng PIN. Kiểm tra app bank không bị ChiTieu yêu cầu quyền overlay không cần thiết.
- Đổi giao diện tối. Thử screenshot/preview đa nhiệm: FLAG_SECURE có thể chặn theo ROM.
- Kiểm tra manifest APK không có INTERNET và `allowBackup=false`; `adb logcat` bản release không được có tài khoản hay nội dung giao dịch từ ChiTieu.
