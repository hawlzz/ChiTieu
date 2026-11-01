package vn.chitieu.data
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
val MIGRATION_1_2=object:Migration(1,2) {
 override fun migrate(db:SupportSQLiteDatabase) {db.execSQL("CREATE TABLE IF NOT EXISTS `receipt_evidence` (`entryId` TEXT NOT NULL, `digest` TEXT NOT NULL, `transactionId` TEXT NOT NULL, `recipientName` TEXT NOT NULL, `receiptTime` INTEGER NOT NULL, `imageName` TEXT NOT NULL, PRIMARY KEY(`entryId`), FOREIGN KEY(`entryId`) REFERENCES `entries`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")}
}
val MIGRATION_2_3=object:Migration(2,3){override fun migrate(db:SupportSQLiteDatabase){db.execSQL("CREATE INDEX IF NOT EXISTS `index_entries_status_createdAt_kind_wallet_category` ON `entries` (`status`,`createdAt`,`kind`,`wallet`,`category`)")}}
