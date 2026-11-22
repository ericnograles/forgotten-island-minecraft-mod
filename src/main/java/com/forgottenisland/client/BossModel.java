package com.forgottenisland.client;

import com.forgottenisland.adventure.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.util.Mth;

/** Separate animations on the existing authored skeleton; companion animations remain untouched. */
public final class BossModel extends EntityModel<IslandBoss> {
    public static ModelLayerLocation layer(BossKind kind){return new ModelLayerLocation(com.forgottenisland.ForgottenIsland.id(kind.id),"main");}
    private final ModelPart root;
    public BossModel(ModelPart root){this.root=root;}
    private ModelPart p(String path){ModelPart part=root.getChild("body_root");for(String s:path.split("/"))part=part.getChild(s);return part;}
    @Override public void setupAnim(IslandBoss boss,float swing,float amount,float age,float yaw,float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        var base=root.getChild("body_root");float t=boss.clock();
        if(boss.kind==BossKind.MANANG) {
            float wing=Mth.sin(age*.22f)*.30f+(boss.exposed()?-.65f:.35f);
            if(boss.stage()==EncounterState.Stage.ENTRANCE)wing=-1.2f+Mth.clamp(t/45,0,1)*1.5f+Mth.sin(age*.2f)*.15f;
            if(boss.stage()==EncounterState.Stage.DEFEAT){wing=.35f-t*.013f;p("upper").xRot=t*.006f;p("lower").xRot=-t*.004f;}
            p("upper").y-=boss.separation()*10;
            var offset=boss.upperOffset();float rotation=boss.getYRot()*Mth.DEG_TO_RAD;
            p("upper").x+=(Mth.cos(rotation)*offset.x+Mth.sin(rotation)*offset.z)*10;
            p("upper").z+=(Mth.sin(rotation)*offset.x-Mth.cos(rotation)*offset.z)*10;
            p("upper/wing_left").zRot+=wing;p("upper/wing_right").zRot-=wing;
            p("upper/head").xRot-=boss.exposed()?.1f:.25f;
            p("upper/arm_left").xRot-=boss.exposed()?.2f:1.1f;p("upper/arm_right").xRot-=boss.exposed()?.2f:1.1f;
            base.y+=Mth.sin(age*.07f)*.2f;
        } else {
            String key="segment0";
            for(int i=0;i<9;i++) {
                p(key).yRot+=Mth.sin(boss.clock()*.025f-i*.45f)*.12f;
                p(key).xRot+=Mth.sin(boss.clock()*.03f-i*.4f)*.012f;
                if(i<8)key+="/segment"+(i+1);
            }
            p("head/jaw").xRot+=boss.phase()==2?.35f:.12f+Mth.sin(age*.06f)*.07f;
            p("fin_left").zRot+=Mth.sin(age*.05f)*.14f;p("fin_right").zRot-=Mth.sin(age*.05f)*.14f;
            if(boss.stage()==EncounterState.Stage.ENTRANCE) {
                float rise=1-Mth.clamp(t/90,0,1);base.y+=rise*42;p("head").xRot-=Mth.sin(t/160*Mth.PI)*.45f;p("head/jaw").xRot+=Mth.sin(t/160*Mth.PI)*.65f;
            }
            if(boss.exposed())p("head").y+=Mth.sin((boss.localClock()-80)/(float)(boss.kind.cycle(boss.phase())-80)*Mth.PI)*6;
            if(boss.stage()==EncounterState.Stage.DEFEAT){base.y-=t*.10f;p("head").xRot+=t*.004f;p("head/jaw").xRot+=.4f;}
        }
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer v,int light,int overlay,float r,float g,float b,float alpha){root.render(pose,v,light,overlay,r,g,b,alpha);}
}
