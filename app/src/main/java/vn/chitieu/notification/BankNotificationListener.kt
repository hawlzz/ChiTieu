package vn.chitieu.notification
import android.app.Notification
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
@AndroidEntryPoint class BankNotificationListener:NotificationListenerService() {
 @Inject lateinit var ingestor:MessageIngestor
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
 override fun onListenerConnected() {super.onListenerConnected();ListenerState.connected.value=true}
 override fun onListenerDisconnected() {ListenerState.connected.value=false;runCatching {requestRebind(ComponentName(this,BankNotificationListener::class.java))}}
 override fun onNotificationPosted(sbn:StatusBarNotification?) {
  if(sbn==null||sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY!=0)return
  if(ingestor.registry.byPackage(sbn.packageName)==null&&!vn.chitieu.debug.TestNotifications.accepts(sbn.packageName))return
  val notification=sbn.notification
  val body=(notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?:notification.extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
  val title=notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
  scope.launch {runCatching {ingestor.accept(sbn.packageName,(title+"\n"+body).take(8192),sbn.postTime)}}
 }
 override fun onDestroy(){ListenerState.connected.value=false;scope.cancel();super.onDestroy()}
}
