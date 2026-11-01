package vn.chitieu.ui.theme
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
object SecurityTokens {val key=64.dp;val dot=12.dp;val switchWidth=52.dp;val switchHeight=32.dp;val enabled=Color(0xFF23834B)}
@Composable fun IosSwitch(checked:Boolean,onChange:(Boolean)->Unit,enabled:Boolean=true,label:String="") {
 val position by animateFloatAsState(if(checked)1f else 0f,animationSpec=if(LocalReduceMotion.current)snap() else spring(stiffness=Spring.StiffnessMedium),label="switch")
 val track=if(checked)SecurityTokens.enabled else MaterialTheme.colorScheme.outline
 Box(Modifier.sizeIn(minWidth=GlassTokens.minTouch,minHeight=GlassTokens.minTouch).semantics {contentDescription=label}.toggleable(checked,enabled=enabled,role=Role.Switch,onValueChange=onChange),contentAlignment=Alignment.Center) {
  Box(Modifier.size(SecurityTokens.switchWidth,SecurityTokens.switchHeight).clip(CircleShape).background(track.copy(alpha=if(enabled)1f else .4f))) {
   Box(Modifier.padding(3.dp).offset {IntOffset((20.dp.toPx()*position).roundToInt(),0)}.size(26.dp).clip(CircleShape).background(Color.White))
  }
 }
}

@Composable fun SettingsRow(title:String,detail:String="",icon:ImageVector=AppIcons.ChevronRight,tint:Color=MaterialTheme.colorScheme.primary,enabled:Boolean=true,onClick:(()->Unit)?=null,trailing:(@Composable ()->Unit)?=null) {
 Column {
  Row(Modifier.fillMaxWidth().heightIn(min=72.dp).then(if(onClick!=null)Modifier.clickable(enabled=enabled,onClick=onClick) else Modifier).padding(horizontal=GlassTokens.row,vertical=GlassTokens.gap),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GlassTokens.gap)) {
   FrostedIconBadge(icon,tint=if(enabled)tint else MaterialTheme.colorScheme.onSurfaceVariant)
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
    Text(title,style=MaterialTheme.typography.bodyMedium.copy(fontWeight=FontWeight.Medium),color=if(enabled)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
    if(detail.isNotBlank())SectionFooter(detail)
   }
   if(trailing!=null)trailing() else if(onClick!=null)Icon(AppIcons.ChevronRight,null,tint=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.size(GlassTokens.icon))
  }
  HorizontalDivider(Modifier.padding(start=GlassTokens.row+GlassTokens.badge+GlassTokens.gap),color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.65f))
 }
}

@Composable fun SectionHeader(text:String){Text(text,Modifier.padding(start=GlassTokens.row,top=GlassTokens.gap,bottom=GlassTokens.small).semantics {heading()},style=MaterialTheme.typography.titleSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}

@Composable fun SectionFooter(text:String){Text(text,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
@Composable fun StatusIcon(state:Int){Icon(when(state){1->AppIcons.CheckCircle;0->AppIcons.Warning;else->AppIcons.RadioButtonUnchecked},when(state){1->"Đã hoàn tất";0->"Cần xử lý";else->"Chưa xác minh"},tint=when(state){1->incomeColor();0->MaterialTheme.colorScheme.tertiary;else->MaterialTheme.colorScheme.onSurfaceVariant})}
@Composable fun StatusCard(count:Int,onClick:()->Unit) {
 val tint=if(count==0)incomeColor() else MaterialTheme.colorScheme.tertiary
 GroupedList {
  Row(Modifier.fillMaxWidth().clickable(onClick=onClick).padding(GlassTokens.row),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GlassTokens.row)) {
   Box(Modifier.size(52.dp).clip(RoundedCornerShape(GlassTokens.controlCorner)).background(tint.copy(alpha=.12f)),contentAlignment=Alignment.Center){Icon(if(count==0)AppIcons.CheckCircle else AppIcons.Tune,null,tint=tint)}
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(if(count==0)"Sẵn sàng" else "Cần xử lý $count mục",style=MaterialTheme.typography.titleMedium);SectionFooter("Kiểm tra thiết lập")}
   Icon(AppIcons.ChevronRight,null,tint=MaterialTheme.colorScheme.onSurfaceVariant)
  }
 }
}

@Composable fun PinDots(count:Int,errorTick:Int=0){val shake=remember {Animatable(0f)};val reduce=LocalReduceMotion.current;LaunchedEffect(errorTick){if(errorTick>0&&!reduce){repeat(3){shake.animateTo(10f,tween(40));shake.animateTo(-10f,tween(40))};shake.animateTo(0f)}};Row(Modifier.offset {IntOffset(shake.value.dp.roundToPx(),0)}.semantics {contentDescription="Đã nhập $count chữ số"},horizontalArrangement=Arrangement.spacedBy(GlassTokens.small)){repeat(count.coerceIn(0,12)){Box(Modifier.size(SecurityTokens.dot).clip(CircleShape).background(MaterialTheme.colorScheme.primary))};if(count==0)Box(Modifier.size(SecurityTokens.dot).clip(CircleShape).border(1.dp,MaterialTheme.colorScheme.outline,CircleShape))}}
@Composable fun PinKeypad(enabled:Boolean=true,biometric:(()->Unit)?=null,onDigit:(Char)->Unit,onDelete:()->Unit) {
 val haptic=LocalHapticFeedback.current
 Column(Modifier.widthIn(max=GlassTokens.keyWidth).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(GlassTokens.small)) {
  listOf(listOf("1","2","3"),listOf("4","5","6"),listOf("7","8","9"),listOf("bio","0","delete")).forEach {row->
   Row(horizontalArrangement=Arrangement.spacedBy(GlassTokens.gap)) {
    row.forEach {key->
     if(key=="bio"&&biometric==null)Spacer(Modifier.weight(1f).aspectRatio(1f)) else {
      val label=when(key){"bio"->"Mở bằng sinh trắc học";"delete"->"Xoá một số";else->key}
      Box(Modifier.weight(1f).aspectRatio(1f).tonalSurface(CircleShape,PastelTokens.shadowControl).testTag("pin-$key").clickable(enabled=enabled,role=Role.Button){haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove);when(key){"bio"->biometric?.invoke();"delete"->onDelete();else->onDigit(key.single())}}.semantics {contentDescription=label},contentAlignment=Alignment.Center) {
       val tint=MaterialTheme.colorScheme.onSurface.copy(alpha=if(enabled)1f else .4f)
       when(key){"delete"->Icon(AppIcons.Backspace,null,tint=tint);"bio"->Icon(AppIcons.Fingerprint,null,tint=tint);else->Text(key,style=MaterialTheme.typography.headlineMedium,color=tint)}
      }
     }
    }
   }
  }
 }
}

@Composable fun StepScaffold(title:String,step:Int,onBack:()->Unit,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState()).padding(GlassTokens.margin),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){TextButton(onClick=onBack,modifier=Modifier.align(Alignment.Start)){Text("Quay lại")};LinearProgressIndicator(progress={step/3f},modifier=Modifier.fillMaxWidth());Text(title,style=MaterialTheme.typography.headlineMedium);content()}}
@Composable fun ConfirmSheet(title:String,onDismiss:()->Unit,content:@Composable ColumnScope.()->Unit){GlassBottomSheet(onDismiss){Column(Modifier.padding(GlassTokens.margin).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){Text(title,style=MaterialTheme.typography.titleLarge);content()}}}
@Preview @Preview(uiMode=android.content.res.Configuration.UI_MODE_NIGHT_YES) @Composable private fun SecurityComponentsPreview(){ChiTieuTheme("system"){Column(Modifier.background(MaterialTheme.colorScheme.background).padding(GlassTokens.margin),verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){StatusCard(2){};GroupedList {SettingsRow("Mở bằng sinh trắc học",icon=AppIcons.Fingerprint,trailing={IosSwitch(true,{})})};SectionHeader("Bảo mật");SectionFooter("Giữ dữ liệu riêng tư");StatusIcon(1);PinDots(6);PinKeypad(onDigit={},onDelete={})}}}



@Preview @Preview(uiMode=android.content.res.Configuration.UI_MODE_NIGHT_YES) @Composable private fun SwitchPreview(){ChiTieuTheme("system"){IosSwitch(true,{})}}
@Preview @Preview(uiMode=android.content.res.Configuration.UI_MODE_NIGHT_YES) @Composable private fun SettingsRowPreview(){ChiTieuTheme("system"){GroupedList{SettingsRow("Khoá ứng dụng","Đã bật",AppIcons.Lock,onClick={})}}}
@Preview @Preview(uiMode=android.content.res.Configuration.UI_MODE_NIGHT_YES) @Composable private fun SectionPreview(){ChiTieuTheme("system"){Column{SectionHeader("Bảo mật");SectionFooter("Mở khoá theo cách của bạn.")}}}
@Preview @Preview(uiMode=android.content.res.Configuration.UI_MODE_NIGHT_YES) @Composable private fun StatusPreview(){ChiTieuTheme("system"){Column{StatusCard(2){};StatusIcon(1)}}}
@Preview @Preview(uiMode=android.content.res.Configuration.UI_MODE_NIGHT_YES) @Composable private fun DotsPreview(){ChiTieuTheme("system"){PinDots(6)}}
@Preview @Preview(uiMode=android.content.res.Configuration.UI_MODE_NIGHT_YES) @Composable private fun KeypadPreview(){ChiTieuTheme("system"){PinKeypad(onDigit={},onDelete={})}}
@Preview @Preview(uiMode=android.content.res.Configuration.UI_MODE_NIGHT_YES) @Composable private fun StepPreview(){ChiTieuTheme("system"){StepScaffold("Tạo mã PIN",1,{}){PinDots(6);PinKeypad(onDigit={},onDelete={})}}}
@Preview @Preview(uiMode=android.content.res.Configuration.UI_MODE_NIGHT_YES) @Composable private fun ConfirmPreview(){ChiTieuTheme("system"){ConfirmSheet("Xác nhận",{}){PillButton("Tiếp tục",{})}}}
