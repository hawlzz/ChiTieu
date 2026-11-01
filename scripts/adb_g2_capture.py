from pathlib import Path
import sys,subprocess,time,json,uuid,re,struct,hashlib,datetime,argparse,os,xml.etree.ElementTree as ET

sys.stdout.reconfigure(encoding='utf-8')
r=Path(__file__).resolve().parents[1]
ap=argparse.ArgumentParser(description='Emulator-only G2 capture: installs debug APK and seeds synthetic test data.')
ap.add_argument('--serial',default='emulator-5554')
ap.add_argument('--skip-install',action='store_true',help='Use the already installed identical debug APK')
ap.add_argument('--adb',default=str(Path(os.environ.get('ANDROID_HOME','D:/ChiTieu/tools/sdk'))/'platform-tools/adb.exe'))
ap.add_argument('--out',type=Path,default=r/'screenshots'/('g2-native-'+datetime.date.today().isoformat()))
cfg=ap.parse_args();serial=cfg.serial;adbexe=cfg.adb;out=cfg.out
assert serial.startswith('emulator-'),'Use an emulator only; this tool seeds debug fixtures.'
out.mkdir(parents=True,exist_ok=True)
apk=r/'app/build/outputs/apk/debug/app-debug.apk'
(out/'capture-build.json').write_text(json.dumps(dict(apk_sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),serial=serial,started=datetime.datetime.now().isoformat(),data='synthetic debug fixtures'),indent=2),encoding='utf-8')

def adb(*args,check=True):return subprocess.run([adbexe,'-s',serial,*args],capture_output=True,timeout=40,check=check).stdout
def txt(*args):return adb(*args).decode('utf-8',errors='replace').strip()
assert txt('get-state')=='device'
font=txt('shell','settings','get','system','font_scale');rot=txt('shell','settings','get','system','user_rotation');auto=txt('shell','settings','get','system','accelerometer_rotation')
def setting(key,value):
 adb('shell','settings','delete','system',key) if value=='null' else adb('shell','settings','put','system',key,value)
def harness(action,*extra):
 run='g2-'+uuid.uuid4().hex[:10]
 adb('shell','am','force-stop','vn.chitieu.debug')
 adb('shell','am','start','-W','-n','vn.chitieu.debug/vn.chitieu.debug.HarnessActivity','--es','action',action,'--es','run',run,*extra)
 deadline=time.monotonic()+60
 for _ in range(240):
  if time.monotonic()>deadline:break
  p=subprocess.run([adbexe,'-s',serial,'shell','run-as','vn.chitieu.debug','cat','files/test-results/'+run+'.json'],capture_output=True,timeout=10)
  if p.returncode==0:
   result=json.loads(p.stdout);assert not result.get('error'),result;return
  time.sleep(.2)
 raise RuntimeError('Harness timeout '+action)
def open_screen(screen):
 adb('shell','am','force-stop','vn.chitieu.debug');adb('shell','am','start','-W','-n','vn.chitieu.debug/vn.chitieu.MainActivity','--es','test_screen',screen);time.sleep(1.5)
def tree():
 for _ in range(3):
  adb('shell','uiautomator','dump','/sdcard/chitieu-g2.xml',check=False)
  raw=adb('shell','cat','/sdcard/chitieu-g2.xml',check=False)
  try:return ET.fromstring(raw),raw
  except ET.ParseError:time.sleep(.4)
 raise RuntimeError('Hierarchy unavailable')
records=[]
def capture(name,expected=None):
 t,raw=tree()
 if expected:
  for attempt in range(5):
   if any(expected in n.get('text','') for n in t.iter('node')):break
   time.sleep(.8);t,raw=tree()
 png=adb('exec-out','screencap','-p');assert png.startswith(b'\x89PNG')
 w,h=struct.unpack('>II',png[16:24]);texts=[n.get('text','') for n in t.iter('node')]
 if expected and not any(expected in text for text in texts):
  (out/('failed-'+name+'.png')).write_bytes(png);(out/('failed-'+name+'.xml')).write_bytes(raw)
  print('Visible text:',repr(texts[:12]),flush=True)
  raise AssertionError('Missing '+expected)
 outside=[]
 for n in t.iter('node'):
  b=list(map(int,re.findall(r'-?\d+',n.get('bounds',''))))
  if len(b)==4 and (b[0]<0 or b[1]<0 or b[2]>w or b[3]>h):outside.append(n.get('text',''))
 (out/(name+'.png')).write_bytes(png);(out/(name+'.xml')).write_bytes(raw)
 records.append(dict(name=name,width=w,height=h,outside_bounds=len(outside)))
 print('Captured',name,w,h,flush=True)
def tap(label):
 t,_=tree()
 candidates=[n for n in t.iter('node') if label in (n.get('text','')+' '+n.get('content-desc',''))]
 assert candidates,'Missing tappable '+label
 n=candidates[0];x1,y1,x2,y2=map(int,re.findall(r'\d+',n.get('bounds')))
 adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2));time.sleep(.8)
try:
 if not cfg.skip_install:adb('install','-r',str(apk))
 else:
  installed=txt('shell','pm','path','vn.chitieu.debug').splitlines()[0].removeprefix('package:')
  assert txt('shell','sha256sum',installed).split()[0]==hashlib.sha256(apk.read_bytes()).hexdigest(),'Installed APK differs'
 adb('shell','input','keyevent','KEYCODE_WAKEUP');adb('shell','wm','dismiss-keyguard')
 setting('font_scale','1.0');setting('accelerometer_rotation','0');setting('user_rotation','0')
 harness('onboarding');harness('theme','--es','value','light');harness('effects','--ez','reduced','false')
 open_screen('ledger');capture('overview-empty-light','Tổng quan')
 harness('seed-statistics')
 for theme in ['light','dark']:
  harness('theme','--es','value',theme)
  for screen,expected in [('ledger','Tổng quan'),('transactions','Giao dịch'),('statistics','Thống kê'),('settings','Cài đặt'),('welcome','Chào mừng')]:
   open_screen(screen);capture(screen+'-'+theme,expected)
  open_screen('ledger');tap('Thêm nhanh');capture('quickadd-'+theme,'Thêm nhanh')
  open_screen('settings');tap('Khoá ứng dụng');capture('security-'+theme,'Khoá ứng dụng');tap('Bật khoá ứng dụng');capture('pin-create-'+theme,'Tạo mã PIN')
 # Small-font and landscape checks avoid the custom wm-size matrix that previously destabilised this AVD.
 harness('theme','--es','value','light');setting('font_scale','1.3')
 open_screen('settings');capture('settings-font130-light','Cài đặt')
 setting('font_scale','2.0');open_screen('ledger');capture('overview-font200-light','Tổng quan')
 setting('font_scale','1.0');setting('user_rotation','1');time.sleep(2)
 open_screen('settings');capture('settings-landscape-light','Cài đặt')
 setting('user_rotation','0');time.sleep(1)
 for reduced in [False,True]:
  harness('effects','--ez','reduced',str(reduced).lower())
  for screen in ['settings','statistics']:
   open_screen(screen)
   if reduced:capture(screen+'-reduced-light')
   adb('shell','dumpsys','gfxinfo','vn.chitieu.debug','reset')
   for i in range(6):adb('shell','input','swipe','540','1800' if i%2==0 else '800','540','800' if i%2==0 else '1800','350');time.sleep(.3)
   (out/('gfxinfo-'+screen+('-reduced' if reduced else '')+'.txt')).write_bytes(adb('shell','dumpsys','gfxinfo','vn.chitieu.debug'))
 harness('effects','--ez','reduced','false')
 # Actual launcher capture: no synthetic bitmap is substituted for it.
 adb('shell','am','start','-a','android.intent.action.MAIN','-c','android.intent.category.HOME');time.sleep(1)
 adb('shell','input','swipe','540','2150','540','700','350');time.sleep(1)
 t,_=tree()
 if any('ChiTieu' in (n.get('text','')+' '+n.get('content-desc','')) for n in t.iter('node')):capture('launcher-native')
finally:
 setting('font_scale',font);setting('user_rotation',rot);setting('accelerometer_rotation',auto)
 (out/'capture-metadata.json').write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf-8')
 adb('shell','rm','-f','/sdcard/chitieu-g2.xml',check=False)
 harness('effects','--ez','reduced','false');harness('theme','--es','value','system');open_screen('ledger')
 print('Restored original emulator font/rotation. Captures use synthetic debug data only.',flush=True)
