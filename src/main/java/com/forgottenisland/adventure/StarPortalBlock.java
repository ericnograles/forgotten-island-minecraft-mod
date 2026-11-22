package com.forgottenisland.adventure;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

public final class StarPortalBlock extends Block {
    public StarPortalBlock(Properties p) {super(p);}
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c) {return box(0,1,0,16,3,16);}
    @Override public void entityInside(BlockState state,Level level,BlockPos pos,Entity entity) {
        if(entity instanceof ServerPlayer player && player.getY()<pos.getY()+.85 && player.getDeltaMovement().y<=0 && !player.isPassenger())
            IslandTravel.queue(player,pos);
    }
    @Override public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random) {
        if(random.nextInt(6)==0)level.addParticle(ParticleTypes.END_ROD,pos.getX()+random.nextDouble(),pos.getY()+.2,pos.getZ()+random.nextDouble(),0,.025,0);
    }
}
