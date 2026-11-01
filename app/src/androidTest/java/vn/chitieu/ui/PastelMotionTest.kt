package vn.chitieu.ui
import android.os.ParcelFileDescriptor
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith
import vn.chitieu.MainActivity
import vn.chitieu.ui.theme.*
@RunWith(AndroidJUnit4::class) class PastelMotionTest {
 @get:Rule val compose=createAndroidComposeRule<MainActivity>()
 private fun shell(command:String)=ParcelFileDescriptor.AutoCloseInputStream(InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)).bufferedReader().use {it.readText().trim()}
 @Test fun systemReducedMotionUpdatesWhileScreenIsOpen(){
  val previous=shell("settings get global animator_duration_scale")
  try {
   shell("settings put global animator_duration_scale 1")
   compose.activityRule.scenario.onActivity {it.setContent{ChiTieuTheme("light"){GlassScene(false){Text(if(LocalReduceMotion.current)"motion reduced" else "motion enabled")}}}}
   compose.waitUntil(10000){compose.onAllNodesWithText("motion enabled").fetchSemanticsNodes().isNotEmpty()}
   shell("settings put global animator_duration_scale 0")
   compose.waitUntil(10000){compose.onAllNodesWithText("motion reduced").fetchSemanticsNodes().isNotEmpty()}
  }finally{shell(if(previous=="null")"settings delete global animator_duration_scale" else "settings put global animator_duration_scale $previous")}
 }
}
