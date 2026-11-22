package com.forgottenisland.adventure;

import com.forgottenisland.ForgottenIsland;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid=ForgottenIsland.ID)
public final class BossEncounters {
    private static final TicketType<Integer> TICKET=TicketType.create("forgottenisland_encounter",Integer::compare,40);
    private static final Map<ServerLevel,EnumMap<BossKind,Runtime>> RUNTIME=new HashMap<>();
    private static final class Runtime {
        final ServerBossEvent meter;
        int emptyTicks,loadTicks;
        Runtime(BossKind kind){meter=new ServerBossEvent(Component.literal(kind.title),kind==BossKind.MANANG?BossEvent.BossBarColor.PURPLE:BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);}
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){RUNTIME.keySet().removeIf(l->l.getServer()==e.getServer());}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e) {
        if(e.phase!=TickEvent.Phase.END)return;
        var level=e.getServer().getLevel(AdventureContent.ISLAND);if(level==null)return;
        var data=IslandSavedData.get(e.getServer());
        var runtime=RUNTIME.computeIfAbsent(level,k->new EnumMap<>(BossKind.class));
        for(var kind:BossKind.values()) {
            var r=runtime.computeIfAbsent(kind,Runtime::new);var state=data.encounter(kind.id);
            var players=level.players().stream().filter(p->p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&kind.inside(p.position())).toList();
            r.emptyTicks=players.isEmpty()?r.emptyTicks+1:0;
            boolean active=state.stage()!=EncounterState.Stage.READY&&state.stage()!=EncounterState.Stage.COMPLETE;
            if(!players.isEmpty()||active) {
                // Ephemeral tickets expire after retreat. No permanent chunk forcing survives a restart.
                level.getChunkSource().addRegionTicket(TICKET,new ChunkPos(kind.center),4,kind.ordinal());
                level.getChunk(kind.center);r.loadTicks++;
            } else r.loadTicks=0;
            if(state.stage()==EncounterState.Stage.READY&&!players.isEmpty()&&r.loadTicks>=40) {
                state.start(kind.baseHealth,players.stream().map(ServerPlayer::getUUID).toList(),UUID.randomUUID());data.setDirty();
            }
            if(state.stage()==EncounterState.Stage.COMBAT||state.stage()==EncounterState.Stage.ENTRANCE) {
                for(var p:players)if(!state.participants().contains(p.getUUID())){state.join(p.getUUID());data.setDirty();}
                if(r.emptyTicks>=200) {
                    var actor=state.actor()==null?null:level.getEntity(state.actor());if(actor!=null)actor.discard();
                    state.retry();data.setDirty();r.meter.removeAllPlayers();continue;
                }
            }
            active=state.stage()!=EncounterState.Stage.READY&&state.stage()!=EncounterState.Stage.COMPLETE;
            if(active&&r.loadTicks>=40&&level.areEntitiesLoaded(new ChunkPos(BlockPos.containing(kind.origin())).toLong())) {
                IslandBoss actor=state.actor()!=null&&level.getEntity(state.actor()) instanceof IslandBoss boss?boss:null;
                if(actor==null) {
                    actor=(kind==BossKind.MANANG?AdventureContent.MANANG_BOSS:AdventureContent.BAKUNAWA_BOSS).get().create(level);
                    if(actor!=null){actor.setUUID(state.actor());actor.moveTo(kind.origin().x,kind.origin().y,kind.origin().z,0,0);actor.sync(state);level.addFreshEntity(actor);}
                }
                state.tick(kind.entranceLength,kind.defeatLength);data.setDirty();
                if(actor!=null)actor.sync(state);
            }
            if(state.stage()==EncounterState.Stage.COMPLETE&&(!players.isEmpty()||r.loadTicks>0)) {
                finish(level,kind,state,data);
                var actor=state.actor()==null?null:level.getEntity(state.actor());if(actor!=null)actor.discard();
            }
            if(state.stage()==EncounterState.Stage.READY||state.stage()==EncounterState.Stage.COMPLETE)r.meter.removeAllPlayers();
            else {
                var ids=new HashSet<UUID>();for(var p:players){ids.add(p.getUUID());r.meter.addPlayer(p);}
                for(var p:List.copyOf(r.meter.getPlayers()))if(!ids.contains(p.getUUID()))r.meter.removePlayer(p);
                r.meter.setProgress(state.maxHealth()>0?state.health()/state.maxHealth():0);
                String phase=state.cinematic()?(state.stage()==EncounterState.Stage.ENTRANCE?"AWAKENING":"RELEASE"):(state.phase()==1?"I · WATCH THE WARNINGS":"II · "+(kind==BossKind.MANANG?"SEVERED FURY":"ECLIPSE"));
                r.meter.setName(Component.literal(kind.title+"  |  "+phase));
            }
        }
    }
    static void finish(ServerLevel level,BossKind kind,EncounterState state,IslandSavedData data) {
        if(state.stage()!=EncounterState.Stage.COMPLETE)return;
        BlockPos altar=kind.altar();
        for(int x=(altar.getX()-5)>>4;x<=(altar.getX()+5)>>4;x++)for(int z=(altar.getZ()-6)>>4;z<=(altar.getZ()+6)>>4;z++)
            IslandWorld.initialize(level,level.getChunk(x,z));
        for(int x=(altar.getX()-5)>>4;x<=(altar.getX()+5)>>4;x++)for(int z=(altar.getZ()-6)>>4;z<=(altar.getZ()+6)>>4;z++)
            if(!level.areEntitiesLoaded(ChunkPos.asLong(x,z)))return;
        if(!data.exitBuilt(kind.id)) {
            // Reserved reward dais. The once-only marker protects all later player edits.
            for(int x=-5;x<=5;x++)for(int z=-6;z<=6;z++) {
                BlockPos p=altar.offset(x,-1,z);level.setBlockAndUpdate(p,Blocks.SMOOTH_QUARTZ.defaultBlockState());
                for(int y=1;y<=3;y++)level.setBlockAndUpdate(p.above(y),Blocks.AIR.defaultBlockState());
            }
            if(kind==BossKind.BAKUNAWA) {
                BlockPos star=altar.offset(0,-1,0);
                for(var c:StarPattern.FRAME)level.setBlockAndUpdate(star.offset(c.x(),0,c.z()),AdventureContent.PAROL_STONE.get().defaultBlockState());
                for(var c:StarPattern.OPENING)level.setBlockAndUpdate(star.offset(c.x(),0,c.z()),AdventureContent.STAR_PORTAL.get().defaultBlockState());
            }
            data.markExitBuilt(kind.id);
        }
        List<Item> items=kind==BossKind.MANANG?List.of(AdventureContent.MANANG_WINGS.get()):List.of(AdventureContent.CRYSTAL_SWORD.get(),AdventureContent.BAKUNAWA_SKULL.get());
        int index=0;
        for(UUID player:state.participants())for(Item item:items) {
            String token=state.rewardToken(kind.id,player,item.toString());
            if(!state.mayDeliver(player,token))continue;
            UUID id=UUID.nameUUIDFromBytes(token.getBytes(StandardCharsets.UTF_8));
            if(level.getEntity(id)==null) {
                var stack=item.getDefaultInstance();stack.getOrCreateTag().putString("IslandReward",token);
                var drop=new ItemEntity(level,altar.getX()+.5+(index%3-1)*1.3,altar.getY()+.5,altar.getZ()+5.5,stack,0,0,0);
                drop.setUUID(id);drop.setTarget(player);drop.setInvulnerable(true);drop.setUnlimitedLifetime();drop.setPickUpDelay(20);
                drop.getPersistentData().putString("IslandReward",token);drop.getPersistentData().putString("IslandBoss",kind.id);
                if(!level.addFreshEntity(drop))continue;
            }
            state.delivered(player,token);data.setDirty();index++;
            var online=level.getServer().getPlayerList().getPlayer(player);
            if(online!=null)online.sendSystemMessage(Component.literal(kind.title.split(" · ")[0]+" defeated. Your reward waits on the south dais; only you can collect it. You can share it afterward."));
        }
    }
    @SubscribeEvent public static void protectScene(LivingAttackEvent e) {
        if(!(e.getEntity() instanceof ServerPlayer p)||p.level().dimension()!=AdventureContent.ISLAND)return;
        for(var kind:BossKind.values())if(kind.inside(p.position())&&IslandSavedData.get(p.server).encounter(kind.id).cinematic()){e.setCanceled(true);return;}
    }
    @SubscribeEvent public static void pickup(PlayerEvent.ItemPickupEvent e) {
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var tag=e.getOriginalEntity().getPersistentData();String token=tag.getString("IslandReward");if(token.isEmpty())return;
        var data=IslandSavedData.get(p.server);if(data.encounter(tag.getString("IslandBoss")).collect(token))data.setDirty();
    }
}
