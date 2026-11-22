package com.forgottenisland.adventure;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;
import javax.annotation.Nullable;

public final class CrystalSwordItem extends com.forgottenisland.arsenal.ArsenalSwordItem {
    public static final float FULL_DAMAGE=6000;
    // Netherite contributes 4, this contributes 5995, and the player's base contributes 1.
    // Forge's attack attribute cap is compensated on the actual primary damage event.
    public CrystalSwordItem() {super(Power.CRYSTAL);}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> lines,TooltipFlag flag) {
        super.appendHoverText(stack,level,lines,flag);
    }
}
