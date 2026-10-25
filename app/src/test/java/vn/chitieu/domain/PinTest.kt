package vn.chitieu.domain
import org.junit.Assert.*
import org.junit.Test
class PinTest {
 @Test fun pinMatchesOnlyCorrectInput() {val salt=PinSecurity.salt();val hash=PinSecurity.hash("123456",salt);assertTrue(PinSecurity.verify("123456",salt,hash));assertFalse(PinSecurity.verify("123457",salt,hash));assertFalse(PinSecurity.verify("abc",salt,hash))}
 @Test fun randomSaltsSeparateHashes() {val a=PinSecurity.salt();val b=PinSecurity.salt();assertNotEquals(a,b);assertNotEquals(PinSecurity.hash("123456",a),PinSecurity.hash("123456",b))}
 @Test(expected=IllegalArgumentException::class) fun shortPinRejected() {PinSecurity.hash("123",PinSecurity.salt())}
}
