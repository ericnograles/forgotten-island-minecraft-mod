package com.forgottenisland.item;
import com.forgottenisland.client.ClientHooks;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class GuideItem extends Item {
    public GuideItem() {super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand) {
        if(l.isClientSide) DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->ClientHooks.openGuide());
        return InteractionResultHolder.sidedSuccess(p.getItemInHand(hand),l.isClientSide);
    }
}
