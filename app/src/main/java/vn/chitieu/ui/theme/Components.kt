package vn.chitieu.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import vn.chitieu.domain.Money

@Composable fun LargeTitleHeader(title:String,compact:Boolean=false,action:(@Composable ()->Unit)?=null) {
 Row(Modifier.fillMaxWidth().padding(vertical=GlassTokens.gap),verticalAlignment=Alignment.CenterVertically){Text(title,Modifier.weight(1f),style=if(compact)MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineLarge);action?.invoke()}
}
@Composable fun GroupedList(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit){Column(modifier.clip(RoundedCornerShape(GlassTokens.corner)).background(MaterialTheme.colorScheme.surface),content=content)}
@Composable fun GroupedRow(title:String,detail:String="",icon:ImageVector=Icons.Rounded.ChevronRight,tint:Color=MaterialTheme.colorScheme.primary,onClick:(()->Unit)?=null,divider:Boolean=true){
 Column {Row(Modifier.fillMaxWidth().then(if(onClick!=null)Modifier.clickable(onClick=onClick) else Modifier).padding(GlassTokens.row),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GlassTokens.gap)){Box(Modifier.size(34.dp).clip(RoundedCornerShape(GlassTokens.small)).background(tint.copy(alpha=.12f)),contentAlignment=Alignment.Center){Icon(icon,null,tint=tint,modifier=Modifier.size(21.dp))};Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.bodyLarge);if(detail.isNotBlank())Text(detail,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};if(onClick!=null)Icon(Icons.Rounded.ChevronRight,null,tint=MaterialTheme.colorScheme.onSurfaceVariant)};if(divider)HorizontalDivider(Modifier.padding(start=62.dp),color=MaterialTheme.colorScheme.outlineVariant)}
}
@Composable fun SegmentedControl(options:List<String>,selected:String,onSelect:(String)->Unit){Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(GlassTokens.inputCorner)).background(MaterialTheme.colorScheme.onSurface.copy(alpha=.07f)).padding(3.dp)){options.forEach {label->Surface(onClick={onSelect(label)},modifier=Modifier.weight(1f),shape=RoundedCornerShape(9.dp),color=if(label==selected)MaterialTheme.colorScheme.surface else Color.Transparent,shadowElevation=if(label==selected)1.dp else 0.dp){Text(label,Modifier.padding(vertical=10.dp),textAlign=androidx.compose.ui.text.style.TextAlign.Center,style=MaterialTheme.typography.labelMedium)}}}}
@Composable fun PillButton(label:String,onClick:()->Unit,modifier:Modifier=Modifier,primary:Boolean=true,danger:Boolean=false,enabled:Boolean=true){Button(onClick=onClick,enabled=enabled,modifier=modifier.heightIn(min=52.dp),shape=CircleShape,colors=ButtonDefaults.buttonColors(containerColor=if(danger)MaterialTheme.colorScheme.error else if(primary)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha=.1f),contentColor=if(primary||danger)Color.White else MaterialTheme.colorScheme.primary)){Text(label)}}
@Composable fun SearchBar(value:String,onChange:(String)->Unit){TextField(value,onChange,Modifier.fillMaxWidth(),placeholder={Text("Tìm giao dịch")},leadingIcon={Icon(Icons.Rounded.Search,null)},singleLine=true,shape=RoundedCornerShape(GlassTokens.inputCorner),colors=TextFieldDefaults.colors(focusedContainerColor=MaterialTheme.colorScheme.onSurface.copy(alpha=.06f),unfocusedContainerColor=MaterialTheme.colorScheme.onSurface.copy(alpha=.06f),focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent))}
@Composable fun AmountText(amount:Long,modifier:Modifier=Modifier,color:Color=MaterialTheme.colorScheme.onSurface,large:Boolean=true,animate:Boolean=false){
 val reduced=LocalReduceMotion.current;val progress=remember {Animatable(1f)}
 LaunchedEffect(amount,animate,reduced){if(animate&&!reduced){progress.snapTo(0f);progress.animateTo(1f,tween(420))}else progress.snapTo(1f)}
 val shown=if(progress.value>=1f)amount else (amount*progress.value.toDouble()).toLong()
 Text(Money.format(shown),modifier,color=color,style=(if(large)MaterialTheme.typography.displayLarge else MaterialTheme.typography.titleMedium).copy(fontFeatureSettings="tnum"),maxLines=2,overflow=TextOverflow.Ellipsis)
}
@Composable fun CategoryIcon(name:String,color:Color=MaterialTheme.colorScheme.primary){val tint=if(color==Color(0xFF267D66))listOf(Color(0xFF187B36),Color(0xFF007AFF),Color(0xFFAF52DE),Color(0xFFB45D00),Color(0xFFCB3358),Color(0xFF008C95))[kotlin.math.abs(name.hashCode()%6)] else color;val icon=when {name.contains("Ăn")->Icons.Rounded.Restaurant;(name.contains("Đi")||name.contains("Di "))->Icons.Rounded.DirectionsCar;name.contains("Mua")->Icons.Rounded.ShoppingBag;name.contains("Nhà")->Icons.Rounded.Home;name.contains("Sức")->Icons.Rounded.Favorite;name.contains("Hoá")->Icons.Rounded.ReceiptLong;name.contains("Giải")->Icons.Rounded.SportsEsports;name.contains("Giáo")->Icons.Rounded.School;name.contains("Lương")->Icons.Rounded.Work;name.contains("Thưởng")->Icons.Rounded.CardGiftcard;else->Icons.Rounded.AccountBalanceWallet};Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(tint.copy(alpha=.12f)),contentAlignment=Alignment.Center){Icon(icon,null,tint=tint,modifier=Modifier.size(24.dp))}}
@Composable fun Chip(label:String,selected:Boolean=false,onClick:()->Unit){FilterChip(selected,onClick,label={Text(label,style=MaterialTheme.typography.labelMedium)},shape=CircleShape)}
@Composable fun EmptyState(onScan:()->Unit,onAdd:()->Unit){Column(Modifier.fillMaxWidth().padding(vertical=28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(GlassTokens.gap)){Box(Modifier.size(88.dp).clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha=.08f)),contentAlignment=Alignment.Center){Icon(Icons.Rounded.ReceiptLong,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(44.dp))};Text("Chưa có giao dịch",style=MaterialTheme.typography.titleLarge);Text("Bắt đầu từ một khoản nhỏ hôm nay.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);PillButton("Quét QR chuyển khoản",onScan,Modifier.fillMaxWidth());PillButton("Thêm khoản đầu tiên",onAdd,Modifier.fillMaxWidth(),primary=false)}}

@Composable fun InitialAvatar(name:String){Box(Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha=.12f)),contentAlignment=Alignment.Center){Text(name.trim().firstOrNull()?.uppercase()?:"?",style=MaterialTheme.typography.headlineMedium,color=MaterialTheme.colorScheme.primary)}}
