package vn.chitieu.notification
import org.junit.Assert.*
import org.junit.Test
import vn.chitieu.data.Entry
import vn.chitieu.domain.*
class MessageTest {
 private val time=1_000_000L
 private fun message(amount:Long=1250000,kind:Kind=Kind.EXPENSE,at:Long=time)=ParsedMessage(amount,kind,"TEST",at)
 private fun pending(id:String="one",amount:Long=1250000,at:Long=time-1000)=Entry(id=id,amount=amount,source=Source.QR,status=Status.PENDING,payerPackage="fake.bank.test",createdAt=at)
 @Test fun fakeGroupedAmounts() {for(s in listOf("1.250.000đ","1,250,000 VND","1250000"))assertEquals(1250000L,SyntheticParser.parse("[SIMULATED] OUT "+s+" MEMO=TEST",time)?.amount)}
 @Test fun fakeIncomeAndBalance() {val m=SyntheticParser.parse("[SIMULATED] IN 500,000 VND BAL=1,250,000 MEMO=TEST",time);assertEquals(Kind.INCOME,m?.kind);assertEquals(1250000L,m?.balance)}
 @Test fun noRealTemplateGuess() {assertNull(UnverifiedParser("test").parse("GD: -1.250.000 VND",time));assertNull(SyntheticParser.parse("GD: -1.250.000 VND",time))}
 @Test fun rejectMalformedMoney() {for(s in listOf("1,25,000","99999999999999999999999999999","0","1.23"))assertNull(SyntheticParser.parse("[SIMULATED] OUT "+s+" MEMO=TEST",time))}
 @Test fun stableFingerprintWithinMinute() {val first=message(at=60_001);val second=message(at=119_999);assertEquals(Reconciliation.fingerprint("bank",first),Reconciliation.fingerprint("bank",second))}
 @Test fun fingerprintSeparatesBankAndDirection() {assertNotEquals(Reconciliation.fingerprint("a",message()),Reconciliation.fingerprint("b",message()));assertNotEquals(Reconciliation.fingerprint("a",message()),Reconciliation.fingerprint("a",message(kind=Kind.INCOME)))}
 @Test fun pendingConfirmation() {assertEquals("one",Reconciliation.qrMatch(listOf(pending()),"fake.bank.test",message())?.id)}
 @Test fun confirmedQrStillDeduplicates() {assertEquals("one",Reconciliation.qrMatch(listOf(pending().copy(status=Status.CONFIRMED)),"fake.bank.test",message())?.id)}
 @Test fun noMatchOnIncomingOrWrongBank() {assertNull(Reconciliation.qrMatch(listOf(pending()),"other",message()));assertNull(Reconciliation.qrMatch(listOf(pending()),"fake.bank.test",message(kind=Kind.INCOME)))}
 @Test fun outsideWindowAndBeforeLaunch() {assertNull(Reconciliation.qrMatch(listOf(pending(at=time-600001)),"fake.bank.test",message()));assertNull(Reconciliation.qrMatch(listOf(pending(at=time+1)),"fake.bank.test",message()))}
 @Test fun exactWindowInclusive() {assertNotNull(Reconciliation.qrMatch(listOf(pending(at=time-600000)),"fake.bank.test",message()))}
 @Test fun ambiguousAmountRequiresReview() {assertNull(Reconciliation.qrMatch(listOf(pending(),pending(id="two")),"fake.bank.test",message()))}
 @Test fun manualPaymentDoesNotConsumeNotification() {assertNull(Reconciliation.qrMatch(listOf(pending().copy(source=Source.MANUAL)),"fake.bank.test",message()))}
}
