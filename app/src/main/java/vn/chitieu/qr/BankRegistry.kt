package vn.chitieu.qr
import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
@Serializable data class Bank(val name:String,val shortName:String,val code:String,val bin:String?=null,val packageName:String?=null,val appId:String?=null,val verification:String="",val aliases:List<String> = emptyList())
@Singleton class BankRegistry @Inject constructor(@ApplicationContext context:Context) {
 val banks:List<Bank> = Json {ignoreUnknownKeys=true}.decodeFromString(context.assets.open("banks.json").bufferedReader().use {it.readText()})
 fun byBin(bin:String):Bank?=banks.firstOrNull {it.bin==bin}
 fun byPackage(pkg:String):Bank?=banks.firstOrNull {it.packageName==pkg}
 fun installed(context:Context):List<Bank> = banks.filter {bank->bank.packageName?.let {pkg->runCatching {context.packageManager.getPackageInfo(pkg,0);true}.getOrDefault(false)}?:false}
}
