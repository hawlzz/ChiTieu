package vn.chitieu.ui
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import kotlinx.coroutines.delay
import vn.chitieu.data.Entry
import vn.chitieu.domain.*
import vn.chitieu.qr.VietQr
import vn.chitieu.receipt.*
import vn.chitieu.ui.theme.*
object NavigationInbox {val screen=kotlinx.coroutines.flow.MutableStateFlow<String?>(null)}
@Composable fun AppRoot(vm:LedgerViewModel) {
 val nav=rememberNavController();var qrText by androidx.compose.runtime.saveable.rememberSaveable {mutableStateOf("")};val qr=remember(qrText){runCatching {kotlinx.serialization.json.Json.decodeFromString<VietQr>(qrText)}.getOrNull()}
 var resumeTick by remember {mutableIntStateOf(0)};var pending by remember {mutableStateOf<Entry?>(null)}
 val entries by vm.entries.collectAsStateWithLifecycle();val settings by vm.settings.collectAsStateWithLifecycle();val connected by vn.chitieu.notification.ListenerState.connected.collectAsStateWithLifecycle();val incoming by ReceiptInbox.incoming.collectAsStateWithLifecycle();val requested by NavigationInbox.screen.collectAsStateWithLifecycle()
 val snackbar=remember {SnackbarHostState()};var bottomHeight by remember {mutableIntStateOf(0)};val density=LocalDensity.current
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()){uri->if(uri!=null){vm.scanReceipt(uri);nav.navigate("receipt"){launchSingleTop=true}}}
 fun pick(id:String?=null){pending=null;vm.receiptTarget.value=id;picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))}
 LifecycleEventEffect(Lifecycle.Event.ON_RESUME){resumeTick++}
 LaunchedEffect(resumeTick){delay(450);vm.repo.expire();pending=vm.repo.dao.all().firstOrNull {it.status==Status.PENDING}}
 LaunchedEffect(entries){if(pending!=null&&entries.none {it.id==pending?.id&&it.status==Status.PENDING})pending=null}
 LaunchedEffect(incoming){incoming?.let {ReceiptInbox.incoming.value=null;vm.receiptTarget.value=null;vm.scanReceipt(it);nav.navigate("receipt"){launchSingleTop=true}}}
 LaunchedEffect(requested){requested?.let {NavigationInbox.screen.value=null;nav.navigate(it){launchSingleTop=true}}}
 LaunchedEffect(Unit){vm.message.collect {message->if(message.isNotBlank()){snackbar.showSnackbar(message,actionLabel="Đóng");vm.message.value=""}}}
 LaunchedEffect(Unit){vn.chitieu.notification.IngestEvents.events.collect {event->if(snackbar.showSnackbar("Đã ghi nhận giao dịch",actionLabel="Hoàn tác")==SnackbarResult.ActionPerformed)vm.work {val current=vm.repo.dao.get(event.id);if(current?.status==Status.CONFIRMED)vm.repo.dao.save(current.copy(status=event.previousStatus?:Status.APPROVAL))}}}
 val destination=nav.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
 Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
  Column(Modifier.fillMaxSize()) {
   if(settings.enabled.isNotEmpty()&&!connected)TextButton(onClick={nav.navigate("settings")}){Text("Đọc thông báo bị ngắt · Sửa",color=MaterialTheme.colorScheme.error)}
   Box(Modifier.weight(1f)){NavHost(navController=nav,startDestination="ledger") {
    composable("ledger"){LedgerScreen(vm,onScan={nav.navigate("scan")},onReceipt={pick()},onHistory={nav.navigate("transactions")})}
    composable("transactions"){LedgerScreen(vm,details=true,onScan={nav.navigate("scan")},onReceipt={pick()})}
    composable("scan"){ScannerScreen(onResult={qrText=kotlinx.serialization.json.Json.encodeToString(it);nav.navigate("confirm")},onBack={nav.popBackStack()})}
    composable("confirm"){qr?.let {ConfirmQrScreen(it,vm,vm.registry,onBack={nav.popBackStack()},onOpened={nav.popBackStack("ledger",false)})}?:TextButton(onClick={nav.navigate("scan")}){Text("Quét lại QR")}}
    composable("receipt"){ReceiptScreen(vm,onChoose={pick(vm.receiptTarget.value)},onDone={nav.navigate("ledger"){popUpTo("ledger"){inclusive=true}}})}
    composable("settings"){SettingsScreen(vm,onAdvanced={nav.navigate("advanced")})}
    composable("advanced"){SettingsScreen(vm,advanced=true,onAdvanced={nav.popBackStack()})}
   }}
   Box(Modifier.onSizeChanged {bottomHeight=it.height}) {GlassBottomBar(if(destination=="transactions")1 else if(destination in listOf("settings","advanced"))2 else 0){index->nav.navigate(listOf("ledger","transactions","settings")[index]){popUpTo("ledger"){saveState=true};launchSingleTop=true;restoreState=true}}}
  }
  SnackbarHost(snackbar,Modifier.align(androidx.compose.ui.Alignment.BottomCenter).padding(bottom=with(density){bottomHeight.toDp()}))
 }
 pending?.let {e->GlassDialog(onDismiss={pending=null}) {Text("Bạn đã chuyển khoản chưa?",style=MaterialTheme.typography.titleLarge);Text(Money.format(e.amount)+" → "+e.recipient);Text("Kiểm tra kết quả trong app ngân hàng trước khi xác nhận.");GlassButton("Đọc ảnh biên lai",onClick={pick(e.id)});TextButton(onClick={vm.work {vm.repo.confirm(e.id)};pending=null}){Text("Đã chuyển")};TextButton(onClick={vm.delete(e.id);pending=null}){Text("Chưa chuyển / Huỷ")};TextButton(onClick={pending=null}){Text("Hỏi lại sau")}}}
}
