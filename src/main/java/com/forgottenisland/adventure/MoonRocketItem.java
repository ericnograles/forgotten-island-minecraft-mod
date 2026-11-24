package com.forgottenisland.adventure;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class MoonRocketItem extends Item {
    MoonRocketItem() { super(new Properties().stacksTo(1).rarity(Rarity.RARE)); }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player pilot, InteractionHand hand) {
        ItemStack stack = pilot.getItemInHand(hand);
        if (pilot instanceof ServerPlayer player) {
            if (!MoonPursuit.canFly(player) || player.isPassenger() || player.isSpectator()
                    || player.server.getLevel(AdventureContent.MOON) == null) return InteractionResultHolder.fail(stack);
            if (!level.canSeeSky(player.blockPosition().above())) {
                player.displayClientMessage(Component.literal("Take your Moon Rocket outside to launch."), true);
                return InteractionResultHolder.fail(stack);
            }
            if (!MoonRocket.board(player, player.position(), false)) return InteractionResultHolder.fail(stack);
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
