package vn.chitieu.receipt
import android.content.Context
import android.graphics.*
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
interface OcrEngine {suspend fun recognize(bitmap:Bitmap):String}
class BundledMlKitEngine:OcrEngine {
 override suspend fun recognize(bitmap:Bitmap):String=suspendCancellableCoroutine {continuation->
  val client=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
  client.process(InputImage.fromBitmap(bitmap,0)).addOnSuccessListener {text->if(continuation.isActive)continuation.resume(text.text)}.addOnFailureListener {e->if(continuation.isActive)continuation.resumeWithException(e)}.addOnCompleteListener {client.close()}
 }
}
data class ReceiptScan(val draft:ReceiptDraft,val digest:String,val uri:String,val elapsedMs:Long)
class ReceiptImages(private val context:Context,private val engine:OcrEngine=BundledMlKitEngine()) {
 suspend fun scan(uri:Uri):ReceiptScan=withContext(Dispatchers.IO) {
  require(uri.scheme=="content"||uri.scheme=="file")
  val bytes=read(uri);val digest=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
  val start=android.os.SystemClock.elapsedRealtime()
  val bitmap=ImageDecoder.decodeBitmap(ImageDecoder.createSource(ByteBuffer.wrap(bytes))) {decoder,info,_->require(info.size.width.toLong()*info.size.height<=60_000_000L);decoder.allocator=ImageDecoder.ALLOCATOR_SOFTWARE;val scale=minOf(1.0,2560.0/maxOf(info.size.width,info.size.height));decoder.setTargetSize((info.size.width*scale).toInt().coerceAtLeast(1),(info.size.height*scale).toInt().coerceAtLeast(1))}
  try {val text=withContext(NonCancellable){engine.recognize(bitmap)};ReceiptScan(ReceiptParsers.parse(text),digest,uri.toString(),android.os.SystemClock.elapsedRealtime()-start)}finally {bitmap.recycle()}
 }
 fun read(uri:Uri):ByteArray {val out=java.io.ByteArrayOutputStream();context.contentResolver.openInputStream(uri)?.use {stream->val buffer=ByteArray(8192);while(true){val n=stream.read(buffer);if(n<0)break;require(out.size()+n<=20*1024*1024){"Ảnh vượt 20 MB"};out.write(buffer,0,n)}}?:error("Không mở được ảnh");return out.toByteArray()}
}
