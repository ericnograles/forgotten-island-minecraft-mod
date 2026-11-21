#!/usr/bin/env python3
"""Check complete registered asset coverage, mesh references, UV bounds and audio."""
from pathlib import Path
import json, re
from PIL import Image
import soundfile as sf

root = Path(__file__).resolve().parents[1]
assets = root / 'src/main/resources/assets/forgottenisland'
species = re.findall(r'^    [A-Z]+\("([a-z]+)"', (root / 'src/main/java/com/forgottenisland/entity/Species.java').read_text(), re.M)
sounds = json.loads((assets / 'sounds.json').read_text())
language = json.loads((assets / 'lang/en_us.json').read_text())
boxes = bones = 0
for name in species:
    path = assets / f'models/creature/{name}.json'
    assert path.read_bytes() == (root / f'assets-src/models/{name}.json').read_bytes(), f'{name}: editable source differs'
    model = json.loads(path.read_text())
    parents = {'root': 'root'}
    paths = set()
    for bone in model['bones']:
        assert bone['parent'] in parents, f'{name}: missing parent {bone["parent"]}'
        full = parents[bone['parent']] + '/' + bone['name']
        assert full not in paths, f'{name}: duplicate bone {full}'
        paths.add(full)
        parents[bone['name']] = full
        bones += 1
        for box in bone['boxes']:
            x,y,z,w,h,d,material = box
            assert min(w,h,d) > 0 and 0 <= material < 8, f'{name}: invalid box'
            assert 2*(w+d) <= 128 and h+d <= 128, f'{name}: UV outside material region'
            boxes += 1
    with Image.open(assets / f'textures/entity/{name}.png') as texture:
        assert texture.size == (1024,128) and texture.mode == 'RGBA', name
    assert f'entity.forgottenisland.{name}' in language, name
    assert f'item.forgottenisland.{name}_spawn_egg' in language, name
    assert (assets / f'models/item/{name}_spawn_egg.json').exists(), name
    assert name + '_call' in sounds, name
    info = sf.info(assets / f'sounds/{name}_call.ogg')
    assert info.channels == 1 and info.samplerate == 22050 and .5 < info.duration < 4, name
print(f'PASS: {len(species)} species; {bones} bones; {boxes} boxes; matching editable models, textures, eggs, labels and mono OGG calls.')
