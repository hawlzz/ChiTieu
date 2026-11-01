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
import androidx.compose.ui.Alignment
import androidx.compose.foundation.selection.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalDensity
import android.provider.Settings
import vn.chitieu.R
import com.kyant.backdrop.backdrops.*
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.*
object GlassTokens {
 val corner=24.dp; val controlCorner=14.dp; val gap=12.dp; val margin=20.dp
 val small=8.dp; val row=16.dp; val inputCorner=12.dp; val bottom=104.dp
 val minTouch=48.dp; val icon=24.dp; val badge=36.dp; val contentWidth=720.dp; val tabWidth=560.dp
 val keyWidth=264.dp; val compactListWidth=380.dp
 val lightBackground=Color(0xFFEAF1F7); val darkBackground=Color(0xFF152735)
 const val amountFitSafety=.98f // Allow glyph rounding without orphaning the currency.
 const val blur=6f; const val refractionHeight=8f; const val refraction=12f
 val ink=Color(0xFF20394E); val accent=Color(0xFF416B8C); val warning=Color(0xFFFF9500)
}
val LocalReduceMotion=staticCompositionLocalOf {false}
val LocalHideNavigation=staticCompositionLocalOf<(Boolean)->Unit> {{}}
@Composable fun incomeColor()=if(MaterialTheme.colorScheme.background.luminance()<.3f)Color(0xFF8ED6B3) else Color(0xFF25724E)
@Composable fun expenseColor()=if(MaterialTheme.colorScheme.background.luminance()<.3f)Color(0xFFF4A3AC) else Color(0xFFB33E50)
private val Inter=FontFamily(Font(R.font.inter,FontWeight.Normal,variationSettings=FontVariation.Settings(FontVariation.weight(400))),Font(R.font.inter,FontWeight.SemiBold,variationSettings=FontVariation.Settings(FontVariation.weight(600))),Font(R.font.inter,FontWeight.Bold,variationSettings=FontVariation.Settings(FontVariation.weight(700))))
private fun type(size:Int,weight:FontWeight=FontWeight.Normal)=TextStyle(fontFamily=Inter,fontSize=size.sp,lineHeight=(size*1.25).sp,fontWeight=weight)
private data class GlassContext(val backdrop:LayerBackdrop,val contentBackdrop:LayerBackdrop,val reduced:Boolean)
private val LocalGlass=staticCompositionLocalOf<GlassContext?> {null}
internal fun pastelColorScheme(dark:Boolean)=if(dark)darkColorScheme(
 primary=Color(0xFFB7D5E9),onPrimary=Color(0xFF20394E),secondary=Color(0xFFC3D8E7),onSecondary=Color(0xFF20394E),
 primaryContainer=Color(0xFF365B77),onPrimaryContainer=Color(0xFFE8F4FC),secondaryContainer=Color(0xFF304B60),onSecondaryContainer=Color(0xFFE8F4FC),
 background=GlassTokens.darkBackground,surface=Color(0xFF253A4B),surfaceVariant=Color(0xFF2B4255),onBackground=Color(0xFFEDF5FA),onSurface=Color(0xFFEDF5FA),
 onSurfaceVariant=Color(0xFFB9CBD9),outline=Color(0xFF8AA3B6),outlineVariant=Color(0xFF3D566B),tertiary=Color(0xFFE5C68F),error=Color(0xFFF4A3AC),onError=Color(0xFF40151C)
) else lightColorScheme(
 primary=GlassTokens.accent,onPrimary=Color.White,secondary=Color(0xFF526F87),onSecondary=Color.White,
 primaryContainer=Color(0xFFC1D9EB),onPrimaryContainer=Color(0xFF254761),secondaryContainer=Color(0xFFD9E7F1),onSecondaryContainer=Color(0xFF294A63),
 background=GlassTokens.lightBackground,surface=Color(0xFFF8FBFD),surfaceVariant=Color(0xFFE5EFF5),onBackground=GlassTokens.ink,onSurface=GlassTokens.ink,
 onSurfaceVariant=Color(0xFF546D80),outline=Color(0xFF6D879B),outlineVariant=Color(0xFFCADBE7),tertiary=Color(0xFF8D6428),error=Color(0xFFB33E50),onError=Color.White
)
@Composable fun ChiTieuTheme(mode:String,content:@Composable ()->Unit) {
 val dark=if(mode=="system")isSystemInDarkTheme() else mode=="dark"
 val colors=pastelColorScheme(dark)
 MaterialTheme(colorScheme=colors,typography=Typography(displayLarge=type(42,FontWeight.Bold),headlineLarge=type(34,FontWeight.Bold),headlineMedium=type(28,FontWeight.Bold),headlineSmall=type(22,FontWeight.Bold),titleLarge=type(22,FontWeight.SemiBold),titleMedium=type(17,FontWeight.SemiBold),titleSmall=type(15,FontWeight.SemiBold),bodyLarge=type(17),bodyMedium=type(16),bodySmall=type(13),labelLarge=type(16,FontWeight.SemiBold),labelMedium=type(13,FontWeight.SemiBold),labelSmall=type(12)),shapes=Shapes(small=RoundedCornerShape(12.dp),medium=RoundedCornerShape(14.dp),large=RoundedCornerShape(24.dp)),content=content)
}

@Composable fun GlassScene(reduce:Boolean,content:@Composable ()->Unit) {
 val context=LocalContext.current;val power=remember {context.getSystemService(PowerManager::class.java)}
 var saving by remember {mutableStateOf(power.isPowerSaveMode)}
 DisposableEffect(Unit){val receiver=object:BroadcastReceiver(){override fun onReceive(c:Context?,i:Intent?){saving=power.isPowerSaveMode}};context.registerReceiver(receiver,IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),Context.RECEIVER_NOT_EXPORTED);onDispose {context.unregisterReceiver(receiver)}}
 var motionOff by remember {mutableStateOf(Settings.Global.getFloat(context.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)==0f)}
 DisposableEffect(context){val observer=object:android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())){override fun onChange(selfChange:Boolean){motionOff=Settings.Global.getFloat(context.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)==0f}};context.contentResolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),false,observer);onDispose{context.contentResolver.unregisterContentObserver(observer)}}
 val lowRam=remember {context.getSystemService(android.app.ActivityManager::class.java)?.isLowRamDevice==true}
 val reduced=reduce||saving||lowRam
 val backdrop=rememberLayerBackdrop();val contentBackdrop=rememberLayerBackdrop();val dark=MaterialTheme.colorScheme.background.luminance()<0.3f
 CompositionLocalProvider(LocalGlass provides GlassContext(backdrop,contentBackdrop,reduced),LocalReducedEffects provides reduced,LocalReduceMotion provides (reduced||motionOff),LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
  Box(Modifier.fillMaxSize()) {
   Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) {drawRect(Brush.verticalGradient(listOf(if(dark)GlassTokens.darkBackground else lerp(GlassTokens.lightBackground,PastelTokens.brandTop,.30f),if(dark)GlassTokens.darkBackground else GlassTokens.lightBackground)))}
   content()
  }
 }
}
@Composable fun Modifier.glassContentSource():Modifier {val glass=LocalGlass.current;return if(glass==null||glass.reduced)this else this.layerBackdrop(glass.contentBackdrop)}
@Composable fun Modifier.glassSurface():Modifier {
 val glass=LocalGlass.current;val shape=RoundedCornerShape(GlassTokens.corner);val tint=MaterialTheme.colorScheme.surface.copy(alpha=if(glass?.reduced!=false).92f else .86f)
 val effect=if(glass!=null&&!glass.reduced)Modifier.drawBackdrop(backdrop=glass.backdrop,shape={shape},effects={vibrancy();blur(GlassTokens.blur);lens(GlassTokens.refractionHeight,GlassTokens.refraction)}) else Modifier
 return this.shadow(if(LocalReducedEffects.current)PastelTokens.shadowControl else PastelTokens.shadowFloat,shape,ambientColor=MaterialTheme.colorScheme.primary.copy(alpha=.20f),spotColor=Color.Black.copy(alpha=.15f)).then(effect).tonalSurface(shape,0.dp,tint)
}
@Composable fun GlassCard(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface){Column(modifier.glassSurface(),content=content)}}
@Composable fun GlassTopBar(title:String,subtitle:String?=null) {Column(Modifier.fillMaxWidth().padding(vertical=12.dp)){Text(title,style=MaterialTheme.typography.headlineLarge);subtitle?.let {Text(it,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
@Composable fun GlassButton(label:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true) {
 val interaction=remember {MutableInteractionSource()};val pressed by interaction.collectIsPressedAsState();val scale by animateFloatAsState(if(pressed&&!LocalReduceMotion.current).97f else 1f,animationSpec=if(LocalReduceMotion.current)snap() else spring(stiffness=Spring.StiffnessMediumLow),label="press")
 Box(modifier.graphicsLayer {scaleX=scale;scaleY=scale}.defaultMinSize(minHeight=52.dp).glassSurface().clickable(enabled=enabled,interactionSource=interaction,indication=null,onClick=onClick).padding(horizontal=20.dp,vertical=14.dp),contentAlignment=androidx.compose.ui.Alignment.Center){Text(label,color=if(enabled)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.labelLarge)}
}
@Composable fun GlassBottomBar(selected:Int,onSelect:(Int)->Unit) {
 val glass=LocalGlass.current
 CompositionLocalProvider(LocalGlass provides glass?.copy(backdrop=glass.contentBackdrop)) {
  val icons=listOf(AppIcons.Home,AppIcons.Transactions,AppIcons.BarChart,AppIcons.Settings)
  val position by animateFloatAsState(selected.toFloat(),animationSpec=if(LocalReduceMotion.current)snap() else spring(stiffness=Spring.StiffnessMediumLow),label="selectedTab")
  val selectedColor=MaterialTheme.colorScheme.primaryContainer
  GlassCard(Modifier.widthIn(max=GlassTokens.tabWidth).padding(horizontal=GlassTokens.row,vertical=GlassTokens.small).fillMaxWidth()) {
   Box(Modifier.padding(6.dp)) {
    Canvas(Modifier.matchParentSize()) {val w=size.width/4;drawRoundRect(selectedColor,topLeft=androidx.compose.ui.geometry.Offset(w*position,0f),size=androidx.compose.ui.geometry.Size(w,size.height),cornerRadius=androidx.compose.ui.geometry.CornerRadius(GlassTokens.controlCorner.toPx()))}
    Row(Modifier.selectableGroup().height(IntrinsicSize.Min)) {
     listOf("Tổng quan","Giao dịch","Thống kê","Cài đặt").forEachIndexed {i,label->
      val tint=if(selected==i)MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
      Column(Modifier.weight(1f).fillMaxHeight().heightIn(min=64.dp).clip(RoundedCornerShape(GlassTokens.controlCorner)).selectable(selected==i,onClick={onSelect(i)},role=Role.Tab).padding(horizontal=2.dp,vertical=GlassTokens.small),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
       Icon(icons[i],null,tint=tint,modifier=Modifier.size(GlassTokens.icon))
       Text(label,color=tint,style=MaterialTheme.typography.labelSmall,maxLines=2,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
      }
     }
    }
   }
  }
 }
}

@Composable fun GlassDialog(onDismiss:()->Unit,content:@Composable ColumnScope.()->Unit) {Dialog(onDismissRequest=onDismiss){GlassCard(Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp),content=content)}}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun GlassBottomSheet(onDismiss:()->Unit,content:@Composable ColumnScope.()->Unit) {ModalBottomSheet(onDismissRequest=onDismiss,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),containerColor=MaterialTheme.colorScheme.surface.copy(alpha=.96f),content=content)}
@Composable fun AmountInput(value:String,onValue:(String)->Unit) {OutlinedTextField(value=value,onValueChange=onValue,modifier=Modifier.fillMaxWidth(),label={Text("Số tiền")},suffix={Text("₫")},textStyle=MaterialTheme.typography.headlineLarge,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=androidx.compose.ui.text.input.KeyboardType.Number),singleLine=true)}


// TODO: verify sustained glass rendering on physical Xiaomi; emulator timing is not a device benchmark.

@Composable fun bottomContentPadding()=GlassTokens.bottom+GlassTokens.minTouch*(LocalDensity.current.fontScale-1f).coerceAtLeast(0f)
