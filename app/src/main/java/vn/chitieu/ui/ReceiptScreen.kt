package vn.chitieu.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import vn.chitieu.receipt.*
import vn.chitieu.domain.*
import vn.chitieu.ui.theme.*
import java.time.*
@Composable fun ReceiptScreen(vm:LedgerViewModel,onChoose:()->Unit,onDone:()->Unit) {
 val state by vm.receiptState.collectAsStateWithLifecycle();val categories by vm.categories.collectAsStateWithLifecycle();val wallets by vm.wallets.collectAsStateWithLifecycle();val settings by vm.settings.collectAsStateWithLifecycle()
 Column(Modifier.fillMaxSize()) {
  GlassTopBar("Đọc ảnh biên lai","Xử lý trên điện thoại · Luôn kiểm tra trước khi lưu")
  if(state.busy)Column(Modifier.padding(24.dp)){LinearProgressIndicator(Modifier.fillMaxWidth());Text("Đang đọc ảnh…")}
  else if(state.error.isNotBlank())Column(Modifier.padding(24.dp)){Text(state.error);GlassButton("Chọn lại ảnh",onChoose)}
  else if(state.scan==null)Column(Modifier.padding(24.dp)){Text("Chọn ảnh biên lai hoặc chia sẻ ảnh từ thư viện vào ChiTieu.");GlassButton("Chọn ảnh",onChoose)}
  else {val scan=state.scan!!;val draft=scan.draft
   if(draft.status.value!=ReceiptStatus.SUCCESS)Column(Modifier.padding(24.dp)){Text(when(draft.status.value){ReceiptStatus.FAILED->"Ảnh ghi nhận giao dịch thất bại hoặc đang chờ xử lý.";ReceiptStatus.UNSUPPORTED->"Ảnh này là yêu cầu hoặc loại giao dịch chưa hỗ trợ, chưa phải biên lai chuyển khoản hoàn tất.";else->"Chưa tìm thấy thông tin giao dịch thành công trong ảnh."});GlassButton("Chọn ảnh khác",onChoose)}
   else key(scan.digest) {
    var amount by remember {mutableStateOf(draft.amount.value?.toString().orEmpty())};var name by remember {mutableStateOf(draft.recipient.value.orEmpty())};var account by remember {mutableStateOf(draft.account.value.orEmpty())};var bank by remember {mutableStateOf(draft.recipientBank.value.orEmpty())};var memo by remember {mutableStateOf(draft.memo.value.orEmpty())};var tx by remember {mutableStateOf(draft.transactionId.value.orEmpty())};var time by remember {mutableStateOf(draft.time.value?.let {Instant.ofEpochMilli(it).atZone(VN_ZONE).toLocalDateTime().toString()}.orEmpty())};var category by remember {mutableStateOf("Khác")};var wallet by remember {mutableStateOf(draft.sourceBank.ifBlank {"Ngân hàng"})};var mergeId by remember {mutableStateOf(state.candidates.firstOrNull {it.id==vm.receiptTarget.value}?.id?:state.candidates.firstOrNull()?.id)};var busy by remember {mutableStateOf(false)};var error by remember {mutableStateOf("")};val scope=rememberCoroutineScope()
    LaunchedEffect(Unit){category=vm.repo.suggest(account,memo)}
    LazyColumn(Modifier.padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=30.dp)) {
     item {GlassCard(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp)){Text("Giao dịch thành công",style=MaterialTheme.typography.titleLarge);Text("Kiểm tra các ô được nhắc bên dưới, đặc biệt số tiền và người nhận.")}}}
     item {AmountInput(Money.input(amount)?.let {Money.format(it).removeSuffix(" ₫")}?:amount){amount=it.filter(Char::isDigit).take(12)}}
     if(draft.amount.confidence<.8f)item {Text("Cần kiểm tra lại số tiền",color=MaterialTheme.colorScheme.tertiary)}
     item {ReceiptInput("Người nhận",name,draft.recipient.confidence){name=it.take(200)}}
     item {ReceiptInput("Số tài khoản",account,draft.account.confidence){account=it.take(100)}}
     item {ReceiptInput("Ngân hàng nhận",bank,draft.recipientBank.confidence){bank=it.take(200)}}
     item {ReceiptInput("Nội dung",memo,draft.memo.confidence){memo=it.take(1000)}}
     item {ReceiptInput("Thời gian yyyy-MM-ddTHH:mm",time,draft.time.confidence){time=it};if(time.isBlank())TextButton(onClick={time=LocalDateTime.now(VN_ZONE).withNano(0).toString()}){Text("Dùng giờ hiện tại")}}
     item {ReceiptInput("Mã giao dịch",tx,draft.transactionId.confidence){tx=it.take(100)}}
     item {Choice("Danh mục",categories.map {it.name},category){category=it};Choice("Ví",(wallets.map {it.name}+draft.sourceBank).distinct(),wallet){wallet=it}}
     if(state.candidates.isNotEmpty())item {val labels=state.candidates.mapIndexed {i,e->"Gộp "+(i+1)+": "+Money.format(e.amount)+" · "+e.recipient.ifBlank {e.wallet}};val index=state.candidates.indexOfFirst {it.id==mergeId};Text("Có giao dịch gần giống. Mặc định gộp để tránh ghi hai lần.");Choice("Cách lưu",listOf("Tạo khoản chi mới")+labels,if(index<0)"Tạo khoản chi mới" else labels[index]){label->mergeId=state.candidates.getOrNull(labels.indexOf(label))?.id}}
     item {Text(if(settings.keepReceipt)"Ảnh sẽ được lưu riêng trên máy cùng giao dịch." else "Ảnh không được giữ lại sau khi xử lý.")}
     if(error.isNotBlank())item {Text(error,color=MaterialTheme.colorScheme.error)}
     item {GlassButton(if(busy)"Đang lưu…" else if(mergeId!=null)"Xác nhận và gộp khoản chi" else "Lưu khoản chi",enabled=!busy,onClick={val money=Money.input(amount);val whenAt=runCatching {LocalDateTime.parse(time).atZone(VN_ZONE).toInstant().toEpochMilli()}.getOrNull();if(money==null||whenAt==null)error="Kiểm tra số tiền và thời gian trước khi lưu." else {busy=true;scope.launch {try {vm.receipts.save(scan,money,name,account,bank,memo,whenAt,tx,category,wallet,mergeId,settings.keepReceipt);vm.repo.dao.saveWallet(vn.chitieu.data.Wallet(wallet));vm.receiptState.value=ReceiptUiState();vm.receiptTarget.value=null;onDone()}catch(_:Exception){error="Chưa lưu được. Hãy kiểm tra dữ liệu hoặc chọn lại ảnh."}finally {busy=false}}}})}
     item {TextButton(onClick=onChoose){Text("Chọn ảnh khác")}}
    }
   }
  }
 }
}
@Composable private fun ReceiptInput(label:String,value:String,confidence:Float,onChange:(String)->Unit) {OutlinedTextField(value,onChange,label={Text(label)},modifier=Modifier.fillMaxWidth(),supportingText={if(confidence<.8f)Text("Cần kiểm tra lại",color=MaterialTheme.colorScheme.tertiary)},colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=if(confidence<.8f)MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline))}
