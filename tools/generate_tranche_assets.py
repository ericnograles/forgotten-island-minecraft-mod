#!/usr/bin/env python3
"""Original pixel assets and synthesized environmental loops; extends the existing art pipeline."""
from pathlib import Path
import json, math
import numpy as np
import soundfile as sf
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
A=ROOT/'assets/forgottenisland'
def write(p,obj):
    p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(json.dumps(obj,indent=2)+'\n')
im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
d.arc((5,0,26,23),0,180,fill='#8d633b',width=3)
d.arc((6,0,25,22),0,180,fill='#f7d58b',width=1)
points=[(16+math.sin(i*math.pi/5)*(10 if i%2==0 else 4),22-math.cos(i*math.pi/5)*(10 if i%2==0 else 4)) for i in range(10)]
d.polygon(points,fill='#e8b56f',outline='#563f5b');d.polygon([(16,17),(20,22),(16,27),(12,22)],fill='#79ede2');d.point((16,20),fill='#ffffff')
im.save(A/'textures/item/star_necklace.png')
write(A/'models/item/star_necklace.json',{'parent':'minecraft:item/generated','textures':{'layer0':'forgottenisland:item/star_necklace'}})
write(ROOT/'data/forgottenisland/recipes/star_necklace.json',{'type':'minecraft:crafting_shaped','pattern':['S S',' C ',' T '],'key':{'S':{'item':'minecraft:string'},'C':{'item':'minecraft:copper_ingot'},'T':{'item':'minecraft:torch'}},'result':{'item':'forgottenisland:star_necklace'}})
write(ROOT/'data/forgottenisland/recipes/creature_guide.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'minecraft:book'},{'item':'minecraft:feather'}],'result':{'item':'forgottenisland:creature_guide'}})
for name in ('stomach_tissue','stomach_crystal'):
    im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
    for y in range(32):
        for x in range(32):
            wave=(math.sin(x*.5+math.sin(y*.2)*2)+math.cos(y*.4))*.5
            im.putpixel((x,y),(int(88+wave*16),int(32+wave*9),int(65+wave*18),255) if name=='stomach_tissue' else (int(142+wave*32),int(199+wave*20),int(218+wave*25),255))
    if name=='stomach_tissue':
        for n in range(3):d.line([(n*11,0),(n*11+4,8),(n*11-2,17),(n*11+3,31)],fill='#b85f87',width=2)
    else:
        d.polygon([(16,1),(30,16),(16,30),(1,16)],outline='#fff8bf',width=2);d.line((16,3,16,29),fill='#ffffff',width=2)
    im.save(A/f'textures/block/{name}.png')
    write(A/f'blockstates/{name}.json',{'variants':{'':{'model':f'forgottenisland:block/{name}'}}})
    write(A/f'models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'forgottenisland:block/{name}'}})
dimtype=json.loads((ROOT/'data/forgottenisland/dimension_type/forgotten_island.json').read_text())
dimtype.update(has_skylight=False,has_ceiling=True,natural=False,fixed_time=18000,ambient_light=.12)
write(ROOT/'data/forgottenisland/dimension_type/bakunawa_interior.json',dimtype)
biome=json.loads((ROOT/'data/forgottenisland/worldgen/biome/festival_island.json').read_text())
biome['effects'].pop('ambient_loop_sound',None)
write(ROOT/'data/forgottenisland/worldgen/biome/festival_island.json',biome)
biome['effects'].update(fog_color=6105169,sky_color=2493751,water_color=11159911)
write(ROOT/'data/forgottenisland/worldgen/biome/bakunawa_interior.json',biome)
write(ROOT/'data/forgottenisland/dimension/bakunawa_interior.json',{'type':'forgottenisland:bakunawa_interior','generator':{'type':'minecraft:flat','settings':{'biome':'forgottenisland:bakunawa_interior','lakes':False,'features':False,'layers':[{'block':'minecraft:bedrock','height':1}],'structure_overrides':[]}}})
write(ROOT/'data/forgottenisland/damage_type/holy_beam.json',{'message_id':'holy_beam','scaling':'never','exhaustion':.1,'effects':'hurt'})
for tag in ('bypasses_armor','bypasses_enchantments'):
    write(ROOT/f'data/minecraft/tags/damage_type/{tag}.json',{'replace':False,'values':['forgottenisland:holy_beam']})
sounds=json.loads((A/'sounds.json').read_text());lang=json.loads((A/'lang/en_us.json').read_text())
lang.update({'item.forgottenisland.star_necklace':'Homeward Star Necklace','block.forgottenisland.stomach_tissue':'Bakunawa Tissue','block.forgottenisland.stomach_crystal':'Memory Crystal','death.attack.holy_beam':'%1$s was unmade by Bakunawa’s Holy Beam','death.attack.holy_beam.player':'%1$s was unmade by Bakunawa’s Holy Beam while fighting %2$s'})
sr=22050;t=np.arange(sr*12)/sr;noise=np.random.default_rng(183).normal(0,1,len(t));soft=np.convolve(noise,np.ones(90)/90,mode='same')
for name,subtitle in [('cave_ambience','Memory cave echoes'),('mountain_ambience','Wind across the twilight ridge'),('coast_ambience','Waves roll under the sea arch')]:
    if name=='cave_ambience':signal=.10*soft+sum(np.sin(2*np.pi*f*t)*np.exp(-((t-2-i*3)/.8)**2)*.07 for i,f in enumerate((330,494,660)))
    elif name=='mountain_ambience':signal=soft*(.3+.25*np.sin(t*.9))+.02*np.sin(2*np.pi*140*t)*np.sin(t*.5)**2
    else:signal=soft*.8*(.3+.7*np.sin(t*.65)**2)
    signal*=np.minimum(1,t)*np.minimum(1,12-t)
    sf.write(A/f'sounds/{name}.ogg',signal,sr,format='OGG',subtype='VORBIS')
    sounds[name]={'subtitle':f'subtitles.forgottenisland.{name}','sounds':[f'forgottenisland:{name}']};lang[f'subtitles.forgottenisland.{name}']=subtitle
write(A/'sounds.json',sounds);write(A/'lang/en_us.json',lang)
print('Generated necklace, interior, damage tags and three original regional soundscapes.')
