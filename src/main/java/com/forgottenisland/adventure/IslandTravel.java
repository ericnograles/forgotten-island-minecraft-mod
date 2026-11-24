package com.forgottenisland.adventure;

import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public final class IslandTravel {
    private record Pending(ServerPlayer player,net.minecraft.resources.ResourceKey<Level> dimension,BlockPos portal) {}
    private static final java.util.Map<java.util.UUID,Pending> PENDING=new java.util.HashMap<>();
    public static final BlockPos MARKET_STAR=new BlockPos(310,64,99), MARKET_ENTRY=new BlockPos(310,65,85);
    public static final BlockPos ARRIVAL=new BlockPos(0,65,40);
    public static final BlockPos RETURN_STAR=new BlockPos(0,64,50);
    private static final String COOLDOWN="ForgottenIslandTravelAfter";
    public static void queue(ServerPlayer player,BlockPos portal) {
        if(player.getPersistentData().getLong(COOLDOWN)<=player.server.overworld().getGameTime())
            PENDING.putIfAbsent(player.getUUID(),new Pending(player,player.level().dimension(),portal.immutable()));
    }
    public static void finishTick(net.minecraft.server.MinecraftServer server) {
        var pending=new java.util.ArrayList<>(PENDING.values());
        PENDING.entrySet().removeIf(e->e.getValue().player.server==server);
        for(var p:pending)if(p.player.server==server&&!p.player.hasDisconnected()&&p.player.level().dimension()==p.dimension&&p.player.distanceToSqr(Vec3.atCenterOf(p.portal))<9)
            through(p.player,p.portal);
    }
    public static void clear(net.minecraft.server.MinecraftServer server) {PENDING.entrySet().removeIf(e->e.getValue().player.server==server);}
    public static void through(ServerPlayer player,BlockPos portal) {
        long now=player.server.overworld().getGameTime();
        if(!player.isAlive()||player.isSpectator()||player.getPersistentData().getLong(COOLDOWN)>now)return;
        if(player.level().dimension()==AdventureContent.ISLAND||player.level().dimension()==AdventureContent.HIGHLANDS) home(player);
        else if(player.level().dimension()==Level.OVERWORLD) {
            var data=IslandSavedData.get(player.server);
            Optional<BlockPos> center=data.portalAt(portal);
            if(center.isEmpty())return;
            ServerLevel destination=player.server.getLevel(AdventureContent.HIGHLANDS);
            if(destination==null){player.displayClientMessage(Component.literal("Forgotten Island is unavailable. Check the server datapacks."),false);return;}
            data.link(player.getUUID(),center.get());
            IslandWorld.initialize(destination,destination.getChunk(MARKET_ENTRY.getX()>>4,MARKET_ENTRY.getZ()>>4));
            transfer(player,destination,safeNear(destination,MARKET_ENTRY).orElseGet(()->fallback(destination,MARKET_ENTRY)));
            player.sendSystemMessage(Component.literal("Welcome to the festival! East junction (400, 35): north to Manang (535, -215), east to Bakunawa (820, -65), south to Batiba (475, 190). /island harbor visits the original island."));
        }
    }
    public static boolean home(ServerPlayer player) {
        if(player.level().dimension()!=AdventureContent.ISLAND&&player.level().dimension()!=AdventureContent.HIGHLANDS&&player.level().dimension()!=AdventureContent.STOMACH&&player.level().dimension()!=AdventureContent.MOON)return false;
        ServerLevel overworld=player.server.overworld();
        BlockPos origin=IslandSavedData.get(player.server).departure(player.getUUID()).orElse(overworld.getSharedSpawnPos());
        Vec3 destination=safeNear(overworld,origin).orElseGet(()->safeNear(overworld,overworld.getSharedSpawnPos()).orElseGet(()->fallback(overworld,overworld.getSharedSpawnPos())));
        BakunawaStomach.forget(player);
        transfer(player,overworld,destination);return true;
    }
    private static void transfer(ServerPlayer player,ServerLevel level,Vec3 pos) {
        player.stopRiding();player.fallDistance=0;player.setDeltaMovement(Vec3.ZERO);
        player.getPersistentData().putLong(COOLDOWN,player.server.overworld().getGameTime()+100);
        BakunawaStomach.transfer(player,level,pos);
        player.connection.resetPosition();
        player.fallDistance=0;player.setDeltaMovement(Vec3.ZERO);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,100,4,false,false));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,100,0,false,false));
    }
    public static boolean safe(ServerLevel level,BlockPos feet) {
        if(feet.getY()<=level.getMinBuildHeight()||feet.getY()>=level.getMaxBuildHeight()-2||!level.getWorldBorder().isWithinBounds(feet))return false;
        var floor=level.getBlockState(feet.below());
        return floor.isFaceSturdy(level,feet.below(),Direction.UP)&&!floor.is(Blocks.MAGMA_BLOCK)&&!floor.is(Blocks.CAMPFIRE)&&!floor.is(Blocks.SOUL_CAMPFIRE)&&!floor.is(Blocks.CACTUS)
            &&level.getBlockState(feet).isAir()&&level.getBlockState(feet.above()).isAir()&&level.getFluidState(feet.below()).isEmpty();
    }
    public static Optional<Vec3> safeNear(ServerLevel level,BlockPos origin) {
        // Prefer the departure's elevation: returning from an underground portal must not send you to a roof.
        for(int radius=0;radius<=16;radius++)for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++) {
            if(Math.max(Math.abs(x),Math.abs(z))!=radius)continue;
            for(int dy:new int[]{1,0,2,-1,3,-2,4,-3}) {
                BlockPos p=origin.offset(x,dy,z);
                if(safe(level,p)) return Optional.of(Vec3.atBottomCenterOf(p));
            }
        }
        return Optional.empty();
    }
    private static Vec3 fallback(ServerLevel level,BlockPos origin) {
        BlockPos bounded=level.getWorldBorder().clampToBounds(origin.getX(),origin.getY(),origin.getZ());
        int y=Math.min(level.getMaxBuildHeight()-4,Math.max(level.getMinBuildHeight()+4,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,bounded.getX(),bounded.getZ())+2));
        BlockPos base=new BlockPos(bounded.getX(),y,bounded.getZ());
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++) {
            level.setBlockAndUpdate(base.offset(x,-1,z),Blocks.STONE_BRICKS.defaultBlockState());
            for(int dy=0;dy<=2;dy++)level.setBlockAndUpdate(base.offset(x,dy,z),Blocks.AIR.defaultBlockState());
        }
        return Vec3.atBottomCenterOf(base);
    }
    private IslandTravel() {}
}
