package com.forgottenisland.adventure;

/** Shared timing/balance constants: renderer and authoritative combat use the same clock. */
public final class BakunawaMoves {
    public static final float TENTACLE_DAMAGE=7,BEAM_DAMAGE=10;
    public static final int IMPACT=60,RECOVERY=80;
    public static double lunge(int tick){
        if(tick<35)return 0;
        if(tick<=60)return smooth((tick-35)/25.0);
        return 1-smooth((tick-67)/28.0);
    }
    public static double tentacleHeight(int tick){
        if(tick<40)return 0;
        if(tick<60)return smooth((tick-40)/20.0);
        if(tick<72)return 1;
        return 1-smooth((tick-72)/20.0);
    }
    public static double smooth(double n){double t=Math.max(0,Math.min(1,n));return t*t*(3-2*t);}
    private BakunawaMoves(){}
}
