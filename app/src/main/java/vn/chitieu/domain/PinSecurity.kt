package vn.chitieu.domain
import java.security.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64
object PinSecurity {
 fun salt():String=ByteArray(16).also {SecureRandom().nextBytes(it)}.let {Base64.getEncoder().encodeToString(it)}
 fun hash(pin:String,salt:String):String {require(Regex("[0-9]{6,12}").matches(pin));val spec=PBEKeySpec(pin.toCharArray(),Base64.getDecoder().decode(salt),210_000,256);return try {Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded)}finally {spec.clearPassword()}}
 fun verify(pin:String,salt:String,expected:String):Boolean=runCatching {MessageDigest.isEqual(Base64.getDecoder().decode(hash(pin,salt)),Base64.getDecoder().decode(expected))}.getOrDefault(false)
}
