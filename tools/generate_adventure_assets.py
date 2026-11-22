#!/usr/bin/env python3
"""Original adventure pixel art, models, data and synthesized festival ambience.
Does not touch any existing companion asset. No film image/audio is sampled.
"""
from pathlib import Path
import json, math, random
from PIL import Image, ImageDraw
import numpy as np
import soundfile as sf
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
A=RES/'assets/forgottenisland'
def write(path,obj):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(obj,indent=2)+'\n')
def tex(name):
    p=A/f'textures/block/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);return p
# Chiseled stone with copper inlay; glowing paper parol star, each authored at 32 px.
for name,bg,accent in [('parol_stone','#374c5b','#ebb866'),('parol_lantern','#773660','#fff2ad')]:
    im=Image.new('RGBA',(32,32),bg);d=ImageDraw.Draw(im)
    for k in range(2):d.rectangle((k,k,31-k,31-k),outline='#b87564' if k==0 else '#192f43')
    points=[(16+math.sin(i*math.pi/5)*(13 if i%2==0 else 6),16-math.cos(i*math.pi/5)*(13 if i%2==0 else 6)) for i in range(10)]
    d.polygon(points,fill=accent);d.line(points+[points[0]],fill='#f9df8b',width=1)
    d.polygon([(16,8),(20,16),(16,24),(12,16)],fill='#87e6db' if name=='parol_stone' else '#ffbb75')
    im.save(tex(name))
    write(A/f'blockstates/{name}.json',{'variants':{'':{'model':f'forgottenisland:block/{name}'}}})
    write(A/f'models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'forgottenisland:block/{name}'}})
    write(A/f'models/item/{name}.json',{'parent':f'forgottenisland:block/{name}'})
    write(RES/f'data/forgottenisland/loot_tables/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'forgottenisland:{name}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
# The floor aperture is an animated translucent sheet, not a vertical portal.
im=Image.new('RGBA',(32,256));rng=random.Random(408)
for f in range(8):
    for y in range(32):
        for x in range(32):
            wave=math.sin((x+y)*.35-f*math.pi/4)*.5+.5
            glint=1 if ((x*17+y*31+f*5)%83==0) else 0
            im.putpixel((x,y+f*32),(int(75+130*wave+50*glint),int(80+90*wave+80*glint),min(255,int(165+70*wave+20*glint)),210))
im.save(tex('star_portal'));write(tex('star_portal').with_suffix('.png.mcmeta'),{'animation':{'frametime':3,'interpolate':True}})
write(A/'blockstates/star_portal.json',{'variants':{'':{'model':'forgottenisland:block/star_portal'}}})
write(A/'models/block/star_portal.json',{'render_type':'minecraft:translucent','textures':{'portal':'forgottenisland:block/star_portal','particle':'forgottenisland:block/star_portal'},'elements':[{'from':[0,1,0],'to':[16,3,16],'faces':{face:{'texture':'#portal','tintindex':-1} for face in ['up','down','north','south','east','west']}}]})
write(RES/'data/forgottenisland/recipes/parol_stone.json',{'type':'minecraft:crafting_shaped','pattern':['SCS','CAC','SCS'],'key':{'S':{'item':'minecraft:cobblestone'},'C':{'item':'minecraft:copper_ingot'},'A':{'item':'minecraft:amethyst_shard'}},'result':{'item':'forgottenisland:parol_stone','count':32}})
write(RES/'data/forgottenisland/recipes/parol_lantern.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'forgottenisland:parol_stone'},{'item':'minecraft:torch'},{'item':'minecraft:paper'}],'result':{'item':'forgottenisland:parol_lantern','count':2}})
write(RES/'data/minecraft/tags/blocks/mineable/pickaxe.json',{'replace':False,'values':['forgottenisland:parol_stone']})
write(RES/'data/forgottenisland/dimension_type/forgotten_island.json',{'ultrawarm':False,'natural':True,'coordinate_scale':1.0,'has_skylight':True,'has_ceiling':False,'ambient_light':.16,'fixed_time':12500,'piglin_safe':False,'bed_works':False,'respawn_anchor_works':False,'has_raids':False,'logical_height':256,'min_y':0,'height':256,'infiniburn':'#minecraft:infiniburn_overworld','effects':'minecraft:overworld','monster_spawn_block_light_limit':0,'monster_spawn_light_level':0})
write(RES/'data/forgottenisland/worldgen/biome/festival_island.json',{'has_precipitation':False,'temperature':.9,'downfall':.5,'effects':{'fog_color':11055269,'sky_color':8170955,'water_color':4258435,'water_fog_color':2700408,'grass_color':7259734,'foliage_color':5885526,'ambient_loop_sound':'forgottenisland:festival_ambience'},'spawners':{},'spawn_costs':{},'carvers':{},'features':[]})
write(RES/'data/forgottenisland/dimension/forgotten_island.json',{'type':'forgottenisland:forgotten_island','generator':{'type':'minecraft:flat','settings':{'biome':'forgottenisland:festival_island','lakes':False,'features':False,'layers':[{'block':'minecraft:bedrock','height':1},{'block':'minecraft:stone','height':55},{'block':'minecraft:water','height':7}],'structure_overrides':[]}}})
# Quiet original repeating wooden chimes, hand-drum pulses and a filtered sea bed.
sr=22050;duration=16;n=sr*duration;t=np.arange(n)/sr
signal=np.zeros(n)
for i,pitch in enumerate([440,554.37,659.25,830.61,659.25,554.37,493.88,659.25]):
    start=int((i*2+.25)*sr);tt=np.arange(min(sr*2,n-start))/sr
    note=(np.sin(2*np.pi*pitch*tt)+.3*np.sin(2*np.pi*pitch*2.76*tt))*np.exp(-tt*4)*.10
    signal[start:start+len(tt)]+=note
for beat in range(32):
    start=int(beat*.5*sr);tt=np.arange(int(sr*.22))/sr
    signal[start:start+len(tt)]+=.065*np.sin(2*np.pi*(94*tt-45*tt*tt))*np.exp(-tt*18)
signal+=np.random.default_rng(408).normal(0,.002,n)*(1+np.sin(t*.7))
(A/'sounds').mkdir(exist_ok=True);sf.write(A/'sounds/festival_ambience.ogg',signal,sr,format='OGG',subtype='VORBIS')
sounds=json.loads((A/'sounds.json').read_text());sounds['festival_ambience']={'subtitle':'subtitles.forgottenisland.festival','sounds':[{'name':'forgottenisland:festival_ambience','stream':False,'volume':.45}]};write(A/'sounds.json',sounds)
lang=json.loads((A/'lang/en_us.json').read_text());lang.update({'block.forgottenisland.parol_stone':'Parol Stone','block.forgottenisland.star_portal':'Homeward Star','block.forgottenisland.parol_lantern':'Parol Lantern','subtitles.forgottenisland.festival':'Festival chimes and sea breeze'});write(A/'lang/en_us.json',lang)
for name in ('crystal_sword','manang_wings','bakunawa_skull'):
    im=Image.new('RGBA',(32,32),(0,0,0,0));d=ImageDraw.Draw(im)
    if name=='crystal_sword':
        d.polygon([(11,23),(23,3),(29,1),(28,8),(16,25)],fill='#113249',outline='#80fff0')
        d.polygon([(14,21),(25,5),(25,10),(17,23)],fill='#c5ffef')
        d.line([(8,19),(20,27)],fill='#e8bc60',width=3);d.line([(13,24),(8,30)],fill='#854c80',width=4)
        d.ellipse((6,27,10,31),fill='#f5d58c')
    elif name=='manang_wings':
        for side in (-1,1):
            points=[(16,17),(16+side*9,3),(16+side*15,2),(16+side*12,11),(16+side*14,24),(16+side*8,19),(16+side*5,28)]
            d.polygon(points,fill='#aa407d',outline='#201b32');d.line(points[:3],fill='#f1c56a',width=2)
            d.line([(16,17),(16+side*11,9),(16+side*8,19)],fill='#4c284d',width=2)
    else:
        d.polygon([(6,12),(1,2),(9,7),(13,10),(19,10),(23,7),(30,2),(26,14),(27,23),(21,29),(11,29),(5,23)],fill='#e9d9a8',outline='#28536a')
        d.polygon([(6,16),(14,17),(12,21),(7,20)],fill='#172e46');d.polygon([(18,17),(26,16),(25,20),(20,21)],fill='#172e46')
        d.polygon([(14,22),(18,22),(16,25)],fill='#448f94');d.rectangle((10,25,22,28),fill='#f9efcf')
        for x in (12,16,20):d.line([(x,25),(x,28)],fill='#526d75')
        d.ellipse((14,11,18,15),fill='#6be4d0')
    im.save(A/f'textures/item/{name}.png')
    write(A/f'models/item/{name}.json',{'parent':'minecraft:item/handheld' if name=='crystal_sword' else 'minecraft:item/generated','textures':{'layer0':f'forgottenisland:item/{name}'}})
lang.update({'item.forgottenisland.crystal_sword':'Crystal Sword','item.forgottenisland.manang_wings':'Manang Wings','item.forgottenisland.bakunawa_skull':'Bakunawa Skull',
 'entity.forgottenisland.manang_boss':'Manang, The Severed Twilight','entity.forgottenisland.bakunawa_boss':'Bakunawa, Devourer of Moons',
 'key.forgottenisland.skip_scene':'Skip my boss cinematic','key.forgottenisland.scene_mode':'Cycle boss cinematic mode'})
# Original carved borders: bat membrane and a pearl-and-scale dragon frame.
(A/'textures/gui').mkdir(exist_ok=True)
for name,accent in [('manang','#ef7cae'),('bakunawa','#78e2e7')]:
    im=Image.new('RGBA',(256,32),(0,0,0,0));d=ImageDraw.Draw(im)
    d.rectangle((20,16,235,26),outline='#ead5a2',width=1);d.line((24,27,231,27),fill='#39445d')
    if name=='manang':
        for side in (-1,1):
            cx=16 if side==-1 else 239
            pts=[(cx,20),(cx+side*14,7),(cx+side*11,21),(cx+side*6,18),(cx+side*4,28)]
            d.polygon(pts,fill='#743d67',outline=accent)
            d.ellipse((cx-4,16,cx+4,24),fill='#f4cf8c',outline='#3f294c')
    else:
        for x in (12,244):
            d.ellipse((x-7,12,x+7,26),fill='#e8d6a2',outline=accent)
            d.ellipse((x-3,10,x+9,22),fill='#152e42')
        for x in range(26,231,9):d.polygon([(x,15),(x+4,11),(x+8,15)],fill='#397d8b',outline=accent)
    im.save(A/f'textures/gui/{name}_meter.png')
# All boss sounds are synthesized here from oscillators and seeded noise (no samples).
im=Image.new('RGBA',(64,64),(0,0,0,0));d=ImageDraw.Draw(im)
d.ellipse((2,2,61,61),fill='#f9e8b5',outline='#ffefcf',width=2)
for x,y,r in [(17,20,5),(41,14,4),(36,39,8),(18,46,4),(48,46,3)]:
    d.ellipse((x-r,y-r,x+r,y+r),fill='#d3c195');d.arc((x-r+1,y-r+1,x+r+1,y+r+1),30,190,fill='#ead8a8',width=2)
im.save(A/'textures/entity/encounter_moon.png')
for name,duration,subtitle in [('boss_warning',1.5,'Danger gathers'),('manang_shriek',2.4,'Manang shrieks'),('boss_impact',1.2,'Island magic strikes'),('moon_devour',4.0,'The moon is swallowed'),('boss_release',4.0,'The island releases a spirit'),('weakpoint',1.0,'A weak point opens')]:
    t=np.arange(int(sr*duration))/sr;envelope=np.minimum(1,t/.06)*np.exp(-t/(duration*.35));noise=np.random.default_rng(719).normal(0,1,len(t))
    if name=='manang_shriek':
        phase=2*np.pi*(350*t+130*t*t+12*np.sin(2*np.pi*5*t));s=(np.sin(phase)+.22*np.sin(phase*2.02)+noise*.07)*envelope*.22
    elif name=='moon_devour':
        s=(np.sin(2*np.pi*(62*t-4*t*t))+.3*np.sin(2*np.pi*93*t)+noise*.10)*envelope*.25
        s+=np.sin(2*np.pi*880*t)*np.exp(-((t-2.6)/.5)**2)*.055
    elif name=='boss_release':
        s=sum(np.sin(2*np.pi*f*t)*np.exp(-t/(1.3+i*.2))*.07 for i,f in enumerate((220,330,440,554.37,659.25)))*np.minimum(1,t/.2)
    elif name=='boss_impact':s=(np.sin(2*np.pi*(100*t-38*t*t))+noise*.25)*envelope*.25
    elif name=='weakpoint':s=(np.sin(2*np.pi*880*t)+.5*np.sin(2*np.pi*1320*t))*envelope*.15
    else:s=(np.sin(2*np.pi*(220*t+80*t*t))+.3*np.sin(2*np.pi*440*t))*envelope*.18
    sf.write(A/f'sounds/{name}.ogg',np.clip(s,-.8,.8),sr,format='OGG',subtype='VORBIS')
    sounds[name]={'subtitle':f'subtitles.forgottenisland.{name}','sounds':[f'forgottenisland:{name}']};lang[f'subtitles.forgottenisland.{name}']=subtitle
write(A/'sounds.json',sounds);write(A/'lang/en_us.json',lang)
print('Generated adventure blocks, rewards, portal animation, Survival recipes, dimension, biome and original festival loop.')
