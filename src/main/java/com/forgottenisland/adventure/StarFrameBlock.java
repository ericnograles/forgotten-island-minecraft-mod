package com.forgottenisland.adventure;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class StarFrameBlock extends Block {
    public StarFrameBlock(Properties p) {super(p);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos center,Player player,InteractionHand hand,BlockHitResult hit) {
        if(!player.getItemInHand(hand).is(Items.GLOW_INK_SAC)) return InteractionResult.PASS;
        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(level.dimension()!=Level.OVERWORLD) return fail(player,"Build the entrance in the Overworld. The island already has a home portal.");
        for(var c:StarPattern.FRAME) if(!level.getBlockState(center.offset(c.x(),0,c.z())).is(AdventureContent.PAROL_STONE.get()))
            return fail(player,"The five-point frame is incomplete. Follow the field guide's north-facing floor plan: 27 border stones + 1 center.");
        for(var c:StarPattern.OPENING) {
            BlockPos pos=center.offset(c.x(),0,c.z());
            if(!level.getWorldBorder().isWithinBounds(pos)||!player.mayUseItemAt(pos,hit.getDirection(),player.getItemInHand(hand))) return InteractionResult.FAIL;
            if(!pos.equals(center)&&!level.getBlockState(pos).canBeReplaced()) return fail(player,"Clear the star's opening at floor level, leaving only the center stone.");
            if(!level.getBlockState(pos.above()).canBeReplaced()||!level.getBlockState(pos.above(2)).canBeReplaced()) return fail(player,"Clear two blocks above the star before lighting it.");
        }
        IslandSavedData.get(((ServerLevel)level).getServer()).registerPortal(center);
        for(var c:StarPattern.OPENING) level.setBlockAndUpdate(center.offset(c.x(),0,c.z()),AdventureContent.STAR_PORTAL.get().defaultBlockState());
        if(!player.isCreative()) player.getItemInHand(hand).shrink(1);
        level.playSound(null,center,SoundEvents.END_PORTAL_SPAWN,SoundSource.BLOCKS,.7f,1.45f);
        player.displayClientMessage(Component.literal("The star remembers this place. Jump into its light. Your return is linked to this portal."),false);
        return InteractionResult.CONSUME;
    }
    private InteractionResult fail(Player player,String text) {player.displayClientMessage(Component.literal(text),true);return InteractionResult.FAIL;}
}
