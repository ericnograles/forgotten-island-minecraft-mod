package com.forgottenisland.adventure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;

public final class BossEggItem extends Item {
    final BossKind kind;
    final boolean direct;
    BossEggItem(BossKind kind,boolean direct){super(new Properties().stacksTo(1).rarity(Rarity.EPIC));this.kind=kind;this.direct=direct;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var hit=getPlayerPOVHitResult(level,player,ClipContext.Fluid.SOURCE_ONLY);
        if(hit.getType()!=HitResult.Type.BLOCK)return InteractionResultHolder.pass(player.getItemInHand(hand));
        var result=useOn(new UseOnContext(player,hand,hit));
        return new InteractionResultHolder<>(result,player.getItemInHand(hand));
    }
    @Override public InteractionResult useOn(UseOnContext ctx){
        if(ctx.getLevel().isClientSide)return InteractionResult.SUCCESS;
        if(!(ctx.getPlayer() instanceof ServerPlayer player))return InteractionResult.FAIL;
        ServerLevel level=player.serverLevel();BlockPos clicked=ctx.getClickedPos();
        if(player.isSpectator())return InteractionResult.FAIL;
        if(level.dimension()!=Level.OVERWORLD&&level.dimension()!=AdventureContent.HIGHLANDS&&level.dimension()!=AdventureContent.ISLAND)
            return fail(player,"Use this boss egg in the Overworld or on Forgotten Island.");
        BlockPos center=clicked.above();
        if(kind==BossKind.BAKUNAWA){
            if(!level.getFluidState(clicked).is(FluidTags.WATER))return fail(player,"Aim at the water surface. Bakunawa needs deep open water to the north.");
            center=center.offset(0,0,31);
        }
        var error=placement(level,kind,center);
        if(error!=null)return fail(player,error);
        var data=IslandSavedData.get(player.server);
        if(BossEncounters.overlaps(data,level,kind,center))return fail(player,"Another arena occupies this space. Move beyond its boundary.");
        var arena=data.addTest(kind,level.dimension(),center,direct);
        if(arena==null)return fail(player,"All twelve test arenas are occupied. Finish a fight or leave it for ten seconds.");
        var guests=new java.util.ArrayList<>(level.players().stream().filter(p->p.isAlive()&&!p.isSpectator()&&arena.contains(p.position())).toList());
        if(!guests.contains(player))guests.add(player);
        BossEncounters.start(level,arena,data,guests);
        if(!player.isCreative())ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
    private static InteractionResult fail(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text),false);return InteractionResult.FAIL;}
    static String placement(ServerLevel level,BossKind kind,BlockPos c){
        boolean ocean=kind==BossKind.BAKUNAWA;
        int span=ocean?24:9,front=ocean?18:9,back=ocean?99:9;
        if(c.getY()+42>=level.getMaxBuildHeight()||c.getY()-(ocean?9:1)<level.getMinBuildHeight())return "The arena needs more vertical clearance.";
        for(int x=(c.getX()-span)>>4;x<=(c.getX()+span)>>4;x++)for(int z=(c.getZ()-back)>>4;z<=(c.getZ()+front)>>4;z++)level.getChunk(x,z);
        var at=new BlockPos.MutableBlockPos();
        for(int dx=-span;dx<=span;dx++)for(int dz=-back;dz<=front;dz++){
            at.set(c.getX()+dx,c.getY(),c.getZ()+dz);
            if(!level.getWorldBorder().isWithinBounds(at))return "The fighting space crosses the world border.";
            int top=level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,at.getX(),at.getZ());
            // The beach may stand up to two blocks above the sea. Buildings above the flight path are rejected.
            boolean water=ocean&&dz<=-23&&Math.abs(dx)<=13;
            if(top>c.getY()+(ocean&&dz>12?2:0))return ocean?"Clear sky is needed over the water and southern fighting area. Try a wider bay.":"Find level ground with a clear 19 × 19 space and open sky.";
            if(water){
                int surface=level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,at.getX(),at.getZ())-1;
                if(surface<c.getY()-3||surface>c.getY()-1)return "Bakunawa needs continuous open water. Check the breach lane at "+at.getX()+", "+at.getZ()+" (surface Y"+surface+").";
                for(int y=surface;y>surface-8;y--){
                    at.setY(y);var s=level.getBlockState(at);
                    if(!s.getFluidState().is(FluidTags.WATER)||!s.getCollisionShape(level,at).isEmpty())return "Bakunawa needs eight clear blocks of water below the breach lane.";
                }
            }else if(!ocean){
                at.setY(c.getY()-1);
                if(!level.getBlockState(at).isFaceSturdy(level,at,Direction.UP))return "Manang needs level solid ground across the roost.";
            }
        }
        return null;
    }
}
