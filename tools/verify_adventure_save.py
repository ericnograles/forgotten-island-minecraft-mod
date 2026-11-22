#!/usr/bin/env python3
"""Read an offline QA world; compare durable island links/population/portal blocks.
This is read-only and uses standard-library NBT/Anvil decoding. Stop or flush-save
Minecraft first. It never edits chunks, entities, player data or save markers.
"""
from pathlib import Path
import gzip, json, struct, sys, zlib
from collections import Counter

class NBT:
    def __init__(self,data): self.data=data;self.pos=0
    def take(self,n): b=self.data[self.pos:self.pos+n];self.pos+=n;return b
    def number(self,fmt):return struct.unpack('>'+fmt,self.take(struct.calcsize('>'+fmt)))[0]
    def string(self):return self.take(self.number('H')).decode('utf-8')
    def payload(self,kind):
        if kind in (1,2,3,4,5,6):return self.number({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[kind])
        if kind==7:return list(self.take(self.number('i')))
        if kind==8:return self.string()
        if kind==9:
            t=self.number('B');n=self.number('i');return [self.payload(t) for _ in range(n)]
        if kind==10:
            result={}
            while True:
                t=self.number('B')
                if t==0:return result
                name=self.string();result[name]=self.payload(t)
        if kind in (11,12):return [self.number('i' if kind==11 else 'q') for _ in range(self.number('i'))]
        raise ValueError(f'Unexpected NBT type: {kind}')
    def root(self):
        t=self.number('B');self.string();return self.payload(t)

def region_chunks(path):
    data=path.read_bytes()
    for i in range(1024):
        offset=int.from_bytes(data[i*4:i*4+3],'big')*4096
        if not offset:continue
        length=int.from_bytes(data[offset:offset+4],'big');compression=data[offset+4]
        raw=data[offset+5:offset+4+length]
        if compression==2:raw=zlib.decompress(raw)
        elif compression==1:raw=gzip.decompress(raw)
        elif compression!=3:raise ValueError(f'Unsupported region compression: {compression}')
        yield i,NBT(raw).root()

def block_at(directory,x,y,z):
    path=directory/f'r.{x//512}.{z//512}.mca'
    target=(x//16%32)+(z//16%32)*32
    chunk=next(data for index,data in region_chunks(path) if index==target)
    section=next(s for s in chunk['sections'] if s['Y']==y//16)
    states=section['block_states'];palette=states['palette'];bits=max(4,(len(palette)-1).bit_length())
    if len(palette)==1:return palette[0]['Name']
    index=(y%16)*256+(z%16)*16+x%16;per_long=64//bits
    value=(states['data'][index//per_long] >> ((index%per_long)*bits)) & ((1<<bits)-1)
    return palette[value]['Name']

def snapshot(world):
    world=Path(world);island=world/'dimensions/forgottenisland/forgotten_island'
    data=NBT(gzip.decompress((world/'data/forgottenisland_adventure.dat').read_bytes())).root()['data']
    residents={};bosses={};rewards={}
    for region in sorted((island/'entities').glob('*.mca')):
        for _,chunk in region_chunks(region):
            for entity in chunk.get('Entities',[]):
                extra=entity.get('ForgeData',{})
                key=','.join(str(n) for n in entity.get('UUID',[]))
                if entity['id'].endswith('_boss'):bosses[key]={'id':entity['id'],'health':entity.get('Health')}
                if extra.get('IslandReward'):
                    token=extra['IslandReward'];assert token not in rewards,f'Duplicate dropped reward: {token}'
                    rewards[token]={'location':'drop','uuid':key,'id':entity['Item']['id'],'count':entity['Item']['Count'],'invulnerable':entity.get('Invulnerable'),'age':entity.get('Age')}
                if not extra.get('IslandMarketResident'):continue
                assert key not in residents, f'Duplicate resident UUID: {key}'
                residents[key]={'id':entity['id'],'home':extra['IslandMarketHome'],'persistent':entity.get('PersistenceRequired'),'invulnerable':entity.get('Invulnerable')}
    assert len(residents)==21,f'Expected 21 saved residents, got {len(residents)}'
    counts=dict(sorted(Counter(e['id'] for e in residents.values()).items()))
    assert len(counts)==11,f'Expected eleven non-boss creature types: {counts}'
    portal=block_at(island/'region',0,64,50)
    assert portal=='forgottenisland:star_portal',f'Retreat portal missing: {portal}'
    for file in (world/'playerdata').glob('*.dat'):
        player=NBT(gzip.decompress(file.read_bytes())).root()
        for item in player.get('Inventory',[])+player.get('EnderItems',[]):
            token=item.get('tag',{}).get('IslandReward')
            if token:
                assert token not in rewards,f'Duplicate reward in inventory: {token}'
                rewards[token]={'location':'inventory','owner':file.stem,'id':item['id'],'count':item['Count']}
    encounters=data.get('Encounters',{})
    for state in encounters.values():
        for key in ('Delivered','Collected'):state[key]=sorted(state.get(key,[]))
        state['Participants']=sorted(state.get('Participants',[]),key=lambda p:p['Id'])
    exits=sorted(data.get('Exits',[]));exit_blocks={}
    if 'bakunawa_boss' in exits:
        exit_blocks['bakunawa']=block_at(island/'region',112,64,-22)
        assert exit_blocks['bakunawa']=='forgottenisland:star_portal','Completed Bakunawa exit missing'
    for name,state in encounters.items():
        if state.get('Stage')=='COMPLETE':assert all(b['id']!='forgottenisland:'+name for b in bosses.values()),f'Completed boss still present: {name}'
    return {'residents':residents,'counts':counts,'departures':data.get('Departures',[]),'portals':data.get('Portals',[]),'arrival_portal':portal,'encounters':encounters,'bosses':bosses,'rewards':rewards,'exits':exit_blocks}

if __name__=='__main__':
    result=snapshot(sys.argv[1]);out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
    out.write_text(json.dumps(result,indent=2,sort_keys=True)+'\n')
    if len(sys.argv)>3:
        before=json.loads(Path(sys.argv[3]).read_text())
        assert all(result.get(k)==v for k,v in before.items()),'Persistent population, encounters, rewards, links or portals changed across reload'
        print('PASS: identical saved population, links, encounters, unique rewards and portals across reload.')
    else:print('PASS: saved 21 unique residents / eleven types; verified encounter completion, reward uniqueness and portal blocks.')
