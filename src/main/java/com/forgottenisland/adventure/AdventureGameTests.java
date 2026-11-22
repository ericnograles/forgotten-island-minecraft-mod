package com.forgottenisland.adventure;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.entity.IslandCreature;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.*;
import java.util.UUID;

@GameTestHolder(ForgottenIsland.ID)
@PrefixGameTestTemplate(false)
public final class AdventureGameTests {
    @GameTest(template="empty",timeoutTicks=200)
    public static void chunkConstructionWaitsForFullAvailability(GameTestHelper h) {
        var level=h.getLevel().getServer().getLevel(AdventureContent.ISLAND);
        var data=IslandSavedData.get(level.getServer());
        int x=1000;
        while(data.chunkBuilt(net.minecraft.world.level.ChunkPos.asLong(x,1000)))x++;
        var pos=new net.minecraft.world.level.ChunkPos(x,1000);
        h.assertTrue(level.getChunkSource().getChunkNow(pos.x,pos.z)==null,"Fixture chunk is already available");
        // Forge may publish Load before its LevelChunk becomes visible in the FULL cache.
        AdventureEvents.chunkLoad(new net.minecraftforge.event.level.ChunkEvent.Load(
            new net.minecraft.world.level.chunk.LevelChunk(level,pos),true));
        // The GameTest runner does not dispatch the dedicated server's END event; exercise that handler explicitly.
        h.onEachTick(()->AdventureEvents.buildChunks(new net.minecraftforge.event.TickEvent.ServerTickEvent(
            net.minecraftforge.event.TickEvent.Phase.END,()->true,level.getServer())));
        h.startSequence().thenIdle(10).thenExecute(()->{
            h.assertTrue(!data.chunkBuilt(pos.toLong()),"Unavailable chunk was built or forced to load");
            h.assertTrue(AdventureEvents.constructionPending(level,pos),"Load event was discarded before FULL availability");
            level.getChunk(pos.x,pos.z);
        }).thenWaitUntil(()->h.assertTrue(data.chunkBuilt(pos.toLong()),"Deferred chunk construction was lost"))
          .thenExecute(h::succeed);
    }
    @GameTest(template="empty",timeoutTicks=80)
    public static void starConstructionConsumesExactCostAndRejectsObstructionsAtomically(GameTestHelper h) {
        var level=h.getLevel();var p=h.makeMockSurvivalPlayer();var center=h.absolutePos(new BlockPos(3,190,3));
        p.setPos(center.offset(10,0,0).getCenter());p.setShiftKeyDown(true);
        for(int x=-4;x<=4;x++)for(int z=-5;z<=4;z++) {
            level.setBlockAndUpdate(center.offset(x,-1,z),Blocks.STONE.defaultBlockState());
            for(int y=0;y<=2;y++)level.setBlockAndUpdate(center.offset(x,y,z),Blocks.AIR.defaultBlockState());
        }
        var stack=new ItemStack(AdventureContent.PAROL_ITEM.get(),32);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var hit=new BlockHitResult(Vec3.atBottomCenterOf(center),Direction.UP,center.below(),false);
        var context=new net.minecraft.world.item.context.UseOnContext(p,InteractionHand.MAIN_HAND,hit);
        var obstruction=center.offset(0,1,-1);level.setBlockAndUpdate(obstruction,Blocks.DIAMOND_BLOCK.defaultBlockState());
        h.assertTrue(stack.useOn(context)==InteractionResult.FAIL&&stack.getCount()==32,"Failed blueprint consumed material");
        h.assertTrue(level.getBlockState(center).isAir()&&level.getBlockState(obstruction).is(Blocks.DIAMOND_BLOCK),"Failed blueprint partially wrote or destroyed blocks");
        level.setBlockAndUpdate(obstruction,Blocks.AIR.defaultBlockState());
        h.assertTrue(stack.useOn(context).consumesAction()&&stack.getCount()==4,"Blueprint did not consume exactly 28 stones");
        for(var cell:StarPattern.FRAME)h.assertTrue(level.getBlockState(center.offset(cell.x(),0,cell.z())).is(AdventureContent.PAROL_STONE.get()),"Frame differs from manual pattern");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GLOW_INK_SAC));
        var frame=AdventureContent.PAROL_STONE.get();
        h.assertTrue(frame.use(frame.defaultBlockState(),level,center,p,InteractionHand.MAIN_HAND,hit).consumesAction(),"Constructed star cannot activate");
        for(var cell:StarPattern.OPENING)h.assertTrue(level.getBlockState(center.offset(cell.x(),0,cell.z())).is(AdventureContent.STAR_PORTAL.get()),"Constructed opening incomplete");
        for(int x=-4;x<=4;x++)for(int z=-5;z<=4;z++)for(int y=-1;y<=2;y++)level.setBlockAndUpdate(center.offset(x,y,z),Blocks.AIR.defaultBlockState());
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void multipartHitsRouteToJournalAndLethalDamageStartsScene(GameTestHelper h) {
        var level=h.getLevel().getServer().getLevel(AdventureContent.ISLAND);var kind=BossKind.BAKUNAWA;
        var data=IslandSavedData.get(level.getServer());var prior=data.encounter(kind.id).snapshot();
        var player=net.minecraftforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"AnchorTest"));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.setPos(Vec3.atBottomCenterOf(kind.center).add(0,0,41));
        var state=new EncounterState();state.start(kind.baseHealth,java.util.List.of(player.getUUID()),UUID.randomUUID());state.tick(1,1);
        for(int i=0;i<85;i++)state.tick(1,1);data.restoreEncounter(kind.id,state.snapshot());state=data.encounter(kind.id);
        var boss=AdventureContent.BAKUNAWA_BOSS.get().create(level);boss.setUUID(state.actor());boss.moveTo(kind.origin());boss.sync(state);
        level.getChunk(kind.center);level.addFreshEntity(boss);boss.tick();
        try {
            var part=boss.getParts()[11];
            h.assertTrue(level.getEntityOrPart(part.getId())==part,"Attack packet cannot resolve the multipart target id");
            h.assertTrue(part.getBoundingBox().distanceToSqr(player.getEyePosition())<9,"Ground anchor cannot be reached with ordinary melee");
            h.assertTrue(part.hurt(level.damageSources().playerAttack(player),6),"Anchor rejected valid Survival damage");
            h.assertTrue(state.health()==kind.baseHealth-9,"Multipart damage did not update authoritative health");
            part.hurt(level.damageSources().playerAttack(player),10000);
            h.assertTrue(state.stage()==EncounterState.Stage.DEFEAT&&!boss.isRemoved()&&boss.getHealth()>0,"Lethal damage removed actor before its scene");
            h.assertTrue(!part.hurt(level.damageSources().playerAttack(player),10000)&&state.snapshot().delivered().isEmpty(),"Concurrent lethal hit duplicated transition or delivered early loot");
            h.succeed();
        } finally {boss.discard();data.restoreEncounter(kind.id,prior);data.setDirty();}
    }
    @GameTest(template="empty",timeoutTicks=200)
    public static void realRewardDropsWaitForSceneAndSurviveRepeatedCompletion(GameTestHelper h) {
        var level=h.getLevel().getServer().getLevel(AdventureContent.ISLAND);var kind=BossKind.BAKUNAWA;
        var data=new IslandSavedData();var state=data.encounter(kind.id);
        UUID first=UUID.randomUUID(),second=UUID.randomUUID();
        state.start(kind.baseHealth,java.util.List.of(first,second),UUID.randomUUID());state.tick(1,1);state.recordHealth(0);
        var altar=kind.altar();
        for(int x=(altar.getX()>>4)-1;x<=(altar.getX()>>4)+1;x++)for(int z=(altar.getZ()>>4)-1;z<=(altar.getZ()>>4)+1;z++)level.setChunkForced(x,z,true);
        h.startSequence().thenWaitUntil(()->{
            for(int x=(altar.getX()-5)>>4;x<=(altar.getX()+5)>>4;x++)for(int z=(altar.getZ()-6)>>4;z<=(altar.getZ()+6)>>4;z++)
                h.assertTrue(level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)),"Waiting for reward entity chunks");
        }).thenExecute(()->{
            BossEncounters.finish(level,kind,state,data);
            h.assertTrue(state.snapshot().delivered().isEmpty(),"Reward delivered before the defeat scene ended");
            state.tick(1,1);BossEncounters.finish(level,kind,state,data);
            h.assertTrue(state.snapshot().delivered().size()==4,"Two participants did not receive both personal rewards");
            var ids=state.snapshot().delivered().stream().map(s->UUID.nameUUIDFromBytes(s.getBytes(java.nio.charset.StandardCharsets.UTF_8))).toList();
            for(UUID id:ids) {
                h.assertTrue(level.getEntity(id) instanceof net.minecraft.world.entity.item.ItemEntity,"Persistent reward entity missing");
                var drop=(net.minecraft.world.entity.item.ItemEntity)level.getEntity(id);
                h.assertTrue(drop.isInvulnerable()&&drop.getAge()==-32768,"Reward may burn or expire before collection");
            }
            var reloaded=IslandSavedData.load(data.save(new CompoundTag()));
            var marker=altar.offset(4,-1,4);level.setBlockAndUpdate(marker,Blocks.EMERALD_BLOCK.defaultBlockState());
            BossEncounters.finish(level,kind,reloaded.encounter(kind.id),reloaded);
            h.assertTrue(reloaded.encounter(kind.id).snapshot().delivered().size()==4,"Repeated completion changed reward ownership");
            h.assertTrue(level.getBlockState(marker).is(Blocks.EMERALD_BLOCK),"Completion overwrote a later player edit");
            h.assertTrue(level.getBlockState(altar.below()).is(AdventureContent.STAR_PORTAL.get()),"Persistent boss exit not created");
            for(UUID id:ids){h.assertTrue(level.getEntity(id)!=null,"Repeated completion removed a reward");level.getEntity(id).discard();}
            for(int x=(altar.getX()>>4)-1;x<=(altar.getX()>>4)+1;x++)for(int z=(altar.getZ()>>4)-1;z<=(altar.getZ()>>4)+1;z++)level.setChunkForced(x,z,false);h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void crystalSwordAppliesSixThousandActualServerDamage(GameTestHelper h) {
        var player=h.makeMockSurvivalPlayer();
        player.setPos(h.absolutePos(new BlockPos(3,185,3)).getCenter());
        player.setNoGravity(true);
        var sword=new ItemStack(AdventureContent.CRYSTAL_SWORD.get());
        player.setItemInHand(InteractionHand.MAIN_HAND,sword);
        for(int i=0;i<40;i++)player.tick();
        player.setOnGround(true);player.fallDistance=0;player.setSprinting(false);
        h.assertTrue(player.getAttackStrengthScale(.5f)==1,"Fixture sword is not fully charged");
        double actualAttribute=player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        h.assertTrue(player.isAlive()&&actualAttribute==2048,"Fixture must exercise vanilla's actual attack attribute cap; value="+actualAttribute+", alive="+player.isAlive()+", held="+player.getMainHandItem());
        var target=net.minecraft.world.entity.EntityType.PIG.create(h.getLevel());
        target.setPos(player.position().add(0,0,1));target.setNoAi(true);h.getLevel().addFreshEntity(target);
        var observed=new float[]{-1};
        java.util.function.Consumer<net.minecraftforge.event.entity.living.LivingDamageEvent> listener=e->{if(e.getEntity()==target)observed[0]=e.getAmount();};
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,false,net.minecraftforge.event.entity.living.LivingDamageEvent.class,listener);
        try {player.attack(target);} finally {net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener);}
        h.assertTrue(Math.abs(observed[0]-6000)<.001,"Actual server LivingDamageEvent was "+observed[0]+", expected 6000");
        h.assertTrue(target.isDeadOrDying(),"Actual melee damage was not applied to the target");
        target.discard();h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void completionJournalSurvivesNbtAndRejectsDuplicateRewards(GameTestHelper h) {
        var data=new IslandSavedData();var state=data.encounter("manang");UUID player=UUID.randomUUID();
        state.start(200,java.util.List.of(player),UUID.randomUUID());state.tick(1,1);state.recordHealth(0);
        var resumed=IslandSavedData.load(data.save(new CompoundTag()));var loaded=resumed.encounter("manang");
        h.assertTrue(loaded.stage()==EncounterState.Stage.DEFEAT,"Lethal transition not durable before reward");
        loaded.tick(1,1);String token=loaded.rewardToken("manang",player,"wings");
        h.assertTrue(loaded.delivered(player,token),"Legitimate victory reward rejected");
        var finalLoad=IslandSavedData.load(resumed.save(new CompoundTag())).encounter("manang");
        h.assertTrue(finalLoad.stage()==EncounterState.Stage.COMPLETE&&!finalLoad.delivered(player,token),"Reload duplicated victory reward");
        h.assertTrue(resumed.encounter("bakunawa").stage()==EncounterState.Stage.READY,"Manang completion changed independent Bakunawa state");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void wingsUseRealElytraDurabilityAndRepairRules(GameTestHelper h) {
        var player=h.makeMockSurvivalPlayer();var wings=new ItemStack(AdventureContent.MANANG_WINGS.get());
        h.assertTrue(wings.canElytraFly(player),"Fresh wings cannot glide");
        h.assertTrue(net.minecraft.world.entity.Mob.getEquipmentSlotForItem(wings)==net.minecraft.world.entity.EquipmentSlot.CHEST,"Wings do not equip in chest slot");
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,wings);
        wings.elytraFlightTick(player,19);
        h.assertTrue(wings.getDamageValue()==1,"Flight did not consume one durability after 20 ticks");
        wings.setDamageValue(wings.getMaxDamage()-1);
        h.assertTrue(!wings.canElytraFly(player),"Broken wings still fly");
        h.assertTrue(wings.getItem().isValidRepairItem(wings,new ItemStack(Items.PHANTOM_MEMBRANE)),"Membrane repair not supported");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=80)
    public static void portalLinksPersistIndependently(GameTestHelper h) {
        var data=new IslandSavedData();UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        BlockPos first=new BlockPos(20,70,-30),second=new BlockPos(-400,12,600);
        data.registerPortal(first);data.registerPortal(second);data.link(a,first);data.link(b,second);data.markChunkBuilt(91);
        var reloaded=IslandSavedData.load(data.save(new CompoundTag()));
        h.assertTrue(reloaded.departure(a).orElseThrow().equals(first),"First player lost home");
        h.assertTrue(reloaded.departure(b).orElseThrow().equals(second),"Second player inherited another home");
        for(var cell:StarPattern.OPENING)h.assertTrue(reloaded.portalAt(second.offset(cell.x(),0,cell.z())).orElseThrow().equals(second),"Aperture cell lost its center");
        reloaded.link(a,second);
        h.assertTrue(data.departure(a).orElseThrow().equals(first),"Reload aliases previous state");
        h.assertTrue(reloaded.chunkBuilt(91),"Generated chunk marker was lost");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=80)
    public static void portalActivationIsAtomicAndSurvivalObtainable(GameTestHelper h) {
        var level=h.getLevel();var p=h.makeMockSurvivalPlayer();var center=h.absolutePos(new BlockPos(3,180,3));
        var frame=AdventureContent.PAROL_STONE.get();
        for(var cell:StarPattern.FRAME)level.setBlockAndUpdate(center.offset(cell.x(),0,cell.z()),frame.defaultBlockState());
        for(var cell:StarPattern.OPENING)for(int dy=0;dy<=2;dy++)level.setBlockAndUpdate(center.offset(cell.x(),dy,cell.z()),Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(center,frame.defaultBlockState());
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GLOW_INK_SAC,2));
        var obstruction=center.offset(0,0,-1);level.setBlockAndUpdate(obstruction,Blocks.DIAMOND_BLOCK.defaultBlockState());
        var hit=new BlockHitResult(center.getCenter(),Direction.UP,center,false);
        var result=frame.use(frame.defaultBlockState(),level,center,p,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(result==InteractionResult.FAIL&&p.getMainHandItem().getCount()==2,"Invalid opening consumed activation material");
        h.assertTrue(level.getBlockState(obstruction).is(Blocks.DIAMOND_BLOCK),"Activation destroyed an obstruction");
        level.setBlockAndUpdate(obstruction,Blocks.AIR.defaultBlockState());
        frame.use(frame.defaultBlockState(),level,center,p,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(p.getMainHandItem().getCount()==1,"Survival activation did not consume exactly one glow ink");
        for(var cell:StarPattern.OPENING)h.assertTrue(level.getBlockState(center.offset(cell.x(),0,cell.z())).is(AdventureContent.STAR_PORTAL.get()),"Incomplete aperture");
        h.assertTrue(IslandSavedData.get(level.getServer()).portalAt(center).orElseThrow().equals(center),"Activated portal not linked");
        for(var cell:StarPattern.OPENING)level.setBlockAndUpdate(center.offset(cell.x(),0,cell.z()),Blocks.AIR.defaultBlockState());
        for(var cell:StarPattern.FRAME)level.setBlockAndUpdate(center.offset(cell.x(),0,cell.z()),Blocks.AIR.defaultBlockState());
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void arrivalInitializationPreservesPlayerChangesAndPopulation(GameTestHelper h) {
        var island=h.getLevel().getServer().getLevel(AdventureContent.ISLAND);
        h.assertTrue(island!=null,"Data-driven dimension missing");
        IslandWorld.prepareArrival(island);
        for(int x=-2;x<=1;x++)for(int z=-2;z<=3;z++)island.setChunkForced(x,z,true);
        var marker=new BlockPos(4,64,40);island.setBlockAndUpdate(marker,Blocks.EMERALD_BLOCK.defaultBlockState());
        var bounds=new AABB(-36,60,-36,36,90,64);
        // Entity-region reads are asynchronous on an existing world. Wait for their load before comparing.
        h.runAfterDelay(20,()->{
            var before=island.getEntitiesOfClass(IslandCreature.class,bounds,IslandCreature::isMarketResident);
            h.assertTrue(before.size()==21,"Expected 21 saved market residents after entity load, got "+before.size());
            h.assertTrue(before.stream().map(c->c.species).distinct().count()==11,"Market lacks its eleven non-boss types");
            var ids=before.stream().map(c->c.getUUID()).collect(java.util.stream.Collectors.toSet());
            IslandWorld.prepareArrival(island);
            h.runAfterDelay(20,()->{
                var after=island.getEntitiesOfClass(IslandCreature.class,bounds,IslandCreature::isMarketResident);
                h.assertTrue(ids.equals(after.stream().map(c->c.getUUID()).collect(java.util.stream.Collectors.toSet())),"Initialization changed or duplicated residents");
                h.assertTrue(island.getBlockState(marker).is(Blocks.EMERALD_BLOCK),"Initialization overwrote a player edit");
                h.assertTrue(island.getBlockState(IslandTravel.RETURN_STAR).is(AdventureContent.STAR_PORTAL.get()),"Retreat portal missing");
                h.assertTrue(IslandTravel.safeNear(island,IslandTravel.ARRIVAL).isPresent(),"No safe arrival");
                for(int x=-2;x<=1;x++)for(int z=-2;z<=3;z++)island.setChunkForced(x,z,false);
                h.succeed();
            });
        });
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void safeReturnRejectsBlockedAndDangerousGround(GameTestHelper h) {
        var level=h.getLevel();var feet=h.absolutePos(new BlockPos(3,185,3));
        level.setBlockAndUpdate(feet.below(),Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(feet,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(feet.above(),Blocks.AIR.defaultBlockState());
        h.assertTrue(IslandTravel.safe(level,feet),"Clear solid return rejected");
        level.setBlockAndUpdate(feet.above(),Blocks.STONE.defaultBlockState());
        h.assertTrue(!IslandTravel.safe(level,feet),"Head obstruction accepted");
        level.setBlockAndUpdate(feet.above(),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(feet.below(),Blocks.MAGMA_BLOCK.defaultBlockState());
        h.assertTrue(!IslandTravel.safe(level,feet),"Burning floor accepted");
        level.setBlockAndUpdate(feet.below(),Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(feet,AdventureContent.STAR_PORTAL.get().defaultBlockState());
        h.assertTrue(!IslandTravel.safe(level,feet),"Return would immediately enter a portal");h.succeed();
    }
}
