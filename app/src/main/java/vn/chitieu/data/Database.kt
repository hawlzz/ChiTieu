package vn.chitieu.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import vn.chitieu.domain.*
@Serializable @Entity(tableName="entries",indices=[Index("createdAt"),Index("status"),Index("recipient")])
data class Entry(@PrimaryKey val id:String=java.util.UUID.randomUUID().toString(),val amount:Long,val kind:Kind=Kind.EXPENSE,val category:String="Khác",val wallet:String="Tiền mặt",val createdAt:Long=System.currentTimeMillis(),val note:String="",val source:Source=Source.MANUAL,val status:Status=Status.CONFIRMED,val bankBin:String="",val recipient:String="",val memo:String="",val receivingBank:String="",val payerPackage:String="")
@Serializable @Entity(tableName="categories") data class Category(@PrimaryKey val name:String,val icon:String="●",val color:Long=0xFF267D66)
@Serializable @Entity(tableName="wallets") data class Wallet(@PrimaryKey val name:String)
@Serializable @Entity(tableName="rules") data class CategoryRule(@PrimaryKey val keyword:String,val category:String)
@Serializable @Entity(tableName="receipts") data class Receipt(@PrimaryKey val fingerprint:String,val entryId:String,val receivedAt:Long)
@Entity(tableName="raw_logs") data class RawLog(@PrimaryKey(autoGenerate=true) val id:Long=0,val bank:String,val body:String,val time:Long)
class Converters {
 @TypeConverter fun kind(v:String):Kind=Kind.valueOf(v)
 @TypeConverter fun kind(v:Kind):String=v.name
 @TypeConverter fun status(v:String):Status=Status.valueOf(v)
 @TypeConverter fun status(v:Status):String=v.name
 @TypeConverter fun source(v:String):Source=Source.valueOf(v)
 @TypeConverter fun source(v:Source):String=v.name
}
@Dao interface LedgerDao {
 @Query("SELECT * FROM entries ORDER BY createdAt DESC") fun observe():Flow<List<Entry>>
 @Query("SELECT * FROM entries ORDER BY createdAt DESC") suspend fun all():List<Entry>
 @Query("SELECT * FROM entries WHERE id=:id") suspend fun get(id:String):Entry?
 @Upsert suspend fun save(e:Entry)
 @Query("DELETE FROM entries WHERE id=:id") suspend fun delete(id:String)
 @Query("DELETE FROM entries") suspend fun clearEntries()
 @Query("UPDATE entries SET status='REVIEW' WHERE status='PENDING' AND createdAt<:cutoff") suspend fun expire(cutoff:Long)
 @Query("SELECT * FROM categories ORDER BY name") fun categories():Flow<List<Category>>
 @Query("SELECT * FROM categories") suspend fun allCategories():List<Category>
 @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun seedCategories(v:List<Category>)
 @Upsert suspend fun saveCategory(v:Category)
 @Query("SELECT * FROM wallets ORDER BY name") fun wallets():Flow<List<Wallet>>
 @Query("SELECT * FROM wallets") suspend fun allWallets():List<Wallet>
 @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun seedWallets(v:List<Wallet>)
 @Upsert suspend fun saveWallet(v:Wallet)
 @Query("SELECT * FROM rules") suspend fun rules():List<CategoryRule>
 @Query("SELECT * FROM rules") fun observeRules():Flow<List<CategoryRule>>
 @Upsert suspend fun saveRule(v:CategoryRule)
 @Query("DELETE FROM rules WHERE keyword=:keyword") suspend fun deleteRule(keyword:String)
 @Query("SELECT * FROM receipts WHERE fingerprint=:fingerprint") suspend fun receipt(fingerprint:String):Receipt?
 @Query("SELECT * FROM receipts") suspend fun receipts():List<Receipt>
 @Upsert suspend fun saveReceipt(v:Receipt)
 @Query("DELETE FROM receipts") suspend fun clearReceipts()
 @Insert suspend fun log(v:RawLog)
 @Query("SELECT * FROM raw_logs ORDER BY time DESC LIMIT 100") fun logs():Flow<List<RawLog>>
 @Query("DELETE FROM raw_logs") suspend fun clearLogs()
 @Query("DELETE FROM raw_logs WHERE id NOT IN (SELECT id FROM raw_logs ORDER BY time DESC LIMIT 100)") suspend fun trimLogs()
 @Query("DELETE FROM rules") suspend fun clearRules()
 @Query("DELETE FROM categories") suspend fun clearCategories()
 @Query("DELETE FROM wallets") suspend fun clearWallets()
}
@Database(entities=[Entry::class,Category::class,Wallet::class,CategoryRule::class,Receipt::class,RawLog::class],version=1,exportSchema=true)
@TypeConverters(Converters::class)
abstract class LedgerDb:RoomDatabase() {abstract fun dao():LedgerDao}