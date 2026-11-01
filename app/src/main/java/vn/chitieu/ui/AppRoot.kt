package vn.chitieu.ui
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
 var hideNavigation by remember {mutableStateOf(false)};val changeNavigation=remember {{hidden:Boolean->hideNavigation=hidden}}
 val reducedMotion=LocalReduceMotion.current
 val travel=with(androidx.compose.ui.platform.LocalDensity.current){PastelTokens.travel.roundToPx()}
 val nav=rememberNavController();var qrText by androidx.compose.runtime.saveable.rememberSaveable {mutableStateOf("")};val qr=remember(qrText){runCatching {kotlinx.serialization.json.Json.decodeFromString<VietQr>(qrText)}.getOrNull()}
 var resumeTick by remember {mutableIntStateOf(0)};var pending by remember {mutableStateOf<Entry?>(null)}
 val pendingStatus by key(pending?.id){remember(vm,pending?.id){pending?.id?.let {vm.repo.dao.observeEntryStatus(it)}?:kotlinx.coroutines.flow.flowOf<Status?>(null)}.collectAsStateWithLifecycle(initialValue=pending?.status)};val settings by vm.settings.collectAsStateWithLifecycle();val connected by vn.chitieu.notification.ListenerState.connected.collectAsStateWithLifecycle();val incoming by ReceiptInbox.incoming.collectAsStateWithLifecycle();val requested by NavigationInbox.screen.collectAsStateWithLifecycle()
 val snackbar=remember {SnackbarHostState()};var bottomHeight by remember {mutableIntStateOf(0)};val density=LocalDensity.current
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()){uri->if(uri!=null){vm.scanReceipt(uri);nav.navigate("receipt"){launchSingleTop=true}}}
 fun pick(id:String?=null){pending=null;vm.receiptTarget.value=id;picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))}
 LifecycleEventEffect(Lifecycle.Event.ON_RESUME){resumeTick++}
 LaunchedEffect(resumeTick){delay(450);vm.repo.expire();pending=vm.repo.dao.pendingOnce()}
 LaunchedEffect(pending?.id,pendingStatus){if(pending!=null&&pendingStatus!=Status.PENDING)pending=null}
 LaunchedEffect(Unit){vm.message.collect {message->if(message.isNotBlank()){snackbar.showSnackbar(message,actionLabel="Đóng");vm.message.value=""}}}
 LaunchedEffect(Unit){vn.chitieu.notification.IngestEvents.events.collect {event->if(snackbar.showSnackbar("Đã ghi nhận giao dịch",actionLabel="Hoàn tác")==SnackbarResult.ActionPerformed)vm.work {val current=vm.repo.dao.get(event.id);if(current?.status==Status.CONFIRMED)vm.repo.dao.save(current.copy(status=event.previousStatus?:Status.APPROVAL))}}}
 // The graph is absent while first-run onboarding is visible. Queue navigation
 // until NavHost is composed, including shared images and the setup shortcut.
 if(!settings.loaded){CircularProgressIndicator();return}
 if(!settings.onboarded){WelcomeScreen {setup->if(setup)NavigationInbox.screen.value="settings";vm.work {vm.preferences.flag("onboarded",true)}};return}
 LaunchedEffect(incoming){incoming?.let {ReceiptInbox.incoming.value=null;vm.receiptTarget.value=null;vm.scanReceipt(it);nav.navigate("receipt"){launchSingleTop=true}}}
 LaunchedEffect(requested){requested?.let {NavigationInbox.screen.value=null;nav.navigate(it){launchSingleTop=true}}}

 val destination=nav.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
 Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
  Box(Modifier.align(androidx.compose.ui.Alignment.TopCenter).then(if(destination in listOf("ledger","transactions","statistics","settings","advanced"))Modifier.widthIn(max=GlassTokens.contentWidth) else Modifier).fillMaxSize()) {
   if(settings.enabled.isNotEmpty()&&!connected)TextButton(onClick={nav.navigate("settings")}){Text("Đọc thông báo bị ngắt · Sửa",color=MaterialTheme.colorScheme.error)}
   Box(Modifier.fillMaxSize().glassContentSource().padding(top=if(settings.enabled.isNotEmpty()&&!connected)48.dp else 0.dp)){CompositionLocalProvider(LocalHideNavigation provides changeNavigation){NavHost(navController=nav,startDestination="ledger",
    enterTransition={if(reducedMotion)EnterTransition.None else fadeIn(tween(PastelTokens.enterMillis))+slideInVertically(tween(PastelTokens.enterMillis)){travel}},
    exitTransition={if(reducedMotion)ExitTransition.None else fadeOut(tween(PastelTokens.exitMillis))},
    popEnterTransition={if(reducedMotion)EnterTransition.None else fadeIn(tween(PastelTokens.enterMillis))},
    popExitTransition={if(reducedMotion)ExitTransition.None else fadeOut(tween(PastelTokens.exitMillis))}
   ) {
    composable("ledger"){LedgerScreen(vm,onScan={nav.navigate("scan")},onReceipt={pick()},onStatistics={month->StatisticsInbox.month=month;nav.navigate("statistics")},onHistory={nav.navigate("transactions")})}
    composable("transactions"){LedgerScreen(vm,details=true,onScan={nav.navigate("scan")},onReceipt={pick()})}
    composable("scan"){ScannerScreen(onResult={qrText=kotlinx.serialization.json.Json.encodeToString(it);nav.navigate("confirm")},onBack={nav.popBackStack()})}
    composable("confirm"){qr?.let {ConfirmQrScreen(it,vm,vm.registry,onBack={nav.popBackStack()},onOpened={nav.popBackStack("ledger",false)})}?:TextButton(onClick={nav.navigate("scan")}){Text("Quét lại QR")}}
    composable("receipt"){ReceiptScreen(vm,onChoose={pick(vm.receiptTarget.value)},onDone={nav.navigate("ledger"){popUpTo("ledger"){inclusive=true}}})}
    composable("welcome"){WelcomeScreen {setup->if(setup)nav.navigate("settings") else nav.popBackStack()}}
    composable("statistics"){StatisticsScreen(vm,onAdd={AddEntryInbox.requested=true;nav.navigate("ledger")})}
    composable("settings"){SettingsScreen(vm,onAdvanced={nav.navigate("advanced")},onWelcome={nav.navigate("welcome")})}
    composable("advanced"){SettingsScreen(vm,advanced=true,onAdvanced={nav.popBackStack()})}
   }}}
   if(!hideNavigation&&destination in listOf("ledger","transactions","statistics","settings","advanced"))Box(Modifier.align(androidx.compose.ui.Alignment.BottomCenter).onSizeChanged {bottomHeight=it.height}) {GlassBottomBar(if(destination=="transactions")1 else if(destination=="statistics")2 else if(destination in listOf("settings","advanced"))3 else 0){index->nav.navigate(listOf("ledger","transactions","statistics","settings")[index]){popUpTo("ledger"){saveState=true};launchSingleTop=true;restoreState=true}}}
  }
  SnackbarHost(snackbar,Modifier.align(androidx.compose.ui.Alignment.BottomCenter).padding(bottom=with(density){bottomHeight.toDp()}))
 }
 pending?.let {e->PendingTransferSheet(e,onReceipt={pick(e.id)},onConfirmed={vm.work {vm.repo.confirm(e.id)};pending=null},onCancelled={vm.delete(e.id);pending=null},onLater={pending=null})}
}
@Composable fun PendingTransferSheet(entry:Entry,onReceipt:()->Unit,onConfirmed:()->Unit,onCancelled:()->Unit,onLater:()->Unit){GlassBottomSheet(onLater){Column(Modifier.padding(GlassTokens.margin),verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){Text("Bạn đã chuyển khoản chưa?",style=MaterialTheme.typography.titleLarge);AmountText(entry.amount);Text("Đến "+entry.recipient);Text("Kiểm tra kết quả trong app ngân hàng trước khi xác nhận.",style=MaterialTheme.typography.bodySmall);PillButton("Đã chuyển",onConfirmed,Modifier.fillMaxWidth().testTag("pending-confirm"));PillButton("Chưa chuyển / Huỷ",onCancelled,Modifier.fillMaxWidth(),primary=false);TextButton(onClick=onReceipt){Text("Đọc ảnh biên lai")};TextButton(onClick=onLater){Text("Hỏi lại sau")}}}
}
