from pathlib import Path
import re,json,sys
s=re.sub(r'\x1b\[[0-9;?]*[A-Za-z]','',Path(sys.argv[1]).read_text())
rows={}
for line in s.splitlines():
 if 'has the following entity data: {Brain:' not in line or 'Tags: ["qa_persist"]' not in line:continue
 name=line.split('minecraft/MinecraftServer]: ')[-1].split(' has the following')[0]
 fields={}
 for field in ['Owner','GiftGiver','Friendship','Sitting','Defend','Variant','CustomName','Pos','PersistenceRequired']:
  m=re.search(r'\b'+field+r": (\[[^\]]*\]|'[^']*'|[^,}]+)",line)
  fields[field]=m.group(1) if m else None
 fields['UUID']=re.findall(r'\bUUID: (\[[^\]]*\])',line)[-1]
 rows[name]=fields
assert len(rows)==int(sys.argv[3]),len(rows)
Path(sys.argv[2]).write_text(json.dumps(rows,sort_keys=True,indent=2)+'\n')
if len(sys.argv)>4:
 before=json.loads(Path(sys.argv[4]).read_text())
 assert before==rows,'Persistent fields differ'
 print(f'PASS: {len(rows)} companions have identical UUID/owner/gifts/friendship/commands/variant/name/position after reload.')
else:print(f'Saved {len(rows)} companions.')
