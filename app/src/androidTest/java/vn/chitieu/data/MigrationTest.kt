package vn.chitieu.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
 @Test fun upgradePreservesLedgerAndAddsEvidence() = runTest {
  val context = ApplicationProvider.getApplicationContext<Context>()
  val name = "test-migration-" + java.util.UUID.randomUUID() + ".db"
  try {
   val initial = Room.databaseBuilder(context, LedgerDb::class.java, name).build()
   initial.dao().save(Entry(id="test-migration",amount=12345,note="[SIMULATED] migration"))
   // Reconstruct the exact v1 tables from v2, whose only added table is receipt_evidence.
   initial.openHelper.writableDatabase.apply {
    execSQL("DROP TABLE receipt_evidence")
    execSQL("UPDATE room_master_table SET identity_hash='d5e4bd1b22bbd3a439bcec73cb3d02df' WHERE id=42")
    version = 1
   }
   initial.close()
   val upgraded = Room.databaseBuilder(context, LedgerDb::class.java, name).addMigrations(MIGRATION_1_2).build()
   try {
    assertEquals(12345L, upgraded.dao().get("test-migration")?.amount)
    upgraded.dao().evidence(ReceiptEvidence("test-migration","a".repeat(64),"TEST","TEST",1))
    assertEquals(1, upgraded.dao().allEvidence().size)
    upgraded.dao().delete("test-migration")
    assertTrue(upgraded.dao().allEvidence().isEmpty())
   } finally { upgraded.close() }
  } finally { context.deleteDatabase(name) }
 }
}
