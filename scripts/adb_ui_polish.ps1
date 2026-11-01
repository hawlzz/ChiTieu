param([string]$Serial='emulator-5554',[string[]]$Names=@(),[switch]$SkipInstall,[switch]$SkipSeed)
$ErrorActionPreference='Stop'
if(-not $Serial.StartsWith('emulator-')){throw 'Kịch bản này chỉ dành cho máy ảo.'}
$project=Split-Path $PSScriptRoot -Parent
$adb='D:\ChiTieu\tools\sdk\platform-tools\adb.exe'
function Adb {& $adb -s $Serial @args;if($LASTEXITCODE -ne 0){throw "ADB failed: $args"}}
if(-not ((& $adb devices) -match ([regex]::Escape($Serial)+'\s+device\s*$'))){throw 'Không có máy ảo ở trạng thái device.'}
$out=Join-Path $project 'screenshots\ui-pro-max-2026-11-01\after'
New-Item -ItemType Directory -Force -Path $out | Out-Null
$font=(Adb shell settings get system font_scale).Trim()
$originalSize=(Adb shell wm size | Select-String '^Override size:' | ForEach-Object {$_.Line})
$restoreSize=if($originalSize){($originalSize -split ':')[1].Trim()}else{'reset'}
function Harness([string]$Action,[string[]]$Extra=@()){
 for($attempt=0;$attempt -lt 3;$attempt++){
  $run='ui-'+[Guid]::NewGuid().ToString('N').Substring(0,10)
  Adb shell am force-stop vn.chitieu.debug
  Adb shell am start -W -n vn.chitieu.debug/vn.chitieu.debug.HarnessActivity --es action $Action --es run $run @Extra | Out-Null
  for($i=0;$i -lt 80;$i++){
   & $adb -s $Serial shell run-as vn.chitieu.debug test -f "files/test-results/$run.json" 2>$null
   if($LASTEXITCODE -eq 0){
    $json=(Adb shell run-as vn.chitieu.debug cat "files/test-results/$run.json") -join "`n"
    if(($json|ConvertFrom-Json).error){throw "Harness failed: $Action"}
    return
   }
   Start-Sleep -Milliseconds 250
  }
 }
 throw "Harness timeout: $Action"
}
function Open([string]$Screen){Adb shell am force-stop vn.chitieu.debug;Adb shell am start -W -n vn.chitieu.debug/vn.chitieu.MainActivity --es test_screen $Screen | Out-Null;Start-Sleep -Milliseconds 1200}
function FreshTree([string]$Expected=''){
 for($attempt=0;$attempt -lt 10;$attempt++){
  Adb shell rm -f /sdcard/chitieu-ui-polish.xml | Out-Null
  Adb shell uiautomator dump /sdcard/chitieu-ui-polish.xml | Out-Null
  & $adb -s $Serial shell test -s /sdcard/chitieu-ui-polish.xml 2>$null
  if($LASTEXITCODE -ne 0){Start-Sleep -Milliseconds 400;continue}
  try{[xml]$tree=(Adb exec-out cat /sdcard/chitieu-ui-polish.xml) -join "`n"}catch{continue}
  $dimensions=(Adb shell wm size | Select-Object -Last 1)
  if($dimensions -notmatch '(\d+)x(\d+)'){throw 'Unknown viewport'}
  $w=[int]$matches[1];$h=[int]$matches[2];$valid=$true
  foreach($node in $tree.SelectNodes('//node')){
   $coords=@([regex]::Matches($node.bounds,'-?\d+') | ForEach-Object {[int]$_.Value})
   if($coords.Count -eq 4 -and ($coords[0] -lt 0 -or $coords[1] -lt 0 -or $coords[2] -gt $w -or $coords[3] -gt $h)){$valid=$false;break}
  }
  if($Expected){$head=@($tree.SelectNodes("//node[@text='$Expected']") | Where-Object {$coords=@([regex]::Matches($_.bounds,'\d+')|ForEach-Object {[int]$_.Value});$coords.Count -eq 4 -and $coords[1] -lt [Math]::Min(600,$h*.3)});if($head.Count -eq 0){$valid=$false}}
  if($valid -and $tree.SelectNodes('//node').Count -gt 0){return $tree}
  Start-Sleep -Milliseconds 400
 }
 throw "No fresh UI hierarchy for $Expected"
}
function Capture([string]$Name){
 $expected=switch(($Name -split '-')[0]){'ledger'{'Tổng quan'};'transactions'{'Giao dịch'};'settings'{'Cài đặt'};'statistics'{'Thống kê'};'pin'{'Tạo mã PIN'}}
 Adb shell cmd statusbar collapse | Out-Null
 $null=FreshTree $expected
 Start-Sleep -Milliseconds 400
 Adb shell screencap -p /sdcard/chitieu-ui-polish.png
 Adb pull /sdcard/chitieu-ui-polish.png (Join-Path $out "$Name.png") | Out-Null
 Adb pull /sdcard/chitieu-ui-polish.xml (Join-Path $out "$Name.xml") | Out-Null
 Write-Output "Captured $Name"
}
function TapText([string]$Text){
 for($attempt=0;$attempt -lt 5;$attempt++){
  [xml]$tree=FreshTree
  $node=$tree.SelectSingleNode("//node[contains(@text,'$Text')]")
  if($null -eq $node){Start-Sleep -Milliseconds 500;continue}
  if($node.bounds -notmatch '\[(\d+),(\d+)\]\[(\d+),(\d+)\]'){throw 'Invalid bounds'}
  Adb shell input tap ([int](($matches[1]-as[int])+($matches[3]-as[int]))/2) ([int](($matches[2]-as[int])+($matches[4]-as[int]))/2)
  Start-Sleep -Milliseconds 500
  return $true
 }
 return $false
}
try {
 if(-not $SkipInstall){Adb install -r (Join-Path $project 'app\build\outputs\apk\debug\app-debug.apk') | Out-Null}
 Adb shell input keyevent KEYCODE_WAKEUP
 Adb shell wm dismiss-keyguard
 if(-not $SkipSeed){
  Adb shell settings put system font_scale 1.0
  Harness 'onboarding';Harness 'clear';Harness 'theme' @('--es','value','light')
  Open 'ledger';Capture 'ledger-empty-light'
  Harness 'seed-statistics'
 }
 $matrix=@()
 foreach($theme in @('light','dark')){
  foreach($screen in @('ledger','transactions','settings','statistics')){$matrix+=@{name="$screen-$theme";screen=$screen;theme=$theme;font='1.0';size=$restoreSize;pin=$false}}
  $matrix+=@{name="pin-create-$theme";screen='settings';theme=$theme;font='1.0';size=$restoreSize;pin=$true}
  foreach($screen in @('ledger','settings','statistics')){$matrix+=@{name="$screen-small-font130-$theme";screen=$screen;theme=$theme;font='1.3';size='984x2132';pin=$false}}
  $matrix+=@{name="settings-landscape-$theme";screen='settings';theme=$theme;font='1.0';size='2132x984';pin=$false}
  $matrix+=@{name="statistics-tablet-$theme";screen='statistics';theme=$theme;font='1.0';size='2100x2400';pin=$false}
 }
 foreach($item in $matrix){
  if($Names.Count -gt 0 -and $item.name -notin $Names){continue}
  Adb shell wm size $item.size
  Adb shell settings put system font_scale $item.font
  Harness 'theme' @('--es','value',$item.theme)
  Harness 'effects' @('--ez','reduced',$(if($item.font -eq '1.3'){'true'}else{'false'}))
  Open $item.screen
  if($item.pin -and (-not (TapText 'Khoá ứng dụng') -or -not (TapText 'Bật khoá ứng dụng'))){throw 'Cannot reach PIN creation'}
  Capture $item.name
 }
} finally {
 Adb shell settings put system font_scale $font
 Adb shell wm size $restoreSize
}
Harness 'theme' @('--es','value','system')
Open 'ledger'
Write-Output 'Restored emulator display/font; only synthetic debug data seeded.'