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
    private static final String RETURN="IslandSwallowReturn";
    private static String id(ServerPlayer p){
        String value=p.getPersistentData().getCompound(RETURN).getString("Arena");
        return value.isEmpty()?BossKind.BAKUNAWA.id:value;
    }
    public static boolean participating(ServerPlayer p,int attempt){return participating(p,BossKind.BAKUNAWA.id,attempt);}
    static boolean participating(ServerPlayer p,String encounter,int attempt){
        return p.level().dimension()==AdventureContent.STOMACH&&id(p).equals(encounter)&&IslandSavedData.get(p.server).interior(encounter).active(p.getUUID(),attempt);
    }
    public static void swallow(IslandBoss boss,ServerPlayer p){
        var inside=p.server.getLevel(AdventureContent.STOMACH);if(inside==null||p.isCreative()||p.isSpectator())return;
        var data=IslandSavedData.get(p.server);String id=boss.encounterId();var arena=data.arena(id);if(arena==null)return;
        var state=data.encounter(id);var stomach=data.interior(id);
        if(state.stage()!=EncounterState.Stage.COMBAT||!state.participants().contains(p.getUUID())||stomach.contains(p.getUUID()))return;
        var marker=new net.minecraft.nbt.CompoundTag();marker.putString("Arena",id);marker.putString("Level",p.level().dimension().location().toString());
        marker.putLong("Feet",p.blockPosition().asLong());marker.putLong("Shore",boss.center().offset(0,0,12).asLong());p.getPersistentData().put(RETURN,marker);
        boolean fresh=stomach.swallow(p.getUUID(),state.attempt());data.setDirty();
        int dx=arena.slot()*96;
        if(arena.slot()==0)prepare(p.server);
        else if(!data.builtRooms.contains(arena.slot())){build(inside,dx);data.builtRooms.add(arena.slot());data.setDirty();}
        if(fresh)for(var c:CRYSTALS)inside.setBlockAndUpdate(c.offset(dx,0,0),AdventureContent.STOMACH_CRYSTAL.get().defaultBlockState());
        transfer(p,inside,Vec3.atBottomCenterOf(ENTRY.offset(dx,0,0)).add(Math.min(3,stomach.guests().size()-1)*2,0,0));
        p.sendSystemMessage(Component.literal("SWALLOWED · Explore the ribs and break all three luminous crystals to escape together. /island home is emergency recovery only."));
        p.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL,SoundSource.HOSTILE,.7f,.5f);
    }
    static int floor(int z){return 64+Math.max(0,(z+24)/6);}
    public static void prepare(net.minecraft.server.MinecraftServer server){
        var data=IslandSavedData.get(server);var l=server.getLevel(AdventureContent.STOMACH);
        if(!data.stomachBuilt&&l!=null){build(l);data.stomachBuilt=true;data.setDirty();}
    }
    static void build(ServerLevel l){build(l,0);}
    private static void build(ServerLevel l,int dx){
        for(int x=-24;x<=24;x++)for(int z=-34;z<=34;z++){
            double edge=x*x/(23.0*23)+z*z/(33.0*33);if(edge>1.16)continue;
            int floor=floor(z),roof=85+(int)(7*Math.max(0,1-edge));
            for(int y=62;y<=roof;y++){
                boolean shell=edge>1||y<=floor||y==roof;
                l.setBlock(new BlockPos(x+dx,y,z),shell?AdventureContent.TISSUE.get().defaultBlockState():Blocks.AIR.defaultBlockState(),2|16);
            }
            // Raised organic ribs along the walls leave the center and crystal routes clear.
            if(edge<1&&Math.floorMod(z,9)==0&&Math.abs(x)>18)for(int y=floor+1;y<roof;y++)
                l.setBlock(new BlockPos(x+dx,y,z),AdventureContent.TISSUE.get().defaultBlockState(),2|16);
            if(edge<1&&Math.floorMod(x*17+z*31,107)==0)l.setBlock(new BlockPos(x+dx,roof-1,z),Blocks.SHROOMLIGHT.defaultBlockState(),2|16);
        }
        for(var c:CRYSTALS)l.setBlockAndUpdate(c.offset(dx,0,0),AdventureContent.STOMACH_CRYSTAL.get().defaultBlockState());
    }
    public static boolean crystal(ServerPlayer p,BlockPos pos){
        var data=IslandSavedData.get(p.server);String id=id(p);var arena=data.arena(id);if(arena==null)return false;
        int index=CRYSTALS.indexOf(pos.offset(-arena.slot()*96,0,0));var encounter=data.encounter(id);var stomach=data.interior(id);
        if(p.level().dimension()!=AdventureContent.STOMACH||index<0||!stomach.breakCrystal(p.getUUID(),index,encounter.attempt()))return false;
        data.setDirty();p.serverLevel().sendParticles(ParticleTypes.END_ROD,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,60,1,1,1,.1);
        p.serverLevel().playSound(null,pos,SoundEvents.AMETHYST_BLOCK_BREAK,SoundSource.BLOCKS,1,.6f);
        int remaining=Integer.bitCount(stomach.crystals());
        for(var guest:p.serverLevel().players())if(stomach.contains(guest.getUUID()))guest.sendSystemMessage(Component.literal(remaining==0?"The last crystal shatters. Bakunawa ejects you!":remaining+" crystals remain. Follow the rising floor through the ribs."));
        return true;
    }
    public static void release(net.minecraft.server.MinecraftServer server){var data=IslandSavedData.get(server);if(!data.stomach.guests().isEmpty()){data.stomach.release();data.setDirty();}}
    public static void forget(ServerPlayer p){var data=IslandSavedData.get(p.server);String id=id(p);if(data.arena(id)!=null)data.interior(id).remove(p.getUUID());p.getPersistentData().remove(RETURN);data.setDirty();}
    static void transfer(ServerPlayer p,ServerLevel target,Vec3 at){
        TRANSFERRING.add(p.getUUID());
        try{p.stopRiding();p.fallDistance=0;p.setDeltaMovement(Vec3.ZERO);p.teleportTo(target,at.x,at.y,at.z,target.dimension()==AdventureContent.STOMACH?0:180,0);p.connection.resetPosition();}
        finally{TRANSFERRING.remove(p.getUUID());}
    }
    private static void eject(ServerPlayer p){
        var marker=p.getPersistentData().getCompound(RETURN);
        var key=marker.contains("Level")?net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,new net.minecraft.resources.ResourceLocation(marker.getString("Level"))):AdventureContent.ISLAND;
        var island=p.server.getLevel(key);if(island==null){IslandTravel.home(p);return;}
        var origin=marker.contains("Shore")?BlockPos.of(marker.getLong("Shore")):BossKind.BAKUNAWA.center.offset(0,0,45);
        IslandWorld.initialize(island,island.getChunk(origin.getX()>>4,origin.getZ()>>4));
        var safe=IslandTravel.safeNear(island,origin);
        if(safe.isEmpty()&&marker.contains("Feet")){
            var feet=BlockPos.of(marker.getLong("Feet"));safe=IslandTravel.safeNear(island,feet);
            if(safe.isEmpty()){
                var surface=island.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,feet);
                safe=Optional.of(Vec3.atBottomCenterOf(surface.above()));
            }
        }
        if(safe.isEmpty()){IslandTravel.home(p);return;}
        transfer(p,island,safe.get().add(0,1.5,0));forget(p);
        p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,80,0,false,false));
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,60,4,false,false));
        island.sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+1,p.getZ(),80,2,1,2,.12);
        for(int i=0;i<24;i++)island.sendParticles(ParticleTypes.END_ROD,origin.getX()+.5,origin.getY()+6-i*.15,origin.getZ()-20+i*.85,2,.2,.2,.2,.02);
        p.playNotifySound(SoundEvents.ENDER_DRAGON_FLAP,SoundSource.HOSTILE,1,.7f);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p)||!p.isAlive())return;
        var data=IslandSavedData.get(p.server);String id=id(p);var arena=data.arena(id);
        if(arena==null){if(p.level().dimension()==AdventureContent.STOMACH)eject(p);return;}
        var s=data.encounter(id);var stomach=data.interior(id);int dx=arena.slot()*96;
        boolean interior=p.level().dimension()==AdventureContent.STOMACH;
        if(stomach.contains(p.getUUID())){
            if(!stomach.active(p.getUUID(),s.attempt())||(s.stage()!=EncounterState.Stage.COMBAT&&s.stage()!=EncounterState.Stage.TRANSFORM)){eject(p);return;}
            if(!interior){forget(p);return;}
        } else if(interior){eject(p);return;}else return;
        if(p.getY()<63||p.getY()>96||Math.pow((p.getX()-dx)/23,2)+Math.pow(p.getZ()/33,2)>1.03)transfer(p,p.serverLevel(),Vec3.atBottomCenterOf(ENTRY.offset(dx,0,0)));
        if(p.tickCount%80==0){
            p.displayClientMessage(Component.literal("INSIDE BAKUNAWA · "+Integer.bitCount(stomach.crystals())+" crystals remain · follow the rising floor"),true);
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
