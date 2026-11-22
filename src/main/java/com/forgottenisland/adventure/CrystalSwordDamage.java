package com.forgottenisland.adventure;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.arsenal.ArsenalSwordItem;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Preserve vanilla timing/critical logic while correcting the capped attack attribute. */
@Mod.EventBusSubscriber(modid=ForgottenIsland.ID)
public final class CrystalSwordDamage {
    private record Hit(UUID target,long tick) {}
    private static final Map<UUID,Hit> PRIMARY=new HashMap<>();
    @SubscribeEvent public static void attack(AttackEntityEvent e) {
        Player p=e.getEntity();
        if(!p.level().isClientSide&&p.getMainHandItem().getItem() instanceof ArsenalSwordItem)
            PRIMARY.put(p.getUUID(),new Hit((e.getTarget() instanceof BossPart part?part.getParent():e.getTarget()).getUUID(),p.level().getGameTime()));
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void correctAttributeCap(LivingHurtEvent e) {
        if(!e.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)||!(e.getSource().getDirectEntity() instanceof Player p)||p.level().isClientSide||!(p.getMainHandItem().getItem() instanceof ArsenalSwordItem sword))return;
        Hit hit=PRIMARY.get(p.getUUID());
        if(hit==null||hit.tick!=p.level().getGameTime()||!hit.target.equals(e.getEntity().getUUID()))return;
        PRIMARY.remove(p.getUUID());
        double effective=p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if(effective>0&&Double.isFinite(effective))e.setAmount(e.getAmount()*(sword.power.damage/(float)effective));
    }
    @SubscribeEvent public static void cleanup(TickEvent.ServerTickEvent e) {
        if(e.phase==TickEvent.Phase.END)PRIMARY.clear();
    }
}
