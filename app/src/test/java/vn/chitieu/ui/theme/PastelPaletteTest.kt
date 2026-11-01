package vn.chitieu.ui.theme
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import org.junit.Assert.*
import org.junit.Test
class PastelPaletteTest {
 private fun ratio(a:Color,b:Color):Float=(maxOf(a.luminance(),b.luminance())+.05f)/(minOf(a.luminance(),b.luminance())+.05f)
 private fun check(dark:Boolean){val c=pastelColorScheme(dark);for((name,a,b) in listOf(
  Triple("body",c.onSurface,c.surface),Triple("secondary",c.onSurfaceVariant,c.surface),Triple("secondary on background",c.onSurfaceVariant,c.background),
  Triple("links",c.primary,c.surface),Triple("native button",c.onPrimary,c.primary),Triple("pastel action",c.onPrimaryContainer,c.primaryContainer)
 ))assertTrue("$name dark=$dark ratio=${ratio(a,b)}",ratio(a,b)>=4.5f)}
 @Test fun lightRolesStayReadable(){check(false)}
 @Test fun darkRolesStayReadable(){check(true)}
 @Test fun brightCustomCategoryRemainsVisible(){for(dark in listOf(false,true)){val c=pastelColorScheme(dark);val bg=lerp(c.surface,c.primaryContainer,.28f);for(proposed in listOf(Color.Yellow,Color.Cyan,Color.White,Color.Black)){assertTrue(ratio(readableIconTint(proposed,bg,c.onSurface),bg)>=3f)}}}
 @Test fun categoryNamesDoNotInferUnrelatedWalletsOrCars(){assertSame(AppIcons.ReceiptLong,AppIcons.category("Hóa đơn"));assertSame(AppIcons.ReceiptLong,AppIcons.category("Hoá đơn"));assertSame(AppIcons.DirectionsCar,AppIcons.category("Di chuyển"));assertSame(AppIcons.Category,AppIcons.category("Điện thoại mới"));assertSame(AppIcons.Category,AppIcons.category("Ăn thử món mới"))}
 @Test fun bundledVectorsParseAtNativeSize(){val getters=AppIcons::class.java.methods.filter {it.name.startsWith("get")&&it.returnType==ImageVector::class.java&&it.parameterCount==0};assertTrue(getters.size>40);getters.forEach {val vector=it.invoke(AppIcons) as ImageVector;assertEquals(24f,vector.viewportWidth);assertEquals(24f,vector.viewportHeight)}}
}
