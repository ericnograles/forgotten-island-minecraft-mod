#!/usr/bin/env python3
"""Original mesh, pixel atlas and synthesized sound sources. Python 3 + Pillow + SoundFile.
Model units are 1/16 block; origin is the creature's feet; negative Y points up.
Run from any directory. Generated JSON remains directly editable in assets-src/models.
"""
from pathlib import Path
import json, math, random
import soundfile as sf
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources/assets/forgottenisland'
SRC = ROOT / 'assets-src'
for folder in ['models/creature','models/item','textures/entity','textures/item','textures/particle','particles','sounds','lang']:
    (RES/folder).mkdir(parents=True,exist_ok=True)
(SRC/'models').mkdir(parents=True,exist_ok=True)

PALETTES = {
    'bakunawa':['244c6c','3d9b9a','e8c775','12243c','f9efc9','7cffe3','6276ba','bbebd3'],
    'raww':['bb8148','b9394b','efda98','493029','fff1d5','59452f','e6b877','75513a'],
    'manang':['4c6a9e','442b52','af376a','191c30','e8dfca','ffcc45','8a4b83','73486d'],
    'sarimanok':['c63743','d45b48','e9b54c','362e40','fff0ce','40c5be','a92d67','f17a50'],
    'datu':['83654c','4c6b45','9ca24c','3f352c','eee1a1','efc85c','b1976a','315440'],
    'duwende':['dbcfb7','347b64','d9e090','293f43','fff8dc','334d54','b59864','547f43'],
    'bungi':['618dd1','b19159','d3bd8d','3a3960','f7eddb','332b4d','85b2e1','d1b5de'],
    'batiba':['d3af69','3c677e','a7dbcc','79503f','fff2d0','55e5ab','edcc92','b87494'],
    'mermahn':['7bb7ba','4394a5','ba517d','273243','e0eeee','68d5ef','caaa7c','3c697f'],
    'zippy':['75d39d','a44eb4','da84e5','343650','f1ffea','86efd9','bce5ff','588f87'],
    'tikbalang':['87634e','64535f','d3b482','302e36','fff0d4','4e3340','b18b69','493e47'],
    'sarangay':['874975','a5647e','deb25f','303149','fff0c2','d8f17e','b98fe3','613c64'],
    'flamingchef':['563d58','eee0c5','bf784c','292b38','fff3b0','ffb445','ed6536','348c87'],
}

class Model:
    def __init__(self): self.bones=[]
    def bone(self,name,pivot=(0,0,0),boxes=(),parent='root',rotation=(0,0,0)):
        self.bones.append(dict(name=name,parent=parent,pivot=list(pivot),rotation=list(rotation),boxes=list(boxes)))
        return name
    def eyes(self,parent,w,y,z,size=2,iris=5):
        for side in [-1,1]:
            self.bone('eye_'+('left' if side<0 else 'right'),(side*w,y,z),[
                [-size/2,-size/2,-.2,size,size,.6,4],[-.5,-size*.32,-.6,1,size*.68,.5,iris]],parent)
    def export(self,name):
        text=json.dumps({'texture_width':1024,'texture_height':128,'bones':self.bones},indent=2)+'\n'
        (SRC/'models'/f'{name}.json').write_text(text)
        (RES/'models/creature'/f'{name}.json').write_text(text)

def bakunawa():
    m=Model()
    m.bone('head',(0,-22,-19),[[-10,-7,-11,20,14,18,0],[-8,-2,-21,16,7,12,1],[-7,4,-18,14,2,15,7],[-6,-8,-12,12,2,14,6]])
    m.bone('jaw',(0,5,-9),[[-7,0,-11,14,3,14,7],[-6,-2,-10,2,2,2,4],[4,-2,-10,2,2,2,4]],'head')
    for s in [-1,1]:
        m.bone('horn_'+str(s),(s*8,-6,1),[[-2,-12,-1,4,13,4,2],[-1.5,-18,2,3,9,3,2]],'head',(.4,0,s*.25))
        m.bone('whisker_'+str(s),(s*8,2,-16),[[0,0,0,14,1,1,2]],'head',(0,-s*.5,0 if s==1 else math.pi))
        m.bone('cheek_fin_'+str(s),(s*9,0,0),[[-1,-8,0,2,16,10,6]],'head',(.1,0,s*.7))
        m.bone('eye_'+str(s),(s*10,-3,-9),[[-.4,-2,-1,1,4,5,5],[-.6,-1,0,1.5,2,2,3]],'head')
    parent='root'
    for i in range(9):
        size=18-i*1.45
        m.bone(f'segment{i}',(0,-20,0) if i==0 else (0,0,12),[
            [-size/2,-size/2,0,size,size,14,0],[-size*.39,size*.3,0,size*.78,size*.22,14,7],
            [-1,-size/2-5,3,2,6,8,6],[-size/2-.15,-2,5,size+.3,3,3,1]],parent)
        parent=f'segment{i}'
    m.bone('tail_fan',(0,0,13),[[-1,-10,0,2,20,13,6],[-7,-1,0,14,2,14,1]],parent)
    for s,n in [(-1,'left'),(1,'right')]:
        m.bone('fin_'+n,(s*7,-21,6),[[-2,-1,0,4,2,22,1],[-12,-.5,7,24,1,14,6]],rotation=(.3,s*.6,s*.2))
    return m

def raww():
    m=Model();m.bone('upright')
    m.bone('torso',(0,-17,0),[[-5,-7,-3,10,14,6,0],[-5.5,-7,-3.4,3,12,7,1],[2.5,-7,-3.4,3,12,7,1],[-5.5,-7,3,11,12,1,1]],'upright')
    m.bone('head',(0,-25,-1),[[-4,-7,-4,8,8,8,0],[-3,-3,-8,6,4,5,6],[-2,-3.3,-8.4,4,1.5,1,3],[-4.2,-5.5,-4.2,8.4,1.3,8.4,2]],'upright')
    m.eyes('head',2,-3.6,-4.35,2.6)
    m.bone('hair',(0,-7,0),[[-4,-3,-2,8,3,6,7],[-3,-6,0,3,5,3,7],[1,-5,1,3,4,3,7]],'head',(-.2,0,-.15))
    m.bone('ear_left',(-3,-6,1),[[-1,-6,-1,3,7,2,0],[-.4,-4,-1.1,1.5,4,1,7]],'head',(0,0,-.2))
    m.bone('ear_right',(3,-6,1),[[-2,-5,-1,3,6,2,0],[-1.5,-3,-1.1,1.5,3,1,7]],'head',(0,0,.35))
    for s,n in [(-1,'left'),(1,'right')]:
        m.bone('arm_'+n,(s*6,-22,0),[[-1.5,0,-2,3,11,4,0],[-2,9,-2.5,4,3,5,6]],'upright')
        m.bone('leg_'+n,(s*2.5,-10,0),[[-2,0,-2,4,9,4,7],[-2,7,-4,4,3,6,0]],'upright')
    m.bone('tail',(0,-11,3),[[-1.5,0,0,3,3,12,0],[-1.6,-.1,9,3.2,3.2,4,6]],'upright',(-.45,0,0))
    m.bone('dog',(0,-7,0),[[-4,-3,-7,8,7,15,0],[-4.3,-3.4,-5,8.6,1,11,1]])
    m.bone('head',(0,-2,-7),[[-3,-5,-5,6,6,6,0],[-2,-1,-8,4,3,4,6],[-1.5,-1.3,-8.3,3,1,1,3],[-3.1,-4,-5.1,6.2,1,6.2,2]],'dog')
    # Eyes/ears need globally unique names for the tree-building pass; child names can repeat only between hierarchies in runtime.
    m.bone('dog_ears',(0,-4,0),[[-3,-5,0,2,5,2,0],[1,-4,0,2,4,2,0]],'head')
    for s in [-1,1]:m.bone('dog_eye'+str(s),(s*1.7,-2.3,-5.2),[[-.8,-.8,0,1.6,1.6,.5,4],[-.4,-.5,-.1,.8,1,.5,3]],'head')
    for name,x,z in [('front_left',-3,-5),('front_right',3,-5),('back_left',-3,5),('back_right',3,5)]:
        m.bone(name,(x,2,z),[[-1,0,-1.5,2,5,3,0]],'dog')
    m.bone('tail',(0,-2,7),[[-1,-1,0,2,2,10,0]],'dog',(-.5,0,0))
    return m

def sarimanok():
    m=Model();m.bone('body',(0,-20,1),[[-7,-8,-9,14,16,20,0],[-5,2,-10,10,6,14,7]])
    m.bone('saddle',(0,-30,8.8),[[-4,-3,-4,8,5,8,1],[-4.3,-3.3,-4.3,8.6,1,8.6,2]])
    m.bone('head',(0,-32,-7),[[-5,-8,-5,10,12,10,0],[-4,-3,-14,8,7,10,2],[-3,4,-7,6,4,6,6]])
    for s in [-1,1]:m.bone('eye'+str(s),(s*4.9,-4,-3),[[-.4,-3,-1,1,6,5,5],[-.6,-2,-1.3,1.4,4,3,4],[-.8,-1,-.7,1.7,2,1,3]],'head')
    m.bone('crest',(0,-8,0),[[-2,-7,-1,4,7,8,6],[-1,-11,2,2,5,4,7],[-7,-1,-6,14,1,13,3]],'head',(-.15,0,0))
    for s,n in [(-1,'left'),(1,'right')]:
        m.bone('wing_'+n,(s*6,-25,0),[[min(0,s*17),0,-6,17,3,16,0],[min(0,s*22),2,-2,22,2,15,6],[min(0,s*26),3,4,26,2,11,5]])
        m.bone('leg_'+n,(s*4,-9,0),[[-1,0,-1,2,7,2,2],[-2,6,-5,4,2,7,2]])
    m.bone('tail',(0,-19,10),[[-5,-2,0,10,4,24,6],[-9,-1,5,18,2,24,0],[-2,-3,10,4,1,28,5]],rotation=(-.18,0,0))
    return m

def manang():
    m=Model();m.bone('upper',(0,-19,0),[[-5,-11,-3,10,12,6,1],[-3,-10,-3.2,6,9,1,0]])
    m.bone('head',(0,-12,0),[[-4,-8,-3.5,8,8,7,0],[-4.7,-9,2,9.4,17,3,3],[-4.5,-9,-3.5,9,2,8,3]],'upper')
    m.eyes('head',2,-4,-3.7,2.4)
    for s,n in [(-1,'left'),(1,'right')]:
        m.bone('horn'+n,(s*3,-8,0),[[-.7,-8,0,1.4,8,2,3]],'head',(.2,0,s*.25))
        m.bone('arm_'+n,(s*5,-9,0),[[-1.5,0,-1.5,3,12,3,0],[-2,10,-2,4,3,4,1]],'upper')
        m.bone('wing_'+n,(s*4,-8,3),[[min(0,s*24),0,0,24,2,3,3],[min(0,s*20),2,1,20,13,1,2],[min(0,s*15),15,1,15,7,1,6]],'upper',(0,s*.2,s*.3))
    m.bone('lower',(0,-18,0),[[-4,0,-3,8,7,6,1]])
    for s,n in [(-1,'left'),(1,'right')]:m.bone('leg_'+n,(s*2,6,0),[[-1.5,0,-2,3,12,4,1]],'lower')
    return m

def humanoid(name):
    m=Model()
    sizes={'datu':(18,26,12,19,14),'duwende':(7,6,5,3,6),'bungi':(20,20,14,9,15),'batiba':(18,12,12,4,16)}
    w,h,d,leg,head=sizes[name];neck=leg+h
    m.bone('body',(0,-leg-h/2,0),[[-w/2,-h/2,-d/2,w,h,d,0]])
    m.bone('head',(0,-neck,0),[[-head/2,-head,-head*.4,head,head,head*.8,0]])
    for s,n in [(-1,'left'),(1,'right')]:
        m.bone('arm_'+n,(s*(w/2+1),-neck+2,0),[[-2,0,-3,4,h*.85,6,0]])
        m.bone('leg_'+n,(s*w*.25,-leg,0),[[-w*.16,0,-d*.35,w*.32,leg,d*.7,0]])
    if name=='batiba':
        for side in [-1,1]:
            m.bone('eye_'+('left' if side<0 else 'right'),(side*4.2,-9,-6.6),[
                [-3.8,-3.8,-.1,7.6,7.6,.7,3],[-3.3,-3.3,-.3,6.6,6.6,.7,4],
                [-2.8,-2.8,-.6,5.6,5.6,.6,5],[-1.4,-2.1,-.8,2.8,4.2,.5,3],
                [-1.9,-2.5,-1,1.5,1.5,.3,4]],'head')
    else:
        m.eyes('head',head*.23,-head*.52,-head*.4-.2,max(2,head*.25))
    if name=='datu':
        m.bone('brow',(0,-head*.8,-head*.4),[[-head/2,0,-1,head,3,3,3]],'head')
        m.bone('beard',(0,-1,-head*.4),[[-head*.4,0,-1,head*.8,12,4,7]],'head')
        m.bone('crown',(0,-head,0),[[-10,-5,-6,20,5,12,1],[-6,-9,-4,12,5,8,2]],'head')
        m.bone('leaf_mantle',(0,-neck+3,1),[[-14,-2,-6,28,5,14,1],[-12,3,5,24,10,3,7]])
        m.bone('bark_ridges',(0,-leg-3,0),[[-10,-10,-6.5,3,12,1,6],[7,-13,-6.5,3,15,1,3]])
    elif name=='duwende':
        m.bone('cap',(0,-head+1,0),[[-7,-2,-6,14,3,12,1],[-5,-4,-4,10,3,8,1],[-3,-5,-2,6,2,4,1]],'head')
        m.bone('cap_spots',(0,-head-2,0),[[-5,-1,-3,2,1,2,2],[2,-1,-4,2,1,2,2],[-1,-3,-1,2,1,2,2]],'head')
        m.bone('crown',(0,-head-5,0),[[-5,-2,-3,10,2,6,6],[-4,-5,-3,2,3,2,2],[-1,-6,-3,2,4,2,2],[2,-5,-3,2,3,2,2]],'head')
        m.bone('sash',(0,-leg,0),[[-4,-2,-3,8,2,6,6]])
    elif name=='bungi':
        m.bone('third_eye',(0,-head*.87,-head*.4-.2),[[-2,-2,-.2,4,4,.5,4],[-.8,-1.2,-.4,1.6,2.4,.5,3]],'head')
        m.bone('mouth',(0,-head*.18,-head*.4-.2),[[-2,-1,0,4,2,.5,3],[-.5,-1.2,-.2,1,1,1,4]],'head')
        m.bone('tuft',(0,-head,1),[[-1,-3,0,2,4,2,6]],'head',(.2,0,.3))
        m.bone('shirt',(0,-neck+1,0),[[-w/2-.3,0,-d/2-.3,w+.6,8,d+.6,1]])
        m.bone('wrap',(0,-leg-1,0),[[-w/2-.2,-5,-d/2-.2,w+.4,7,d+.4,2]])
    elif name=='batiba':
        for s,n in [(-1,'left'),(1,'right')]:
            m.bone('ear_'+n,(s*head/2,-head*.65,0),[[-3,-4,-1,6,7,2,7]],'head',(0,0,s*.35))
        m.bone('smile',(0,-3,-head*.4-.3),[[-5,-1,0,10,3,.5,3],[-4,-1.2,-.2,8,1,1,4]],'head')
        m.bone('bun',(0,-head,2),[[-3,-4,-3,6,4,6,6],[-2,-6,-2,4,2,4,6]],'head')
        m.bone('dress',(0,-leg,0),[[-10,-10,-7,20,13,14,1],[-10.3,-3,-7.3,20.6,2,14.6,6]])
    return m

def mermahn():
    m=Model();m.bone('body',(0,-20,0),[[-7,-7,-4,14,12,8,0],[-5,5,-3,10,4,6,0]])
    m.bone('head',(0,-28,0),[[-4,-8,-4,8,8,8,0],[-4.2,-7,-4.2,8.4,2,8.4,2]])
    m.eyes('head',2,-3.6,-4.2,2,3)
    m.bone('smile',(0,-1,-4.2),[[-2.5,-.6,0,5,1.6,.5,3],[-2,-.6,-.2,4,.6,.5,4]],'head')
    m.bone('hair',(0,-8,1),[[-4.5,-2,-4,9,3,9,3],[-4,-1,4,8,10,3,3],[-3,8,5,6,10,4,3]],'head',(.15,0,0))
    for s,n in [(-1,'left'),(1,'right')]:
        m.bone('ear'+n,(s*4,-4,0),[[-1,-3,-1,2,5,2,7]],'head',(0,0,s*.4))
        m.bone('arm_'+n,(s*8,-25,0),[[-2,0,-2.5,4,13,5,0]])
        m.bone('star'+n,(s*3,-21,-4.8),[[-1,-1,0,2,2,1,2],[-.4,-2,0,.8,4,1,2],[-2,-.4,0,4,.8,1,2]],rotation=(0,0,s*.2))
    m.bone('net',(0,-20,-4.5),[[-6,-5,0,12,.6,.5,6],[-6,-1,0,12,.6,.5,6],[-3,-7,0,.6,11,.5,6],[2.5,-7,0,.6,11,.5,6]])
    m.bone('tail',(0,-10,0),[[-4,0,-3,8,7,7,1],[-3,5,1,6,5,8,1],[-8,7,7,16,2,8,7]],rotation=(0,0,0))
    return m

def zippy():
    m=Model()
    m.bone('body',(0,-6,0),[[-2,-3,-1.5,4,5,3,0],[-2.2,-1,-1.7,4.4,3,3.4,1]])
    m.bone('head',(0,-9,0),[[-3,-4,-2.5,6,5,5,0]])
    m.eyes('head',1.5,-2,-2.7,2,3)
    m.bone('smile',(0,-.1,-2.7),[[-1.8,-.3,0,3.6,.9,.4,3],[-1.5,-.3,-.1,3,.35,.4,4]],'head')
    m.bone('crest',(0,-4,1),[[-2.5,-2,-2,5,2,4,1],[-2,-5,0,1.5,4,2,2],[0,-6,.5,1.5,5,2,1],[1.7,-4,1,1,3,2,1]],'head',(-.25,0,.12))
    for s,n in [(-1,'left'),(1,'right')]:
        m.bone('ear_'+n,(s*3,-2,0),[[-.5,-2,-.4,1,3,1,0]],'head',(0,0,s*.65))
        m.bone('arm_'+n,(s*2.2,-8,0),[[-.6,0,-.6,1.2,4,1.2,0]])
        m.bone('leg_'+n,(s*.9,-4,0),[[-.6,0,-.6,1.2,3.5,1.2,0],[-.7,3,-1,1.4,.7,2,7]])
        m.bone('wing_'+n,(s*1.5,-9,1.8),[[min(0,s*6),-4,0,6,5,.5,6],[min(0,s*4),1,0,4,3,.5,6]],rotation=(0,s*.2,s*.35))
    m.bone('bottle',(-3.4,-4,-2),[[-1,-1,-.8,2,2,1.6,5],[-.5,-2,-.5,1,1,1,2]])
    return m

def tikbalang():
    m=Model()
    m.bone('body',(0,-25,0),[[-5,-8,-3.5,10,15,7,0],[-5.3,3,-3.8,10.6,6,7.6,1],[-5.5,3.5,-4,11,1,8,2]])
    m.bone('head',(0,-33,-1),[[-3.5,-8,-3.5,7,9,7,0],[-3,-4,-9,6,5,7,6],[-2.7,-.5,-9.2,5.4,1,1,2]])
    m.eyes('head',2.3,-4.5,-3.7,2.3,3)
    m.bone('nostrils',(0,-2,-9.2),[[-2.3,-.3,0,1,.6,.3,3],[1.3,-.3,0,1,.6,.3,3]],'head')
    m.bone('mane',(0,-8,2),[[-2,-2,-3,4,2,7,3],[-2,0,2,4,12,2,3]],'head')
    for s,n in [(-1,'left'),(1,'right')]:
        m.bone('ear_'+n,(s*2.5,-7,0),[[-1,-6,-1,2,6,2,0],[-.6,-5,-1.1,1.2,4,.4,6]],'head',(0,0,s*.15))
        m.bone('arm_'+n,(s*6,-32,0),[[-1.5,0,-2,3,15,4,0],[-1.8,13,-2.3,3.6,3,4.6,6]])
        m.bone('leg_'+n,(s*2.6,-17,0),[[-1.7,0,-2,3.4,15,4,0],[-2,14,-3,4,3,5,3]])
    m.bone('tail',(0,-19,3),[[-.8,0,0,1.6,10,1.6,0],[-1.5,8,-.5,3,6,3,3]],rotation=(.3,0,0))
    return m

def sarangay():
    # Broad bovine silhouette / purple palette from Ryan Carlson's public production painting.
    # Hooves, simple wrap and jewel brooch are original adaptations of obscured details.
    m=Model()
    m.bone('body',(0,-27,2),[[-13,-12,-7,26,22,15,0],[-10,-15,0,20,7,10,7],[-9,7,-6,18,6,13,7],[-9.2,7,-6.2,18.4,1.5,13.4,2]])
    m.bone('head',(0,-38,-5),[[-7,-9,-5,14,13,12,0],[-6,-3,-13,12,7,10,1],[-5,3,-13.2,10,1.2,1,3],[-4.5,3,-13.5,9,.7,.7,4]])
    m.eyes('head',4,-4.8,-5.3,3,5)
    m.bone('brows',(0,-6,-5.4),[[-6,-1,-.3,5,1.6,1,7],[1,-1,-.3,5,1.6,1,7]],'head')
    m.bone('nostrils',(0,0,-13.3),[[-4,-.5,0,2,1.4,.5,3],[2,-.5,0,2,1.4,.5,3]],'head')
    m.bone('tuft',(0,-9,1),[[-5,-3,-3,10,4,8,7],[-3,-5,-1,6,3,5,7]],'head',(.1,0,-.1))
    for s,n in [(-1,'left'),(1,'right')]:
        m.bone('horn_'+n,(s*6,-7,1),[[min(0,s*8),-2,-2,8,4,5,3],[s*7-1.5,-8,-1.5,3,9,3,3],[s*8-1,-12,-1,2,5,2,2]],'head',(0,0,-s*.18))
        m.bone('ear_'+n,(s*7,-3,2),[[-3,-1,-2,6,2,4,1]],'head',(0,0,s*.3))
        m.bone('arm_'+n,(s*14,-35,1),[[-4,-2,-4,8,17,9,0],[-4.5,12,-4.5,9,8,10,1]])
        m.bone('leg_'+n,(s*6,-15,1),[[-4,0,-4,8,13,8,0],[-4.5,10,-6,9,5,11,3],[-.35,12,-6.2,.7,3,.6,2]])
    m.bone('jewel',(-6,-31,-5.8),[[-2.5,-2.5,-.5,5,5,1,2],[-1.7,-1.7,-1,3.4,3.4,1,6]],rotation=(0,0,math.pi/4))
    m.bone('tail',(0,-18,8),[[-1,0,0,2,13,2,0],[-2,11,-1,4,5,4,7]],rotation=(.35,0,0))
    return m

def flamingchef():
    # Entirely original gameplay interpretation, explicitly authorized by the user.
    m=Model()
    m.bone('body',(0,-17,0),[[-6,-7,-4,12,14,8,1],[-6.3,-3,-4.3,12.6,8,8.6,7],[-5,-5,-4.5,10,10,.8,7],[-2,1,-5,4,3,.7,2]])
    m.bone('buttons',(0,-22,-4.4),[[-2,-1,0,1,1,.6,2],[1,-1,0,1,1,.6,2],[-2,1,0,1,1,.6,2],[1,1,0,1,1,.6,2]])
    m.bone('head',(0,-25,0),[[-5,-7,-4,10,8,8,0],[-4,-1,-5,8,3,6,0]])
    m.eyes('head',2.5,-3.7,-4.3,2.8,5)
    m.bone('brows',(0,-5.7,-4.4),[[-4,-.5,0,3,1,1,3],[1,-.5,0,3,1,1,3]],'head')
    m.bone('smile',(0,-.7,-5.2),[[-3,-.4,0,6,1.5,.5,3],[-2.6,-.4,-.1,1,1.4,.6,4],[1.6,-.4,-.1,1,1.4,.6,4]],'head')
    m.bone('nose',(0,-2.4,-4.3),[[-1,-.7,-1.5,2,2,2,2]],'head')
    m.bone('collar',(0,-24,-3.8),[[-5,-1,0,4,2,1,1],[1,-1,0,4,2,1,1],[-1,-.5,-.3,2,3,1,7]])
    for side,n in [(-1,'left'),(1,'right')]:
        m.bone('ear_'+n,(side*5,-4,0),[[-2,-2,-.7,4,4,1.5,0],[-1,-2.8,-.5,2,2,1,2]],'head',(0,0,side*.5))
        m.bone('arm_'+n,(side*7,-23,0),[[-2,0,-2.5,4,6,5,1],[-2.2,5,-2.7,4.4,2,5.4,7],[-1.8,7,-2.2,3.6,4,4.4,0]])
        m.bone('leg_'+n,(side*3,-10,0),[[-2,0,-2,4,8,4,3],[-2.3,7,-4,4.6,3,6,3]])
    # Three separate flame tongues: orange outer shells with golden raised cores.
    for i,(x,h,tilt) in enumerate([(-3,7,-.2),(0,11,.08),(3,8,.22)]):
        m.bone('flame'+str(i),(x,-7,0),[[-1.9,-h,-2,3.8,h+1,4,6],[-1.2,-h*.7,-2.3,2.4,h*.7+1,.6,5],[-.8,-h-2,-1,1.6,3,2,5]],'head',(0,0,tilt))
    # Pan pivot is at the hand; counter-rotation keeps its contents upright while walking.
    m.bone('pan',(0,9,-1),[[-.7,-.5,-7,1.4,1,7,3],[-4.5,0,-15,9,1.2,9,2],[-4.5,-1.5,-15,9,1.5,1,2],[-4.5,-1.5,-7,9,1.5,1,2],[-4.5,-1.5,-14,1,1.5,7,2],[3.5,-1.5,-14,1,1.5,7,2],[-3.4,-.3,-13.9,6.8,.4,6.8,5]],'arm_left')
    m.bone('spatula',(0,9,-2),[[-.5,-.5,-7,1,1,8,2],[-2,-.7,-12,4,.7,5,3]],'arm_right')
    return m

models={'bakunawa':bakunawa(),'raww':raww(),'sarimanok':sarimanok(),'manang':manang(),'mermahn':mermahn(),'zippy':zippy(),'tikbalang':tikbalang(),'sarangay':sarangay(),'flamingchef':flamingchef()}
for name in ['datu','duwende','bungi','batiba']:models[name]=humanoid(name)
for name,model in models.items():model.export(name)
(SRC/'palettes.json').write_text(json.dumps(PALETTES,indent=2)+'\n')

for name,palette in PALETTES.items():
    rng=random.Random(name)
    im=Image.new('RGBA',(1024,128));px=im.load()
    for mat,color in enumerate(palette):
        rgb=tuple(int(color[i:i+2],16) for i in (0,2,4))
        for y in range(128):
            for x in range(128):
                jitter=rng.choice([-7,-3,0,0,0,3,6]) if mat not in (4,5) else 0
                pattern=0
                if mat==0 and name=='bakunawa': pattern=10 if (y%6==0 and (x+y//6*3)%8<5) else 0
                if mat==0 and name=='datu': pattern=-13 if (x+y//12)%7==0 else 0
                if mat==1 and name=='batiba':pattern=16 if ((x//4+y//4)%4==0) else 0
                if mat==1 and name=='duwende':pattern=18 if (x%11-5)**2+(y%11-5)**2<5 else 0
                if name=='flamingchef' and mat==7:pattern=12 if (x//3+y//3)%2==0 else 0
                if name=='flamingchef' and mat==6:pattern=15 if (x+y*2)%7<2 else 0
                if mat==2:pattern=7 if y%4==0 else 0
                px[mat*128+x,y]=tuple(max(0,min(255,c+jitter+pattern)) for c in rgb)+(255,)
    im.save(RES/'textures/entity'/f'{name}.png')

for name in ['creature_guide','friendship_whistle','showcase_wand']:
    im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
    if name=='creature_guide':
        d.rectangle((5,3,26,29),fill='#263e50');d.rectangle((7,3,25,26),fill='#539b8e');d.rectangle((7,26,24,28),fill='#f5ddb0');d.rectangle((5,3,8,29),fill='#ab7f51')
        d.ellipse((12,8,23,19),fill='#efd18a');d.ellipse((16,7,25,17),fill='#539b8e');d.rectangle((12,22,22,23),fill='#c4e8d7')
    elif name=='friendship_whistle':
        d.line((5,7,13,24),fill='#bb8250',width=2);d.ellipse((10,9,26,23),fill='#b98c55');d.ellipse((12,10,24,20),fill='#efc97a');d.rectangle((5,14,15,21),fill='#e1b365');d.rectangle((18,12,21,15),fill='#28576a')
    else:
        d.line((6,27,23,9),fill='#614a78',width=5);d.line((7,26,24,9),fill='#cba45f',width=2);d.polygon([(23,2),(26,8),(31,10),(25,13),(22,19),(20,13),(14,10),(20,8)],fill='#78e4d2');d.rectangle((22,8,24,11),fill='#fff0bb')
    im.save(RES/'textures/item'/f'{name}.png')
    (RES/'models/item'/f'{name}.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':f'forgottenisland:item/{name}'}})+'\n')

names={'bakunawa':'Bakunawa','raww':'Raww','manang':'Manang','sarimanok':'Sarimanok','datu':'Datu','duwende':'Duwende','bungi':'Bungi','batiba':'Batiba','mermahn':'Prince Mermahn','zippy':'Zippy the Potion Fairy','tikbalang':'Tikbalang','sarangay':'Sarangay','flamingchef':'Flaming Aswang Chef'}
lang={'tab.forgottenisland':'Forgotten Island','item.forgottenisland.creature_guide':'Wild Companion Field Guide','item.forgottenisland.friendship_whistle':'Friendship Whistle','item.forgottenisland.showcase_wand':'Creative Showcase Wand','key.forgottenisland.ability':'Companion ability','key.forgottenisland.roar':'Bakunawa roar','key.forgottenisland.descend':'Mount descend','key.categories.forgottenisland':'Forgotten Island'}
sounds={}
for i,(name,label) in enumerate(names.items()):
    lang[f'entity.forgottenisland.{name}']=label
    lang[f'item.forgottenisland.{name}_spawn_egg']=label+' Spawn Egg'
    lang[f'subtitles.forgottenisland.{name}']=label+' calls'
    (RES/'models/item'/f'{name}_spawn_egg.json').write_text('{"parent":"minecraft:item/template_spawn_egg"}\n')
    sounds[name+'_call']={'subtitle':f'subtitles.forgottenisland.{name}','sounds':[{'name':f'forgottenisland:{name}_call','attenuation_distance':32 if name=='bakunawa' else 16}]}
    rng=random.Random(name);rate=22050;duration=2.8 if name=='bakunawa' else 1.1
    samples=[]
    for n in range(int(rate*duration)):
        t=n/rate;q=t/duration;envelope=math.sin(math.pi*q)**1.4
        if name=='bakunawa':
            base=55+38*(1-q);sample=.4*math.sin(2*math.pi*(80*t-12*t*t))+.22*math.sin(2*math.pi*base*2.01*t)+rng.uniform(-.13,.13)
        elif name=='flamingchef':
            # A little pan chime over a soft synthesized cooking sizzle; no sampled audio.
            sample=.12*rng.uniform(-1,1)+.25*math.sin(2*math.pi*660*t)*math.exp(-4*t)+.14*math.sin(2*math.pi*990*t)*math.exp(-6*t)
        else:
            base=[0,210,310,640,100,880,270,740,390,1040,165,85][i]
            frequency=base*(1+.05*math.sin(t*24))
            sample=.38*math.sin(2*math.pi*frequency*t)+.16*math.sin(2*math.pi*frequency*1.5*t)
            sample*=.6+.4*math.sin(t*(20 if name in ('raww','bungi') else 7))
        samples.append(.76*envelope*sample)
    sf.write(RES/'sounds'/f'{name}_call.ogg',samples,rate,format='OGG',subtype='VORBIS')
(RES/'lang/en_us.json').write_text(json.dumps(lang,indent=2)+'\n')
(RES/'sounds.json').write_text(json.dumps(sounds,indent=2)+'\n')
im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
d.polygon([(7,0),(9,5),(15,7),(10,10),(8,15),(5,10),(0,8),(5,5)],fill='#b2ffe4')
d.rectangle((6,6,9,9),fill='#fff3b4');im.save(RES/'textures/particle/memory_mote.png')
(RES/'particles/memory_mote.json').write_text('{"textures":["forgottenisland:memory_mote"]}\n')
print(f'Generated {len(models)} original models, pixel atlases, icons and synthesized calls.')
