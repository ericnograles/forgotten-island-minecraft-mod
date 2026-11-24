package com.forgottenisland.adventure;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.network.IslandNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ForgottenIsland.ID)
public final class MoonPursuit {
    public static final int BITE_TICKS = 120;
    private static final String PURSUIT = "ForgottenIslandMoonPursuit";
    private static final String REVEAL = "ForgottenIslandMoonrise";

    // Forge copies this compound when a real death replaces the player entity.
    private static CompoundTag journal(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG)) data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return data.getCompound(Player.PERSISTED_NBT_TAG);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void defeated(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getSource().getEntity() instanceof IslandBoss boss
                && boss.kind == BossKind.BAKUNAWA && boss.stage() == EncounterState.Stage.COMBAT) {
            journal(player).putBoolean(PURSUIT, true);
            journal(player).putBoolean(REVEAL, true);
        }
    }

    static boolean canFly(ServerPlayer player) { return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(PURSUIT); }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.isEndConquered() && event.getEntity() instanceof ServerPlayer player && canFly(player)) giveRocket(player);
    }

    private static void giveRocket(ServerPlayer player) {
        ItemStack rocket = AdventureContent.MOON_ROCKET.get().getDefaultInstance();
        if (!player.getInventory().contains(rocket) && !player.addItem(rocket)) {
            var drop = player.drop(rocket, false);
            if (drop != null) { drop.setTarget(player.getUUID()); drop.setNoPickUpDelay(); }
        }
    }

    @SubscribeEvent
    public static void reveal(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !player.isAlive() || player.tickCount < 20 || player.tickCount % 10 != 0) return;
        if (player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(REVEAL)) {
            journal(player).remove(REVEAL);
            IslandNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new IslandNetwork.Moonrise());
            player.sendSystemMessage(Component.literal("Bakunawa is climbing toward the Moon! Use your Moon Rocket under open sky to follow."));
        }
    }

    public static void recoverRocket(ServerPlayer player) {
        if (player.getVehicle() instanceof MoonRocket) return;
        if (canFly(player) && player.getInventory().getFreeSlot() < 0) {
            player.displayClientMessage(Component.literal("Make room in your inventory for the Moon Rocket."),true);
            return;
        }
        if (canFly(player)) giveRocket(player);
        else player.sendSystemMessage(Component.literal("The Moon Rocket answers those defeated by Bakunawa."));
    }

    static void landed(ServerPlayer player) {
        journal(player).remove(PURSUIT);
        journal(player).remove(REVEAL);
        player.sendSystemMessage(Component.literal("Lunar impact basin · Strike Bakunawa’s lowered jaw to save the Moon. /island home returns you home."));
    }
    private MoonPursuit() {}
}
