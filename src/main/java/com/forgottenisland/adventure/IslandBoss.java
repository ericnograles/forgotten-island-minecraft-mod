package com.forgottenisland.adventure;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Separate hostile variants. Encounter clocks, damage, targets and hit regions live on the server. */
public final class IslandBoss extends Monster {
    private static final EntityDataAccessor<Integer> STAGE=SynchedEntityData.defineId(IslandBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLOCK=SynchedEntityData.defineId(IslandBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(IslandBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTEMPT=SynchedEntityData.defineId(IslandBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> AIM_X=SynchedEntityData.defineId(IslandBoss.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AIM_Z=SynchedEntityData.defineId(IslandBoss.class,EntityDataSerializers.FLOAT);
    public final BossKind kind;
    private final BossPart[] parts;
    private final Set<UUID> struck=new HashSet<>();
    private int lastWarning=-1;
    public IslandBoss(EntityType<? extends Monster> type,Level level,BossKind kind) {
        super(type,level);this.kind=kind;setPersistenceRequired();setNoGravity(true);noCulling=true;
        if(kind==BossKind.MANANG)parts=new BossPart[]{new BossPart(this,1.7f,2.1f,true),new BossPart(this,2.8f,2.6f,false)};
        else {
            parts=new BossPart[13];
            for(int i=0;i<10;i++)parts[i]=new BossPart(this,i==0?11:8,7,false);
            for(int i=10;i<13;i++)parts[i]=new BossPart(this,3,3,true);
        }
        setId(ENTITY_COUNTER.getAndAdd(parts.length+1)+1);
    }
    public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,1000).add(Attributes.MOVEMENT_SPEED,0).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,128);}
    @Override protected void registerGoals() {}
    @Override protected void defineSynchedData() {
        super.defineSynchedData();entityData.define(STAGE,0);entityData.define(CLOCK,0);entityData.define(PHASE,1);entityData.define(ATTEMPT,0);entityData.define(AIM_X,0f);entityData.define(AIM_Z,0f);
    }
    @Override public void setId(int id){super.setId(id);if(parts!=null)for(int i=0;i<parts.length;i++)parts[i].setId(id+i+1);}
    @Override public boolean isMultipartEntity(){return true;}
    @Override public BossPart[] getParts(){return parts;}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override protected boolean shouldDespawnInPeaceful(){return false;}
    @Override public boolean causeFallDamage(float d,float m,DamageSource source){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public boolean isPickable(){return kind==BossKind.MANANG&&stage()==EncounterState.Stage.COMBAT;}
    @Override public void knockback(double strength,double x,double z) {}
    @Override protected SoundEvent getHurtSound(DamageSource s){return kind==BossKind.MANANG?SoundEvents.PHANTOM_HURT:SoundEvents.ENDER_DRAGON_HURT;}
    public EncounterState.Stage stage(){return EncounterState.Stage.values()[Mth.clamp(entityData.get(STAGE),0,4)];}
    public int clock(){return entityData.get(CLOCK);}
    public int phase(){return entityData.get(PHASE);}
    public int attempt(){return entityData.get(ATTEMPT);}
    public int localClock(){return clock()%kind.cycle(phase());}
    public int attack(){return clock()/kind.cycle(phase())%3;}
    public boolean exposed(){return stage()==EncounterState.Stage.COMBAT&&localClock()>=80;}
    public Vec3 aim(){return new Vec3(entityData.get(AIM_X),65,entityData.get(AIM_Z));}
    public Vec3 upperOffset() {
        if(kind!=BossKind.MANANG||stage()!=EncounterState.Stage.COMBAT||attack()!=0)return Vec3.ZERO;
        float t=localClock();if(t<40||t>=80)return Vec3.ZERO;
        return aim().subtract(kind.origin()).multiply(1,0,1).scale(Math.sin((t-40)/40*Math.PI));
    }
    public float separation() {
        if(kind!=BossKind.MANANG)return 0;
        if(stage()==EncounterState.Stage.ENTRANCE)return Mth.sin(Mth.clamp((clock()-20)/80f,0,1)*Mth.PI)*3.5f;
        if(stage()==EncounterState.Stage.DEFEAT)return Mth.sin(Mth.clamp(clock()/(float)kind.defeatLength,0,1)*Mth.PI)*2;
        if(stage()!=EncounterState.Stage.COMBAT||exposed())return 0;
        return attack()==0&&localClock()>=40?3.5f*(1-(float)Math.sin((localClock()-40)/40f*Math.PI)):3.5f;
    }
    public Vec3 weakPoint(int i) {
        if(kind==BossKind.MANANG)return kind.origin();
        return Vec3.atBottomCenterOf(kind.center).add((i-1)*15,0,i==1?38:27);
    }
    public Vec3 bodyPoint(int segment) {
        double x=0,z=0,angle=0;
        for(int i=0;i<=segment;i++) {
            if(i>0){x+=Math.sin(angle)*6.75;z-=Math.cos(angle)*6.75;}
            angle+=Math.sin(clock()*.025-i*.45)*.12;
        }
        return kind.origin().add(x+Math.sin(angle)*3.9,6.3,z-Math.cos(angle)*3.9);
    }
    public void sync(EncounterState state) {
        entityData.set(STAGE,state.stage().ordinal());entityData.set(CLOCK,state.ticks());entityData.set(PHASE,state.phase());entityData.set(ATTEMPT,state.attempt());
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(1,state.maxHealth()));
        setHealth(Math.max(1,state.health()));
    }
    @Override public void tick() {
        super.tick();setDeltaMovement(Vec3.ZERO);clearFire();fallDistance=0;
        if(kind==null)return;
        if(!level().isClientSide) {
            if(level().dimension()!=AdventureContent.ISLAND){discard();return;}
            var data=IslandSavedData.get(level().getServer());var state=data.encounter(kind.id);
            if(!getUUID().equals(state.actor())||state.stage()==EncounterState.Stage.READY||state.stage()==EncounterState.Stage.COMPLETE){discard();return;}
            sync(state);setPos(kind.origin());
            if(stage()==EncounterState.Stage.COMBAT)combat((ServerLevel)level());
            else cinematicEffects((ServerLevel)level());
        }
        positionParts();
    }
    private void positionParts() {
        if(kind==BossKind.MANANG) {
            parts[0].setPos(kind.origin());parts[1].setPos(kind.origin().add(0,2.1+separation(),0).add(upperOffset()));
        } else {
            parts[0].setPos(kind.origin().add(0,8,13));
            for(int i=1;i<10;i++)parts[i].setPos(bodyPoint(i-1));
            for(int i=0;i<3;i++)parts[10+i].setPos(weakPoint(i));
        }
    }
    @Override public boolean hurt(DamageSource source,float amount){return hurtPart(source,amount,kind==BossKind.MANANG);}
    public boolean hurtPart(DamageSource source,float amount,boolean weak) {
        if(level().isClientSide||stage()!=EncounterState.Stage.COMBAT||!(source.getEntity() instanceof ServerPlayer p)||!kind.inside(p.position())||p.isSpectator()||p.isCreative())return false;
        var data=IslandSavedData.get(p.server);var state=data.encounter(kind.id);
        if(!state.participants().contains(p.getUUID()))return false;
        if(weak&&!exposed()) {
            p.displayClientMessage(Component.literal(kind==BossKind.MANANG?"Her wings shield the lower body. Strike after the attack!":"Moon anchors are shielded. Strike when they turn gold!"),true);return false;
        }
        boolean hit=super.hurt(source,amount*(weak?1.5f:.6f));
        if(stage()==EncounterState.Stage.COMBAT)state.recordHealth(getHealth());
        data.setDirty();return hit;
    }
    @Override public void die(DamageSource source) {
        // Do not invoke vanilla death removal/loot. Journal the lethal hit before any scene or reward.
        if(level() instanceof ServerLevel level) {
            var data=IslandSavedData.get(level.getServer());var state=data.encounter(kind.id);
            if(state.recordHealth(0)){data.setDirty();sync(state);}
        }
        setHealth(1);deathTime=0;
    }
    private List<ServerPlayer> players(){return ((ServerLevel)level()).players().stream().filter(p->p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&kind.inside(p.position())).toList();}
    private void combat(ServerLevel level) {
        int t=localClock(),attack=attack();
        var players=players();
        int warning=phase()*1000000+clock()/kind.cycle(phase());
        if(t<=1&&warning!=lastWarning) {
            lastWarning=warning;
            struck.clear();
            if(!players.isEmpty()) {
                var target=players.get((clock()/kind.cycle(phase()))%players.size());
                Vec3 center=Vec3.atBottomCenterOf(kind.center),delta=target.position().subtract(center).multiply(1,0,1);
                Vec3 point=center.add(delta.length()>kind.radius-8?delta.normalize().scale(kind.radius-8):delta);
                entityData.set(AIM_X,(float)point.x);entityData.set(AIM_Z,(float)point.z);
                if(kind==BossKind.MANANG){setYRot((float)(Mth.atan2(-(target.getX()-getX()),target.getZ()-getZ())*180/Math.PI));yBodyRot=getYRot();}
            }
            warn(players,attack);sound(AdventureContent.BOSS_WARNING.get(),.85f,kind==BossKind.MANANG?1.2f:.65f);
        }
        if(t==80){sound(AdventureContent.WEAKPOINT.get(),1,1);for(var p:players)p.displayClientMessage(Component.literal(kind==BossKind.MANANG?"Wings folded — strike her lower body!":"Moon anchors exposed — strike the gold crystals!"),true);}
        if(t<60&&t%4==0) {
            if(attack==0)ring(level,aim(),kind==BossKind.MANANG?4:6,ParticleTypes.FLAME,28);
            else if(attack==1)ring(level,Vec3.atBottomCenterOf(kind.center),kind==BossKind.MANANG?7:12,ParticleTypes.SOUL_FIRE_FLAME,36);
            else line(level,kind==BossKind.MANANG?position():Vec3.atBottomCenterOf(kind.center),aim(),ParticleTypes.END_ROD);
        }
        if(t==60)sound(attack==1?AdventureContent.SHRIEK.get():AdventureContent.BOSS_IMPACT.get(),1.2f,kind==BossKind.MANANG?1:.6f);
        if(t>=60&&t<80) {
            double r=(t-60)*(kind==BossKind.MANANG?1.5:3.1);
            if(attack==1&&t%2==0)ring(level,Vec3.atBottomCenterOf(kind.center),r,ParticleTypes.SOUL_FIRE_FLAME,64);
            if(t==60&&attack==0)level.sendParticles(ParticleTypes.EXPLOSION,aim().x,65.4,aim().z,7,2,.2,2,0);
            if(t%3==0&&attack==2)line(level,Vec3.atBottomCenterOf(kind.center),aim(),ParticleTypes.DRAGON_BREATH);
            for(var p:players) {
                if(struck.contains(p.getUUID()))continue;
                Vec3 c=Vec3.atBottomCenterOf(kind.center),v=p.position().subtract(c),direction=aim().subtract(c).normalize();
                boolean hit=attack==0?t==60&&p.position().subtract(aim()).horizontalDistanceSqr()<Math.pow(kind==BossKind.MANANG?4:6,2):
                    attack==1?Math.abs(v.horizontalDistance()-r)<1.6&&p.getY()<66.1:
                    v.horizontalDistance()<kind.radius&&v.normalize().dot(direction)>(kind==BossKind.MANANG?.77:.985)&&p.getY()<69;
                if(hit){p.hurt(damageSources().mobAttack(this),(phase()==1?6:8)+(kind==BossKind.BAKUNAWA?2:0));p.knockback(.55,-v.x,-v.z);struck.add(p.getUUID());}
            }
        }
        if(exposed()&&t%6==0)for(int i=0;i<(kind==BossKind.MANANG?1:3);i++) {
            Vec3 v=weakPoint(i);level.sendParticles(ParticleTypes.WAX_ON,v.x,v.y+1.5,v.z,4,.8,1,.8,0);
        }
        if(kind==BossKind.BAKUNAWA&&phase()==2&&t==0)sound(AdventureContent.MOON_DEVOUR.get(),1,.8f);
    }
    private void warn(List<ServerPlayer> players,int attack) {
        String[] m={"SHADOW DIVE · leave the marked circle","SHRIEK · jump over the expanding ring","WING CUT · move out of her facing arc"};
        String[] b={"MOONFALL · leave the burning circle","LUNAR TIDE · jump over the blue ring","ECLIPSE BREATH · move sideways out of the beam"};
        for(var p:players)p.displayClientMessage(Component.literal((kind==BossKind.MANANG?m:b)[attack]),true);
    }
    private void cinematicEffects(ServerLevel level) {
        if(clock()==1||clock()==kind.entranceLength/2&&stage()==EncounterState.Stage.ENTRANCE)
            sound(stage()==EncounterState.Stage.DEFEAT?AdventureContent.BOSS_RELEASE.get():kind==BossKind.MANANG?AdventureContent.SHRIEK.get():AdventureContent.MOON_DEVOUR.get(),1.3f,1);
        if(clock()%5==0) {
            float r=stage()==EncounterState.Stage.DEFEAT?Math.max(1,18-clock()*.12f):Math.min(kind.radius,clock()*.35f);
            ring(level,Vec3.atBottomCenterOf(kind.center),r,stage()==EncounterState.Stage.DEFEAT?ParticleTypes.END_ROD:ParticleTypes.SOUL_FIRE_FLAME,48);
        }
    }
    private void sound(SoundEvent sound,float volume,float pitch) {
        // Send only to players inside this arena; no global time/weather or destructive effects.
        for(var p:((ServerLevel)level()).players())if(kind.inside(p.position()))p.playNotifySound(sound,SoundSource.HOSTILE,volume,pitch);
    }
    public static void ring(ServerLevel l,Vec3 c,double radius,net.minecraft.core.particles.SimpleParticleType type,int count) {
        for(int i=0;i<count;i++){double a=i*Math.PI*2/count;l.sendParticles(type,c.x+Math.cos(a)*radius,c.y+.15,c.z+Math.sin(a)*radius,1,0,0,0,0);}
    }
    private void line(ServerLevel l,Vec3 a,Vec3 b,net.minecraft.core.particles.SimpleParticleType type) {
        Vec3 d=b.subtract(a).normalize();for(int i=0;i<kind.radius;i+=2){Vec3 p=a.add(d.scale(i));if(!l.isLoaded(net.minecraft.core.BlockPos.containing(p)))break;l.sendParticles(type,p.x,65.7,p.z,1,.2,.2,.2,0);}
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putFloat("AimX",entityData.get(AIM_X));tag.putFloat("AimZ",entityData.get(AIM_Z));}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);entityData.set(AIM_X,tag.getFloat("AimX"));entityData.set(AIM_Z,tag.getFloat("AimZ"));}
}
