package vn.chitieu.ui
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vn.chitieu.data.*
import vn.chitieu.domain.*
import java.time.*
import vn.chitieu.ui.theme.*
private fun statusLabel(status:Status)=when(status){Status.CONFIRMED->"Đã ghi nhận";Status.PENDING->"Chờ xác nhận";Status.REVIEW->"Cần xem lại (quá 24 giờ)";Status.APPROVAL->"Chờ duyệt"}
private fun sourceLabel(source:Source)=when(source){Source.MANUAL->"Thủ công";Source.QR->"QR";Source.NOTIFICATION->"Tự động từ thông báo";Source.SMS->"SMS Banking";Source.RECEIPT->"Ảnh biên lai"}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LedgerScreen(vm:LedgerViewModel,details:Boolean=false,onScan:()->Unit={},onReceipt:()->Unit={},onHistory:()->Unit={}) {
 val entries by vm.entries.collectAsStateWithLifecycle();val categories by vm.categories.collectAsStateWithLifecycle();val wallets by vm.wallets.collectAsStateWithLifecycle();val evidence by vm.evidence.collectAsStateWithLifecycle()
 var editing by remember {mutableStateOf<Entry?>(null)};var adding by remember {mutableStateOf(false)};var deleting by remember {mutableStateOf<Entry?>(null)}
 var showFilters by remember {mutableStateOf(details)}
 var month by remember {mutableStateOf(YearMonth.now(VN_ZONE))};var search by remember {mutableStateOf("")};var category by remember {mutableStateOf("Tất cả")};var wallet by remember {mutableStateOf("Tất cả")};var day by remember {mutableStateOf("")};var filter by remember {mutableStateOf("Tất cả")}
 val filtered=entries.filter {e->monthOf(e.createdAt)==month&&(category=="Tất cả"||e.category==category)&&(wallet=="Tất cả"||e.wallet==wallet)&&(day.isBlank()||Instant.ofEpochMilli(e.createdAt).atZone(VN_ZONE).toLocalDate().toString()==day)&&listOf(e.note,e.memo,e.category,e.wallet,e.recipient,e.receivingBank).any {it.contains(search,true)}&&(filter=="Tất cả"||if(filter=="Đã ghi nhận")e.status==Status.CONFIRMED else e.status!=Status.CONFIRMED)}
 val confirmed=filtered.filter {it.status==Status.CONFIRMED};val income=confirmed.filter {it.kind==Kind.INCOME}.sumOf {it.amount};val expense=confirmed.filter {it.kind==Kind.EXPENSE}.sumOf {it.amount};val expenseGroups=confirmed.filter {it.kind==Kind.EXPENSE}.groupBy {it.category}.mapValues {it.value.sumOf {e->e.amount}}.toList().sortedByDescending {it.second}
 Scaffold(containerColor=Color.Transparent,topBar={GlassTopBar(if(details)"Giao dịch" else "ChiTieu",if(details)"Tìm và xem lại các khoản thu chi" else "Mỗi khoản tiền, rõ ràng hơn")},floatingActionButton={if(details)FloatingActionButton(onClick={adding=true}){Text("＋")}}) {padding->
  LazyColumn(Modifier.padding(padding).padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   if(!details)item {Column(verticalArrangement=Arrangement.spacedBy(10.dp)){GlassButton("Quét QR",onScan,Modifier.fillMaxWidth());Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){GlassButton("Thêm nhanh",{adding=true},Modifier.weight(1f));GlassButton("Đọc ảnh biên lai",onReceipt,Modifier.weight(1f))}}}
   item {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){TextButton(onClick={month=month.minusMonths(1)}){Text("‹ Trước")};Text(month.monthValue.toString()+" / "+month.year,Modifier.padding(top=12.dp));TextButton(onClick={month=month.plusMonths(1)}){Text("Sau ›")}}}
   item {GlassCard(Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp)){Text("Chênh lệch thu chi");Text(Money.format(income-expense),style=MaterialTheme.typography.headlineLarge);Text("Thu "+Money.format(income));Text("Chi "+Money.format(expense));Text("Theo bộ lọc hiện tại · Chỉ giao dịch đã ghi nhận")}}}
   if(expenseGroups.isNotEmpty())item {Card(Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp)){Text("Chi theo danh mục",style=MaterialTheme.typography.titleMedium);Canvas(Modifier.fillMaxWidth().height(140.dp).padding(12.dp)){var start=-90f;val stroke=Stroke(width=24f);expenseGroups.forEachIndexed {i,(name,value)->val sweep=(value.toDouble()/expense*360).toFloat();val color=categories.firstOrNull {it.name==name}?.let {Color(it.color)}?:Color(0xFF267D66);drawArc(color,start,sweep,false,topLeft=androidx.compose.ui.geometry.Offset(size.width/2-55f,size.height/2-55f),size=androidx.compose.ui.geometry.Size(110f,110f),style=stroke);start+=sweep}};expenseGroups.forEach {(name,value)->Text(name+": "+Money.format(value))}}}}
   item {TextButton(onClick={showFilters=!showFilters}){Text(if(showFilters)"Ẩn bộ lọc" else "Tìm và lọc giao dịch")}}
   if(showFilters){
   item {OutlinedTextField(search,{search=it.take(200)},label={Text("Tìm ghi chú, tài khoản, nội dung…")},modifier=Modifier.fillMaxWidth(),singleLine=true)}
   item {Choice("Danh mục",listOf("Tất cả")+categories.map {it.name},category){category=it};Choice("Ví",listOf("Tất cả")+wallets.map {it.name},wallet){wallet=it};Choice("Trạng thái",listOf("Tất cả","Đã ghi nhận","Chờ xử lý"),filter){filter=it}}
   item {OutlinedTextField(day,{day=it.take(10)},label={Text("Lọc ngày yyyy-MM-dd (để trống = cả tháng)")},modifier=Modifier.fillMaxWidth(),singleLine=true);if(day.isNotBlank()&&runCatching {LocalDate.parse(day)}.isFailure)Text("Ngày chưa đúng định dạng",color=MaterialTheme.colorScheme.error)}
   }
   if(!details)item {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Gần đây",style=MaterialTheme.typography.titleLarge);TextButton(onClick=onHistory){Text("Tất cả")}}}
   if(entries.any {it.status==Status.REVIEW})item {Text("Có giao dịch chờ quá 24 giờ cần xem lại. Chọn tháng tương ứng và lọc Chờ xử lý.",color=MaterialTheme.colorScheme.error)}
   if(filtered.isEmpty())item {Text("Chưa có giao dịch phù hợp. Chạm ＋ để thêm thu chi hoặc thay bộ lọc.")}
   items(if(details)filtered else filtered.take(6),key={it.id}) {e->Card(onClick={editing=e},modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text((categories.firstOrNull {it.name==e.category}?.icon?:"●")+" "+e.category+" · "+Money.format(e.amount),style=MaterialTheme.typography.titleMedium);Text((if(e.kind==Kind.INCOME)"Thu" else "Chi")+" · "+e.wallet);Text(Instant.ofEpochMilli(e.createdAt).atZone(VN_ZONE).toLocalDateTime().withNano(0).toString().replace('T',' '));Text(sourceLabel(e.source)+" · "+statusLabel(e.status));evidence.firstOrNull {it.entryId==e.id}?.let {meta->if(meta.recipientName.isNotBlank())Text(meta.recipientName);if(meta.transactionId.isNotBlank())Text("Mã giao dịch: "+meta.transactionId)};if(e.note.isNotBlank())Text(e.note);if(e.recipient.isNotBlank())Text("Người nhận: "+e.recipient);if(e.memo.isNotBlank())Text("Nội dung: "+e.memo);if(e.status!=Status.CONFIRMED)Row {TextButton(onClick={vm.work {vm.repo.confirm(e.id)}}){Text(if(e.status==Status.APPROVAL)"Duyệt" else "Đã chuyển")};TextButton(onClick={deleting=e}){Text("Huỷ")}}}}}
   item {Spacer(Modifier.height(90.dp))}
  }
 }
 if(adding)QuickAdd(vm,onDismiss={adding=false},onSave={vm.save(it);adding=false})
 if(editing!=null)EntryEditor(editing,categories.map {it.name},wallets.map {it.name},onDismiss={adding=false;editing=null},onSave={vm.save(it);adding=false;editing=null},onDelete={id->deleting=entries.firstOrNull {it.id==id};editing=null})
 deleting?.let {e->AlertDialog(onDismissRequest={deleting=null},title={Text("Xoá giao dịch?")},text={Text(Money.format(e.amount)+" · "+e.category)},confirmButton={TextButton(onClick={vm.delete(e.id);deleting=null}){Text("Xoá")}},dismissButton={TextButton(onClick={deleting=null}){Text("Giữ lại")}})}
}
@Composable fun EntryEditor(entry:Entry?,categories:List<String>,wallets:List<String>,onDismiss:()->Unit,onSave:(Entry)->Unit,onDelete:(String)->Unit) {
 var amount by remember {mutableStateOf(entry?.amount?.toString()?:"")}
 var note by remember {mutableStateOf(entry?.note?:"")}
 var kind by remember {mutableStateOf(entry?.kind?:Kind.EXPENSE)}
 var category by remember {mutableStateOf(entry?.category?:"Khác")}
 var wallet by remember {mutableStateOf(entry?.wallet?:"Tiền mặt")}
 var date by remember {mutableStateOf(Instant.ofEpochMilli(entry?.createdAt?:System.currentTimeMillis()).atZone(VN_ZONE).toLocalDateTime().withSecond(0).withNano(0).toString())}
 var invalid by remember {mutableStateOf(false)}
 AlertDialog(onDismissRequest=onDismiss,title={Text(if(entry==null) "Thêm thu chi" else "Sửa giao dịch")},text={LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)) {
  item {Row {FilterChip(selected=kind==Kind.EXPENSE,onClick={kind=Kind.EXPENSE},label={Text("Chi")});Spacer(Modifier.width(8.dp));FilterChip(selected=kind==Kind.INCOME,onClick={kind=Kind.INCOME},label={Text("Thu")})}}
  item {OutlinedTextField(Money.input(amount)?.let {Money.format(it).removeSuffix(" ₫")}?:amount,{amount=it.filter(Char::isDigit).take(12)},label={Text("Số tiền VND")},supportingText={Text(Money.input(amount)?.let(Money::format)?:"Nhập số nguyên dương")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true)}
  item {Choice("Danh mục",categories.ifEmpty {Defaults.categories},category){category=it}}
  item {Choice("Ví",wallets.ifEmpty {listOf("Tiền mặt")},wallet){wallet=it}}
  item {OutlinedTextField(date,{date=it},label={Text("Ngày giờ: yyyy-MM-ddTHH:mm")},singleLine=true)}
  item {OutlinedTextField(note,{note=it.take(1000)},label={Text("Ghi chú")})}
  if(invalid)item {Text("Số tiền hoặc ngày giờ không hợp lệ",color=MaterialTheme.colorScheme.error)}
  if(entry!=null)item {TextButton(onClick={onDelete(entry.id)}) {Text("Xoá giao dịch")}}
 }},confirmButton={TextButton(onClick={val value=Money.input(amount);val time=runCatching {LocalDateTime.parse(date).atZone(VN_ZONE).toInstant().toEpochMilli()}.getOrNull();if(value==null||time==null)invalid=true else onSave((entry?:Entry(amount=value)).copy(amount=value,kind=kind,category=category,wallet=wallet,createdAt=time,note=note))}) {Text("Lưu")}},dismissButton={TextButton(onClick=onDismiss){Text("Huỷ")}})
}
@Composable fun Choice(label:String,options:List<String>,selected:String,onSelect:(String)->Unit) {
 var open by remember {mutableStateOf(false)}
 Box {OutlinedButton(onClick={open=true},modifier=Modifier.fillMaxWidth()){Text(label+": "+selected)};DropdownMenu(expanded=open,onDismissRequest={open=false}) {options.forEach {value->DropdownMenuItem(text={Text(value)},onClick={onSelect(value);open=false})}}}
}





@Composable fun QuickAdd(vm:LedgerViewModel,onDismiss:()->Unit,onSave:(Entry)->Unit) {
 val categories by vm.categories.collectAsStateWithLifecycle();var amount by remember {mutableStateOf("")};var category by remember {mutableStateOf("Ăn uống")};var kind by remember {mutableStateOf(Kind.EXPENSE)}
 GlassBottomSheet(onDismiss) {Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){Text("Thêm nhanh",style=MaterialTheme.typography.headlineSmall);Row {FilterChip(kind==Kind.EXPENSE,{kind=Kind.EXPENSE},label={Text("Chi")});Spacer(Modifier.width(12.dp));FilterChip(kind==Kind.INCOME,{kind=Kind.INCOME},label={Text("Thu")})};AmountInput(Money.input(amount)?.let {Money.format(it).removeSuffix(" ₫")}?:amount){amount=it.filter(Char::isDigit).take(12)};Choice("Danh mục",categories.map {it.name},category){category=it};GlassButton("Lưu",enabled=Money.input(amount)!=null,onClick={onSave(Entry(amount=Money.input(amount)!!,category=category,kind=kind))},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(24.dp))}}
}
