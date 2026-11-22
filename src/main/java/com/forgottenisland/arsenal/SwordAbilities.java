package com.forgottenisland.arsenal;

import com.forgottenisland.ForgottenIsland;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid=ForgottenIsland.ID)
public final class SwordAbilities {
    private static final Set<UUID> SNEAKING=new HashSet<>();
    public static boolean enemy(Player owner,LivingEntity target){return target instanceof Monster&&target.isAlive()&&!owner.isAlliedTo(target)&&!target.isAlliedTo(owner);}
    public static boolean activate(ServerPlayer p,ItemStack held){
        if(!(held.getItem() instanceof ArsenalSwordItem sword)||!p.isAlive()||p.isSpectator()||p.isPassenger())return false;
        var power=sword.power;var data=p.getPersistentData();long now=p.server.overworld().getGameTime();String key="IslandSwordReady_"+power.name();
        long remaining=data.getLong(key)-now;
        if(remaining>0){p.displayClientMessage(Component.literal(power.ability+": ready in "+((remaining+19)/20)+"s"),true);return false;}
        data.putLong(key,now+power.cooldown);p.getCooldowns().addCooldown(sword,power.cooldown);
        var level=p.serverLevel();
        if(power==ArsenalSwordItem.Power.SAPPHIRE){
            int count=0;
            for(var target:level.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(32),e->enemy(p,e)))if(p.distanceToSqr(target)<=32*32){target.addEffect(new MobEffectInstance(MobEffects.GLOWING,160,0,false,false));count++;}
            level.sendParticles(ParticleTypes.SOUL,p.getX(),p.getY()+1,p.getZ(),48,2,1,2,.05);
            level.playSound(null,p.blockPosition(),SoundEvents.ALLAY_AMBIENT_WITH_ITEM,SoundSource.PLAYERS,1,.65f);
            p.displayClientMessage(Component.literal("Sapphire sight: "+count+" enemies revealed for 8 seconds"),true);
        }else if(power==ArsenalSwordItem.Power.PHOENIX){
            var fireball=new PhoenixFireball(level,p);fireball.shootFromRotation(p,p.getXRot(),p.getYRot(),0,1.6f,0);level.addFreshEntity(fireball);
            level.playSound(null,p.blockPosition(),SoundEvents.BLAZE_SHOOT,SoundSource.PLAYERS,.8f,1.2f);
        }else{
            for(var target:level.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(6),e->enemy(p,e)))if(p.distanceToSqr(target)<=36&&p.hasLineOfSight(target)){
                if(target.hurt(p.damageSources().indirectMagic(p,p),60)){target.setSecondsOnFire(5);target.addEffect(new MobEffectInstance(MobEffects.POISON,160,0),p);}
            }
            for(int i=0;i<80;i++){double a=i*Math.PI*2/80;for(int r=2;r<=6;r+=2){level.sendParticles(i%2==0?ParticleTypes.FLAME:ParticleTypes.SNEEZE,p.getX()+Math.cos(a)*r,p.getY()+.7,p.getZ()+Math.sin(a)*r,1,0,.3,0,.02);}}
            level.playSound(null,p.blockPosition(),SoundEvents.FIRECHARGE_USE,SoundSource.PLAYERS,1,.7f);
        }
        held.hurtAndBreak(1,p,e->e.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));return true;
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p))return;
        if(!p.isShiftKeyDown()){SNEAKING.remove(p.getUUID());return;}
        if(SNEAKING.add(p.getUUID()))activate(p,p.getMainHandItem());
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){SNEAKING.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void clone(PlayerEvent.Clone e){
        // Forge replaces the player entity after death; retain server cooldowns there too.
        for(var power:ArsenalSwordItem.Power.values()){
            String key="IslandSwordReady_"+power.name();
            if(e.getOriginal().getPersistentData().contains(key))e.getEntity().getPersistentData().putLong(key,e.getOriginal().getPersistentData().getLong(key));
        }
        SNEAKING.remove(e.getEntity().getUUID());
    }
    private SwordAbilities(){}
}
