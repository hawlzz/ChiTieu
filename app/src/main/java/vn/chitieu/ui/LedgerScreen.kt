package vn.chitieu.ui
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vn.chitieu.data.*
import vn.chitieu.domain.*
import vn.chitieu.ui.theme.*
import java.time.*
import java.time.format.DateTimeFormatter
private fun statusLabel(s:Status)=when(s){Status.CONFIRMED->"Đã ghi nhận";Status.PENDING->"Chờ xác nhận";Status.REVIEW->"Cần xem lại";Status.APPROVAL->"Chờ duyệt"}
private fun sourceLabel(s:Source)=when(s){Source.MANUAL->"Thủ công";Source.QR->"QR";Source.NOTIFICATION->"Thông báo";Source.SMS->"SMS";Source.RECEIPT->"Biên lai"}
@OptIn(ExperimentalMaterial3Api::class,ExperimentalFoundationApi::class)
@Composable fun LedgerScreen(vm:LedgerViewModel,details:Boolean=false,onScan:()->Unit={},onReceipt:()->Unit={},onHistory:()->Unit={}) {
 val entries by vm.entries.collectAsStateWithLifecycle();val categories by vm.categories.collectAsStateWithLifecycle();val wallets by vm.wallets.collectAsStateWithLifecycle();val evidence by vm.evidence.collectAsStateWithLifecycle()
 var viewing by remember {mutableStateOf<Entry?>(null)}
 var editing by remember {mutableStateOf<Entry?>(null)};var adding by remember {mutableStateOf(false)};var deleting by remember {mutableStateOf<Entry?>(null)}
 var showFilters by remember {mutableStateOf(false)};var monthPicker by remember {mutableStateOf(false)};var info by remember {mutableStateOf(false)}
 var month by remember {mutableStateOf(YearMonth.now(VN_ZONE))};var search by remember {mutableStateOf("")};var category by remember {mutableStateOf("Tất cả")};var wallet by remember {mutableStateOf("Tất cả")};var day by remember {mutableStateOf("")};var filter by remember {mutableStateOf("Tất cả")};var kindFilter by remember {mutableStateOf("Tất cả")}
 val listState=rememberLazyListState();val compact by remember {derivedStateOf {listState.firstVisibleItemIndex>0}}
 val haptic=LocalHapticFeedback.current
 val filtered=entries.filter {e->monthOf(e.createdAt)==month&&(category=="Tất cả"||e.category==category)&&(wallet=="Tất cả"||e.wallet==wallet)&&(day.isBlank()||Instant.ofEpochMilli(e.createdAt).atZone(VN_ZONE).toLocalDate().toString()==day)&&listOf(e.note,e.memo,e.category,e.wallet,e.recipient,e.receivingBank).any {it.contains(search,true)}&&(filter=="Tất cả"||if(filter=="Đã ghi nhận")e.status==Status.CONFIRMED else e.status!=Status.CONFIRMED)&&(kindFilter=="Tất cả"||e.kind==if(kindFilter=="Thu")Kind.INCOME else Kind.EXPENSE)}
 val confirmed=filtered.filter {it.status==Status.CONFIRMED};val income=confirmed.filter {it.kind==Kind.INCOME}.sumOf {it.amount};val expense=confirmed.filter {it.kind==Kind.EXPENSE}.sumOf {it.amount}
 Column(Modifier.fillMaxSize().padding(horizontal=GlassTokens.margin)) {
  LargeTitleHeader(if(details)"Giao dịch" else "Tổng quan",compact){FilledIconButton(onClick={adding=true;haptic.performHapticFeedback(HapticFeedbackType.LongPress)},shape=CircleShape){Icon(Icons.Rounded.Add,"Thêm nhanh")}}
  LazyColumn(Modifier.fillMaxSize(),state=listState,verticalArrangement=Arrangement.spacedBy(GlassTokens.gap),contentPadding=PaddingValues(bottom=GlassTokens.bottom)) {
   item {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){IconButton(onClick={month=month.minusMonths(1)}){Icon(Icons.Rounded.ChevronLeft,"Tháng trước")};Surface(onClick={monthPicker=true},shape=CircleShape,color=MaterialTheme.colorScheme.surface){Text("Tháng ${month.monthValue}, ${month.year}",Modifier.padding(horizontal=16.dp,vertical=10.dp),style=MaterialTheme.typography.titleSmall)};IconButton(onClick={month=month.plusMonths(1)}){Icon(Icons.Rounded.ChevronRight,"Tháng sau")}}}
   if(!details)item {var drag=0f;GlassCard(Modifier.fillMaxWidth().pointerInput(month){detectHorizontalDragGestures(onDragEnd={if(kotlin.math.abs(drag)>80)month=if(drag<0)month.plusMonths(1) else month.minusMonths(1);drag=0f}){_,delta->drag+=delta}}){Column(Modifier.padding(GlassTokens.margin),verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){
    Row(verticalAlignment=Alignment.CenterVertically){Text("Chênh lệch tháng này",Modifier.weight(1f),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);IconButton(onClick={info=true},Modifier.size(24.dp)){Icon(Icons.Rounded.Info,"Thông tin tổng tiền",Modifier.size(18.dp))}}
    AmountText(income-expense,color=if(income>=expense)incomeColor() else expenseColor(),animate=true)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(GlassTokens.gap)){listOf(Triple("Thu",income,incomeColor()),Triple("Chi",expense,expenseColor())).forEach {(label,value,color)->Column(Modifier.weight(1f).clip(RoundedCornerShape(GlassTokens.controlCorner)).background(color.copy(alpha=.07f)).padding(12.dp)){Row {Icon(if(label=="Thu")Icons.Rounded.SouthWest else Icons.Rounded.NorthEast,null,tint=color,modifier=Modifier.size(16.dp));Text(label,color=color,style=MaterialTheme.typography.bodySmall)};AmountText(value,large=false)}}}
    if(expense>0){val groups=confirmed.filter {it.kind==Kind.EXPENSE}.groupBy {it.category}.mapValues {it.value.sumOf {e->e.amount}};Row(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),horizontalArrangement=Arrangement.spacedBy(3.dp)){groups.forEach {(name,value)->Box(Modifier.weight((value.toDouble()/expense).toFloat().coerceAtLeast(.001f)).fillMaxHeight().background(categories.firstOrNull {it.name==name}?.let {Color(it.color)}?:expenseColor()))}}}
   }}}
   if(!details)item {Row(horizontalArrangement=Arrangement.spacedBy(GlassTokens.small)){listOf(Triple("Quét QR",Icons.Rounded.QrCodeScanner,onScan),Triple("Thêm nhanh",Icons.Rounded.Add,{adding=true}),Triple("Biên lai",Icons.Rounded.Image,onReceipt)).forEachIndexed {i,(label,icon,action)->val color=if(i==0)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface;Column(Modifier.weight(1f).clip(RoundedCornerShape(GlassTokens.corner)).background(color).clickable {haptic.performHapticFeedback(HapticFeedbackType.LongPress);action()}.padding(vertical=20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){Icon(icon,if(label=="Biên lai")"Đọc ảnh biên lai" else null,tint=if(i==0)Color.White else MaterialTheme.colorScheme.primary,modifier=Modifier.size(28.dp));Text(label,color=if(i==0)Color.White else MaterialTheme.colorScheme.onSurface,style=MaterialTheme.typography.labelSmall)}}}}
   item {SearchBar(search){search=it.take(200)}}
   if(details)item {SegmentedControl(listOf("Tất cả","Chi","Thu"),kindFilter){kindFilter=it}}
   item {Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){Chip("Bộ lọc",showFilters){showFilters=!showFilters};Chip(category,category!="Tất cả"){showFilters=true};Chip(if(wallet=="Tất cả")"Mọi ví" else wallet,wallet!="Tất cả"){showFilters=true};Chip("Chờ xử lý",filter=="Chờ xử lý"){filter=if(filter=="Chờ xử lý")"Tất cả" else "Chờ xử lý"}}}
   if(showFilters)item {GroupedList {Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Choice("Danh mục",listOf("Tất cả")+categories.map {it.name},category){category=it};Choice("Ví",listOf("Tất cả")+wallets.map {it.name},wallet){wallet=it};Choice("Trạng thái",listOf("Tất cả","Đã ghi nhận","Chờ xử lý"),filter){filter=it};OutlinedTextField(day,{day=it.take(10)},label={Text("Ngày yyyy-MM-dd")},singleLine=true);TextButton(onClick={category="Tất cả";wallet="Tất cả";day="";filter="Tất cả";kindFilter="Tất cả"}){Text("Đặt lại bộ lọc")}}}}
   if(!details)item {Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Gần đây",Modifier.weight(1f),style=MaterialTheme.typography.titleLarge);Chip("Xem tất cả",onClick=onHistory)}}
   if(entries.any {it.status==Status.REVIEW})item {Text("Có khoản chờ quá 24 giờ cần xem lại.",color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)}
   if(filtered.isEmpty())item {EmptyState(onScan,{adding=true})}
   val shown=if(details)filtered else filtered.take(6)
   shown.groupBy {Instant.ofEpochMilli(it.createdAt).atZone(VN_ZONE).toLocalDate()}.forEach {(date,rows)->
    item(key="date-$date"){val today=LocalDate.now(VN_ZONE);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(when(date){today->"Hôm nay";today.minusDays(1)->"Hôm qua";else->date.format(DateTimeFormatter.ofPattern("dd 'thg' MM"))},style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);if(details)Text(Money.format(rows.filter {it.status==Status.CONFIRMED}.sumOf {if(it.kind==Kind.INCOME)it.amount else -it.amount}),style=MaterialTheme.typography.labelMedium)}}
    items(rows,key={it.id}) {e->var menu by remember {mutableStateOf(false)}
     val swipe=rememberSwipeToDismissBoxState(confirmValueChange={value->if(value==SwipeToDismissBoxValue.EndToStart)deleting=e else if(value==SwipeToDismissBoxValue.StartToEnd)editing=e;false})
     SwipeToDismissBox(state=swipe,backgroundContent={Row(Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.error.copy(alpha=.1f)).padding(16.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Sửa");Text("Xoá",color=MaterialTheme.colorScheme.error)}}){
      Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).combinedClickable(onClick={viewing=e},onLongClick={menu=true}).padding(16.dp)){
       Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){CategoryIcon(e.category,categories.firstOrNull {it.name==e.category}?.let {Color(it.color)}?:MaterialTheme.colorScheme.primary);Column(Modifier.weight(1f)){Text(e.note.ifBlank {e.category},maxLines=1,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.titleMedium);Text(e.wallet+" · "+sourceLabel(e.source),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);if(e.status!=Status.CONFIRMED)Text(statusLabel(e.status),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.tertiary)};AmountText(e.amount,Modifier.widthIn(max=150.dp),color=if(e.kind==Kind.INCOME)incomeColor() else expenseColor(),large=false)}
       if(e.status!=Status.CONFIRMED)Row {TextButton(onClick={vm.work {vm.repo.confirm(e.id)}}){Text(if(e.status==Status.APPROVAL)"Duyệt" else "Đã chuyển")};TextButton(onClick={deleting=e}){Text("Huỷ")}}
       DropdownMenu(menu,{menu=false}){DropdownMenuItem(text={Text("Chi tiết")},onClick={menu=false;viewing=e});DropdownMenuItem(text={Text("Sửa")},onClick={menu=false;editing=e});DropdownMenuItem(text={Text("Xoá")},onClick={menu=false;deleting=e})}
      }
     }
    }
   }
  }
 }
 if(monthPicker)GlassBottomSheet({monthPicker=false}){var year by remember {mutableIntStateOf(month.year)};Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){TextButton(onClick={year--}){Text("‹")};Text("$year",style=MaterialTheme.typography.titleLarge);TextButton(onClick={year++}){Text("›")}};(1..12).chunked(3).forEach {row->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){row.forEach {m->OutlinedButton(onClick={month=YearMonth.of(year,m);monthPicker=false},Modifier.weight(1f)){Text("Tháng $m")}}};Spacer(Modifier.height(24.dp))}}}
 if(info)GlassDialog({info=false}){Text("Tổng tiền theo bộ lọc",style=MaterialTheme.typography.titleLarge);Text("Chỉ tính các giao dịch đã ghi nhận trong tháng và bộ lọc hiện tại. Khoản đang chờ được tính sau khi xác nhận.");TextButton(onClick={info=false}){Text("Đã hiểu")}}
 viewing?.let {e->EntryDetailsSheet(e,evidence.firstOrNull {it.entryId==e.id},{viewing=null},{viewing=null;editing=e})}
 if(adding)QuickAdd(vm,{adding=false}){vm.save(it);adding=false;haptic.performHapticFeedback(HapticFeedbackType.LongPress)}
 editing?.let {EntryEditor(it,categories.map {c->c.name},wallets.map {w->w.name},{editing=null},{vm.save(it);editing=null},{id->deleting=entries.firstOrNull {it.id==id};editing=null})}
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
 Box {GroupedList {GroupedRow(label,selected,onClick={open=true},divider=false)};DropdownMenu(expanded=open,onDismissRequest={open=false}) {options.forEach {value->DropdownMenuItem(text={Text(value)},onClick={onSelect(value);open=false})}}}
}





@Composable fun QuickAdd(vm:LedgerViewModel,onDismiss:()->Unit,onSave:(Entry)->Unit) {
 val categories by vm.categories.collectAsStateWithLifecycle();val wallets by vm.wallets.collectAsStateWithLifecycle()
 var amount by remember {mutableStateOf("")};var category by remember {mutableStateOf("Ăn uống")};var kind by remember {mutableStateOf(Kind.EXPENSE)};var wallet by remember {mutableStateOf("Tiền mặt")};var note by remember {mutableStateOf("")};var showCategories by remember {mutableStateOf(false)}
 GlassBottomSheet(onDismiss){Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Text("Thêm nhanh",style=MaterialTheme.typography.titleLarge)
  SegmentedControl(listOf("Chi","Thu"),if(kind==Kind.INCOME)"Thu" else "Chi"){kind=if(it=="Thu")Kind.INCOME else Kind.EXPENSE}
  Box(Modifier.fillMaxWidth().padding(vertical=12.dp),contentAlignment=Alignment.Center){AmountText(amount.toLongOrNull()?:0,color=if(kind==Kind.INCOME)incomeColor() else expenseColor())}
  Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){CategoryIcon(category);TextButton(onClick={showCategories=!showCategories}){Text(category+" ⌄")};Box(Modifier.weight(1f)){Choice("Ví",wallets.map {it.name}.ifEmpty {listOf("Tiền mặt")},wallet){wallet=it}}}
  if(showCategories)categories.chunked(3).forEach {row->Row(Modifier.fillMaxWidth()){row.forEach {c->Column(Modifier.weight(1f).clickable {category=c.name;showCategories=false}.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally){CategoryIcon(c.name,Color(c.color));Text(c.name,style=MaterialTheme.typography.labelSmall,maxLines=2)}}}}
  OutlinedTextField(note,{note=it.take(1000)},label={Text("Ghi chú (tuỳ chọn)")},modifier=Modifier.fillMaxWidth(),singleLine=true)
  listOf(listOf("1","2","3"),listOf("4","5","6"),listOf("7","8","9"),listOf("000","0","⌫")).forEach {keys->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){keys.forEach {digit->Surface(onClick={amount=if(digit=="⌫")amount.dropLast(1) else (amount+digit).trimStart('0').take(12)},modifier=Modifier.weight(1f),shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.onSurface.copy(alpha=.04f)){Text(digit,Modifier.padding(vertical=10.dp),style=MaterialTheme.typography.headlineSmall,textAlign=androidx.compose.ui.text.style.TextAlign.Center)}}}}
  PillButton("Lưu",{onSave(Entry(amount=Money.input(amount)!!,category=category,kind=kind,wallet=wallet,note=note))},Modifier.fillMaxWidth(),enabled=Money.input(amount)!=null)
  Spacer(Modifier.height(24.dp))
 }}
}

@Composable fun EntryDetailsSheet(entry:Entry,evidence:ReceiptEvidence?,onDismiss:()->Unit,onEdit:()->Unit){
 GlassBottomSheet(onDismiss){Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(GlassTokens.margin),verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){
 Text("Chi tiết giao dịch",style=MaterialTheme.typography.titleLarge)
 AmountText(entry.amount,color=if(entry.kind==Kind.INCOME)incomeColor() else expenseColor())
 GroupedList {
  GroupedRow(entry.category,entry.wallet)
  GroupedRow("Nguồn",sourceLabel(entry.source))
  GroupedRow("Trạng thái",statusLabel(entry.status))
  GroupedRow("Thời gian",Instant.ofEpochMilli(entry.createdAt).atZone(VN_ZONE).toLocalDateTime().withNano(0).toString().replace('T',' '))
  if(entry.recipient.isNotBlank())GroupedRow("Người nhận / tài khoản",entry.recipient)
  if(entry.receivingBank.isNotBlank())GroupedRow("Tên từ QR / ngân hàng nhận",entry.receivingBank)
  if(entry.memo.isNotBlank())GroupedRow("Nội dung",entry.memo)
  if(entry.note.isNotBlank())GroupedRow("Ghi chú",entry.note)
  evidence?.let {if(it.recipientName.isNotBlank())GroupedRow("Tên trên biên lai",it.recipientName);if(it.transactionId.isNotBlank())GroupedRow("Mã giao dịch",it.transactionId)}
 }
 PillButton("Sửa giao dịch",onEdit,Modifier.fillMaxWidth())
 Spacer(Modifier.height(24.dp))
 }}
}
