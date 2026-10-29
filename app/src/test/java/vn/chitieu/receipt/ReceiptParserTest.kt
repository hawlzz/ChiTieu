package vn.chitieu.receipt
import org.junit.Assert.*
import org.junit.Test
import vn.chitieu.data.*
import vn.chitieu.domain.*
// Synthetic texts only. No real names, account numbers, amounts or identifiers from D:/Train.
class ReceiptParserTest {
 private fun vi(amount:String="1.250.000")="""
 TECHCOMBANK
 Chuyển thành công
 VND $amount
 Người nhận
 NGUOI NHAN MAU
 0000000000
 Ngân hàng nhận
 NGAN HANG THU NGHIEM
 Lời nhắn
 NOI DUNG GIA
 Ngày chuyển
 1 thg 1, 2026 lúc 9:30
 Mã giao dịch
 TEST-REFERENCE-001
 Mã tham chiếu
 SYNTHETIC
 """.trimIndent()
 @Test fun vietnameseLayout() {val p=ReceiptParsers.parse(vi());assertEquals(1250000L,p.amount.value);assertEquals("NGUOI NHAN MAU",p.recipient.value);assertEquals("0000000000",p.account.value);assertEquals(ReceiptStatus.SUCCESS,p.status.value);assertEquals("NOI DUNG GIA",p.memo.value);assertNotNull(p.time.value);assertEquals("TEST-REFERENCE-001",p.transactionId.value)}
 @Test fun englishBeneficiary() {val p=ReceiptParsers.parse("TECHCOMBANK\nSuccessfully transferred\nTo TEST RECIPIENT\nVND 20,000\nBeneficiary account\nSYNTHETIC BANK\n0000000000\nMessage\nTEST MEMO\nTransfer date\n1 Jan 2026 at 9:30 PM\nTransaction ID\nTEST0001\nDone");assertEquals("TEST RECIPIENT",p.recipient.value);assertEquals("SYNTHETIC BANK",p.recipientBank.value);assertEquals(20000L,p.amount.value);assertNotNull(p.time.value)}
 @Test fun failureOverridesSuccessWord() {assertEquals(ReceiptStatus.FAILED,ReceiptParsers.parse(vi()+"\nGiao dịch không thành công").status.value)}
 @Test fun withdrawalRequestIsNotCompletedTransfer() {val p=ReceiptParsers.parse("TECHCOMBANK\nWithdrawal request successfully\nVND 50,000\nTransaction code\n000000\nExpiry date\n1 Jan 2026");assertEquals(ReceiptStatus.UNSUPPORTED,p.status.value);assertFalse(p.canReview)}
 @Test fun unsupportedImageCannotCreateExpense() {assertFalse(ReceiptParsers.parse("A photograph of a tree").canReview)}
 @Test fun overlayBalanceDoesNotReplaceTransferAmount() {assertEquals(1250000L,ReceiptParsers.parse("- VND 99,999,999\nSố dư VND 88,888,888\n"+vi()).amount.value)}
 @Test fun ambiguousPrimaryAmountsAreNotGuessed() {assertNull(ReceiptParsers.parse(vi().replace("Người nhận","VND 20,000\nNgười nhận")).amount.value)}
 @Test fun malformedAmounts() {for(s in listOf("0","1.25","9999999999999999"))assertNull(ReceiptMoney.parse(s))}
 @Test fun commonOcrDigitConfusion() {assertEquals(10000L,ReceiptMoney.parse("1O,OOO"));assertEquals(1250000L,ReceiptMoney.parse("1,250,000.00"))}
 @Test fun dateWithoutTimeIsLowConfidence() {val parsed=ReceiptDates.parse("Jan 1, 2026");assertNotNull(parsed);assertTrue(parsed!!.second<.8f)}
 @Test fun missingRecipientRemainsEmpty() {assertNull(ReceiptParsers.parse("TECHCOMBANK\nChuyển thành công\nVND 10,000").recipient.value)}
 @Test fun pendingMatchingUsesAmountAndTime() {val e=Entry(id="pending",amount=10000,status=Status.PENDING,createdAt=1000000);assertEquals(e,ReceiptMatching.candidates(listOf(e),emptyList(),10000,"","",1001000,"hash").single());assertTrue(ReceiptMatching.candidates(listOf(e),emptyList(),10000,"","",2000000,"hash").isEmpty())}
 @Test fun identicalImageFindsExistingEvenAfterTenMinutes() {val e=Entry(id="existing",amount=10000,createdAt=1);val evidence=ReceiptEvidence(e.id,"samehash","TEST","TEST",1);assertEquals(e,ReceiptMatching.candidates(listOf(e),listOf(evidence),10000,"","",99999999,"samehash").single())}
 @Test fun unrelatedSameAmountIsNotMerged() {val e=Entry(amount=10000,recipient="00001",createdAt=1000000);assertTrue(ReceiptMatching.candidates(listOf(e),emptyList(),10000,"00002","",1001000,"hash").isEmpty())}
}
