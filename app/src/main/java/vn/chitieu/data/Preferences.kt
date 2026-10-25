package vn.chitieu.data
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
private val Context.preferences by preferencesDataStore("settings")
data class Settings(val dark:Boolean=false,val onboarded:Boolean=false,val autoAdd:Boolean=false,val rawLog:Boolean=false,val sms:Boolean=false,val enabled:Set<String> = emptySet(),val defaultBank:String="",val pinSalt:String="",val pinHash:String="",val biometric:Boolean=false,val loaded:Boolean=false)
@Singleton class Preferences @Inject constructor(@ApplicationContext context:Context) {
 private val store=context.preferences
 val settings=store.data.map {p->Settings(p[booleanPreferencesKey("dark")]?:false,p[booleanPreferencesKey("onboarded")]?:false,p[booleanPreferencesKey("autoAdd")]?:false,p[booleanPreferencesKey("rawLog")]?:false,p[booleanPreferencesKey("sms")]?:false,p[stringSetPreferencesKey("enabled")]?:emptySet(),p[stringPreferencesKey("defaultBank")]?:"",p[stringPreferencesKey("pinSalt")]?:"",p[stringPreferencesKey("pinHash")]?:"",p[booleanPreferencesKey("biometric")]?:false,true)}
 suspend fun flag(name:String,value:Boolean) {store.edit {it[booleanPreferencesKey(name)]=value}}
 suspend fun text(name:String,value:String) {store.edit {it[stringPreferencesKey(name)]=value}}
 suspend fun enable(packageName:String,enabled:Boolean) {store.edit {val key=stringSetPreferencesKey("enabled");val values=(it[key]?:emptySet()).toMutableSet();if(enabled)values.add(packageName) else values.remove(packageName);it[key]=values}}
 suspend fun pin(salt:String,hash:String) {store.edit {it[stringPreferencesKey("pinSalt")]=salt;it[stringPreferencesKey("pinHash")]=hash}}
}
