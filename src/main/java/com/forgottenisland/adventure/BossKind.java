package com.forgottenisland.adventure;

import com.forgottenisland.entity.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public enum BossKind {
    MANANG("manang_boss","Manang · The Severed Twilight",Species.MANANG,new BlockPos(IslandLayout.MOUNTAIN_X,105,IslandLayout.MOUNTAIN_Z),29,220,120,110),
    BAKUNAWA("bakunawa_boss","Bakunawa · Devourer of Moons",Species.BAKUNAWA,new BlockPos(IslandLayout.COAST_X,65,IslandLayout.COAST_Z),84,400,160,140);
    public final String id,title;
    public final Species species;
    public final BlockPos center;
    public final int radius,entranceLength,defeatLength;
    public final float baseHealth;
    BossKind(String id,String title,Species species,BlockPos center,int radius,float hp,int entrance,int defeat) {
        this.id=id;this.title=title;this.species=species;this.center=center;this.radius=radius;
        baseHealth=hp;entranceLength=entrance;defeatLength=defeat;
    }
    public Vec3 origin(){return Vec3.atBottomCenterOf(center).add(0,0,this==BAKUNAWA?23:0);}
    public boolean inside(Vec3 p){return Math.abs(p.y-center.getY())<48&&p.subtract(Vec3.atBottomCenterOf(center)).horizontalDistanceSqr()<radius*radius;}
    public BlockPos altar(){return center.offset(0,0,this==MANANG?20:50);}
    public int cycle(int phase){return this==MANANG?(phase==1?180:155):(phase==1?220:185);}
}
