package vn.chitieu.statistics
import java.time.*
import org.junit.Assert.*
import org.junit.Test
class StatisticsTest {
 @Test fun monthYearWeekAndTimezoneBoundaries(){val p=periodFor("Tháng",LocalDate.of(2024,2,15));assertEquals(29L,p.days);assertEquals(Instant.parse("2024-01-31T17:00:00Z").toEpochMilli(),p.from);assertEquals(LocalDate.of(2024,1,1),previousPeriod("Tháng",p).start);assertEquals(LocalDate.of(2024,12,30),periodFor("Tuần",LocalDate.of(2025,1,1)).start);assertEquals(LocalDate.of(2024,1,1),previousPeriod("Năm",periodFor("Năm",LocalDate.of(2025,1,1))).start)}
 @Test fun zeroAndEmptyDoNotDivide(){assertTrue(categorySlices(emptyList()).isEmpty());assertNull(comparison(100,0));assertEquals(0L,forecast(200,0,31))}
 @Test fun roundingAlwaysTotals100(){val slices=categorySlices(listOf(Bucket("A",1),Bucket("B",1),Bucket("C",1)));assertEquals(100,slices.sumOf{it.percent});assertEquals(listOf(34,33,33),slices.map{it.percent})}
 @Test fun smallCategoriesGroupedWithoutLosingMembers(){val slices=categorySlices(listOf(Bucket("A",9700000000),Bucket("B",100000000),Bucket("C",100000000),Bucket("Khác",100000000)));assertEquals(100,slices.sumOf{it.percent});assertEquals(10000000000,slices.sumOf{it.amount});assertEquals(setOf("B","C","Khác"),slices.first{it.label=="Khác"}.members)}
 @Test fun largeValuesAvoidIntermediateOverflow(){assertEquals(100,categorySlices(listOf(Bucket("A",Long.MAX_VALUE/2),Bucket("B",Long.MAX_VALUE/2))).sumOf{it.percent});assertEquals(31000000000L,forecast(1000000000,1,31));assertEquals("↑ 100%",comparison(4000000000,2000000000))}
 @Test fun forecastUsesOnlyElapsedDaysAndCurrentMonth(){val period=periodFor("Tháng",LocalDate.of(2026,10,10));val days=listOf(Bucket("2026-10-01",1000),Bucket("2026-10-20",10000));assertEquals(3100L,calculateInsights(period,LocalDate.of(2026,10,10),true,emptyList(),days).forecast);assertNull(calculateInsights(period,LocalDate.of(2026,11,10),true,emptyList(),days).forecast)}
 @Test fun customPeriodUsesSameNumberOfPreviousDays(){val period=Period(LocalDate.of(2026,1,1),LocalDate.of(2026,1,4));assertEquals(LocalDate.of(2025,12,29),period.previous().start);assertEquals(3L,period.previous().days)}
 @Test fun longCustomPeriodKeepsChartBoundedAndPreservesTotals(){val period=Period(LocalDate.of(1900,1,1),LocalDate.of(2101,1,1));assertTrue(timeChartKeys(period,false).size<=120);val rows=listOf(Bucket("1900-01-01",3000000000),Bucket("1900-01-02",4000000000),Bucket("2100-12-31",5000000000));assertEquals(12000000000,timeChartBuckets(rows,period,false).values.sum());assertEquals(7000000000,timeChartBuckets(rows,period,false)["1900-01-01"])}
 @Test fun shortPeriodKeepsDailyColumnsAndYearKeeps12Months(){val month=periodFor("Tháng",LocalDate.of(2026,10,1));assertEquals(31,timeChartKeys(month,false).size);assertEquals(1L,timeChartStride(month));assertEquals(12,timeChartKeys(periodFor("Năm",month.start),true).size)}
}
