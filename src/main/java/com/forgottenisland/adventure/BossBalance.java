package com.forgottenisland.adventure;

/** Damage above a charged diamond sword has diminishing returns against boss hide. */
public final class BossBalance {
    public static float damage(float raw,boolean exposed,boolean secondForm){
        if(!Float.isFinite(raw)||raw<=0)return 0;
        double effective=raw<=8?raw:8+3*Math.log1p((raw-8)/8);
        return (float)(effective*(exposed?1.5:.6)*(secondForm?.85:1));
    }
    private BossBalance(){}
}
