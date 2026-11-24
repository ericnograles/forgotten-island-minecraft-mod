package com.forgottenisland.adventure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraftforge.entity.PartEntity;

/** Server-routed multipart hit regions; no independent saves, AI or item drops. */
public final class BossPart extends PartEntity<IslandBoss> {
    private final EntityDimensions size;
    public final boolean weak;
    public BossPart(IslandBoss parent,float width,float height,boolean weak) {
        super(parent);size=EntityDimensions.scalable(width,height);this.weak=weak;refreshDimensions();
    }
    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override public EntityDimensions getDimensions(Pose pose){return size==null?EntityDimensions.fixed(1,1):size;}
    void place(net.minecraft.world.phys.Vec3 at){
        setPos(at);
        setBoundingBox(size.scale((float)getParent().sizeFactor()).makeBoundingBox(at));
    }
    @Override public boolean isPickable(){return getParent().stage()==EncounterState.Stage.COMBAT || getParent().awaitingMoonStrike();}
    @Override public boolean canBeCollidedWith(){return getParent().stage()==EncounterState.Stage.COMBAT;}
    @Override public boolean hurt(DamageSource source,float amount){return getParent().hurtPart(source,amount,weak);}
    @Override public boolean is(Entity other){return this==other||getParent()==other;}
    @Override public boolean shouldBeSaved(){return false;}
}
