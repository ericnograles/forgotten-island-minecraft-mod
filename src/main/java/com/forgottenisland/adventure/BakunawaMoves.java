package com.forgottenisland.adventure;

/** Shared timing/balance constants: renderer and authoritative combat use the same clock. */
public final class BakunawaMoves {
    public static final float TENTACLE_DAMAGE=7,BEAM_DAMAGE=10;
    public static final float CHOMP_DAMAGE=9,ROCK_DAMAGE=9,FIREBALL_DAMAGE=9,ATOMIC_DAMAGE=1,TAIL_DAMAGE=5,SLASH_DAMAGE=5;
    public static final int CHOMP=0,TENTACLES=1,HOLY_BEAM=2,MOON_ROCKS=3,LUNAR_TIDE=4,ECLIPSE_BREATH=5,FIREBALLS=6,ATOMIC_BREATH=7,WARD=8,TAIL_WHIP=9,SLASH=10,COUNT=11;
    public static final int IMPACT=60,RECOVERY=80;
    public static double lunge(double tick){
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
    public static int recovery(int attack){return attack==ATOMIC_BREATH?130:attack==WARD?120:attack==FIREBALLS?92:80;}
    public static boolean shield(int attack,int tick){return attack==WARD&&tick>=20&&tick<120;}
    public static double distanceToSegment(net.minecraft.world.phys.Vec3 point,net.minecraft.world.phys.Vec3 from,net.minecraft.world.phys.Vec3 to){
        var d=to.subtract(from);double t=d.lengthSqr()<1e-8?0:Math.max(0,Math.min(1,point.subtract(from).dot(d)/d.lengthSqr()));
        return point.distanceTo(from.add(d.scale(t)));
    }
    private BakunawaMoves(){}
}
