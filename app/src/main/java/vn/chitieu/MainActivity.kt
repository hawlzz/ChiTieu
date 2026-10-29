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
 override fun onCreate(savedInstanceState:Bundle?) {val splash=installSplashScreen();super.onCreate(savedInstanceState);splash.setOnExitAnimationListener {view->view.view.animate().alpha(0f).setDuration(if(android.provider.Settings.Global.getFloat(contentResolver,android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,1f)==0f)0 else 180).withEndAction {view.remove()}.start()};if(!BuildConfig.DEBUG)window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);enableEdgeToEdge();receive(intent);setContent {
  val vm:LedgerViewModel=hiltViewModel();val settings by vm.settings.collectAsStateWithLifecycle()
  val dark=when(settings.themeMode){"dark"->true;"light"->false;else->androidx.compose.foundation.isSystemInDarkTheme()}
  SideEffect {androidx.core.view.WindowCompat.getInsetsController(window,window.decorView).apply {isAppearanceLightStatusBars=!dark;isAppearanceLightNavigationBars=!dark}}
  val screenState=androidx.compose.runtime.saveable.rememberSaveableStateHolder()
  var unlocked by remember {mutableStateOf(false)}
  androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_STOP){unlocked=false}
  ChiTieuTheme(settings.themeMode) {GlassScene(settings.reduceEffects) {if(!settings.loaded) CircularProgressIndicator() else if(settings.pinHash.isNotBlank()&&!unlocked) LockScreen(settings){unlocked=true} else screenState.SaveableStateProvider("app"){AppRoot(vm)}}}
 }}
 override fun onNewIntent(intent:android.content.Intent){super.onNewIntent(intent);setIntent(intent);receive(intent)}
 private fun receive(intent:android.content.Intent){if(intent.action==android.content.Intent.ACTION_SEND&&intent.type?.startsWith("image/")==true){val uri=intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM,android.net.Uri::class.java);if(uri?.scheme=="content")vn.chitieu.receipt.ReceiptInbox.incoming.value=uri};if(BuildConfig.DEBUG)intent.getStringExtra("test_screen")?.takeIf {it in listOf("ledger","transactions","scan","settings","advanced","receipt","welcome") }?.let {NavigationInbox.screen.value=it}}
}
