package com.forgottenisland;

import com.forgottenisland.entity.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;

@GameTestHolder(ForgottenIsland.ID)
@PrefixGameTestTemplate(false)
public final class IslandGameTests {
    @GameTest(template="empty",timeoutTicks=240)
    public static void chefWardRespectsAlliesAndExpires(GameTestHelper helper) {
        Player owner=helper.makeMockPlayer();
        Player stranger=helper.makeMockPlayer();stranger.setUUID(java.util.UUID.randomUUID());
        var pos=helper.absolutePos(new net.minecraft.core.BlockPos(3,200,3));
        owner.setPos(pos.getCenter());
        IslandCreature chef=ForgottenIsland.TYPES.get(Species.CHEF).get().create(helper.getLevel());
        IslandCreature ally=ForgottenIsland.TYPES.get(Species.RAWW).get().create(helper.getLevel());
        IslandCreature outsider=ForgottenIsland.TYPES.get(Species.RAWW).get().create(helper.getLevel());
        for(IslandCreature c:new IslandCreature[]{chef,ally,outsider}) {
            c.setPos(pos.getCenter());c.setNoAi(true);c.setNoGravity(true);helper.getLevel().addFreshEntity(c);
        }
        chef.befriend(owner);ally.befriend(owner);outsider.befriend(stranger);
        chef.setOrderedToSit(true);ally.setOrderedToSit(true);outsider.setOrderedToSit(true);
        var wool=net.minecraft.world.level.block.Blocks.WHITE_WOOL.defaultBlockState();
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)helper.getLevel().setBlockAndUpdate(pos.offset(x,-1,z),wool);
        helper.assertTrue(chef.fireImmune(),"Flaming chef is not fire immune");
        float health=chef.getHealth();chef.hurt(chef.damageSources().inFire(),4);
        helper.assertTrue(chef.getHealth()==health,"Chef's own element damages it");
        ally.setSecondsOnFire(8);outsider.setSecondsOnFire(8);
        helper.assertTrue(chef.requestAbility(owner,false),"Targetless supper rejected");
        var fireResistance=net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE;
        helper.assertTrue(!ally.isOnFire()&&ally.hasEffect(fireResistance),"Supper did not extinguish and protect an allied companion");
        helper.assertTrue(ally.getEffect(fireResistance).getDuration()==200,"Fire ward has the wrong duration");
        helper.assertTrue(outsider.isOnFire()&&!outsider.hasEffect(fireResistance),"Supper affected another owner's companion");
        outsider.clearFire();
        helper.assertTrue(!chef.requestAbility(owner,false),"Supper bypassed its cooldown");
        helper.runAfterDelay(205,()->{
            helper.assertTrue(!ally.hasEffect(fireResistance)&&chef.activeTicks()==0,"Supper failed to expire");
            for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++) {
                helper.assertTrue(helper.getLevel().getBlockState(pos.offset(x,-1,z)).equals(wool),"Supper changed flammable terrain");
                helper.assertTrue(helper.getLevel().getBlockState(pos.offset(x,0,z)).isAir(),"Supper placed fire or blocks");
            }
            chef.discard();ally.discard();outsider.discard();helper.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=80)
    public static void aquaticStayHoldsDepth(GameTestHelper helper) {
        for(int x=0;x<8;x++)for(int z=0;z<8;z++)for(int y=0;y<5;y++)
            helper.setBlock(x,y,z,(x==0||z==0||x==7||z==7||y==0)?net.minecraft.world.level.block.Blocks.GLASS:net.minecraft.world.level.block.Blocks.WATER);
        Player owner=helper.makeMockPlayer();
        IslandCreature serpent=ForgottenIsland.TYPES.get(Species.BAKUNAWA).get().create(helper.getLevel());
        IslandCreature mermahn=ForgottenIsland.TYPES.get(Species.MERMAHN).get().create(helper.getLevel());
        serpent.setPos(helper.absolutePos(new net.minecraft.core.BlockPos(3,1,3)).getCenter());
        mermahn.setPos(helper.absolutePos(new net.minecraft.core.BlockPos(5,1,5)).getCenter());
        for(IslandCreature c:new IslandCreature[]{serpent,mermahn}){c.befriend(owner);c.setOrderedToSit(true);helper.getLevel().addFreshEntity(c);}
        double initialY=serpent.getY();
        helper.runAfterDelay(40,()->{
            for(IslandCreature c:new IslandCreature[]{serpent,mermahn}) {
                helper.assertTrue(c.isInWater(),"Aquatic fixture not submerged: "+c.species.id);
                helper.assertTrue(Math.abs(c.getY()-initialY)<.2,"Staying swimmer drifted to the surface: "+c.species.id);
                helper.assertTrue(c.getAirSupply()==c.getMaxAirSupply(),"Aquatic companion lost air");c.discard();
            }
            helper.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void elevatedRidersCannotEnterCeilings(GameTestHelper helper) {
        Player owner=helper.makeMockPlayer();
        for(Species s:Species.values()) if(s.mount) {
            IslandCreature c=ForgottenIsland.TYPES.get(s).get().create(helper.getLevel());
            c.setPos(helper.absolutePos(new net.minecraft.core.BlockPos(3,200,3)).getCenter());
            helper.getLevel().addFreshEntity(c);c.befriend(owner);
            owner.setPos(c.position());
            helper.assertTrue(owner.startRiding(c),"Open saddle rejected: "+s.id);
            double oldY=c.getY();
            int ceilingY=(int)Math.ceil(oldY+c.getPassengersRidingOffset()+owner.getMyRidingOffset()+owner.getBbHeight());
            var base=net.minecraft.core.BlockPos.containing(c.getX(),ceilingY,c.getZ());
            for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)helper.getLevel().setBlockAndUpdate(base.offset(x,0,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            c.move(net.minecraft.world.entity.MoverType.SELF,new net.minecraft.world.phys.Vec3(0,1,0));
            helper.assertTrue(c.getY()==oldY,"Rider entered low ceiling: "+s.id);
            owner.stopRiding();
            c.setPos(c.getX(),oldY+1,c.getZ());
            helper.assertTrue(!owner.startRiding(c),"Obstructed saddle accepted: "+s.id);
            for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)helper.getLevel().setBlockAndUpdate(base.offset(x,0,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            c.discard();
        }
        helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void partialGiftsAndFairyRemedy(GameTestHelper helper) {
        Player owner=helper.makeMockPlayer();
        owner.setPos(helper.absolutePos(new net.minecraft.core.BlockPos(3,2,3)).getCenter());
        IslandCreature fairy=ForgottenIsland.TYPES.get(Species.ZIPPY).get().create(helper.getLevel());
        fairy.setPos(owner.position());helper.getLevel().addFreshEntity(fairy);
        owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Species.ZIPPY.gift,3));
        fairy.mobInteract(owner,InteractionHand.MAIN_HAND);
        CompoundTag saved=new CompoundTag();fairy.saveWithoutId(saved);fairy.discard();
        IslandCreature loaded=ForgottenIsland.TYPES.get(Species.ZIPPY).get().create(helper.getLevel());loaded.load(saved);helper.getLevel().addFreshEntity(loaded);
        loaded.mobInteract(owner,InteractionHand.MAIN_HAND);loaded.mobInteract(owner,InteractionHand.MAIN_HAND);
        helper.assertTrue(loaded.isOwnedBy(owner),"Partial gift progress was lost on reload");
        loaded.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,200));
        loaded.requestAbility(owner,false);
        helper.assertTrue(!loaded.hasEffect(net.minecraft.world.effect.MobEffects.POISON)&&loaded.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED),"Fairy remedy did not cleanse and grant haste");
        helper.assertTrue(!loaded.canAttack(owner),"Companion can attack its owner");
        loaded.discard();helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void mountsRejectStrangersAndProtectDismounts(GameTestHelper helper) {
        Player owner=helper.makeMockPlayer();
        Player stranger=helper.makeMockPlayer();stranger.setUUID(java.util.UUID.randomUUID());
        for(Species s:Species.values()) if(s.mount) {
            IslandCreature c=ForgottenIsland.TYPES.get(s).get().create(helper.getLevel());
            c.setPos(helper.absolutePos(new net.minecraft.core.BlockPos(3,200,3)).getCenter());
            helper.getLevel().addFreshEntity(c);c.befriend(owner);
            helper.assertTrue(!stranger.startRiding(c),"Stranger mounted "+s.id);
            helper.assertTrue(owner.startRiding(c),"Owner could not mount "+s.id+" at "+c.position()+"; blocks "+helper.getLevel().getBlockState(c.blockPosition().above(2))+" / "+helper.getLevel().getBlockState(c.blockPosition().above(3)));
            helper.assertTrue(c.getControllingPassenger()==owner,"Wrong controller: "+s.id);
            var destination=c.getDismountLocationForPassenger(owner);
            helper.assertTrue(helper.getLevel().noCollision(owner,owner.getDimensions(net.minecraft.world.entity.Pose.STANDING).makeBoundingBox(destination)),"Obstructed dismount: "+s.id);
            var protection=owner.getEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING);
            helper.assertTrue(protection!=null&&protection.getDuration()>1000,"High-altitude dismount protection too short: "+s.id);
            owner.stopRiding();c.discard();
        }
        helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void friendshipPersistenceAndRequests(GameTestHelper helper) {
        Player owner=helper.makeMockPlayer();
        owner.setPos(helper.absolutePos(new net.minecraft.core.BlockPos(3,2,3)).getCenter());
        Player stranger=helper.makeMockPlayer();
        // Mock players may share a profile. Use an independent explicit UUID.
        stranger.setUUID(java.util.UUID.randomUUID());
        for(Species s:Species.values()) {
            IslandCreature c=ForgottenIsland.TYPES.get(s).get().create(helper.getLevel());
            helper.assertTrue(c!=null,"Registered factory missing: "+s.id);
            c.setPos(owner.position());helper.getLevel().addFreshEntity(c);
            owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(s.gift,3));
            for(int i=0;i<3;i++)c.mobInteract(owner,InteractionHand.MAIN_HAND);
            helper.assertTrue(c.isOwnedBy(owner)&&c.friendship()==3,"Friendship failed: "+s.id);
            helper.assertTrue(!c.requestAbility(stranger,false),"Foreign player controlled "+s.id);
            helper.assertTrue(c.requestAbility(owner,false),"Targetless ability failed: "+s.id);
            if(s==Species.SARANGAY) helper.assertTrue(c.getAbsorptionAmount()==8,"Jewel aegis did not grant four temporary hearts");
            helper.assertTrue(!c.requestAbility(owner,false),"Cooldown bypass: "+s.id);
            c.setCustomName(Component.literal("Remember "+s.id));c.setOrderedToSit(true);c.command(owner,true);c.nextVariant();
            CompoundTag saved=new CompoundTag();c.saveWithoutId(saved);
            IslandCreature loaded=ForgottenIsland.TYPES.get(s).get().create(helper.getLevel());loaded.load(saved);
            helper.assertTrue(loaded.isOwnedBy(owner),"Owner lost: "+s.id);
            helper.assertTrue(loaded.friendship()==3&&loaded.isOrderedToSit()&&!loaded.defending(),"Commands lost: "+s.id);
            helper.assertTrue(loaded.cooldown()==c.cooldown()&&loaded.activeTicks()==c.activeTicks(),"Duration lost: "+s.id);
            helper.assertTrue(loaded.variant()==(s.hasVariants()?1:0)&&loaded.getCustomName().getString().equals("Remember "+s.id),"Name/variant lost: "+s.id);
            helper.assertTrue(loaded.isAlliedTo(owner),"Owner not allied: "+s.id);
            c.discard();
        }
        helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=400)
    public static void abilitiesExpireAndDoNotChangeWorld(GameTestHelper helper) {
        Player owner=helper.makeMockPlayer();
        owner.setPos(helper.absolutePos(new net.minecraft.core.BlockPos(3,2,3)).getCenter());
        IslandCreature a=ForgottenIsland.TYPES.get(Species.BAKUNAWA).get().create(helper.getLevel());
        IslandCreature b=ForgottenIsland.TYPES.get(Species.BAKUNAWA).get().create(helper.getLevel());
        for(IslandCreature c:new IslandCreature[]{a,b}){c.setPos(owner.position());c.befriend(owner);c.setOrderedToSit(true);helper.getLevel().addFreshEntity(c);c.requestAbility(owner,false);}
        long time=helper.getLevel().getDayTime();boolean raining=helper.getLevel().isRaining();
        a.discard();
        helper.runAfterDelay(IslandConfig.ECLIPSE_SECONDS.get()*20L+2,()->{
            helper.assertTrue(b.activeTicks()==0,"Eclipse failed to expire after overlapping source removal");
            long elapsed=helper.getLevel().getDayTime()-time;
            helper.assertTrue(elapsed>=0&&elapsed<=620,"Ability changed world time");
            helper.assertTrue(helper.getLevel().isRaining()==raining,"Ability changed weather");
            b.discard();helper.succeed();
        });
    }
}
