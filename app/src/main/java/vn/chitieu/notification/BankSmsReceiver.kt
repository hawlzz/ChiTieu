package vn.chitieu.notification
import android.content.*
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import vn.chitieu.data.Preferences
import vn.chitieu.domain.Source
@AndroidEntryPoint class BankSmsReceiver:BroadcastReceiver() {
 @Inject lateinit var ingestor:MessageIngestor
 @Inject lateinit var preferences:Preferences
 // TODO: verify — approved bank brandnames/numbers and SMS templates. Empty allowlist intentionally ignores all SMS until verified.
 private val senders:Map<String,String> = emptyMap()
 override fun onReceive(context:Context,intent:Intent) {
  if(intent.action!=Telephony.Sms.Intents.SMS_RECEIVED_ACTION)return
  val result=goAsync()
  CoroutineScope(Dispatchers.IO).launch {try {if(!preferences.settings.first().sms)return@launch
   val parts=Telephony.Sms.Intents.getMessagesFromIntent(intent);val sender=parts.firstOrNull()?.originatingAddress?:return@launch
   val pkg=senders[sender]?:return@launch
   ingestor.accept(pkg,parts.joinToString(""){it.messageBody.orEmpty()},parts.first().timestampMillis,Source.SMS)
  }finally {result.finish()}}
 }
}
