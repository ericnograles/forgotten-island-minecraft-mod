package com.forgottenisland.adventure;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import java.util.*;

/** Formation is cosmetic until a second, atomic placement validation succeeds. */
public final class StarNecklaceItem extends Item {
    public static final List<StarPattern.Cell> CELLS;
    static {
        String[] plan={"   #   ","  ###  ","#######"," ##### ","  ###  "," ## ## "," #   # "};
        var cells=new ArrayList<StarPattern.Cell>();
        for(int z=0;z<plan.length;z++)for(int x=0;x<7;x++)if(plan[z].charAt(x)=='#')cells.add(new StarPattern.Cell(x-3,z-3));
        CELLS=List.copyOf(cells);
    }
    private record Formation(ServerPlayer player, BlockPos center, ItemStack necklace, long start) {}
    private static final Map<UUID,Formation> FORMING=new HashMap<>();
    public StarNecklaceItem(){super(new Properties().durability(64).rarity(Rarity.UNCOMMON));}
    @Override public InteractionResult useOn(UseOnContext c) {
        if(c.getLevel().isClientSide)return InteractionResult.SUCCESS;
        if(!(c.getPlayer() instanceof ServerPlayer p))return InteractionResult.FAIL;
        if(p.level().dimension()!=Level.OVERWORLD||c.getClickedFace()!=Direction.UP)
            return fail(p,"Use the necklace on the top of dry, solid ground in the Overworld.");
        if(FORMING.containsKey(p.getUUID())||p.getCooldowns().isOnCooldown(this))return InteractionResult.FAIL;
        var level=p.serverLevel();var floor=c.getClickedPos();
        if(!IslandTravel.safe(level,floor.above()))return fail(p,"Choose dry ground with space to stand above it.");
        // One raised layer clears small slopes without excavating terrain or requiring a platform.
        BlockPos center=null;
        for(int rise=1;rise<=2;rise++)if(placementError(p,floor.above(rise),c.getItemInHand())==null){center=floor.above(rise);break;}
        if(center==null)return fail(p,"The star needs open air within 3 blocks. Move away from walls, trees or water; nothing was changed.");
        FORMING.put(p.getUUID(),new Formation(p,center,c.getItemInHand(),level.getGameTime()));
        p.getCooldowns().addCooldown(this,60);
        level.playSound(null,center,SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.PLAYERS,1,.7f);
        p.displayClientMessage(Component.literal("The necklace is drawing a star. Stay nearby, then jump down into its light."),true);
        return InteractionResult.CONSUME;
    }
    static String placementError(ServerPlayer p,BlockPos center,ItemStack stack) {
        var level=p.serverLevel();
        for(var c:CELLS)for(int dy=0;dy<=2;dy++) {
            var pos=center.offset(c.x(),dy,c.z());
            if(!level.isInWorldBounds(pos)||!level.getWorldBorder().isWithinBounds(pos)||!level.mayInteract(p,pos)||!p.mayUseItemAt(pos,Direction.UP,stack))return "This land is protected.";
            var state=level.getBlockState(pos);
            if(!state.canBeReplaced()||!level.getFluidState(pos).isEmpty()||level.getBlockEntity(pos)!=null)return "Something moved into the star. Choose an open spot.";
        }
        return null;
    }
    public static void tick(MinecraftServer server) {
        var it=FORMING.values().iterator();
        while(it.hasNext()) {
            var f=it.next();var p=f.player;if(p.server!=server)continue;
            if(p.hasDisconnected()||!p.isAlive()||p.level().dimension()!=Level.OVERWORLD||p.distanceToSqr(f.center.getCenter())>144
                ||!(p.getMainHandItem()==f.necklace||p.getOffhandItem()==f.necklace)){it.remove();continue;}
            int t=(int)(p.serverLevel().getGameTime()-f.start);
            for(int n=0;n<Math.min(CELLS.size(),1+t);n++) {
                if(t%3!=0)break;var c=CELLS.get(n);
                p.serverLevel().sendParticles(ParticleTypes.END_ROD,f.center.getX()+c.x()+.5,f.center.getY()+.2,f.center.getZ()+c.z()+.5,1,.12,.03,.12,0);
            }
            if(t<40)continue;
            it.remove();String error=placementError(p,f.center,f.necklace);
            if(error!=null){fail(p,error+" Necklace charge preserved.");continue;}
            var data=IslandSavedData.get(server);data.registerNecklacePortal(f.center);
            for(var c:CELLS)p.serverLevel().setBlockAndUpdate(f.center.offset(c.x(),0,c.z()),AdventureContent.STAR_PORTAL.get().defaultBlockState());
            f.necklace.hurtAndBreak(1,p,q->q.broadcastBreakEvent(q.getUsedItemHand()));
            p.serverLevel().playSound(null,f.center,SoundEvents.END_PORTAL_SPAWN,SoundSource.BLOCKS,.65f,1.5f);
            p.displayClientMessage(Component.literal("Star ready! Jump into the light. Your own way home will be remembered."),false);
        }
    }
    public static void clear(MinecraftServer server){FORMING.values().removeIf(f->f.player.server==server);}
    private static InteractionResult fail(ServerPlayer p,String s){p.displayClientMessage(Component.literal(s),true);return InteractionResult.FAIL;}
    @Override public void appendHoverText(ItemStack s,Level l,List<Component> lines,TooltipFlag f){
        lines.add(Component.literal("Use on ground · forms a luminous floor star"));
        lines.add(Component.literal("No frame or flat platform needed · 64 formations"));
        lines.add(Component.literal("Jump down into the light to travel"));
    }
}
