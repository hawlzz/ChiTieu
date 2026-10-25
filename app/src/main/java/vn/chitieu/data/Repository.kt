package vn.chitieu.data
import javax.inject.Inject
import javax.inject.Singleton
import vn.chitieu.domain.*
import androidx.room.withTransaction
@Singleton class Repository @Inject constructor(val db:LedgerDb) {
 val dao=db.dao()
 val entries=dao.observe()
 suspend fun seed() {dao.seedCategories(Defaults.categories.map {Category(it)});dao.seedWallets(listOf(Wallet("Tiền mặt"),Wallet("Ngân hàng")))}
 suspend fun save(e:Entry) {Money.validate(e.amount);require(e.category.isNotBlank()&&e.wallet.isNotBlank());dao.save(e)}
 suspend fun confirm(id:String) {db.withTransaction {dao.get(id)?.let {if(it.status!=Status.CONFIRMED)dao.save(it.copy(status=Status.CONFIRMED))}}}
 suspend fun expire(now:Long=System.currentTimeMillis())=dao.expire(now-86_400_000L)
 suspend fun suggest(recipient:String,memo:String):String=dao.rules().firstOrNull {memo.contains(it.keyword,true)}?.category?:dao.all().firstOrNull {recipient.isNotBlank()&&it.recipient==recipient&&it.status==Status.CONFIRMED}?.category?:"Khác"
}