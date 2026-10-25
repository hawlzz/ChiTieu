package vn.chitieu.domain
import java.time.*
import java.text.NumberFormat
import java.util.Locale
import kotlinx.serialization.Serializable
@Serializable enum class Kind { INCOME, EXPENSE }
@Serializable enum class Status { CONFIRMED, PENDING, REVIEW, APPROVAL }
@Serializable enum class Source { MANUAL, QR, NOTIFICATION, SMS }
object Money {
 const val MAX=999_999_999_999L
 fun validate(amount:Long):Long { require(amount in 1..MAX) { "Số tiền phải từ 1 đến 999.999.999.999 ₫" };return amount }
 fun input(text:String):Long?=text.filter(Char::isDigit).toLongOrNull()?.takeIf {it in 1..MAX}
 fun format(amount:Long):String=NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(amount)+" ₫"
}
val VN_ZONE:ZoneId=ZoneId.of("Asia/Ho_Chi_Minh")
fun monthOf(time:Long):YearMonth=YearMonth.from(Instant.ofEpochMilli(time).atZone(VN_ZONE))
object Defaults { val categories=listOf("Ăn uống","Di chuyển","Nhà ở","Hoá đơn","Mua sắm","Giải trí","Sức khoẻ","Giáo dục","Lương","Thưởng","Khác") }