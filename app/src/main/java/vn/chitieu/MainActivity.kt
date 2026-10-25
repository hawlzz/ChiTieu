package vn.chitieu
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import vn.chitieu.ui.*
@AndroidEntryPoint class MainActivity:FragmentActivity() {
 override fun onCreate(savedInstanceState:Bundle?) {super.onCreate(savedInstanceState);window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);enableEdgeToEdge();setContent {
  val vm:LedgerViewModel=hiltViewModel();val settings by vm.settings.collectAsStateWithLifecycle()
  val screenState=androidx.compose.runtime.saveable.rememberSaveableStateHolder()
  var unlocked by remember {mutableStateOf(false)}
  androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_STOP){unlocked=false}
  MaterialTheme(colorScheme=if(settings.dark)darkColorScheme() else lightColorScheme()) {Surface {if(!settings.loaded) CircularProgressIndicator() else if(settings.pinHash.isNotBlank()&&!unlocked) LockScreen(settings){unlocked=true} else screenState.SaveableStateProvider("app"){AppRoot(vm)}}}
 }}
}



