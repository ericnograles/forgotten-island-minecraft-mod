package com.forgottenisland.egg;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;
import javax.annotation.Nullable;

/** Retains the existing spawn-egg registry ID so old inventories migrate without missing items. */
public final class BakunawaEggItem extends BlockItem {
    public BakunawaEggItem(){super(EggContent.EGG.get(),new Item.Properties().stacksTo(16).rarity(Rarity.RARE));}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> lines,TooltipFlag flag){
        lines.add(Component.literal("Place to hatch your friendly Bakunawa in 60 loaded seconds."));
        lines.add(Component.literal("Needs open space. Grows up in 20 loaded minutes."));
        lines.add(Component.literal("Feed prismarine crystals to help it grow. Adults can be ridden."));
    }
}
