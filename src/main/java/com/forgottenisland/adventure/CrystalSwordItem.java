package com.forgottenisland.adventure;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;
import javax.annotation.Nullable;

public final class CrystalSwordItem extends SwordItem {
    public static final float FULL_DAMAGE=6000;
    // Netherite contributes 4, this contributes 5995, and the player's base contributes 1.
    // Forge's attack attribute cap is compensated on the actual primary damage event.
    public CrystalSwordItem() {super(Tiers.NETHERITE,5995,-2.4f,new Item.Properties().durability(4096).fireResistant().rarity(Rarity.EPIC));}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> lines,TooltipFlag flag) {
        lines.add(Component.literal("6,000 base melee damage at full charge."));
        lines.add(Component.literal("Before armor; criticals and enchantments can increase it."));
        lines.add(Component.literal("Repair with netherite ingots, or Mending."));
    }
}
