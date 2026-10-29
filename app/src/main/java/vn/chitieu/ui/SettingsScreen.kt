package vn.chitieu.ui
import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import vn.chitieu.notification.*
import vn.chitieu.ui.theme.*
@Composable fun SettingsScreen(vm:LedgerViewModel,advanced:Boolean=false,onAdvanced:()->Unit={}) {
 val context=LocalContext.current;val settings by vm.settings.collectAsStateWithLifecycle();val connected by ListenerState.connected.collectAsStateWithLifecycle();val logs by vm.logs.collectAsStateWithLifecycle();var consent by remember {mutableStateOf(false)};var security by remember {mutableStateOf(false)};var about by remember {mutableStateOf(false)};var tick by remember {mutableIntStateOf(0)}
 LifecycleEventEffect(Lifecycle.Event.ON_RESUME){tick++}
 val granted=remember(tick){context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)}
 val battery=remember(tick){context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)}
 val installed=remember {vm.registry.installed(context)}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){grantedSms->vm.work {vm.preferences.flag("sms",grantedSms)}}
 fun details(){runCatching {context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+context.packageName)))}}
 fun open(intent:Intent){try {context.startActivity(intent)}catch(_:Exception){details()}}
 LazyColumn(Modifier.fillMaxSize().padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(top=12.dp,bottom=30.dp)) {
  item {GlassTopBar(if(advanced)"Nâng cao" else "Cài đặt")}
  if(!advanced) {
   item {GlassCard(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Sẵn sàng chưa?",style=MaterialTheme.typography.titleLarge);Text("Có thể dùng thêm nhanh và đọc ảnh ngay. Các bước sau giúp đọc thông báo ổn định hơn.")
    if(!granted)SetupStep("Quyền đọc thông báo",onFix={open(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))})
    if(!granted&&"restricted" !in settings.setupConfirmed)SetupStep("Cho phép cài đặt hạn chế","Thông tin ứng dụng → ⋮; vị trí tùy bản HyperOS.",onFix={details()},onConfirm={vm.work {vm.preferences.confirmSetup("restricted")}})
    if("autostart" !in settings.setupConfirmed)SetupStep("Tự khởi động","Chưa thể kiểm tra tự động trên bản HyperOS này.",onFix={details()},onConfirm={vm.work {vm.preferences.confirmSetup("autostart")}})
    if(!battery)SetupStep("Pin: Không hạn chế",onFix={open(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))})
    if("popup" !in settings.setupConfirmed)SetupStep("Mở cửa sổ khi chạy nền","Tự xác nhận sau khi kiểm tra trong Quyền khác.",onFix={details()},onConfirm={vm.work {vm.preferences.confirmSetup("popup")}})
    if(granted&&battery&&setOf("autostart","popup").all {it in settings.setupConfirmed})Text("✓ Các bước đã hoàn tất (một số bước do bạn xác nhận).",color=MaterialTheme.colorScheme.primary)
    if(granted&&!connected)TextButton(onClick={runCatching {android.service.notification.NotificationListenerService.requestRebind(ComponentName(context,BankNotificationListener::class.java))}}){Text("Kết nối lại đọc thông báo")}
   }}}
   item {Choice("Giao diện",listOf("Theo hệ thống","Sáng","Tối"),when(settings.themeMode){"dark"->"Tối";"light"->"Sáng";else->"Theo hệ thống"}){value->vm.work {vm.preferences.text("themeMode",when(value){"Tối"->"dark";"Sáng"->"light";else->"system"})}}}
   item {Text("Ngân hàng của bạn",style=MaterialTheme.typography.titleLarge);if(installed.isEmpty())Text("Chưa thấy app ngân hàng đã được xác minh trên máy.")}
   installed.forEach {bank->item {Toggle(bank.shortName,bank.packageName in settings.enabled){enabled->vm.work {vm.preferences.enable(bank.packageName!!,enabled)}}}}
   item {Choice("Khi nhận giao dịch",listOf("Tự ghi nhận","Chờ duyệt"),if(settings.autoAdd)"Tự ghi nhận" else "Chờ duyệt"){vm.work {vm.preferences.flag("autoAdd",it=="Tự ghi nhận")}};Text("Chưa có mẫu thông báo ngân hàng đã xác minh. Trong lúc này, dùng đọc ảnh hoặc thêm nhanh.",style=MaterialTheme.typography.bodySmall)}
   item {GlassButton("Khoá ứng dụng",{security=true},Modifier.fillMaxWidth())}
   item {GlassButton("Nâng cao",onAdvanced,Modifier.fillMaxWidth())}
   item {TextButton(onClick={about=true}){Text("Giới thiệu & giấy phép")}}
  } else {
   item {Toggle("Giảm hiệu ứng",settings.reduceEffects){vm.work {vm.preferences.flag("reduceEffects",it)}};Text("Tự giảm khi bật tiết kiệm pin.")}
   item {Toggle("Lưu ảnh biên lai cùng giao dịch",settings.keepReceipt){vm.work {vm.preferences.flag("keepReceipt",it)}};Text("Mặc định tắt. Khi bật, ảnh nằm trong bộ nhớ riêng của app; không đưa vào bản sao lưu JSON.")}
   item {Toggle("SMS Banking dự phòng",settings.sms){enabled->if(!enabled)vm.work {vm.preferences.flag("sms",false)}else if(ContextCompat.checkSelfPermission(context,Manifest.permission.RECEIVE_SMS)==PackageManager.PERMISSION_GRANTED)vm.work {vm.preferences.flag("sms",true)}else permission.launch(Manifest.permission.RECEIVE_SMS)};Text("Quyền nhạy cảm, mặc định tắt. Chưa có đầu số và mẫu thật nên chưa tự nhập SMS.")}
   item {Toggle("Ghi nội dung thông báo cục bộ",settings.rawLog){enabled->if(enabled)consent=true else vm.work {vm.preferences.flag("rawLog",false)}};Text("Chỉ ngân hàng đã bật, tối đa 100 bản tin. Không gửi ra ngoài.");TextButton(onClick={vm.work {vm.repo.dao.clearLogs()}}){Text("Xoá nội dung đã ghi")}}
   if(settings.rawLog)logs.forEach {log->item {Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(log.bank);Text(log.body)}}}}
   item {ManageSettings(vm)}
   item {BackupSettings(vm)}
   item {TextButton(onClick=onAdvanced){Text("Quay lại cài đặt")}}
  }
 }
 if(consent)AlertDialog(onDismissRequest={consent=false},title={Text("Cho phép lưu thông báo trên máy?")},text={Text("Thông báo có thể chứa số tài khoản, số dư và nội dung giao dịch. Bạn có thể tắt và xoá bất cứ lúc nào.")},confirmButton={TextButton(onClick={vm.work {vm.preferences.flag("rawLog",true)};consent=false}){Text("Cho phép")}},dismissButton={TextButton(onClick={consent=false}){Text("Huỷ")}})
 if(security)GlassDialog({security=false}){Column(Modifier.heightIn(max=600.dp).verticalScrollCompat()){SecuritySettings(vm,settings);TextButton(onClick={security=false}){Text("Đóng")}}}
 if(about)GlassDialog({about=false}){Text("ChiTieu 2",style=MaterialTheme.typography.titleLarge);Text("Kính lỏng: Backdrop 2.0.1 — Kyant0, Apache-2.0. Component giao diện do ChiTieu xây dựng.");Text("OCR: ML Kit Text Recognition bundled — Google, theo ML Kit Terms of Service. Mô hình được đóng gói; app không có quyền Internet.");Text("Dữ liệu chỉ xử lý trên máy. Không có mẫu ngân hàng đã xác minh thì không tự đoán giao dịch.");TextButton(onClick={about=false}){Text("Đóng")}}
}
@Composable private fun Modifier.verticalScrollCompat():Modifier=this.verticalScroll(rememberScrollState())
@Composable private fun SetupStep(title:String,hint:String="",onFix:()->Unit,onConfirm:(()->Unit)?=null){Column {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("○ "+title,Modifier.weight(1f).padding(top=12.dp));TextButton(onClick=onFix){Text("Sửa")}};if(hint.isNotBlank())Text(hint,style=MaterialTheme.typography.bodySmall);onConfirm?.let {TextButton(onClick=it){Text("Tôi đã kiểm tra")}}}}
@Composable fun Toggle(label:String,value:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label,Modifier.weight(1f).padding(end=12.dp));Switch(checked=value,onCheckedChange=onChange)}}
