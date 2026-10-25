package vn.chitieu.data
import android.content.Context
import androidx.work.*
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.TimeUnit
@EntryPoint @InstallIn(SingletonComponent::class) interface WorkerDependencies {fun repository():Repository}
class PendingWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params) {
 override suspend fun doWork():Result=try {EntryPointAccessors.fromApplication(applicationContext,WorkerDependencies::class.java).repository().expire();Result.success()}catch(_:Exception){Result.retry()}
 companion object {fun schedule(context:Context) {WorkManager.getInstance(context).enqueueUniquePeriodicWork("pending-review",ExistingPeriodicWorkPolicy.KEEP,PeriodicWorkRequestBuilder<PendingWorker>(12,TimeUnit.HOURS).build())}}
}
