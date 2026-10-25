package vn.chitieu.ui
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import vn.chitieu.data.*
import java.io.ByteArrayOutputStream
private fun limitedRead(input:java.io.InputStream):String {val out=ByteArrayOutputStream();val buffer=ByteArray(8192);var n:Int;while(input.read(buffer).also {n=it}!=-1){require(out.size()+n<=16*1024*1024){"File vượt 16 MB"};out.write(buffer,0,n)};return out.toString("UTF-8")}
@Composable fun BackupSettings(vm:LedgerViewModel) {
 val context=LocalContext.current;val scope=rememberCoroutineScope();var busy by remember {mutableStateOf(false)};var message by remember {mutableStateOf("")};var backup by remember {mutableStateOf<Backup?>(null)}
 fun launchWork(block:suspend ()->Unit) {busy=true;scope.launch {try {withContext(Dispatchers.IO){block()}}catch(_:Exception){message="Không thể đọc/ghi file. Kiểm tra định dạng, quyền và dung lượng."}finally {busy=false}}}
 val exportCsv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri->if(uri!=null)launchWork {val text=Csv.export(vm.repo.dao.all());context.contentResolver.openOutputStream(uri,"wt")?.bufferedWriter(Charsets.UTF_8)?.use {it.write(text)}?:error("Không mở file");message="Đã xuất CSV"}}
 val exportBackup=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->if(uri!=null)launchWork {val text=BackupIo.export(vm.repo);context.contentResolver.openOutputStream(uri,"wt")?.bufferedWriter(Charsets.UTF_8)?.use {it.write(text)}?:error("Không mở file");message="Đã sao lưu. File chứa dữ liệu tài chính dạng rõ; hãy cất riêng."}}
 val importCsv=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)launchWork {val text=context.contentResolver.openInputStream(uri)?.use(::limitedRead)?:error("Không mở file");val entries=Csv.parse(text);BackupIo.importCsv(vm.repo,entries);message="Đã nhập CSV; bỏ qua ID đã có"}}
 val restore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)launchWork {val text=context.contentResolver.openInputStream(uri)?.use(::limitedRead)?:error("Không mở file");backup=BackupIo.decode(text)}}
 Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
  Text("Xuất nhập & sao lưu",style=MaterialTheme.typography.titleLarge);Text("Dùng bộ chọn file hệ thống để lưu cục bộ. ChiTieu không tự tải lên cloud. CSV chỉ nhận định dạng ChiTieu v1; ô văn bản thêm dấu nháy bảo vệ công thức, được gỡ đúng khi nhập lại.")
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(enabled=!busy,onClick={exportCsv.launch("ChiTieu.csv")}){Text("Xuất CSV")};OutlinedButton(enabled=!busy,onClick={importCsv.launch(arrayOf("text/*","application/csv","application/octet-stream"))}){Text("Nhập CSV")}}
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(enabled=!busy,onClick={exportBackup.launch("ChiTieu-backup.json")}){Text("Sao lưu")};OutlinedButton(enabled=!busy,onClick={restore.launch(arrayOf("application/json","text/plain","application/octet-stream"))}){Text("Khôi phục")}}
  if(busy)LinearProgressIndicator();Text(message)
 }
 backup?.let {b->AlertDialog(onDismissRequest={backup=null},title={Text("Thay dữ liệu hiện tại?")},text={Text("Bản sao lưu có "+b.entries.size+" giao dịch. Khôi phục sẽ thay toàn bộ giao dịch, danh mục, ví, quy tắc và xoá log thô hiện tại. Hãy sao lưu trước khi tiếp tục. PIN và quyền hệ thống được giữ nguyên.")},confirmButton={TextButton(onClick={backup=null;launchWork {BackupIo.restore(vm.repo,b);message="Đã khôi phục"}}){Text("Thay dữ liệu & khôi phục")}},dismissButton={TextButton(onClick={backup=null}){Text("Huỷ")}})}
}
