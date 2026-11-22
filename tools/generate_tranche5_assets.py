"""Original 0.5 model revisions, material atlases and Creative egg icons."""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'assets-src/models'
RES = ROOT / 'src/main/resources/assets/forgottenisland'


def bone(name, parent, pivot, boxes, rotation=(0, 0, 0)):
    return dict(name=name, parent=parent, pivot=pivot, rotation=rotation, boxes=boxes)


def save_model(name, bones):
    text = json.dumps(dict(texture_width=1024, texture_height=128, bones=bones), indent=2) + '\n'
    for dest in [SOURCE, RES/'models/creature']:
        (dest/f'{name}.json').write_text(text)


def atlas(name, colors, storm=False):
    image = Image.new('RGBA', (1024, 128))
    pixels = image.load()
    for i, color in enumerate(colors):
        rgb = tuple(bytes.fromhex(color))
        for x in range(128):
            for y in range(128):
                shade = ((x//3*7+y//4*11+i*3)%9-4) + (3 if (x+2*y)%13==0 else 0)
                vein = storm and i in (0,1,2) and (y+int(3*math.sin(x*.65))+x//5)%31==0
                pixels[i*128+x,y] = (255,215,39,255) if vein else tuple(max(0,min(255,c+shade)) for c in rgb)+(255,)
    image.save(RES/'textures/entity'/f'{name}.png')


def manang():
    bones = [bone('upper','root',[0,-19,0],[[-5,-11,-3,10,12,6,1],[-3,-10,-3.2,6,9,1,6]]),
             bone('head','upper',[0,-12,0],[[-4.6,-9,-3.7,9.2,7.4,7,0],[-3.7,-3,-3.6,7.4,2.8,6.5,0],
                [-.6,-5,-4.6,1.2,3.3,1.2,0],[-1,-2.5,-5.1,2,1,1.7,6],[-2.8,-.9,-3.8,5.6,1.1,1,1]]),
             bone('crown','head',[0,-8,0],[[-4.8,-2,-3.5,9.6,3,7.4,3],[-4,-4,-2.3,8,3,6.6,3],[-3,-5.6,-.5,6,3,5.2,3]],(-.17,0,0)),
             bone('jaw','head',[0,-.9,-.5],[[-3,0,-3,6,1.6,4.8,0],[-2,1.3,-2.5,4,1,3,0],[-2.4,-.15,-3.4,4.8,.6,1,1]])]
    for side, name in [(-1,'left'),(1,'right')]:
        def add(label,parent,pivot,boxes,rotation=(0,0,0)):
            bones.append(bone(label+'_'+name,parent,pivot,boxes,rotation))
        add('eye','head',[side*2.2,-5.4,-3.85],[[-1.38,-.88,-.4,2.76,1.76,1,5],[-.26,-.8,-.48,.52,1.6,.3,3]],(0,side*.04,-side*.14))
        add('brow','head',[side*2.2,-6.9,-3.95],[[-1.72,-.38,-.44,3.44,.76,1.12,3]],(0,0,-side*.32))
        add('cheek','head',[side*3.25,-3.65,-3.2],[[-1.38,-.55,-.78,2.76,1.38,1.6,0]],(.09,side*.24,-side*.32))
        add('ear','head',[side*4.4,-5,0],[[-.62,-2.4,-1.1,1.24,3.8,2.2,6]],(0,0,side*.65))
        add('temple','head',[side*4.4,-7.3,.2],[[-.83,-3.6,-1.1,1.66,4.4,2.2,3],[-.44,-5.5,-.75,.88,2,1.5,3]],(.14,0,side*.29))
        add('lock','head',[side*4.55,-7,1],[[-.95,0,-1.45,1.9,9.5,3.1,3]],(-.07,0,-side*.06))
        add('tip','lock_'+name,[0,9,0],[[-.7,0,-1.15,1.4,7,2.3,3],[-.36,6,-.7,.72,2.8,1.4,3]],(-.13,0,side*.1))
        add('mantle','head',[side*2.4,-7,3],[[-2,0,-.8,4,14,3,3],[-1.5,13,0,3,8,2.5,3]],(.16,0,0))
        add('fang','head',[side*2.22,-1.2,-3.82],[[-.6,0,-.36,1.2,1.2,.85,4],[-.31,1.1,-.34,.62,.88,.7,4]],(0,0,side*.16))
        add('tusk','jaw',[side*1.4,0,-3.22],[[-.34,-1.2,-.34,.68,1.5,.7,4]],(0,0,-side*.14))
        add('arm','upper',[side*5,-9,0],[[-1.5,0,-1.5,3,12,3,0],[-2,10,-2,4,3,4,1]])
        add('wing','upper',[side*4,-8,3],[[min(0,side*13),0,0,13,1.6,2.4,3],[min(0,side*12),1,.6,12,8,1.1,2]],(0,side*.2,side*.3))
        add('spar','wing_'+name,[side*12,0,0],[[min(0,side*12),0,0,12,1.1,1.6,3],[min(0,side*10),1,.5,10,6,1,2],[min(0,side*6),6,.5,6,4,1,6]],(0,-side*.22,-side*.38))
    bones.append(bone('lower','root',[0,-18,0],[[-4,0,-3,8,7,6,1]]))
    for s,n in [(-1,'left'),(1,'right')]:
        bones.append(bone('leg_'+n,'lower',[s*2,6,0],[[-1.5,0,-2,3,12,4,1]]))
    save_model('manang',bones)
    atlas('manang',['5963a8','39253f','733352','171526','ffeacf','ffd344','6c518e','88426e'])


def serpent():
    model=json.loads((SOURCE/'bakunawa_boss.json').read_text())
    bones=[b for b in model['bones'] if not b['name'].startswith(('stormwing','wingtip'))]
    for s,n in [(-1,'left'),(1,'right')]:
        boxes=[[min(0,s*22),-2,0,22,3,3,3],[min(0,s*20),0,1,20,1.2,12,0],
               [min(0,s*14),0,12,14,1.15,6,0],[min(0,s*15),-1,7,15,2,2,6]]
        bones.append(bone('stormwing_'+n,'root',[0,0,0],boxes))
        bones.append(bone('wingtip_'+n,'stormwing_'+n,[s*21,0,0],[[min(0,s*20),-1,0,20,2,2.1,6],[min(0,s*17),0,.9,17,1,8,0],[min(0,s*11),0,8,11,1,6,0],[min(0,s*5),0,13,5,1,5,0]],(0,s*.36,0)))
    save_model('bakunawa_boss',bones)
    atlas('bakunawa_storm',['191827','312d3e','282237','39313f','ffe496','ffe028','ffbd26','322b32'],True)
    glow=Image.open(RES/'textures/entity/bakunawa_storm.png').convert('RGBA')
    glow.putdata([p if p[0]>200 and p[1]>140 else (0,0,0,0) for p in glow.getdata()])
    glow.save(RES/'textures/entity/bakunawa_storm_glow.png')


def eggs():
    labels=json.loads((RES/'lang/en_us.json').read_text())
    for i,(name,body,trim) in enumerate([
        ('Bakunawa Boss Spawn Egg','#14666a','#fbb95c'),('Phase Two Bakunawa Boss Spawn Egg','#201e31','#ffe33a'),
        ('Manang Boss Spawn Egg','#6557a3','#ffce5a'),('Separated Manang Boss Spawn Egg','#491d50','#f384b6')]):
        key=name.lower().replace(' ','_')
        art=Image.new('RGBA',(32,32));d=ImageDraw.Draw(art)
        d.polygon([(6,23),(8,13),(12,6),(16,3),(20,5),(24,12),(27,23),(24,28),(16,30),(8,28)],fill=body,outline='#181722',width=2)
        d.polygon([(7,15),(3,8),(11,13),(16,9),(21,13),(29,8),(25,16),(16,21)],fill=trim)
        d.line([(10,24),(15,22),(21,24)],fill=trim,width=2)
        if i%2:d.line([(20,6),(16,14),(20,14),(12,25),(15,18),(12,18)],fill='#fff9cf',width=2)
        art.save(RES/'textures/item'/f'{key}.png')
        (RES/'models/item'/f'{key}.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':f'forgottenisland:item/{key}'}})+'\n')
        labels['item.forgottenisland.'+key]=name
    (RES/'lang/en_us.json').write_text(json.dumps(labels,indent=2,ensure_ascii=False)+'\n')

if __name__=='__main__':
    manang()
    serpent()
    eggs()
