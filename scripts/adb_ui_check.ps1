param([string]$Serial='emulator-5554',[switch]$SkipInstall,[switch]$SkipTests)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path $PSScriptRoot -Parent
$adb=if($env:ANDROID_HOME){Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'}else{'D:\ChiTieu\tools\sdk\platform-tools\adb.exe'}
function Adb { & $adb -s $Serial @args; if($LASTEXITCODE -ne 0){throw "ADB failed: $args"} }
$devices=& $adb devices
if(-not ($devices -match ([regex]::Escape($Serial)+'\s+device\s*$'))){throw 'Không có thiết bị ở trạng thái device; dừng test trên máy.'}
$out=Join-Path $projectRoot 'screenshots\pin-settings-statistics\after'
New-Item -ItemType Directory -Force $out | Out-Null
$font=(Adb shell settings get system font_scale).Trim()
$night=(Adb shell settings get secure ui_night_mode).Trim()
function Capture([string]$name){Adb shell cmd statusbar collapse;Start-Sleep -Milliseconds 300;Adb shell screencap -p /sdcard/chitieu-ui.png;Adb pull /sdcard/chitieu-ui.png (Join-Path $out ($name+'.png'));Adb shell uiautomator dump /sdcard/chitieu-ui.xml;Adb pull /sdcard/chitieu-ui.xml (Join-Path $out ($name+'.xml'))}
try {
 Adb shell input keyevent KEYCODE_WAKEUP
 Adb shell wm dismiss-keyguard
 Adb shell cmd statusbar collapse
 if(-not $SkipInstall){Adb install -r (Join-Path $projectRoot 'app\build\outputs\apk\debug\app-debug.apk');Adb install -r -t (Join-Path $projectRoot 'app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk')}
 if(-not $SkipTests){$testOutput=Adb shell am instrument -w -r -e class vn.chitieu.ui.SecurityStatisticsUiTest vn.chitieu.debug.test/androidx.test.runner.AndroidJUnitRunner;$testOutput | Tee-Object (Join-Path $out 'instrumentation.txt');if(($testOutput -join "`n") -notmatch 'OK \(\d+ tests?\)'){throw 'Kiểm thử giao diện chưa đạt; xem instrumentation.txt'}}
 Adb shell am start -n vn.chitieu.debug/vn.chitieu.debug.HarnessActivity --es action seed-statistics --es run statistics-seed
 Start-Sleep -Seconds 2
 foreach($theme in @('light','dark')){
  Adb shell am start -n vn.chitieu.debug/vn.chitieu.debug.HarnessActivity --es action theme --es value $theme --es run ('theme-'+$theme)
  foreach($screen in @('settings','statistics')){
   Adb shell cmd statusbar collapse
   Adb shell am start -W -n vn.chitieu.debug/vn.chitieu.MainActivity --es test_screen $screen
   Start-Sleep -Seconds 2
   $focus=(Adb shell dumpsys window | Select-String 'mCurrentFocus') -join " "
   if($focus -notmatch 'vn\.chitieu\.debug'){throw "Ứng dụng chưa ở trước màn hình: $focus"}
   Capture ($screen+'-'+$theme)
   Adb shell dumpsys gfxinfo vn.chitieu.debug reset | Out-Null
   1..5 | ForEach-Object {Adb shell input swipe 500 1600 500 450 500;Adb shell input swipe 500 500 500 1600 500}
   Adb shell dumpsys gfxinfo vn.chitieu.debug | Set-Content (Join-Path $out ($screen+'-'+$theme+'-gfxinfo.txt'))
   Adb shell settings put system font_scale 1.3
   Start-Sleep -Seconds 1
   Capture ($screen+'-'+$theme+'-font130')
   Adb shell settings put system font_scale $font
  }
 }
 $tarPath=Join-Path $out 'security-gallery.tar'
 $startInfo=[Diagnostics.ProcessStartInfo]::new($adb)
 $startInfo.UseShellExecute=$false
 $startInfo.RedirectStandardOutput=$true
 foreach($arg in @('-s',$Serial,'exec-out','run-as','vn.chitieu.debug','tar','-C','files','-cf','-','security-gallery')){$startInfo.ArgumentList.Add($arg)}
 $process=[Diagnostics.Process]::Start($startInfo)
 $stream=[IO.File]::Create($tarPath)
 try{$process.StandardOutput.BaseStream.CopyTo($stream)}finally{$stream.Dispose()}
 $process.WaitForExit()
 if($process.ExitCode -ne 0){throw 'Không lấy được ảnh chụp nội bộ'}
 & tar -xf $tarPath -C $out
 if($LASTEXITCODE -ne 0){throw 'Không giải nén được ảnh chụp'}
 $galleryPath=Join-Path $out 'security-gallery'
 Get-ChildItem -LiteralPath $galleryPath -File | Where-Object {$_.Name -match '[├─ß]'} | ForEach-Object {
  $newName=[Text.Encoding]::UTF8.GetString([Text.Encoding]::GetEncoding(437).GetBytes($_.Name))
  if([IO.Path]::GetFileName($newName) -ne $newName){throw 'Tên ảnh không hợp lệ'}
  $destination=[IO.Path]::GetFullPath((Join-Path $galleryPath $newName))
  if(-not $destination.StartsWith([IO.Path]::GetFullPath($galleryPath)+'\')){throw 'Đường dẫn ảnh không hợp lệ'}
  Move-Item -LiteralPath $_.FullName -Destination $destination -Force
 }
} finally {
 Adb shell settings put system font_scale $font
 # Theme is a DataStore preference; return it to system mode after capture.
 Adb shell am start -n vn.chitieu.debug/vn.chitieu.debug.HarnessActivity --es action theme --es value system --es run restore-theme
 Adb shell am start -n vn.chitieu.debug/vn.chitieu.debug.HarnessActivity --es action clear --es run clear-synthetic
}
