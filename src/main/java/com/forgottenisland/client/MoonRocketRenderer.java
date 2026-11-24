package com.forgottenisland.client;

import com.forgottenisland.adventure.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

public final class MoonRocketRenderer extends EntityRenderer<MoonRocket> {
    public MoonRocketRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius=.8f; }
    @Override public ResourceLocation getTextureLocation(MoonRocket rocket) { return TextureAtlas.LOCATION_BLOCKS; }
    @Override public void render(MoonRocket ship,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose();
        pose.translate(0,1.02,0);
        pose.scale(2.05f,2.05f,2.05f);
        Minecraft.getInstance().getItemRenderer().renderStatic(AdventureContent.MOON_ROCKET.get().getDefaultInstance(),
            ItemDisplayContext.NONE, net.minecraft.client.renderer.LightTexture.pack(
                Math.max(8,net.minecraft.client.renderer.LightTexture.block(light)),net.minecraft.client.renderer.LightTexture.sky(light)),OverlayTexture.NO_OVERLAY,pose,buffers,ship.level(),ship.getId());
        pose.popPose();
        super.render(ship,yaw,partial,pose,buffers,light);
    }
}
