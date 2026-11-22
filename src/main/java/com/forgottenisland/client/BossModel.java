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
        root.getAllParts().forEach(part->{part.resetPose();part.xScale=part.yScale=part.zScale=1;});
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
        } else animateBakunawa(boss,age);
    }
    private void at(ModelPart part,net.minecraft.world.phys.Vec3 offset){part.x=(float)(offset.x*16/9);part.y=(float)(-offset.y*16/9);part.z=(float)(-offset.z*16/9);}
    private void animateBakunawa(IslandBoss boss,float age){
        float partial=Mth.clamp(age-boss.tickCount,0,1),tick=boss.localClock()+partial,scene=boss.clock()+partial;
        var head=p("head");at(head,boss.headPoint(partial).subtract(boss.position()));
        boolean defeat=boss.stage()==EncounterState.Stage.DEFEAT;
        double split=defeat?BakunawaMoves.smooth((scene-75)/45.0):0;
        head.x+=(float)(split*22);head.zRot+=(float)(split*.65);head.y-=(float)(split*9);
        head.visible=!defeat||scene<168;
        float jaw=.12f+Mth.sin(age*.06f)*.04f;
        if(boss.stage()==EncounterState.Stage.ENTRANCE){
            float roar=(float)(BakunawaMoves.smooth((scene-115)/12.0)*(1-BakunawaMoves.smooth((scene-165)/20.0)));
            jaw+=roar*1.05f;head.xRot-=roar*.24f;
        }else if(defeat){jaw=.55f;head.xRot-=scene*.002f;}
        else if(boss.stage()==EncounterState.Stage.COMBAT){
            switch(boss.attack()){
                case 0 -> {jaw=tick<60?.15f+(float)BakunawaMoves.smooth(tick/40)*1.05f:tick<67?.04f:.12f;head.xRot+=(float)(tick>=67&&tick<95?Math.sin((tick-67)/28*Math.PI)*.3:0);}
                case 2,7 -> {jaw=tick<60?.2f+tick*.012f:tick<(boss.attack()==7?120:72)?.95f:.2f;head.xRot-=tick>=60&&tick<120?.07f*Math.abs(Mth.sin(age*.8f)):0;}
                case 3,6 -> {jaw=tick<28?.2f+tick*.02f:tick<86?.75f+Mth.sin(tick*.4f)*.12f:.15f;head.xRot+=tick<35?-.2f:.08f;}
                case 8 -> {jaw=.25f;head.xRot=-.2f;}
                case 10 -> {jaw=.5f;head.zRot=Mth.sin((tick-52)*.14f)*(float)BakunawaMoves.lunge(tick)*.6f;}
                default -> {}
            }
        }
        p("head/jaw").xRot=jaw;
        for(int i=0;i<9;i++){
            var part=p("segment"+i);var point=boss.bodyPoint(i,partial);var delta=boss.bodyPoint(i+1,partial).subtract(point);
            at(part,point.subtract(boss.position()));
            part.yRot=(float)Math.atan2(-delta.x,-delta.z);part.xRot=(float)Math.atan2(delta.y,delta.horizontalDistance());
            part.zScale=(float)Math.max(.5,delta.length()/7.875+.12);
            if(defeat){part.x+=(i%2==0?1:-1)*split*(12+i*3);part.zRot+=(i%2==0?1:-1)*split*.5;part.y-=(float)(split*8);}
            part.visible=!defeat||scene<168;
        }
        var neck=boss.bodyPoint(1,partial).subtract(boss.position());
        for(String name:java.util.List.of("fin_left","fin_right")){
            int side=name.endsWith("left")?-1:1;var fin=p(name);at(fin,neck.add(side*4,0,0));
            fin.zRot=side*(.25f+Mth.sin(age*.08f)*.12f+(boss.warded()?.7f:0));
            if(boss.stage()==EncounterState.Stage.COMBAT&&boss.attack()==1)fin.zRot+=side*(float)Math.sin(Math.min(1,tick/90)*Math.PI)*.8f;
            fin.visible=!defeat||scene<168;
        }
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer v,int light,int overlay,float r,float g,float b,float alpha){root.render(pose,v,light,overlay,r,g,b,alpha);}
}
