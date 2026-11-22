package com.forgottenisland.adventure;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.entity.IslandCreature;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=ForgottenIsland.ID)
public final class AdventureEvents {
    private static final java.util.Map<ServerLevel,java.util.Set<net.minecraft.world.level.ChunkPos>> PENDING=new java.util.concurrent.ConcurrentHashMap<>();
    static boolean constructionPending(ServerLevel level,net.minecraft.world.level.ChunkPos pos) {
        var queue=PENDING.get(level);return queue!=null&&queue.contains(pos);
    }
    @SubscribeEvent public static void noExplodingBeds(PlayerInteractEvent.RightClickBlock e) {
        if(e.getLevel().dimension()!=AdventureContent.ISLAND&&e.getLevel().dimension()!=AdventureContent.HIGHLANDS)return;
        var block=e.getLevel().getBlockState(e.getPos()).getBlock();
        if(block instanceof net.minecraft.world.level.block.BedBlock||block instanceof net.minecraft.world.level.block.RespawnAnchorBlock) {
            e.setCanceled(true);e.setCancellationResult(InteractionResult.FAIL);
            if(!e.getLevel().isClientSide)e.getEntity().displayClientMessage(Component.literal("Rest at home. The island keeps your Overworld respawn and return link."),true);
        }
    }
    @SubscribeEvent public static void chunkLoad(ChunkEvent.Load e) {
        if(e.getLevel() instanceof ServerLevel level&&e.getChunk() instanceof LevelChunk chunk&&(level.dimension()==AdventureContent.ISLAND&&IslandWorld.legacyChunk(chunk.getPos())||level.dimension()==AdventureContent.HIGHLANDS&&IslandExpansion.settlementChunk(chunk.getPos())))
            PENDING.computeIfAbsent(level,k->java.util.concurrent.ConcurrentHashMap.newKeySet()).add(chunk.getPos());
    }
    @SubscribeEvent public static void chunkUnload(ChunkEvent.Unload event){
        if(event.getLevel() instanceof ServerLevel l){var pending=PENDING.get(l);if(pending!=null)pending.remove(event.getChunk().getPos());}
    }
    @SubscribeEvent public static void buildChunks(TickEvent.ServerTickEvent e) {
        if(e.phase!=TickEvent.Phase.END)return;
        IslandTravel.finishTick(e.getServer());
        StarNecklaceItem.tick(e.getServer());
        PENDING.forEach((level,queue)->{
            if(level.getServer()!=e.getServer())return;
            long deadline=System.nanoTime()+6_000_000;
            var positions=queue.iterator();
            for(int n=0;n<4&&positions.hasNext();n++) {
                if(n>0&&System.nanoTime()>deadline)break;
                var pos=positions.next();
                var data=IslandSavedData.get(e.getServer());
                if(level.dimension()==AdventureContent.HIGHLANDS?data.settled(pos.toLong()):data.chunkBuilt(pos.toLong())&&data.expanded(pos.toLong())){positions.remove();continue;}
                var chunk=level.getChunkSource().getChunkNow(pos.x,pos.z);
                if(chunk!=null){positions.remove();IslandWorld.initialize(level,chunk);}
                // Load fires before FULL promotion; retain work without forcing or blocking chunk loads.

            }
        });
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e) {
        PENDING.keySet().removeIf(level->level.getServer()==e.getServer());
        IslandTravel.clear(e.getServer());
        StarNecklaceItem.clear(e.getServer());
    }
    @SubscribeEvent public static void commands(RegisterCommandsEvent e) {
        for(boolean legacy:new boolean[]{true,false}){
            e.getDispatcher().register(Commands.literal("island").then(Commands.literal(legacy?"harbor":"market").executes(c->{
                var p=c.getSource().getPlayerOrException();
                if(p.level().dimension()!=AdventureContent.ISLAND&&p.level().dimension()!=AdventureContent.HIGHLANDS)return 0;
                var l=p.server.getLevel(legacy?AdventureContent.ISLAND:AdventureContent.HIGHLANDS);
                var at=legacy?IslandTravel.ARRIVAL:IslandTravel.MARKET_ENTRY;
                if(l==null)return 0;
                IslandWorld.initialize(l,l.getChunk(at.getX()>>4,at.getZ()>>4));
                var landing=IslandTravel.safeNear(l,at);
                if(landing.isEmpty())return 0;
                BakunawaStomach.transfer(p,l,landing.get());return 1;
            })));
        }
        e.getDispatcher().register(Commands.literal("island").then(Commands.literal("home").executes(c->{
            var p=c.getSource().getPlayerOrException();
            if(!IslandTravel.home(p))c.getSource().sendFailure(Component.literal("Emergency home travel is available only on Forgotten Island."));
            return 1;
        })));
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract e) {
        if(!(e.getTarget() instanceof IslandCreature c)||!c.getPersistentData().getBoolean("IslandMarketResident"))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(e.getLevel().isClientSide)return;
        String line=switch(c.species) {
            case RAWW->"Follow the eastern shore to the larger festival quarter! Its junction leads to the mountain, cave and coast. Both bosses are independent.";
            case CHEF->"Kain tayo! Enjoy the merienda stalls. Full bellies make brave adventurers.";
            case ZIPPY->"Watch the glowing ground. A warning gives you time to move!";
            case TIKBALANG->"Follow the parol lanterns. The fork leads to two separate challenges.";
            case SARANGAY->"Our market is a sanctuary. The bosses cannot hurt us here.";
            case BATIBA->"Visit my Memory Hollow beyond the eastern festival. There are glowing berries in a hidden alcove. Your star still remembers home.";
            case MERMAHN->"Welcome to my lagoon! Beyond the eastern shore the moon meets the sea.";
            case BUNGI->"Big wings! Bigger dragon! You can choose either adventure first.";
            case DATU->"Even if your portal is lost, the star finds safe ground near home.";
            case DUWENDE->"Lost your way? /island home always brings you safely out of the island.";
            default->"Maligayang pagdating! Explore the market, then follow either lantern trail.";
        };
        e.getEntity().displayClientMessage(Component.literal(c.species.displayName+": "+line),false);
        c.reactionTicks=40;
        c.level().broadcastEntityEvent(c,(byte)11);
        ((ServerLevel)e.getLevel()).sendParticles(ParticleTypes.HAPPY_VILLAGER,c.getX(),c.getY()+1,c.getZ(),5,.3,.3,.3,.01);
    }
    @SubscribeEvent public static void residentTick(LivingEvent.LivingTickEvent e) {
        if(!(e.getEntity() instanceof IslandCreature c)||c.level().isClientSide||!c.getPersistentData().getBoolean("IslandMarketResident"))return;
        BlockPos home=BlockPos.of(c.getPersistentData().getLong("IslandMarketHome"));
        c.setTarget(null);c.clearFire();
        if(c.tickCount%40==0&&c.distanceToSqr(Vec3.atBottomCenterOf(home))>16) {
            c.getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.7);
            if(c.distanceToSqr(Vec3.atBottomCenterOf(home))>144)c.teleportTo(home.getX()+.5,home.getY(),home.getZ()+.5);
        }
        if(c.tickCount%120==Math.floorMod(c.getId(),120))c.level().broadcastEntityEvent(c,(byte)11);
    }
    @SubscribeEvent public static void playerTick(TickEvent.PlayerTickEvent e) {
        if(e.phase==TickEvent.Phase.END&&e.player instanceof ServerPlayer p&&p.level().dimension()==AdventureContent.ISLAND&&p.getY()<0)IslandTravel.home(p);
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p)||(p.level().dimension()!=AdventureContent.ISLAND&&p.level().dimension()!=AdventureContent.HIGHLANDS)||p.tickCount%320!=0)return;
        int x=p.getBlockX(),z=p.getBlockZ();
        var sound=IslandLayout.cavern(x,z)?AdventureContent.CAVE_AMBIENCE.get():IslandLayout.mountain(x,z)<160?AdventureContent.MOUNTAIN_AMBIENCE.get():IslandLayout.coast(x,z)<150?AdventureContent.COAST_AMBIENCE.get():IslandLayout.market(x,z)||Math.abs(x)<38&&Math.abs(z)<60?AdventureContent.FESTIVAL.get():net.minecraft.sounds.SoundEvents.AMBIENT_CAVE.value();
        p.playNotifySound(sound,net.minecraft.sounds.SoundSource.AMBIENT,.45f,1);
    }
}
