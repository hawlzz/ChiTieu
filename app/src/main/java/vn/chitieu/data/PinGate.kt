package vn.chitieu.data
import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import vn.chitieu.domain.*
import javax.inject.Inject
import javax.inject.Singleton
@Singleton class PinGate @Inject constructor(@ApplicationContext context:Context,val preferences:Preferences) {
 private val disk=context.getSharedPreferences("pin_attempts",Context.MODE_PRIVATE)
 private val mutex=Mutex()
 private val resolver=context.contentResolver
 private fun boot()=Settings.Global.getInt(resolver,Settings.Global.BOOT_COUNT,0)
 private fun read()=PinAttempts(disk.getInt("failures",0),disk.getLong("until",0),disk.getInt("boot",-1))
 private fun save(value:PinAttempts){check(disk.edit().putInt("failures",value.failures).putLong("until",value.until).putInt("boot",value.boot).commit())}
 suspend fun remaining():Long=withContext(Dispatchers.IO){mutex.withLock {val raw=read();val v=raw.afterBoot(SystemClock.elapsedRealtime(),boot());if(v!=raw)save(v);v.remaining(SystemClock.elapsedRealtime(),boot())}}
 suspend fun verify(pin:CharArray,settings:vn.chitieu.data.Settings):Boolean=withContext(Dispatchers.IO){mutex.withLock {try {val now=SystemClock.elapsedRealtime();val state=read().afterBoot(now,boot());if(state.remaining(now,boot())>0){save(state);false}else {val valid=PinSecurity.verify(pin,settings.pinSalt,settings.pinHash);save(if(valid)PinAttempts() else state.failed(now,boot()));valid}}finally {pin.fill('\u0000')}}}
 suspend fun change(current:CharArray,new:CharArray,settings:vn.chitieu.data.Settings):Boolean {try {if(settings.pinHash.isNotBlank()&&!verify(current,settings))return false;val salt=PinSecurity.salt();val hash=withContext(Dispatchers.Default){PinSecurity.hash(new,salt)};preferences.pin(salt,hash);return true}finally {current.fill('\u0000');new.fill('\u0000')}}
 suspend fun disable(current:CharArray,settings:vn.chitieu.data.Settings):Boolean {if(!verify(current,settings))return false;preferences.pin("","");preferences.flag("biometric",false);return true}
}
