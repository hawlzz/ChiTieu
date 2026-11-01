package vn.chitieu
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import vn.chitieu.ui.*
import vn.chitieu.ui.theme.*
@AndroidEntryPoint class MainActivity:FragmentActivity() {
 private lateinit var biometricGateway: AndroidBiometricGateway
 private var unlocked by mutableStateOf(false)
 private val lockSession=vn.chitieu.domain.LockSession()
 private var timeoutMillis=60_000L
 override fun onStop(){lockSession.stop(android.os.SystemClock.elapsedRealtime());super.onStop()}
 override fun onStart(){super.onStart();lockSession.resume(android.os.SystemClock.elapsedRealtime(),timeoutMillis);unlocked=lockSession.unlocked}
 override fun onCreate(savedInstanceState:Bundle?) {val splash=installSplashScreen();super.onCreate(savedInstanceState);biometricGateway=AndroidBiometricGateway(this);splash.setOnExitAnimationListener {view->view.view.animate().alpha(0f).setDuration(if(android.provider.Settings.Global.getFloat(contentResolver,android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,1f)==0f)0 else 180).withEndAction {view.remove()}.start()};enableEdgeToEdge();receive(intent);setContent {
  val vm:LedgerViewModel=hiltViewModel();val settings by vm.settings.collectAsStateWithLifecycle()
  val dark=when(settings.themeMode){"dark"->true;"light"->false;else->androidx.compose.foundation.isSystemInDarkTheme()}
  SideEffect {androidx.core.view.WindowCompat.getInsetsController(window,window.decorView).apply {isAppearanceLightStatusBars=!dark;isAppearanceLightNavigationBars=!dark}}
  val screenState=androidx.compose.runtime.saveable.rememberSaveableStateHolder()
  SideEffect {timeoutMillis=settings.autoLockMinutes*60_000L;if(settings.loaded&&settings.pinHash.isBlank()){lockSession.unlock();unlocked=true};setRecentsScreenshotEnabled(!settings.hideRecents);if(settings.blockScreenshots&&!BuildConfig.DEBUG)window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE) else window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)}

  CompositionLocalProvider(LocalBiometricGateway provides biometricGateway) {ChiTieuTheme(settings.themeMode) {GlassScene(settings.reduceEffects) {if(!settings.loaded) CircularProgressIndicator() else if(settings.pinHash.isNotBlank()&&!unlocked) LockScreen(settings,vm){lockSession.unlock();unlocked=true} else screenState.SaveableStateProvider("app"){AppRoot(vm)}}}}
 }}
 override fun onNewIntent(intent:android.content.Intent){super.onNewIntent(intent);setIntent(intent);receive(intent)}
 private fun receive(intent:android.content.Intent){if(intent.action==android.content.Intent.ACTION_SEND&&intent.type?.startsWith("image/")==true){val uri=intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM,android.net.Uri::class.java);if(uri?.scheme=="content")vn.chitieu.receipt.ReceiptInbox.incoming.value=uri};if(BuildConfig.DEBUG)intent.getStringExtra("test_screen")?.takeIf {it in listOf("ledger","transactions","scan","settings","advanced","receipt","welcome","statistics") }?.let {NavigationInbox.screen.value=it}}
}
