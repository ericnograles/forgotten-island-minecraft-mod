package com.forgottenisland.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.*;

/** Faceted crown, shoulder crystals and greaves from the supplied armor drawing. */
public final class CrystalArmorModel extends HumanoidModel<LivingEntity> {
    private static CrystalArmorModel outer,inner;
    private CrystalArmorModel(boolean leggings){super(make(leggings).bakeRoot());}
    private static LayerDefinition make(boolean leggings){
        var mesh=HumanoidModel.createMesh(new CubeDeformation(leggings?.5f:1),0);var root=mesh.getRoot();
        if(!leggings){
            for(int side:new int[]{-1,1}){
                root.getChild("head").addOrReplaceChild("crystal_"+side,CubeListBuilder.create().texOffs(0,0).addBox(-1,-5,-1,2,6,2),PartPose.offsetAndRotation(side*3,-8,0,0,0,side*.22f));
                String arm=side<0?"right_arm":"left_arm";
                root.getChild(arm).addOrReplaceChild("shoulder",CubeListBuilder.create().texOffs(40,16).addBox(-1,-4,-1,2,6,2),PartPose.offsetAndRotation(side*3,-1,0,0,0,side*.45f));
                root.getChild(side<0?"right_leg":"left_leg").addOrReplaceChild("greave",CubeListBuilder.create().texOffs(0,16).addBox(-1,-2,-1,2,5,2),PartPose.offsetAndRotation(side*2,8,-2,-.3f,0,side*.3f));
            }
            root.getChild("head").addOrReplaceChild("crown",CubeListBuilder.create().texOffs(0,0).addBox(-1,-4,-1,2,4,2),PartPose.offset(0,-8,-3));
        }
        return LayerDefinition.create(mesh,64,32);
    }
    public static HumanoidModel<?> fitted(EquipmentSlot slot,HumanoidModel<?> source){
        boolean leggings=slot==EquipmentSlot.LEGS;
        if(leggings&&inner==null)inner=new CrystalArmorModel(true);if(!leggings&&outer==null)outer=new CrystalArmorModel(false);
        var model=leggings?inner:outer;
        ModelPart[] a={model.head,model.hat,model.body,model.leftArm,model.rightArm,model.leftLeg,model.rightLeg};
        ModelPart[] b={source.head,source.hat,source.body,source.leftArm,source.rightArm,source.leftLeg,source.rightLeg};
        for(int i=0;i<a.length;i++){a[i].copyFrom(b[i]);a[i].visible=b[i].visible;}
        model.young=source.young;model.riding=source.riding;model.crouching=source.crouching;return model;
    }
}
