package com.forgottenisland.adventure;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

record Arena(String id,BossKind kind,ResourceKey<Level> dimension,BlockPos center,int slot,boolean direct) {
    static Arena island(BossKind kind){return new Arena(kind.id,kind,AdventureContent.HIGHLANDS,kind.center,0,false);}
    boolean testing(){return slot>0;}
    boolean contains(Vec3 point){return kind.inside(point,center);}
}
