package vn.chitieu.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.biometric.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import vn.chitieu.data.Settings
import vn.chitieu.domain.PinSecurity
@Composable fun LockScreen(settings:Settings,onUnlock:()->Unit) {
 val context=LocalContext.current;val scope=rememberCoroutineScope();var pin by remember {mutableStateOf("")};var error by remember {mutableStateOf("")};var busy by remember {mutableStateOf(false)};var failures by remember {mutableIntStateOf(0)};var lockedUntil by remember {mutableLongStateOf(0)}
 Column(Modifier.fillMaxSize().padding(32.dp),verticalArrangement=Arrangement.Center) {
  Text("Mở khoá ChiTieu",style=MaterialTheme.typography.headlineMedium);Text("Dữ liệu tài chính của bạn được giữ riêng tư.");Spacer(Modifier.height(24.dp))
  OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(12)},label={Text("PIN 6–12 số")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),singleLine=true)
  Text(error,color=MaterialTheme.colorScheme.error)
  Button(enabled=!busy,onClick={if(System.currentTimeMillis()<lockedUntil)error="Thử lại sau 30 giây" else {busy=true;scope.launch {val ok=withContext(Dispatchers.Default){PinSecurity.verify(pin,settings.pinSalt,settings.pinHash)};pin="";busy=false;if(ok){failures=0;onUnlock()}else {failures++;if(failures>=5){lockedUntil=System.currentTimeMillis()+30000;failures=0};error="PIN không đúng"}}}}){Text(if(busy)"Đang kiểm tra…" else "Mở khoá")}
  if(settings.biometric)OutlinedButton(onClick={val activity=context as? FragmentActivity;if(activity==null)error="Không mở được xác thực sinh trắc học" else try {val prompt=BiometricPrompt(activity,ContextCompat.getMainExecutor(context),object:BiometricPrompt.AuthenticationCallback(){override fun onAuthenticationSucceeded(result:BiometricPrompt.AuthenticationResult){onUnlock()};override fun onAuthenticationError(code:Int,message:CharSequence){error="Không thể dùng vân tay. Bạn có thể dùng PIN."}});prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Mở khoá ChiTieu").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG).setNegativeButtonText("Dùng PIN").build())}catch(_:Exception){error="Không có vân tay khả dụng. Dùng PIN."}}){Text("Dùng vân tay")}
 }
}
@Composable fun SecuritySettings(vm:LedgerViewModel,settings:Settings) {
 val context=LocalContext.current;var current by remember {mutableStateOf("")};var pin by remember {mutableStateOf("")};var confirm by remember {mutableStateOf("")};var error by remember {mutableStateOf("")};var busy by remember {mutableStateOf(false)};val scope=rememberCoroutineScope()
 Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
  Text("Khoá ứng dụng",style=MaterialTheme.typography.titleLarge);Text("PIN được băm PBKDF2 với salt ngẫu nhiên, không lưu PIN gốc. Quên PIN: dữ liệu không có cơ chế khôi phục bằng cloud. Sao lưu file trước khi cài lại.")
  if(settings.pinHash.isNotBlank())OutlinedTextField(current,{current=it.filter(Char::isDigit).take(12)},label={Text("PIN hiện tại")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword))
  OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(12)},label={Text("PIN mới (6–12 số)")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword))
  OutlinedTextField(confirm,{confirm=it.filter(Char::isDigit).take(12)},label={Text("Nhập lại PIN mới")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword))
  Button(enabled=!busy,onClick={if(!Regex("[0-9]{6,12}").matches(pin)||pin!=confirm)error="PIN cần 6–12 số và nhập lại giống nhau" else {busy=true;scope.launch {try {val valid=withContext(Dispatchers.Default){settings.pinHash.isEmpty()||PinSecurity.verify(current,settings.pinSalt,settings.pinHash)};if(!valid)error="PIN hiện tại không đúng" else {val salt=PinSecurity.salt();val hash=withContext(Dispatchers.Default){PinSecurity.hash(pin,salt)};vm.preferences.pin(salt,hash);pin="";confirm="";current="";error="Đã cập nhật PIN"}}finally {busy=false}}}}){Text("Lưu PIN")}
  if(settings.pinHash.isNotBlank())TextButton(enabled=!busy,onClick={busy=true;scope.launch {try {val valid=withContext(Dispatchers.Default){PinSecurity.verify(current,settings.pinSalt,settings.pinHash)};if(valid){vm.preferences.pin("","");vm.preferences.flag("biometric",false);current="";error="Đã tắt khoá"}else error="PIN hiện tại không đúng"}finally {busy=false}}}){Text("Tắt khoá (cần PIN hiện tại)")}
  Toggle("Cho phép vân tay (cần có PIN)",settings.biometric){enabled->if(enabled&&settings.pinHash.isBlank())error="Hãy đặt PIN trước" else if(enabled&&BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)!=BiometricManager.BIOMETRIC_SUCCESS)error="Chưa có sinh trắc học mạnh hoặc chưa đăng ký vân tay" else vm.work {vm.preferences.flag("biometric",enabled)}}
  if(error.isNotBlank())Text(error)
 }
}
