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
    public BossRenderer(EntityRendererProvider.Context context,BossKind kind){super(context,new BossModel(context.bakeLayer(BossModel.layer(kind))),kind==BossKind.MANANG?1.4f:7);}
    @Override public ResourceLocation getTextureLocation(IslandBoss boss){return ForgottenIsland.id("textures/entity/"+boss.kind.species.id+".png");}
    @Override protected void scale(IslandBoss boss,PoseStack pose,float partial){float s=boss.kind==BossKind.MANANG?1.6f:9;pose.scale(s,s,s);}
    @Override protected boolean shouldShowName(IslandBoss boss){return false;}
    @Override public boolean shouldRender(IslandBoss boss,Frustum frustum,double x,double y,double z){return frustum.isVisible(new AABB(boss.kind.center).inflate(boss.kind.radius+12,50,boss.kind.radius+12));}
    @Override public void render(IslandBoss boss,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        super.render(boss,yaw,partial,pose,buffers,light);
        if(boss.kind!=BossKind.BAKUNAWA||boss.stage()==EncounterState.Stage.COMPLETE)return;
        moon(boss,partial,pose,buffers);
        for(int i=0;i<3;i++) {
            var p=boss.weakPoint(i).subtract(boss.position());pose.pushPose();pose.translate(p.x-1,p.y,p.z-1);pose.scale(2,1,2);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);
            pose.popPose();pose.pushPose();pose.translate(p.x,p.y+1.7+Math.sin((boss.tickCount+partial)*.08+i)*.15,p.z);
            pose.mulPose(Axis.YP.rotationDegrees((boss.tickCount+partial)*1.5f));pose.mulPose(Axis.ZP.rotationDegrees(45));pose.scale(1.2f,1.2f,1.2f);pose.translate(-.5,-.5,-.5);
            var block=boss.exposed()?AdventureContent.PAROL.get():Blocks.AMETHYST_BLOCK;
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(),pose,buffers,15728880,OverlayTexture.NO_OVERLAY);pose.popPose();
        }
    }
    private void moon(IslandBoss boss,float partial,PoseStack pose,MultiBufferSource buffers) {
        boolean entrance=boss.stage()==EncounterState.Stage.ENTRANCE,defeat=boss.stage()==EncounterState.Stage.DEFEAT;
        if(!entrance&&!defeat)return;
        float t=(boss.clock()+partial)/(entrance?boss.kind.entranceLength:boss.kind.defeatLength);
        float swallow=Mth.clamp((t-.15f)/.65f,0,1),radius=entrance?5*(1-swallow):Mth.clamp(t*6,0,4);
        if(radius<.05f)return;
        pose.pushPose();pose.translate(0,entrance?20-8*swallow:20+t*15,entrance?31-10*swallow:23);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());pose.scale(radius,radius,radius);
        var part=pose.last();var v=buffers.getBuffer(RenderType.entityTranslucent(ForgottenIsland.id("textures/entity/encounter_moon.png")));
        float alpha=defeat?Mth.clamp((1-t)*4,0,1):1;
        float[][] corners={{-1,-1,0,1},{1,-1,1,1},{1,1,1,0},{-1,1,0,0}};
        for(float[] c:corners)v.vertex(part.pose(),c[0],c[1],0).color(1f,1f,1f,alpha).uv(c[2],c[3]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(part.normal(),0,0,1).endVertex();
        pose.popPose();
    }
}
