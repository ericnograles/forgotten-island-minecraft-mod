package com.forgottenisland.adventure;

import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;

/** Optional construction gesture; uses the same blocks, cost and activation as hand placement. */
public final class ParolStoneItem extends BlockItem {
    public ParolStoneItem() {super(AdventureContent.PAROL_STONE.get(),new Item.Properties());}
    @Override public InteractionResult useOn(UseOnContext context) {
        var player=context.getPlayer();
        if(player==null||!player.isShiftKeyDown())return super.useOn(context);
        var level=context.getLevel();var stack=context.getItemInHand();
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(level.dimension()!=Level.OVERWORLD||context.getClickedFace()!=Direction.UP)
            return fail(context,"Crouch-use the top of solid ground in the Overworld to lay a north-facing star.");
        if(!player.isCreative()&&stack.getCount()<28)return fail(context,"Hold at least 28 Parol Stone in one stack to lay the complete frame.");
        var center=context.getClickedPos().above();
        var cells=new java.util.ArrayList<>(StarPattern.FRAME);cells.addAll(StarPattern.OPENING);
        var state=getBlock().defaultBlockState();
        // Validate the complete footprint before writing or charging any material.
        for(var cell:cells) {
            var pos=center.offset(cell.x(),0,cell.z());
            if(!level.isInWorldBounds(pos.above(2))||!level.getWorldBorder().isWithinBounds(pos)
                ||!level.mayInteract(player,pos)||!player.mayUseItemAt(pos,Direction.UP,stack))
                return fail(context,"The whole star must be inside land you can build on.");
            if(!level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP))
                return fail(context,"The star needs flat solid ground across its 9 by 10 footprint.");
            for(int dy=0;dy<=2;dy++)if(!level.getBlockState(pos.above(dy)).canBeReplaced()||!level.getFluidState(pos.above(dy)).isEmpty())
                return fail(context,"Clear the star's footprint and two blocks above it first.");
            if((StarPattern.FRAME.contains(cell)||pos.equals(center))&&!level.isUnobstructed(state,pos,CollisionContext.empty()))
                return fail(context,"Step out of the frame before laying the star.");
        }
        for(var cell:StarPattern.FRAME)level.setBlockAndUpdate(center.offset(cell.x(),0,cell.z()),state);
        level.setBlockAndUpdate(center,state);
        if(!player.isCreative())stack.shrink(28);
        level.playSound(null,center,SoundEvents.STONE_PLACE,SoundSource.BLOCKS,1,.8f);
        player.displayClientMessage(Component.literal("Star laid facing north. Use one glow ink sac on the center, then jump into the light."),false);
        return InteractionResult.CONSUME;
    }
    private InteractionResult fail(UseOnContext context,String message) {
        context.getPlayer().displayClientMessage(Component.literal(message),true);return InteractionResult.FAIL;
    }
    @Override public void appendHoverText(ItemStack stack,Level level,java.util.List<Component> lines,TooltipFlag flag) {
        super.appendHoverText(stack,level,lines,flag);
        lines.add(Component.literal("Crouch-use ground with 28 to lay a star"));
    }
}
