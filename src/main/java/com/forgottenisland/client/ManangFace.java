package com.forgottenisland.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Shared facial rig for the friendly and hostile Manang. */
final class ManangFace {
    static void animate(ModelPart upper,float age,float snarl,float gaze){
        ModelPart head=upper.getChild("head");
        head.getChild("jaw").xRot-=.16f+snarl*.68f+.035f*Mth.sin(age*.15f);
        for(int side:new int[]{-1,1}){
            String end=side<0?"left":"right";
            head.getChild("eye_"+end).yRot+=gaze;
            head.getChild("brow_"+end).zRot-=side*snarl*.19f;
            var lock=head.getChild("lock_"+end);
            lock.zRot+=side*(.04f*Mth.sin(age*.12f)+snarl*.07f);
            lock.getChild("tip_"+end).zRot+=side*(.11f*Mth.sin(age*.12f-.9f)+snarl*.09f);
            head.getChild("mantle_"+end).xRot+=.06f*Mth.sin(age*.1f-.65f);
            upper.getChild("wing_"+end).getChild("spar_"+end).zRot+=side*(.17f*Mth.sin(age*.22f-.8f)+.2f*snarl);
        }
    }
    private ManangFace(){}
}
