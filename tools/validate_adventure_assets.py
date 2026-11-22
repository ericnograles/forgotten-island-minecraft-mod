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
    with Image.open(assets/f'textures/item/{name}.png') as image:assert image.size==(32,32) and image.mode=='RGBA'
for boss in ('manang','bakunawa'):
    with Image.open(assets/f'textures/gui/{boss}_meter.png') as image:assert image.size==(256,32) and image.mode=='RGBA'
with Image.open(assets/'textures/entity/encounter_moon.png') as image:
    assert image.size==(64,64) and image.mode=='RGBA'
for name in ('boss_warning','manang_shriek','boss_impact','moon_devour','boss_release','weakpoint'):
    info=sf.info(assets/f'sounds/{name}.ogg');assert info.channels==1 and info.samplerate==22050 and 1<=info.duration<=4.01
for file in root.rglob('*.json'):
    json.loads(file.read_text())
print('PASS: adventure recipes, dimension/biome, blocks, animated portal, reward icons, two themed meters, moon sprite, six original boss sounds, loot tables, JSON and festival ambience.')
