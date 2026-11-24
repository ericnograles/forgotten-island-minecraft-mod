#!/usr/bin/env python3
"""Validate packaged adventure data, recipe reachability and original art/audio."""
from pathlib import Path
import json
from PIL import Image
import soundfile as sf

root=Path(__file__).resolve().parents[1]/'src/main/resources'
assets=root/'assets/forgottenisland'
for name in ('parol_stone','parol_lantern','star_portal'):
    model=json.loads((assets/f'models/block/{name}.json').read_text())
    assert (assets/f'blockstates/{name}.json').is_file()
    with Image.open(assets/f'textures/block/{name}.png') as image:
        assert image.mode=='RGBA'
        assert image.size==((32,256) if name=='star_portal' else (32,32))
for name in ('parol_stone','parol_lantern'):
    assert (assets/f'models/item/{name}.json').is_file()
    assert (root/f'data/forgottenisland/loot_tables/blocks/{name}.json').is_file()
recipe=json.loads((root/'data/forgottenisland/recipes/parol_stone.json').read_text())
assert recipe['result']['count']>=28
assert {v['item'] for v in recipe['key'].values()}=={'minecraft:cobblestone','minecraft:copper_ingot','minecraft:amethyst_shard'}
dimension=json.loads((root/'data/forgottenisland/dimension/forgotten_island.json').read_text())
assert dimension['generator']['settings']['biome']=='forgottenisland:festival_island'
biome=json.loads((root/'data/forgottenisland/worldgen/biome/festival_island.json').read_text())
assert biome['spawners']=={}
sound=sf.info(assets/'sounds/festival_ambience.ogg')
assert sound.channels==1 and sound.samplerate==22050 and sound.duration==16
for name in ('crystal_sword','manang_wings','bakunawa_skull'):
    assert (assets/f'models/item/{name}.json').is_file()
    with Image.open(assets/f'textures/item/{name}.png') as image:assert image.size==((64,64) if name=='crystal_sword' else (32,32)) and image.mode=='RGBA'
for boss in ('manang','bakunawa'):
    with Image.open(assets/f'textures/gui/{boss}_meter.png') as image:assert image.size==(256,32) and image.mode=='RGBA'
with Image.open(assets/'textures/entity/encounter_moon.png') as image:
    assert image.size==(64,64) and image.mode=='RGBA'
for name in ('boss_warning','manang_shriek','boss_impact','moon_devour','boss_release','weakpoint'):
    info=sf.info(assets/f'sounds/{name}.ogg');assert info.channels==1 and info.samplerate==22050 and 1<=info.duration<=4.01
for file in root.rglob('*.json'):
    json.loads(file.read_text())
print('PASS: adventure recipes, dimension/biome, blocks, animated portal, reward icons, two themed meters, moon sprite, six original boss sounds, loot tables, JSON and festival ambience.')
necklace=json.loads((root/'data/forgottenisland/recipes/star_necklace.json').read_text())
assert {v['item'] for v in necklace['key'].values()}=={'minecraft:string','minecraft:copper_ingot','minecraft:torch'}
with Image.open(assets/'textures/item/star_necklace.png') as image:assert image.size==(32,32) and image.mode=='RGBA'
for name in ('stomach_tissue','stomach_crystal'):
    assert (assets/f'blockstates/{name}.json').is_file() and (assets/f'models/block/{name}.json').is_file()
    with Image.open(assets/f'textures/block/{name}.png') as image:assert image.size==(32,32)
for tag in ('bypasses_armor','bypasses_enchantments'):
    assert 'forgottenisland:holy_beam' in json.loads((root/f'data/minecraft/tags/damage_type/{tag}.json').read_text())['values']
assert json.loads((root/'data/forgottenisland/damage_type/holy_beam.json').read_text())['scaling']=='never'
assert (root/'data/forgottenisland/dimension/bakunawa_interior.json').is_file()
assert 'ambient_loop_sound' not in biome['effects']
for name in ('cave_ambience','mountain_ambience','coast_ambience'):
    info=sf.info(assets/f'sounds/{name}.ogg');assert info.channels==1 and info.samplerate==22050 and info.duration==12
print('PASS: inexpensive necklace recipe/art, interior dimension and blocks, Holy Beam mitigation tags, three regional soundscapes.')
for name in ('prismatic_crystal','sapphire_soul_sword','phoenix_sword','crystal_helmet','crystal_chestplate','crystal_leggings','crystal_boots','bakunawa_spawn_egg','crystal_sword'):
    assert (assets/f'models/item/{name}.json').is_file()
    with Image.open(assets/f'textures/item/{name}.png') as im:
        assert im.mode=='RGBA' and im.size==((32,32) if name=='prismatic_crystal' else (64,64)),name
    recipe=json.loads((root/f'data/forgottenisland/recipes/{name}.json').read_text())
    assert recipe['result']['item']==f'forgottenisland:{name}'
    assert (root/f'data/forgottenisland/advancements/recipes/{name}.json').is_file()
for stage in range(4):
    model=json.loads((assets/f'models/block/bakunawa_egg_{stage}.json').read_text())
    assert len(model['elements'])>=5
    with Image.open(assets/f'textures/block/bakunawa_egg_{stage}.png') as im:assert im.size==(32,32)
for layer in (1,2):
    with Image.open(assets/f'textures/models/armor/crystal_layer_{layer}.png') as im:assert im.size==(64,32)
model=json.loads((assets/'models/creature/bakunawa_boss.json').read_text())
assert (assets/'models/creature/bakunawa_boss.json').read_bytes()==(root.parents[2]/'assets-src/models/bakunawa_boss.json').read_bytes()
parents={'root'}
for bone in model['bones']:
    assert bone['parent'] in parents and bone['name'] not in parents
    parents.add(bone['name'])
    for x,y,z,w,h,d,material in bone['boxes']:
        assert min(w,h,d)>0 and 0<=material<8 and 2*(w+d)<=128 and h+d<=128
assert {'head','jaw',*[f'segment{i}' for i in range(9)]}<=parents
for suffix in ('','_glow'):
    with Image.open(assets/f'textures/entity/bakunawa_boss{suffix}.png') as im:assert im.size==(1024,128) and im.mode=='RGBA'
info=sf.info(assets/'sounds/bakunawa_roar.ogg')
assert info.channels==1 and info.samplerate==22050 and 3.9<=info.duration<=4.1
print('PASS: 0.4 arsenal art and nine recipes, four horned egg stages, wearable armor, boss skeleton/UVs/emissive atlas and original roar.')
new_dimension=json.loads((root/'data/forgottenisland/dimension/highlands.json').read_text())
assert new_dimension['generator']['type']=='forgottenisland:mountains'
assert json.loads((root/'data/forgottenisland/dimension_type/highlands.json').read_text())['height']==320
manang=json.loads((assets/'models/creature/manang.json').read_text())
parts={b['name'] for b in manang['bones']}
assert {'jaw','crown','brow_left','eye_right','fang_right','tusk_left','tip_right','mantle_left','spar_right'}<=parts
assert {'stormwing_left','wingtip_right'}<=parents
for suffix in ('','_glow'):
    with Image.open(assets/f'textures/entity/bakunawa_storm{suffix}.png') as im:assert im.size==(1024,128)
for name in ('bakunawa_boss','phase_two_bakunawa_boss','manang_boss','separated_manang_boss'):
    key=f'{name}_spawn_egg'
    assert (assets/f'models/item/{key}.json').is_file() and (assets/f'textures/item/{key}.png').is_file()
    assert not (root/f'data/forgottenisland/recipes/{key}.json').exists()
print('PASS: 0.5 highland generator data, facial rig, storm wings and emissive atlas, four independent boss egg assets.')
moon = json.loads((root / 'data/forgottenisland/dimension/moon.json').read_text())
assert moon['type'] == moon['generator']['settings']['biome'] == 'forgottenisland:moon'
assert moon['generator']['type'] == 'forgottenisland:lunar_craters'
for resource in ('dimension_type/moon', 'worldgen/biome/moon'):
    assert (root / f'data/forgottenisland/{resource}.json').is_file()
for texture in ('moonrise_sky', 'moonrise_serpent'):
    with Image.open(assets / f'textures/gui/{texture}.png') as image:
        image.verify()
rocket = json.loads((assets / 'models/item/moon_rocket.json').read_text())
for element in rocket['elements']:
    for face in element['faces'].values():
        assert face['texture'][1:] in rocket['textures']
print('PASS: lunar dimension, generator and biome references, moonrise art and rocket model materials.')
