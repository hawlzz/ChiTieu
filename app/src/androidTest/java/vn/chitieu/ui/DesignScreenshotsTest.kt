package vn.chitieu.ui

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith
import vn.chitieu.MainActivity
import vn.chitieu.data.Entry
import vn.chitieu.domain.*
import vn.chitieu.qr.VietQr
import vn.chitieu.receipt.*
import vn.chitieu.ui.theme.*

/** All fixtures below are synthetic. No bank app is opened and no fixture is saved. */
@RunWith(AndroidJUnit4::class)
class DesignScreenshotsTest {
 @get:Rule val compose=createAndroidComposeRule<MainActivity>()
 @Test fun syntheticScreenGallery(){
  val instrumentation=InstrumentationRegistry.getInstrumentation()
  lateinit var vm:LedgerViewModel
  compose.activityRule.scenario.onActivity {a->a.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);vm=ViewModelProvider(a)[LedgerViewModel::class.java]}
  val qr=VietQr("970407","0000000000","NGUOI NHAN THU NGHIEM",125000L,"[SIMULATED] Minh hoa","","",false,"QRIBFTTA")
  val receiptFile=java.io.File(instrumentation.targetContext.cacheDir,"synthetic-receipt.png")
  val sample=android.graphics.Bitmap.createBitmap(800,500,android.graphics.Bitmap.Config.ARGB_8888)
  android.graphics.Canvas(sample).apply {drawColor(android.graphics.Color.WHITE);val paint=android.graphics.Paint().apply {color=android.graphics.Color.BLACK;textSize=40f};drawText("[SIMULATED] RECEIPT",40f,80f,paint);drawText("125.000 VND",40f,180f,paint);drawText("DEMO-ONLY-001",40f,260f,paint)}
  receiptFile.outputStream().use {sample.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};sample.recycle()
  val draft=ReceiptDraft(amount=ReceiptField(125000L,.95f),status=ReceiptField(ReceiptStatus.SUCCESS,1f),recipient=ReceiptField("NGUOI NHAN THU NGHIEM",.6f),account=ReceiptField("0000000000",.95f),recipientBank=ReceiptField("Ngan hang thu nghiem",.9f),memo=ReceiptField("[SIMULATED] Minh hoa",.7f),time=ReceiptField(System.currentTimeMillis(),.9f),transactionId=ReceiptField("DEMO-ONLY-001",.9f),sourceBank="Vi thu nghiem")
  val dir=java.io.File(instrumentation.targetContext.filesDir,"design-gallery").apply {mkdirs()}
  for(mode in listOf("light","dark"))for(screen in listOf("quick-add","confirm","receipt","pending","detail")){
   compose.activityRule.scenario.onActivity {activity->
    if(screen=="receipt")vm.receiptState.value=ReceiptUiState(scan=ReceiptScan(draft,"synthetic-only",android.net.Uri.fromFile(receiptFile).toString(),0))
    activity.setContent {ChiTieuTheme(mode){GlassScene(true){Box(Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp)){
     when(screen){
      "detail"->EntryDetailsSheet(Entry(amount=125000,recipient="0000000000",memo="[SIMULATED] Minh hoa",source=Source.RECEIPT),vn.chitieu.data.ReceiptEvidence("demo","synthetic","DEMO-ONLY-001","NGUOI NHAN THU NGHIEM",System.currentTimeMillis()),{},{})
      "quick-add"->QuickAdd(vm,{},{})
      "confirm"->ConfirmQrScreen(qr,vm,vm.registry,{},{})
      "receipt"->ReceiptScreen(vm,{},{})
      "pending"->PendingTransferSheet(Entry(amount=125000,recipient="NGUOI NHAN THU NGHIEM"),{},{},{},{})
     }
    }}}}
   }
   compose.waitForIdle();Thread.sleep(800)
   val bitmap=instrumentation.uiAutomation.takeScreenshot();java.io.File(dir,"$screen-$mode.png").outputStream().use {bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
  }
  compose.activityRule.scenario.onActivity {vm.receiptState.value=ReceiptUiState()}
 }
}
