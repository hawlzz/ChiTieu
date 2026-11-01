package vn.chitieu.statistics
import java.time.*
import java.time.temporal.TemporalAdjusters
import java.math.BigInteger
import vn.chitieu.domain.VN_ZONE
data class Period(val start:LocalDate,val end:LocalDate){init{require(end>start)};val from get()=start.atStartOfDay(VN_ZONE).toInstant().toEpochMilli();val until get()=end.atStartOfDay(VN_ZONE).toInstant().toEpochMilli();val days get()=java.time.temporal.ChronoUnit.DAYS.between(start,end);fun previous()=Period(start.minusDays(days),start)}
fun periodFor(mode:String,date:LocalDate):Period=when(mode){"Tuần"->{val start=date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));Period(start,start.plusWeeks(1))};"Năm"->Period(date.withDayOfYear(1),date.withDayOfYear(1).plusYears(1));else->Period(date.withDayOfMonth(1),date.withDayOfMonth(1).plusMonths(1))}
fun previousPeriod(mode:String,period:Period)=when(mode){"Tháng"->periodFor(mode,period.start.minusMonths(1));"Năm"->periodFor(mode,period.start.minusYears(1));else->period.previous()}
data class Summary(val income:Long=0,val expense:Long=0,val count:Long=0,val largest:Long=0){fun total(mode:String)=when(mode){"Chi"->expense;"Thu"->income;else->Math.addExact(income,expense)}}
data class Bucket(val label:String,val amount:Long,val count:Long=0)
data class Slice(val label:String,val amount:Long,val percent:Int,val members:Set<String>)
fun categorySlices(rows:List<Bucket>):List<Slice>{val total=rows.fold(0L){a,b->Math.addExact(a,b.amount)};if(total==0L)return emptyList();val other=rows.filter{BigInteger.valueOf(it.amount)*BigInteger.valueOf(100)<BigInteger.valueOf(total)*BigInteger.valueOf(3)};val kept=rows.filter{it !in other}.map{Slice(it.label,it.amount,0,setOf(it.label))}.toMutableList();if(other.isNotEmpty()){val existing=kept.indexOfFirst{it.label=="Khác"};val amount=other.sumOf{it.amount}+(kept.getOrNull(existing)?.amount?:0);val names=other.map{it.label}.toSet()+(kept.getOrNull(existing)?.members?:emptySet());if(existing>=0)kept.removeAt(existing);kept.add(Slice("Khác",amount,0,names))};val sorted=kept.sortedByDescending{it.amount};val percentages=sorted.map{(BigInteger.valueOf(it.amount)*BigInteger.valueOf(100)/BigInteger.valueOf(total)).toInt()}.toMutableList();val rank=sorted.indices.sortedByDescending{BigInteger.valueOf(sorted[it].amount)*BigInteger.valueOf(100)%BigInteger.valueOf(total)};repeat(100-percentages.sum()){percentages[rank[it%rank.size]]++};return sorted.mapIndexed{i,v->v.copy(percent=percentages[i])}}
fun comparison(current:Long,previous:Long):String? {if(previous==0L)return null;val delta=BigInteger.valueOf(current)-BigInteger.valueOf(previous);val percent=delta.abs()*BigInteger.valueOf(100)/BigInteger.valueOf(previous);return (if(delta.signum()>=0)"↑ " else "↓ ")+percent+"%"}
fun forecast(total:Long,elapsedDays:Long,days:Long):Long=if(elapsedDays<=0)0 else (BigInteger.valueOf(total)*BigInteger.valueOf(days)/BigInteger.valueOf(elapsedDays)).longValueExact()

data class Insights(val topCategory:Bucket?,val topDay:Bucket?,val weekday:java.time.DayOfWeek?,val forecast:Long?)
fun calculateInsights(period:Period,today:LocalDate,monthly:Boolean,categories:List<Bucket>,days:List<Bucket>):Insights {
 val weekday=days.groupBy{LocalDate.parse(it.label).dayOfWeek}.mapValues{it.value.sumOf{row->row.amount}}.maxByOrNull{it.value}?.key
 val estimate=if(monthly&&YearMonth.from(period.start)==YearMonth.from(today))forecast(days.filter{LocalDate.parse(it.label)<=today}.sumOf{it.amount},today.dayOfMonth.toLong(),period.days) else null
 return Insights(categories.maxByOrNull{it.amount},days.maxByOrNull{it.amount},weekday,estimate)
}

/** Bound chart width independently of the date-picker range and display density. */
fun timeChartStride(period:Period):Long=((period.days+119)/120).coerceAtLeast(1)
fun timeChartKeys(period:Period,monthly:Boolean):List<String> = if(monthly) (0L..11).map {period.start.plusMonths(it).toString().take(7)} else (0L until period.days step timeChartStride(period)).map {period.start.plusDays(it).toString()}
fun timeChartBuckets(rows:List<Bucket>,period:Period,monthly:Boolean):Map<String,Long> {
 if(monthly)return rows.associate {it.label to it.amount}
 val stride=timeChartStride(period)
 return rows.filter {val date=LocalDate.parse(it.label);date>=period.start&&date<period.end}.groupBy {row->val offset=java.time.temporal.ChronoUnit.DAYS.between(period.start,LocalDate.parse(row.label));period.start.plusDays(offset/stride*stride).toString()}.mapValues {(_,values)->values.fold(0L){sum,row->Math.addExact(sum,row.amount)}}
}
