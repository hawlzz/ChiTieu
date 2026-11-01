package vn.chitieu.ui
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import vn.chitieu.ui.theme.*

@Composable fun WelcomeScreen(onFinish:(Boolean)->Unit){
 val pager=rememberPagerState {3};val scope=rememberCoroutineScope();val reduced=LocalReduceMotion.current
 val titles=listOf("Chào mừng đến ChiTieu","Quét, chuyển, ghi chép","Riêng tư trên thiết bị")
 val descriptions=listOf("Mọi khoản tiền, rõ ràng hơn.","Quét VietQR, mở ngân hàng và quay lại xác nhận khoản chi.","Dữ liệu chỉ nằm trên máy. Không tài khoản, không quảng cáo.")
 val icons=listOf(AppIcons.AccountBalanceWallet,AppIcons.QrCodeScanner,AppIcons.Shield)
 Column(Modifier.fillMaxSize().safeDrawingPadding().padding(GlassTokens.margin),horizontalAlignment=Alignment.CenterHorizontally){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){TextButton(onClick={onFinish(false)}){Text("Bỏ qua")}}
  HorizontalPager(pager,Modifier.weight(1f)){page->Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical=32.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(24.dp)){
   if(page==0)BrandMark(Modifier.size(PastelTokens.welcome)) else FrostedIconBadge(icons[page],size=PastelTokens.welcome,glyphSize=PastelTokens.welcomeGlyph)
   Text(titles[page],style=MaterialTheme.typography.headlineLarge,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
   Text(descriptions[page],style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
  }}
  Row(Modifier.padding(20.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){repeat(3){i->Box(Modifier.size(if(i==pager.currentPage)10.dp else 7.dp).clip(CircleShape).background(if(i==pager.currentPage)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant))}}
  PillButton(if(pager.currentPage==2)"Bắt đầu" else "Tiếp tục",{if(pager.currentPage==2)onFinish(false) else scope.launch {if(reduced)pager.scrollToPage(pager.currentPage+1) else pager.animateScrollToPage(pager.currentPage+1)}},Modifier.fillMaxWidth())
  if(pager.currentPage==2)TextButton(onClick={onFinish(true)}){Text("Thiết lập thông báo ngân hàng")}
  Spacer(Modifier.height(20.dp))
 }
}
