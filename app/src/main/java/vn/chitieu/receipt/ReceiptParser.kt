package vn.chitieu.receipt
import kotlinx.serialization.Serializable
import java.text.Normalizer
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import vn.chitieu.domain.*
@Serializable enum class ReceiptStatus { SUCCESS, FAILED, UNKNOWN, UNSUPPORTED }
@Serializable data class ReceiptField<T>(val value:T?=null,val confidence:Float=0f)
@Serializable data class ReceiptDraft(val amount:ReceiptField<Long> = ReceiptField(),val status:ReceiptField<ReceiptStatus> = ReceiptField(),val recipient:ReceiptField<String> = ReceiptField(),val account:ReceiptField<String> = ReceiptField(),val recipientBank:ReceiptField<String> = ReceiptField(),val memo:ReceiptField<String> = ReceiptField(),val time:ReceiptField<Long> = ReceiptField(),val transactionId:ReceiptField<String> = ReceiptField(),val sourceBank:String="Không rõ",val profile:String="generic-v1") {
 val canReview:Boolean get()=status.value==ReceiptStatus.SUCCESS&&amount.value!=null
}
interface ReceiptParser { fun parse(text:String):ReceiptDraft }
internal fun fold(text:String):String=Normalizer.normalize(text,Normalizer.Form.NFD).replace(Regex("\\p{M}+"),"").replace('đ','d').replace('Đ','D').lowercase(Locale.ROOT).trim().replace(Regex("\\s+")," ")
object ReceiptMoney {
 fun parse(text:String):Long? {val value=text.trim().replace('O','0').replace('o','0').replace('I','1').replace('l','1').replace(" ","");if(!Regex("(?:[0-9]+|[0-9]{1,3}(?:[.,][0-9]{3})+)(?:[.,]00)?").matches(value))return null
  val trimmed=if(Regex(".*[.,]00$").matches(value))value.dropLast(3) else value
  return trimmed.filter(Char::isDigit).toLongOrNull()?.takeIf {it in 1..Money.MAX}
 }
}
object ReceiptDates {
 fun parse(text:String):Pair<Long,Float>? {
  val clean=text.trim().replace(Regex("\\s+")," ")
  runCatching {LocalDateTime.parse(clean).atZone(VN_ZONE).toInstant().toEpochMilli()}.getOrNull()?.let {return it to .95f}
  val vi=Regex("(\\d{1,2})\\s*thg\\s*(\\d{1,2})[,]?\\s*(\\d{4})(?:\\s*(?:luc|lúc|at)\\s*(\\d{1,2}):(\\d{2}))?",RegexOption.IGNORE_CASE).find(clean)
  if(vi!=null)return runCatching {val v=vi.groupValues;LocalDateTime.of(v[3].toInt(),v[2].toInt(),v[1].toInt(),v[4].ifBlank {"0"}.toInt(),v[5].ifBlank {"0"}.toInt()).atZone(VN_ZONE).toInstant().toEpochMilli() to if(v[4].isBlank()).5f else .9f}.getOrNull()
  val formats=listOf("d MMM uuuu 'at' h:mm a","d MMM uuuu 'at' H:mm","MMM d, uuuu 'at' h:mm a","d/M/uuuu H:mm","d-M-uuuu H:mm","d/M/uuuu H:mm:ss")
  for(format in formats)runCatching {LocalDateTime.parse(clean,DateTimeFormatter.ofPattern(format,Locale.ENGLISH)).atZone(VN_ZONE).toInstant().toEpochMilli()}.getOrNull()?.let {return it to .9f}
  for(format in listOf("MMM d, uuuu","d MMM uuuu","d/M/uuuu"))runCatching {LocalDate.parse(clean,DateTimeFormatter.ofPattern(format,Locale.ENGLISH)).atStartOfDay(VN_ZONE).toInstant().toEpochMilli()}.getOrNull()?.let {return it to .5f}
  return null
 }
}
open class GenericReceiptParser:ReceiptParser {
 protected open val profile="generic-v1"
 protected open val sourceBank="Không rõ"
 protected val labels=mapOf("recipient" to setOf("nguoi nhan","recipient details","recipient","ten nguoi nhan"),"bank" to setOf("ngan hang nhan","recipient bank","ngan hang"),"beneficiary" to setOf("tai khoan nhan","beneficiary account"),"memo" to setOf("loi nhan","noi dung","noi dung chuyen khoan","message","transfer message"),"time" to setOf("ngay chuyen","ngay thuc hien","ngay thanh toan","transfer date","transaction time","date and time"),"id" to setOf("ma giao dich","transaction id","transaction reference","reference number"))
 override fun parse(text:String):ReceiptDraft {
  val lines=text.lineSequence().map(String::trim).filter(String::isNotBlank).take(400).toList();val normalized=lines.map(::fold)
  val negative=normalized.any {Regex("khong thanh cong|that bai|unsuccessful|failed|dang xu ly|processing|pending|scheduled").containsMatchIn(it)}
  val request=normalized.any {it.contains("withdrawal request")||it.contains("yeu cau rut tien")||it.contains("transaction code")&&normalized.any {n->n.contains("expiry date")}}
  val successIndex=normalized.indexOfFirst {Regex("chuyen thanh cong|thanh toan thanh cong|giao dich thanh cong|chuyen khoan thanh cong|da chuyen|hoan tat|successfully transferred|transfer successful|payment successful|successfully paid").containsMatchIn(it)}
  val status=when {negative->ReceiptStatus.FAILED;request->ReceiptStatus.UNSUPPORTED;successIndex>=0->ReceiptStatus.SUCCESS;else->ReceiptStatus.UNKNOWN}
  fun section(key:String):List<String> {val index=normalized.indexOfFirst {it.removeSuffix(":") in labels.getValue(key)};if(index<0)return emptyList();val stop=(index+1 until lines.size).firstOrNull {i->normalized[i].removeSuffix(":") in labels.values.flatten()||Regex("^(ma tham chieu|reference|chia se|share receipt|save as template|new transfer|done|hoan thanh|back to dashboard|quay ve|day la khoan|what is this)").containsMatchIn(normalized[i])}?:lines.size;return lines.subList(index+1,stop).take(6)}
  val main=if(successIndex>=0)lines.drop(successIndex).take(8) else emptyList()
  val amounts=main.mapNotNull {line->val match=Regex("(?:VND|VNĐ|₫|đ)\\s*[:]?\\s*([0-9OolI][0-9OolI., ]*)",RegexOption.IGNORE_CASE).find(line)?:Regex("([0-9OolI][0-9OolI., ]*)\\s*(?:VND|VNĐ|₫|đ)\\b",RegexOption.IGNORE_CASE).find(line);match?.groupValues?.get(1)?.let(ReceiptMoney::parse)}.distinct()
  fun accountLine(s:String)=s.any(Char::isDigit)&&Regex("[A-Za-z0-9 ]{5,40}").matches(s)
  val recipientLines=section("recipient");val beneficiary=section("beneficiary");val account=(recipientLines+beneficiary).lastOrNull(::accountLine)?.replace(" ","")
  var name=recipientLines.takeWhile {!accountLine(it)}.joinToString(" ").takeIf(String::isNotBlank)
  if(name==null&&successIndex>=0){val header=lines.drop(successIndex+1).takeWhile {!Regex("VND|VNĐ|₫",RegexOption.IGNORE_CASE).containsMatchIn(it)};val start=header.indexOfFirst {Regex("^(To|Tới)\\s+",RegexOption.IGNORE_CASE).containsMatchIn(it)};if(start>=0)name=header.drop(start).joinToString(" ").replaceFirst(Regex("^(To|Tới)\\s+",RegexOption.IGNORE_CASE),"").trim()}
  val bank=section("bank").joinToString(" ").ifBlank {beneficiary.filterNot(::accountLine).joinToString(" ")}.takeIf(String::isNotBlank)
  val memo=section("memo").joinToString(" ").takeIf(String::isNotBlank)
  val timeText=section("time").joinToString(" ");val date=ReceiptDates.parse(timeText)
  val id=section("id").firstOrNull()?.takeIf {Regex("[A-Za-z0-9/-]{5,80}").matches(it)}
  fun field(s:String?,confidence:Float)=ReceiptField(s,if(s==null)0f else confidence)
  return ReceiptDraft(amount=ReceiptField(amounts.singleOrNull(),if(amounts.size==1).92f else 0f),status=ReceiptField(status,if(status==ReceiptStatus.UNKNOWN)0f else .95f),recipient=field(name,.78f),account=field(account,.8f),recipientBank=field(bank,.75f),memo=field(memo,.72f),time=ReceiptField(date?.first,date?.second?:0f),transactionId=field(id,.85f),sourceBank=sourceBank,profile=profile)
 }
 // TODO: verify — generic rules for banks absent from the development dataset. Confidence values are heuristics, not measured probabilities.
}
class TechcombankReceiptParser:GenericReceiptParser() {
 override val profile="techcombank-train-v1"
 override val sourceBank="Techcombank"
 // Layout labels/header rules were checked only against the frozen development split. Vietnamese and English transfer/payment screens are supported; withdrawal requests are rejected.
}
object ReceiptParsers:ReceiptParser {
 override fun parse(text:String):ReceiptDraft=if(fold(text).replace(" ","").contains("techcombank"))TechcombankReceiptParser().parse(text) else GenericReceiptParser().parse(text)
}
