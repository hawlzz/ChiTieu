package vn.chitieu.debug
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import vn.chitieu.data.*
import vn.chitieu.domain.*
import vn.chitieu.receipt.*
import java.io.File
@Serializable data class EvaluationItem(val file:String,val draft:ReceiptDraft?=null,val elapsedMs:Long=0,val error:String?=null)
@Serializable data class EvaluationResult(val engine:String="mlkit-bundled-16.0.1",val items:List<EvaluationItem>)
@AndroidEntryPoint class HarnessActivity:FragmentActivity() {
 @Inject lateinit var repo:Repository
 @Inject lateinit var preferences:Preferences
 override fun onCreate(savedInstanceState:Bundle?) {super.onCreate(savedInstanceState);setContent {Text("ChiTieu · Kiểm thử cục bộ\nChỉ dùng dữ liệu thử nghiệm.")};execute(intent)}
 override fun onNewIntent(intent:Intent){super.onNewIntent(intent);setIntent(intent);execute(intent)}
 private fun execute(request:Intent){val action=request.getStringExtra("action").orEmpty();val runId=request.getStringExtra("run")?:"latest";require(Regex("[A-Za-z0-9_-]{1,80}").matches(runId))
  lifecycleScope.launch {try {when(action) {
   "enable-shell"->{TestNotifications.enabled=true;result(runId,"{\"enabled\":true}")}
   "clear"->{repo.dao.all().filter {it.id.startsWith("test-")||it.payerPackage=="com.android.shell"}.forEach {repo.dao.delete(it.id)};result(runId,"{\"clearedSynthetic\":true}")}
   "seed"->{repo.seed();val now=System.currentTimeMillis();repeat(200){i->repo.save(Entry(id="test-seed-"+i,amount=(i+1)*1000L,category=Defaults.categories[i%Defaults.categories.size],createdAt=now-i*60000L,note="[SIMULATED] TEST",kind=if(i%5==0)Kind.INCOME else Kind.EXPENSE))};result(runId,"{\"seeded\":200}")}
   "state"->{val entries=repo.dao.all().filter {it.id.startsWith("test-")||it.payerPackage=="com.android.shell"};result(runId,Json.encodeToString(entries))}
   "theme"->{preferences.text("themeMode",request.getStringExtra("value")?:"system");result(runId,"{\"themeChanged\":true}")}
   "effects"->{preferences.flag("reduceEffects",request.getBooleanExtra("reduced",false));result(runId,"{\"effectsChanged\":true}")}
   "evaluate"->{val dir=File(filesDir,"receipt-eval");val items=withContext(Dispatchers.IO){dir.listFiles().orEmpty().filter {it.name.matches(Regex("receipt_[0-9]{3}\\.(jpg|png|jpeg)"))}.sortedBy {it.name}.map {file->try {val scan=ReceiptImages(this@HarnessActivity).scan(Uri.fromFile(file));EvaluationItem(file.name,scan.draft,scan.elapsedMs)}catch(e:Exception){EvaluationItem(file.name,error=e.javaClass.simpleName)}}};result(runId,Json.encodeToString(EvaluationResult(items=items)))}
   "load"->{val file=File(request.getStringExtra("path")?:error("Missing path")).canonicalFile;val allowed=File(filesDir,"receipt-eval").canonicalFile;require(file.path.startsWith(allowed.path+File.separator)&&file.isFile);val scan=ReceiptImages(this@HarnessActivity).scan(Uri.fromFile(file));result(runId,Json.encodeToString(EvaluationResult(items=listOf(EvaluationItem(file.name,scan.draft,scan.elapsedMs)))))}
   "screen"->{val screen=request.getStringExtra("screen")?:"ledger";require(screen in setOf("ledger","transactions","scan","settings","advanced","receipt"));startActivity(Intent(this@HarnessActivity,vn.chitieu.MainActivity::class.java).putExtra("test_screen",screen).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));result(runId,"{\"screenOpened\":true}")}
   else->result(runId,"{\"error\":\"Unknown test action\"}")
  }}catch(e:Exception){result(runId,"{\"error\":\""+e.javaClass.simpleName+"\"}")}}
 }
 private suspend fun result(run:String,text:String)=withContext(Dispatchers.IO) {val dir=File(filesDir,"test-results").apply {mkdirs()};val temp=File(dir,run+".tmp");temp.writeText(text);check(temp.renameTo(File(dir,run+".json")))}
}

