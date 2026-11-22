package com.forgottenisland.adventure;

import com.forgottenisland.ForgottenIsland;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgottenIsland.ID)
@PrefixGameTestTemplate(false)
public final class TrancheGameTests {
    private static ServerPlayer player(net.minecraft.server.level.ServerLevel l,String name){
        l.getServer().setDifficulty(Difficulty.NORMAL,true);
        var p=new ServerPlayer(l.getServer(),l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),name));
        p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(l.getServer(),new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),p){
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet){}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){}
        };
        p.setGameMode(GameType.SURVIVAL);p.setNoGravity(true);p.setPos(0,150,0);
        // Exercise real ServerPlayer damage, after vanilla's login invulnerability expires.
        for(int i=0;i<65;i++){p.tick();p.doTick();}p.invulnerableTime=0;return p;
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void necklaceFormationIsDelayedAndAcceptsUnevenGround(GameTestHelper h){
        var l=h.getLevel();var p=player(l,"Necklace");var floor=h.absolutePos(new BlockPos(0,160,0));
        for(var c:StarNecklaceItem.CELLS){
            var pos=floor.offset(c.x(),0,c.z());l.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
            for(int y=1;y<=4;y++)l.setBlockAndUpdate(pos.above(y),Blocks.AIR.defaultBlockState());
        }
        l.setBlockAndUpdate(floor.offset(2,1,-1),Blocks.DIRT.defaultBlockState());
        p.setPos(floor.getCenter().add(0,1,0));var stack=new ItemStack(AdventureContent.STAR_NECKLACE.get());p.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var c=new net.minecraft.world.item.context.UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(floor.getCenter(),Direction.UP,floor,false));
        h.assertTrue(stack.useOn(c).consumesAction(),"Necklace rejected a one-block slope");
        h.assertTrue(l.getBlockState(floor.above(2)).isAir()&&stack.getDamageValue()==0,"Formation wrote/charged before animation");
        h.onEachTick(()->StarNecklaceItem.tick(l.getServer()));
        h.runAfterDelay(45,()->{
            for(var cell:StarNecklaceItem.CELLS)h.assertTrue(l.getBlockState(floor.above(2).offset(cell.x(),0,cell.z())).is(AdventureContent.STAR_PORTAL.get()),"Necklace left an incomplete star");
            h.assertTrue(stack.getDamageValue()==1,"Formation did not consume one charge");
            h.assertTrue(l.getBlockState(floor.offset(2,1,-1)).is(Blocks.DIRT),"Slope was excavated");
            var restored=IslandSavedData.load(IslandSavedData.get(l.getServer()).save(new CompoundTag()));
            for(var cell:StarNecklaceItem.CELLS)h.assertTrue(restored.portalAt(floor.above(2).offset(cell.x(),0,cell.z())).orElseThrow().equals(floor.above(2)),"New portal cells lost linkage on restart");
            h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void necklaceRechecksNewObstructionAndCostsNothing(GameTestHelper h){
        var l=h.getLevel();var p=player(l,"BlockedStar");var floor=h.absolutePos(new BlockPos(0,175,0));
        for(var cell:StarNecklaceItem.CELLS){var pos=floor.offset(cell.x(),0,cell.z());l.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());for(int y=1;y<=4;y++)l.setBlockAndUpdate(pos.above(y),Blocks.AIR.defaultBlockState());}
        p.setPos(floor.getCenter().add(0,1,0));var stack=new ItemStack(AdventureContent.STAR_NECKLACE.get());p.setItemInHand(InteractionHand.MAIN_HAND,stack);
        stack.useOn(new net.minecraft.world.item.context.UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(floor.getCenter(),Direction.UP,floor,false)));
        var obstruction=floor.offset(0,2,-1);l.setBlockAndUpdate(obstruction,Blocks.CHEST.defaultBlockState());
        h.onEachTick(()->StarNecklaceItem.tick(l.getServer()));
        h.runAfterDelay(45,()->{
            h.assertTrue(stack.getDamageValue()==0&&l.getBlockState(floor.above()).isAir(),"Interrupted formation charged or partially wrote");
            h.assertTrue(l.getBlockState(obstruction).is(Blocks.CHEST),"Formation destroyed a player build");h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void expansionPreservesBuiltColumnsAndUpgradeJournal(GameTestHelper h){
        var l=h.getLevel().getServer().getLevel(AdventureContent.ISLAND);var chunk=l.getChunk(17,5);var data=IslandSavedData.get(l.getServer());
        // Simulate an old, explored ocean chunk and a build both above and below sea level.
        for(int x=272;x<288;x++)for(int z=80;z<96;z++)for(int y=0;y<256;y++)l.setBlock(new BlockPos(x,y,z),(y==0?Blocks.BEDROCK:y<56?Blocks.STONE:y<63?Blocks.WATER:Blocks.AIR).defaultBlockState(),2|16);
        var high=new BlockPos(279,120,88);var low=new BlockPos(279,58,88);l.setBlockAndUpdate(high,Blocks.DIAMOND_BLOCK.defaultBlockState());l.setBlockAndUpdate(low,Blocks.CHEST.defaultBlockState());
        data.markChunkBuilt(chunk.getPos().toLong());IslandExpansion.initialize(l,chunk);
        h.assertTrue(l.getBlockState(high).is(Blocks.DIAMOND_BLOCK)&&l.getBlockState(low).is(Blocks.CHEST),"Upgrade overwrote a player column");
        h.assertTrue(l.getBlockState(new BlockPos(278,64,88)).isSolid(),"Previously explored baseline ocean was not upgraded");
        var saved=data.save(new CompoundTag());var reload=IslandSavedData.load(saved);
        h.assertTrue(reload.chunkBuilt(chunk.getPos().toLong())&&reload.expanded(chunk.getPos().toLong()),"Versioned construction markers were not persisted");
        h.assertTrue(saved.getCompound("Encounters").equals(reload.save(new CompoundTag()).getCompound("Encounters")),"Upgrade changed encounter/reward history");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void holyBeamKillsArmoredNormalPlayerInTwoSeparateHits(GameTestHelper h){
        var l=h.getLevel();var p=player(l,"BeamArmor");p.setPos(h.absolutePos(new BlockPos(0,150,0)).getCenter());
        var armor=List.of(Items.NETHERITE_BOOTS,Items.NETHERITE_LEGGINGS,Items.NETHERITE_CHESTPLATE,Items.NETHERITE_HELMET);
        var slots=List.of(EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);
        for(int i=0;i<4;i++){var stack=new ItemStack(armor.get(i));stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION,4);p.setItemSlot(slots.get(i),stack);}p.tick();p.doTick();p.setHealth(20);
        h.assertTrue(p.getArmorValue()==20,"Test armor attributes are not applied");
        var holder=l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,ForgottenIsland.id("holy_beam")));
        var source=new net.minecraft.world.damagesource.DamageSource(holder);p.invulnerableTime=0;
        p.hurt(source,BakunawaMoves.BEAM_DAMAGE);h.assertTrue(p.getHealth()==10,"First beam was mitigated by ordinary armor/enchantments: "+p.getHealth());
        p.invulnerableTime=0;p.hurt(source,BakunawaMoves.BEAM_DAMAGE);h.assertTrue(p.isDeadOrDying(),"Two unblocked beams did not kill a full-health armored player");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void tentaclesDealSevenOncePerWave(GameTestHelper h){
        var l=h.getLevel().getServer().getLevel(AdventureContent.ISLAND);var p=player(l,"Tentacle");var b=AdventureContent.BAKUNAWA_BOSS.get().create(l);
        var tag=new CompoundTag();b.saveWithoutId(tag);tag.putFloat("AimX",820.5f);tag.putFloat("AimZ",-64.5f);b.readAdditionalSaveData(tag);
        p.setPos(b.tentaclePoint(0));p.setHealth(20);
        b.bakunawaCombat(l,List.of(p),60,1);h.assertTrue(p.getHealth()==13,"Tentacle did not deal seven damage: "+p.getHealth());
        p.invulnerableTime=0;b.bakunawaCombat(l,List.of(p),60,1);b.bakunawaCombat(l,List.of(p),61,1);
        h.assertTrue(p.getHealth()==13,"Same tentacle wave repeatedly hurt the player");b.discard();h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void holyBeamDischargeCannotHitTwiceEvenAfterActorReload(GameTestHelper h){
        var l=h.getLevel().getServer().getLevel(AdventureContent.ISLAND);var p=player(l,"OneBeam");var b=AdventureContent.BAKUNAWA_BOSS.get().create(l);
        var s=new EncounterState();s.start(400,List.of(p.getUUID()),UUID.randomUUID());s.tick(1,1);for(int i=0;i<500;i++)s.tick(1,1);b.sync(s);b.moveTo(BossKind.BAKUNAWA.origin());
        var tag=new CompoundTag();b.saveWithoutId(tag);tag.putFloat("AimX",820.5f);tag.putFloat("AimZ",-64.5f);b.readAdditionalSaveData(tag);
        p.setPos(new Vec3(820.5,65,-64.5));p.setHealth(20);p.invulnerableTime=0;
        b.bakunawaCombat(l,List.of(p),60,2);h.assertTrue(p.getHealth()==10,"Actual beam discharge did not hit the aim line: "+p.getHealth());
        p.invulnerableTime=0;for(int i=61;i<72;i++)b.bakunawaCombat(l,List.of(p),i,2);
        var saved=new CompoundTag();b.saveWithoutId(saved);var loaded=AdventureContent.BAKUNAWA_BOSS.get().create(l);loaded.readAdditionalSaveData(saved);loaded.sync(s);loaded.moveTo(BossKind.BAKUNAWA.origin());
        loaded.bakunawaCombat(l,List.of(p),60,2);h.assertTrue(p.getHealth()==10,"One beam registered two hits across its visual frames/reload");b.discard();loaded.discard();h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void necklaceDownwardTravelKeepsPersonalReturnAndCooldown(GameTestHelper h){
        var l=h.getLevel();var p=player(l,"StarTravel");var other=player(l,"OtherLink");var center=h.absolutePos(new BlockPos(2,205,2));
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)l.setBlockAndUpdate(center.offset(dx,-1,dz),Blocks.STONE.defaultBlockState());
        var data=IslandSavedData.get(l.getServer());data.registerNecklacePortal(center);var otherHome=center.offset(200,0,0);data.link(other.getUUID(),otherHome);
        l.setBlockAndUpdate(center,AdventureContent.STAR_PORTAL.get().defaultBlockState());p.setPos(center.getCenter().add(0,-.3,0));p.setDeltaMovement(0,-.1,0);
        AdventureContent.STAR_PORTAL.get().entityInside(l.getBlockState(center),l,center,p);IslandTravel.finishTick(l.getServer());
        h.assertTrue(p.level().dimension()==AdventureContent.ISLAND,"Downward portal contact did not travel");
        h.assertTrue(data.departure(p.getUUID()).orElseThrow().equals(center)&&data.departure(other.getUUID()).orElseThrow().equals(otherHome),"Travel mixed personal departures");
        IslandTravel.through(p,IslandTravel.RETURN_STAR);h.assertTrue(p.level().dimension()==AdventureContent.ISLAND,"Travel cooldown was ignored");
        IslandTravel.home(p);h.assertTrue(p.level().dimension()==Level.OVERWORLD&&p.position().distanceTo(center.getCenter())<20,"Emergency return lost necklace destination");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=200)
    public static void stomachTransfersPreservesMembershipAndEjectsAfterThreeCrystals(GameTestHelper h){
        var l=h.getLevel().getServer().getLevel(AdventureContent.ISLAND);var data=IslandSavedData.get(l.getServer());var prior=data.encounter(BossKind.BAKUNAWA.id).snapshot();
        var oldStomach=data.stomach.guests();int oldAttempt=data.stomach.attempt(),oldCrystals=data.stomach.crystals();
        var p=player(l,"Stomach");var state=new EncounterState();state.start(400,List.of(p.getUUID()),UUID.randomUUID());state.tick(1,1);data.restoreEncounter(BossKind.BAKUNAWA.id,state.snapshot());
        data.stomach.restore(-1,0,Map.of());var b=AdventureContent.BAKUNAWA_BOSS.get().create(l);b.sync(state);
        try{
            BakunawaStomach.swallow(b,p);
            h.assertTrue(p.level().dimension()==AdventureContent.STOMACH&&BakunawaStomach.participating(p,state.attempt()),"Swallowed player was lost as encounter participant");
            var reload=IslandSavedData.load(data.save(new CompoundTag()));h.assertTrue(reload.stomach.active(p.getUUID(),state.attempt()),"Restart lost stomach membership");
            for(int i=0;i<3;i++){
                var pos=BakunawaStomach.CRYSTALS.get(i);h.assertTrue(p.serverLevel().getBlockState(pos).is(AdventureContent.STOMACH_CRYSTAL.get()),"Objective crystal missing");
                h.assertTrue(IslandTravel.safe(p.serverLevel(),pos.offset(1,0,0)),"Crystal route is not walkable");
                h.assertTrue(BakunawaStomach.crystal(p,pos),"Crystal rejected an encounter participant");
                if(i<2)h.assertTrue(data.stomach.active(p.getUUID(),state.attempt()),"Escaped before the last crystal");
            }
            BakunawaStomach.tick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END,p));
            h.assertTrue(p.level().dimension()==AdventureContent.ISLAND&&!data.stomach.contains(p.getUUID()),"Last crystal did not eject safely");
            h.assertTrue(data.encounter(BossKind.BAKUNAWA.id).participants().contains(p.getUUID()),"Escape lost reward eligibility");h.succeed();
        }finally{b.discard();data.restoreEncounter(BossKind.BAKUNAWA.id,prior);data.stomach.restore(oldAttempt,oldCrystals,oldStomach);data.setDirty();}
    }
}
