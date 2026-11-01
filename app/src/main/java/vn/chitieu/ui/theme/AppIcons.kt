package vn.chitieu.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Lucide native vectors, ISC; notice in res/raw/lucide_license.txt. Parsed once per glyph. */
object AppIcons {
 private fun glyph(name:String,mirror:Boolean,vararg paths:String)=ImageVector.Builder(
  name=name,defaultWidth=24.dp,defaultHeight=24.dp,viewportWidth=24f,viewportHeight=24f,autoMirror=mirror
 ).apply {paths.forEach {data->addPath(pathData=PathParser().parsePathString(data).toNodes(),fill=null,
  stroke=SolidColor(Color.Black),strokeLineWidth=2f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)}}.build()
 val Home:ImageVector by lazy { glyph("Home", false, "M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8", "M3 10a2 2 0 0 1 .709-1.528l7-6a2 2 0 0 1 2.582 0l7 6A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z") }
 val Transactions:ImageVector by lazy { glyph("Transactions", true, "m16 3 4 4-4 4", "M20 7H4", "m8 21-4-4 4-4", "M4 17h16") }
 val ReceiptLong:ImageVector by lazy { glyph("ReceiptLong", false, "M13 16H8", "M14 8H8", "M16 12H8", "M4 3a1 1 0 0 1 1-1 1.3 1.3 0 0 1 .7.2l.933.6a1.3 1.3 0 0 0 1.4 0l.934-.6a1.3 1.3 0 0 1 1.4 0l.933.6a1.3 1.3 0 0 0 1.4 0l.933-.6a1.3 1.3 0 0 1 1.4 0l.934.6a1.3 1.3 0 0 0 1.4 0l.933-.6A1.3 1.3 0 0 1 19 2a1 1 0 0 1 1 1v18a1 1 0 0 1-1 1 1.3 1.3 0 0 1-.7-.2l-.933-.6a1.3 1.3 0 0 0-1.4 0l-.934.6a1.3 1.3 0 0 1-1.4 0l-.933-.6a1.3 1.3 0 0 0-1.4 0l-.933.6a1.3 1.3 0 0 1-1.4 0l-.934-.6a1.3 1.3 0 0 0-1.4 0l-.933.6a1.3 1.3 0 0 1-.7.2 1 1 0 0 1-1-1z") }
 val BarChart:ImageVector by lazy { glyph("BarChart", false, "M5 21v-6", "M12 21V9", "M19 21V3") }
 val Settings:ImageVector by lazy { glyph("Settings", false, "M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915", "M9.0 12.0a3.0 3.0 0 1 0 6.0 0a3.0 3.0 0 1 0 -6.0 0") }
 val ChevronLeft:ImageVector by lazy { glyph("ChevronLeft", true, "m15 18-6-6 6-6") }
 val ChevronRight:ImageVector by lazy { glyph("ChevronRight", true, "m9 18 6-6-6-6") }
 val Add:ImageVector by lazy { glyph("Add", false, "M5 12h14", "M12 5v14") }
 val Backspace:ImageVector by lazy { glyph("Backspace", false, "M10 5a2 2 0 0 0-1.344.519l-6.328 5.74a1 1 0 0 0 0 1.481l6.328 5.741A2 2 0 0 0 10 19h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2z", "m12 9 6 6", "m18 9-6 6") }
 val Image:ImageVector by lazy { glyph("Image", false, "M5.0 3.0H19.0Q21.0 3.0 21.0 5.0V19.0Q21.0 21.0 19.0 21.0H5.0Q3.0 21.0 3.0 19.0V5.0Q3.0 3.0 5.0 3.0Z", "M7.0 9.0a2.0 2.0 0 1 0 4.0 0a2.0 2.0 0 1 0 -4.0 0", "m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21") }
 val Info:ImageVector by lazy { glyph("Info", false, "M2.0 12.0a10.0 10.0 0 1 0 20.0 0a10.0 10.0 0 1 0 -20.0 0", "M12 16v-4", "M12 8h.01") }
 val NorthEast:ImageVector by lazy { glyph("NorthEast", false, "M7 7h10v10", "M7 17 17 7") }
 val SouthWest:ImageVector by lazy { glyph("SouthWest", false, "M17 7 7 17", "M17 17H7V7") }
 val QrCodeScanner:ImageVector by lazy { glyph("QrCodeScanner", false, "M3 7V5a2 2 0 0 1 2-2h2", "M17 3h2a2 2 0 0 1 2 2v2", "M21 17v2a2 2 0 0 1-2 2h-2", "M7 21H5a2 2 0 0 1-2-2v-2", "M7 12h10") }
 val Close:ImageVector by lazy { glyph("Close", false, "M18 6 6 18", "m6 6 12 12") }
 val FlashOff:ImageVector by lazy { glyph("FlashOff", false, "M11.652 6H18", "M12 13v1", "M16 16v4a2 2 0 0 1-2 2h-4a2 2 0 0 1-2-2v-8a4 4 0 0 0-.8-2.4l-.6-.8A3 3 0 0 1 6 7V6", "m2 2 20 20", "M7.649 2H17a1 1 0 0 1 1 1v4a3 3 0 0 1-.6 1.8l-.6.8a4 4 0 0 0-.55 1.007") }
 val FlashOn:ImageVector by lazy { glyph("FlashOn", false, "M12 13v1", "M17 2a1 1 0 0 1 1 1v4a3 3 0 0 1-.6 1.8l-.6.8A4 4 0 0 0 16 12v8a2 2 0 0 1-2 2H10a2 2 0 0 1-2-2v-8a4 4 0 0 0-.8-2.4l-.6-.8A3 3 0 0 1 6 7V3a1 1 0 0 1 1-1z", "M6 6h12") }
 val CheckCircle:ImageVector by lazy { glyph("CheckCircle", false, "M2.0 12.0a10.0 10.0 0 1 0 20.0 0a10.0 10.0 0 1 0 -20.0 0", "m9 12 2 2 4-4") }
 val Fingerprint:ImageVector by lazy { glyph("Fingerprint", false, "M12 10a2 2 0 0 0-2 2c0 1.02-.1 2.51-.26 4", "M14 13.12c0 2.38 0 6.38-1 8.88", "M17.29 21.02c.12-.6.43-2.3.5-3.02", "M2 12a10 10 0 0 1 18-6", "M2 16h.01", "M21.8 16c.2-2 .131-5.354 0-6", "M5 19.5C5.5 18 6 15 6 12a6 6 0 0 1 .34-2", "M8.65 22c.21-.66.45-1.32.57-2", "M9 6.8a6 6 0 0 1 9 5.2v2") }
 val Lock:ImageVector by lazy { glyph("Lock", false, "M11.0 16.0a1.0 1.0 0 1 0 2.0 0a1.0 1.0 0 1 0 -2.0 0", "M5.0 10.0H19.0Q21.0 10.0 21.0 12.0V20.0Q21.0 22.0 19.0 22.0H5.0Q3.0 22.0 3.0 20.0V12.0Q3.0 10.0 5.0 10.0Z", "M7 10V7a5 5 0 0 1 10 0v3") }
 val Password:ImageVector by lazy { glyph("Password", false, "M2.586 17.414A2 2 0 0 0 2 18.828V21a1 1 0 0 0 1 1h3a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h1a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h.172a2 2 0 0 0 1.414-.586l.814-.814a6.5 6.5 0 1 0-4-4z", "M16.0 7.5a0.5 0.5 0 1 0 1.0 0a0.5 0.5 0 1 0 -1.0 0") }
 val Timer:ImageVector by lazy { glyph("Timer", false, "M10 2L14 2", "M12 14L15 11", "M4.0 14.0a8.0 8.0 0 1 0 16.0 0a8.0 8.0 0 1 0 -16.0 0") }
 val VisibilityOff:ImageVector by lazy { glyph("VisibilityOff", false, "M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49", "M14.084 14.158a3 3 0 0 1-4.242-4.242", "M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143", "m2 2 20 20") }
 val Warning:ImageVector by lazy { glyph("Warning", false, "m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3", "M12 9v4", "M12 17h.01") }
 val AccountBalance:ImageVector by lazy { glyph("AccountBalance", false, "M10 18v-7", "M11.12 2.198a2 2 0 0 1 1.76.006l7.866 3.847c.476.233.31.949-.22.949H3.474c-.53 0-.695-.716-.22-.949z", "M14 18v-7", "M18 18v-7", "M3 22h18", "M6 18v-7") }
 val AccountBalanceWallet:ImageVector by lazy { glyph("AccountBalanceWallet", false, "M19 7V4a1 1 0 0 0-1-1H5a2 2 0 0 0 0 4h15a1 1 0 0 1 1 1v4h-3a2 2 0 0 0 0 4h3a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1", "M3 5v14a2 2 0 0 0 2 2h15a1 1 0 0 0 1-1v-4") }
 val AutoFixHigh:ImageVector by lazy { glyph("AutoFixHigh", false, "M2 5h20", "M6 12h12", "M9 19h6") }
 val Backup:ImageVector by lazy { glyph("Backup", false, "M3.0 3.0H21.0Q22.0 3.0 22.0 4.0V7.0Q22.0 8.0 21.0 8.0H3.0Q2.0 8.0 2.0 7.0V4.0Q2.0 3.0 3.0 3.0Z", "M4 8v11a2 2 0 0 0 2 2h2", "M20 8v11a2 2 0 0 1-2 2h-2", "m9 15 3-3 3 3", "M12 12v9") }
 val BatteryFull:ImageVector by lazy { glyph("BatteryFull", false, "M10 10v4", "M14 10v4", "M22 14v-4", "M6 10v4", "M4.0 6.0H16.0Q18.0 6.0 18.0 8.0V16.0Q18.0 18.0 16.0 18.0H4.0Q2.0 18.0 2.0 16.0V8.0Q2.0 6.0 4.0 6.0Z") }
 val BlurOff:ImageVector by lazy { glyph("BlurOff", false, "M13 13.74a2 2 0 0 1-2 0L2.5 8.87a1 1 0 0 1 0-1.74L11 2.26a2 2 0 0 1 2 0l8.5 4.87a1 1 0 0 1 0 1.74z", "m20 14.285 1.5.845a1 1 0 0 1 0 1.74L13 21.74a2 2 0 0 1-2 0l-8.5-4.87a1 1 0 0 1 0-1.74l1.5-.845") }
 val Category:ImageVector by lazy { glyph("Category", false, "M8.3 10a.7.7 0 0 1-.626-1.079L11.4 3a.7.7 0 0 1 1.198-.043L16.3 8.9a.7.7 0 0 1-.572 1.1Z", "M4.0 14.0H9.0Q10.0 14.0 10.0 15.0V20.0Q10.0 21.0 9.0 21.0H4.0Q3.0 21.0 3.0 20.0V15.0Q3.0 14.0 4.0 14.0Z", "M14.0 17.5a3.5 3.5 0 1 0 7.0 0a3.5 3.5 0 1 0 -7.0 0") }
 val DeleteForever:ImageVector by lazy { glyph("DeleteForever", false, "M10 11v6", "M14 11v6", "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6", "M3 6h18", "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2") }
 val Description:ImageVector by lazy { glyph("Description", false, "M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z", "M14 2v5a1 1 0 0 0 1 1h5", "M10 9H8", "M16 13H8", "M16 17H8") }
 val FileDownload:ImageVector by lazy { glyph("FileDownload", false, "M4 11V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.706.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-1", "M14 2v5a1 1 0 0 0 1 1h5", "M2 15h10", "m9 18 3-3-3-3") }
 val FileUpload:ImageVector by lazy { glyph("FileUpload", false, "M4.226 20.925A2 2 0 0 0 6 22h12a2 2 0 0 0 2-2V8a2.4 2.4 0 0 0-.706-1.706l-3.588-3.588A2.4 2.4 0 0 0 14 2H6a2 2 0 0 0-2 2v3.127", "M14 2v5a1 1 0 0 0 1 1h5", "m5 11-3 3", "m5 17-3-3h10") }
 val History:ImageVector by lazy { glyph("History", false, "M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8", "M3 3v5h5", "M12 7v5l4 2") }
 val Notifications:ImageVector by lazy { glyph("Notifications", false, "M10.268 21a2 2 0 0 0 3.464 0", "M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326") }
 val Palette:ImageVector by lazy { glyph("Palette", false, "M12 22a1 1 0 0 1 0-20 10 9 0 0 1 10 9 5 5 0 0 1-5 5h-2.25a1.75 1.75 0 0 0-1.4 2.8l.3.4a1.75 1.75 0 0 1-1.4 2.8z", "M13.0 6.5a0.5 0.5 0 1 0 1.0 0a0.5 0.5 0 1 0 -1.0 0", "M17.0 10.5a0.5 0.5 0 1 0 1.0 0a0.5 0.5 0 1 0 -1.0 0", "M6.0 12.5a0.5 0.5 0 1 0 1.0 0a0.5 0.5 0 1 0 -1.0 0", "M8.0 7.5a0.5 0.5 0 1 0 1.0 0a0.5 0.5 0 1 0 -1.0 0") }
 val PlaylistAdd:ImageVector by lazy { glyph("PlaylistAdd", false, "M16 5H3", "M11 12H3", "M16 19H3", "M18 9v6", "M21 12h-6") }
 val PrivacyTip:ImageVector by lazy { glyph("PrivacyTip", false, "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z", "m9 12 2 2 4-4") }
 val Screenshot:ImageVector by lazy { glyph("Screenshot", false, "M3 7V5a2 2 0 0 1 2-2h2", "M17 3h2a2 2 0 0 1 2 2v2", "M21 17v2a2 2 0 0 1-2 2h-2", "M7 21H5a2 2 0 0 1-2-2v-2") }
 val SettingsSuggest:ImageVector by lazy { glyph("SettingsSuggest", false, "M14 17H5", "M19 7h-9", "M14.0 17.0a3.0 3.0 0 1 0 6.0 0a3.0 3.0 0 1 0 -6.0 0", "M4.0 7.0a3.0 3.0 0 1 0 6.0 0a3.0 3.0 0 1 0 -6.0 0") }
 val Sms:ImageVector by lazy { glyph("Sms", false, "M22 17a2 2 0 0 1-2 2H6.828a2 2 0 0 0-1.414.586l-2.202 2.202A.71.71 0 0 1 2 21.286V5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2z") }
 val Tune:ImageVector by lazy { glyph("Tune", false, "M10 5H3", "M12 19H3", "M14 3v4", "M16 17v4", "M21 12h-9", "M21 19h-5", "M21 5h-7", "M8 10v4", "M8 12H3") }
 val WavingHand:ImageVector by lazy { glyph("WavingHand", false, "M11.017 2.814a1 1 0 0 1 1.966 0l1.051 5.558a2 2 0 0 0 1.594 1.594l5.558 1.051a1 1 0 0 1 0 1.966l-5.558 1.051a2 2 0 0 0-1.594 1.594l-1.051 5.558a1 1 0 0 1-1.966 0l-1.051-5.558a2 2 0 0 0-1.594-1.594l-5.558-1.051a1 1 0 0 1 0-1.966l5.558-1.051a2 2 0 0 0 1.594-1.594z", "M20 2v4", "M22 4h-4", "M2.0 20.0a2.0 2.0 0 1 0 4.0 0a2.0 2.0 0 1 0 -4.0 0") }
 val CardGiftcard:ImageVector by lazy { glyph("CardGiftcard", false, "M12 7v14", "M20 11v8a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-8", "M7.5 7a1 1 0 0 1 0-5A4.8 8 0 0 1 12 7a4.8 8 0 0 1 4.5-5 1 1 0 0 1 0 5", "M4.0 7.0H20.0Q21.0 7.0 21.0 8.0V10.0Q21.0 11.0 20.0 11.0H4.0Q3.0 11.0 3.0 10.0V8.0Q3.0 7.0 4.0 7.0Z") }
 val DirectionsCar:ImageVector by lazy { glyph("DirectionsCar", false, "M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2", "M5.0 17.0a2.0 2.0 0 1 0 4.0 0a2.0 2.0 0 1 0 -4.0 0", "M9 17h6", "M15.0 17.0a2.0 2.0 0 1 0 4.0 0a2.0 2.0 0 1 0 -4.0 0") }
 val Favorite:ImageVector by lazy { glyph("Favorite", false, "M2 9.5a5.5 5.5 0 0 1 9.591-3.676.56.56 0 0 0 .818 0A5.49 5.49 0 0 1 22 9.5c0 2.29-1.5 4-3 5.5l-5.492 5.313a2 2 0 0 1-3 .019L5 15c-1.5-1.5-3-3.2-3-5.5", "M3.22 13H9.5l.5-1 2 4.5 2-7 1.5 3.5h5.27") }
 val Restaurant:ImageVector by lazy { glyph("Restaurant", false, "M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2", "M7 2v20", "M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7") }
 val School:ImageVector by lazy { glyph("School", false, "M21.42 10.922a1 1 0 0 0-.019-1.838L12.83 5.18a2 2 0 0 0-1.66 0L2.6 9.08a1 1 0 0 0 0 1.832l8.57 3.908a2 2 0 0 0 1.66 0z", "M22 10v6", "M6 12.5V16a6 3 0 0 0 12 0v-3.5") }
 val Search:ImageVector by lazy { glyph("Search", false, "m21 21-4.34-4.34", "M3.0 11.0a8.0 8.0 0 1 0 16.0 0a8.0 8.0 0 1 0 -16.0 0") }
 val ShoppingBag:ImageVector by lazy { glyph("ShoppingBag", false, "M16 10a4 4 0 0 1-8 0", "M3.103 6.034h17.794", "M3.4 5.467a2 2 0 0 0-.4 1.2V20a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6.667a2 2 0 0 0-.4-1.2l-2-2.667A2 2 0 0 0 17 2H7a2 2 0 0 0-1.6.8z") }
 val SportsEsports:ImageVector by lazy { glyph("SportsEsports", false, "M6 11L10 11", "M8 9L8 13", "M15 12L15.01 12", "M18 10L18.01 10", "M17.32 5H6.68a4 4 0 0 0-3.978 3.59c-.006.052-.01.101-.017.152C2.604 9.416 2 14.456 2 16a3 3 0 0 0 3 3c1 0 1.5-.5 2-1l1.414-1.414A2 2 0 0 1 9.828 16h4.344a2 2 0 0 1 1.414.586L17 18c.5.5 1 1 2 1a3 3 0 0 0 3-3c0-1.545-.604-6.584-.685-7.258-.007-.05-.011-.1-.017-.151A4 4 0 0 0 17.32 5z") }
 val Work:ImageVector by lazy { glyph("Work", false, "M12 12h.01", "M16 6V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2", "M22 13a18.15 18.15 0 0 1-20 0", "M4.0 6.0H20.0Q22.0 6.0 22.0 8.0V18.0Q22.0 20.0 20.0 20.0H4.0Q2.0 20.0 2.0 18.0V8.0Q2.0 6.0 4.0 6.0Z") }
 val RadioButtonUnchecked:ImageVector by lazy { glyph("RadioButtonUnchecked", false, "M2.0 12.0a10.0 10.0 0 1 0 20.0 0a10.0 10.0 0 1 0 -20.0 0") }
 val Shield:ImageVector by lazy { glyph("Shield", false, "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z", "m9 12 2 2 4-4") }
 fun category(name:String):ImageVector=when(java.text.Normalizer.normalize(name,java.text.Normalizer.Form.NFC).trim().lowercase(java.util.Locale.ROOT)) {
  "ăn uống"->Restaurant;"di chuyển","đi lại"->DirectionsCar;"nhà ở"->Home;"hoá đơn","hóa đơn"->ReceiptLong
  "mua sắm"->ShoppingBag;"giải trí"->SportsEsports;"sức khoẻ","sức khỏe"->Favorite;"giáo dục"->School
  "lương"->Work;"thưởng"->CardGiftcard;else->Category
 }
}
