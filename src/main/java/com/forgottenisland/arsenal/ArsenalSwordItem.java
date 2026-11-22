package com.forgottenisland.arsenal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;
import javax.annotation.Nullable;

public class ArsenalSwordItem extends SwordItem {
    public enum Power {
        CRYSTAL(6000,200,"Fire and poison burst"), SAPPHIRE(1000,300,"Reveal nearby enemies"), PHOENIX(2600,100,"Launch a fireball");
        public final int damage,cooldown;
        public final String ability;
        Power(int damage,int cooldown,String ability){this.damage=damage;this.cooldown=cooldown;this.ability=ability;}
    }
    public final Power power;
    public ArsenalSwordItem(Power power){
        super(Tiers.NETHERITE,power.damage-5,-2.4f,new Item.Properties().durability(4096).fireResistant().rarity(Rarity.EPIC));
        this.power=power;
    }
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> lines,TooltipFlag flag){
        lines.add(Component.literal(String.format(java.util.Locale.ROOT,"%,d base melee damage at full charge.",power.damage)));
        lines.add(Component.literal("Press Sneak while held: "+power.ability));
        lines.add(Component.literal((power.cooldown/20)+" second cooldown. Release Sneak before using again."));
        lines.add(Component.literal("Repair with netherite ingots, or Mending."));
    }
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){
        super.inventoryTick(stack,level,entity,slot,selected);
        if(level.isClientSide&&selected&&entity instanceof LivingEntity living&&entity.tickCount%5==0&&power!=Power.SAPPHIRE){
            var point=living.getEyePosition().add(living.getLookAngle().scale(.7)).add(.18,-.55,0);
            level.addParticle(power==Power.CRYSTAL?ParticleTypes.SMALL_FLAME:ParticleTypes.FLAME,point.x,point.y,point.z,0,.015,0);
        }
    }
}
