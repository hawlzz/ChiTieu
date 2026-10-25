package vn.chitieu.ui
import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vn.chitieu.notification.*
import vn.chitieu.domain.Money
@Composable fun SettingsScreen(vm:LedgerViewModel) {
 val context=LocalContext.current;val settings by vm.settings.collectAsStateWithLifecycle();val connected by ListenerState.connected.collectAsStateWithLifecycle();val seen by ListenerState.lastSeen.collectAsStateWithLifecycle();val logs by vm.logs.collectAsStateWithLifecycle()
 var consent by remember {mutableStateOf(false)};var synthetic by remember {mutableStateOf("")};var result by remember {mutableStateOf("")}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->vm.work {vm.preferences.flag("sms",granted)}}
 fun details() {runCatching {context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+context.packageName)))}}
 fun open(intent:Intent) {try {context.startActivity(intent)}catch(_:Exception){details()}}
 LazyColumn(Modifier.fillMaxSize().padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=20.dp)) {
  item {Text("Cài đặt & kiểm tra thiết lập",style=MaterialTheme.typography.headlineMedium)}
  item {Text("Dữ liệu ở trên máy. Không có Internet, quảng cáo, analytics hay Google Play Services.")}
  item {Toggle("Giao diện tối",settings.dark){vm.work {vm.preferences.flag("dark",it)}}}
  item {SecuritySettings(vm,settings)}
  item {BackupSettings(vm)}
  item {ManageSettings(vm)}
  item {Text("Đọc biến động số dư",style=MaterialTheme.typography.titleLarge);Text("Chỉ đọc app ngân hàng bạn bật dưới đây. Quyền truy cập thông báo cho phép hệ thống chuyển thông báo tới dịch vụ; ChiTieu bỏ qua các app khác.")}
  item {Text(if(connected)"Listener đã kết nối" else "Listener chưa kết nối — kiểm tra quyền và hạn chế chạy nền");Button(onClick={open(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))}){Text("Cấp quyền truy cập thông báo")};OutlinedButton(onClick={runCatching {NotificationListenerService.requestRebind(ComponentName(context,BankNotificationListener::class.java))}}){Text("Kết nối lại listener")}}
  item {Text("Chưa có mẫu thông báo/SMS thật: parser từng ngân hàng đang ở dạng khung, chưa tự ghi giao dịch. Bật log sau khi đồng ý để thu thập mẫu; chỉ gửi mẫu đã che dữ liệu khi muốn bổ sung parser.",color=MaterialTheme.colorScheme.error)}
  items(vm.registry.banks.filter {it.packageName!=null},key={it.code}) {bank->Column {Toggle(bank.shortName,bank.packageName in settings.enabled){enabled->vm.work {vm.preferences.enable(bank.packageName!!,enabled)}};Text(seen[bank.packageName]?.let {"Đã nhận thông báo trong phiên này lúc "+java.time.Instant.ofEpochMilli(it).atZone(vn.chitieu.domain.VN_ZONE).toLocalTime().withNano(0)}?:"Chưa thấy thông báo trong phiên này. Hãy kiểm tra bằng thông báo thật từ ngân hàng.")}}
  item {Text("Máy không có GMS có thể không nhận thông báo FCM. Listener không thể đọc thông báo chưa từng đến máy. Bản ROM, Play Integrity và chính sách từng ngân hàng cần tự kiểm tra.")}
  item {Toggle("Tự thêm sau khi parser đã xác minh",settings.autoAdd){vm.work {vm.preferences.flag("autoAdd",it)}};Text("Tắt: giao dịch từ parser vào Chờ duyệt. Khớp một giao dịch QR duy nhất trong 10 phút: xác nhận giao dịch đó và chống trùng.")}
  item {Toggle("Ghi log thông báo thô (tối đa 100)",settings.rawLog){enabled->if(enabled)consent=true else vm.work {vm.preferences.flag("rawLog",false)}};Text("Mặc định tắt. Chỉ ghi nội dung app ngân hàng được bật, vào bộ nhớ riêng cục bộ. Log có thể chứa dữ liệu tài chính.");TextButton(onClick={vm.work {vm.repo.dao.clearLogs()}}){Text("Xoá toàn bộ log thô")}}
  items(logs,key={it.id}) {log->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(log.bank);Text(log.body)}}}
  item {Toggle("Đọc SMS Banking dự phòng",settings.sms){enabled->if(!enabled)vm.work {vm.preferences.flag("sms",false)} else if(ContextCompat.checkSelfPermission(context,Manifest.permission.RECEIVE_SMS)==PackageManager.PERMISSION_GRANTED)vm.work {vm.preferences.flag("sms",true)} else permission.launch(Manifest.permission.RECEIVE_SMS)};Text("SMS là quyền nhạy cảm. Mặc định tắt; chỉ bật nếu bạn chủ động dùng SMS Banking. Danh sách đầu số/brandname hiện trống vì chưa có mẫu được xác minh; SMS khác bị bỏ qua, không lưu OTP.")}
  item {Text("Thử parser bằng dữ liệu GIẢ",style=MaterialTheme.typography.titleMedium);OutlinedTextField(synthetic,{synthetic=it.take(4096)},label={Text("[SIMULATED] OUT 1.250.000 VND MEMO=TEST")});TextButton(onClick={val p=SyntheticParser.parse(synthetic,System.currentTimeMillis());result=p?.let {"DỮ LIỆU GIẢ: "+it.kind+" "+Money.format(it.amount)+" · "+it.memo}?:"Không khớp định dạng giả. Không có giao dịch nào được lưu."}){Text("Phân tích thử (không lưu)")};Text(result)}
  item {Text("Hướng dẫn Xiaomi / HyperOS",style=MaterialTheme.typography.titleLarge)}
  item {Text("1. Bật tự khởi động trong Thông tin ứng dụng → Quyền / Tự khởi động. Tên mục khác nhau theo ROM.");OutlinedButton(onClick={details()}){Text("Mở Thông tin ứng dụng")}}
  item {Text("2. Pin → Không hạn chế. Tìm ChiTieu trong danh sách tối ưu pin.");OutlinedButton(onClick={open(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))}){Text("Mở tối ưu pin")}}
  item {Text("3. Mở đa nhiệm, giữ thẻ ChiTieu và chọn khoá ứng dụng. ROM có thể có thao tác khác.")}
  item {Text("4. Với APK sideload Android 13+: Thông tin ứng dụng → ⋮ → Cho phép cài đặt hạn chế nếu quyền truy cập thông báo bị chặn.");OutlinedButton(onClick={details()}){Text("Mở trang quyền / cài đặt hạn chế")}}
  item {Text("5. Nếu HyperOS chặn mở app khi chạy nền, kiểm tra Quyền khác → Hiển thị cửa sổ bật lên khi chạy nền. ChiTieu chỉ chủ động mở ngân hàng từ thao tác trên màn hình; không yêu cầu quyền vẽ đè.");OutlinedButton(onClick={details()}){Text("Mở Quyền khác (tìm thủ công)")}}
  item {Text("// TODO: verify — đường dẫn HyperOS/Autostart và vị trí các mục trên Xiaomi 15 thực tế. Các nút dùng trang hệ thống có fallback; không đoán component Xiaomi.")}
  item {Button(onClick={vm.work {vm.preferences.flag("onboarded",true)}}){Text("Đã xem hướng dẫn thiết lập")}}
 }
 if(consent)AlertDialog(onDismissRequest={consent=false},title={Text("Cho phép ghi log tài chính cục bộ?")},text={Text("Có thể lưu số tài khoản, số dư và nội dung giao dịch từ các ngân hàng bạn đã bật. Không gửi ra ngoài. Bạn có thể tắt và xoá log bất kỳ lúc nào.")},confirmButton={TextButton(onClick={vm.work {vm.preferences.flag("rawLog",true)};consent=false}){Text("Tôi đồng ý bật")}},dismissButton={TextButton(onClick={consent=false}){Text("Huỷ")}})
}
@Composable fun Toggle(label:String,value:Boolean,onChange:(Boolean)->Unit) {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label,Modifier.weight(1f).padding(end=12.dp));Switch(checked=value,onCheckedChange=onChange)}}

