package com.forgottenisland.entity;

import com.forgottenisland.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import java.util.UUID;
import javax.annotation.Nullable;

public class IslandCreature extends TamableAnimal {
    private static final EntityDataAccessor<Integer> FRIENDSHIP = SynchedEntityData.defineId(IslandCreature.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DEFEND = SynchedEntityData.defineId(IslandCreature.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ACTIVE = SynchedEntityData.defineId(IslandCreature.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ABILITY = SynchedEntityData.defineId(IslandCreature.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COOLDOWN = SynchedEntityData.defineId(IslandCreature.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(IslandCreature.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(IslandCreature.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STAY = SynchedEntityData.defineId(IslandCreature.class,EntityDataSerializers.BOOLEAN);
    public final Species species;
    private UUID giftGiver;
    private int verticalInput, inputAge;
    public int reactionTicks;

    public IslandCreature(EntityType<? extends TamableAnimal> type, Level level, Species species) {
        super(type,level); this.species=species;
        setMaxUpStep(1.2f); setPersistenceRequired();
        if(!species.aquatic()) goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(4,new TemptGoal(this,1.0,net.minecraft.world.item.crafting.Ingredient.of(species.gift),false));
    }
    public static AttributeSupplier.Builder attributes(Species s) {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,s.health).add(Attributes.MOVEMENT_SPEED,.28)
            .add(Attributes.ATTACK_DAMAGE,s==Species.BAKUNAWA?8:4).add(Attributes.FOLLOW_RANGE,32)
            .add(Attributes.KNOCKBACK_RESISTANCE,s.mount?.65:0);
    }
    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FRIENDSHIP,0); entityData.define(DEFEND,true); entityData.define(ACTIVE,0);
        entityData.define(ABILITY,0); entityData.define(COOLDOWN,0); entityData.define(VARIANT,0); entityData.define(FLYING,false);
        entityData.define(STAY,false);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1,new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2,new MeleeAttackGoal(this,1.15,true));
        goalSelector.addGoal(3,new FollowOwnerGoal(this,1.1,5,2,false));
        goalSelector.addGoal(7,new WaterAvoidingRandomStrollGoal(this,.65) {
            @Override public boolean canUse() {return !isOrderedToSit()&&super.canUse();}
            @Override public boolean canContinueToUse() {return !isOrderedToSit()&&super.canContinueToUse();}
        });
        goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8));
        goalSelector.addGoal(9,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2,new OwnerHurtTargetGoal(this));
    }
    public int friendship() {return entityData.get(FRIENDSHIP);}
    @Override public boolean isOwnedBy(LivingEntity player) {return isTame() && getOwnerUUID()!=null && getOwnerUUID().equals(player.getUUID());}
    @Override public boolean isOrderedToSit() {return entityData.get(STAY);}
    @Override public void setOrderedToSit(boolean stay) {super.setOrderedToSit(stay);entityData.set(STAY,stay);}
    public boolean defending() {return entityData.get(DEFEND);}
    public int activeTicks() {return entityData.get(ACTIVE);}
    public int abilityKind() {return entityData.get(ABILITY);}
    public int cooldown() {return entityData.get(COOLDOWN);}
    public int variant() {return entityData.get(VARIANT);}
    public void nextVariant() {if(species.hasVariants())entityData.set(VARIANT,(variant()+1)%2);}
    @Override protected Component getTypeName() {return Component.literal(species==null?"Island companion":species.variantName(variant()));}
    public boolean flying() {return entityData.get(FLYING);}
    public boolean transformed() {return activeTicks()>0 && (species==Species.RAWW || species==Species.MANANG);}
    public void befriend(Player p) {
        if (isTame() && !isOwnedBy(p)) return;
        tame(p); entityData.set(FRIENDSHIP,3); giftGiver=p.getUUID();
        setOrderedToSit(false); setTarget(null); heal(getMaxHealth());
        level().broadcastEntityEvent(this,(byte)7); reactionTicks=30;
    }
    public void command(Player p, boolean changeDefense) {
        if (!isOwnedBy(p)) return;
        if(changeDefense) entityData.set(DEFEND,!defending());
        else setOrderedToSit(!isOrderedToSit());
        navigation.stop(); setTarget(null);
        p.displayClientMessage(Component.literal(species.displayName+": "+(isOrderedToSit()?"Stay":"Follow")+" • "+(defending()?"Defend":"Passive")),true);
        level().broadcastEntityEvent(this,(byte)11);
    }
    @Override public InteractionResult mobInteract(Player p, InteractionHand hand) {
        ItemStack held=p.getItemInHand(hand);
        if(held.is(Items.NAME_TAG)) return super.mobInteract(p,hand);
        if(level().isClientSide) return InteractionResult.SUCCESS;
        if(held.is(ForgottenIsland.SHOWCASE.get())) {
            if(!p.isCreative()) {say(p,"The Showcase Wand requires Creative mode."); return InteractionResult.FAIL;}
            if(isTame()&&!isOwnedBy(p)) {say(p,"Already bonded to another adventurer."); return InteractionResult.FAIL;}
            if(!isTame()) {befriend(p); say(p,"Friendship made! Use again to demonstrate."+(species.hasVariants()?" Sneak-use changes variant.":""));}
            else if(p.isShiftKeyDown()) {nextVariant(); say(p,species.hasVariants()?species.variantName(variant()):"This companion has one appearance.");}
            else requestAbility(p,false);
            return InteractionResult.SUCCESS;
        }
        if(held.is(species.gift)) {
            if(isTame()&&!isOwnedBy(p)) {say(p,"Already bonded to another adventurer."); return InteractionResult.FAIL;}
            if(!p.isCreative()) held.shrink(1);
            if(isTame()) {heal(6); say(p,"A welcome snack!");}
            else {
                int progress=CompanionRules.giftProgress(giftGiver,p.getUUID(),friendship());
                giftGiver=p.getUUID(); entityData.set(FRIENDSHIP,progress);
                if(progress>=3) {befriend(p); say(p,"Best friends! Empty-hand sneak-use: Follow/Stay. Whistle: ability.");}
                else say(p,"Friendship "+progress+"/3 — another "+held.getHoverName().getString()+"?");
                level().broadcastEntityEvent(this,(byte)7);
            }
            return InteractionResult.SUCCESS;
        }
        if(isOwnedBy(p)) {
            if(held.is(ForgottenIsland.WHISTLE.get())) {
                if(p.isShiftKeyDown()) command(p,true); else requestAbility(p,false);
            } else if(p.isShiftKeyDown()) command(p,false);
            else if(held.isEmpty() && species.mount) {
                if(p.startRiding(this)) {
                    setOrderedToSit(false); navigation.stop();
                    say(p,species.flies?"WASD steer • Space ascend • C descend • Shift dismount • R ability":"WASD steer • Shift dismount • R ability");
                } else say(p,"There is not enough room above the saddle. Move to an open space.");
            } else say(p,"Sneak + empty hand: Follow/Stay. Whistle: ability; sneak + whistle: Passive/Defend.");
        } else say(p,"Befriend with 3 × "+species.gift.getDescription().getString()+". Read the Creature Guide for more.");
        return InteractionResult.SUCCESS;
    }
    private void say(Player p,String text) {p.displayClientMessage(Component.literal(species.displayName+": "+text),true);}
    public boolean requestAbility(Player p, boolean secondary) {
        if(!CompanionRules.mayRequest(getOwnerUUID(),p.getUUID(),isAlive(),distanceToSqr(p))) return false;
        if(cooldown()>0) {say(p,"Ready in "+((cooldown()+19)/20)+"s"); return false;}
        int kind=secondary&&species==Species.BAKUNAWA?2:1;
        entityData.set(ABILITY,kind);
        int duration=species==Species.BAKUNAWA&&kind==1?IslandConfig.ECLIPSE_SECONDS.get()*20:100;
        entityData.set(ACTIVE,duration);
        entityData.set(COOLDOWN,Math.max(duration+20,IslandConfig.COOLDOWN_SECONDS.get()*20));
        level().playSound(null,blockPosition(),ForgottenIsland.CALLS.get(species).get(),getSoundSource(),species==Species.BAKUNAWA?1.5f:.8f,kind==2?.7f:1);
        AbilityEffects.activate(this,kind);
        say(p,kind==2?species.secondary:species.ability);
        return true;
    }
    public void setRideInput(int vertical) {
        verticalInput=Mth.clamp(vertical,-1,1); inputAge=0;
        // Remote-player mounts do not run server travel(), so mode changes belong to the validated input path.
        if(species.flies && verticalInput>0 && !isInWater()) entityData.set(FLYING,true);
        if(species.aquatic() && isInWater()) entityData.set(FLYING,false);
        if(onGround() && verticalInput<0) entityData.set(FLYING,false);
    }
    @Override public void tick() {
        super.tick();
        if(reactionTicks>0) reactionTicks--;
        if(!level().isClientSide) {
            if(cooldown()>0) entityData.set(COOLDOWN,cooldown()-1);
            if(activeTicks()>0) {
                entityData.set(ACTIVE,activeTicks()-1);
                if(tickCount%5==0) AbilityEffects.pulse(this);
            }
            if(++inputAge>10) verticalInput=0;
            if(!defending() || isOrderedToSit()) setTarget(null);
            if(isOrderedToSit()) {
                navigation.stop();
                if(species.aquatic()&&isInWater()) setDeltaMovement(Vec3.ZERO);
            }
            if(getTarget()!=null && (!canAttack(getTarget()) || !(getTarget() instanceof Monster))) setTarget(null);
            if(species.aquatic()) setAirSupply(getMaxAirSupply());
            if(species.flies && !isVehicle()) {
                LivingEntity owner=getOwner();
                boolean airborne=!isOrderedToSit() && owner!=null && (species==Species.ZIPPY || owner.getY()-getY()>2 || !onGround()&&flying());
                entityData.set(FLYING,airborne);
                if(airborne && owner!=null) {
                    navigation.stop();
                    double formation=getId()*2.399963;
                    Vec3 delta=owner.position().add(Math.cos(formation)*4,2,Math.sin(formation)*4).subtract(position());
                    if(delta.length()>3) setDeltaMovement(getDeltaMovement().scale(.8).add(delta.normalize().scale(.07)));
                    else setDeltaMovement(getDeltaMovement().scale(.8));
                }
            }
            if(species==Species.MERMAHN && isInWater() && isTame() && !isOrderedToSit() && getOwner()!=null && getOwner().isInWater()) {
                Vec3 delta=getOwner().position().subtract(position());
                if(delta.length()>3) {navigation.stop();setDeltaMovement(getDeltaMovement().scale(.85).add(delta.normalize().scale(.05)));}
            }
            setNoGravity(flying() || species.aquatic()&&isInWater());
        }
        if(species.aquatic()&&isInWater()&&getControllingPassenger() instanceof Player rider) {
            rider.setAirSupply(rider.getMaxAirSupply());
        }
        if(!level().isClientSide && getControllingPassenger() instanceof Player rider && rider.isInWall()) rider.stopRiding();
    }
    private Vec3 seatPosition(Entity passenger) {
        double angle=getYRot()*Math.PI/180;
        double back=species==Species.SARIMANOK?.55:0;
        double side=species==Species.DATU?.85:0;
        return position().add(Math.sin(angle)*back+Math.cos(angle)*side,
            getPassengersRidingOffset()+passenger.getMyRidingOffset(),
            -Math.cos(angle)*back+Math.sin(angle)*side);
    }
    private boolean clearRiderBox(Entity passenger,AABB box) {
        return level().getWorldBorder().isWithinBounds(box)&&!level().getBlockCollisions(passenger,box).iterator().hasNext();
    }
    @Override public void move(MoverType type,Vec3 motion) {
        // Vanilla clips the mount's hull; also clip its elevated passenger against ceilings/walls.
        if(species!=null && getControllingPassenger() instanceof Player rider) {
            AABB box=rider.getDimensions(Pose.STANDING).makeBoundingBox(seatPosition(rider));
            double dy=clearRiderBox(rider,box.move(0,motion.y,0))?motion.y:0;
            box=box.move(0,dy,0);
            double dx=clearRiderBox(rider,box.move(motion.x,0,0))?motion.x:0;
            box=box.move(dx,0,0);
            double dz=clearRiderBox(rider,box.move(0,0,motion.z))?motion.z:0;
            motion=new Vec3(dx,dy,dz);
        }
        super.move(type,motion);
    }
    @Override public void travel(Vec3 input) {
        if(getControllingPassenger() instanceof Player rider && isAlive()) {
            setYRot(rider.getYRot()); yRotO=getYRot(); yBodyRot=getYRot(); yHeadRot=getYRot();
            setXRot(rider.getXRot()*.25f);
            if(!level().isClientSide) {
                if(species.flies && verticalInput>0 && !isInWater()) entityData.set(FLYING,true);
                if(species.aquatic() && isInWater()) entityData.set(FLYING,false);
                if(onGround() && verticalInput<0) entityData.set(FLYING,false);
            }
            boolean free=flying() || species.aquatic()&&isInWater();
            if(isControlledByLocalInstance()) {
                float speed=(float)(.34*IslandConfig.MOUNT_SPEED.get()*(free?1.6:1));
                setSpeed(speed);
                if(free) {
                    setNoGravity(true);
                    // The client predicts vertical input; server receives the same bounded key state.
                    double vertical=verticalInput*.32;
                    if(getY()>=level().getMaxBuildHeight()-2) vertical=Math.min(0,vertical);
                    moveRelative(speed*.22f,new Vec3(rider.xxa*.65,0,rider.zza));
                    setDeltaMovement(getDeltaMovement().x,vertical,getDeltaMovement().z);
                    move(MoverType.SELF,getDeltaMovement());
                    setDeltaMovement(getDeltaMovement().scale(.8));
                } else super.travel(new Vec3(rider.xxa*.65,input.y,rider.zza));
            } else setDeltaMovement(Vec3.ZERO);
            calculateEntityAnimation(false);
        } else if(isNoGravity()) {
            moveRelative(.04f,input); move(MoverType.SELF,getDeltaMovement()); setDeltaMovement(getDeltaMovement().scale(.91));
        } else super.travel(input);
    }
    @Override @Nullable public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player p && isOwnedBy(p)?p:null;
    }
    @Override public double getPassengersRidingOffset() {return species==Species.BAKUNAWA?1.8:species==Species.DATU?2.5:1.75;}
    @Override protected void positionRider(Entity passenger,Entity.MoveFunction move) {
        if(!hasPassenger(passenger))return;
        Vec3 seat=seatPosition(passenger);
        move.accept(passenger,seat.x,seat.y,seat.z);
    }
    @Override protected boolean canAddPassenger(Entity p) {
        return species.mount && getPassengers().isEmpty() && p instanceof Player player && isOwnedBy(player)
            &&clearRiderBox(p,p.getDimensions(Pose.STANDING).makeBoundingBox(seatPosition(p)));
    }
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        passenger.fallDistance=0;
        for(int radius=2;radius<=5;radius++) for(int dy=0;dy<=3;dy++) for(int angle=0;angle<8;angle++) {
            Vec3 candidate=position().add(Math.cos(angle*Math.PI/4)*radius,dy+.5,Math.sin(angle*Math.PI/4)*radius);
            AABB box=passenger.getDimensions(Pose.STANDING).makeBoundingBox(candidate);
            if(level().getWorldBorder().isWithinBounds(box)&&level().noCollision(passenger,box)) return protectDismount(passenger,candidate);
        }
        return protectDismount(passenger,super.getDismountLocationForPassenger(passenger));
    }
    private Vec3 protectDismount(LivingEntity passenger,Vec3 destination) {
        // Cover a full descent to the world's floor, even when drifting off a high ledge.
        // Ten seconds is insufficient for a flying mount at normal exploration altitudes.
        int ticks=Mth.clamp((int)Math.ceil((destination.y-level().getMinBuildHeight())*10)+100,200,6000);
        passenger.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,ticks,0,false,false));
        passenger.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,400,0,false,false));
        return destination;
    }
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source) {return species.flies?false:super.causeFallDamage(distance,multiplier,source);}
    @Override public boolean canBreatheUnderwater() {return species!=null&&species.aquatic();}
    @Override public boolean canBeLeashed(Player p) {return !isLeashed()&&(!isTame()||isOwnedBy(p));}
    @Override public boolean isFood(ItemStack stack) {return false;}
    @Override @Nullable public AgeableMob getBreedOffspring(ServerLevel l,AgeableMob mate) {return null;}
    @Override public boolean canAttack(LivingEntity target) {return defending()&&!isOrderedToSit()&&target instanceof Monster&&!isAlliedTo(target)&&super.canAttack(target);}
    @Override public boolean doHurtTarget(Entity target) {return IslandConfig.DEFENSE_DAMAGE.get()&&target instanceof LivingEntity living&&canAttack(living)&&super.doHurtTarget(target);}
    @Override public boolean isAlliedTo(Entity other) {
        if(isTame() && (other.getUUID().equals(getOwnerUUID()) || other instanceof TamableAnimal t && getOwnerUUID()!=null&&getOwnerUUID().equals(t.getOwnerUUID()))) return true;
        return super.isAlliedTo(other);
    }
    @Override public void handleEntityEvent(byte event) {
        if(event==7||event==11) reactionTicks=30;
        if(event==11) return;
        super.handleEntityEvent(event);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Friendship",friendship()); if(giftGiver!=null)tag.putUUID("GiftGiver",giftGiver);
        tag.putBoolean("Defend",defending());tag.putInt("AbilityTicks",activeTicks());tag.putInt("Ability",abilityKind());
        tag.putInt("Cooldown",cooldown());tag.putInt("Variant",variant());tag.putBoolean("Flying",flying());
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Vanilla restores its private orderedToSit field directly, bypassing the setter.
        entityData.set(STAY,tag.getBoolean("Sitting"));
        entityData.set(FRIENDSHIP,Mth.clamp(tag.getInt("Friendship"),0,3));
        giftGiver=tag.hasUUID("GiftGiver")?tag.getUUID("GiftGiver"):null;
        entityData.set(DEFEND,!tag.contains("Defend")||tag.getBoolean("Defend"));
        entityData.set(ACTIVE,CompanionRules.clampTicks(tag.getInt("AbilityTicks")));
        entityData.set(ABILITY,Mth.clamp(tag.getInt("Ability"),0,2));
        entityData.set(COOLDOWN,CompanionRules.clampTicks(tag.getInt("Cooldown")));
        entityData.set(VARIANT,species.hasVariants()?Mth.clamp(tag.getInt("Variant"),0,1):0);entityData.set(FLYING,species.flies&&tag.getBoolean("Flying"));
    }
}
