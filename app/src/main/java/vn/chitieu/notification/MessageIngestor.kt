package vn.chitieu.notification
import android.content.Context
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import vn.chitieu.data.*
import vn.chitieu.domain.*
import vn.chitieu.qr.*
import vn.chitieu.notification.parsers.*
object ListenerState {
 val connected=MutableStateFlow(false)
 val lastSeen=MutableStateFlow<Map<String,Long>>(emptyMap())
}
@Singleton class MessageIngestor @Inject constructor(val repo:Repository,val preferences:Preferences,val registry:BankRegistry) {
 private val parsers:Map<String,BankMessageParser> = mapOf("VCB" to VietcombankParser(),"ICB" to VietinBankParser(),"BIDV" to BIDVParser(),"VBA" to AgribankParser(),"TCB" to TechcombankParser(),"MB" to MBParser(),"ACB" to ACBParser(),"VPB" to VPBankParser(),"TPB" to TPBankParser(),"STB" to SacombankParser(),"HDB" to HDBankParser(),"VIB" to VIBParser(),"SHB" to SHBParser(),"OCB" to OCBParser(),"MSB" to MSBParser(),"SEAB" to SeABankParser(),"LPB" to LPBankParser(),"EIB" to EximbankParser(),"NAB" to NamABankParser(),"BAB" to BacABankParser(),"ABB" to ABBankParser(),"VCCB" to BVBankParser(),"CAKE" to CakeParser(),"TIMO" to TimoParser(),"Ubank" to UbankParser(),"KLB" to KienlongbankParser(),"NCB" to NCBParser(),"PVCB" to PVcomBankParser())
 suspend fun accept(pkg:String,body:String,time:Long,source:Source=Source.NOTIFICATION) {
  val settings=preferences.settings.first()
  val bank=registry.byPackage(pkg)?:return
  if(pkg !in settings.enabled)return
  if(source==Source.SMS&&!settings.sms)return
  if(source !in listOf(Source.NOTIFICATION,Source.SMS))return
  ListenerState.lastSeen.value=ListenerState.lastSeen.value+(pkg to time)
  if(settings.rawLog) {repo.dao.log(RawLog(bank=bank.shortName,body=body.take(4096),time=time));repo.dao.trimLogs()}
  val parsed=parsers[bank.code]?.parse(body.take(8192),time)?:return
  record(pkg,parsed,source,settings.autoAdd)
 }
 suspend fun record(pkg:String,parsed:ParsedMessage,source:Source,autoAdd:Boolean) {
  Money.validate(parsed.amount)
  val fingerprint=Reconciliation.fingerprint(pkg,parsed)
  repo.db.withTransaction {
   if(repo.dao.receipt(fingerprint)!=null)return@withTransaction
   val used=repo.dao.receipts().map {it.entryId}.toSet()
   val match=Reconciliation.qrMatch(repo.dao.all().filter {it.id !in used},pkg,parsed)
   val entryId=if(match!=null) {repo.dao.save(match.copy(status=Status.CONFIRMED));match.id} else {
    val entry=Entry(amount=parsed.amount,kind=parsed.kind,category=repo.suggest("",parsed.memo),wallet="Ngân hàng",createdAt=parsed.time,note=parsed.memo,source=source,status=if(autoAdd)Status.CONFIRMED else Status.APPROVAL,payerPackage=pkg,memo=parsed.memo)
    repo.dao.save(entry);entry.id
   }
   repo.dao.saveReceipt(Receipt(fingerprint,entryId,parsed.time))
  }
 }
}

