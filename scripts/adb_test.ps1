[CmdletBinding()]
param(
 [Parameter(Mandatory=$true)][ValidatePattern('^[A-Za-z0-9_.:-]+$')][string]$Serial,
 [string]$Adb='D:\ChiTieu\tools\sdk\platform-tools\adb.exe',
 [switch]$SkipInstall,
 [switch]$SkipOcr,
 [string]$DeepLinkUrl,
 [ValidatePattern('^[A-Za-z0-9_.]+$')][string]$BankPackage='vn.com.techcombank.bb.app'
)
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$pkg='vn.chitieu.debug'
$component="$pkg/vn.chitieu.debug.HarnessActivity"
$out=Join-Path $root 'local-reports'
$shots=Join-Path $root 'screenshots'
New-Item -ItemType Directory -Force $out,$shots | Out-Null
function Invoke-Adb {
 $CommandArgs=$args
 $result=& $Adb -s $Serial @CommandArgs 2>&1
 if($LASTEXITCODE -ne 0){throw "ADB failed: $($CommandArgs[0]) / $result"}
 return $result
}
function Harness([string]$Action,[string]$Run,[string[]]$Extra=@()) {
 Invoke-Adb shell am start -W -f 0x10008000 -n $component --es action $Action --es run $Run @Extra | Out-Null
}
function Result([string]$Run,[int]$TimeoutSeconds=30) {
 $deadline=[DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
 do {
  $raw=& $Adb -s $Serial exec-out run-as $pkg cat "files/test-results/$Run.json" 2>$null
  if($LASTEXITCODE -eq 0){try {$text=$raw -join "`n"; $null=ConvertFrom-Json $text -ErrorAction Stop; return $text} catch {}}
  Start-Sleep -Milliseconds 500
 } while([DateTime]::UtcNow -lt $deadline)
 throw "Timed out waiting for harness $Run"
}
function Stage-Receipt([System.IO.FileInfo]$File) {
 if($File.Name -notmatch '^receipt_[0-9]{3}\.(jpg|jpeg|png)$'){throw 'Invalid receipt alias'}
 # Write bytes directly to adb stdin; PowerShell text pipelines corrupt JPEGs.
 $start=New-Object System.Diagnostics.ProcessStartInfo
 $start.FileName=$Adb
 $start.Arguments="-s $Serial exec-in run-as $pkg sh -c `"cat > files/receipt-eval/$($File.Name)`""
 $start.UseShellExecute=$false
 $start.CreateNoWindow=$true
 $start.RedirectStandardInput=$true
 $start.RedirectStandardError=$true
 $process=[System.Diagnostics.Process]::Start($start)
 try {
  $stream=$File.OpenRead()
  try {$stream.CopyTo($process.StandardInput.BaseStream)} finally {$stream.Dispose()}
  $process.StandardInput.Close()
  $errorText=$process.StandardError.ReadToEnd()
  $process.WaitForExit()
  if($process.ExitCode -ne 0){throw "Receipt staging failed: $errorText"}
 } finally {$process.Dispose()}
}
function Screenshot([string]$Name) {
 $remote="/sdcard/chitieu-test-$Name.png"
 Invoke-Adb shell screencap -p $remote | Out-Null
 try {Invoke-Adb pull $remote (Join-Path $shots "$Name.png") | Out-Null} finally {Invoke-Adb shell rm $remote | Out-Null}
 Invoke-Adb shell uiautomator dump /sdcard/chitieu-test-ui.xml | Out-Null
 try {Invoke-Adb pull /sdcard/chitieu-test-ui.xml (Join-Path $shots "$Name.xml") | Out-Null} finally {Invoke-Adb shell rm /sdcard/chitieu-test-ui.xml | Out-Null}
}
$devices=& $Adb devices
if(-not ($devices | Where-Object {$_ -match ('^'+[regex]::Escape($Serial)+'\s+device$')})){throw 'Chosen device is not connected/authorized. Connect and unlock Xiaomi first.'}
$run='run-'+[DateTime]::UtcNow.ToString('yyyyMMddHHmmss')
if(-not $SkipInstall){Invoke-Adb install -r (Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk') | Out-Null}
Invoke-Adb shell am force-stop $pkg | Out-Null
Invoke-Adb shell am start -W -n "$pkg/vn.chitieu.MainActivity" | Set-Content (Join-Path $out "$run-start.txt")
try {Invoke-Adb shell pm grant $pkg android.permission.CAMERA | Out-Null} catch {Write-Warning 'HyperOS blocked CAMERA grant; grant in app when scanning.'}
try {Invoke-Adb shell cmd notification allow_listener "$pkg/vn.chitieu.notification.BankNotificationListener" | Out-Null} catch {Write-Warning 'HyperOS blocked listener grant; grant Notification access manually.'}
Invoke-Adb shell settings get secure enabled_notification_listeners | Set-Content (Join-Path $out "$run-listeners.txt")
Harness clear "$run-clear"
$null=Result "$run-clear"
Harness enable-shell "$run-shell"
$null=Result "$run-shell"
foreach($direction in @('OUT','IN')) {
 foreach($repeat in 1..2) {
  $body="[SIMULATED] $direction 12345 VND MEMO=TEST-$run-$direction"
  Invoke-Adb shell cmd notification post -S bigtext -t TEST "test-$run-$direction" "'$body'" | Out-Null
 }
}
Start-Sleep -Seconds 2
Harness state "$run-state"
$state=Result "$run-state" | ConvertFrom-Json
$actual=@($state | Where-Object {$_.memo -like "TEST-$run-*"})
if($actual.Count -ne 2 -or @($actual | Where-Object {$_.amount -ne 12345}).Count -ne 0){Write-Warning 'Synthetic notification dedup/amount check failed; inspect listener permission.'}
$actual | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $out "$run-notifications.json")
$staged=@()
try {
 if(-not $SkipOcr) {
  $images=@(Get-ChildItem (Join-Path $root 'testdata\receipts') -File | Where-Object {$_.Name -match '^receipt_[0-9]{3}\.(jpg|jpeg|png)$'})
  if($images.Count -eq 0){throw 'No private receipt dataset found; use -SkipOcr only when deliberately skipping.'}
  Invoke-Adb shell run-as $pkg mkdir -p files/receipt-eval | Out-Null
  foreach($image in $images){$staged+=$image.Name;Stage-Receipt $image}
  Harness evaluate "$run-ocr"
  $json=Result "$run-ocr" 300
  $evaluation=$json | ConvertFrom-Json
  if(@($evaluation.items).Count -ne $images.Count){throw 'OCR processed count differs from staged images'}
  # Raw extracted fields stay in ignored private testdata, never in console/git.
  $json | Set-Content (Join-Path $root "testdata\receipt-results-$run.json") -Encoding utf8
  Write-Host "OCR completed: $($images.Count) images. Human-verified labels required for accuracy."
 }
} finally {
 foreach($alias in $staged){Invoke-Adb shell run-as $pkg rm -f "files/receipt-eval/$alias" | Out-Null}
 if($staged.Count){Invoke-Adb shell run-as $pkg rmdir files/receipt-eval | Out-Null}
}
Harness seed "$run-seed"
$null=Result "$run-seed"
foreach($theme in @('light','dark')) {
 Harness theme "$run-$theme" @('--es','value',$theme)
 $null=Result "$run-$theme"
 foreach($screen in @('ledger','transactions','settings','advanced','receipt')) {
  Harness screen "$run-$theme-$screen" @('--es','screen',$screen)
  $null=Result "$run-$theme-$screen"
  Start-Sleep -Seconds 1
  Screenshot "$theme-$screen"
 }
}
foreach($reduced in @('false','true')) {
 Harness effects "$run-effects-$reduced" @('--ez','reduced',$reduced)
 $null=Result "$run-effects-$reduced"
 Harness screen "$run-frames-$reduced" @('--es','screen','transactions')
 Invoke-Adb shell dumpsys gfxinfo $pkg reset | Out-Null
 try {foreach($i in 1..6){Invoke-Adb shell input swipe 600 2100 600 700 450 | Out-Null}} catch {Write-Warning 'HyperOS blocked input injection; frame measurement is not a scroll benchmark.'}
 Invoke-Adb shell dumpsys gfxinfo $pkg | Set-Content (Join-Path $out "$run-gfx-reduced-$reduced.txt")
}
# Optional: supply a synthetic VietQR URL explicitly. Never click transfer confirmation.
if($DeepLinkUrl) {
 $link=[Uri]$DeepLinkUrl
 if($link.Scheme -ne 'https' -or $link.Host -ne 'dl.vietqr.io' -or $DeepLinkUrl -notmatch '^https://dl\.vietqr\.io/[A-Za-z0-9_/?%=&@.:+\-]+$'){throw 'Only a validated VietQR HTTPS test URL is accepted.'}
 try {Invoke-Adb shell am start -W -a android.intent.action.VIEW -d "'$DeepLinkUrl'" -p $BankPackage | Set-Content (Join-Path $out "$run-deeplink.txt")} catch {Write-Warning 'Bank deep link blocked/unsupported; opening is not proof of payment.'}
} else {Write-Host 'Deep link: skipped; supply -DeepLinkUrl with synthetic data for an explicitly authorized run.'}
# force-stop intentionally stops Android components. This checks recovery, not survival.
Harness clear "$run-before-background"
$null=Result "$run-before-background"
Invoke-Adb shell am force-stop $pkg | Out-Null
Invoke-Adb shell cmd notification post -S bigtext -t TEST test-stopped "'[SIMULATED] OUT 7777 VND MEMO=TEST-STOPPED'" | Out-Null
Invoke-Adb shell dumpsys notification | Set-Content (Join-Path $out "$run-background.txt")
Harness state "$run-stopped"
$stopped=@((Result "$run-stopped" | ConvertFrom-Json))
if($stopped.Count -ne 0){Write-Warning 'Unexpected synthetic entries after force-stop; inspect locally.'}
try {Invoke-Adb shell cmd notification allow_listener "$pkg/vn.chitieu.notification.BankNotificationListener" | Out-Null} catch {Write-Warning 'Listener needs manual rebind after force-stop.'}
Harness enable-shell "$run-reenable"
$null=Result "$run-reenable"
Invoke-Adb shell cmd notification post -S bigtext -t TEST test-recovered "'[SIMULATED] OUT 8888 VND MEMO=TEST-RECOVERED'" | Out-Null
Start-Sleep -Seconds 2
Harness state "$run-recovered"
$recovered=@((Result "$run-recovered" | ConvertFrom-Json))
if(@($recovered | Where-Object {$_.memo -eq 'TEST-RECOVERED'}).Count -ne 1){Write-Warning 'Listener recovery failed; inspect notification access and rebind.'}
Harness clear "$run-cleanup"
$null=Result "$run-cleanup"
Harness effects "$run-effects-reset" @('--ez','reduced','false')
$null=Result "$run-effects-reset"
Harness theme "$run-theme-reset" @('--es','value','system')
$null=Result "$run-theme-reset"
Invoke-Adb shell am start -W -n "$pkg/vn.chitieu.MainActivity" | Out-Null
Invoke-Adb logcat -d -b crash -v brief | Set-Content (Join-Path $out "$run-crashes.txt")
Write-Host 'ADB actions finished; warnings indicate blocked/failed checks. Inspect screenshots, crash process names and gfxinfo. Run connectedDebugAndroidTest separately with ANDROID_SERIAL set.'
