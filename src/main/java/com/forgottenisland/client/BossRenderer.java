package com.forgottenisland.client;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.adventure.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;

public final class BossRenderer extends MobRenderer<IslandBoss,BossModel> {
    public BossRenderer(EntityRendererProvider.Context context,BossKind kind){
        super(context,new BossModel(context.bakeLayer(BossModel.layer(kind))),kind==BossKind.MANANG?1.4f:7);
        if(kind==BossKind.BAKUNAWA)addLayer(new net.minecraft.client.renderer.entity.layers.RenderLayer<IslandBoss,BossModel>(this){
            @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,IslandBoss boss,float swing,float amount,float partial,float age,float yaw,float pitch){
                getParentModel().renderToBuffer(pose,buffers.getBuffer(RenderType.eyes(ForgottenIsland.id("textures/entity/bakunawa_boss_glow.png"))),15728880,OverlayTexture.NO_OVERLAY,1,1,1,1);
            }
        });
    }
    @Override public ResourceLocation getTextureLocation(IslandBoss boss){return ForgottenIsland.id("textures/entity/"+(boss.kind==BossKind.BAKUNAWA?"bakunawa_boss":boss.kind.species.id)+".png");}
    @Override protected float getWhiteOverlayProgress(IslandBoss boss,float partial){return boss.kind==BossKind.BAKUNAWA&&boss.stage()==EncounterState.Stage.DEFEAT&&boss.clock()>=60&&boss.clock()<86?(float)Math.sin((boss.clock()+partial-60)/26*Math.PI):0;}
    @Override protected void scale(IslandBoss boss,PoseStack pose,float partial){float s=boss.kind==BossKind.MANANG?1.6f:9;pose.scale(s,s,s);}
    @Override protected boolean shouldShowName(IslandBoss boss){return false;}
    @Override public boolean shouldRender(IslandBoss boss,Frustum frustum,double x,double y,double z){return frustum.isVisible(new AABB(boss.kind.center).inflate(boss.kind.radius+80,100,boss.kind.radius+80));}
    @Override public void render(IslandBoss boss,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        super.render(boss,yaw,partial,pose,buffers,light);
        if(boss.kind!=BossKind.BAKUNAWA||boss.stage()==EncounterState.Stage.COMPLETE)return;
        moon(boss,partial,pose,buffers);
        cinematicWaves(boss,partial,pose,buffers);
        attacks(boss,partial,pose,buffers,light);
        for(int i=0;i<3;i++) {
            var p=boss.weakPoint(i).subtract(boss.position());pose.pushPose();pose.translate(p.x-1,p.y,p.z-1);pose.scale(2,1,2);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);
            pose.popPose();pose.pushPose();pose.translate(p.x,p.y+1.7+Math.sin((boss.tickCount+partial)*.08+i)*.15,p.z);
            pose.mulPose(Axis.YP.rotationDegrees((boss.tickCount+partial)*1.5f));pose.mulPose(Axis.ZP.rotationDegrees(45));pose.scale(1.2f,1.2f,1.2f);pose.translate(-.5,-.5,-.5);
            var block=boss.exposed()?AdventureContent.PAROL.get():Blocks.AMETHYST_BLOCK;
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(),pose,buffers,15728880,OverlayTexture.NO_OVERLAY);pose.popPose();
        }
    }
    private void attacks(IslandBoss boss,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        if(boss.stage()!=EncounterState.Stage.COMBAT)return;
        int t=boss.localClock();
        if(boss.attack()==1){
            double height=BakunawaMoves.tentacleHeight(t);
            if(height<=0)return;
            for(int i=0;i<5;i++){
                var base=boss.tentaclePoint(i).subtract(boss.position());
                for(int n=0;n<7;n++){
                    double bend=Math.sin((t-40)*.045+n*.26+i)*.45*n;
                    double slam=t<52?0:t<=60?BakunawaMoves.smooth((t-52)/8.0):1-BakunawaMoves.smooth((t-60)/12.0);
                    float size=1.9f-n*.19f;
                    pose.pushPose();pose.translate(base.x+bend*height,base.y-1+n*1.15*height*(1-slam*.98),base.z+slam*n*.28);
                    pose.mulPose(Axis.ZP.rotationDegrees((float)(-bend*8*height)));pose.scale(size,(float)(1.8*height),size);pose.translate(-.5,0,-.5);
                    Minecraft.getInstance().getBlockRenderer().renderSingleBlock((n==6?Blocks.SHROOMLIGHT:Blocks.CRIMSON_HYPHAE).defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();
                }
            }
        }else if((boss.attack()==2||boss.attack()==7)&&t<(boss.attack()==7?120:80)){
            boolean atomic=boss.attack()==7;
            var from=boss.mouth().subtract(boss.position());
            if(t<60){
                float size=.2f+t*.018f;pose.pushPose();pose.translate(from.x,from.y,from.z);pose.mulPose(Axis.YP.rotationDegrees((t+partial)*8));pose.mulPose(Axis.ZP.rotationDegrees(45));pose.scale(size,size,size);pose.translate(-.5,-.5,-.5);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.SEA_LANTERN.defaultBlockState(),pose,buffers,15728880,OverlayTexture.NO_OVERLAY);pose.popPose();
            }else if(t<(atomic?120:72)){
                var to=boss.beamEnd().subtract(boss.position());var direction=to.subtract(from).normalize();
                var side=direction.cross(new net.minecraft.world.phys.Vec3(0,1,0)).normalize().scale(.55);
                var other=direction.cross(side).normalize().scale(.55);var v=buffers.getBuffer(RenderType.lightning());
                for(var normal:java.util.List.of(side,other)){
                    for(var p:java.util.List.of(from.add(normal),to.add(normal),to.subtract(normal),from.subtract(normal)))v.vertex(pose.last().pose(),(float)p.x,(float)p.y,(float)p.z).color(atomic?.2f:1f,.95f,atomic?1f:.65f,.88f).endVertex();
                    for(var p:java.util.List.of(from.subtract(normal),to.subtract(normal),to.add(normal),from.add(normal)))v.vertex(pose.last().pose(),(float)p.x,(float)p.y,(float)p.z).color(atomic?.2f:1f,.95f,atomic?1f:.65f,.88f).endVertex();
                }
            }
        }else if(boss.attack()==3||boss.attack()==6){
            boolean rock=boss.attack()==3;
            for(int i=0;i<(rock?1:3);i++){
                if(t<(rock?28:38+i*8)||t>=(rock?60:68+i*8))continue;
                var point=boss.projectilePoint(i,t+partial,rock).subtract(boss.position());
                pose.pushPose();pose.translate(point.x,point.y,point.z);pose.mulPose(Axis.YP.rotationDegrees((t+partial)*9));pose.mulPose(Axis.ZP.rotationDegrees((t+partial)*4));float s=rock?3:1.8f;pose.scale(s,s,s);pose.translate(-.5,-.5,-.5);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock((rock?Blocks.END_STONE:Blocks.MAGMA_BLOCK).defaultBlockState(),pose,buffers,rock?light:15728880,OverlayTexture.NO_OVERLAY);pose.popPose();
            }
        }else if(boss.warded()){
            var c=boss.headPoint(partial).subtract(boss.position());
            for(int axis=0;axis<3;axis++)ring(pose,buffers,c,13,.16f,1,.76f,.2f,.6f,axis);
        }
        // Electricity follows the actual animated tail rather than an unrelated fixed particle cloud.
        if(boss.attack()==9&&t>=28&&t<85){var p=boss.bodyPoint(9,partial).subtract(boss.position());ring(pose,buffers,p,2,.15f,1,.9f,.3f,.8f,0);}
    }
    private void cinematicWaves(IslandBoss boss,float partial,PoseStack pose,MultiBufferSource buffers){
        float t=boss.clock()+partial;
        if(boss.stage()==EncounterState.Stage.ENTRANCE&&t>=24&&t<125){
            for(int i=0;i<3;i++){float radius=((t-24+i*12)%48)*.7f;ring(pose,buffers,boss.kind.origin().subtract(boss.position()).add(0,-.8,-8),radius,.22f,.65f,.9f,1,.45f*(1-radius/34),0);}
        }else if(boss.stage()==EncounterState.Stage.DEFEAT&&t>=130&&t<168){
            ring(pose,buffers,boss.headPoint(partial).subtract(boss.position()),(t-130)*2,.6f,1,.8f,.4f,1-(t-130)/38,0);
        }
    }
    private static void ring(PoseStack pose,MultiBufferSource buffers,net.minecraft.world.phys.Vec3 center,float radius,float width,float r,float g,float b,float alpha,int axis){
        var v=buffers.getBuffer(RenderType.lightning());
        for(int n=0;n<64;n++){
            double a=n*Math.PI/32,next=(n+1)*Math.PI/32;
            for(double[] corner:new double[][]{{a,radius-width},{next,radius-width},{next,radius+width},{a,radius+width}}){
                double x=Math.cos(corner[0])*corner[1],z=Math.sin(corner[0])*corner[1];
                var p=center.add(axis==1?0:x,axis==0?0:axis==1?x:z,axis==2?0:z);
                v.vertex(pose.last().pose(),(float)p.x,(float)p.y,(float)p.z).color(r,g,b,Math.max(0,alpha)).endVertex();
            }
        }
    }
    private void moon(IslandBoss boss,float partial,PoseStack pose,MultiBufferSource buffers) {
        boolean entrance=boss.stage()==EncounterState.Stage.ENTRANCE,defeat=boss.stage()==EncounterState.Stage.DEFEAT;
        if(!defeat||boss.clock()>=130)return;
        float t=(boss.clock()+partial)/(entrance?boss.kind.entranceLength:boss.kind.defeatLength);
        float swallow=Mth.clamp((t-.15f)/.65f,0,1),radius=entrance?5*(1-swallow):Mth.clamp(t*6,0,4);
        if(radius<.05f)return;
        var point=boss.mouth().subtract(boss.position());pose.pushPose();pose.translate(point.x,point.y+3,point.z+2);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());pose.scale(radius,radius,radius);
        var part=pose.last();var v=buffers.getBuffer(RenderType.entityTranslucent(ForgottenIsland.id("textures/entity/encounter_moon.png")));
        float alpha=defeat?Mth.clamp((1-t)*4,0,1):1;
        float[][] corners={{-1,-1,0,1},{1,-1,1,1},{1,1,1,0},{-1,1,0,0}};
        for(float[] c:corners)v.vertex(part.pose(),c[0],c[1],0).color(1f,1f,1f,alpha).uv(c[2],c[3]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(part.normal(),0,0,1).endVertex();
        pose.popPose();
    }
}
