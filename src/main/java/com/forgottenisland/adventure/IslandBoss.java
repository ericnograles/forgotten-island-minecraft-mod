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
    private int atomicPulse=-1;
    public IslandBoss(EntityType<? extends Monster> type,Level level,BossKind kind) {
        super(type,level);this.kind=kind;setPersistenceRequired();setNoGravity(true);noCulling=true;
        if(kind==BossKind.MANANG)parts=new BossPart[]{new BossPart(this,1.7f,2.1f,true),new BossPart(this,2.8f,2.6f,false)};
        else {
            parts=new BossPart[13];
            for(int i=0;i<10;i++)parts[i]=new BossPart(this,i==0?22:8,i==0?11:7,false);
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
    public int attack(){return clock()/kind.cycle(phase())%(kind==BossKind.BAKUNAWA?BakunawaMoves.COUNT:3);}
    public boolean exposed(){return stage()==EncounterState.Stage.COMBAT&&localClock()>=(kind==BossKind.BAKUNAWA?BakunawaMoves.recovery(attack()):80);}
    public boolean warded(){return kind==BossKind.BAKUNAWA&&stage()==EncounterState.Stage.COMBAT&&BakunawaMoves.shield(attack(),localClock());}
    public Vec3 aim(){return new Vec3(entityData.get(AIM_X),kind.center.getY(),entityData.get(AIM_Z));}
    public Vec3 headOffset(){return headOffset(0);}
    public Vec3 headOffset(double partial){
        double tick=localClock()+partial;
        if(kind!=BossKind.BAKUNAWA||stage()!=EncounterState.Stage.COMBAT)return Vec3.ZERO;
        if(attack()==0)return aim().add(0,4,-14).subtract(kind.origin().add(0,40,32)).scale(BakunawaMoves.lunge(tick));
        if(attack()==10)return slashPoint(tick).add(0,4,-14).subtract(kind.origin().add(0,40,32))
            .scale(BakunawaMoves.smooth((tick-24)/28.0)*(1-BakunawaMoves.smooth((tick-72)/28.0)));
        if(attack()==2||attack()==7)return new Vec3(0,-25*BakunawaMoves.smooth(tick/35.0)*(1-BakunawaMoves.smooth((tick-(attack()==7?120:72))/28.0)),0);
        return Vec3.ZERO;
    }
    public double emergence(){return emergence(0);}
    public double emergence(double partial){return stage()==EncounterState.Stage.ENTRANCE?-52*(1-BakunawaMoves.smooth((clock()+partial-32)/80.0)):0;}
    public Vec3 headPoint(){return headPoint(0);}
    public Vec3 headPoint(double partial){return kind.origin().add(0,40+emergence(partial),32).add(headOffset(partial));}
    public Vec3 mouth(){return headPoint().add(0,-3.5,14);}
    public Vec3 slashPoint(double tick){double a=-1.25+2.5*BakunawaMoves.smooth((tick-52)/18.0);return aim().add(Math.sin(a)*11,1,Math.cos(a)*4-4);}
    public Vec3 tailTip(double tick){double a=-1.2+2.4*BakunawaMoves.smooth((tick-52)/18.0);return aim().add(Math.sin(a)*22,1,(Math.cos(a)-1)*22);}
    public Vec3 fireTarget(int index){return aim().add((index-1)*5,1,0);}
    public Vec3 projectilePoint(int index,double tick,boolean rock){
        double start=rock?28:38+index*8,end=rock?60:68+index*8;
        double t=Mth.clamp((tick-start)/(end-start),0,1);var from=kind.origin().add(0,37,46);var to=rock?aim().add(0,1,0):fireTarget(index);
        return from.lerp(to,t).add(0,Math.sin(t*Math.PI)*(rock?16:4),0);
    }
    public Vec3 tentaclePoint(int i){
        double a=(i-1)*Math.PI/2;Vec3 p=i==0?aim():aim().add(Math.cos(a)*8,0,Math.sin(a)*8);
        var c=Vec3.atBottomCenterOf(kind.center);var d=p.subtract(c);return d.horizontalDistance()>kind.radius-4?c.add(d.normalize().scale(kind.radius-4)):p;
    }
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
    public Vec3 bodyPoint(int segment) {return bodyPoint(segment,0);}
    public Vec3 bodyPoint(int segment,double partial) {
        if(segment<=4){
            double f=segment/4.0;var top=headPoint(partial).subtract(kind.origin()).add(0,-4,-7);
            return kind.origin().add(top.lerp(new Vec3(0,2+emergence(partial),-8),f)).add(Math.sin((clock()+partial)*.035-segment*.6)*1.1*Math.sin(f*Math.PI),0,0);
        }
        double f=(segment-4)/5.0;
        var start=kind.origin().add(0,2+emergence(partial),-8);
        var end=kind.origin().add(Math.sin((clock()+partial)*.025)*4,-3+emergence(partial),-49);
        if(stage()==EncounterState.Stage.COMBAT&&attack()==9){double lift=BakunawaMoves.smooth((localClock()+partial-20)/25.0)*(1-BakunawaMoves.smooth((localClock()+partial-72)/28.0));end=end.lerp(tailTip(localClock()+partial),lift);return start.lerp(end,f).add(0,Math.sin(f*Math.PI)*14*lift,0);}
        return start.lerp(end,f);
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
            sync(state);setPos(kind.actorOrigin());
            if(stage()==EncounterState.Stage.COMBAT)combat((ServerLevel)level());
            else cinematicEffects((ServerLevel)level());
        }
        positionParts();
    }
    private void positionParts() {
        if(kind==BossKind.MANANG) {
            parts[0].setPos(kind.origin());parts[1].setPos(kind.origin().add(0,2.1+separation(),0).add(upperOffset()));
        } else {
            parts[0].setPos(headPoint().add(0,-5,0));
            for(int i=1;i<10;i++)parts[i].setPos(bodyPoint(i-1));
            for(int i=0;i<3;i++)parts[10+i].setPos(weakPoint(i));
        }
    }
    @Override public boolean hurt(DamageSource source,float amount){return hurtPart(source,amount,kind==BossKind.MANANG);}
    public boolean hurtPart(DamageSource source,float amount,boolean weak) {
        if(level().isClientSide||stage()!=EncounterState.Stage.COMBAT||!(source.getEntity() instanceof ServerPlayer p)||!kind.inside(p.position())||p.isSpectator()||p.isCreative())return false;
        var data=IslandSavedData.get(p.server);var state=data.encounter(kind.id);
        if(!state.participants().contains(p.getUUID()))return false;
        if(warded()){p.displayClientMessage(Component.literal("Bakunawa's golden ward is invincible — wait for it to fade!"),true);return false;}
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
        if(warning!=lastWarning) {
            lastWarning=warning;
            struck.clear();
            atomicPulse=-1;
            if(!players.isEmpty()) {
                var target=players.get((clock()/kind.cycle(phase()))%players.size());
                Vec3 center=Vec3.atBottomCenterOf(kind.center),delta=target.position().subtract(center).multiply(1,0,1);
                Vec3 point=center.add(delta.length()>kind.radius-8?delta.normalize().scale(kind.radius-8):delta);
                entityData.set(AIM_X,(float)point.x);entityData.set(AIM_Z,(float)point.z);
                if(kind==BossKind.MANANG){setYRot((float)(Mth.atan2(-(target.getX()-getX()),target.getZ()-getZ())*180/Math.PI));yBodyRot=getYRot();}
            }
            warn(players,attack);sound(AdventureContent.BOSS_WARNING.get(),.85f,kind==BossKind.MANANG?1.2f:.65f);
        }
        if(t==(kind==BossKind.BAKUNAWA?BakunawaMoves.recovery(attack):80)){sound(AdventureContent.WEAKPOINT.get(),1,1);for(var p:players)p.displayClientMessage(Component.literal(kind==BossKind.MANANG?"Wings folded — strike her lower body!":"Moon anchors exposed — strike the gold crystals!"),true);}
        if(kind==BossKind.BAKUNAWA&&(attack<4||attack>=6)){bakunawaCombat(level,players,t,attack);exposedEffects(level,t);return;}
        if(kind==BossKind.BAKUNAWA)attack-=3;
        if(t<60&&t%4==0) {
            if(attack==0)ring(level,aim(),kind==BossKind.MANANG?4:6,ParticleTypes.FLAME,28);
            else if(attack==1)ring(level,Vec3.atBottomCenterOf(kind.center),kind==BossKind.MANANG?7:12,ParticleTypes.SOUL_FIRE_FLAME,36);
            else line(level,kind==BossKind.MANANG?position():Vec3.atBottomCenterOf(kind.center),aim(),ParticleTypes.END_ROD);
        }
        if(t==60)sound(attack==1?AdventureContent.SHRIEK.get():AdventureContent.BOSS_IMPACT.get(),1.2f,kind==BossKind.MANANG?1:.6f);
        if(t>=60&&t<80) {
            double r=(t-60)*(kind==BossKind.MANANG?1.5:3.1);
            if(attack==1&&t%2==0)ring(level,Vec3.atBottomCenterOf(kind.center),r,ParticleTypes.SOUL_FIRE_FLAME,64);
            if(t==60&&attack==0)level.sendParticles(ParticleTypes.EXPLOSION,aim().x,kind.center.getY()+.4,aim().z,7,2,.2,2,0);
            if(t%3==0&&attack==2)line(level,Vec3.atBottomCenterOf(kind.center),aim(),ParticleTypes.DRAGON_BREATH);
            for(var p:players) {
                if(struck.contains(p.getUUID()))continue;
                Vec3 c=Vec3.atBottomCenterOf(kind.center),v=p.position().subtract(c),direction=aim().subtract(c).normalize();
                boolean hit=attack==0?t==60&&p.position().subtract(aim()).horizontalDistanceSqr()<Math.pow(kind==BossKind.MANANG?4:6,2):
                    attack==1?Math.abs(v.horizontalDistance()-r)<1.6&&p.getY()<kind.center.getY()+1.1:
                    v.horizontalDistance()<kind.radius&&v.normalize().dot(direction)>(kind==BossKind.MANANG?.77:.985)&&p.getY()<kind.center.getY()+4;
                if(hit){p.hurt(damageSources().mobAttack(this),(phase()==1?6:8)+(kind==BossKind.BAKUNAWA?2:0));p.knockback(.55,-v.x,-v.z);struck.add(p.getUUID());}
            }
        }
        exposedEffects(level,t);
    }
    private void exposedEffects(ServerLevel level,int t){
        if(exposed()&&t%6==0)for(int i=0;i<(kind==BossKind.MANANG?1:3);i++) {
            Vec3 v=weakPoint(i);level.sendParticles(ParticleTypes.WAX_ON,v.x,v.y+1.5,v.z,4,.8,1,.8,0);
        }
        if(kind==BossKind.BAKUNAWA&&phase()==2&&t==0)sound(AdventureContent.MOON_DEVOUR.get(),1,.8f);
    }
    void bakunawaCombat(ServerLevel level,List<ServerPlayer> players,int t,int attack){
        if(attack==0){
            if(t<60&&t%4==0)ring(level,aim(),4,ParticleTypes.FLAME,32);
            if(t==35)sound(SoundEvents.ENDER_DRAGON_FLAP,1,.65f);
            if(t==60){
                sound(SoundEvents.ENDER_DRAGON_GROWL,1,.7f);
                level.sendParticles(ParticleTypes.CLOUD,aim().x,aim().y+2,aim().z,50,3,1,3,.12);
                for(var p:players)if(p.position().subtract(aim()).horizontalDistanceSqr()<16&&Math.abs(p.getY()-aim().y)<4&&struck.add(p.getUUID())){
                    p.hurt(damageSources().mobAttack(this),BakunawaMoves.CHOMP_DAMAGE);
                    if(p.isAlive())BakunawaStomach.swallow(this,p);
                }
            }
        }else if(attack==1){
            for(int i=0;i<5;i++){
                var point=tentaclePoint(i);
                if(t<60&&t%5==0)ring(level,point,2.7,ParticleTypes.SOUL_FIRE_FLAME,18);
                if(t==40)level.sendParticles(ParticleTypes.POOF,point.x,point.y,point.z,12,1,.2,1,.03);
                if(t==60){
                    level.sendParticles(ParticleTypes.EXPLOSION,point.x,point.y+.3,point.z,1,0,0,0,0);
                    for(var p:players)if(p.position().subtract(point).horizontalDistanceSqr()<2.7*2.7&&p.getY()<point.y+3&&struck.add(p.getUUID())){
                        p.hurt(damageSources().mobAttack(this),BakunawaMoves.TENTACLE_DAMAGE);p.knockback(.4,point.x-p.getX(),point.z-p.getZ());
                    }
                }
            }
            if(t==40||t==60)sound(AdventureContent.BOSS_IMPACT.get(),1,t==40?.5f:.9f);
        }else if(attack==2){
            Vec3 from=mouth(),end=beamEnd();
            if(t<60&&t%3==0){
                beamParticles(level,from,end,ParticleTypes.ELECTRIC_SPARK,3);
                level.sendParticles(ParticleTypes.ENCHANT,from.x,from.y,from.z,8,.8,.8,.8,.1);
            }
            if(t==45)sound(SoundEvents.BEACON_ACTIVATE,1.2f,1.4f);
            if(t>=60&&t<72){
                if(t%2==0)beamParticles(level,from,end,ParticleTypes.END_ROD,1);
                if(t==60){
                    sound(SoundEvents.GUARDIAN_ATTACK,1.2f,.75f);
                    var holder=level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,com.forgottenisland.ForgottenIsland.id("holy_beam")));
                    var source=new DamageSource(holder,this,this,from);
                    // One discharge has one impact tick, regardless of its twelve visual frames.
                    for(var p:players)if(p.getBoundingBox().inflate(.8).clip(from,end).isPresent()&&struck.add(p.getUUID()))p.hurt(source,BakunawaMoves.BEAM_DAMAGE);
                    level.sendParticles(ParticleTypes.FLASH,end.x,end.y,end.z,1,0,0,0,0);
                }
            }
        }else if(attack==3||attack==6){
            boolean rock=attack==3;
            if(t<60&&t%4==0){if(rock)ring(level,aim(),6,ParticleTypes.CRIT,32);else for(int i=0;i<3;i++)ring(level,fireTarget(i).add(0,-1,0),3,ParticleTypes.FLAME,20);}
            int count=rock?1:3;
            for(int i=0;i<count;i++){
                int launch=rock?28:38+i*8,impact=rock?60:68+i*8;
                if(t==launch)sound(rock?SoundEvents.ENDER_DRAGON_FLAP:SoundEvents.BLAZE_SHOOT,1,rock?.6f:.7f+i*.15f);
                if(t>=launch&&t<impact&&t%2==0){var point=projectilePoint(i,t,rock);level.sendParticles(rock?ParticleTypes.CLOUD:ParticleTypes.FLAME,point.x,point.y,point.z,6,.5,.5,.5,.02);}
                if(t==impact){var target=rock?aim():fireTarget(i);
                    level.sendParticles(ParticleTypes.EXPLOSION,target.x,target.y+1,target.z,5,1.5,1,1.5,0);sound(AdventureContent.BOSS_IMPACT.get(),1.1f,rock?.55f:1.1f);
                    for(var p:players)if(p.position().subtract(target).horizontalDistanceSqr()<(rock?36:9)&&Math.abs(p.getY()-target.y)<4&&struck.add(p.getUUID()))p.hurt(damageSources().mobAttack(this),rock?BakunawaMoves.ROCK_DAMAGE:BakunawaMoves.FIREBALL_DAMAGE);
                }
            }
        }else if(attack==7){
            var from=mouth();var end=beamEnd();
            if(t<60&&t%4==0)beamParticles(level,from,end,ParticleTypes.SOUL_FIRE_FLAME,3);
            if(t==40)sound(SoundEvents.RESPAWN_ANCHOR_CHARGE,1.1f,.5f);
            if(t>=60&&t<120){
                if(t%3==0)beamParticles(level,from,end,ParticleTypes.SOUL_FIRE_FLAME,1);
                if((t-60)%20==0&&atomicPulse!=t){
                    atomicPulse=t;sound(SoundEvents.GUARDIAN_ATTACK,.7f,.5f);
                    for(var p:players)if(p.getBoundingBox().inflate(1.2).clip(from,end).isPresent())p.hurt(damageSources().mobAttack(this),BakunawaMoves.ATOMIC_DAMAGE);
                }
            }
        }else if(attack==8){
            if(t==20)sound(SoundEvents.BEACON_ACTIVATE,1,.65f);
            if(BakunawaMoves.shield(attack,t)&&t%5==0){var center=headPoint();ring(level,center.add(0,-3,0),12,ParticleTypes.WAX_ON,48);for(int i=0;i<3;i++)ring(level,weakPoint(i),2,ParticleTypes.ENCHANTED_HIT,16);}
            if(t==120)sound(SoundEvents.GLASS_BREAK,1,.6f);
        }else if(attack==9||attack==10){
            boolean tail=attack==9;
            if(t<52&&t%5==0)for(int i=0;i<13;i++){var point=tail?tailTip(52+i*1.5):slashPoint(52+i*1.5);level.sendParticles(tail?ParticleTypes.ELECTRIC_SPARK:ParticleTypes.FLAME,point.x,aim().y+.2,point.z,2,.2,0,.2,0);}
            if(t==48)sound(SoundEvents.ENDER_DRAGON_FLAP,1.1f,.65f);
            if(t>=52&&t<=70){
                var tip=tail?tailTip(t):slashPoint(t);var previous=tail?tailTip(t-1):slashPoint(t-1);
                level.sendParticles(tail?ParticleTypes.ELECTRIC_SPARK:ParticleTypes.SWEEP_ATTACK,tip.x,tip.y,tip.z,5,.6,.5,.6,.03);
                for(var p:players)if(BakunawaMoves.distanceToSegment(p.position().add(0,1,0),previous,tip)<3&&struck.add(p.getUUID())){p.hurt(damageSources().mobAttack(this),tail?BakunawaMoves.TAIL_DAMAGE:BakunawaMoves.SLASH_DAMAGE);p.knockback(.9,previous.x-p.getX(),previous.z-p.getZ());}
            }
            if(t==60)sound(SoundEvents.PLAYER_ATTACK_SWEEP,1.2f,.5f);
        }
    }
    public Vec3 beamEnd(){
        Vec3 from=mouth(),end=from.add(aim().add(0,1,0).subtract(from).normalize().scale(kind.radius*2));
        return level().clip(new net.minecraft.world.level.ClipContext(from,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getLocation();
    }
    private static void beamParticles(ServerLevel level,Vec3 from,Vec3 to,net.minecraft.core.particles.SimpleParticleType type,int step){
        var d=to.subtract(from);for(int n=0;n<d.length();n+=step){var p=from.add(d.normalize().scale(n));level.sendParticles(type,p.x,p.y,p.z,1,.08,.08,.08,0);}
    }
    private void warn(List<ServerPlayer> players,int attack) {
        String[] m={"SHADOW DIVE · leave the marked circle","SHRIEK · jump over the expanding ring","WING CUT · move out of her facing arc"};
        String[] b={"CHOMP · leave the amber circle! Crystals free swallowed players","TENTACLES · leave the five blue circles","HOLY BEAM · sidestep the white aim line! Two unblocked hits are lethal","MOON ROCKS · leave the landing circle","LUNAR TIDE · jump over the blue ring","ECLIPSE BREATH · move sideways out of the beam","FIREBALLS · leave the three orange targets","ATOMIC BREATH · leave the blue line; sustained contact hurts each second","GOLDEN WARD · invincible for five seconds; prepare for the opening","TAIL WHIP · move away from the yellow sweep","HORN SLASH · leave the red crescent"};
        for(var p:players)p.displayClientMessage(Component.literal((kind==BossKind.MANANG?m:b)[attack]),true);
    }
    private void cinematicEffects(ServerLevel level) {
        if(kind==BossKind.BAKUNAWA){bakunawaCinematic(level);return;}
        if(clock()==1||clock()==kind.entranceLength/2&&stage()==EncounterState.Stage.ENTRANCE)
            sound(stage()==EncounterState.Stage.DEFEAT?AdventureContent.BOSS_RELEASE.get():kind==BossKind.MANANG?AdventureContent.SHRIEK.get():AdventureContent.MOON_DEVOUR.get(),1.3f,1);
        if(clock()%5==0) {
            float r=stage()==EncounterState.Stage.DEFEAT?Math.max(1,18-clock()*.12f):Math.min(kind.radius,clock()*.35f);
            ring(level,Vec3.atBottomCenterOf(kind.center),r,stage()==EncounterState.Stage.DEFEAT?ParticleTypes.END_ROD:ParticleTypes.SOUL_FIRE_FLAME,48);
        }
    }
    private void bakunawaCinematic(ServerLevel level){
        boolean entrance=stage()==EncounterState.Stage.ENTRANCE;int t=clock();var water=kind.origin().add(0,-1,-8);
        if(entrance){
            if(t==1)sound(AdventureContent.MOON_DEVOUR.get(),1.2f,.55f);
            if(t>=24&&t<120&&t%4==0){ring(level,water,6+(t-24)*.16,ParticleTypes.SPLASH,90);level.sendParticles(ParticleTypes.CLOUD,water.x,water.y+2,water.z,16,7,1,7,.06);}
            if(t==52)sound(SoundEvents.GENERIC_SPLASH,1.7f,.55f);
            if(t==125){sound(AdventureContent.BAKUNAWA_ROAR.get(),1.5f,1);var mouth=mouth();level.sendParticles(ParticleTypes.CLOUD,mouth.x,mouth.y,mouth.z,70,4,2,4,.16);}
        }else{
            if(t==1)sound(AdventureContent.BOSS_RELEASE.get(),1.2f,.6f);
            if(t<90&&t%4==0){var point=headPoint();ring(level,point,Math.max(1,18-t*.18),ParticleTypes.END_ROD,48);}
            if(t==75)sound(SoundEvents.WITHER_BREAK_BLOCK,1.3f,.6f);
            if(t==130){sound(SoundEvents.GENERIC_EXPLODE,1.6f,.55f);var center=headPoint();level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,center.x,center.y,center.z,8,10,10,8,0);level.sendParticles(ParticleTypes.FIREWORK,center.x,center.y,center.z,120,8,8,8,.2);}
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
        Vec3 d=b.subtract(a).normalize();for(int i=0;i<kind.radius;i+=2){Vec3 p=a.add(d.scale(i));if(!l.isLoaded(net.minecraft.core.BlockPos.containing(p)))break;l.sendParticles(type,p.x,kind.center.getY()+.7,p.z,1,.2,.2,.2,0);}
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);tag.putFloat("AimX",entityData.get(AIM_X));tag.putFloat("AimZ",entityData.get(AIM_Z));tag.putInt("Warning",lastWarning);tag.putInt("AtomicPulse",atomicPulse);
        var hits=new net.minecraft.nbt.ListTag();for(UUID id:struck){var entry=new CompoundTag();entry.putUUID("Player",id);hits.add(entry);}tag.put("Struck",hits);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);entityData.set(AIM_X,tag.getFloat("AimX"));entityData.set(AIM_Z,tag.getFloat("AimZ"));lastWarning=tag.contains("Warning")?tag.getInt("Warning"):-1;atomicPulse=tag.contains("AtomicPulse")?tag.getInt("AtomicPulse"):-1;
        struck.clear();for(var entry:tag.getList("Struck",net.minecraft.nbt.Tag.TAG_COMPOUND))if(((CompoundTag)entry).hasUUID("Player"))struck.add(((CompoundTag)entry).getUUID("Player"));
    }
}
