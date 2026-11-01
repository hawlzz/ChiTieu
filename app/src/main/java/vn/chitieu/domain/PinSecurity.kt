package vn.chitieu.domain
import java.security.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64
object PinSecurity {
 fun salt():String=ByteArray(16).also {SecureRandom().nextBytes(it)}.let {Base64.getEncoder().encodeToString(it)}
 fun hash(pin:CharArray,salt:String):String {require(pin.size in 6..12&&pin.all {it in '0'..'9'});val spec=PBEKeySpec(pin,Base64.getDecoder().decode(salt),210_000,256);return try {Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded)}finally {spec.clearPassword()}}
 fun verify(pin:CharArray,salt:String,expected:String):Boolean=runCatching {MessageDigest.isEqual(Base64.getDecoder().decode(hash(pin,salt)),Base64.getDecoder().decode(expected))}.getOrDefault(false)
 // Kept for existing integrations; new PIN entry uses erasable character buffers.
 fun hash(pin:String,salt:String):String=pin.toCharArray().let {try {hash(it,salt)}finally {it.fill('\u0000')}}
 fun verify(pin:String,salt:String,expected:String):Boolean=pin.toCharArray().let {try {verify(it,salt,expected)}finally {it.fill('\u0000')}}
}
data class PinAttempts(val failures:Int=0,val until:Long=0,val boot:Int=-1) {
 fun remaining(now:Long,currentBoot:Int):Long=if(boot==currentBoot)(until-now).coerceAtLeast(0) else 0
 fun failed(now:Long,currentBoot:Int):PinAttempts {val count=failures+1;val wait=when {count<5->0L;count==5->30_000L;count==6->60_000L;else->300_000L};return PinAttempts(count,now+wait,currentBoot)}
 // On reboot elapsedRealtime restarts: restart the current penalty, never compare wall time.
 fun afterBoot(now:Long,currentBoot:Int):PinAttempts=if(boot!=currentBoot&&failures>=5)copy(until=now+when(failures){5->30_000L;6->60_000L;else->300_000L},boot=currentBoot) else this
}
class LockSession {
 var unlocked=false;private set
 private var backgroundAt:Long?=null
 fun unlock(){unlocked=true}
 fun stop(now:Long){backgroundAt=now}
 fun resume(now:Long,timeout:Long){backgroundAt?.let {if(now-it>=timeout)unlocked=false};backgroundAt=null}
}
