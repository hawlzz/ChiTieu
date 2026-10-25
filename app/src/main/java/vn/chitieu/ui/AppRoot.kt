package vn.chitieu.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import vn.chitieu.data.Entry
import vn.chitieu.domain.*
import vn.chitieu.qr.VietQr
@Composable fun AppRoot(vm:LedgerViewModel) {
 val nav=rememberNavController();var qrText by androidx.compose.runtime.saveable.rememberSaveable {mutableStateOf("")}
 val qr=remember(qrText){runCatching {kotlinx.serialization.json.Json.decodeFromString<VietQr>(qrText)}.getOrNull()}
 var resumeTick by remember {mutableIntStateOf(0)};var pending by remember {mutableStateOf<Entry?>(null)}
 val entries by vm.entries.collectAsStateWithLifecycle()
 val settings by vm.settings.collectAsStateWithLifecycle()
 val connected by vn.chitieu.notification.ListenerState.connected.collectAsStateWithLifecycle()
 LaunchedEffect(Unit){if(!settings.onboarded)nav.navigate("settings")}
 LifecycleEventEffect(Lifecycle.Event.ON_RESUME){resumeTick++}
 LaunchedEffect(resumeTick) {delay(300);vm.repo.expire();pending=vm.repo.dao.all().firstOrNull {it.status==Status.PENDING}}
 LaunchedEffect(entries){if(pending!=null&&entries.none {it.id==pending?.id&&it.status==Status.PENDING})pending=null}
 Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
  if(settings.enabled.isNotEmpty()&&!connected)TextButton(onClick={nav.navigate("settings")}){Text("Listener mất kết nối — kiểm tra thiết lập",color=MaterialTheme.colorScheme.error)}
  Box(Modifier.weight(1f)) {NavHost(navController=nav,startDestination="ledger") {
   composable("ledger"){LedgerScreen(vm)}
   composable("scan"){ScannerScreen(onResult={qrText=kotlinx.serialization.json.Json.encodeToString(it);nav.navigate("confirm")},onBack={nav.popBackStack()})}
   composable("confirm"){qr?.let {ConfirmQrScreen(it,vm,vm.registry,onBack={nav.popBackStack()},onOpened={nav.popBackStack("ledger",false)})}?:TextButton(onClick={nav.popBackStack("ledger",false)}){Text("Quét lại QR")}}
   composable("settings"){SettingsScreen(vm)}
  }}
  NavigationBar {NavigationBarItem(selected=nav.currentBackStackEntryAsState().value?.destination?.route=="ledger",onClick={nav.navigate("ledger"){popUpTo("ledger"){inclusive=true}}},icon={Text("▤")},label={Text("Sổ thu chi")});NavigationBarItem(selected=false,onClick={nav.navigate("scan")},icon={Text("▣")},label={Text("Quét QR")});NavigationBarItem(selected=false,onClick={nav.navigate("settings")},icon={Text("⚙")},label={Text("Cài đặt")})}
 }
 pending?.let {e->AlertDialog(onDismissRequest={pending=null},title={Text("Bạn đã chuyển khoản thành công chưa?")},text={Column {Text(Money.format(e.amount)+" → "+e.recipient);Text("Nội dung: "+e.memo);Text("Chỉ xác nhận sau khi kiểm tra kết quả trong app ngân hàng.")}},confirmButton={TextButton(onClick={vm.work {vm.repo.confirm(e.id)};pending=null}){Text("Đã chuyển")}},dismissButton={Column {TextButton(onClick={vm.delete(e.id);pending=null}){Text("Chưa chuyển / Huỷ")};TextButton(onClick={pending=null}){Text("Hỏi lại sau")}}})}
}



