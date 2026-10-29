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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import vn.chitieu.ui.theme.*
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
 val haptic=LocalHapticFeedback.current
 val scope=rememberCoroutineScope()
 val reduced=LocalReduceMotion.current
 val zoom by animateFloatAsState(if(consumed&&!reduced)1.05f else 1f,label="scanZoom")
 val handle:(String)->Unit={raw->if(!consumed) {runCatching {EmvParser.parse(raw)}.onSuccess {consumed=true;haptic.performHapticFeedback(HapticFeedbackType.LongPress);scope.launch {if(!reduced)delay(160);onResult(it)}}.onFailure {error=it.message?:"QR không hợp lệ"}}}
 val currentHandle by rememberUpdatedState(handle)
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {uri->if(uri!=null)scope.launch {val result=withContext(Dispatchers.IO) {runCatching {val options=BitmapFactory.Options().apply {inJustDecodeBounds=true};context.contentResolver.openInputStream(uri)?.use {BitmapFactory.decodeStream(it,null,options)};if(options.outWidth<=0||options.outHeight<=0)throw IllegalArgumentException();var sample=1;while(options.outWidth/sample>2048||options.outHeight/sample>2048)sample*=2;val bitmap=context.contentResolver.openInputStream(uri)?.use {BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize=sample})}?:throw IllegalArgumentException();try {QrDecoder.bitmap(bitmap)}finally {bitmap.recycle()}}.getOrNull()};if(result==null)error="Ảnh không có QR đọc được hoặc định dạng không hỗ trợ" else handle(result)}}
 Box(Modifier.fillMaxSize().background(Color.Black)) {
  if(allowed)AndroidView(factory={PreviewView(it).also {v->preview=v}},modifier=Modifier.fillMaxSize().graphicsLayer {scaleX=zoom;scaleY=zoom})
  Canvas(Modifier.fillMaxSize()){val w=size.width*.7f;val x=(size.width-w)/2;val y=(size.height-w)/2;val line=w*.16f;val stroke=4.dp.toPx();listOf(Offset(x,y) to Offset(1f,1f),Offset(x+w,y) to Offset(-1f,1f),Offset(x,y+w) to Offset(1f,-1f),Offset(x+w,y+w) to Offset(-1f,-1f)).forEach {(p,d)->drawLine(Color.White,p,p+Offset(line*d.x,0f),stroke,androidx.compose.ui.graphics.StrokeCap.Round);drawLine(Color.White,p,p+Offset(0f,line*d.y),stroke,androidx.compose.ui.graphics.StrokeCap.Round)}}
  Column(Modifier.fillMaxSize().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Rounded.Close,"Quay lại",tint=Color.White)};Text("Quét VietQR",color=Color.White,style=MaterialTheme.typography.titleLarge)}
   Spacer(Modifier.weight(1f))
   if(!allowed){Text("Cho phép camera để quét, hoặc chọn ảnh QR.",color=Color.White);PillButton("Cho phép camera",{permission.launch(Manifest.permission.CAMERA)})}
   if(error.isNotBlank())Text(error,color=Color(0xFFFFB4AB))
   Text("Đặt mã QR trong khung",color=Color.White,style=MaterialTheme.typography.bodyMedium)
   Row(Modifier.padding(top=20.dp,bottom=GlassTokens.bottom),horizontalArrangement=Arrangement.spacedBy(24.dp)){
    FilledTonalIconButton(onClick={picker.launch("image/*")},modifier=Modifier.size(56.dp),shape=CircleShape){Icon(Icons.Rounded.Image,"Chọn ảnh QR")}
    FilledTonalIconButton(onClick={torch=!torch},enabled=camera?.cameraInfo?.hasFlashUnit()==true,modifier=Modifier.size(56.dp),shape=CircleShape){Icon(if(torch)Icons.Rounded.FlashOff else Icons.Rounded.FlashOn,if(torch)"Tắt đèn" else "Bật đèn")}
   }
  }
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

// TODO: verify camera/haptics and launcher rendering on Xiaomi 15 HyperOS 2 without GMS.
