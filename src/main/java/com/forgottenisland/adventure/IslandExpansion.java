package com.forgottenisland.adventure;

import com.forgottenisland.entity.Species;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/** Additive revision: preserve every non-baseline column, even in previously explored ocean. */
public final class IslandExpansion {
    private static final Block[] COLORS={Blocks.ORANGE_WOOL,Blocks.CYAN_WOOL,Blocks.MAGENTA_WOOL,Blocks.YELLOW_WOOL,Blocks.LIME_WOOL,Blocks.RED_WOOL};
    public static void initialize(ServerLevel level,LevelChunk chunk){
        if(chunk.getPos().getMaxBlockX()<176||chunk.getPos().getMinBlockX()>1056||chunk.getPos().getMaxBlockZ()< -368||chunk.getPos().getMinBlockZ()>352){
            IslandSavedData.get(level.getServer()).markExpanded(chunk.getPos().toLong());return;
        }
        var w=new Writer(level,chunk);int mx=chunk.getPos().getMinBlockX(),mz=chunk.getPos().getMinBlockZ();
        for(int x=mx;x<mx+16;x++)for(int z=mz;z<mz+16;z++){
            if(!IslandLayout.land(x,z)||!w.writable(x,z))continue;
            int top=IslandLayout.height(x,z);var path=IslandLayout.path(x,z);
            boolean market=IslandLayout.market(x,z),coast=IslandLayout.coast(x,z)<88,mountain=IslandLayout.mountain(x,z)<140;
            Block surface=coast?Blocks.SMOOTH_QUARTZ:market?Blocks.SMOOTH_SANDSTONE:top<=64?Blocks.SAND:mountain?Blocks.MOSS_BLOCK:Blocks.GRASS_BLOCK;
            if(path.distance()<3.3)surface=mountain?Blocks.POLISHED_ANDESITE:Blocks.CUT_SANDSTONE;
            if(coast&&IslandLayout.coast(x,z)>85)surface=Blocks.SEA_LANTERN;
            if(IslandLayout.mountain(x,z)<31)surface=(x+z)%11==0?Blocks.PURPLE_TERRACOTTA:Blocks.DEEPSLATE_TILES;
            for(int y=56;y<top;y++)w.set(x,y,z,y<top-3?Blocks.STONE:Blocks.DIRT);
            w.set(x,top,z,surface);
            // An elongated chamber, side galleries and an open southern tunnel.
            if(IslandLayout.cavern(x,z)||IslandLayout.caveTunnel(x,z)&&top>=75){
                boolean room=IslandLayout.cavern(x,z);int ceiling=room?83+(int)(5*Math.sin(x*.1)*Math.cos(z*.1)):74;
                int floor=room?66:path.y();
                w.set(x,floor,z,Math.floorMod(x*71+z*29,23)==0?Blocks.CALCITE:Blocks.MOSS_BLOCK);
                for(int y=floor+1;y<=ceiling;y++)w.set(x,y,z,Blocks.AIR);
                if(room){
                    for(int y=ceiling+1;y<=Math.max(ceiling+4,top);y++)w.set(x,y,z,y==top?Blocks.MOSS_BLOCK:Blocks.TUFF);
                    if(Math.floorMod(x*71+z*29,89)==0){w.set(x,ceiling,z,Blocks.GLOWSTONE);w.set(x,67,z,Blocks.SMALL_AMETHYST_BUD);}
                    if(Math.floorMod(x*13+z*7,431)==0)for(int y=67;y<ceiling;y++)w.set(x,y,z,Blocks.DRIPSTONE_BLOCK);
                    else if(Math.floorMod(x*97+z*43,79)==0)for(int y=ceiling-2;y<=ceiling;y++)w.set(x,y,z,Blocks.DRIPSTONE_BLOCK);
                } else {
                    for(int y=ceiling+1;y<=Math.max(ceiling+3,top);y++)w.set(x,y,z,Blocks.TUFF);
                }
            }
            if(!market&&!coast&&!IslandLayout.cavern(x,z)&&path.distance()>9&&IslandLayout.mountain(x,z)>34){
                if(Math.floorMod(x*739+z*313,137)==0)w.set(x,top+1,z,Blocks.FERN);
                if(Math.floorMod(x*317+z*47,257)==0)w.set(x,top+1,z,Blocks.AZALEA);
            }
            // Trail lamps and stone waymarkers on both verges; no endless empty corridors.
            if(path.distance()>3.5&&path.distance()<5&&!coast&&IslandLayout.mountain(x,z)>35&&Math.floorMod(x+z,19)==0){
                for(int y=top+1;y<=top+3;y++)w.set(x,y,z,Blocks.MOSSY_COBBLESTONE_WALL);
                w.set(x,top+4,z,AdventureContent.PAROL.get());
            }
        }
        // Include neighboring tree centers so fronds continue across chunk boundaries.
        for(int x=mx-4;x<mx+20;x++)for(int z=mz-4;z<mz+20;z++)
            if(IslandLayout.land(x,z)&&!IslandLayout.market(x,z)&&IslandLayout.coast(x,z)>=88&&!IslandLayout.cavern(x,z)
                &&IslandLayout.path(x,z).distance()>9&&IslandLayout.mountain(x,z)>34&&Math.floorMod(x*29+z*131,653)==0)
                palm(w,x,IslandLayout.height(x,z)+1,z,IslandLayout.mountain(x,z)<140);
        for(int row=0;row<4;row++)for(int side:new int[]{-1,1})for(int lane=0;lane<2;lane++)
            stall(w,310+side*(18+lane*30),29+row*24,COLORS[(row+lane*2+(side==1?1:0))%6]);
        // Festival stage, woven canopy and long strings of parol lights.
        for(int x=294;x<=326;x++)for(int z=4;z<=15;z++)w.set(x,65,z,Blocks.JUNGLE_PLANKS);
        for(int x=250;x<=370;x+=10)for(int z:new int[]{16,112}){
            for(int y=65;y<74;y++)w.set(x,y,z,Blocks.STRIPPED_JUNGLE_LOG);
            w.set(x,74,z,AdventureContent.PAROL.get());
        }
        for(int z:new int[]{16,112})for(int x=250;x<=370;x++){
            w.set(x,75,z,Blocks.OAK_FENCE);if(x%4==0)w.set(x,74,z,COLORS[Math.floorMod(x/4,6)]);
        }
        sign(w,246,65,65,"FESTIVAL QUARTER","16 food & craft stalls","West: old harbor","East: three trails");
        sign(w,397,65,35,"TRAIL JUNCTION","↑ Twilight Mountain","→ Moonbreak Coast","↓ Batiba's cave");
        sign(w,475,67,136,"MEMORY HOLLOW","Batiba lives within","Follow violet lights","Look in side alcoves");
        sign(w,535,105,-177,"TWILIGHT SUMMIT","Manang's roost","Independent challenge","Retreat down lanterns");
        sign(w,820,65,26,"MOONBREAK COAST","Bakunawa's sanctuary","Bite / tentacles / beam","Crystals free the eaten");
        // Memory alcove, resting ledges and an obtainable exploration cache.
        for(int x=459;x<=465;x++)for(int z=199;z<=207;z++)w.set(x,67,z,Blocks.MOSSY_STONE_BRICKS);
        sign(w,462,68,201,"A MEMORY REMAINS","Listen. Look. Remember.","Every friend matters.","Batiba's resting place");
        cache(w,463,68,205,Items.GLOW_BERRIES,12);
        cache(w,550,105,-231,Items.HONEY_BOTTLE,3);
        cache(w,672,69,39,Items.BREAD,8);
        // A sea arch and jetty give the coastal approach a recognizable silhouette.
        for(int x=676;x<=700;x++)for(int z=36;z<=42;z++)w.set(x,68,z,Blocks.JUNGLE_PLANKS);
        for(int x:new int[]{706,724})for(int y=64;y<83;y++)for(int z=-13;z<=-9;z++)w.set(x,y,z,Blocks.PRISMARINE_BRICKS);
        for(int x=706;x<=724;x++)for(int z=-13;z<=-9;z++)w.set(x,83+(int)(3*Math.sin((x-706)*Math.PI/18)),z,Blocks.PRISMARINE_BRICKS);
        for(int i=0;i<12;i++){
            if(i==3)continue; // Keep the southern ascent open.
            double a=i*Math.PI/6;int x=535+(int)(Math.cos(a)*31),z=-215+(int)(Math.sin(a)*31);
            for(int y=105;y<110;y++)w.set(x,y,z,Blocks.POLISHED_BLACKSTONE_BRICKS);
            w.set(x,110,z,AdventureContent.PAROL.get());
        }
        for(int i=0;i<24;i++){
            int x=310+(i%2==0?-1:1)*(8+(i/8)*19),z=24+(i/2%4)*24;
            if(w.writable(x,z))IslandWorld.resident(level,chunk,100+i,Species.values()[new int[]{1,5,6,3,9,10,11,12}[i%8]],new BlockPos(x,65,z));
        }
        if(w.writable(475,199))IslandWorld.resident(level,chunk,150,Species.BATIBA,new BlockPos(475,67,199));
        IslandSavedData.get(level.getServer()).markExpanded(chunk.getPos().toLong());chunk.setUnsaved(true);
    }
    private static void stall(Writer w,int x,int z,Block color){
        for(int dx=-5;dx<=5;dx++)for(int dz=-4;dz<=4;dz++){
            w.set(x+dx,64,z+dz,Blocks.JUNGLE_PLANKS);w.set(x+dx,71+(Math.abs(dx)<3?1:0),z+dz,(dx&1)==0?color:Blocks.WHITE_WOOL);
        }
        for(int dx:new int[]{-5,5})for(int dz:new int[]{-4,4})for(int y=65;y<=71;y++)w.set(x+dx,y,z+dz,Blocks.BAMBOO_BLOCK);
        for(int dx=-3;dx<=3;dx++){w.set(x+dx,65,z,Blocks.BARREL);if(dx%2==0)w.set(x+dx,66,z,(x+z)%2==0?Blocks.MELON:Blocks.CAKE);}
        w.set(x,70,z,AdventureContent.PAROL.get());
    }
    private static void palm(Writer w,int x,int y,int z,boolean mountain){
        for(int h=0;h<8;h++)w.set(x,y+h,z,mountain?Blocks.SPRUCE_LOG:Blocks.JUNGLE_LOG);
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)if(Math.abs(dx)<=1||Math.abs(dz)<=1)
            w.state(x+dx,y+7+(Math.abs(dx)+Math.abs(dz)<3?1:0),z+dz,(mountain?Blocks.SPRUCE_LEAVES:Blocks.JUNGLE_LEAVES).defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
    }
    private static void sign(Writer w,int x,int y,int z,String... lines){
        if(!w.writable(x,z))return;w.set(x,y,z,Blocks.OAK_SIGN);
        if(w.level.getBlockEntity(new BlockPos(x,y,z)) instanceof SignBlockEntity s){
            var text=new SignText();for(int i=0;i<4;i++)text=text.setMessage(i,Component.literal(lines[i]));s.setText(text,true);s.setText(text,false);s.setWaxed(true);
        }
    }
    private static void cache(Writer w,int x,int y,int z,Item item,int count){
        if(!w.writable(x,z))return;w.set(x,y-1,z,Blocks.CHISELED_STONE_BRICKS);w.set(x,y,z,Blocks.BARREL);
        if(w.level.getBlockEntity(new BlockPos(x,y,z)) instanceof BarrelBlockEntity b)b.setItem(13,new ItemStack(item,count));
    }
    static final class Writer {
        final ServerLevel level;final ChunkPos chunk;final boolean[] clear=new boolean[256];
        Writer(ServerLevel level,LevelChunk chunk){
            this.level=level;this.chunk=chunk.getPos();
            // Compare the entire editable height, not just the surface: underwater and sky builds count.
            for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++){
                int x=this.chunk.getMinBlockX()+dx,z=this.chunk.getMinBlockZ()+dz;boolean pristine=x>=176;
                for(int y=0;pristine&&y<level.getMaxBuildHeight();y++){
                    var state=chunk.getBlockState(new BlockPos(x,y,z));
                    pristine=y==0?state.is(Blocks.BEDROCK):y<56?state.is(Blocks.STONE):y<63?state.is(Blocks.WATER)&&state.getFluidState().isSource():state.isAir();
                }
                clear[dx*16+dz]=pristine;
                if(!pristine&&IslandLayout.land(x,z))IslandSavedData.get(level.getServer()).protectColumn(x,z);
            }
        }
        boolean writable(int x,int z){return x>>4==chunk.x&&z>>4==chunk.z&&clear[(x&15)*16+(z&15)];}
        void set(int x,int y,int z,Block b){state(x,y,z,b.defaultBlockState());}
        void state(int x,int y,int z,BlockState s){if(writable(x,z))level.setBlock(new BlockPos(x,y,z),s,2|16);}
    }
    private IslandExpansion(){}
}
