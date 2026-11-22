package com.forgottenisland.adventure;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;
import javax.annotation.Nullable;

public final class ManangWingsItem extends ElytraItem {
    public ManangWingsItem(){super(new Item.Properties().durability(648).rarity(Rarity.EPIC));}
    @Override public boolean isValidRepairItem(ItemStack damaged,ItemStack ingredient){return ingredient.is(Items.PHANTOM_MEMBRANE);}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> lines,TooltipFlag flag) {
        lines.add(Component.literal("Chest slot • Jump again while falling to glide."));
        lines.add(Component.literal("648 durability • 1 durability each second of flight."));
        lines.add(Component.literal("Repair with phantom membranes; supports Mending."));
        lines.add(Component.literal("Firework rockets boost flight as with Elytra."));
    }
}
