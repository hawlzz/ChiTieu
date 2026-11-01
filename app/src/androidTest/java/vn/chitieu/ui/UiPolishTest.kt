package vn.chitieu.ui

import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import vn.chitieu.MainActivity
import vn.chitieu.domain.Money
import vn.chitieu.ui.theme.*

@RunWith(AndroidJUnit4::class)
class UiPolishTest {
 @get:Rule val compose=createAndroidComposeRule<MainActivity>()

 @Test fun tabSelectionIsExposedAndTargetsStayLarge() {
  compose.activityRule.scenario.onActivity {activity->activity.setContent {
   ChiTieuTheme("light"){GlassScene(true){var selected by remember {mutableIntStateOf(0)};GlassBottomBar(selected){selected=it}}}
  }}
  compose.onNode(hasText("Tổng quan") and isSelected()).assertExists()
  compose.onNodeWithText("Thống kê").performClick()
  compose.onNode(hasText("Thống kê") and isSelected()).assertExists()
  val density=compose.activity.resources.displayMetrics.density
  val bounds=compose.onNodeWithText("Thống kê").fetchSemanticsNode().boundsInRoot
  assertTrue("Tab target must be >=48dp",bounds.height>=48*density&&bounds.width>=48*density)
 }

 @Test fun largeCurrencyRemainsReadableWithEnlargedText() {
  for(theme in listOf("light","dark")) {
   compose.activityRule.scenario.onActivity {activity->activity.setContent {
    val base=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(base.density,1.3f)) {
     ChiTieuTheme(theme){GlassScene(true){Box(Modifier.width(360.dp).background(MaterialTheme.colorScheme.background).padding(20.dp)){AmountText(Money.MAX,Modifier.width(156.dp),large=false)}}}
    }
   }}
   val layouts=mutableListOf<TextLayoutResult>()
   compose.onNodeWithText(Money.format(Money.MAX)).performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(layouts)}
   assertTrue(layouts.isNotEmpty())
   assertFalse("The full monetary value must fit",layouts.single().hasVisualOverflow)
  }
 }

 @Test fun pinKeysAreCircularAndAbsentBiometricIsNotFocusable() {
  compose.activityRule.scenario.onActivity {activity->activity.setContent {ChiTieuTheme("dark"){GlassScene(true){PinKeypad(onDigit={},onDelete={})}}}}
  compose.onNodeWithTag("pin-bio").assertDoesNotExist()
  val bounds=compose.onNodeWithTag("pin-1").fetchSemanticsNode().boundsInRoot
  val density=compose.activity.resources.displayMetrics.density
  assertTrue(bounds.width>=48*density)
  assertEquals(bounds.width,bounds.height,1f)
 }
 @Test fun negativeHeroKeepsCurrencyOnTheSameLine() {
  compose.activityRule.scenario.onActivity {activity->activity.setContent {
   val base=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(base.density,1.3f)) {
    ChiTieuTheme("light"){GlassScene(true){AmountText(-256_785_000L,Modifier.width(296.dp))}}
   }
  }}
  val layouts=mutableListOf<TextLayoutResult>()
  compose.onNodeWithText(Money.format(-256_785_000L)).performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(layouts)}
  assertFalse("font=${layouts.single().layoutInput.style.fontSize}, lines=${layouts.single().lineCount}, size=${layouts.single().size}, constraints=${layouts.single().layoutInput.constraints}, widthOverflow=${layouts.single().didOverflowWidth}, heightOverflow=${layouts.single().didOverflowHeight}",layouts.single().hasVisualOverflow)
  assertEquals("Currency must not be orphaned",1,layouts.single().lineCount)
 }
}
