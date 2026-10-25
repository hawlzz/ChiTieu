package vn.chitieu.qr
import android.graphics.Bitmap
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
object QrDecoder {
 fun bitmap(bitmap:Bitmap):String? {val pixels=IntArray(bitmap.width*bitmap.height);bitmap.getPixels(pixels,0,bitmap.width,0,0,bitmap.width,bitmap.height);return read(RGBLuminanceSource(bitmap.width,bitmap.height,pixels))}
 fun read(source:LuminanceSource):String? {
  var image=source
  repeat(4) {
   val decoded=runCatching {MultiFormatReader().apply {setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),DecodeHintType.TRY_HARDER to true,DecodeHintType.CHARACTER_SET to "UTF-8"))}.decodeWithState(BinaryBitmap(HybridBinarizer(image))).text}.getOrNull()
   if(decoded!=null)return decoded
   if(image.isRotateSupported)image=image.rotateCounterClockwise()
  }
  return null
 }
}
