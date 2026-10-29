package vn.chitieu.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import vn.chitieu.MainActivity

/** Synthetic UI checks: no bank accounts, images or real ledger data. */
@RunWith(AndroidJUnit4::class)
class MainFlowsTest {
 @get:Rule(order=0) val foreground = object:org.junit.rules.ExternalResource() {
  override fun before() {
   val automation=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation
   android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("am start -W -n vn.chitieu.debug/vn.chitieu.MainActivity")).bufferedReader().use {it.readText()}
  }
 }
 @get:Rule(order=1) val compose = createAndroidComposeRule<MainActivity>()
 private fun home() {
  compose.activityRule.scenario.onActivity { it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);NavigationInbox.screen.value="ledger" }
  compose.waitUntil(15000) { compose.onAllNodesWithText("Quét QR").fetchSemanticsNodes().isNotEmpty() }
 }
 @Test fun homeHasThreePrimaryActions() {
  home()
  compose.onNodeWithText("Quét QR").assertIsDisplayed()
  compose.onNodeWithText("Thêm nhanh").assertIsDisplayed()
  compose.onNodeWithText("Đọc ảnh biên lai").assertIsDisplayed()
 }
 @Test fun quickAddRequiresPositiveAmount() {
  home()
  compose.onNodeWithText("Thêm nhanh").performClick()
  compose.onNodeWithText("Lưu").assertIsNotEnabled()
  compose.onNode(hasSetTextAction()).performTextInput("0")
  compose.onNodeWithText("Lưu").assertIsNotEnabled()
  compose.onNode(hasSetTextAction()).performTextClearance()
  compose.onNode(hasSetTextAction()).performTextInput("10000")
  compose.onNodeWithText("Lưu").assertIsEnabled()
  // Close without saving so the test leaves no transactions behind.
  compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
 }
 @Test fun settingsExposesAdvancedPage() {
  home()
  compose.onNodeWithText("Cài đặt").performClick()
  compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Nâng cao"))
  compose.onNodeWithText("Nâng cao").performClick()
  compose.onNodeWithText("Giảm hiệu ứng", substring=true).assertExists()
 }
}





