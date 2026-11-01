package vn.chitieu.statistics
import androidx.room.*
import kotlinx.coroutines.flow.*
import vn.chitieu.data.*
import javax.inject.Inject
@Dao interface StatisticsDao {
 @Query("SELECT COALESCE(SUM(CASE WHEN kind='INCOME' THEN amount ELSE 0 END),0) AS income, COALESCE(SUM(CASE WHEN kind='EXPENSE' THEN amount ELSE 0 END),0) AS expense, COUNT(*) AS count, COALESCE(MAX(amount),0) AS largest FROM entries WHERE status='CONFIRMED' AND createdAt>=:from AND createdAt<:until AND (:wallet='' OR wallet=:wallet) AND (:kind='' OR kind=:kind)")
 fun summary(from:Long,until:Long,wallet:String,kind:String):Flow<Summary>
 @Query("SELECT category AS label,SUM(amount) AS amount,COUNT(*) AS count FROM entries WHERE status='CONFIRMED' AND createdAt>=:from AND createdAt<:until AND (:wallet='' OR wallet=:wallet) AND (:kind='' OR kind=:kind) GROUP BY category ORDER BY amount DESC")
 fun categories(from:Long,until:Long,wallet:String,kind:String):Flow<List<Bucket>>
 @Query("SELECT strftime('%Y-%m-%d',createdAt/1000,'unixepoch','+7 hours') AS label,SUM(amount) AS amount,COUNT(*) AS count FROM entries WHERE status='CONFIRMED' AND createdAt>=:from AND createdAt<:until AND (:wallet='' OR wallet=:wallet) AND kind=:kind GROUP BY label ORDER BY label")
 fun days(from:Long,until:Long,wallet:String,kind:String):Flow<List<Bucket>>
 @Query("SELECT strftime('%Y-%m',createdAt/1000,'unixepoch','+7 hours') AS label,SUM(amount) AS amount,COUNT(*) AS count FROM entries WHERE status='CONFIRMED' AND createdAt>=:from AND createdAt<:until AND (:wallet='' OR wallet=:wallet) AND kind=:kind GROUP BY label ORDER BY label")
 fun months(from:Long,until:Long,wallet:String,kind:String):Flow<List<Bucket>>
 @Query("SELECT COALESCE(NULLIF(r.recipientName,''),NULLIF(CASE WHEN e.source='QR' THEN e.receivingBank ELSE '' END,'')) AS label,SUM(e.amount) AS amount,COUNT(*) AS count FROM entries e LEFT JOIN receipt_evidence r ON r.entryId=e.id WHERE e.status='CONFIRMED' AND e.kind='EXPENSE' AND e.createdAt>=:from AND e.createdAt<:until AND (:wallet='' OR e.wallet=:wallet) AND (e.source='QR' OR e.source='RECEIPT') AND COALESCE(NULLIF(r.recipientName,''),NULLIF(CASE WHEN e.source='QR' THEN e.receivingBank ELSE '' END,'')) IS NOT NULL GROUP BY label ORDER BY amount DESC LIMIT 5")
 fun recipients(from:Long,until:Long,wallet:String):Flow<List<Bucket>>
 @Query("SELECT * FROM entries WHERE status='CONFIRMED' AND createdAt>=:from AND createdAt<:until AND (:wallet='' OR wallet=:wallet) AND (:kind='' OR kind=:kind) AND category IN (:categories) ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
 fun entries(from:Long,until:Long,wallet:String,kind:String,categories:List<String>,limit:Int=50,offset:Int=0):Flow<List<Entry>>
}
data class TimeSeries(val income:List<Bucket>,val expense:List<Bucket>)
data class StatisticsData(val summary:Summary,val previous:Summary,val categories:List<Slice>,val series:TimeSeries,val trend:TimeSeries,val recipients:List<Bucket>,val expenseDays:List<Bucket>,val expenseCategories:List<Bucket>)
class StatisticsRepository @Inject constructor(private val db:LedgerDb){val dao get()=db.statistics()
 fun observe(period:Period,mode:String,wallet:String,kind:String):Flow<StatisticsData>{val previous=previousPeriod(mode,period);val months=periodFor("Tháng",period.end.minusDays(1)).end;val trendStart=months.minusMonths(12).atStartOfDay(vn.chitieu.domain.VN_ZONE).toInstant().toEpochMilli();val trendEnd=months.atStartOfDay(vn.chitieu.domain.VN_ZONE).toInstant().toEpochMilli()
 val head=combine(dao.summary(period.from,period.until,wallet,kind),dao.summary(previous.from,previous.until,wallet,kind),dao.categories(period.from,period.until,wallet,kind)){a,b,c->Triple(a,b,categorySlices(c))}
 val time=combine(if(mode=="Năm")dao.months(period.from,period.until,wallet,"INCOME") else dao.days(period.from,period.until,wallet,"INCOME"),if(mode=="Năm")dao.months(period.from,period.until,wallet,"EXPENSE") else dao.days(period.from,period.until,wallet,"EXPENSE"),::TimeSeries)
 val trend=combine(dao.months(trendStart,trendEnd,wallet,"INCOME"),dao.months(trendStart,trendEnd,wallet,"EXPENSE"),::TimeSeries)
 val insights=combine(dao.recipients(period.from,period.until,wallet),dao.days(period.from,period.until,wallet,"EXPENSE"),dao.categories(period.from,period.until,wallet,"EXPENSE")){a,b,c->Triple(a,b,c)}
 return combine(head,time,trend,insights){h,t,r,n->val keys=(0L..11).map {months.minusMonths(12).plusMonths(it).toString().take(7)};val filled:(List<Bucket>)->List<Bucket> = {rows->keys.map {key->rows.firstOrNull{it.label==key}?:Bucket(key,0)}};StatisticsData(h.first,h.second,h.third,t,TimeSeries(filled(r.income),filled(r.expense)),n.first,n.second,n.third)}.filter {data->val total=when(kind){"EXPENSE"->data.summary.expense;"INCOME"->data.summary.income;else->Math.addExact(data.summary.income,data.summary.expense)};data.categories.sumOf{it.amount}==total && (kind=="INCOME"||data.series.expense.sumOf{it.amount}==data.summary.expense) && (kind=="EXPENSE"||data.series.income.sumOf{it.amount}==data.summary.income)}
 }
}
class GetPeriodSummary @Inject constructor(private val repo:StatisticsRepository){operator fun invoke(period:Period,wallet:String,kind:String)=repo.dao.summary(period.from,period.until,wallet,kind)}
class GetCategoryBreakdown @Inject constructor(private val repo:StatisticsRepository){operator fun invoke(period:Period,wallet:String,kind:String)=repo.dao.categories(period.from,period.until,wallet,kind).map(::categorySlices)}
class GetTimeSeries @Inject constructor(private val repo:StatisticsRepository){operator fun invoke(period:Period,wallet:String,kind:String)=repo.dao.days(period.from,period.until,wallet,kind)}
class GetInsights @Inject constructor(private val repo:StatisticsRepository){operator fun invoke(period:Period,wallet:String,today:java.time.LocalDate=java.time.LocalDate.now(vn.chitieu.domain.VN_ZONE),monthly:Boolean=true)=combine(repo.dao.categories(period.from,period.until,wallet,"EXPENSE"),repo.dao.days(period.from,period.until,wallet,"EXPENSE")){categories,days->calculateInsights(period,today,monthly,categories,days)}}
