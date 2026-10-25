package vn.chitieu.qr
import org.junit.Assert.*
import org.junit.Test
import vn.chitieu.bank.DeepLink
// All payloads below are SYNTHETIC fixtures, never real accounts or banking notifications.
class EmvTest {
 private fun field(tag:String,value:String)=tag+value.length.toString().padStart(2,'0')+value
 private fun payload(amount:String?="1250000",mode:String="11",service:String="QRIBFTTA",currency:String="704",account:String="0000000000"):String {
  val receiver=field("00","970436")+field("01",account)
  val merchant=field("00","A000000727")+field("01",receiver)+field("02",service)
  val base=field("00","01")+field("01",mode)+field("38",merchant)+field("53",currency)+(amount?.let {field("54",it)}?:"")+field("58","VN")+field("59","SYNTHETIC TEST")+field("62",field("01","TEST-001")+field("08","TEST PAYMENT"))+"6304"
  return base+Crc16.compute(base)
 }
 @Test fun referenceCrc() {assertEquals("29B1",Crc16.compute("123456789"))}
 @Test fun staticWithAmount() {val qr=EmvParser.parse(payload());assertEquals(1250000L,qr.amount);assertFalse(qr.dynamic);assertEquals("TEST PAYMENT",qr.memo);assertEquals("TEST-001",qr.bill)}
 @Test fun dynamicWithoutAmount() {val qr=EmvParser.parse(payload(null,"12"));assertTrue(qr.dynamic);assertNull(qr.amount)}
 @Test fun staticWithoutAmount() {assertNull(EmvParser.parse(payload(null)).amount)}
 @Test fun dynamicWithAmount() {assertEquals(1250000L,EmvParser.parse(payload(mode="12")).amount)}
 @Test fun cardService() {assertEquals("QRIBFTTC",EmvParser.parse(payload(service="QRIBFTTC")).service)}
 @Test(expected=QrException::class) fun badCrc() {val q=payload();EmvParser.parse(q.dropLast(4)+if(q.takeLast(4)=="0000")"0001" else "0000")}
 @Test(expected=QrException::class) fun unknownQr() {EmvParser.parse("https://example.test/not-a-bank")}
 @Test(expected=QrException::class) fun truncatedTlv() {EmvParser.tlv("0099abc")}
 @Test(expected=QrException::class) fun duplicateTlv() {EmvParser.tlv("000201000201")}
 @Test(expected=QrException::class) fun invalidMode() {EmvParser.parse(payload(mode="99"))}
 @Test(expected=QrException::class) fun fractionalVnd() {EmvParser.parse(payload("1.25"))}
 @Test(expected=QrException::class) fun hugeAmount() {EmvParser.parse(payload("999999999999999999999999999999"))}
 @Test(expected=QrException::class) fun foreignCurrency() {EmvParser.parse(payload(currency="840"))}
 @Test fun zeroDecimals() {assertEquals(1000L,EmvParser.parse(payload("1000.00")).amount)}
 @Test fun urlEncoding() {val url=DeepLink.build("mb","0000000000","VCB",1250000,"Tiền ăn & cafe","NGUYEN A");assertTrue(url.contains("ba=0000000000%40vcb"));assertTrue(url.contains("tn=Ti%E1%BB%81n%20%C4%83n%20%26%20cafe"));assertFalse(url.contains(" "))}
 @Test(expected=IllegalArgumentException::class) fun injectionRejected() {DeepLink.build("mb&app=other","0","vcb",1,"","")}
}
