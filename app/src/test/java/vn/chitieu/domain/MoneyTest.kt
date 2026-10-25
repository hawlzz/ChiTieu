package vn.chitieu.domain
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
class MoneyTest {
 @Test fun boundaries() {assertNull(Money.input("0"));assertNull(Money.input("9999999999999"));assertEquals(1250000L,Money.input("1.250.000"));assertEquals("1.250.000 ₫",Money.format(1250000))}
 @Test fun vnMonthBoundary() {assertEquals("2026-11",monthOf(Instant.parse("2026-10-31T17:00:00Z").toEpochMilli()).toString())}
 @Test(expected=IllegalArgumentException::class) fun negativeRejected() {Money.validate(-1)}
}
