package vn.chitieu.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.selection.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import vn.chitieu.domain.Money

@Composable fun LargeTitleHeader(title:String,compact:Boolean=false,action:(@Composable ()->Unit)?=null) {
 Row(Modifier.fillMaxWidth().padding(vertical=GlassTokens.gap),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GlassTokens.small)) {
  Text(title,Modifier.weight(1f).semantics {heading()},style=if(compact)MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineLarge)
  action?.invoke()
 }
}

@Composable fun GroupedList(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit){Column(modifier.tonalSurface(),content=content)}
@Composable fun GroupedRow(title:String,detail:String="",icon:ImageVector=AppIcons.ChevronRight,tint:Color=MaterialTheme.colorScheme.primary,onClick:(()->Unit)?=null,divider:Boolean=true){
 Column {Row(Modifier.fillMaxWidth().then(if(onClick!=null)Modifier.clickable(onClick=onClick) else Modifier).padding(GlassTokens.row),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GlassTokens.gap)){FrostedIconBadge(icon,tint=tint);Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.bodyLarge);if(detail.isNotBlank())Text(detail,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};if(onClick!=null)Icon(AppIcons.ChevronRight,null,tint=MaterialTheme.colorScheme.onSurfaceVariant)};if(divider)HorizontalDivider(Modifier.padding(start=62.dp),color=MaterialTheme.colorScheme.outlineVariant)}
}
@Composable fun SegmentedControl(options:List<String>,selected:String,onSelect:(String)->Unit) {
 Row(Modifier.fillMaxWidth().selectableGroup().height(IntrinsicSize.Min).clip(RoundedCornerShape(GlassTokens.controlCorner)).background(MaterialTheme.colorScheme.onSurface.copy(alpha=.06f)).padding(4.dp)) {
  options.forEach {label->
   val active=label==selected
   Box(Modifier.weight(1f).fillMaxHeight().heightIn(min=GlassTokens.minTouch).clip(RoundedCornerShape(GlassTokens.inputCorner)).background(if(active)MaterialTheme.colorScheme.surface else Color.Transparent).selectable(active,onClick={onSelect(label)},role=Role.Tab).padding(horizontal=4.dp,vertical=GlassTokens.small),contentAlignment=Alignment.Center) {
    Text(label,color=if(active)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.labelMedium,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
   }
  }
 }
}

@Composable fun PillButton(label:String,onClick:()->Unit,modifier:Modifier=Modifier,primary:Boolean=true,danger:Boolean=false,enabled:Boolean=true) {
 val source=remember {androidx.compose.foundation.interaction.MutableInteractionSource()}
 Button(onClick=onClick,enabled=enabled,interactionSource=source,modifier=modifier.heightIn(min=52.dp).softPress(source,enabled),shape=CircleShape,
  elevation=ButtonDefaults.buttonElevation(defaultElevation=if(LocalReducedEffects.current)2.dp else PastelTokens.shadowControl,pressedElevation=2.dp,disabledElevation=0.dp),
  colors=ButtonDefaults.buttonColors(containerColor=if(danger)MaterialTheme.colorScheme.error else if(primary)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
   contentColor=if(danger)MaterialTheme.colorScheme.onError else if(primary)MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary)) {Text(label,textAlign=androidx.compose.ui.text.style.TextAlign.Center)}
}

@Composable fun SearchBar(value:String,onChange:(String)->Unit) {
 TextField(value,onChange,Modifier.fillMaxWidth().tonalSurface(RoundedCornerShape(GlassTokens.controlCorner),PastelTokens.shadowControl).semantics {contentDescription="Tìm giao dịch"},placeholder={Text("Tìm giao dịch")},leadingIcon={Icon(AppIcons.Search,null)},trailingIcon=if(value.isNotEmpty())({IconButton(onClick={onChange("")}){Icon(AppIcons.Close,"Xoá nội dung tìm kiếm")}}) else null,singleLine=true,shape=RoundedCornerShape(GlassTokens.controlCorner),colors=TextFieldDefaults.colors(focusedContainerColor=MaterialTheme.colorScheme.surface,unfocusedContainerColor=MaterialTheme.colorScheme.surface,focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent))
}

@Composable fun AmountText(amount:Long,modifier:Modifier=Modifier,color:Color=MaterialTheme.colorScheme.onSurface,large:Boolean=true,animate:Boolean=false,prefix:String="",textAlign:androidx.compose.ui.text.style.TextAlign=androidx.compose.ui.text.style.TextAlign.Start) {
 val reduced=LocalReduceMotion.current;val progress=remember {Animatable(1f)}
 LaunchedEffect(amount,animate,reduced){if(animate&&!reduced){progress.snapTo(0f);progress.animateTo(1f,tween(420))}else progress.snapTo(1f)}
 val shown=if(progress.value>=1f)amount else (amount*progress.value.toDouble()).toLong()
 val style=(if(large)MaterialTheme.typography.displayLarge else MaterialTheme.typography.titleMedium).copy(fontFeatureSettings="tnum",letterSpacing=0.sp)
 val measurer=rememberTextMeasurer()
 // Fit the final value, so counting never changes the font size mid-animation.
 BoxWithConstraints(modifier) {
  val width=measurer.measure(AnnotatedString(prefix+Money.format(amount)),style=style,softWrap=false,maxLines=1).size.width
  val fraction=if(width>0)(constraints.maxWidth.toFloat()/width*GlassTokens.amountFitSafety).coerceAtMost(1f) else 1f
  val minimum=if(large)24.sp else 13.sp
  val size=(style.fontSize.value*fraction).coerceAtLeast(minimum.value).sp
  Text(prefix+Money.format(shown),Modifier.fillMaxWidth(),color=color,textAlign=textAlign,style=style.copy(fontSize=size),maxLines=2,overflow=TextOverflow.Ellipsis)
 }
}

@Composable fun CategoryIcon(name:String,color:Color=MaterialTheme.colorScheme.primary){FrostedIconBadge(AppIcons.category(name),tint=color,size=44.dp,glyphSize=GlassTokens.icon)}
@Composable fun Chip(label:String,selected:Boolean=false,onClick:()->Unit){val colors=MaterialTheme.colorScheme;FilterChip(selected,onClick,modifier=Modifier.tonalSurface(CircleShape,PastelTokens.shadowBadge*.6f,if(selected)colors.primaryContainer else colors.surface),label={Text(label,style=MaterialTheme.typography.labelMedium)},shape=CircleShape,colors=FilterChipDefaults.filterChipColors(containerColor=Color.Transparent,selectedContainerColor=Color.Transparent,labelColor=colors.onSurfaceVariant,selectedLabelColor=colors.onPrimaryContainer),border=null)}
@Composable fun EmptyState(onScan:()->Unit,onAdd:()->Unit){Column(Modifier.fillMaxWidth().padding(vertical=28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){Box(Modifier.size(88.dp).clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha=.08f)),contentAlignment=Alignment.Center){Icon(AppIcons.ReceiptLong,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(44.dp))};Text("Chưa có giao dịch",style=MaterialTheme.typography.titleLarge);Text("Bắt đầu từ một khoản nhỏ hôm nay.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);PillButton("Quét QR chuyển khoản",onScan,Modifier.fillMaxWidth());PillButton("Thêm khoản đầu tiên",onAdd,Modifier.fillMaxWidth(),primary=false)}}

@Composable fun InitialAvatar(name:String){Box(Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha=.12f)),contentAlignment=Alignment.Center){Text(name.trim().firstOrNull()?.uppercase()?:"?",style=MaterialTheme.typography.headlineMedium,color=MaterialTheme.colorScheme.primary)}}
