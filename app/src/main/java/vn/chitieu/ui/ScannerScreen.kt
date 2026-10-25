package vn.chitieu.ui
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.PlanarYUVLuminanceSource
import kotlinx.coroutines.*
import vn.chitieu.qr.*
import java.util.concurrent.Executors

@Composable fun ScannerScreen(onResult:(VietQr)->Unit,onBack:()->Unit) {
 val context=LocalContext.current;val lifecycle=LocalLifecycleOwner.current
 var allowed by remember {mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){allowed=it}
 var error by remember {mutableStateOf("")};var torch by remember {mutableStateOf(false)};var camera by remember {mutableStateOf<Camera?>(null)}
 var preview by remember {mutableStateOf<PreviewView?>(null)};var consumed by remember {mutableStateOf(false)}
 val scope=rememberCoroutineScope()
 val handle:(String)->Unit={raw->if(!consumed) {runCatching {EmvParser.parse(raw)}.onSuccess {consumed=true;onResult(it)}.onFailure {error=it.message?:"QR không hợp lệ"}}}
 val currentHandle by rememberUpdatedState(handle)
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {uri->if(uri!=null)scope.launch {val result=withContext(Dispatchers.IO) {runCatching {val options=BitmapFactory.Options().apply {inJustDecodeBounds=true};context.contentResolver.openInputStream(uri)?.use {BitmapFactory.decodeStream(it,null,options)};if(options.outWidth<=0||options.outHeight<=0)throw IllegalArgumentException();var sample=1;while(options.outWidth/sample>2048||options.outHeight/sample>2048)sample*=2;val bitmap=context.contentResolver.openInputStream(uri)?.use {BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize=sample})}?:throw IllegalArgumentException();try {QrDecoder.bitmap(bitmap)}finally {bitmap.recycle()}}.getOrNull()};if(result==null)error="Ảnh không có QR đọc được hoặc định dạng không hỗ trợ" else handle(result)}}
 Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
  Text("Quét VietQR",style=MaterialTheme.typography.headlineMedium)
  Text("Kiểm tra người nhận trong app ngân hàng trước khi chuyển.")
  if(allowed)AndroidView(factory={PreviewView(it).also {v->preview=v}},modifier=Modifier.weight(1f).fillMaxWidth()) else {Text("Cần quyền camera để quét. Bạn vẫn có thể chọn ảnh QR.");Button(onClick={permission.launch(Manifest.permission.CAMERA)}){Text("Cho phép camera")};Spacer(Modifier.weight(1f))}
  if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {OutlinedButton(onClick={picker.launch("image/*")}){Text("Chọn ảnh QR")};OutlinedButton(onClick={torch=!torch},enabled=camera?.cameraInfo?.hasFlashUnit()==true){Text(if(torch)"Tắt đèn" else "Bật đèn")}}
  TextButton(onClick=onBack){Text("Quay lại")}
 }
 DisposableEffect(allowed,preview,lifecycle) {
  val view=preview
  val executor=Executors.newSingleThreadExecutor();var disposed=false;var provider:ProcessCameraProvider?=null;var analysis:ImageAnalysis?=null;var cameraPreview:Preview?=null
  if(allowed&&view!=null) {
   val future=ProcessCameraProvider.getInstance(context)
   future.addListener({if(!disposed)try {
    provider=future.get();val p=Preview.Builder().build().also {it.setSurfaceProvider(view.surfaceProvider)};cameraPreview=p
    val a=ImageAnalysis.Builder().setTargetResolution(Size(1280,720)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();analysis=a
    var last=0L
    a.setAnalyzer(executor) {image->try {if(System.currentTimeMillis()-last>800) {last=System.currentTimeMillis();val plane=image.planes[0];val w=image.width;val h=image.height;val data=ByteArray(w*h);val buffer=plane.buffer;for(y in 0 until h)for(x in 0 until w)data[y*w+x]=buffer.get(y*plane.rowStride+x*plane.pixelStride);val result=QrDecoder.read(PlanarYUVLuminanceSource(data,w,h,0,0,w,h,false));if(result!=null)ContextCompat.getMainExecutor(context).execute {currentHandle(result)}}}catch(_:Exception){}finally {image.close()}}
    camera=provider?.bindToLifecycle(lifecycle,CameraSelector.DEFAULT_BACK_CAMERA,p,a)
   }catch(_:Exception){error="Không thể mở camera. Dùng Chọn ảnh QR hoặc kiểm tra quyền."}},ContextCompat.getMainExecutor(context))
  }
  onDispose {disposed=true;analysis?.clearAnalyzer();val cases=listOfNotNull(cameraPreview,analysis).toTypedArray();if(cases.isNotEmpty())provider?.unbind(*cases);executor.shutdown()}
 }
 LaunchedEffect(torch,camera){camera?.cameraControl?.enableTorch(torch)}
}
