package vn.chitieu.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*

object PastelTokens {
 val shadowCard=10.dp;val shadowControl=6.dp;val shadowBadge=5.dp;val shadowFloat=14.dp
 val edge=1.dp;val brand=72.dp;val welcome=180.dp;val welcomeGlyph=76.dp
 val brandTop=Color(0xFFE1EFF7);val brandBottom=Color(0xFFA4C3DB)
 const val enterMillis=280;const val exitMillis=140;const val pressScale=.97f
 val travel=12.dp
}
val LocalReducedEffects=staticCompositionLocalOf {false}

/** Static tonal depth for lists/controls. Only glassSurface uses Backdrop, never per row. */
@Composable fun Modifier.tonalSurface(shape:Shape=RoundedCornerShape(GlassTokens.corner),elevation:Dp=PastelTokens.shadowCard,tint:Color?=null):Modifier {
 val colors=MaterialTheme.colorScheme;val dark=colors.background.luminance()<.3f
 val base=tint?:colors.surface
 val top=if(dark)lerp(base,colors.onSurface,.025f) else lerp(base,Color.White,.30f)
 val bottom=lerp(base,colors.primaryContainer,if(tint==null).14f else .08f)
 val brush=remember(top,bottom){Brush.linearGradient(listOf(top,bottom))}
 val edge=remember(dark,colors.outlineVariant){Brush.linearGradient(listOf(if(dark)Color.White.copy(alpha=.13f) else Color.White.copy(alpha=.75f),colors.outlineVariant.copy(alpha=.3f)))}
 val shadowTint=if(dark)Color.Black else colors.primary
 return shadow(if(LocalReducedEffects.current)elevation*.45f else elevation,shape,clip=false,ambientColor=shadowTint.copy(alpha=.20f),spotColor=shadowTint.copy(alpha=.24f))
  .clip(shape).background(brush).border(PastelTokens.edge,edge,shape)
}

/** Scale is read in graphicsLayer: feedback does not resize the layout/hit target. */
@Composable fun Modifier.softPress(source:MutableInteractionSource,enabled:Boolean=true):Modifier {
 val pressed by source.collectIsPressedAsState();val reduced=LocalReduceMotion.current
 val scale by animateFloatAsState(if(pressed&&enabled&&!reduced)PastelTokens.pressScale else 1f,
  animationSpec=if(reduced)snap() else spring(dampingRatio=.72f,stiffness=Spring.StiffnessMedium),label="pastelPress")
 return graphicsLayer {scaleX=scale;scaleY=scale}
}

@Composable fun FrostedIconBadge(icon:ImageVector,modifier:Modifier=Modifier,tint:Color=MaterialTheme.colorScheme.primary,size:Dp=GlassTokens.badge,glyphSize:Dp=GlassTokens.icon) {
 val colors=MaterialTheme.colorScheme;val base=lerp(colors.surface,colors.primaryContainer,.28f)
 val ink=readableIconTint(tint,base,colors.onSurface)
 Box(modifier.size(size).tonalSurface(RoundedCornerShape(GlassTokens.inputCorner),PastelTokens.shadowBadge,base),contentAlignment=Alignment.Center){Icon(icon,null,tint=ink,modifier=Modifier.size(glyphSize))}
}
internal fun readableIconTint(proposed:Color,background:Color,ink:Color):Color {
 fun contrast(c:Color):Float {val a=c.luminance();val b=background.luminance();return (maxOf(a,b)+.05f)/(minOf(a,b)+.05f)}
 if(contrast(proposed)>=3f)return proposed
 for(step in 1..10){val candidate=lerp(proposed,ink,step/10f);if(contrast(candidate)>=3f)return candidate}
 return ink
}

@Composable fun BrandMark(modifier:Modifier=Modifier,description:String?=null) {
 Box(modifier.clip(RoundedCornerShape(23)).background(Brush.linearGradient(listOf(PastelTokens.brandTop,PastelTokens.brandBottom))),contentAlignment=Alignment.Center){
  Image(painterResource(vn.chitieu.R.drawable.ic_launcher_foreground),description,Modifier.fillMaxSize())
 }
}
