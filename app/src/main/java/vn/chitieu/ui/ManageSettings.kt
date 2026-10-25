package vn.chitieu.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vn.chitieu.data.*
@Composable fun ManageSettings(vm:LedgerViewModel) {
 val categories by vm.categories.collectAsStateWithLifecycle();val rules by vm.rules.collectAsStateWithLifecycle()
 var name by remember {mutableStateOf("")};var icon by remember {mutableStateOf("●")};var color by remember {mutableStateOf("Xanh lá")};var wallet by remember {mutableStateOf("")};var keyword by remember {mutableStateOf("")};var category by remember {mutableStateOf("Khác")}
 val colors=mapOf("Xanh lá" to 0xFF267D66L,"Xanh dương" to 0xFF3973B9L,"Cam" to 0xFFD77B35L,"Tím" to 0xFF8555A4L,"Đỏ" to 0xFFC25454L)
 Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
  Text("Danh mục & ví",style=MaterialTheme.typography.titleLarge)
  Choice("Sửa danh mục có sẵn",categories.map {it.name},name.ifBlank {"Chọn hoặc nhập mới"}){value->val c=categories.first {it.name==value};name=c.name;icon=c.icon;color=colors.entries.firstOrNull {it.value==c.color}?.key?:"Xanh lá"}
  OutlinedTextField(name,{name=it.take(100)},label={Text("Tên danh mục")});Choice("Biểu tượng",listOf("●","🍜","🚗","🏠","🛍","♥","📚","💰"),icon){icon=it};Choice("Màu",colors.keys.toList(),color){color=it}
  TextButton(enabled=name.isNotBlank(),onClick={val value=Category(name.trim(),icon,colors.getValue(color));vm.work {vm.repo.dao.saveCategory(value)};name=""}){Text("Lưu danh mục")}
  OutlinedTextField(wallet,{wallet=it.take(100)},label={Text("Ví mới / tên ngân hàng")});TextButton(enabled=wallet.isNotBlank(),onClick={val value=Wallet(wallet.trim());vm.work {vm.repo.dao.saveWallet(value)};wallet=""}){Text("Thêm ví")}
  Text("Quy tắc gợi ý danh mục",style=MaterialTheme.typography.titleMedium);Text("Khớp từ khoá trong nội dung; nếu không khớp, gợi ý theo lịch sử người nhận QR.")
  OutlinedTextField(keyword,{keyword=it.take(200)},label={Text("Từ khoá")});Choice("Danh mục gợi ý",categories.map {it.name},category){category=it};TextButton(enabled=keyword.isNotBlank(),onClick={val key=keyword.trim();val cat=category;vm.work {vm.repo.dao.saveRule(CategoryRule(key,cat))};keyword=""}){Text("Lưu quy tắc")}
  rules.forEach {rule->Row {Text(rule.keyword+" → "+rule.category,androidx.compose.ui.Modifier.weight(1f));TextButton(onClick={vm.work {vm.repo.dao.deleteRule(rule.keyword)}}){Text("Xoá")}}}
 }
}

