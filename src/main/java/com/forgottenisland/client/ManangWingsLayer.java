package com.forgottenisland.client;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.adventure.AdventureContent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;

/** Original broad, scalloped bat wings, folded while walking and spread for gliding. */
public final class ManangWingsLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    private final ModelPart root,left,right;
    public ManangWingsLayer(PlayerRenderer renderer) {
        super(renderer);
        var mesh=new MeshDefinition();var r=mesh.getRoot();
        for(int side:new int[]{-1,1}) {
            CubeListBuilder cubes=CubeListBuilder.create();
            cubes.texOffs(3*128,0).addBox(side<0?-23:0,0,0,23,2,2);
            for(int i=0;i<4;i++) {
                float start=i*5,drop=13-i*2;
                cubes.texOffs(2*128,0).addBox(side<0?-start-5:start,2,1,5,drop,1);
                cubes.texOffs(3*128,0).addBox(side<0?-start-1:start,1,.8f,1,drop+1,1.5f);
            }
            cubes.texOffs(5*128,0).addBox(side<0?-25:23,-1,0,2,5,2);
            r.addOrReplaceChild(side<0?"left":"right",cubes,PartPose.offset(side*2,2,3));
        }
        root=LayerDefinition.create(mesh,1024,128).bakeRoot();left=root.getChild("left");right=root.getChild("right");
    }
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,float swing,float amount,float partial,float age,float yaw,float pitch) {
        var item=player.getItemBySlot(EquipmentSlot.CHEST);
        if(!item.is(AdventureContent.MANANG_WINGS.get())||player.isInvisible())return;
        float spread=player.isFallFlying()?.12f:1.08f;
        float flutter=(float)Math.sin(age*(player.isFallFlying()?.13:.07))*.055f;
        left.zRot=-spread+flutter;right.zRot=spread-flutter;
        left.yRot=.22f;right.yRot=-.22f;
        pose.pushPose();getParentModel().body.translateAndRotate(pose);pose.translate(0,0,.10);
        var vertices=ItemRenderer.getArmorFoilBuffer(buffers,RenderType.entityCutoutNoCull(ForgottenIsland.id("textures/entity/manang.png")),false,item.hasFoil());
        root.render(pose,vertices,light,OverlayTexture.NO_OVERLAY,1,1,1,1);pose.popPose();
    }
}
