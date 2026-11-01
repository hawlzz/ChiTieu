package vn.chitieu.domain

import java.util.Locale

data class SetupGuide(val id: String, val title: String, val steps: String)

/** Optional help, never prerequisites for readiness on an unrelated Android device. */
object AndroidSetup {
    fun guides(manufacturer: String, sdk: Int): List<SetupGuide> = buildList {
        if (sdk >= 33) add(SetupGuide("restricted", "Cài đặt hạn chế",
            "Nếu Android chặn quyền truy cập thông báo:\n1. Mở Thông tin ứng dụng ChiTieu.\n2. Chọn dấu ⋮ và cho phép cài đặt hạn chế nếu có.\n3. Quay lại cấp quyền truy cập thông báo."))
        when (manufacturer.trim().lowercase(Locale.ROOT)) {
            "xiaomi", "redmi", "poco" -> {
                add(SetupGuide("autostart", "Tự khởi động", "Nếu thông báo bị ngắt khi chạy nền:\n1. Mở cài đặt ứng dụng ChiTieu.\n2. Bật Tự khởi động nếu có."))
                add(SetupGuide("recents", "Giữ app trong đa nhiệm", "Nếu máy thường dừng ChiTieu:\n1. Mở màn hình đa nhiệm.\n2. Giữ thẻ ChiTieu và chọn khoá nếu có."))
            }
            "samsung" -> add(SetupGuide("background-samsung", "Ứng dụng chạy nền",
                "Nếu thông báo bị ngắt:\n1. Mở cài đặt Pin của điện thoại.\n2. Kiểm tra Giới hạn sử dụng dưới nền.\n3. Bỏ ChiTieu khỏi danh sách ngủ hoặc ngủ sâu nếu có."))
            else -> add(SetupGuide("background", "Ứng dụng chạy nền",
                "Nếu thông báo bị ngắt:\n1. Mở Thông tin ứng dụng ChiTieu.\n2. Kiểm tra mục Pin hoặc Hoạt động dưới nền.\n3. Cho phép chạy nền nếu điện thoại có tuỳ chọn này."))
        }
    }
    fun requiredIssues(notificationGranted: Boolean, listenerConnected: Boolean): Int =
        if (notificationGranted && listenerConnected) 0 else 1
}
