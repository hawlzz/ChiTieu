package vn.chitieu.data
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import vn.chitieu.domain.*
import vn.chitieu.notification.*
import vn.chitieu.qr.BankRegistry
@RunWith(AndroidJUnit4::class)
class LedgerIntegrationTest {
 private lateinit var db:LedgerDb
 private lateinit var repo:Repository
 @Before fun setup() {db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),LedgerDb::class.java).build();repo=Repository(db)}
 @After fun close() {db.close()}
 @Test fun pendingConfirmAndExpire()=runTest {val time=System.currentTimeMillis();val old=Entry(amount=10000,status=Status.PENDING,source=Source.QR,createdAt=time-86_400_001L);val recent=Entry(amount=20000,status=Status.PENDING,source=Source.QR,createdAt=time);repo.save(old);repo.save(recent);repo.expire(time);assertEquals(Status.REVIEW,repo.dao.get(old.id)?.status);assertEquals(Status.PENDING,repo.dao.get(recent.id)?.status);repo.confirm(recent.id);repo.confirm(recent.id);assertEquals(2,repo.dao.all().size);assertEquals(Status.CONFIRMED,repo.dao.get(recent.id)?.status)}
 @Test fun matchingNotificationExactlyOnce()=runTest {val context=ApplicationProvider.getApplicationContext<android.content.Context>();val time=System.currentTimeMillis();val pending=Entry(amount=10000,status=Status.PENDING,source=Source.QR,createdAt=time-1000,payerPackage="fake.bank.test");repo.save(pending);val ingestor=MessageIngestor(repo,Preferences(context),BankRegistry(context));val parsed=ParsedMessage(10000,Kind.EXPENSE,"SYNTHETIC ONLY",time);ingestor.record("fake.bank.test",parsed,Source.NOTIFICATION,false);ingestor.record("fake.bank.test",parsed,Source.NOTIFICATION,false);assertEquals(1,repo.dao.all().size);assertEquals(Status.CONFIRMED,repo.dao.get(pending.id)?.status);assertEquals(1,repo.dao.receipts().size)}
 @Test fun invalidCsvCannotClearLedger()=runTest {val e=Entry(amount=10000);repo.save(e);runCatching {val parsed=Csv.parse("broken");BackupIo.importCsv(repo,parsed)};assertEquals(e,repo.dao.get(e.id))}
 @Test fun cancelDeletesPending()=runTest {val e=Entry(amount=10000,status=Status.PENDING);repo.save(e);repo.dao.delete(e.id);assertNull(repo.dao.get(e.id))}
}
