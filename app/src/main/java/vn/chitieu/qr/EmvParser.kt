package vn.chitieu.qr
import java.math.BigDecimal
import vn.chitieu.domain.Money

@kotlinx.serialization.Serializable
 data class VietQr(val bin:String,val account:String,val recipientName:String,val amount:Long?,val memo:String,val bill:String,val city:String,val dynamic:Boolean,val service:String)
class QrException(message:String):IllegalArgumentException(message)
object Crc16 {
 fun compute(input:String):String {
  var crc=0xFFFF
  for(b in input.toByteArray(Charsets.UTF_8)) {crc=crc xor ((b.toInt() and 255) shl 8);repeat(8) {crc=if(crc and 0x8000!=0) (crc shl 1) xor 0x1021 else crc shl 1;crc=crc and 0xFFFF}}
  return crc.toString(16).uppercase().padStart(4,'0')
 }
}
object EmvParser {
 fun tlv(input:String):Map<String,String> {
  val result=linkedMapOf<String,String>();var p=0
  while(p<input.length) {
   if(p+4>input.length)throw QrException("QR không hợp lệ: TLV thiếu dữ liệu")
   val tag=input.substring(p,p+2);val length=input.substring(p+2,p+4)
   if(!tag.all(Char::isDigit)||!length.all(Char::isDigit))throw QrException("QR không hợp lệ: tag/độ dài")
   val n=length.toInt();p+=4
   if(n==0||p+n>input.length||result.containsKey(tag))throw QrException("QR không hợp lệ: độ dài hoặc tag trùng")
   result[tag]=input.substring(p,p+n);p+=n
  }
  return result
 }
 fun parse(raw:String):VietQr {
  if(raw.length !in 12..4096)throw QrException("QR không hợp lệ")
  if(!Regex("6304[0-9A-Fa-f]{4}$").containsMatchIn(raw))throw QrException("QR không hợp lệ: thiếu CRC")
  if(!Crc16.compute(raw.dropLast(4)).equals(raw.takeLast(4),true))throw QrException("QR không hợp lệ: CRC sai")
  val root=tlv(raw)
  if(root.keys.lastOrNull()!="63"||root["00"]!="01")throw QrException("QR không hợp lệ: phiên bản")
  val mode=root["01"];if(mode!=null&&mode!in listOf("11","12"))throw QrException("QR không hợp lệ: phương thức khởi tạo")
  val merchant=root["38"]?.let(::tlv)?:throw QrException("QR này không phải VietQR chuyển khoản")
  if(merchant["00"]!="A000000727")throw QrException("QR này không phải VietQR/NAPAS")
  val service=merchant["02"]?:throw QrException("QR thiếu mã dịch vụ")
  if(service !in listOf("QRIBFTTA","QRIBFTTC"))throw QrException("Dịch vụ QR chưa được hỗ trợ")
  if(root["53"]!="704"||root["58"]!="VN")throw QrException("Chỉ hỗ trợ VietQR bằng VND tại Việt Nam")
  val receiver=merchant["01"]?.let(::tlv)?:throw QrException("QR thiếu người nhận")
  val bin=receiver["00"]?:throw QrException("QR thiếu BIN")
  val account=receiver["01"]?:throw QrException("QR thiếu số tài khoản/thẻ")
  if(!Regex("[0-9]{6}").matches(bin)||!Regex("[A-Za-z0-9]{1,34}").matches(account))throw QrException("QR có ngân hàng/tài khoản không hợp lệ")
  val amount=root["54"]?.let { value->
   if(!Regex("[0-9]+(?:\\.[0-9]{1,2})?").matches(value))throw QrException("Số tiền QR không hợp lệ")
   val number=try {BigDecimal(value).longValueExact()}catch(_:ArithmeticException){throw QrException("VND phải là số nguyên")}
   try {Money.validate(number)}catch(_:IllegalArgumentException){throw QrException("Số tiền QR ngoài giới hạn")}
  }
  val extra=root["62"]?.let(::tlv).orEmpty()
  return VietQr(bin,account,root["59"].orEmpty(),amount,extra["08"].orEmpty(),extra["01"].orEmpty(),root["60"].orEmpty(),mode=="12",service)
 }
}

