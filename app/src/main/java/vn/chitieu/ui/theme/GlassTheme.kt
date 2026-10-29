package vn.chitieu.ui.theme
import android.content.*
import android.os.PowerManager
import androidx.compose.animation.core.animateFloatAsState
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
import com.kyant.backdrop.backdrops.*
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.*
object GlassTokens {
 val corner=28.dp; val controlCorner=22.dp; val gap=12.dp; val margin=20.dp
 const val blur=10f; const val refractionHeight=18f; const val refraction=24f
 val ink=Color(0xFF13273F); val accent=Color(0xFF235EC8)
}
private data class GlassContext(val backdrop:LayerBackdrop,val reduced:Boolean)
private val LocalGlass=staticCompositionLocalOf<GlassContext?> {null}
@Composable fun ChiTieuTheme(mode:String,content:@Composable ()->Unit) {
 val dark=if(mode=="system")isSystemInDarkTheme() else mode=="dark"
 val colors=if(dark)darkColorScheme(primary=Color(0xFFAFC9FF),background=Color(0xFF101B2C),surface=Color(0xFF17243A)) else lightColorScheme(primary=GlassTokens.accent,onBackground=GlassTokens.ink,onSurface=GlassTokens.ink,background=Color(0xFFF2F5FB),surface=Color(0xFFF8FAFE))
 MaterialTheme(colorScheme=colors,shapes=Shapes(medium=RoundedCornerShape(20.dp),large=RoundedCornerShape(28.dp)),content=content)
}
@Composable fun GlassScene(reduce:Boolean,content:@Composable ()->Unit) {
 val context=LocalContext.current;val power=remember {context.getSystemService(PowerManager::class.java)}
 var saving by remember {mutableStateOf(power.isPowerSaveMode)}
 DisposableEffect(Unit){val receiver=object:BroadcastReceiver(){override fun onReceive(c:Context?,i:Intent?){saving=power.isPowerSaveMode}};context.registerReceiver(receiver,IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),Context.RECEIVER_NOT_EXPORTED);onDispose {context.unregisterReceiver(receiver)}}
 val backdrop=rememberLayerBackdrop();val dark=MaterialTheme.colorScheme.background.luminance()<0.3f
 CompositionLocalProvider(LocalGlass provides GlassContext(backdrop,reduce||saving),LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
  Box(Modifier.fillMaxSize()) {
   Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) {drawRect(Brush.verticalGradient(if(dark)listOf(Color(0xFF111B31),Color(0xFF1A3050),Color(0xFF142C33)) else listOf(Color(0xFFEAF1FF),Color(0xFFF3EEF7),Color(0xFFE9F4F2))));drawCircle(Brush.radialGradient(listOf(Color(0x554788FF),Color.Transparent),center=androidx.compose.ui.geometry.Offset(size.width*.9f,size.height*.28f),radius=size.width*.85f),radius=size.width*.85f,center=androidx.compose.ui.geometry.Offset(size.width*.9f,size.height*.28f));drawCircle(Color(0x2279C9B5),radius=size.width*.6f,center=androidx.compose.ui.geometry.Offset(size.width*.1f,size.height*.72f))}
   content()
  }
 }
}
@Composable fun Modifier.glassSurface():Modifier {
 val glass=LocalGlass.current;val shape=RoundedCornerShape(GlassTokens.corner);val tint=MaterialTheme.colorScheme.surface.copy(alpha=if(glass?.reduced!=false).92f else .66f)
 val effect=if(glass!=null&&!glass.reduced)Modifier.drawBackdrop(backdrop=glass.backdrop,shape={shape},effects={vibrancy();blur(GlassTokens.blur);lens(GlassTokens.refractionHeight,GlassTokens.refraction)}) else Modifier
 return this.shadow(6.dp,shape,ambientColor=Color.Black.copy(alpha=.08f),spotColor=Color.Black.copy(alpha=.08f)).then(effect).clip(shape).background(tint).border(1.dp,Color.White.copy(alpha=.48f),shape)
}
@Composable fun GlassCard(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface){Column(modifier.glassSurface(),content=content)}}
@Composable fun GlassTopBar(title:String,subtitle:String?=null) {GlassCard(Modifier.fillMaxWidth().padding(horizontal=GlassTokens.margin,vertical=8.dp)){Column(Modifier.padding(18.dp)){Text(title,style=MaterialTheme.typography.headlineSmall);subtitle?.let {Text(it,style=MaterialTheme.typography.bodySmall)}}}}
@Composable fun GlassButton(label:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true) {
 val interaction=remember {MutableInteractionSource()};val pressed by interaction.collectIsPressedAsState();val scale by animateFloatAsState(if(pressed).97f else 1f,label="press")
 Box(modifier.graphicsLayer {scaleX=scale;scaleY=scale}.defaultMinSize(minHeight=52.dp).glassSurface().clickable(enabled=enabled,interactionSource=interaction,indication=null,onClick=onClick).padding(horizontal=20.dp,vertical=14.dp),contentAlignment=androidx.compose.ui.Alignment.Center){Text(label,color=if(enabled)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.labelLarge)}
}
@Composable fun GlassBottomBar(selected:Int,onSelect:(Int)->Unit) {
 GlassCard(Modifier.padding(horizontal=16.dp,vertical=10.dp).fillMaxWidth()){Row(Modifier.padding(6.dp),horizontalArrangement=Arrangement.SpaceEvenly){listOf("Tổng quan","Giao dịch","Cài đặt").forEachIndexed {i,label->val alpha by animateFloatAsState(if(selected==i)1f else .68f,label="tab");TextButton(onClick={onSelect(i)},modifier=Modifier.weight(1f).heightIn(min=48.dp).alpha(alpha),colors=ButtonDefaults.textButtonColors(containerColor=if(selected==i)MaterialTheme.colorScheme.primary.copy(alpha=.12f) else Color.Transparent)){Text(label,maxLines=2)}}}}
}
@Composable fun GlassDialog(onDismiss:()->Unit,content:@Composable ColumnScope.()->Unit) {Dialog(onDismissRequest=onDismiss){GlassCard(Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp),content=content)}}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun GlassBottomSheet(onDismiss:()->Unit,content:@Composable ColumnScope.()->Unit) {ModalBottomSheet(onDismissRequest=onDismiss,containerColor=MaterialTheme.colorScheme.surface.copy(alpha=.96f),content=content)}
@Composable fun AmountInput(value:String,onValue:(String)->Unit) {OutlinedTextField(value=value,onValueChange=onValue,modifier=Modifier.fillMaxWidth(),label={Text("Số tiền")},suffix={Text("₫")},textStyle=MaterialTheme.typography.headlineLarge,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=androidx.compose.ui.text.input.KeyboardType.Number),singleLine=true)}

