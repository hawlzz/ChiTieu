package vn.chitieu.bank
import android.content.*
import android.net.Uri
import vn.chitieu.data.Entry
import vn.chitieu.qr.Bank
object BankLauncher {
 fun open(context:Context,payer:Bank,receiver:Bank?,entry:Entry,service:String):String {
  val pkg=payer.packageName?:return "Ứng dụng ngân hàng chưa có package được xác minh"
  if(service=="QRIBFTTA"&&payer.appId!=null&&receiver!=null) {
   val url=runCatching {DeepLink.build(payer.appId,entry.recipient,receiver.code,entry.amount,entry.memo,entry.receivingBank)}.getOrNull()
   if(url!=null) {try {val intent=Intent(Intent.ACTION_VIEW,Uri.parse(url)).setPackage(pkg);if(intent.resolveActivity(context.packageManager)!=null){context.startActivity(intent);return "Đã mở app. Hãy kiểm tra kỹ người nhận, số tiền và nội dung trong app ngân hàng."}}catch(_:ActivityNotFoundException){}catch(_:SecurityException){}}
  }
  // TODO: verify — no bank-specific scheme is enabled without official documentation and device validation.
  return try {
   (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Tài khoản người nhận",entry.recipient))
   val intent=context.packageManager.getLaunchIntentForPackage(pkg)?:return "Không thể mở app ngân hàng. Giao dịch vẫn chờ xác nhận."
   context.startActivity(intent)
   "Đã sao chép "+(if(service=="QRIBFTTC")"số thẻ" else "số tài khoản")+". Hãy dán vào ô người nhận; nhập tay số tiền và nội dung hiển thị ở giao dịch chờ."
  }catch(_:Exception){"Hệ thống chặn mở app. Bạn có thể chuyển thủ công rồi xác nhận giao dịch chờ."}
 }
}
