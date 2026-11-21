#!/usr/bin/env python3
"""Reproduce the empty 8×5×8 Forge GameTest template (NBT, no third-party library)."""
from pathlib import Path
import gzip, struct

def text(value):
    raw=value.encode('utf-8')
    return struct.pack('>H',len(raw))+raw

def tag(kind,name,value):
    return bytes([kind])+text(name)+value

nbt=tag(10,'',
    tag(3,'DataVersion',struct.pack('>i',3465))+
    tag(9,'size',bytes([3])+struct.pack('>iiii',3,8,5,8))+
    tag(9,'palette',bytes([10])+struct.pack('>i',1)+tag(8,'Name',text('minecraft:air'))+b'\0')+
    tag(9,'blocks',bytes([10])+struct.pack('>i',0))+
    tag(9,'entities',bytes([10])+struct.pack('>i',0))+b'\0')
path=Path(__file__).resolve().parents[1]/'src/main/resources/data/forgottenisland/structures/empty.nbt'
path.write_bytes(gzip.compress(nbt,mtime=0))
print(path)
