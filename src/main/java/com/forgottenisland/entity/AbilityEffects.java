package com.forgottenisland.entity;

import com.forgottenisland.IslandConfig;
import com.forgottenisland.ForgottenIsland;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class AbilityEffects {
    public static void activate(IslandCreature c,int kind) {
        if(!(c.level() instanceof ServerLevel level)) return;
        double range=IslandConfig.ABILITY_RANGE.get();
        for(LivingEntity e:level.getEntitiesOfClass(LivingEntity.class,c.getBoundingBox().inflate(range), e->e.isAlive()&&e.distanceToSqr(c)<range*range)) {
            boolean ally=e==c||c.isAlliedTo(e);
            if(ally) switch(c.species) {
                case BAKUNAWA -> {if(kind==1) effect(e,MobEffects.DAMAGE_RESISTANCE,200,0);}
                case RAWW -> effect(e,MobEffects.MOVEMENT_SPEED,160,0);
                case MANANG -> effect(e,MobEffects.SLOW_FALLING,160,0);
                case SARIMANOK -> effect(e,MobEffects.SLOW_FALLING,240,0);
                case DATU -> effect(e,MobEffects.DAMAGE_RESISTANCE,200,1);
                case DUWENDE -> effect(e,MobEffects.REGENERATION,100,0);
                case BUNGI -> effect(e,MobEffects.JUMP,160,0);
                case BATIBA -> effect(e,MobEffects.NIGHT_VISION,300,0);
                case MERMAHN -> {effect(e,MobEffects.WATER_BREATHING,400,0);effect(e,MobEffects.DOLPHINS_GRACE,200,0);}
                case ZIPPY -> {
                    e.removeEffect(MobEffects.POISON);e.removeEffect(MobEffects.HUNGER);
                    effect(e,MobEffects.DIG_SPEED,200,0);
                }
                case TIKBALANG -> effect(e,MobEffects.MOVEMENT_SPEED,160,0);
                case SARANGAY -> effect(e,MobEffects.ABSORPTION,200,1);
                case CHEF -> {e.clearFire();effect(e,MobEffects.FIRE_RESISTANCE,200,0);}
            }
            else if(e instanceof Monster && !c.isAlliedTo(e)) switch(c.species) {
                case BAKUNAWA, BUNGI, SARIMANOK -> {
                    Vec3 d=e.position().subtract(c.position()).normalize();
                    e.push(d.x*.9,.25,d.z*.9);e.hurtMarked=true;
                }
                case MANANG -> effect(e,MobEffects.WEAKNESS,120,0);
                case RAWW, BATIBA -> effect(e,MobEffects.GLOWING,160,0);
                case TIKBALANG -> effect(e,MobEffects.MOVEMENT_SLOWDOWN,120,1);
                default -> { }
            }
        }
        pulse(c);
    }
    private static void effect(LivingEntity e,MobEffect effect,int ticks,int amplifier) {
        e.addEffect(new MobEffectInstance(effect,ticks,amplifier,false,true,true));
    }
    public static void pulse(IslandCreature c) {
        if(!(c.level() instanceof ServerLevel level)) return;
        ParticleOptions particle=switch(c.species) {
            case BAKUNAWA -> c.abilityKind()==1?ForgottenIsland.MEMORY_MOTE.get():ParticleTypes.CLOUD;
            case RAWW -> ParticleTypes.CRIT;
            case MANANG -> ParticleTypes.ENCHANT;
            case SARIMANOK -> new DustParticleOptions(new Vector3f(1,.6f,.15f),1.1f);
            case DATU -> ParticleTypes.HAPPY_VILLAGER;
            case DUWENDE -> ParticleTypes.HEART;
            case BUNGI -> ParticleTypes.NOTE;
            case BATIBA -> ForgottenIsland.MEMORY_MOTE.get();
            case MERMAHN -> ParticleTypes.BUBBLE_POP;
            case ZIPPY -> new DustParticleOptions(new Vector3f(.65f,.35f,1),.7f);
            case TIKBALANG -> ParticleTypes.WAX_OFF;
            case SARANGAY -> new DustParticleOptions(new Vector3f(.8f,.5f,1),1.1f);
            case CHEF -> ParticleTypes.SMALL_FLAME;
        };
        double phase=c.tickCount*.15;
        int count=c.species==Species.DUWENDE?4:c.species==Species.BAKUNAWA?12:8;
        for(int i=0;i<count;i++) {
            double a=i*Math.PI*2/count+phase, radius=1.3+Math.sin(c.activeTicks()*.06)*.3;
            level.sendParticles(particle,c.getX()+Math.cos(a)*radius,c.getY()+.5+(i%3)*.5,c.getZ()+Math.sin(a)*radius,1,0,.03,0,.01);
        }
        if(c.species==Species.CHEF) level.sendParticles(ParticleTypes.CLOUD,c.getX(),c.getY()+1.2,c.getZ(),2,.2,.1,.2,.015);
    }
}
