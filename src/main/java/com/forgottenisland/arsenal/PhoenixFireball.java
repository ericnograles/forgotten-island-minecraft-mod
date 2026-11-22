package com.forgottenisland.arsenal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

public final class PhoenixFireball extends ThrowableItemProjectile {
    public PhoenixFireball(EntityType<? extends PhoenixFireball> type,Level level){super(type,level);}
    public PhoenixFireball(Level level,LivingEntity owner){super(ArsenalContent.FIREBALL.get(),owner,level);}
    @Override protected Item getDefaultItem(){return Items.FIRE_CHARGE;}
    @Override protected float getGravity(){return 0;}
    @Override public void tick(){
        super.tick();
        if(level().isClientSide){level().addParticle(ParticleTypes.FLAME,getX(),getY(),getZ(),0,0,0);level().addParticle(ParticleTypes.SMOKE,getX(),getY(),getZ(),0,.01,0);}
        else if(tickCount>80)discard();
    }
    @Override protected void onHitEntity(EntityHitResult hit){
        super.onHitEntity(hit);
        if(!level().isClientSide&&getOwner() instanceof Player owner&&hit.getEntity() instanceof LivingEntity target&&SwordAbilities.enemy(owner,target)){
            if(target.hurt(damageSources().indirectMagic(this,owner),200))target.setSecondsOnFire(5);
        }
    }
    @Override protected void onHit(HitResult hit){
        super.onHit(hit);
        if(level() instanceof ServerLevel level){
            level.sendParticles(ParticleTypes.FLAME,getX(),getY(),getZ(),36,.6,.6,.6,.08);
            level.playSound(null,blockPosition(),SoundEvents.GENERIC_EXPLODE,SoundSource.PLAYERS,.55f,1.4f);discard();
        }
    }
}
