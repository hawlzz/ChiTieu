package vn.chitieu.bank
import java.net.URLEncoder
import vn.chitieu.domain.Money
object DeepLink {
 fun build(app:String,account:String,bankCode:String,amount:Long,memo:String,name:String):String {
  Money.validate(amount);require(Regex("[a-z0-9]+").matches(app));require(Regex("[A-Za-z0-9]{1,34}").matches(account));require(Regex("[A-Za-z0-9]+").matches(bankCode))
  val values=linkedMapOf("app" to app,"ba" to (account+"@"+bankCode.lowercase()),"am" to amount.toString(),"tn" to memo,"bn" to name)
  return "https://dl.vietqr.io/pay?"+values.entries.joinToString("&"){it.key+"="+URLEncoder.encode(it.value,"UTF-8").replace("+","%20")}
 }
 // TODO: verify — provider currently documents a required `url` parameter; only enable autofill after testing each app. No callback is treated as payment proof.
}
