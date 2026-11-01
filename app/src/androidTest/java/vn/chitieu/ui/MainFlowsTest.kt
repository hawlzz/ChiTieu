package vn.chitieu.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import vn.chitieu.MainActivity

/** Synthetic UI checks: no bank accounts, images or real ledger data. */
@RunWith(AndroidJUnit4::class)
class MainFlowsTest {
 private lateinit var previous:vn.chitieu.data.Settings
 @get:Rule(order=0) val foreground = object:org.junit.rules.ExternalResource() {
  override fun before() {
   kotlinx.coroutines.runBlocking {val preferences=vn.chitieu.data.Preferences(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext);previous=preferences.settings.first();preferences.pin("","");preferences.flag("onboarded",true)}
   val automation=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation
   android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("am start -W -n vn.chitieu.debug/vn.chitieu.MainActivity")).bufferedReader().use {it.readText()}
  }
  override fun after(){kotlinx.coroutines.runBlocking {val preferences=vn.chitieu.data.Preferences(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext);preferences.pin(previous.pinSalt,previous.pinHash);preferences.flag("onboarded",previous.onboarded)}}
 }
 @get:Rule(order=1) val compose = createAndroidComposeRule<MainActivity>()
 private fun home() {
  compose.activityRule.scenario.onActivity { it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);NavigationInbox.screen.value="ledger" }
  lateinit var vm:LedgerViewModel;compose.activityRule.scenario.onActivity {vm=androidx.lifecycle.ViewModelProvider(it)[LedgerViewModel::class.java]}
  val hasPending=kotlinx.coroutines.runBlocking {vm.repo.expire();vm.repo.dao.pendingOnce()!=null}
  if(hasPending){compose.waitUntil(20000){compose.onAllNodesWithText("Hỏi lại sau").fetchSemanticsNodes().isNotEmpty()};compose.onNodeWithText("Hỏi lại sau").performClick()}
  try{compose.waitUntil(15000) { compose.onAllNodesWithText("Quét QR").fetchSemanticsNodes().isNotEmpty() }}catch(failure:Throwable){compose.onRoot(useUnmergedTree=true).printToLog("ChiTieuUITest");throw failure}
 }
 @Test fun homeHasThreePrimaryActions() {
  home()
  compose.onNodeWithText("Quét QR").assertIsDisplayed()
  compose.onNodeWithText("Thêm nhanh").assertIsDisplayed()
  compose.onNodeWithContentDescription("Đọc ảnh biên lai").assertIsDisplayed()
 }
 @Test fun quickAddRequiresPositiveAmount() {
  home()
  compose.onNodeWithText("Thêm nhanh").performClick()
  compose.onNodeWithText("Lưu").assertIsNotEnabled()
  compose.onNodeWithText("0",useUnmergedTree=true).performClick()
  compose.onNodeWithText("Lưu").assertIsNotEnabled()
  compose.onNodeWithText("1",useUnmergedTree=true).performClick()
  compose.onNodeWithText("000",useUnmergedTree=true).performClick()
  compose.onNodeWithText("0",useUnmergedTree=true).performClick()
  compose.onNodeWithText("Lưu").assertIsEnabled()
  // Close without saving so the test leaves no transactions behind.
  compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
 }
 @Test fun settingsExposesAdvancedPage() {
  home()
  compose.onNodeWithText("Cài đặt").performClick()
  compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Nâng cao"))
  compose.onNodeWithText("Nâng cao").performClick()
  compose.onNodeWithText("Chặn chụp màn hình", substring=true).assertExists()
 }

 @Test fun welcomeCanBeSkippedWithoutPermissions() {
  home()
  compose.activityRule.scenario.onActivity {NavigationInbox.screen.value="welcome"}
  compose.waitUntil(15000){compose.onAllNodesWithText("Bỏ qua").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithText("Chào mừng đến ChiTieu").assertIsDisplayed()
  compose.onNodeWithText("Bỏ qua").performClick()
  compose.onNodeWithText("Quét QR").assertExists()
 }
 @Test fun monthPickerCanSelectMonth() {
  home()
  compose.onNodeWithText("Tháng ",substring=true).performClick()
  compose.onNodeWithText("Tháng 1").performClick()
  compose.onNodeWithText("Tháng 1, ",substring=true).assertExists()
 }
 @Test fun transactionsHaveKindSegments() {
  home()
  compose.onNodeWithText("Giao dịch").performClick()
  compose.onNodeWithText("Chi").performClick()
  compose.onNodeWithText("Thu").assertExists()
 }

 @Test fun transactionDetailsRetainReceiptMetadata(){
  home()
  lateinit var vm:LedgerViewModel
  compose.activityRule.scenario.onActivity {vm=androidx.lifecycle.ViewModelProvider(it)[LedgerViewModel::class.java]}
  val entry=vn.chitieu.data.Entry(id="test-ui-detail",amount=125000,note="[SIMULATED] DETAIL",recipient="0000000000",memo="Noi dung thu nghiem",source=vn.chitieu.domain.Source.RECEIPT)
  kotlinx.coroutines.runBlocking {vm.repo.save(entry);vm.repo.dao.evidence(vn.chitieu.data.ReceiptEvidence(entry.id,"synthetic","DEMO-ONLY-001","NGUOI NHAN THU NGHIEM",entry.createdAt))}
  try {
   compose.onNodeWithText("Giao dịch").performClick()
   compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("[SIMULATED] DETAIL"))
   compose.onNodeWithText("[SIMULATED] DETAIL").performClick()
   compose.onNodeWithText("Chi tiết giao dịch").assertExists()
   compose.onNodeWithText("0000000000").assertExists()
   compose.onNodeWithText("DEMO-ONLY-001").assertExists()
  } finally {kotlinx.coroutines.runBlocking {vm.receipts.delete(entry.id)}}
 }

 private fun freshOnboarding(){
  home()
  kotlinx.coroutines.runBlocking {vn.chitieu.data.Preferences(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext).flag("onboarded",false)}
  compose.activityRule.scenario.recreate()
  compose.waitUntil(15000){compose.onAllNodesWithText("Chào mừng đến ChiTieu").fetchSemanticsNodes().isNotEmpty()}
 }
 @Test fun freshInstallQueuesNavigationUntilOnboardingCompletes(){
  freshOnboarding()
  compose.activityRule.scenario.onActivity {NavigationInbox.screen.value="settings"}
  compose.waitForIdle();compose.onNodeWithText("Chào mừng đến ChiTieu").assertIsDisplayed()
  org.junit.Assert.assertEquals("settings",NavigationInbox.screen.value)
  compose.onNodeWithText("Bỏ qua").performClick()
  compose.waitUntil(15000){compose.onAllNodesWithText("Kiểm tra thiết lập").fetchSemanticsNodes().isNotEmpty()}
 }
 @Test fun firstRunSetupShortcutWaitsForNavigationGraph(){
  freshOnboarding()
  repeat(2){compose.onNodeWithText("Tiếp tục").performClick();compose.waitForIdle()}
  compose.onNodeWithText("Thiết lập thông báo ngân hàng").performClick()
  compose.waitUntil(15000){compose.onAllNodesWithText("Kiểm tra thiết lập").fetchSemanticsNodes().isNotEmpty()}
 }
}
