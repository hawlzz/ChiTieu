package vn.chitieu.data
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import androidx.room.withTransaction
import vn.chitieu.domain.*
@Serializable data class Backup(val version:Int=1,val entries:List<Entry>,val categories:List<Category>,val wallets:List<Wallet>,val rules:List<CategoryRule>,val receipts:List<Receipt>)
object Csv {
 val header=listOf("format","id","amount","kind","category","wallet","createdAt","note","source","status","bankBin","recipient","memo","receivingBank","payerPackage")
 private fun quote(s:String)="\""+s.replace("\"","\"\"")+"\""
 private fun text(s:String)="'"+s
 fun export(entries:List<Entry>):String=(listOf(header)+entries.map {e->listOf("chitieu-v1",text(e.id),e.amount.toString(),text(e.kind.name),text(e.category),text(e.wallet),e.createdAt.toString(),text(e.note),text(e.source.name),text(e.status.name),text(e.bankBin),text(e.recipient),text(e.memo),text(e.receivingBank),text(e.payerPackage))}).joinToString("\r\n"){row->row.joinToString(",",transform=::quote)}
 fun rows(input:String):List<List<String>> {
  val rows=mutableListOf<List<String>>();val row=mutableListOf<String>();val cell=StringBuilder();var quoted=false;var closed=false;var at=0
  fun finishCell(){row.add(cell.toString());cell.setLength(0);closed=false}
  fun finishRow(){finishCell();rows.add(row.toList());row.clear();require(rows.size<=50_001){"Tối đa 50.000 giao dịch"}}
  val data=input.removePrefix("\uFEFF")
  while(at<data.length){val c=data[at];if(quoted){if(c=='"'){if(at+1<data.length&&data[at+1]=='"'){cell.append('"');at++}else{quoted=false;closed=true}}else cell.append(c)}else when(c){'"'->{require(cell.isEmpty()&&!closed){"CSV sai dấu ngoặc kép"};quoted=true};','->finishCell();'\r','\n'->{if(c=='\r'&&at+1<data.length&&data[at+1]=='\n')at++;finishRow()};else->{require(!closed){"CSV có dữ liệu sau dấu ngoặc kép"};cell.append(c)}};at++}
  require(!quoted){"CSV thiếu dấu ngoặc kép"};if(cell.isNotEmpty()||row.isNotEmpty()||closed)finishRow();return rows
 }
 fun parse(input:String):List<Entry> {
  val rows=rows(input);require(rows.firstOrNull()==header){"Chỉ nhận CSV ChiTieu v1 có đúng tiêu đề"}
  return rows.drop(1).map {r->require(r.size==header.size&&r[0]=="chitieu-v1"){"CSV không đúng định dạng"};fun t(i:Int):String {require(r[i].startsWith("'"));return r[i].drop(1)}
   Entry(id=t(1),amount=r[2].toLong(),kind=Kind.valueOf(t(3)),category=t(4),wallet=t(5),createdAt=r[6].toLong(),note=t(7),source=Source.valueOf(t(8)),status=Status.valueOf(t(9)),bankBin=t(10),recipient=t(11),memo=t(12),receivingBank=t(13),payerPackage=t(14)).also(::validate)
  }.also {require(it.map {e->e.id}.distinct().size==it.size){"CSV có ID trùng"}}
 }
 fun validate(e:Entry) {Money.validate(e.amount);require(e.id.length in 1..100&&e.category.length in 1..100&&e.wallet.length in 1..100&&e.note.length<=4000&&e.memo.length<=1000&&e.recipient.length<=100&&e.payerPackage.length<=200);require(e.createdAt in -2_208_988_800_000L..7_258_118_400_000L){"Ngày ngoài phạm vi 1900–2200"}}
}
object BackupIo {
 private val json=Json {encodeDefaults=true;ignoreUnknownKeys=false}
 suspend fun export(repo:Repository):String=repo.db.withTransaction {json.encodeToString(Backup(entries=repo.dao.all(),categories=repo.dao.allCategories(),wallets=repo.dao.allWallets(),rules=repo.dao.rules(),receipts=repo.dao.receipts()))}
 fun decode(input:String):Backup {val b=json.decodeFromString<Backup>(input);require(b.version==1&&b.entries.size<=50_000&&b.categories.size<=1000&&b.wallets.size<=1000&&b.rules.size<=1000&&b.receipts.size<=100_000);b.entries.forEach(Csv::validate);require(b.entries.map {it.id}.distinct().size==b.entries.size);require(b.categories.all {it.name.length in 1..100}&&b.wallets.all {it.name.length in 1..100});require(b.rules.all {it.keyword.length in 1..200&&it.category.length in 1..100});require(b.categories.map {it.name}.distinct().size==b.categories.size&&b.wallets.map {it.name}.distinct().size==b.wallets.size);require(b.categories.all {it.icon.length<=20&&it.color in 0..0xFFFFFFFFL});val ids=b.entries.map {it.id}.toSet();require(b.receipts.all {Regex("[a-f0-9]{64}").matches(it.fingerprint)&&it.entryId in ids});return b}
 suspend fun restore(repo:Repository,b:Backup) {repo.db.withTransaction {repo.dao.clearReceipts();repo.dao.clearEntries();repo.dao.clearRules();repo.dao.clearCategories();repo.dao.clearWallets();b.categories.forEach {repo.dao.saveCategory(it)};b.wallets.forEach {repo.dao.saveWallet(it)};b.rules.forEach {repo.dao.saveRule(it)};b.entries.forEach {repo.dao.save(it)};b.receipts.forEach {repo.dao.saveReceipt(it)};repo.dao.clearLogs();repo.seed()}}
 suspend fun importCsv(repo:Repository,entries:List<Entry>) {repo.db.withTransaction {val ids=repo.dao.all().map {it.id}.toSet();entries.filter {it.id !in ids}.forEach {repo.dao.save(it);repo.dao.saveCategory(Category(it.category));repo.dao.saveWallet(Wallet(it.wallet))}}}
}

