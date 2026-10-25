package vn.chitieu.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import vn.chitieu.bank.BankLauncher
import vn.chitieu.data.Entry
import vn.chitieu.domain.*
import vn.chitieu.qr.*
@Composable fun ConfirmQrScreen(qr:VietQr,vm:LedgerViewModel,registry:BankRegistry,onBack:()->Unit,onOpened:()->Unit) {
 val context=LocalContext.current;val scope=rememberCoroutineScope();val settings by vm.settings.collectAsStateWithLifecycle();val categories by vm.categories.collectAsStateWithLifecycle();val wallets by vm.wallets.collectAsStateWithLifecycle()
 val installed=remember {registry.installed(context)};val receiver=registry.byBin(qr.bin)
 var payer by remember {mutableStateOf(installed.firstOrNull {it.packageName==settings.defaultBank}?:installed.firstOrNull())}
 var amount by remember {mutableStateOf(qr.amount?.toString()?:"")};var memo by remember {mutableStateOf(qr.memo)};var category by remember {mutableStateOf("Khác")};var wallet by remember {mutableStateOf("Ngân hàng")};var note by remember {mutableStateOf("")};var busy by remember {mutableStateOf(false)};var error by remember {mutableStateOf("")}
 LaunchedEffect(qr.account){category=vm.repo.suggest(qr.account,qr.memo)}
 LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
  item {Text("Xác nhận chuyển khoản",style=MaterialTheme.typography.headlineMedium)}
  item {Card(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp)){Text(receiver?.shortName?:"BIN chưa có trong danh sách: "+qr.bin);Text((if(qr.service=="QRIBFTTC")"Số thẻ: " else "Số tài khoản: ")+qr.account);Text("Tên từ QR: "+qr.recipientName.ifBlank {"Không có"});Text("Tên này chưa được đối chiếu với ngân hàng.")}}}
  item {OutlinedTextField(Money.input(amount)?.let {Money.format(it).removeSuffix(" ₫")}?:amount,{amount=it.filter(Char::isDigit).take(12)},label={Text("Số tiền VND")},supportingText={Text(Money.input(amount)?.let(Money::format)?:"Cần nhập số tiền")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))}
  item {OutlinedTextField(memo,{memo=it.take(200)},label={Text("Nội dung chuyển khoản")})}
  item {Choice("Danh mục",categories.map {it.name},category){category=it}}
  item {Choice("Ví trả tiền",wallets.map {it.name},wallet){wallet=it}}
  item {OutlinedTextField(note,{note=it.take(1000)},label={Text("Ghi chú riêng")})}
  item {if(installed.isEmpty())Text("Không tìm thấy app ngân hàng đã được xác minh và đang cài. Bạn có thể ghi khoản chờ rồi chuyển thủ công.") else Choice("App ngân hàng của tôi",installed.map {it.shortName},payer?.shortName.orEmpty()){name->payer=installed.first {it.shortName==name}}}
  if(qr.service=="QRIBFTTC")item {Text("QR tới thẻ: chỉ sao chép số thẻ và mở app; chưa xác minh deep link tới thẻ.")}
  if(error.isNotBlank())item {Text(error,color=MaterialTheme.colorScheme.error)}
  item {Button(enabled=!busy,onClick={val value=Money.input(amount);if(value==null)error="Số tiền không hợp lệ" else {busy=true;scope.launch {try {val e=Entry(amount=value,category=category,wallet=wallet,note=note,source=Source.QR,status=Status.PENDING,bankBin=qr.bin,recipient=qr.account,memo=memo,receivingBank=qr.recipientName,payerPackage=payer?.packageName.orEmpty());vm.repo.save(e);payer?.let {vm.preferences.text("defaultBank",it.packageName.orEmpty());vm.message.value=BankLauncher.open(context,it,receiver,e,qr.service)}?:run {vm.message.value="Đã lưu chờ. Chuyển thủ công "+Money.format(value)+"; nội dung: "+memo};onOpened()}catch(_:Exception){error="Không lưu được giao dịch. Chưa mở app ngân hàng."}finally {busy=false}}}}){Text(if(busy)"Đang lưu…" else "Lưu chờ và mở ngân hàng")}}
  item {Text("App không tự biết chuyển khoản thành công. Khi quay lại, bạn cần xác nhận; mở app ngân hàng không đồng nghĩa đã trả tiền.")}
  item {TextButton(onClick=onBack){Text("Huỷ")}}
 }
}

