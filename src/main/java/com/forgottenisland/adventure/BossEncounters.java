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
    private static final TicketType<String> TICKET=TicketType.create("forgottenisland_encounter",String::compareTo,40);
    private static final Map<ServerLevel,Map<String,Runtime>> RUNTIME=new HashMap<>();
    private static final class Runtime {
        final ServerBossEvent meter;
        int emptyTicks;
        long nextPlacement;
        String obstruction;
        Runtime(BossKind kind){meter=new ServerBossEvent(Component.literal(kind.title),kind==BossKind.MANANG?BossEvent.BossBarColor.PURPLE:BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);}
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){RUNTIME.keySet().removeIf(l->l.getServer()==e.getServer());}
    static boolean overlaps(IslandSavedData data,ServerLevel level,BossKind kind,BlockPos center){
        var arenas=new ArrayList<>(data.tests.values());
        for(var k:BossKind.values())arenas.add(Arena.island(k));
        return arenas.stream().anyMatch(a->a.dimension()==level.dimension()&&Math.hypot(a.center().getX()-center.getX(),a.center().getZ()-center.getZ())<a.kind().radius+kind.radius+8);
    }
    static void start(ServerLevel level,Arena a,IslandSavedData data,List<ServerPlayer> guests){
        var state=data.encounter(a.id());
        if(!state.start(a.kind().baseHealth,guests.stream().map(ServerPlayer::getUUID).toList(),UUID.randomUUID()))return;
        if(a.direct())state.directForm(a.kind()==BossKind.BAKUNAWA);
        level.getChunk(a.center());
        spawn(level,a,state);
        data.setDirty();
    }
    private static IslandBoss spawn(ServerLevel level,Arena a,EncounterState state){
        var actor=(a.kind()==BossKind.MANANG?AdventureContent.MANANG_BOSS:AdventureContent.BAKUNAWA_BOSS).get().create(level);
        if(actor==null)return null;
        actor.setUUID(state.actor());actor.bind(a);actor.moveTo(Vec3.atBottomCenterOf(a.center()));actor.sync(state);
        return level.addFreshEntity(actor)?actor:null;
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e) {
        if(e.phase!=TickEvent.Phase.END)return;
        var server=e.getServer();var data=IslandSavedData.get(server);
        var arenas=new ArrayList<>(data.tests.values());
        for(var k:BossKind.values()){
            var a=Arena.island(k);
            arenas.add(data.legacyVictories.contains(k.id)?new Arena(k.id,k,AdventureContent.ISLAND,k.center,0,false):a);
        }
        for(var a:arenas){var l=server.getLevel(a.dimension());if(l!=null)update(l,a,data);}
    }
    static void update(ServerLevel level,Arena a,IslandSavedData data){
        var kind=a.kind();var state=data.encounter(a.id());
        var runtime=RUNTIME.computeIfAbsent(level,k->new HashMap<>());
        var r=runtime.computeIfAbsent(a.id(),k->new Runtime(kind));
        var players=level.getServer().getPlayerList().getPlayers().stream().filter(p->p.isAlive()&&!p.isSpectator()&&
            (p.level()==level&&a.contains(p.position())&&!(p.getVehicle() instanceof MoonRocket)||kind==BossKind.BAKUNAWA&&BakunawaStomach.participating(p,a.id(),state.attempt()))).toList();
        boolean approach=level.players().stream().anyMatch(p->!p.isSpectator()&&p.distanceToSqr(Vec3.atCenterOf(a.center()))<Math.pow(kind.radius+36,2));
        boolean active=state.stage()!=EncounterState.Stage.READY&&state.stage()!=EncounterState.Stage.COMPLETE;
        var chunk=new ChunkPos(a.center());
        if(level.getServer().getPlayerList().getPlayers().isEmpty()){
            level.getChunkSource().removeRegionTicket(TICKET,chunk,2,a.id());r.meter.removeAllPlayers();return;
        }
        if(active||approach&&state.stage()==EncounterState.Stage.READY)level.getChunkSource().addRegionTicket(TICKET,chunk,2,a.id());
        if(!a.lunar()&&state.stage()==EncounterState.Stage.READY&&approach&&(level.getGameTime()>=r.nextPlacement||!players.isEmpty()&&r.emptyTicks==0)){
            r.obstruction=BossEggItem.placement(level,kind,a.center());
            r.nextPlacement=level.getGameTime()+40;
        }
        if(state.stage()==EncounterState.Stage.READY&&!players.isEmpty()){
            if(r.obstruction!=null){if(level.getGameTime()%40==0||r.emptyTicks==0)for(var p:players)p.displayClientMessage(Component.literal(r.obstruction),false);r.emptyTicks=1;return;}
            start(level,a,data,players);
        }
        if(state.stage()==EncounterState.Stage.READY)return;
        if(state.stage()!=EncounterState.Stage.COMPLETE){
            if(players.isEmpty())r.emptyTicks++;else r.emptyTicks=0;
            if(state.stage()!=EncounterState.Stage.DEFEAT&&r.emptyTicks>=200){
                var actor=level.getEntity(state.actor());if(actor!=null)actor.discard();
                if(kind==BossKind.BAKUNAWA)data.interior(a.id()).release();
                state.retry();r.meter.removeAllPlayers();level.getChunkSource().removeRegionTicket(TICKET,chunk,2,a.id());
                if(a.testing() || a.lunar()){data.removeTest(a);runtime.remove(a.id());}data.setDirty();return;
            }
            for(var p:players)if(!state.participants().contains(p.getUUID())){state.join(p.getUUID());data.setDirty();}
            if(level.areEntitiesLoaded(chunk.toLong())){
                var actor=level.getEntity(state.actor()) instanceof IslandBoss boss?boss:spawn(level,a,state);
                if(actor!=null){
                    boolean fighting=players.stream().anyMatch(p->!p.isCreative());
                    boolean awaitingStrike = a.lunar() && state.stage() == EncounterState.Stage.ENTRANCE && state.ticks() >= MoonPursuit.BITE_TICKS;
                    if (!awaitingStrike && (state.stage()!=EncounterState.Stage.COMBAT||fighting)) {
                        state.tick(kind.entranceLength,kind.defeatLength);data.setDirty();
                    }
                    actor.sync(state);
                }
            }
        }
        if(state.stage()==EncounterState.Stage.COMPLETE){
            if(kind==BossKind.BAKUNAWA&&data.interior(a.id()).crystals()!=0){data.interior(a.id()).release();data.setDirty();}
            var actor=state.actor()==null?null:level.getEntity(state.actor());if(actor!=null)actor.discard();
            if(a.testing()){data.removeTest(a);runtime.remove(a.id());}
            else if (a.lunar()) {
                for (var p : players) p.sendSystemMessage(Component.literal("The Moon is free. Use /island home to return."));
                data.removeTest(a);
                runtime.remove(a.id());
            }
            else if(!players.isEmpty()||active)finish(level,kind,state,data);
            level.getChunkSource().removeRegionTicket(TICKET,chunk,2,a.id());r.meter.removeAllPlayers();return;
        }
        var ids=new HashSet<UUID>();for(var p:players){ids.add(p.getUUID());r.meter.addPlayer(p);}
        for(var p:List.copyOf(r.meter.getPlayers()))if(!ids.contains(p.getUUID()))r.meter.removePlayer(p);
        r.meter.setProgress(state.health()/state.maxHealth());
        String label=switch(state.stage()){
            case ENTRANCE -> a.lunar() ? (state.ticks() >= MoonPursuit.BITE_TICKS ? "STRIKE THE JAW" : "THE MOON IN ITS JAWS") : "AWAKENING"; case TRANSFORM -> "METAMORPHOSIS"; case DEFEAT -> "RELEASE";
            default -> state.phase()==1?"I · WATCH THE WARNINGS":kind==BossKind.MANANG?"II · SEVERED FURY":"II · LIGHTNING";
        };
        r.meter.setName(Component.literal(kind.title+"  |  "+label));
    }
    static void finish(ServerLevel level,BossKind kind,EncounterState state,IslandSavedData data) {
        if(state.stage()!=EncounterState.Stage.COMPLETE)return;
        BlockPos altar=data.rewardDais(kind);
        if(!data.exitBuilt(kind.id)){
            BlockPos base=rewardSite(level,kind);
            BlockPos open=null;
            for(int rise=0;rise<=3&&open==null;rise++)for(int dx:new int[]{0,-14,14}){
                var candidate=base.offset(dx,rise,0);
                for(int x=(candidate.getX()-5)>>4;x<=(candidate.getX()+5)>>4;x++)for(int z=(candidate.getZ()-6)>>4;z<=(candidate.getZ()+6)>>4;z++)IslandWorld.initialize(level,level.getChunk(x,z));
                if(clearDais(level,candidate)){open=candidate;break;}
            }
            if(open==null)return; // A fully obstructed dais waits; it never excavates a player's build.
            altar=open;data.rewardDais(kind,altar);
        }
        for(int x=(altar.getX()-5)>>4;x<=(altar.getX()+5)>>4;x++)for(int z=(altar.getZ()-6)>>4;z<=(altar.getZ()+6)>>4;z++)
            IslandWorld.initialize(level,level.getChunk(x,z));
        for(int x=(altar.getX()-5)>>4;x<=(altar.getX()+5)>>4;x++)for(int z=(altar.getZ()-6)>>4;z<=(altar.getZ()+6)>>4;z++)
            if(!level.areEntitiesLoaded(ChunkPos.asLong(x,z)))return;
        if(!data.exitBuilt(kind.id)) {
            // Only validated air above an untouched surface; never clear player blocks.
            for(int x=-5;x<=5;x++)for(int z=-6;z<=6;z++) {
                BlockPos p=altar.offset(x,-1,z);level.setBlockAndUpdate(p,Blocks.SMOOTH_QUARTZ.defaultBlockState());
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
    static BlockPos rewardSite(ServerLevel level,BossKind kind){
        if(level.dimension()!=AdventureContent.HIGHLANDS||kind!=BossKind.BAKUNAWA)return kind.altar();
        var site=kind.altar().south(1);
        return new BlockPos(site.getX(),MountainTerrain.height(site.getX(),site.getZ())+2,site.getZ());
    }
    static boolean clearDais(ServerLevel level,BlockPos altar){
        for(int x=-5;x<=5;x++)for(int z=-6;z<=6;z++){
            if(level.dimension()==AdventureContent.ISLAND&&IslandSavedData.get(level.getServer()).protectedColumn(altar.getX()+x,altar.getZ()+z))return false;
            var floor=level.getBlockState(altar.offset(x,-1,z));
            if(!floor.isAir()&&!floor.is(Blocks.SMOOTH_QUARTZ)&&!floor.is(Blocks.DEEPSLATE_TILES)&&!floor.is(Blocks.PURPLE_TERRACOTTA)&&!floor.is(Blocks.POLISHED_ANDESITE))return false;
            for(int y=0;y<=3;y++)if(!level.getBlockState(altar.offset(x,y,z)).isAir())return false;
        }
        return true;
    }
    @SubscribeEvent public static void protectScene(LivingAttackEvent e) {
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var data=IslandSavedData.get(p.server);
        var arenas=new ArrayList<>(data.tests.values());for(var k:BossKind.values())arenas.add(Arena.island(k));
        for(var a:arenas)if(p.level().dimension()==a.dimension()&&a.contains(p.position())&&data.encounter(a.id()).cinematic()){e.setCanceled(true);return;}
    }
    @SubscribeEvent public static void pickup(PlayerEvent.ItemPickupEvent e) {
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var tag=e.getOriginalEntity().getPersistentData();String token=tag.getString("IslandReward");if(token.isEmpty())return;
        var data=IslandSavedData.get(p.server);if(data.encounter(tag.getString("IslandBoss")).collect(token))data.setDirty();
    }
}
