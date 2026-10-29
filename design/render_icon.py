"""Render the project's own vector icon at 512px using existing Pillow (no download)."""
from pathlib import Path
from PIL import Image, ImageDraw
N=1536
im=Image.new('RGB',(N,N))
p=im.load()
for y in range(N):
 for x in range(N):
  t=(x+y)/(2*(N-1)); p[x,y]=tuple(round(a+(b-a)*t) for a,b in zip((10,77,255),(25,194,201)))
d=ImageDraw.Draw(im);scale=N/108
xy=lambda ps:[(round(x*scale),round(y*scale)) for x,y in ps]
def line(ps,width=3.8):
 q=xy(ps);w=round(width*scale);d.line(q,fill='white',width=w,joint='curve')
 for x,y in [q[0],q[-1]]:d.ellipse((x-w/2,y-w/2,x+w/2,y+w/2),fill='white')
for ps in [[(38,29),(33,29),(29,33),(29,38)],[(70,29),(75,29),(79,33),(79,38)],[(29,70),(29,75),(33,79),(38,79)],[(79,70),(79,75),(75,79),(70,79)]]:line(ps)
d.ellipse(tuple(v*scale for v in (37,37,71,71)),outline='white',width=round(3.8*scale))
line([(49,44),(55,44),(59,46),(62,51),(62,57),(59,62),(55,64),(49,64),(49,44)])
line([(45,51),(57,51)])
d.polygon(xy([(65,62),(69,66),(77,57),(80,60),(69,72),(62,65)]),fill='white')
mask=Image.new('L',(N,N));ImageDraw.Draw(mask).rounded_rectangle((0,0,N,N),radius=N*.225,fill=255)
im=im.convert('RGBA');im.putalpha(mask)
im.resize((512,512),Image.Resampling.LANCZOS).save(Path(__file__).parent/'icon-preview.png')
