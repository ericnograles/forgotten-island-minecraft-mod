package com.forgottenisland.egg;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import javax.annotation.Nullable;

public final class BakunawaEggBlock extends BaseEntityBlock {
    public static final IntegerProperty HATCH=IntegerProperty.create("hatch",0,3);
    public BakunawaEggBlock(){super(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG).strength(1).lightLevel(s->5).noOcclusion());registerDefaultState(stateDefinition.any().setValue(HATCH,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(HATCH);}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Block.box(3,0,3,13,15,13);}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new BakunawaEggEntity(pos,state);}
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,EggContent.EGG_ENTITY.get(),BakunawaEggEntity::tick);}
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,@Nullable LivingEntity placer,ItemStack stack){
        if(!level.isClientSide&&placer instanceof Player&&level.getBlockEntity(pos) instanceof BakunawaEggEntity egg)egg.setOwner(placer.getUUID());
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof BakunawaEggEntity egg)egg.status(player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
