package vn.chitieu.receipt
import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import vn.chitieu.data.*
import vn.chitieu.domain.*
import java.io.File
import kotlin.math.abs
object ReceiptMatching {
 fun candidates(entries:List<Entry>,evidence:List<ReceiptEvidence>,amount:Long,account:String,name:String,time:Long,digest:String):List<Entry> {
  val byId=evidence.associateBy {it.entryId}
  return entries.filter {entry->
   if(byId[entry.id]?.digest==digest)return@filter true
   if(entry.kind!=Kind.EXPENSE||entry.amount!=amount||abs(time-entry.createdAt)>600_000)return@filter false
   val sameAccount=account.isNotBlank()&&entry.recipient.isNotBlank()&&account.replace(" ","")==entry.recipient.replace(" ","")
   val oldName=byId[entry.id]?.recipientName.orEmpty()
   val sameName=name.isNotBlank()&&oldName.isNotBlank()&&fold(name)==fold(oldName)
   entry.status==Status.PENDING||sameAccount||sameName
  }.sortedWith(compareBy<Entry> {if(byId[it.id]?.digest==digest)0 else 1}.thenBy {abs(time-it.createdAt)})
 }
}
@Singleton class ReceiptRepository @Inject constructor(@ApplicationContext val context:Context,val repo:Repository) {
 suspend fun candidates(scan:ReceiptScan):List<Entry> = ReceiptMatching.candidates(repo.dao.all(),repo.dao.allEvidence(),scan.draft.amount.value?:0,scan.draft.account.value.orEmpty(),scan.draft.recipient.value.orEmpty(),scan.draft.time.value?:0,scan.digest)
 suspend fun save(scan:ReceiptScan,amount:Long,name:String,account:String,bank:String,memo:String,time:Long,transactionId:String,category:String,wallet:String,mergeId:String?,keepImage:Boolean):String {
  require(scan.draft.status.value==ReceiptStatus.SUCCESS){"Ảnh chưa xác nhận giao dịch thành công"};Money.validate(amount);require(Regex("[a-f0-9]{64}").matches(scan.digest));require(name.length<=200&&account.length<=100&&memo.length<=1000&&transactionId.length<=100)
  var filename="";var created:File?=null;var oldImage=""
  if(keepImage){val dir=File(context.filesDir,"receipts").apply {mkdirs()};val file=File(dir,scan.digest+".img");if(!file.exists()){val bytes=ReceiptImages(context).read(Uri.parse(scan.uri));require(java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}==scan.digest){"Ảnh đã thay đổi, hãy chọn lại"};file.writeBytes(bytes);created=file};filename=file.name}
  val savedId=try {repo.db.withTransaction {
   val existing=mergeId?.let {repo.dao.get(it)?:error("Giao dịch đã thay đổi")}
   require(existing==null||existing.kind==Kind.EXPENSE){"Chỉ gộp với khoản chi"}
   val e=(existing?:Entry(amount=amount,source=Source.RECEIPT,createdAt=time)).copy(amount=amount,kind=Kind.EXPENSE,status=Status.CONFIRMED,category=category,wallet=wallet,recipient=account,receivingBank=bank,memo=memo,note=existing?.note.orEmpty())
   repo.save(e);val previous=repo.dao.evidenceFor(e.id);oldImage=previous?.imageName.orEmpty()
   repo.dao.evidence(ReceiptEvidence(e.id,scan.digest,transactionId,name,time,filename.ifBlank {if(previous?.digest==scan.digest)previous.imageName else ""}));e.id
  }}catch(e:Exception){created?.delete();throw e}
  runCatching {deleteUnusedImage(oldImage)};return savedId
 }
 suspend fun delete(id:String) {val old=repo.dao.evidenceFor(id);repo.dao.delete(id);deleteUnusedImage(old?.imageName.orEmpty())}
 private suspend fun deleteUnusedImage(name:String) {if(name.matches(Regex("[a-f0-9]{64}\\.img"))&&repo.dao.allEvidence().none {it.imageName==name})File(File(context.filesDir,"receipts"),name).delete()}
}
