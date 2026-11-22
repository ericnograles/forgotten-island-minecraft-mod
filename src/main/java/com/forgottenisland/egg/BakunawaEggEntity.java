package com.forgottenisland.egg;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.entity.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.UUID;

public final class BakunawaEggEntity extends BlockEntity {
    public static final int HATCH_TICKS=1200,GROWTH_TICKS=24000;
    private UUID owner;
    private int incubated;
    public BakunawaEggEntity(BlockPos pos,BlockState state){super(EggContent.EGG_ENTITY.get(),pos,state);}
    public void setOwner(UUID id){owner=id;setChanged();}
    public int progress(){return incubated;}
    public UUID owner(){return owner;}
    public void status(Player p){
        if(owner==null){owner=p.getUUID();setChanged();}
        p.displayClientMessage(Component.literal(incubated<HATCH_TICKS?"Bakunawa egg: "+((HATCH_TICKS-incubated+19)/20)+" seconds until hatching":"Ready to hatch! Clear space around the egg."),true);
    }
    public static void tick(Level level,BlockPos pos,BlockState state,BakunawaEggEntity egg){
        if(!(level instanceof ServerLevel server))return;
        egg.incubated=Math.min(HATCH_TICKS,egg.incubated+1);
        if(egg.incubated%20==0)egg.setChanged();
        int hatch=Math.min(3,egg.incubated/300);
        if(state.getValue(BakunawaEggBlock.HATCH)!=hatch){
            level.setBlock(pos,state.setValue(BakunawaEggBlock.HATCH,hatch),3);
            level.playSound(null,pos,SoundEvents.TURTLE_EGG_CRACK,SoundSource.BLOCKS,.7f,.7f+hatch*.15f);
        }
        if(level.getGameTime()%40==0)server.sendParticles(ParticleTypes.ELECTRIC_SPARK,pos.getX()+.5,pos.getY()+.8,pos.getZ()+.5,5,.35,.4,.35,.02);
        if(egg.incubated<HATCH_TICKS||level.getGameTime()%20!=0)return;
        var baby=ForgottenIsland.TYPES.get(Species.BAKUNAWA).get().create(server);if(baby==null)return;
        baby.hatch(egg.owner);baby.setPos(pos.getX()+.5,pos.getY()+1.05,pos.getZ()+.5);
        if(!level.noCollision(baby)||!level.getWorldBorder().isWithinBounds(baby.getBoundingBox()))return;
        if(!server.addFreshEntity(baby))return;
        level.removeBlock(pos,false);
        server.sendParticles(ParticleTypes.HEART,baby.getX(),baby.getY()+.5,baby.getZ(),12,.5,.4,.5,.03);
        level.playSound(null,pos,SoundEvents.TURTLE_EGG_HATCH,SoundSource.BLOCKS,1,.8f);
    }
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.putInt("Incubated",incubated);if(owner!=null)tag.putUUID("Owner",owner);}
    @Override public void load(CompoundTag tag){super.load(tag);incubated=Math.max(0,Math.min(HATCH_TICKS,tag.getInt("Incubated")));owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;}
}
