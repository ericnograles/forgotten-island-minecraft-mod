package com.forgottenisland.arsenal;

import com.forgottenisland.ForgottenIsland;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/** Keep vanilla armor/toughness, enchantment and absorption order, but honor literal crystal strength. */
@Mod.EventBusSubscriber(modid=ForgottenIsland.ID)
public final class CrystalArmorProtection {
    public static double uncapped(AttributeInstance attribute){
        double base=attribute.getBaseValue();
        for(var m:attribute.getModifiers(AttributeModifier.Operation.ADDITION))base+=m.getAmount();
        double value=base;
        for(var m:attribute.getModifiers(AttributeModifier.Operation.MULTIPLY_BASE))value+=base*m.getAmount();
        for(var m:attribute.getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL))value*=1+m.getAmount();
        return Math.max(0,value);
    }
    public static float correctedInput(float damage,float capped,float full,float toughness){
        float desired=CombatRules.getDamageAfterAbsorb(damage,full,toughness),lo=0,hi=damage;
        // Invert the normal monotonic armor calculation; vanilla then applies armor exactly once.
        for(int i=0;i<32;i++){float mid=(lo+hi)*.5f;if(CombatRules.getDamageAfterAbsorb(mid,capped,toughness)<desired)lo=mid;else hi=mid;}
        return (lo+hi)*.5f;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void protect(LivingHurtEvent e){
        if(e.getEntity().level().isClientSide||e.getSource().is(DamageTypeTags.BYPASSES_ARMOR)||!Float.isFinite(e.getAmount())||e.getAmount()<=0)return;
        boolean crystal=false;for(var stack:e.getEntity().getArmorSlots())if(stack.getItem() instanceof CrystalArmorItem){crystal=true;break;}
        if(!crystal)return;
        var armor=e.getEntity().getAttribute(Attributes.ARMOR);if(armor==null)return;
        float capped=e.getEntity().getArmorValue(),full=(float)uncapped(armor);
        if(full>capped)e.setAmount(correctedInput(e.getAmount(),capped,full,(float)e.getEntity().getAttributeValue(Attributes.ARMOR_TOUGHNESS)));
    }
    private CrystalArmorProtection(){}
}
