package vn.chitieu.ui
import androidx.activity.compose.setContent
import androidx.room.withTransaction
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.runner.RunWith
import vn.chitieu.MainActivity
import vn.chitieu.data.*
import vn.chitieu.domain.*
import vn.chitieu.ui.theme.*
@RunWith(AndroidJUnit4::class) class SecurityStatisticsUiTest {
 private lateinit var old:Settings
 @get:Rule(order=0) val prepare=object:org.junit.rules.ExternalResource(){override fun before(){val context=InstrumentationRegistry.getInstrumentation().targetContext;runBlocking {val p=Preferences(context);old=p.settings.first();p.pin("","");p.flag("onboarded",true);p.flag("biometric",false)};context.getSharedPreferences("pin_attempts",0).edit().clear().commit();val automation=InstrumentationRegistry.getInstrumentation().uiAutomation;android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("cmd statusbar collapse")).bufferedReader().use {it.readText()};android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("am start -W -n vn.chitieu.debug/vn.chitieu.MainActivity")).bufferedReader().use {it.readText()}};override fun after(){runBlocking{val p=Preferences(InstrumentationRegistry.getInstrumentation().targetContext);p.pin(old.pinSalt,old.pinHash);p.flag("biometric",old.biometric)}}}
 @get:Rule(order=1) val compose=createAndroidComposeRule<MainActivity>()
 private fun vm():LedgerViewModel {lateinit var vm:LedgerViewModel;compose.activityRule.scenario.onActivity {vm=ViewModelProvider(it)[LedgerViewModel::class.java];it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)};return vm}
 private fun digits(value:String){compose.waitUntil(10000){compose.onAllNodes(hasTestTag("pin-1") and isEnabled()).fetchSemanticsNodes().isNotEmpty()};value.forEach{compose.onNodeWithTag("pin-$it").performClick()}}
 private fun ready(text:String){println("UI wait: $text");try{compose.waitUntil(20000){compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()}}catch(failure:Throwable){compose.onRoot(useUnmergedTree=true).printToLog("ChiTieuUITest");throw failure}}
 private fun shot(name:String){compose.waitForIdle();Thread.sleep(300);compose.waitForIdle();val instrumentation=InstrumentationRegistry.getInstrumentation();val dir=java.io.File(instrumentation.targetContext.filesDir,"security-gallery").apply{mkdirs()};val focus=android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand("dumpsys window")).bufferedReader().use {it.readText()};check(Regex("mCurrentFocus=.*vn\\.chitieu\\.debug").containsMatchIn(focus)){"App is not foreground; refusing invalid screenshot"};val image=instrumentation.uiAutomation.takeScreenshot();java.io.File(dir,"$name.png").outputStream().use{image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};image.recycle()}
 @Test fun createMismatchChangeAndDisablePin(){val vm=vm();for(theme in listOf("light","dark")){compose.activityRule.scenario.onActivity{it.setContent{val settings by vm.settings.collectAsStateWithLifecycle();ChiTieuTheme(theme){GlassScene(true){SecuritySettings(vm,settings)}}}};ready("Bật khoá ứng dụng");compose.onNodeWithText("Bật khoá ứng dụng").performClick();shot("pin-create-$theme");digits("123456");compose.onNodeWithText("Tiếp tục").performScrollTo().performClick();ready("Nhập lại để xác nhận");shot("pin-confirm-$theme");digits("000000");compose.onNodeWithText("Tiếp tục").performScrollTo().performClick();ready("Hai mã chưa khớp");digits("123456");compose.onNodeWithText("Tiếp tục").performScrollTo().performClick();digits("123456");compose.onNodeWithText("Tiếp tục").performScrollTo().performClick();ready("Để sau");compose.onNodeWithText("Để sau").performScrollTo().performClick();ready("Đổi mã PIN");shot("pin-enabled-$theme");compose.onNodeWithText("Đổi mã PIN").performClick();digits("000000");compose.onNodeWithText("Tiếp tục").performScrollTo().performClick();ready("Mã PIN chưa đúng");digits("123456");compose.onNodeWithText("Tiếp tục").performScrollTo().performClick();ready("Tạo mã PIN");digits("654321");compose.onNodeWithText("Tiếp tục").performScrollTo().performClick();digits("654321");compose.onNodeWithText("Tiếp tục").performScrollTo().performClick();ready("Để sau");compose.onNodeWithText("Để sau").performClick();compose.onNode(hasContentDescription("Khoá ứng dụng") and hasClickAction()).performClick();ready("Nhập PIN hiện tại");shot("pin-disable-$theme");digits("654321");compose.onNodeWithText("Tắt khoá").performScrollTo().performClick();ready("Bật khoá ứng dụng");Assert.assertTrue(runBlocking{vm.preferences.settings.first().pinHash.isBlank()})}}
 @Test fun fiveFailuresDisableKeypad(){val vm=vm();val settings=Settings(loaded=true,pinSalt="AAECAwQFBgcICQoLDA0ODw==",pinHash="986qxnMbMmzuHyS3MK9F7gz4EXIuhMjAubUlwLFpHTo=");compose.activityRule.scenario.onActivity{it.setContent{ChiTieuTheme("dark"){GlassScene(true){LockScreen(settings,vm){Assert.fail("Wrong PIN unlocked")}}}}};ready("Nhập mã PIN");repeat(5){digits("000000");compose.onNodeWithText("Mở khoá").performScrollTo().performClick();compose.waitUntil(20000){compose.onAllNodesWithText("Mở khoá").fetchSemanticsNodes().isNotEmpty()};compose.waitForIdle()};compose.onNodeWithText("Thử lại sau",substring=true).assertExists();compose.onNodeWithTag("pin-1").assertIsNotEnabled();shot("pin-throttled")}
 @Test fun pendingSheetRemainsOpenWhenAnotherPendingArrives(){val vm=vm();val first="test-pending-current-review";val second="test-pending-new-review";runBlocking{vm.repo.save(Entry(id=first,amount=123456,status=Status.PENDING,recipient="[SIMULATED] CURRENT",createdAt=System.currentTimeMillis()+10000))};try{compose.activityRule.scenario.recreate();ready("Bạn đã chuyển khoản chưa?");runBlocking{vm.repo.save(Entry(id=second,amount=654321,status=Status.PENDING,createdAt=System.currentTimeMillis()+20000))};compose.waitForIdle();compose.onNodeWithTag("pending-confirm").assertExists().performClick();compose.waitUntil(10000){runBlocking{vm.repo.dao.get(first)?.status==Status.CONFIRMED}};Assert.assertEquals(Status.PENDING,runBlocking{vm.repo.dao.get(second)?.status})}finally{runBlocking{vm.repo.dao.delete(first);vm.repo.dao.delete(second)}}}
 @Test fun pendingTransferSurvivesBackgroundLock(){val vm=vm();val id="test-pending-lock";runBlocking{vm.repo.save(Entry(id=id,amount=100000,status=Status.PENDING,recipient="[SIMULATED] BANK"));vm.preferences.pin("AAECAwQFBgcICQoLDA0ODw==","986qxnMbMmzuHyS3MK9F7gz4EXIuhMjAubUlwLFpHTo=");vm.preferences.number("autoLockMinutes",0)};try{compose.activityRule.scenario.recreate();ready("Nhập mã PIN");digits("123456");compose.onNodeWithText("Mở khoá").performScrollTo().performClick();ready("Bạn đã chuyển khoản chưa?");compose.onNodeWithText("Hỏi lại sau").performClick();compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED);compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);ready("Nhập mã PIN");digits("123456");compose.onNodeWithText("Mở khoá").performScrollTo().performClick();ready("Bạn đã chuyển khoản chưa?");compose.onNodeWithTag("pending-confirm").performClick();compose.waitUntil(10000){runBlocking{vm.repo.dao.get(id)?.status==Status.CONFIRMED}}}finally{runBlocking{vm.repo.dao.delete(id);vm.preferences.pin("","");vm.preferences.number("autoLockMinutes",old.autoLockMinutes)}}}
 @Test fun galleryAndStatisticsSegments(){val vm=vm();val context=InstrumentationRegistry.getInstrumentation().targetContext;val dir=java.io.File(context.filesDir,"security-gallery").apply{mkdirs()};for(theme in listOf("light","dark")){
  for(page in listOf("settings","security","unlock","statistics")){
   if(page=="statistics")StatisticsInbox.month=java.time.YearMonth.of(1950,1)
   compose.activityRule.scenario.onActivity{activity->activity.setContent{val settings by vm.settings.collectAsStateWithLifecycle();ChiTieuTheme(theme){GlassScene(true){Box(Modifier.fillMaxSize().safeDrawingPadding()){when(page){"settings"->SettingsScreen(vm);"security"->SecuritySettings(vm,settings);"unlock"->LockScreen(settings.copy(pinHash="synthetic",biometric=false),vm){};else->StatisticsScreen(vm)}}}}}}
   compose.waitForIdle();Thread.sleep(600);shot("$page-$theme"); if(page=="settings")for(title in listOf("Kiểm tra thiết lập","Ngân hàng theo dõi","Cách thêm giao dịch","Đọc ảnh biên lai","Quản lý ví","Danh mục","Quy tắc tự phân loại","Chủ đề","Sao lưu & khôi phục","Xuất CSV","Nhập CSV","Nâng cao","Quyền riêng tư & bảo mật","Giấy phép mã nguồn mở")){
    compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(title));compose.onNode(hasText(title) and hasClickAction()).performClick();compose.waitForIdle();shot("settings-${title.replace('/','-')}-$theme");compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Quay lại"));compose.onNodeWithText("Quay lại").performClick()
   }
  }
 }
 val now=java.time.LocalDate.now(VN_ZONE);runBlocking{repeat(180){i->vm.repo.save(Entry(id="test-gallery-$i",amount=if(i==0)5_000_000_000 else (i+1)*17000L,createdAt=now.minusDays(i.toLong()).atTime(12,0).atZone(VN_ZONE).toInstant().toEpochMilli(),kind=if(i%3==0)Kind.INCOME else Kind.EXPENSE,category=Defaults.categories[i%Defaults.categories.size],note="[SIMULATED]"))}}
 try{for(theme in listOf("light","dark")){StatisticsInbox.month=java.time.YearMonth.from(now);compose.activityRule.scenario.onActivity{it.setContent{ChiTieuTheme(theme){GlassScene(true){StatisticsScreen(vm)}}}};ready("Thống kê");for(period in listOf("Tuần","Tháng","Năm")){compose.onNodeWithText(period).performClick();for(kind in listOf("Chi","Thu","Cả hai")){compose.onNodeWithText(kind).performClick();compose.waitForIdle();Thread.sleep(500);shot("statistics-$period-$kind-$theme")}}}}finally{runBlocking{vm.repo.db.withTransaction {repeat(180){vm.repo.dao.delete("test-gallery-$it")}}}}
 }
 @Test fun pinSetupUsesFullScreenWithEnlargedText(){
  val vm=vm()
  compose.activityRule.scenario.onActivity{activity->activity.setContent{
   val base=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(base.density,1.3f)){
    ChiTieuTheme("light"){GlassScene(true){Box(Modifier.width(375.dp).height(812.dp)){AppRoot(vm)}}}
   }
  };NavigationInbox.screen.value="settings"}
  ready("Khoá ứng dụng");compose.onNodeWithText("Khoá ứng dụng").performClick()
  ready("Bật khoá ứng dụng");compose.onNodeWithText("Bật khoá ứng dụng").performScrollTo().performClick()
  ready("Tạo mã PIN");compose.onAllNodesWithText("Tổng quan").assertCountEquals(0)
  digits("123456");compose.onNodeWithText("Tiếp tục").performScrollTo().performClick();ready("Nhập lại để xác nhận")
 }

 private class FakeBiometric(private val supported:Boolean=true):BiometricGateway {
  override var inProgress=false
  var attempts=0
  private var result:((BiometricOutcome)->Unit)?=null
  override fun available()=supported
  override fun bind(result:(BiometricOutcome)->Unit):()->Unit {this.result=result;return {if(this.result===result)this.result=null}}
  override fun authenticate(){if(!inProgress){attempts++;inProgress=true}}
  override fun cancel(){inProgress=false}
  fun finish(outcome:BiometricOutcome){inProgress=false;result?.invoke(outcome)}
 }
 private fun biometricLock(fake:FakeBiometric,onUnlock:()->Unit={}){
  val vm=vm();val settings=Settings(loaded=true,biometric=true,pinSalt="AAECAwQFBgcICQoLDA0ODw==",pinHash="986qxnMbMmzuHyS3MK9F7gz4EXIuhMjAubUlwLFpHTo=")
  compose.activityRule.scenario.onActivity{it.setContent{CompositionLocalProvider(LocalBiometricGateway provides fake){ChiTieuTheme("light"){GlassScene(true){LockScreen(settings,vm,onUnlock)}}}}}
 }
 @Test fun biometricsAreFirstAndPinCanRetryBiometrics(){
  val fake=FakeBiometric();var unlocked=false;biometricLock(fake){unlocked=true}
  compose.waitUntil(10000){fake.attempts==1};compose.onNodeWithTag("biometric-wait").assertExists();compose.onNodeWithTag("pin-1").assertDoesNotExist()
  compose.onNodeWithText("Dùng PIN").performClick();ready("Nhập mã PIN")
  compose.onNodeWithTag("pin-bio").performClick();compose.waitUntil(10000){fake.attempts==2}
  compose.runOnIdle {fake.finish(BiometricOutcome.SUCCESS)};compose.waitUntil(10000){unlocked}
 }
 @Test fun biometricCancellationFallsBackWithoutPromptLoop(){
  val fake=FakeBiometric();biometricLock(fake);compose.waitUntil(10000){fake.attempts==1}
  compose.runOnIdle {fake.finish(BiometricOutcome.USE_PIN)};ready("Nhập mã PIN")
  compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED);compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
  compose.waitForIdle();Assert.assertEquals(1,fake.attempts);digits("123456")
  compose.onNodeWithText("Mở khoá").performScrollTo().assertIsEnabled()
 }
 @Test fun absentBiometricsImmediatelyOfferPin(){
  val fake=FakeBiometric(false);biometricLock(fake);ready("Nhập mã PIN");compose.onNodeWithTag("pin-bio").assertDoesNotExist();Assert.assertEquals(0,fake.attempts)
 }
 @Test fun biometricLockoutOffersWorkingPin(){
  val fake=FakeBiometric();var unlocked=false;biometricLock(fake){unlocked=true};compose.waitUntil(10000){fake.attempts==1}
  compose.runOnIdle {fake.finish(BiometricOutcome.LOCKED_OUT)};ready("Nhập mã PIN");digits("123456");compose.onNodeWithText("Mở khoá").performScrollTo().performClick();compose.waitUntil(10000){unlocked}
 }
 @Test fun pinCooldownPreventsBiometricBypass(){
  val vm=vm();val settings=Settings(loaded=true,biometric=true,pinSalt="AAECAwQFBgcICQoLDA0ODw==",pinHash="986qxnMbMmzuHyS3MK9F7gz4EXIuhMjAubUlwLFpHTo=")
  runBlocking {repeat(5){vm.pinGate.verify("000000".toCharArray(),settings)}}
  val fake=FakeBiometric();biometricLock(fake){Assert.fail("Cooldown bypassed")};ready("Nhập mã PIN")
  compose.onNodeWithTag("pin-1").assertIsNotEnabled();Assert.assertEquals(0,fake.attempts)
 }
}
