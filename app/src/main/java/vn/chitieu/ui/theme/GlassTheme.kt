package vn.chitieu.ui.theme
import android.content.*
import android.os.PowerManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.Alignment
import android.provider.Settings
import vn.chitieu.R
import com.kyant.backdrop.backdrops.*
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.*
object GlassTokens {
 val corner=24.dp; val controlCorner=14.dp; val gap=12.dp; val margin=20.dp
 val small=8.dp; val row=16.dp; val inputCorner=12.dp; val bottom=104.dp
 const val blur=6f; const val refractionHeight=8f; const val refraction=12f
 val ink=Color.Black; val accent=Color(0xFF007AFF); val warning=Color(0xFFFF9500)
}
val LocalReduceMotion=staticCompositionLocalOf {false}
@Composable fun incomeColor()=if(MaterialTheme.colorScheme.background.luminance()<.3f)Color(0xFF30D158) else Color(0xFF187B36)
@Composable fun expenseColor()=if(MaterialTheme.colorScheme.background.luminance()<.3f)Color(0xFFFF453A) else Color(0xFFC62D24)
private val Inter=FontFamily(Font(R.font.inter,FontWeight.Normal,variationSettings=FontVariation.Settings(FontVariation.weight(400))),Font(R.font.inter,FontWeight.SemiBold,variationSettings=FontVariation.Settings(FontVariation.weight(600))),Font(R.font.inter,FontWeight.Bold,variationSettings=FontVariation.Settings(FontVariation.weight(700))))
private fun type(size:Int,weight:FontWeight=FontWeight.Normal)=TextStyle(fontFamily=Inter,fontSize=size.sp,lineHeight=(size*1.25).sp,fontWeight=weight)
private data class GlassContext(val backdrop:LayerBackdrop,val contentBackdrop:LayerBackdrop,val reduced:Boolean)
private val LocalGlass=staticCompositionLocalOf<GlassContext?> {null}
@Composable fun ChiTieuTheme(mode:String,content:@Composable ()->Unit) {
 val dark=if(mode=="system")isSystemInDarkTheme() else mode=="dark"
 val colors=if(dark)darkColorScheme(primary=Color(0xFF0A84FF),onPrimary=Color.White,background=Color.Black,surface=Color(0xFF1C1C1E),onBackground=Color.White,onSurface=Color.White,onSurfaceVariant=Color(0xFFAEAEB2),outlineVariant=Color(0xFF38383A),tertiary=GlassTokens.warning,error=Color(0xFFFF453A)) else lightColorScheme(primary=GlassTokens.accent,onPrimary=Color.White,onBackground=GlassTokens.ink,onSurface=GlassTokens.ink,onSurfaceVariant=Color(0xFF636366),background=Color(0xFFF2F2F7),surface=Color.White,outlineVariant=Color(0xFFE5E5EA),tertiary=Color(0xFF945600),error=Color(0xFFC62D24))
 MaterialTheme(colorScheme=colors,typography=Typography(displayLarge=type(40,FontWeight.Bold),headlineLarge=type(34,FontWeight.Bold),headlineMedium=type(28,FontWeight.Bold),headlineSmall=type(22,FontWeight.Bold),titleLarge=type(22,FontWeight.SemiBold),titleMedium=type(17,FontWeight.SemiBold),titleSmall=type(15,FontWeight.SemiBold),bodyLarge=type(17),bodyMedium=type(16),bodySmall=type(13),labelLarge=type(16,FontWeight.SemiBold),labelMedium=type(13,FontWeight.SemiBold),labelSmall=type(12)),shapes=Shapes(small=RoundedCornerShape(12.dp),medium=RoundedCornerShape(14.dp),large=RoundedCornerShape(24.dp)),content=content)
}

@Composable fun GlassScene(reduce:Boolean,content:@Composable ()->Unit) {
 val context=LocalContext.current;val power=remember {context.getSystemService(PowerManager::class.java)}
 var saving by remember {mutableStateOf(power.isPowerSaveMode)}
 DisposableEffect(Unit){val receiver=object:BroadcastReceiver(){override fun onReceive(c:Context?,i:Intent?){saving=power.isPowerSaveMode}};context.registerReceiver(receiver,IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),Context.RECEIVER_NOT_EXPORTED);onDispose {context.unregisterReceiver(receiver)}}
 val motionOff=Settings.Global.getFloat(context.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)==0f
 val backdrop=rememberLayerBackdrop();val contentBackdrop=rememberLayerBackdrop();val dark=MaterialTheme.colorScheme.background.luminance()<0.3f
 CompositionLocalProvider(LocalGlass provides GlassContext(backdrop,contentBackdrop,reduce||saving),LocalReduceMotion provides (reduce||saving||motionOff),LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
  Box(Modifier.fillMaxSize()) {
   Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) {drawRect(if(dark)Color.Black else Color(0xFFF2F2F7))}
   content()
  }
 }
}
@Composable fun Modifier.glassContentSource():Modifier {val glass=LocalGlass.current;return if(glass==null||glass.reduced)this else this.layerBackdrop(glass.contentBackdrop)}
@Composable fun Modifier.glassSurface():Modifier {
 val glass=LocalGlass.current;val shape=RoundedCornerShape(GlassTokens.corner);val tint=MaterialTheme.colorScheme.surface.copy(alpha=if(glass?.reduced!=false).92f else .86f)
 val effect=if(glass!=null&&!glass.reduced)Modifier.drawBackdrop(backdrop=glass.backdrop,shape={shape},effects={vibrancy();blur(GlassTokens.blur);lens(GlassTokens.refractionHeight,GlassTokens.refraction)}) else Modifier
 return this.shadow(6.dp,shape,ambientColor=Color.Black.copy(alpha=.08f),spotColor=Color.Black.copy(alpha=.08f)).then(effect).clip(shape).background(tint).border(1.dp,MaterialTheme.colorScheme.outlineVariant.copy(alpha=.65f),shape)
}
@Composable fun GlassCard(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface){Column(modifier.glassSurface(),content=content)}}
@Composable fun GlassTopBar(title:String,subtitle:String?=null) {Column(Modifier.fillMaxWidth().padding(vertical=12.dp)){Text(title,style=MaterialTheme.typography.headlineLarge);subtitle?.let {Text(it,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
@Composable fun GlassButton(label:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true) {
 val interaction=remember {MutableInteractionSource()};val pressed by interaction.collectIsPressedAsState();val scale by animateFloatAsState(if(pressed&&!LocalReduceMotion.current).97f else 1f,animationSpec=if(LocalReduceMotion.current)snap() else spring(stiffness=Spring.StiffnessMediumLow),label="press")
 Box(modifier.graphicsLayer {scaleX=scale;scaleY=scale}.defaultMinSize(minHeight=52.dp).glassSurface().clickable(enabled=enabled,interactionSource=interaction,indication=null,onClick=onClick).padding(horizontal=20.dp,vertical=14.dp),contentAlignment=androidx.compose.ui.Alignment.Center){Text(label,color=if(enabled)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.labelLarge)}
}
@Composable fun GlassBottomBar(selected:Int,onSelect:(Int)->Unit) {
 val glass=LocalGlass.current
 CompositionLocalProvider(LocalGlass provides glass?.copy(backdrop=glass.contentBackdrop)){
 val icons=listOf(Icons.Rounded.Home,Icons.Rounded.ReceiptLong,Icons.Rounded.Settings)
 val position by animateFloatAsState(selected.toFloat(),animationSpec=if(LocalReduceMotion.current)snap() else spring(stiffness=Spring.StiffnessMediumLow),label="selectedTab")
 val selectedColor=MaterialTheme.colorScheme.primary.copy(alpha=.12f)
 GlassCard(Modifier.padding(horizontal=16.dp,vertical=10.dp).fillMaxWidth()){Box(Modifier.padding(6.dp)){
 Canvas(Modifier.matchParentSize()){val w=size.width/3;drawRoundRect(selectedColor,topLeft=androidx.compose.ui.geometry.Offset(w*position,0f),size=androidx.compose.ui.geometry.Size(w,size.height),cornerRadius=androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()))}
 Row(horizontalArrangement=Arrangement.SpaceEvenly){listOf("Tổng quan","Giao dịch","Cài đặt").forEachIndexed {i,label->val tint=if(selected==i)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
 Column(Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).clickable {onSelect(i)}.padding(vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(icons[i],null,tint=tint,modifier=Modifier.size(23.dp));Text(label,color=tint,style=MaterialTheme.typography.labelSmall,maxLines=1)}
 }}}}
 }
}
@Composable fun GlassDialog(onDismiss:()->Unit,content:@Composable ColumnScope.()->Unit) {Dialog(onDismissRequest=onDismiss){GlassCard(Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp),content=content)}}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun GlassBottomSheet(onDismiss:()->Unit,content:@Composable ColumnScope.()->Unit) {ModalBottomSheet(onDismissRequest=onDismiss,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),containerColor=MaterialTheme.colorScheme.surface.copy(alpha=.96f),content=content)}
@Composable fun AmountInput(value:String,onValue:(String)->Unit) {OutlinedTextField(value=value,onValueChange=onValue,modifier=Modifier.fillMaxWidth(),label={Text("Số tiền")},suffix={Text("₫")},textStyle=MaterialTheme.typography.headlineLarge,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=androidx.compose.ui.text.input.KeyboardType.Number),singleLine=true)}


// TODO: verify sustained glass rendering on physical Xiaomi; emulator timing is not a device benchmark.
