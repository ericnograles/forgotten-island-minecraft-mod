package com.forgottenisland.adventure;

import com.forgottenisland.ForgottenIsland;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid=ForgottenIsland.ID)
public final class BakunawaStomach {
    public static final BlockPos ENTRY=new BlockPos(0,65,-24);
    public static final List<BlockPos> CRYSTALS=List.of(new BlockPos(-15,67,-12),new BlockPos(15,70,6),new BlockPos(0,73,24));
    private static final Set<UUID> TRANSFERRING=new HashSet<>();
    public static boolean participating(ServerPlayer p,int attempt){
        return p.level().dimension()==AdventureContent.STOMACH&&IslandSavedData.get(p.server).stomach.active(p.getUUID(),attempt);
    }
    public static void swallow(IslandBoss boss,ServerPlayer p){
        var inside=p.server.getLevel(AdventureContent.STOMACH);if(inside==null)return;
        var data=IslandSavedData.get(p.server);var state=data.encounter(BossKind.BAKUNAWA.id);
        if(state.stage()!=EncounterState.Stage.COMBAT||!state.participants().contains(p.getUUID())||data.stomach.contains(p.getUUID()))return;
        boolean fresh=data.stomach.swallow(p.getUUID(),state.attempt());data.setDirty();
        prepare(p.server);
        if(fresh)for(var c:CRYSTALS)inside.setBlockAndUpdate(c,AdventureContent.STOMACH_CRYSTAL.get().defaultBlockState());
        transfer(p,inside,Vec3.atBottomCenterOf(ENTRY).add(Math.min(3,data.stomach.guests().size()-1)*2,0,0));
        p.sendSystemMessage(Component.literal("SWALLOWED · Explore the ribs and break all three luminous crystals to escape together. /island home is emergency recovery only."));
        p.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL,SoundSource.HOSTILE,.7f,.5f);
    }
    static int floor(int z){return 64+Math.max(0,(z+24)/6);}
    public static void prepare(net.minecraft.server.MinecraftServer server){
        var data=IslandSavedData.get(server);var l=server.getLevel(AdventureContent.STOMACH);
        if(!data.stomachBuilt&&l!=null){build(l);data.stomachBuilt=true;data.setDirty();}
    }
    static void build(ServerLevel l){
        for(int x=-24;x<=24;x++)for(int z=-34;z<=34;z++){
            double edge=x*x/(23.0*23)+z*z/(33.0*33);if(edge>1.16)continue;
            int floor=floor(z),roof=85+(int)(7*Math.max(0,1-edge));
            for(int y=62;y<=roof;y++){
                boolean shell=edge>1||y<=floor||y==roof;
                l.setBlock(new BlockPos(x,y,z),shell?AdventureContent.TISSUE.get().defaultBlockState():Blocks.AIR.defaultBlockState(),2|16);
            }
            // Raised organic ribs along the walls leave the center and crystal routes clear.
            if(edge<1&&Math.floorMod(z,9)==0&&Math.abs(x)>18)for(int y=floor+1;y<roof;y++)
                l.setBlock(new BlockPos(x,y,z),AdventureContent.TISSUE.get().defaultBlockState(),2|16);
            if(edge<1&&Math.floorMod(x*17+z*31,107)==0)l.setBlock(new BlockPos(x,roof-1,z),Blocks.SHROOMLIGHT.defaultBlockState(),2|16);
        }
        for(var c:CRYSTALS)l.setBlockAndUpdate(c,AdventureContent.STOMACH_CRYSTAL.get().defaultBlockState());
    }
    public static boolean crystal(ServerPlayer p,BlockPos pos){
        int index=CRYSTALS.indexOf(pos);var data=IslandSavedData.get(p.server);var encounter=data.encounter(BossKind.BAKUNAWA.id);
        if(p.level().dimension()!=AdventureContent.STOMACH||index<0||!data.stomach.breakCrystal(p.getUUID(),index,encounter.attempt()))return false;
        data.setDirty();p.serverLevel().sendParticles(ParticleTypes.END_ROD,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,60,1,1,1,.1);
        p.serverLevel().playSound(null,pos,SoundEvents.AMETHYST_BLOCK_BREAK,SoundSource.BLOCKS,1,.6f);
        int remaining=Integer.bitCount(data.stomach.crystals());
        for(var guest:p.serverLevel().players())guest.sendSystemMessage(Component.literal(remaining==0?"The last crystal shatters. Bakunawa ejects you!":remaining+" crystals remain. Follow the rising floor through the ribs."));
        return true;
    }
    public static void release(net.minecraft.server.MinecraftServer server){var data=IslandSavedData.get(server);if(!data.stomach.guests().isEmpty()){data.stomach.release();data.setDirty();}}
    public static void forget(ServerPlayer p){var data=IslandSavedData.get(p.server);data.stomach.remove(p.getUUID());data.setDirty();}
    static void transfer(ServerPlayer p,ServerLevel target,Vec3 at){
        TRANSFERRING.add(p.getUUID());
        try{p.stopRiding();p.fallDistance=0;p.setDeltaMovement(Vec3.ZERO);p.teleportTo(target,at.x,at.y,at.z,target.dimension()==AdventureContent.STOMACH?0:180,0);p.connection.resetPosition();}
        finally{TRANSFERRING.remove(p.getUUID());}
    }
    private static void eject(ServerPlayer p){
        var island=p.server.getLevel(AdventureContent.ISLAND);if(island==null){IslandTravel.home(p);return;}
        var origin=BossKind.BAKUNAWA.center.offset(0,0,45);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)IslandWorld.initialize(island,island.getChunk((origin.getX()>>4)+x,(origin.getZ()>>4)+z));
        var safe=IslandTravel.safeNear(island,origin);if(safe.isEmpty()){IslandTravel.home(p);return;}
        transfer(p,island,safe.get().add(0,1.5,0));forget(p);
        p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,80,0,false,false));
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,60,4,false,false));
        island.sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+1,p.getZ(),80,2,1,2,.12);
        for(int i=0;i<24;i++)island.sendParticles(ParticleTypes.END_ROD,origin.getX()+.5,origin.getY()+6-i*.15,origin.getZ()-20+i*.85,2,.2,.2,.2,.02);
        p.playNotifySound(SoundEvents.ENDER_DRAGON_FLAP,SoundSource.HOSTILE,1,.7f);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p)||!p.isAlive())return;
        var data=IslandSavedData.get(p.server);var s=data.encounter(BossKind.BAKUNAWA.id);
        boolean interior=p.level().dimension()==AdventureContent.STOMACH;
        if(data.stomach.contains(p.getUUID())){
            if(!data.stomach.active(p.getUUID(),s.attempt())||s.stage()!=EncounterState.Stage.COMBAT){eject(p);return;}
            if(!interior){forget(p);return;}
        } else if(interior){eject(p);return;}else return;
        if(p.getY()<63||p.getY()>96||Math.pow(p.getX()/23,2)+Math.pow(p.getZ()/33,2)>1.03)transfer(p,p.serverLevel(),Vec3.atBottomCenterOf(ENTRY));
        if(p.tickCount%80==0){
            p.displayClientMessage(Component.literal("INSIDE BAKUNAWA · "+Integer.bitCount(data.stomach.crystals())+" crystals remain · follow the rising floor"),true);
            p.playNotifySound(SoundEvents.WARDEN_HEARTBEAT,SoundSource.AMBIENT,.5f,.65f);
        }
    }
    @SubscribeEvent public static void breaking(BlockEvent.BreakEvent e){
        if(e.getLevel() instanceof ServerLevel l&&l.dimension()==AdventureContent.STOMACH&&e.getPlayer() instanceof ServerPlayer p){
            if(!e.getState().is(AdventureContent.STOMACH_CRYSTAL.get())||!crystal(p,e.getPos()))e.setCanceled(true);
        }
    }
    @SubscribeEvent public static void placing(BlockEvent.EntityPlaceEvent e){if(e.getLevel() instanceof ServerLevel l&&l.dimension()==AdventureContent.STOMACH)e.setCanceled(true);}
    @SubscribeEvent public static void useBlock(PlayerInteractEvent.RightClickBlock e){if(e.getLevel().dimension()==AdventureContent.STOMACH)e.setCanceled(true);}
    @SubscribeEvent public static void teleport(EntityTeleportEvent e){
        // Operator commands remain available for recovery and assisted testing; teleport items do not.
        if(e instanceof EntityTeleportEvent.TeleportCommand&&e.getEntity() instanceof ServerPlayer p&&p.hasPermissions(2))return;
        if(e.getEntity().level().dimension()==AdventureContent.STOMACH)e.setCanceled(true);
    }
    @SubscribeEvent public static void dimension(EntityTravelToDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.level().dimension()==AdventureContent.STOMACH&&!TRANSFERRING.contains(p.getUUID()))e.setCanceled(true);}
    @SubscribeEvent public static void died(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)forget(p);}
    private BakunawaStomach(){}
}
