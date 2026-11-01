package vn.chitieu.domain
import org.junit.Assert.*
import org.junit.Test
class PinUpgradeTest {
 private val salt="AAECAwQFBgcICQoLDA0ODw=="
 private val legacy="986qxnMbMmzuHyS3MK9F7gz4EXIuhMjAubUlwLFpHTo="
 @Test fun legacyStoredPinRemainsCompatible(){val input=charArrayOf('1','2','3','4','5','6');assertTrue(PinSecurity.verify(input,salt,legacy));assertEquals(legacy,PinSecurity.hash(input,salt));input.fill('\u0000');assertFalse(PinSecurity.verify(charArrayOf('1','1','1','1','1','1'),salt,legacy))}
 @Test fun throttleEscalatesUsingMonotonicClock(){var state=PinAttempts();repeat(4){state=state.failed(1000,3);assertEquals(0L,state.remaining(1000,3))};state=state.failed(1000,3);assertEquals(30000L,state.remaining(1000,3));assertEquals(15000L,state.remaining(16000,3));state=state.failed(31000,3);assertEquals(60000L,state.remaining(31000,3));state=state.failed(91000,3);assertEquals(300000L,state.remaining(91000,3))}
 @Test fun rebootRestartsPenalty(){val state=PinAttempts(6,200000,2).afterBoot(100,3);assertEquals(60000L,state.remaining(100,3))}
 @Test fun bankRoundTripWithinTimeoutStaysUnlocked(){val session=LockSession();session.unlock();session.stop(1000);session.resume(30000,60000);assertTrue(session.unlocked);session.stop(31000);session.resume(92000,60000);assertFalse(session.unlocked);assertFalse(LockSession().unlocked)}
}
