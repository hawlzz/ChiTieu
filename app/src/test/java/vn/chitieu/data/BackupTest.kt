package vn.chitieu.data
import org.junit.Assert.*
import org.junit.Test
import vn.chitieu.domain.*
import kotlinx.serialization.json.Json
class BackupTest {
 private fun entry()=Entry(id="synthetic",amount=123456,category="=SUM(A1)",wallet="'Cash",createdAt=1_700_000_000_000,note="=HYPERLINK(\"https://example.test\")\nNội dung, có dấu \"nháy\"",memo="+123",recipient="0000000000")
 @Test fun csvRoundTripWithQuotesAndNewlines() {val data=entry();assertEquals(listOf(data),Csv.parse(Csv.export(listOf(data))))}
 @Test fun csvNeutralizesEveryTextCell() {val row=Csv.rows(Csv.export(listOf(entry())))[1];for(i in listOf(1,3,4,5,7,8,9,10,11,12,13,14))assertTrue(row[i].startsWith("'"));assertEquals("123456",row[2])}
 @Test fun csvEmptyAndBom() {assertTrue(Csv.parse("\uFEFF"+Csv.export(emptyList())).isEmpty())}
 @Test(expected=IllegalArgumentException::class) fun duplicateIdsRejectedBeforeImport() {Csv.parse(Csv.export(listOf(entry(),entry())))}
 @Test(expected=IllegalArgumentException::class) fun unclosedQuotesRejected() {Csv.rows("\"unterminated")}
 @Test(expected=IllegalArgumentException::class) fun garbageAfterQuoteRejected() {Csv.rows("\"ok\"bad")}
 @Test(expected=IllegalArgumentException::class) fun negativeMoneyRejected() {Csv.parse(Csv.export(listOf(entry().copy(amount=-1))))}
 @Test fun backupRoundTrip() {val backup=Backup(entries=listOf(entry()),categories=listOf(Category("=SUM(A1)")),wallets=listOf(Wallet("'Cash")),rules=emptyList(),receipts=emptyList());assertEquals(backup,BackupIo.decode(Json.encodeToString(backup)))}
 @Test(expected=IllegalArgumentException::class) fun futureSchemaRejected() {BackupIo.decode(Json.encodeToString(Backup(version=99,entries=emptyList(),categories=emptyList(),wallets=emptyList(),rules=emptyList(),receipts=emptyList())))}
}
