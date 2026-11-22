package com.forgottenisland.adventure;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.arsenal.*;
import com.forgottenisland.egg.*;
import com.forgottenisland.entity.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgottenIsland.ID)
@PrefixGameTestTemplate(false)
public final class Tranche4GameTests {
    private static ServerPlayer player(ServerLevel level,String name){
        level.getServer().setDifficulty(Difficulty.NORMAL,true);
        var p=new ServerPlayer(level.getServer(),level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),name));
        p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(level.getServer(),new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),p){
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet){}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){}
        };
        p.setGameMode(GameType.SURVIVAL);p.setNoGravity(true);p.setPos(0,180,0);for(int i=0;i<65;i++){p.tick();p.doTick();}p.invulnerableTime=0;return p;
    }
    private static EncounterState state(IslandBoss boss,int attack,int tick,UUID player){
        return EncounterState.restore(new EncounterState.Snapshot(EncounterState.Stage.COMBAT,attack*BossKind.BAKUNAWA.cycle(1)+tick,1,1,400,400,boss.getUUID(),Set.of(player),Set.of(),Set.of()));
    }
    private static IslandBoss boss(ServerLevel l,int attack,int tick,UUID player){
        var b=AdventureContent.BAKUNAWA_BOSS.get().create(l);var tag=new CompoundTag();b.saveWithoutId(tag);tag.putFloat("AimX",820.5f);tag.putFloat("AimZ",-64.5f);b.readAdditionalSaveData(tag);b.sync(state(b,attack,tick,player));b.setPos(BossKind.BAKUNAWA.actorOrigin());return b;
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void allThreeSwordsDealTheirActualStatedDamage(GameTestHelper h){
        var items=List.of(AdventureContent.CRYSTAL_SWORD.get(),ArsenalContent.SAPPHIRE.get(),ArsenalContent.PHOENIX.get());
        for(var item:items){
            var p=h.makeMockSurvivalPlayer();p.setNoGravity(true);p.setPos(h.absolutePos(new BlockPos(1,180,1)).getCenter());p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));
            for(int i=0;i<40;i++)p.tick();p.setOnGround(true);p.fallDistance=0;p.setSprinting(false);
            var target=EntityType.PIG.create(h.getLevel());target.setPos(p.position().add(0,0,1));target.setNoAi(true);h.getLevel().addFreshEntity(target);
            float[] observed={-1};java.util.function.Consumer<net.minecraftforge.event.entity.living.LivingDamageEvent> listener=e->{if(e.getEntity()==target)observed[0]=e.getAmount();};
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,false,net.minecraftforge.event.entity.living.LivingDamageEvent.class,listener);
            try{p.attack(target);}finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener);target.discard();}
            h.assertTrue(Math.abs(observed[0]-((ArsenalSwordItem)item).power.damage)<.01,"Wrong actual damage for "+item+": "+observed[0]);
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void crystalArmorHonorsStrengthAndHolyBeamBypass(GameTestHelper h){
        var l=h.getLevel();var p=player(l,"CrystalArmor");p.setPos(h.absolutePos(new BlockPos(1,185,1)).getCenter());
        var items=List.of(ArsenalContent.HELMET.get(),ArsenalContent.CHESTPLATE.get(),ArsenalContent.LEGGINGS.get(),ArsenalContent.BOOTS.get());
        var slots=List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET);
        for(int i=0;i<4;i++)p.setItemSlot(slots.get(i),new ItemStack(items.get(i)));p.tick();p.doTick();
        h.assertTrue(CrystalArmorProtection.uncapped(p.getAttribute(Attributes.ARMOR))==1319,"Armor strength is not 100 + 900 + 250 + 69");
        p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);p.setHealth(1000);p.invulnerableTime=0;
        var attacker=EntityType.ZOMBIE.create(l);p.hurt(p.damageSources().mobAttack(attacker),100);
        h.assertTrue(Math.abs(p.getHealth()-980)<.01,"Crystal armor did not apply exactly 80% maximum reduction: "+p.getHealth());
        var holder=l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,ForgottenIsland.id("holy_beam")));
        p.invulnerableTime=0;p.hurt(new net.minecraft.world.damagesource.DamageSource(holder),10);
        h.assertTrue(Math.abs(p.getHealth()-970)<.01,"Crystal armor incorrectly blocked Holy Beam");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void newAttacksDealNineAndFiveWithoutRepeatedContactDamage(GameTestHelper h){
        var l=h.getLevel().getServer().getLevel(AdventureContent.ISLAND);var p=player(l,"NewMoves");
        var slash=boss(l,10,52,p.getUUID());
        h.assertTrue(slash.mouth().distanceTo(slash.slashPoint(52))<1,"Slash damage begins before the head reaches its marked path");slash.discard();
        for(int attack:new int[]{0,3,6,9,10}){
            int t=attack==6?76:60;var b=boss(l,attack,t,p.getUUID());p.setPos(attack==9?b.tailTip(t):attack==10?b.slashPoint(t):b.aim());p.setHealth(20);p.invulnerableTime=0;
            b.bakunawaCombat(l,List.of(p),t,attack);float expected=attack>=9?15:11;
            h.assertTrue(p.getHealth()==expected,"Wrong damage for attack "+attack+": "+p.getHealth());
            p.invulnerableTime=0;b.bakunawaCombat(l,List.of(p),t,attack);
            h.assertTrue(p.getHealth()==expected,"Attack "+attack+" hit twice on repeated impact");b.discard();
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void atomicBreathPulsesOncePerSecondAndSurvivesReload(GameTestHelper h){
        var l=h.getLevel().getServer().getLevel(AdventureContent.ISLAND);var p=player(l,"Atomic");var b=boss(l,7,60,p.getUUID());p.setPos(b.aim());p.setHealth(20);
        b.bakunawaCombat(l,List.of(p),60,7);h.assertTrue(p.getHealth()==19,"First atomic pulse was not one HP: "+p.getHealth());
        var saved=new CompoundTag();b.saveWithoutId(saved);var loaded=boss(l,7,60,p.getUUID());loaded.readAdditionalSaveData(saved);
        p.invulnerableTime=0;loaded.bakunawaCombat(l,List.of(p),60,7);for(int t=61;t<80;t++)loaded.bakunawaCombat(l,List.of(p),t,7);
        h.assertTrue(p.getHealth()==19,"Atomic breath dealt frame damage or repeated a persisted pulse");
        loaded.sync(state(loaded,7,80,p.getUUID()));p.invulnerableTime=0;loaded.bakunawaCombat(l,List.of(p),80,7);
        h.assertTrue(p.getHealth()==18,"Second atomic pulse missing");b.discard();loaded.discard();h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void wardBlocksBodyHitsForExactlyFiveSeconds(GameTestHelper h){
        var l=h.getLevel().getServer().getLevel(AdventureContent.ISLAND);var p=player(l,"Ward");var b=boss(l,8,20,p.getUUID());p.setPos(b.weakPoint(0));
        var data=IslandSavedData.get(l.getServer());var previous=data.encounter(BossKind.BAKUNAWA.id).snapshot();
        try{
            data.restoreEncounter(BossKind.BAKUNAWA.id,state(b,8,20,p.getUUID()).snapshot());
            for(int t=20;t<120;t++){b.sync(state(b,8,t,p.getUUID()));h.assertTrue(b.warded()&&!b.hurtPart(p.damageSources().playerAttack(p),10,false),"Ward let body damage through at "+t);}
            b.sync(state(b,8,120,p.getUUID()));h.assertTrue(!b.warded()&&b.exposed(),"Ward exceeded five seconds");
            h.assertTrue(b.hurtPart(p.damageSources().playerAttack(p),10,false),"Body remained invincible after ward");
        }finally{data.restoreEncounter(BossKind.BAKUNAWA.id,previous);data.setDirty();b.discard();}h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=80)
    public static void swordAbilitiesIgnoreFriendsAndCannotBeSpammed(GameTestHelper h){
        var l=h.getLevel();var p=player(l,"Arsenal");p.setPos(h.absolutePos(new BlockPos(1,190,1)).getCenter());
        var enemy=EntityType.VINDICATOR.create(l);enemy.setPos(p.position().add(0,0,3));enemy.setNoAi(true);enemy.setNoGravity(true);enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);enemy.setHealth(500);l.addFreshEntity(enemy);
        var friend=ForgottenIsland.TYPES.get(Species.RAWW).get().create(l);friend.setPos(p.position().add(1,0,2));friend.befriend(p);friend.setNoAi(true);friend.setNoGravity(true);l.addFreshEntity(friend);
        // Entity additions are processed asynchronously by the server.
        h.runAfterDelay(3,()->{
            var sapphire=new ItemStack(ArsenalContent.SAPPHIRE.get());p.setItemInHand(InteractionHand.MAIN_HAND,sapphire);
            h.assertTrue(SwordAbilities.activate(p,sapphire)&&enemy.isCurrentlyGlowing()&&!friend.isCurrentlyGlowing(),"Reveal did not distinguish enemies from companions");
            h.assertTrue(!SwordAbilities.activate(p,sapphire),"Reveal can be spammed");
            var crystal=new ItemStack(AdventureContent.CRYSTAL_SWORD.get());p.setItemInHand(InteractionHand.MAIN_HAND,crystal);float health=friend.getHealth();
            h.assertTrue(SwordAbilities.activate(p,crystal)&&enemy.getHealth()<500&&enemy.hasEffect(net.minecraft.world.effect.MobEffects.POISON),"Crystal burst did not apply damage and poison");
            h.assertTrue(friend.getHealth()==health&&!friend.isOnFire(),"Crystal burst harmed a companion");
            h.assertTrue(!SwordAbilities.activate(p,crystal),"Crystal burst can be spammed");
            var persisted=new CompoundTag();p.saveWithoutId(persisted);h.assertTrue(persisted.toString().contains("IslandSwordReady_CRYSTAL"),"Ability cooldown was not persisted");
            var respawned=player(l,"RespawnedArsenal");
            SwordAbilities.clone(new net.minecraftforge.event.entity.player.PlayerEvent.Clone(respawned,p,true));
            h.assertTrue(!SwordAbilities.activate(respawned,crystal),"Death reset the sword cooldown");
            friend.discard();enemy.discard();h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void phoenixProjectileHitsEnemyWithoutChangingTerrain(GameTestHelper h){
        var l=h.getLevel();var p=player(l,"Phoenix");p.setPos(h.absolutePos(new BlockPos(1,205,1)).getCenter());p.setYRot(0);p.setXRot(0);
        var enemy=EntityType.ZOMBIE.create(l);enemy.setPos(p.position().add(0,0,6));enemy.setNoAi(true);enemy.setNoGravity(true);enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);enemy.setHealth(500);l.addFreshEntity(enemy);
        var wall=BlockPos.containing(p.position().add(0,1,9));l.setBlockAndUpdate(wall,Blocks.OAK_PLANKS.defaultBlockState());
        h.runAfterDelay(3,()->{var stack=new ItemStack(ArsenalContent.PHOENIX.get());p.setItemInHand(InteractionHand.MAIN_HAND,stack);h.assertTrue(SwordAbilities.activate(p,stack)&&!SwordAbilities.activate(p,stack),"Phoenix cooldown failed");});
        h.runAfterDelay(18,()->{h.assertTrue(enemy.getHealth()<400,"Fireball did not hit the target: "+enemy.getHealth());h.assertTrue(l.getBlockState(wall).is(Blocks.OAK_PLANKS),"Fireball damaged terrain");enemy.discard();h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void eggRetainsOwnerAndWaitsForSpaceThenHatchesOnlyOnce(GameTestHelper h){
        var l=h.getLevel();var pos=h.absolutePos(new BlockPos(1,175,1));var owner=UUID.randomUUID();
        l.setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(pos,EggContent.EGG.get().defaultBlockState());l.setBlockAndUpdate(pos.above(),Blocks.STONE.defaultBlockState());
        var egg=(BakunawaEggEntity)l.getBlockEntity(pos);egg.setOwner(owner);var saved=egg.saveWithoutMetadata();saved.putInt("Incubated",1199);egg.load(saved);
        h.runAfterDelay(25,()->{h.assertTrue(l.getBlockEntity(pos) instanceof BakunawaEggEntity,"Blocked egg hatched inside solid terrain");h.assertTrue(egg.owner().equals(owner)&&egg.progress()==1200,"Egg owner/progress lost on reload");l.setBlockAndUpdate(pos.above(),Blocks.AIR.defaultBlockState());});
        h.runAfterDelay(55,()->{
            var babies=l.getEntitiesOfClass(IslandCreature.class,new AABB(pos).inflate(5),c->c.species==Species.BAKUNAWA&&owner.equals(c.getOwnerUUID()));
            h.assertTrue(l.getBlockState(pos).isAir()&&babies.size()==1,"Egg failed to hatch once: "+babies.size());var baby=babies.get(0);
            h.assertTrue(baby.isBaby()&&baby.getScale()<.4&&baby.friendship()==3,"Hatchling is not a small bonded baby");
            var tag=new CompoundTag();baby.saveWithoutId(tag);var loaded=ForgottenIsland.TYPES.get(Species.BAKUNAWA).get().create(l);loaded.readAdditionalSaveData(tag);
            h.assertTrue(loaded.isBaby()&&owner.equals(loaded.getOwnerUUID())&&loaded.getAge()==baby.getAge(),"Hatchling growth or ownership lost across save");baby.discard();loaded.discard();h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void feedingGrowsHatchlingAndOldAdultRemainsAdult(GameTestHelper h){
        var l=h.getLevel();var p=player(l,"Grower");var baby=ForgottenIsland.TYPES.get(Species.BAKUNAWA).get().create(l);baby.hatch(p.getUUID());
        var snack=new ItemStack(Items.PRISMARINE_CRYSTALS,2);p.setItemInHand(InteractionHand.MAIN_HAND,snack);int age=baby.getAge();
        baby.mobInteract(p,InteractionHand.MAIN_HAND);h.assertTrue(baby.getAge()>age&&snack.getCount()==1,"Feeding did not spend one crystal and advance growth");
        baby.setAge(-1);baby.tick();h.assertTrue(!baby.isBaby(),"Growth never reaches adulthood");
        var adult=ForgottenIsland.TYPES.get(Species.BAKUNAWA).get().create(l);var tag=new CompoundTag();adult.saveWithoutId(tag);adult.readAdditionalSaveData(tag);
        h.assertTrue(!adult.isBaby()&&adult.getScale()==1,"Legacy adult was shrunk or reset");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void everyNewCraftingRecipeResolvesToRegisteredEquipment(GameTestHelper h){
        for(String id:List.of("prismatic_crystal","crystal_sword","sapphire_soul_sword","phoenix_sword","crystal_helmet","crystal_chestplate","crystal_leggings","crystal_boots","bakunawa_spawn_egg")){
            var recipe=h.getLevel().getRecipeManager().byKey(ForgottenIsland.id(id)).orElseThrow();var result=recipe.getResultItem(h.getLevel().registryAccess());
            h.assertTrue(!result.isEmpty()&&net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(result.getItem()).equals(ForgottenIsland.id(id)),"Broken recipe result: "+id);
            h.assertTrue(recipe.getIngredients().stream().filter(i->!i.isEmpty()).allMatch(i->i.getItems().length>0),"Unobtainable/missing ingredient: "+id);
        }h.succeed();
    }
}
