#!/usr/bin/env python3
"""Original 0.4 pixel art and geometry, interpreting the owner's nine paper drawings."""
from pathlib import Path
import json, math, copy
import numpy as np
import soundfile as sf
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'; A=RES/'assets/forgottenisland'
def write(p,obj):
    p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n')
def texture(name,im,folder='item'):
    p=A/f'textures/{folder}/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
def item(name,im,hand=False):
    texture(name,im);write(A/f'models/item/{name}.json',{'parent':'minecraft:item/handheld' if hand else 'minecraft:item/generated','textures':{'layer0':f'forgottenisland:item/{name}'}})

# Three distinct silhouettes, faceted colored blades and the drawings' horn/wing guards.
def sword(kind):
    im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
    dark='#17212f';blue='#2aa4da';pink='#d955ba';gold='#f8c34e';lime='#b4e56b'
    if kind=='sapphire_soul_sword':
        d.polygon([(31,3),(40,1),(35,10),(35,37),(29,43),(26,35),(27,12)],fill='#101c2d',outline=blue,width=2)
        d.polygon([(31,7),(35,4),(31,31),(29,35)],fill='#82def4')
        for side in (-1,1):
            d.polygon([(32+side*4,35),(32+side*13,31),(32+side*17,18),(32+side*17,32),(32+side*10,39),(32+side*3,39)],fill=dark,outline=blue)
        d.rectangle((29,37,34,54),fill=dark,outline=blue)
        d.polygon([(28,53),(36,53),(32,62)],fill=dark,outline=blue,width=2)
        for y in (40,45,50):d.line((29,y,34,y),fill='#a1eafb')
    elif kind=='phoenix_sword':
        d.polygon([(32,1),(37,11),(35,38),(29,38),(27,11)],fill=gold,outline='#963520',width=2)
        d.polygon([(32,4),(33,34),(29,29)],fill='#fff6b3')
        for side in (-1,1):
            d.polygon([(32+side*3,35),(32+side*12,29),(32+side*25,18),(32+side*20,34),(32+side*11,41),(32+side*3,40)],fill='#e75b2b',outline='#ffcf62')
            for j in range(3):d.line((32+side*6,37-j,32+side*(19+j*2),27-j*3),fill='#fff0a1',width=1)
        d.rectangle((29,39,35,55),fill='#9b4227',outline=gold)
        d.ellipse((27,53,37,62),fill='#ec732c',outline='#ffd767',width=2)
        d.polygon([(34,5),(43,6),(38,10)],fill='#d75b2a',outline=gold)
    else:
        d.polygon([(32,1),(38,12),(36,35),(31,43),(25,33),(26,13)],fill=blue,outline=dark,width=2)
        d.polygon([(32,4),(32,37),(27,31),(29,16)],fill='#fa9bd0')
        d.polygon([(32,4),(35,15),(34,33),(32,37)],fill='#d44ba9')
        for side in (-1,1):
            for y in (16,23,30):d.polygon([(32+side*5,y+6),(32+side*10,y-3),(32+side*5,y)],fill=lime,outline='#4d7d54')
            d.polygon([(32+side*4,37),(32+side*17,30),(32+side*21,34),(32+side*14,42),(32+side*4,41)],fill=pink,outline=dark)
            d.polygon([(32+side*8,38),(32+side*16,34),(32+side*14,39)],fill='#8adff6')
        d.rectangle((29,41,34,52),fill=lime,outline=dark)
        d.polygon([(32,49),(39,55),(32,63),(25,55)],fill=pink,outline=gold,width=2)
        d.polygon([(32,52),(35,55),(32,59),(29,55)],fill='#ffb9df')
    return im
for name in ('crystal_sword','sapphire_soul_sword','phoenix_sword'):item(name,sword(name),True)
colors=['#62c8f0','#ec84c5','#b4df68','#db5964','#9672ce']
im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
d.polygon([(16,1),(26,9),(24,23),(16,31),(7,22),(6,9)],fill=colors[0],outline='#142039',width=2)
d.polygon([(16,3),(16,28),(9,21),(8,10)],fill=colors[1]);d.polygon([(17,4),(24,10),(23,21),(17,28)],fill=colors[2]);d.line((16,4,16,27),fill='#fff3d1',width=2)
item('prismatic_crystal',im)

# Armor retains the exact requested palette and distinct spiked profiles.
for name,shape in {
    'crystal_helmet':[(10,12),(54,12),(57,45),(43,45),(43,29),(21,29),(21,45),(7,45)],
    'crystal_chestplate':[(18,10),(25,15),(39,15),(46,10),(61,17),(55,31),(47,28),(47,57),(17,57),(17,28),(9,31),(3,17)],
    'crystal_leggings':[(16,8),(48,8),(48,58),(35,58),(33,31),(30,31),(28,58),(15,58)],
    'crystal_boots':[(10,11),(25,11),(26,42),(30,48),(30,57),(6,57),(6,46),(10,41),(10,11)],
}.items():
    mask=Image.new('L',(64,64));md=ImageDraw.Draw(mask);md.polygon(shape,fill=255)
    if name=='crystal_boots':md.polygon([(x+29,y) for x,y in shape],fill=255)
    if name in ('crystal_helmet','crystal_chestplate'):
        for x in (12,22,42,52):md.polygon([(x-4,18),(x,1),(x+4,18)],fill=255)
    im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
    for x in range(-64,100,7):d.polygon([(x,0),(x+6,0),(x+25,64),(x+19,64)],fill=colors[(x//7)%5])
    im.putalpha(mask);d=ImageDraw.Draw(im);d.line(shape+[shape[0]],fill='#28354e',width=2)
    for x in (19,37,48):d.line((x,18,x+6,50),fill='#fff6dc',width=1)
    # Restore the silhouette after adding facet highlights.
    im.putalpha(mask);item(name,im)
for layer in (1,2):
    im=Image.new('RGBA',(64,32));d=ImageDraw.Draw(im)
    for x in range(64):
        for y in range(32):
            color=colors[((x+y//2)//3)%len(colors)];im.putpixel((x,y),tuple(bytes.fromhex(color[1:]))+(255,))
    for x in range(0,64,8):d.line((x,0,x,31),fill='#344666');d.line((x+1,0,x+1,31),fill='#d8f4f3')
    texture(f'crystal_layer_{layer}',im,'models/armor')

# Egg: striped shell, two curling horns, black/yellow nodules, increasing cracks.
for stage in range(4):
    im=Image.new('RGBA',(32,32),'#accfdd');d=ImageDraw.Draw(im)
    for x in range(0,32,4):d.rectangle((x,0,x+1,31),fill=colors[x//4%5])
    for x,y in ((5,9),(23,6),(16,18),(6,25),(25,25)):
        d.rectangle((x-2,y-2,x+2,y+2),fill='#192433');d.rectangle((x,y-2,x+2,y+1),fill='#f6d15d')
    for n in range(stage):d.line([(4+n*9,0),(7+n*8,7),(3+n*8,12),(8+n*7,19),(5+n*8,31)],fill='#25243b',width=1)
    texture(f'bakunawa_egg_{stage}',im,'block')
    def box(start,end):return {'from':start,'to':end,'faces':{f:{'texture':'#shell'} for f in ('north','south','east','west','up','down')}}
    elements=[box([5,0,5],[11,2,11]),box([3,2,3],[13,9,13]),box([4,9,4],[12,12,12]),box([6,12,6],[10,14,10])]
    for side in (0,1):
        x=2 if side==0 else 12
        elements.extend([box([x,9,6],[x+2,15,8]),box([x if side==0 else x-2,14,6],[x+4 if side==0 else x+2,16,8])])
    write(A/f'models/block/bakunawa_egg_{stage}.json',{'textures':{'shell':f'forgottenisland:block/bakunawa_egg_{stage}','particle':f'forgottenisland:block/bakunawa_egg_{stage}'},'elements':elements})
write(A/'blockstates/bakunawa_egg.json',{'variants':{f'hatch={n}':{'model':f'forgottenisland:block/bakunawa_egg_{n}'} for n in range(4)}})
im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im);d.ellipse((13,13,51,60),fill='#b9d9e9',outline='#27303e',width=2)
mask=im.getchannel('A');stripes=Image.new('RGBA',(64,64));sd=ImageDraw.Draw(stripes)
for x in range(0,64,5):sd.line((x,0,x+9,63),fill=colors[x//5%5],width=2)
stripes.putalpha(Image.composite(stripes.getchannel('A'),Image.new('L',(64,64)),mask));im.alpha_composite(stripes);d=ImageDraw.Draw(im)
for side in (-1,1):
    d.line([(32+side*16,23),(32+side*24,14),(32+side*24,3),(32+side*16,4),(32+side*15,9)],fill='#753c45',width=6)
    d.line([(32+side*16,22),(32+side*23,13),(32+side*23,5),(32+side*17,6)],fill='#ef8e79',width=3)
for x,y in ((22,27),(43,25),(33,40),(21,48),(44,49)):
    d.ellipse((x-4,y-4,x+3,y+3),fill='#142133');d.rectangle((x,y-3,x+2,y+1),fill='#ffe18b')
item('bakunawa_spawn_egg',im)
write(RES/'data/forgottenisland/loot_tables/blocks/bakunawa_egg.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'forgottenisland:bakunawa_spawn_egg'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})

# The boss has its own skeleton/texture; the thirteen companion designs are preserved.
m=copy.deepcopy(json.loads((ROOT/'assets-src/models/bakunawa.json').read_text()))
for b in m['bones']:
    if b['name'].startswith('segment'):
        b['parent']='root';b['pivot']=[0,0,0];b['rotation']=[0,0,0]
    if b['name']=='head':
        b['pivot']=[0,0,0];b['boxes']=[[-12,-8,-13,24,16,21,0],[-10,-3,-25,20,8,15,1],[-9,-4,-24,18,1,14,7]]
        for x in range(-9,10,3):b['boxes'].append([x,5,-24,1,4,2,4])
        for x in (-9,7):b['boxes'].append([x,5,-22,2,7,3,4])
    if b['name']=='jaw':
        b['boxes']=[[-10,0,-15,20,3,19,1],[-9,-.1,-14,18,.2,17,7]]
        for x in range(-9,10,3):b['boxes'].append([x,-3,-14,1,3,2,4])
    if b['name'].startswith('horn_'):
        side=-1 if b['name'].endswith('-1') else 1;b['pivot']=[side*10,-6,0]
        b['boxes']=[[-2,-14,-1,4,14,4,2],[-2,-19,0,4,7,4,2],[-2-side*5,-20,0,9,3,3,2]]
    if b['name'].startswith('whisker_'):b['boxes']=[]
    if b['name'].startswith('eye_'):
        side=-1 if b['name'].endswith('-1') else 1;b['pivot']=[side*12.3,-4,-10]
    if b['name'].startswith('cheek_fin_') or b['name'] in ('fin_left','fin_right'):
        for box in b['boxes']:
            if box[6]==6:box[6]=2
write(ROOT/'assets-src/models/bakunawa_boss.json',m);write(A/'models/creature/bakunawa_boss.json',m)
palette=['#171b25','#62262d','#bd3a45','#1e1321','#f5e5ca','#ff6d30','#eaaa32','#9d3341']
im=Image.new('RGBA',(1024,128));glow=Image.new('RGBA',(1024,128));d=ImageDraw.Draw(im);gd=ImageDraw.Draw(glow)
for n,color in enumerate(palette):
    d.rectangle((n*128,0,n*128+127,127),fill=color)
    for y in range(128):
        for x in range(128):
            if n in (0,1,7):
                if y%21 in (3,4,5):d.point((n*128+x,y),fill='#c44738' if y%42<21 else '#dc793e')
                elif (x+y//4)%13==0:d.point((n*128+x,y),fill='#31333c')
            elif n==2 and (x+y)%11<2:d.point((n*128+x,y),fill='#ee7a69')
    if n in (5,6):gd.rectangle((n*128,0,n*128+127,127),fill=color)
texture('bakunawa_boss',im,'entity');texture('bakunawa_boss_glow',glow,'entity')

# All gear uses vanilla ingredients. Recipes also unlock in the vanilla recipe book.
def recipe(name,pattern,key,result=None,count=1):
    write(RES/f'data/forgottenisland/recipes/{name}.json',{'type':'minecraft:crafting_shaped','category':'equipment','pattern':pattern,'key':{k:{'item':v} for k,v in key.items()},'result':{'item':f'forgottenisland:{result or name}','count':count}})
    first=next(iter(key.values()))
    write(RES/f'data/forgottenisland/advancements/recipes/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'ingredient':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':[first]}]}},'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':f'forgottenisland:{name}'}}},'requirements':[['ingredient','has_recipe']],'rewards':{'recipes':[f'forgottenisland:{name}']}})
recipe('prismatic_crystal',[' A ','PDB',' A '],{'A':'minecraft:amethyst_shard','P':'minecraft:prismarine_crystals','D':'minecraft:diamond','B':'minecraft:blaze_powder'})
for name,accent in [('crystal_sword','minecraft:spider_eye'),('sapphire_soul_sword','minecraft:lapis_lazuli'),('phoenix_sword','minecraft:blaze_rod')]:
    recipe(name,[' C ','CSC',' A '],{'C':'forgottenisland:prismatic_crystal','S':'minecraft:netherite_sword','A':accent})
for part in ('helmet','chestplate','leggings','boots'):
    recipe(f'crystal_{part}',[' C ','CAC',' C '],{'C':'forgottenisland:prismatic_crystal','A':f'minecraft:netherite_{part}'})
recipe('bakunawa_spawn_egg',['PAP','PEP',' P '],{'P':'minecraft:prismarine_crystals','A':'minecraft:amethyst_shard','E':'minecraft:egg'})

lang=json.loads((A/'lang/en_us.json').read_text())
lang.update({'item.forgottenisland.prismatic_crystal':'Prismatic Crystal','item.forgottenisland.sapphire_soul_sword':'Sapphire Soul Sword','item.forgottenisland.phoenix_sword':'Phoenix Sword','item.forgottenisland.bakunawa_spawn_egg':'Bakunawa Hatching Egg','block.forgottenisland.bakunawa_egg':'Bakunawa Egg','entity.forgottenisland.phoenix_fireball':'Phoenix Fireball'})
for part in ('helmet','chestplate','leggings','boots'):lang[f'item.forgottenisland.crystal_{part}']='Crystal '+part.title()
sounds=json.loads((A/'sounds.json').read_text())
sr=22050;duration=4;t=np.arange(sr*duration)/sr;rng=np.random.default_rng(430)
noise=rng.normal(0,1,len(t));rough=np.convolve(noise,np.ones(9)/9,mode='same')
pitch=58+19*np.sin(t*.9)+6*np.sin(t*14);phase=2*np.pi*np.cumsum(pitch)/sr
voice=sum(np.sin(phase*h+.3*h)*(.25/h) for h in range(1,10))
env=np.minimum(1,t/.2)*np.minimum(1,(duration-t)/1.5)*(0.8+0.2*np.sin(t*8)**2)
sound=(voice+rough*.2)*env;sound=np.tanh(sound*1.5)*.7
sf.write(A/'sounds/bakunawa_roar.ogg',sound,sr,format='OGG',subtype='VORBIS')
sounds['bakunawa_roar']={'subtitle':'subtitles.forgottenisland.bakunawa_roar','sounds':['forgottenisland:bakunawa_roar']};lang['subtitles.forgottenisland.bakunawa_roar']='Bakunawa roars across the ocean'
write(A/'sounds.json',sounds);write(A/'lang/en_us.json',lang)
print('Generated 0.4 swords, crystal armor, horned hatching egg, boss skeleton/atlases, crafting recipes and original roar.')
