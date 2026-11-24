package com.forgottenisland.adventure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class MoonRocket extends Entity {
    private static BlockPos freeBasin(net.minecraft.server.level.ServerLevel moon, ServerPlayer pilot) {
        var data = IslandSavedData.get(moon.getServer());
        for (int index=0; ; index++) {
            var center = new BlockPos(index*LunarGenerator.BASIN_SPACING, 65, 0);
            if (data.tests.values().stream().noneMatch(a -> a.lunar() && a.center().equals(center))
                    && moon.players().stream().noneMatch(p -> p != pilot && p.distanceToSqr(Vec3.atCenterOf(center)) < 128*128))
                return center;
        }
    }
    private boolean descending;
    private int flightTicks;
    private double landingY;

    public MoonRocket(EntityType<? extends MoonRocket> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    static boolean board(ServerPlayer player, Vec3 feet, boolean descending) {
        var level = player.serverLevel();
        var ship = new MoonRocket(AdventureContent.ROCKET.get(), level);
        ship.descending = descending;
        ship.landingY = feet.y - 24;
        ship.moveTo(feet.x, feet.y, feet.z, 180, 0);
        if (!level.addFreshEntity(ship)) return false;
        if (!player.startRiding(ship, true)) { ship.discard(); return false; }
        player.playNotifySound(descending ? SoundEvents.BEACON_ACTIVATE : SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.PLAYERS, 1, descending ? .8f : .6f);
        return true;
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            for (int i=0; i<4; i++) {
                double x=getX()+(random.nextDouble()-.5)*.45, z=getZ()+(random.nextDouble()-.5)*.45;
                level().addParticle(i==0 ? ParticleTypes.CAMPFIRE_COSY_SMOKE : ParticleTypes.FLAME, x,getY()-.18,z,0,-.18,0);
            }
            return;
        }
        if (!(getFirstPassenger() instanceof ServerPlayer pilot)) { discard(); return; }
        flightTicks++;
        pilot.fallDistance = 0;
        if (flightTicks%20==1) {
            pilot.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 0, false, false));
            pilot.displayClientMessage(Component.literal(descending ? "Lunar approach · landing in the impact basin" : "Moon Rocket · climbing to lunar orbit"),true);
        }
        if (descending) {
            setPos(getX(), Math.max(landingY, getY()-.44), getZ());
            if (getY() <= landingY+.01) {
                pilot.stopRiding();
                BakunawaStomach.transfer(pilot,pilot.serverLevel(),position());
                startBattle(pilot);
                discard();
            }
        } else if (flightTicks < 105) {
            setPos(getX(),getY()+Math.min(.95,flightTicks*.016),getZ());
        } else {
            var moon=pilot.server.getLevel(AdventureContent.MOON);
            if (moon == null) { pilot.stopRiding(); discard(); return; }
            var landing=freeBasin(moon,pilot).south(8);
            moon.getChunk(landing);
            var ground=moon.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, landing);
            var approach=Vec3.atBottomCenterOf(ground).add(0,24,0);
            BakunawaStomach.transfer(pilot,moon,approach);
            if (!board(pilot,approach,true)) {
                BakunawaStomach.transfer(pilot,moon,Vec3.atBottomCenterOf(ground));
                startBattle(pilot);
            }
            discard();
        }
    }

    private static void startBattle(ServerPlayer pilot) {
        var level = pilot.serverLevel();
        var data = IslandSavedData.get(pilot.server);
        BlockPos landingCenter = new BlockPos(pilot.getBlockX(),65,pilot.getBlockZ()-8);
        BlockPos center = landingCenter;
        // A saved flight can reconnect above a basin claimed while its pilot was offline.
        if (data.tests.values().stream().anyMatch(a -> a.lunar() && a.center().equals(landingCenter))) {
            var free = freeBasin(level,pilot);
            level.getChunk(free);
            BakunawaStomach.transfer(pilot,level,Vec3.atBottomCenterOf(level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,free.south(8))));
            center = free;
        }
        var arena = data.openMoonBattle(center);
        BossEncounters.start(level,arena,data,java.util.List.of(pilot));
        MoonPursuit.landed(pilot);
    }

    @Override public double getPassengersRidingOffset() { return 1.30; }
    @Override protected void defineSynchedData() {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Descending",descending); tag.putInt("FlightTicks",flightTicks); tag.putDouble("LandingY",landingY);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        descending=tag.getBoolean("Descending"); flightTicks=tag.getInt("FlightTicks"); landingY=tag.getDouble("LandingY");
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
