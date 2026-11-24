package com.forgottenisland.adventure;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.arsenal.ArsenalContent;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgottenIsland.ID)
@PrefixGameTestTemplate(false)
public class HighlandGameTests {
    @GameTest(template="empty")
    public static void lunarRematchesSurviveReloadAndWaitForAPlayerStrike(GameTestHelper h) {
        var data = new IslandSavedData();
        var first = data.openMoonBattle(new BlockPos(0,65,0));
        var second = data.openMoonBattle(new BlockPos(LunarGenerator.BASIN_SPACING,65,0));
        var pilot = UUID.randomUUID();
        var state = data.encounter(first.id());
        state.start(400,List.of(pilot),UUID.randomUUID());
        h.assertTrue(!state.interruptMoonBite(),"Player interrupted the landing cinematic early");
        for (int t=0; t<MoonPursuit.BITE_TICKS; t++) state.tick(BossKind.BAKUNAWA.entranceLength,200);
        var restored = IslandSavedData.load(data.save(new CompoundTag()));
        var waiting = restored.encounter(first.id());
        h.assertTrue(restored.arena(first.id()).lunar() && restored.arena(second.id()).center().equals(second.center()),
            "Lunar arena identity or position was lost on reload");
        h.assertTrue(waiting.stage()==EncounterState.Stage.ENTRANCE && waiting.interruptMoonBite(),
            "Reloaded bite did not wait for its opening hit");
        h.assertTrue(waiting.stage()==EncounterState.Stage.COMBAT && !waiting.interruptMoonBite(),
            "Second hit restarted combat");
        restored.removeTest(first);
        h.assertTrue(restored.arena(second.id())!=null && restored.encounter(BossKind.BAKUNAWA.id).stage()==EncounterState.Stage.READY,
            "Completing a lunar encounter changed another arena");
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=100)
    public static void mountainArenasAndObstruction(GameTestHelper h){
        var l=h.getLevel().getServer().getLevel(AdventureContent.HIGHLANDS);
        for(var kind:BossKind.values()){
            String reason=BossEggItem.placement(l,kind,kind.center);
            h.assertTrue(reason==null,kind+" inaccessible: "+reason);
            var site=BossEncounters.rewardSite(l,kind);
            for(int x=(site.getX()-5)>>4;x<=(site.getX()+5)>>4;x++)for(int z=(site.getZ()-6)>>4;z<=(site.getZ()+6)>>4;z++)IslandWorld.initialize(l,l.getChunk(x,z));
            h.assertTrue(BossEncounters.clearDais(l,site),"No space for the "+kind+" reward on generated terrain");
        }
        var block=BossKind.BAKUNAWA.center.offset(0,-5,-30);var original=l.getBlockState(block);
        try{
            l.setBlockAndUpdate(block,Blocks.DIAMOND_BLOCK.defaultBlockState());
            h.assertTrue(BossEggItem.placement(l,BossKind.BAKUNAWA,BossKind.BAKUNAWA.center)!=null,"Obstructed water passed");
            h.assertTrue(l.getBlockState(block).is(Blocks.DIAMOND_BLOCK),"Placement damaged the submerged build");
        }finally{l.setBlockAndUpdate(block,original);}
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void bothFormsKeepSwordDifferencesAndMeleeReach(GameTestHelper h){
        var level=h.getLevel();var p=Tranche4GameTests.player(level,"Reach");
        var data=IslandSavedData.get(level.getServer());
        var at=h.absolutePos(new BlockPos(1,160,1));var a=data.addTest(BossKind.BAKUNAWA,level.dimension(),at,false);
        var boss=AdventureContent.BAKUNAWA_BOSS.get().create(level);boss.bind(a);boss.moveTo(Vec3.atBottomCenterOf(at));
        try{
            for(int form=1;form<=2;form++){
                float last=0;
                for(Item item:List.of(Items.IRON_SWORD,ArsenalContent.SAPPHIRE.get(),ArsenalContent.PHOENIX.get(),AdventureContent.CRYSTAL_SWORD.get())){
                    float max=form==1?400:500;
                    data.restoreEncounter(a.id(),new EncounterState.Snapshot(EncounterState.Stage.COMBAT,100,1,form,max,max,boss.getUUID(),Set.of(p.getUUID()),Set.of(),Set.of()));
                    boss.sync(data.encounter(a.id()));boss.tick();boss.invulnerableTime=0;
                    p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));for(int i=0;i<35;i++)p.tick();
                    var jaw=boss.getParts()[1];var box=jaw.getBoundingBox();p.setPos(box.maxX+1,at.getY(),box.getCenter().z);p.setOnGround(true);p.fallDistance=0;
                    h.assertTrue(box.distanceToSqr(p.getEyePosition())<9,"Perch puts the jaw outside weapon reach");p.attack(jaw);
                    float damage=max-data.encounter(a.id()).health();
                    h.assertTrue(damage>last&&damage<max/4,"Actual player attack damage with "+item+" in form "+form+": "+damage+" after "+last);
                    last=damage;
                }
            }
        }finally{boss.discard();data.removeTest(a);}
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void eggRejectsAndCreatesIndependentRoosts(GameTestHelper h){
        var l=h.getLevel();var data=IslandSavedData.get(l.getServer());var p=Tranche4GameTests.player(l,"Roost");p.setGameMode(GameType.CREATIVE);
        var ground=h.absolutePos(new BlockPos(0,175,0));for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++)l.setBlockAndUpdate(ground.offset(x,0,z),Blocks.STONE.defaultBlockState());
        for(var egg:AdventureContent.BOSS_EGGS){
            var stack=new ItemStack(egg.get());var item=(BossEggItem)egg.get();p.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var hit=new BlockHitResult(Vec3.atCenterOf(ground),Direction.UP,ground,false);var ctx=new UseOnContext(p,InteractionHand.MAIN_HAND,hit);
            var before=Set.copyOf(data.tests.keySet());var result=item.useOn(ctx);
            if(item.kind==BossKind.BAKUNAWA){h.assertTrue(result==InteractionResult.FAIL&&stack.getCount()==1&&data.tests.keySet().equals(before),"Water egg accepted dry land");}
            else{
                h.assertTrue(result.consumesAction(),"Manang ground egg rejected");
                var a=data.tests.values().stream().filter(v->!before.contains(v.id())).findFirst().orElseThrow();
                var state=data.encounter(a.id());var actor=(IslandBoss)l.getEntity(state.actor());
                h.assertTrue(state.stage()==EncounterState.Stage.ENTRANCE&&actor.center().equals(ground.above()),"Creative egg entrance missing");
                h.assertTrue(item.direct==(actor.separation()>0),"Wrong joined/separated entrance");
                var restored=IslandSavedData.load(data.save(new CompoundTag()));h.assertTrue(restored.arena(a.id()).equals(a)&&restored.encounter(a.id()).actor().equals(actor.getUUID()),"Egg identity lost");
                actor.discard();data.removeTest(a);
            }
        }
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void stormDischargesOnceAndMissesDodgingPlayer(GameTestHelper h){
        var l=h.getLevel();var p=Tranche4GameTests.player(l,"Storm");var q=Tranche4GameTests.player(l,"Dodge");
        var at=h.absolutePos(new BlockPos(1,165,1));var boss=AdventureContent.BAKUNAWA_BOSS.get().create(l);
        boss.bind(new Arena("probe",BossKind.BAKUNAWA,l.dimension(),at,1,true));boss.moveTo(Vec3.atBottomCenterOf(at));
        var tag=new CompoundTag();boss.saveWithoutId(tag);tag.putFloat("AimX",at.getX()+.5f);tag.putFloat("AimZ",at.getZ()+.5f);boss.readAdditionalSaveData(tag);
        for(int attack:new int[]{11,12}){
            var b=AdventureContent.BAKUNAWA_BOSS.get().create(l);b.readAdditionalSaveData(tag);
            b.sync(EncounterState.restore(new EncounterState.Snapshot(EncounterState.Stage.COMBAT,(attack-11)*185+60,1,2,500,500,b.getUUID(),Set.of(p.getUUID(),q.getUUID()),Set.of(),Set.of())));
            p.setPos(b.aim());q.setPos(b.aim().add(9,0,5));p.setHealth(20);p.invulnerableTime=0;
            b.bakunawaCombat(l,List.of(p,q),60,attack);p.invulnerableTime=0;b.bakunawaCombat(l,List.of(p,q),60,attack);
            h.assertTrue(p.getHealth()==(attack==11?11:12)&&q.getHealth()==20,"Lightning geometry or hit limit differs for "+attack);b.discard();
        }
        boss.discard();h.succeed();
    }
    @GameTest(template="empty")
    public static void migratedVictoryAndInteriorOwnershipSurviveReload(GameTestHelper h){
        var d=new IslandSavedData();var p=UUID.randomUUID();var s=d.encounter(BossKind.BAKUNAWA.id);
        s.start(400,List.of(p),UUID.randomUUID());s.tick(1,1);s.recordHealth(0);s.tick(1,1);s.delivered(p,"prior-reward");
        d.stomach.swallow(p,1);d.link(p,new BlockPos(31,81,17));
        var old=d.save(new CompoundTag());old.putInt("Version",2);var upgraded=IslandSavedData.load(old);
        h.assertTrue(upgraded.legacyVictories.contains(BossKind.BAKUNAWA.id)&&!upgraded.encounter(BossKind.BAKUNAWA.id).mayDeliver(p,"prior-reward")&&!upgraded.stomach.active(p,1),"Migration replayed victory or stranded pre-upgrade guest");
        var a=upgraded.addTest(BossKind.BAKUNAWA,Level.OVERWORLD,BlockPos.ZERO,false);var b=upgraded.addTest(BossKind.BAKUNAWA,Level.OVERWORLD,new BlockPos(400,64,0),true);
        var q=UUID.randomUUID();upgraded.interior(a.id()).swallow(p,1);upgraded.interior(b.id()).swallow(q,1);upgraded.interior(a.id()).breakCrystal(p,1,1);
        var copy=IslandSavedData.load(upgraded.save(new CompoundTag()));
        h.assertTrue(a.slot()!=b.slot()&&copy.interior(a.id()).crystals()==5&&copy.interior(b.id()).crystals()==7&&!copy.interior(a.id()).breakCrystal(q,2,1),"Two stomachs shared a room, member or crystal");
        h.assertTrue(copy.departure(p).orElseThrow().equals(new BlockPos(31,81,17)),"Migration changed personal return");
        h.succeed();
    }
}
