package vn.chitieu.ui
import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import vn.chitieu.notification.*
import vn.chitieu.ui.theme.*
@OptIn(ExperimentalFoundationApi::class)
@Composable fun SettingsScreen(vm:LedgerViewModel,advanced:Boolean=false,onAdvanced:()->Unit={},onWelcome:()->Unit={}){
 val resources=LocalResources.current;val context=LocalContext.current;val settings by vm.settings.collectAsStateWithLifecycle();val connected by ListenerState.connected.collectAsStateWithLifecycle();var page by rememberSaveable {mutableStateOf(if(advanced)"Nâng cao" else "")};var consent by remember {mutableStateOf(false)};var guide by remember {mutableStateOf("")};var timeout by remember {mutableStateOf(false)};var erase by remember {mutableStateOf(false)};var erasePin by remember {mutableStateOf(false)};var tick by remember {mutableIntStateOf(0)}
 LifecycleEventEffect(Lifecycle.Event.ON_RESUME){tick++}
 val granted=remember(tick){context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)};val battery=remember(tick){context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)};val installed=remember {vm.registry.installed(context)}
 val guides=remember {vn.chitieu.domain.AndroidSetup.guides(android.os.Build.MANUFACTURER,android.os.Build.VERSION.SDK_INT)}
 val manual=guides.map {it.id};val labels=guides.associate {it.id to it.title}
 val count=vn.chitieu.domain.AndroidSetup.requiredIssues(granted,connected)
 fun flag(name:String,value:Boolean){vm.work {vm.preferences.flag(name,value)}}
 fun details(){runCatching {context.startActivity(Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+context.packageName)))}}
 fun open(intent:Intent){try {context.startActivity(intent)}catch(_:Exception){details()}}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){flag("sms",it)}
 fun rowPage(title:String,icon:androidx.compose.ui.graphics.vector.ImageVector):@Composable ()->Unit = {SettingsRow(title,icon=icon,onClick={page=title})}
 if(erasePin){LockScreen(settings.copy(biometric=false),vm){erasePin=false;erase=true};return}
 if(page=="Khoá ứng dụng"){SecuritySettings(vm,settings,onBack={page=""},onBackup={page="Sao lưu & khôi phục"});return}
 androidx.activity.compose.BackHandler(page.isNotEmpty()){page=""}
 LazyColumn(Modifier.fillMaxSize().padding(horizontal=GlassTokens.margin),verticalArrangement=Arrangement.spacedBy(GlassTokens.gap),contentPadding=PaddingValues(bottom=bottomContentPadding())){
 item {if(page.isNotEmpty())TextButton(onClick={page=""}){Text("Quay lại")};LargeTitleHeader(page.ifBlank {"Cài đặt"})}
 when(page){
 ""->{
 item {StatusCard(count){page="Kiểm tra thiết lập"}}
 item {SectionHeader("Bảo mật");GroupedList {
 SettingsRow("Khoá ứng dụng",if(settings.pinHash.isBlank())"Chưa bật" else "Đã bật",AppIcons.Lock,onClick={page="Khoá ứng dụng"})
 SettingsRow("Tự khoá sau",timeoutLabel(settings.autoLockMinutes),AppIcons.Timer,onClick={timeout=true})
 SettingsRow("Ẩn trong đa nhiệm",icon=AppIcons.VisibilityOff,trailing={IosSwitch(settings.hideRecents,{flag("hideRecents",it)},label="Ẩn trong đa nhiệm")})}}
 item {SectionHeader("Giao dịch tự động");GroupedList {
 SettingsRow("Thông báo ngân hàng",if(!granted)"Chưa cấp quyền" else if(connected)"Đã kết nối" else "Đang ngắt kết nối",AppIcons.Notifications,trailing={IosSwitch(granted,{open(Intent(AndroidSettings.ACTION_NOTIFICATION_LISTENER_SETTINGS))},label="Thông báo ngân hàng")})
 SettingsRow("Ngân hàng theo dõi",if(settings.enabled.isEmpty())"Chưa chọn" else "${settings.enabled.size} ngân hàng",AppIcons.AccountBalance,onClick={page="Ngân hàng theo dõi"})
 SettingsRow("Cách thêm giao dịch",if(settings.autoAdd)"Tự thêm" else "Chờ duyệt",AppIcons.PlaylistAdd,onClick={page="Cách thêm giao dịch"})
 SettingsRow("Đọc ảnh biên lai",icon=AppIcons.ReceiptLong,onClick={page="Đọc ảnh biên lai"})}}
 item {SectionHeader("Ví & danh mục");GroupedList {rowPage("Quản lý ví",AppIcons.AccountBalanceWallet)();rowPage("Danh mục",AppIcons.Category)();rowPage("Quy tắc tự phân loại",AppIcons.AutoFixHigh)()}}
 item {SectionHeader("Giao diện");GroupedList {SettingsRow("Chủ đề",when(settings.themeMode){"dark"->"Tối";"light"->"Sáng";else->"Hệ thống"},AppIcons.Palette,onClick={page="Chủ đề"});SettingsRow("Giảm hiệu ứng kính",icon=AppIcons.BlurOff,trailing={IosSwitch(settings.reduceEffects,{flag("reduceEffects",it)},label="Giảm hiệu ứng kính")})}}
 item {SectionHeader("Dữ liệu");GroupedList {rowPage("Sao lưu & khôi phục",AppIcons.Backup)();rowPage("Xuất CSV",AppIcons.FileUpload)();rowPage("Nhập CSV",AppIcons.FileDownload)();SettingsRow("Xoá toàn bộ dữ liệu",icon=AppIcons.DeleteForever,tint=MaterialTheme.colorScheme.error,onClick={if(settings.pinHash.isNotBlank())erasePin=true else erase=true})}}
 item {GroupedList {SettingsRow("Nâng cao","SMS, ghi thông báo và ảnh chụp",AppIcons.Tune,onClick={page="Nâng cao"})}}
 item {SectionHeader("Giới thiệu");GroupedList {SettingsRow("ChiTieu",vn.chitieu.BuildConfig.VERSION_NAME,AppIcons.Info);rowPage("Quyền riêng tư & bảo mật",AppIcons.PrivacyTip)();rowPage("Giấy phép mã nguồn mở",AppIcons.Description)();SettingsRow("Xem lại chào mừng",icon=AppIcons.WavingHand,onClick=onWelcome)}}
 }
 "Kiểm tra thiết lập"->{item {SectionFooter("Quyền truy cập thông báo cần thiết cho tự ghi nhận. Các tuỳ chọn chạy nền chỉ cần chỉnh nếu thông báo bị ngắt.");GroupedList {SettingsRow("Truy cập thông báo",if(!granted)"Chưa cấp" else if(connected)"Đã kết nối" else "Đã cấp · đang ngắt kết nối",AppIcons.Notifications,onClick={open(Intent(AndroidSettings.ACTION_NOTIFICATION_LISTENER_SETTINGS))},trailing={StatusIcon(if(granted&&connected)1 else 0)});SettingsRow("Tiết kiệm pin",if(battery)"Không hạn chế" else "Đang tối ưu pin · tuỳ chọn",AppIcons.BatteryFull,onClick={open(Intent(AndroidSettings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))},trailing={StatusIcon(if(battery)1 else 0)});manual.forEach {id->SettingsRow(labels.getValue(id),if(id in settings.setupConfirmed)"Bạn đã xác nhận" else "Bạn tự xác nhận",AppIcons.Settings,onClick={guide=id},trailing={StatusIcon(if(id in settings.setupConfirmed)1 else -1)})}};if(granted&&!connected)PillButton("Kết nối lại",{runCatching {android.service.notification.NotificationListenerService.requestRebind(ComponentName(context,BankNotificationListener::class.java))}},primary=false)}}
 "Ngân hàng theo dõi"->{if(installed.isEmpty())item {SectionFooter("Chưa thấy ứng dụng ngân hàng được hỗ trợ trên máy.")};items(installed){bank->GroupedList {SettingsRow(bank.shortName,bank.bin.orEmpty(),AppIcons.AccountBalance,trailing={IosSwitch(bank.packageName in settings.enabled,{value->vm.work {vm.preferences.enable(bank.packageName!!,value)}},label=bank.shortName)})}}}
 "Cách thêm giao dịch"->item {SegmentedControl(listOf("Tự thêm","Chờ duyệt"),if(settings.autoAdd)"Tự thêm" else "Chờ duyệt"){flag("autoAdd",it=="Tự thêm")};SectionFooter("Chỉ ghi nhận thông báo mà ứng dụng đọc được. Bạn có thể xem lại giao dịch.")}
 "Chủ đề"->item {SegmentedControl(listOf("Hệ thống","Sáng","Tối"),when(settings.themeMode){"dark"->"Tối";"light"->"Sáng";else->"Hệ thống"}){value->vm.work {vm.preferences.text("themeMode",when(value){"Sáng"->"light";"Tối"->"dark";else->"system"})}}}
 "Đọc ảnh biên lai"->item {GroupedList {SettingsRow("Giữ ảnh biên lai","Ảnh được lưu riêng trên máy",AppIcons.Image,trailing={IosSwitch(settings.keepReceipt,{flag("keepReceipt",it)},label="Giữ ảnh biên lai")})};SectionFooter("Chọn Biên lai từ Tổng quan để đọc ảnh và kiểm tra trước khi lưu.")}
 "Quản lý ví","Danh mục","Quy tắc tự phân loại"->item {ManageSettings(vm,page)}
 "Sao lưu & khôi phục","Xuất CSV","Nhập CSV"->item {BackupSettings(vm,page)}
 "Nâng cao"->item {GroupedList {
 SettingsRow("SMS dự phòng","Chỉ dùng khi đã cho phép",AppIcons.Sms,trailing={IosSwitch(settings.sms,{value->if(!value)flag("sms",false) else if(ContextCompat.checkSelfPermission(context,Manifest.permission.RECEIVE_SMS)==PackageManager.PERMISSION_GRANTED)flag("sms",true) else permission.launch(Manifest.permission.RECEIVE_SMS)},label="SMS dự phòng")})
 SettingsRow("Ghi thông báo thô","Lưu tối đa 100 thông báo trên máy",AppIcons.History,trailing={IosSwitch(settings.rawLog,{if(it)consent=true else flag("rawLog",false)},label="Ghi thông báo thô")})
 SettingsRow("Chặn chụp màn hình",if(vn.chitieu.BuildConfig.DEBUG)"Không áp dụng ở bản thử nghiệm" else "Ngăn chụp và ghi hình ứng dụng",AppIcons.Screenshot,trailing={IosSwitch(settings.blockScreenshots,{flag("blockScreenshots",it)},label="Chặn chụp màn hình")})
 };PillButton("Xoá thông báo đã ghi",{vm.work {vm.repo.dao.clearLogs()}},primary=false)}
 "Quyền riêng tư & bảo mật"->item {GroupedList {Column(Modifier.padding(GlassTokens.row),verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){Text("Dữ liệu được xử lý trên máy. Không tài khoản, không quảng cáo, không gửi dữ liệu lên máy chủ.");Text("PIN bảo vệ việc mở ứng dụng. Cơ sở dữ liệu chưa được mã hoá riêng; hãy dùng khoá màn hình của điện thoại.");Text("PIN dùng PBKDF2-HMAC-SHA256 với salt riêng. Bản sao lưu chứa dữ liệu dạng rõ; hãy cất ở nơi riêng tư.");Text("Quyền thông báo, camera và SMS chỉ được dùng khi bạn cho phép.")}}}
 "Giấy phép mã nguồn mở"->item {Text("Inter — SIL Open Font License 1.1\nLucide icons (ISC) · Material Icons, Backdrop — Apache-2.0\nML Kit Text Recognition — Google ML Kit Terms of Service");SectionFooter("Các tệp giấy phép đi kèm trong mã nguồn của ứng dụng.");Text(remember(resources) {resources.openRawResource(vn.chitieu.R.raw.lucide_license).bufferedReader().use {it.readText()}},style=MaterialTheme.typography.bodySmall)}
 }
 }
 if(timeout)AutoLockSheet(settings.autoLockMinutes,{timeout=false}){value->vm.work {vm.preferences.number("autoLockMinutes",value)};timeout=false}
 if(guide.isNotEmpty())ConfirmSheet(labels.getValue(guide),{guide=""}){Icon(AppIcons.SettingsSuggest,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(GlassTokens.bottom));Text(guides.first {it.id==guide}.steps);SectionFooter("Tên mục có thể khác theo hãng và phiên bản Android. Nếu không có, bạn có thể bỏ qua.");PillButton("Mở cài đặt",{details()},Modifier.fillMaxWidth());PillButton("Tôi đã làm xong",{val id=guide;vm.work {vm.preferences.confirmSetup(id)};guide=""},Modifier.fillMaxWidth(),primary=false)}
 if(consent)ConfirmSheet("Lưu thông báo trên máy?",{consent=false}){Text("Thông báo có thể có số tài khoản, số dư và nội dung giao dịch. Bạn có thể tắt và xoá bất cứ lúc nào.");PillButton("Cho phép",{flag("rawLog",true);consent=false});PillButton("Huỷ",{consent=false},primary=false)}
 if(erase)ConfirmSheet("Xoá toàn bộ dữ liệu?",{erase=false}){Text("Hãy sao lưu trước. Nhấn giữ nút bên dưới để xoá giao dịch, ví, danh mục và quy tắc.");Surface(Modifier.fillMaxWidth().combinedClickable(onClick={},onLongClick={erase=false;vm.work {vm.receipts.clearAll()}}),color=MaterialTheme.colorScheme.error,shape=MaterialTheme.shapes.large){Text("Giữ để xoá",Modifier.padding(GlassTokens.margin))};PillButton("Huỷ",{erase=false},primary=false)}
}
@Composable fun Toggle(label:String,value:Boolean,onChange:(Boolean)->Unit){SettingsRow(label,trailing={IosSwitch(value,onChange,label=label)})}
