# Phương án icon ChiTieu

1. **Mặc định — Quét và ghi nhận:** gradient xanh dương → teal, khung QR, đồng xu cách điệu và dấu tích. Đã triển khai adaptive/round/monochrome; hình giữa không dùng chữ/font.
2. **Ví và dấu tích:** giữ gradient và khung, thay đồng xu bằng ví nét trắng. Chưa áp dụng.
3. **Biên lai và tia quét:** giữ gradient, đặt biên lai với hai dòng và một dấu tích ở giữa. Chưa áp dụng.

`icon-preview.png` 512×512 là bản xem trước mô phỏng mặt nạ bo vuông. Launcher Android/HyperOS có thể cắt khác. Nguồn thật là vector XML trong res/drawable, vùng hình giữa x=27–81, y=27–81 của viewport 108dp; không dùng tài sản Apple. `render_icon.py` dùng Pillow đã có để dựng bản xem trước.
