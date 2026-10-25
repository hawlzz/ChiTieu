package vn.chitieu.notification
import vn.chitieu.domain.*
import vn.chitieu.data.Entry
import java.security.MessageDigest
import java.util.Locale

data class ParsedMessage(val amount:Long,val kind:Kind,val memo:String,val time:Long,val balance:Long?=null)
interface BankMessageParser {fun parse(body:String,time:Long):ParsedMessage?}
open class UnverifiedParser(val bank:String):BankMessageParser {
 // TODO: verify — no real notification/SMS samples supplied. Do not interpret financial messages until bank rules have verified fixtures.
 override fun parse(body:String,time:Long):ParsedMessage?=null
}
object SyntheticParser:BankMessageParser {
 // Explicitly synthetic grammar; used by unit tests / manual diagnostics only, never registered in the production listener.
 override fun parse(body:String,time:Long):ParsedMessage? {
  val match=Regex("^\\[SIMULATED] (IN|OUT) ([0-9][0-9.,]*)(?: VND|đ)?(?: BAL=([0-9][0-9.,]*))? MEMO=(.{1,200})$").matchEntire(body)?:return null
  fun amount(s:String):Long? {if(!Regex("(?:[0-9]+|[0-9]{1,3}(?:,[0-9]{3})+|[0-9]{1,3}(?:\\.[0-9]{3})+)").matches(s))return null;return s.filter(Char::isDigit).toLongOrNull()?.takeIf {it in 1..Money.MAX}}
  val value=amount(match.groupValues[2])?:return null
  val balance=match.groupValues[3].takeIf {it.isNotEmpty()}?.let {amount(it)?:return null}
  return ParsedMessage(value,if(match.groupValues[1]=="IN")Kind.INCOME else Kind.EXPENSE,match.groupValues[4],time,balance)
 }
}
object Reconciliation {
 fun fingerprint(bank:String,message:ParsedMessage):String {
  val text=bank+"|"+message.kind+"|"+message.amount+"|"+message.memo.trim().lowercase(Locale.ROOT).replace(Regex("\\s+")," ")+"|"+(message.time/60_000L)
  return MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it)}
 }
 fun qrMatch(entries:List<Entry>,pkg:String,message:ParsedMessage):Entry? {
  if(message.kind!=Kind.EXPENSE)return null
  val candidates=entries.filter {it.source==Source.QR&&it.kind==Kind.EXPENSE&&it.status in listOf(Status.PENDING,Status.CONFIRMED)&&it.payerPackage==pkg&&it.amount==message.amount&&message.time>=it.createdAt&&message.time-it.createdAt<=600_000L}
  return candidates.singleOrNull()
 }
}
