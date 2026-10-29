package vn.chitieu.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import vn.chitieu.domain.*
import vn.chitieu.receipt.*

@RunWith(AndroidJUnit4::class)
class ReceiptPersistenceTest {
 private lateinit var db:LedgerDb
 private lateinit var receipts:ReceiptRepository
 private val context:Context get()=ApplicationProvider.getApplicationContext()
 @Before fun open() {db=Room.inMemoryDatabaseBuilder(context,LedgerDb::class.java).build();receipts=ReceiptRepository(context,Repository(db))}
 @After fun close() {db.close()}
 private fun scan(digest:String,status:ReceiptStatus=ReceiptStatus.SUCCESS)=ReceiptScan(ReceiptDraft(status=ReceiptField(status,1f)),digest,"content://synthetic/never-read",1)
 @Test fun confirmedReceiptMergesPendingWithoutKeepingImage()=runTest {
  db.dao().save(Entry(id="test-pending",amount=10000,status=Status.PENDING,source=Source.QR))
  val id=receipts.save(scan("a".repeat(64)),10000,"SYNTHETIC","TEST","TEST","TEST",1,"TEST","Khác","TEST","test-pending",false)
  assertEquals("test-pending",id)
  assertEquals(1,db.dao().all().size)
  assertEquals(Status.CONFIRMED,db.dao().get(id)?.status)
  assertEquals("",db.dao().evidenceFor(id)?.imageName)
 }
 @Test fun failedReceiptCannotCreateTransaction()=runTest {
  val failure=runCatching {receipts.save(scan("a".repeat(64),ReceiptStatus.FAILED),10000,"TEST","TEST","TEST","TEST",1,"TEST","Khác","TEST",null,false)}
  assertTrue(failure.isFailure)
  assertTrue(db.dao().all().isEmpty())
 }
 @Test fun receiptCannotOverwriteIncome()=runTest {
  db.dao().save(Entry(id="test-income",amount=10000,kind=Kind.INCOME))
  assertTrue(runCatching {receipts.save(scan("a".repeat(64)),10000,"TEST","TEST","TEST","TEST",1,"TEST","Khác","TEST","test-income",false)}.isFailure)
  assertEquals(Kind.INCOME,db.dao().get("test-income")?.kind)
 }
}
