package vn.chitieu.ui
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.fragment.app.FragmentActivity
import androidx.biometric.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import vn.chitieu.data.Settings
import vn.chitieu.domain.PinSecurity
import vn.chitieu.ui.theme.*
private class PinBuffer {val chars=CharArray(12);var size by mutableIntStateOf(0);fun add(c:Char){if(size<12){chars[size]=c;size++}};fun delete(){if(size>0){size--;chars[size]='\u0000'}};fun clear(){chars.fill('\u0000');size=0};fun copy()=chars.copyOf(size)}
@Composable fun LockScreen(settings:Settings,vm:LedgerViewModel,onUnlock:()->Unit){
 val context=LocalContext.current;val gateway=LocalBiometricGateway.current
 val scope=rememberCoroutineScope();val buffer=remember {PinBuffer()}
 var error by remember {mutableStateOf("")};var busy by remember {mutableStateOf(false)}
 var remaining by remember {mutableLongStateOf(1)};var shake by remember {mutableIntStateOf(0)}
 val haptic=LocalHapticFeedback.current
 val latestSettings by rememberUpdatedState(settings);val unlock by rememberUpdatedState(onUnlock)
 val canBiometric=settings.biometric&&gateway?.available()==true
 var showPin by androidx.compose.runtime.saveable.rememberSaveable {mutableStateOf(!canBiometric)}
 var attempted by remember {mutableStateOf(false)}
 DisposableEffect(Unit){onDispose {buffer.clear()}}
 LaunchedEffect(Unit){while(true){remaining=vm.pinGate.remaining();delay(250)}}
 DisposableEffect(gateway,settings.biometric){
  val unbind=if(settings.biometric)gateway?.bind {outcome->
   when(outcome){
    BiometricOutcome.SUCCESS->scope.launch {if(latestSettings.biometric&&vm.pinGate.remaining()==0L)unlock() else showPin=true}
    else->{showPin=true;error=""}
   }
  } else null
  onDispose {unbind?.invoke();if((context as? FragmentActivity)?.isChangingConfigurations!=true)gateway?.cancel()}
 }
 fun openBiometric(){scope.launch {
  remaining=vm.pinGate.remaining()
  if(remaining==0L&&!busy&&canBiometric){buffer.clear();error="";showPin=false;gateway?.authenticate()}else showPin=true
 }}
 // Attempt once per lock entry. Rotation reattaches to the existing system prompt;
 // cancellation selects PIN and does not trigger another automatic prompt on resume.
 androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME){
  if(!attempted&&!showPin){attempted=true;openBiometric()}
 }
 val lockBackground=androidx.compose.ui.graphics.Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha=.08f),MaterialTheme.colorScheme.background))
 val windowSize=LocalWindowInfo.current.containerSize;val landscape=windowSize.width>windowSize.height
 @Composable fun heading(){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){
  BrandMark(Modifier.size(PastelTokens.brand))
  Text(if(showPin)"Nhập mã PIN" else "Mở khoá ChiTieu",style=MaterialTheme.typography.headlineMedium)
  SectionFooter("Dữ liệu tài chính của bạn được giữ riêng tư.")
  if(showPin){PinDots(buffer.size,shake);Text(if(remaining>0)"Thử lại sau ${(remaining+999)/1000} giây" else error,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)}
 }}
 @Composable fun keys(){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){
  if(!showPin){
   Icon(AppIcons.Fingerprint,"Sinh trắc học",Modifier.size(GlassTokens.minTouch).testTag("biometric-wait"),tint=MaterialTheme.colorScheme.primary)
   SectionFooter("Xác nhận bằng sinh trắc học để tiếp tục")
   PillButton("Dùng PIN",{showPin=true;gateway?.cancel()},primary=false)
  }else{
   PinKeypad(!busy&&remaining==0L,if(canBiometric)({openBiometric()}) else null,buffer::add,buffer::delete)
   PillButton(if(busy)"Đang kiểm tra…" else "Mở khoá",{busy=true;val entered=buffer.copy();buffer.clear();scope.launch {try {if(vm.pinGate.verify(entered,settings)){unlock()}else{remaining=vm.pinGate.remaining();error="Mã PIN chưa đúng";shake++;haptic.performHapticFeedback(HapticFeedbackType.LongPress)}}finally{busy=false}}},enabled=buffer.size>=6&&!busy&&remaining==0L)
  }
 }}
 if(landscape)Row(Modifier.fillMaxSize().background(lockBackground).windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState()).padding(GlassTokens.margin),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GlassTokens.margin)){Box(Modifier.weight(1f)){heading()};Box(Modifier.weight(1f)){keys()}}else BoxWithConstraints(Modifier.fillMaxSize().background(lockBackground).windowInsetsPadding(WindowInsets.safeDrawing)){Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min=maxHeight).padding(GlassTokens.margin),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(GlassTokens.margin,Alignment.CenterVertically)){heading();keys()}}
}
@Composable fun SecuritySettings(vm:LedgerViewModel,settings:Settings,onBack:()->Unit={},onBackup:()->Unit={}){val context=LocalContext.current;val scope=rememberCoroutineScope();var flow by remember {mutableStateOf("")};var step by remember {mutableIntStateOf(1)};val buffer=remember {PinBuffer()};val first=remember {PinBuffer()};var error by remember {mutableStateOf("")};var busy by remember {mutableStateOf(false)};var shake by remember {mutableIntStateOf(0)};var remaining by remember {mutableLongStateOf(0)};var timeout by remember {mutableStateOf(false)}
 val hideNavigation=LocalHideNavigation.current;SideEffect {hideNavigation(flow.isNotEmpty())}
 DisposableEffect(hideNavigation){onDispose {hideNavigation(false)}}
 DisposableEffect(Unit){onDispose {buffer.clear();first.clear()}}
 LaunchedEffect(flow){while(flow.isNotEmpty()){remaining=vm.pinGate.remaining();delay(250)}}
 fun start(mode:String){flow=mode;step=if(mode=="create")2 else 1;buffer.clear();first.clear();error=""}
 fun reset(){flow="";buffer.clear();first.clear();error=""}
 androidx.activity.compose.BackHandler {if(flow.isNotEmpty())reset() else onBack()}
 var availabilityTick by remember {mutableIntStateOf(0)}
 androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME){availabilityTick++}
 val available=remember(availabilityTick){runCatching {BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)==BiometricManager.BIOMETRIC_SUCCESS}.getOrDefault(false)}
 if(flow.isNotEmpty()&&flow!="done"){
 val title=if(step==1)"Nhập PIN hiện tại" else if(step==2)"Tạo mã PIN" else "Nhập lại để xác nhận"
 StepScaffold(title,if(flow=="create")step-1 else step,{reset()}){SectionFooter("Dùng từ 6 đến 12 chữ số");PinDots(buffer.size,shake);Text(if(remaining>0)"Thử lại sau ${(remaining+999)/1000} giây" else error,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall);PinKeypad(!busy&&remaining==0L,onDigit=buffer::add,onDelete=buffer::delete)
 PillButton(if(flow=="disable")"Tắt khoá" else "Tiếp tục",{busy=true;val entered=buffer.copy();buffer.clear();scope.launch {try {when(step){1->{val valid=if(flow=="disable")vm.pinGate.disable(entered,settings) else vm.pinGate.verify(entered,settings);if(valid){if(flow=="disable")reset() else step=2}else {error="Mã PIN chưa đúng";shake++}};2->{first.clear();entered.forEach(first::add);entered.fill('\u0000');step=3;error=""};3->{val expected=first.copy();val same=expected.contentEquals(entered);expected.fill('\u0000');if(!same){entered.fill('\u0000');first.clear();step=2;error="Hai mã chưa khớp";shake++}else{val salt=PinSecurity.salt();val hash=withContext(Dispatchers.Default){try{PinSecurity.hash(entered,salt)}finally{entered.fill('\u0000')}};vm.preferences.pin(salt,hash);first.clear();flow="done"}}}}finally{busy=false;remaining=vm.pinGate.remaining()}}},Modifier.fillMaxWidth(),enabled=!busy&&buffer.size>=6&&remaining==0L)};return
 }
 if(flow=="done"){StepScaffold("Đã lưu mã PIN",3,{reset()}){Icon(AppIcons.CheckCircle,null,tint=incomeColor());Text("Dùng sinh trắc học để mở nhanh hơn?");PillButton("Bật",{vm.work {vm.preferences.flag("biometric",true)};reset()},enabled=available);PillButton("Để sau",{reset()},primary=false)};return}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(GlassTokens.margin).padding(bottom=bottomContentPadding()),verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){
 TextButton(onClick=onBack){Text("Quay lại")};LargeTitleHeader("Khoá ứng dụng")
 if(settings.pinHash.isBlank()){Icon(AppIcons.Lock,null,Modifier.size(72.dp),tint=MaterialTheme.colorScheme.primary);Text("Giữ các khoản tiền của bạn riêng tư.");PillButton("Bật khoá ứng dụng",{start("create")},Modifier.fillMaxWidth())}else GroupedList {
 SettingsRow("Khoá ứng dụng",icon=AppIcons.Lock,trailing={IosSwitch(true,{start("disable")},label="Khoá ứng dụng")})
 SettingsRow("Đổi mã PIN",icon=AppIcons.Password,onClick={start("change")})
 SettingsRow("Mở bằng sinh trắc học",if(available)"Ưu tiên trước PIN" else "Cần vân tay hoặc khuôn mặt đủ an toàn đã đăng ký",AppIcons.Fingerprint,trailing={IosSwitch(settings.biometric,{value->vm.work {vm.preferences.flag("biometric",value)}},enabled=available,label="Mở bằng sinh trắc học")})
 if(!available)PillButton("Mở cài đặt",{try {context.startActivity(android.content.Intent(android.provider.Settings.ACTION_BIOMETRIC_ENROLL).putExtra(android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,BiometricManager.Authenticators.BIOMETRIC_STRONG))}catch(_:Exception){runCatching {context.startActivity(android.content.Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS))}}},primary=false)
 SettingsRow("Tự khoá sau",timeoutLabel(settings.autoLockMinutes),AppIcons.Timer,onClick={timeout=true})
 SettingsRow("Ẩn trong đa nhiệm",icon=AppIcons.VisibilityOff,trailing={IosSwitch(settings.hideRecents,{vm.work {vm.preferences.flag("hideRecents",it)}},label="Ẩn trong đa nhiệm")})
 }
 GroupedList {Column(Modifier.background(MaterialTheme.colorScheme.tertiary.copy(alpha=.1f)).padding(GlassTokens.row),verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){Icon(AppIcons.Warning,null,tint=MaterialTheme.colorScheme.tertiary);Text("Nếu quên PIN, dữ liệu không thể khôi phục. Hãy sao lưu thường xuyên.");PillButton("Sao lưu ngay",onBackup,primary=false)}}
 }
 if(timeout)AutoLockSheet(settings.autoLockMinutes,{timeout=false}){value->vm.work {vm.preferences.number("autoLockMinutes",value)};timeout=false}
}
fun timeoutLabel(value:Int)=if(value==0)"Ngay lập tức" else "$value phút"
@Composable fun AutoLockSheet(value:Int,onDismiss:()->Unit,onSelect:(Int)->Unit){ConfirmSheet("Tự khoá sau",onDismiss){listOf(0,1,5,15).forEach {minutes->SettingsRow(timeoutLabel(minutes),if(value==minutes)"Đang chọn" else "",onClick={onSelect(minutes)})}}}
