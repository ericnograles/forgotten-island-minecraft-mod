"""Build the pixel-art moonrise and the open-cockpit rocket mesh."""
import json
import math
import random
from pathlib import Path
from PIL import Image, ImageDraw

ASSETS = Path(__file__).resolve().parents[1] / "src/main/resources/assets/forgottenisland"
GUI = ASSETS / "textures/gui"
rng = random.Random(1959)
sky = Image.new("RGB", (480, 270))
draw = ImageDraw.Draw(sky)
for y in range(270):
    t = (y / 269) ** 1.7
    draw.line((0,y,480,y), fill=tuple(round(a+(b-a)*t) for a,b in zip((8,13,39),(167,93,112))))
for _ in range(620):
    x,y=rng.randrange(480),rng.randrange(212)
    light=rng.randrange(110,242)
    draw.point((x,y),fill=(light,light,min(255,light+21)))
    if light>237:
        draw.line((x-2,y,x+2,y), fill=(127,163,202))
        draw.line((x,y-2,x,y+2), fill=(190,205,227))
for layer,(baseline,color) in enumerate([(201,"#514664"),(218,"#373953"),(235,"#172e46")]):
    ridge=[(x,round(baseline+10*math.sin(x*.024+layer)+5*math.cos(x*.071+layer))) for x in range(481)]
    draw.polygon(ridge+[(480,270),(0,270)],fill=color)
draw.rectangle((0,241,480,270),fill="#1b3148")
for _ in range(340):
    x,y=rng.randrange(480),rng.randrange(242,270)
    draw.line((x,y,x+rng.randrange(2,14),y),fill=rng.choice(["#284055","#334a60","#47546a"]))
sky.save(GUI/"moonrise_sky.png")

serpent=Image.new("RGBA",(160,106))
d=ImageDraw.Draw(serpent)
points=[(2,88),(13,94),(27,94),(37,86),(34,77),(22,72),(18,62),(25,53),(42,51),(60,61),(73,59),(78,47),(76,36),(84,28),(101,29),(114,24),(124,19)]
for i,(a,b) in enumerate(zip(points,points[1:])):
    radius=1+i*.42
    d.line([a,b],fill="#16152e",width=int(radius*2.4))
    x,y=b
    d.ellipse((x-radius,y-radius,x+radius,y+radius),fill="#44253e")
    d.line([(a[0],a[1]-radius*.55),(x,y-radius*.55)],fill="#a84a4e",width=max(1,int(radius*.7)))
    if i>3:
        d.polygon([(x-4,y-radius+2),(x-7,y-radius-5),(x+2,y-radius)],fill="#e19b5b")
for side in [0,1]:
    x,y=92+side*9,30+side*2
    d.polygon([(x,y),(x-21,y-18),(x-16,y-3),(x-24,y-3),(x-4,y+9)],fill="#552b4a",outline="#d17a57")
d.polygon([(111,19),(114,11),(128,6),(138,10),(151,4),(157,6),(147,18),(132,21),(143,21),(152,17),(154,20),(141,28),(126,30),(116,25)],fill="#653141",outline="#281c31")
d.polygon([(118,13),(112,1),(125,8),(130,2),(132,10)],fill="#edad70")
d.line([(127,13),(133,11),(138,12)],fill="#ffcf5b",width=3)
for x,y in [(139,14),(145,12),(149,10),(141,23),(146,21)]:
    d.polygon([(x,y),(x+2,y-1),(x+1,y+3)],fill="#ffedcb")
serpent.save(GUI/"moonrise_serpent.png")

# A low open cockpit leaves the player's head and arms visible above the rails.
hull=[]
def box(lo, hi, material):
    hull.append({"from":lo,"to":hi,"faces":{s:{"texture":"#"+material} for s in ("up","down","north","south","east","west")}})
box([4,1,4],[12,5,13],"dark")
box([3,4,3],[13,7,14],"hull")
box([4,7,5],[12,8,13],"seat")
for x in [2,12]:
    box([x,6,3],[x+2,9,14],"hull")
    box([x,9,4],[x+2,11,12],"trim")
    box([x-.02,5,13.8],[x+2.02,8,18],"fin")
    box([x,1,13],[x+2,5,18],"fin")
box([4,7,2],[12,11,5],"hull")
box([5,11,2],[11,13,4],"window")
box([5,6,-1],[11,9,3],"hull")
box([6,6,-3],[10,8,-1],"trim")
for x in [3,10]:
    box([x,0,7],[x+3,3,11],"dark")
    box([x+.5,-.5,7.5],[x+2.5,.2,10.5],"glow")
# Twin vertical boosters frame the pilot and taper into copper nose cones.
for x in [0,13]:
    box([x,1,9],[x+3,3,13],"hull")
    box([x,5,9],[x+3,11,13],"hull")
    box([x,3,9],[x+3,5,13],"fin")
    box([x,11,9],[x+3,13,13],"trim")
    box([x+.4,13,9.5],[x+2.6,15,12.5],"hull")
    box([x+1,15,10.3],[x+2,17,11.7],"trim")
    box([x,-1,9],[x+3,1,13],"dark")
    box([x+.5,-1.2,9.5],[x+2.5,-.9,12.5],"glow")
model={"textures":{k:"minecraft:block/"+v for k,v in dict(hull="quartz_block_bottom",trim="cut_copper",seat="cyan_terracotta",fin="oxidized_cut_copper",dark="polished_deepslate",window="diamond_block",glow="shroomlight",particle="quartz_block_bottom").items()},"elements":hull,
"display":{"gui":{"rotation":[26,144,0],"translation":[0,1,0],"scale":[.76,.76,.76]},"firstperson_righthand":{"rotation":[5,130,0],"translation":[0,3,0],"scale":[.52,.52,.52]},"thirdperson_righthand":{"rotation":[0,90,0],"scale":[.5,.5,.5]}}}
(ASSETS/"models/item/moon_rocket.json").write_text(json.dumps(model,indent=2)+"\n")

labels_path = ASSETS / "lang/en_us.json"
labels = json.loads(labels_path.read_text())
labels.update({"item.forgottenisland.moon_rocket": "Moon Rocket", "entity.forgottenisland.moon_rocket": "Moon Rocket"})
labels_path.write_text(json.dumps(labels, indent=2, ensure_ascii=False)+"\n")
