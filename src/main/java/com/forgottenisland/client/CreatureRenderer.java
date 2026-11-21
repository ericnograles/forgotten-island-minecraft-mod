package com.forgottenisland.client;
import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.entity.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;

public class CreatureRenderer extends MobRenderer<IslandCreature,CreatureModel> {
    public CreatureRenderer(EntityRendererProvider.Context context,Species s) {
        super(context,new CreatureModel(context.bakeLayer(CreatureModel.layer(s))),s.width*.4f);
    }
    @Override public ResourceLocation getTextureLocation(IslandCreature entity) {return ForgottenIsland.id("textures/entity/"+entity.species.id+".png");}
    @Override public boolean shouldRender(IslandCreature e,Frustum f,double x,double y,double z) {
        return f.isVisible(e.getBoundingBox().inflate(e.species==Species.BAKUNAWA?12:4));
    }
}
