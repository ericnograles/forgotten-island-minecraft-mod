package com.forgottenisland.adventure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import java.util.*;
import java.util.concurrent.*;

/** Only writes the supplied protochunk, before lighting, saving and player edits. */
public final class MountainGenerator extends ChunkGenerator {
    public static final Codec<MountainGenerator> CODEC=BiomeSource.CODEC.fieldOf("biome_source")
        .xmap(MountainGenerator::new,MountainGenerator::getBiomeSource).codec();
    public MountainGenerator(BiomeSource source){super(source);}
    @Override protected Codec<MountainGenerator> codec(){return CODEC;}
    @Override public int getSeaLevel(){return MountainTerrain.SEA+1;}
    @Override public int getGenDepth(){return 320;}
    @Override public int getMinY(){return 0;}
    private record Column(int top,int roof,int floor,Block surface,boolean cascade,boolean hollow,boolean lamp){
        Column(int x,int z,int top,int roof){
            this(top,roof,MountainTerrain.caveFloor(x,z,roof),surface(x,z,top),
                top>85&&!MountainTerrain.hub(x,z)&&Math.abs(Math.sin(x*.012+z*.019))<.02&&MountainTerrain.river(x,z)<49, IslandLayout.cavern(x,z),Math.floorMod(MountainTerrain.hash(x,z),37)==0);
        }
        static Column at(int x,int z){int h=MountainTerrain.height(x,z);return new Column(x,z,h,MountainTerrain.caveRoof(x,z,h));}
        private static Block surface(int x,int z,int y){
            if(MountainTerrain.hub(x,z)){
                if(IslandLayout.mountain(x,z)<31)return (x+z)%11==0?Blocks.PURPLE_TERRACOTTA:Blocks.DEEPSLATE_TILES;
                if(IslandLayout.coast(x,z)<77&&y>=63){
                    if(Math.abs(x-820)<=10&&z>=-24&&z<=-9)return Blocks.SMOOTH_QUARTZ;
                    double r=IslandLayout.coast(x,z);
                    if(Math.abs(r-36)<.75||Math.abs(r-13)<.65)return Blocks.SEA_LANTERN;
                    if(Math.abs(x-820)<4||Math.abs(z+65)<2)return Blocks.PRISMARINE_BRICKS;
                    return r<26?Blocks.SMOOTH_QUARTZ:Blocks.SAND;
                }
                if(IslandLayout.path(x,z).distance()<3.5)return Blocks.CUT_SANDSTONE;
                if(IslandLayout.market(x,z))return Blocks.SMOOTH_SANDSTONE;
            }
            if(y<65)return Blocks.SAND;
            if(y>201)return Blocks.SNOW_BLOCK;
            if(y>154&&MountainTerrain.noise(x,z,34)>.02)return Blocks.ANDESITE;
            return Blocks.GRASS_BLOCK;
        }
        Block block(int y){
            if(y==0)return Blocks.BEDROCK;
            if(y>top)return y<=MountainTerrain.SEA||cascade&&y==top+1?Blocks.WATER:Blocks.AIR;
            if(y>floor&&y<=roof)return y<=MountainTerrain.SEA?Blocks.WATER:Blocks.AIR;
            if(hollow){
                if(y==roof+1)return lamp?Blocks.SHROOMLIGHT:Blocks.AMETHYST_BLOCK;
                if(y==floor)return lamp?Blocks.CALCITE:Blocks.MOSS_BLOCK;
                if(y>=floor-2&&y<roof+4)return Blocks.TUFF;
            }
            if(y==top)return surface;
            if(y>=top-2&&surface==Blocks.GRASS_BLOCK)return Blocks.DIRT;
            return Blocks.STONE;
        }
    }
    @Override public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor,Blender blender,RandomState random,StructureManager structures,ChunkAccess chunk){
        return CompletableFuture.supplyAsync(()->{
            var pos=chunk.getPos();
            for(int x=0;x<16;x++)for(int z=0;z<16;z++){
                var col=Column.at(pos.getMinBlockX()+x,pos.getMinBlockZ()+z);
                for(int y=0;y<=Math.max(MountainTerrain.SEA,col.top+1);y++){
                    Block block=col.block(y);
                    if(block!=Blocks.AIR)chunk.getSection(chunk.getSectionIndex(y)).setBlockState(x,y&15,z,block.defaultBlockState(),false);
                }
                if(col.cascade)chunk.markPosForPostprocessing(new BlockPos(pos.getMinBlockX()+x,col.top+1,pos.getMinBlockZ()+z));
            }
            Heightmap.primeHeightmaps(chunk,EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG,Heightmap.Types.OCEAN_FLOOR_WG));
            return chunk;
        },executor);
    }
    @Override public void buildSurface(WorldGenRegion region,StructureManager structures,RandomState random,ChunkAccess chunk){
        var pos=chunk.getPos();
        // Place each verge light in its own chunk and leave the traversable center clear.
        if(IslandExpansion.settlementChunk(pos)){
            for(int x=pos.getMinBlockX();x<=pos.getMaxBlockX();x++)for(int z=pos.getMinBlockZ();z<=pos.getMaxBlockZ();z++){
                var road=IslandLayout.path(x,z);
                if(road.distance()<4||road.distance()>5.5||Math.floorMod(x+z,22)!=0||IslandLayout.market(x,z)||IslandLayout.coast(x,z)<60||IslandLayout.cavern(x,z)||IslandLayout.mountain(x,z)<35)continue;
                int y=MountainTerrain.height(x,z);
                for(int n=1;n<=3;n++)chunk.setBlockState(new BlockPos(x,y+n,z),Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState(),false);
                chunk.setBlockState(new BlockPos(x,y+4,z),AdventureContent.PAROL.get().defaultBlockState(),false);
            }
        }
        int cellX=Math.floorDiv(pos.x,12),cellZ=Math.floorDiv(pos.z,12),bits=MountainTerrain.hash(cellX,cellZ);
        if(Math.floorMod(pos.x,12)==((bits>>>3)&7)+2&&Math.floorMod(pos.z,12)==((bits>>>9)&7)+2){
            int cx=pos.getMinBlockX()+8,cz=pos.getMinBlockZ()+8,y=MountainTerrain.height(cx,cz);
            if(!MountainTerrain.hub(cx,cz)&&y>70&&y<205){
                for(int dx=-4;dx<=4;dx++)for(int dz=-1;dz<=1;dz++){
                    int base=MountainTerrain.height(cx+dx,cz+dz);
                    for(int by=base+1;by<=y+8;by++)if(Math.abs(dx)>=3||by>=y+7)
                        chunk.setBlockState(new BlockPos(cx+dx,by,cz+dz),Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),false);
                }
                chunk.setBlockState(new BlockPos(cx,y+6,cz),AdventureContent.PAROL.get().defaultBlockState(),false);
            }
        }
        for(int gx=(pos.getMinBlockX()-3)>>3;gx<=(pos.getMaxBlockX()+3)>>3;gx++)
            for(int gz=(pos.getMinBlockZ()-3)>>3;gz<=(pos.getMaxBlockZ()+3)>>3;gz++){
                int h=MountainTerrain.hash(gx,gz),x=gx*8+(h&7),z=gz*8+((h>>>3)&7);
                if((h&64)==0||MountainTerrain.hub(x,z)&&(IslandLayout.market(x,z)||IslandLayout.path(x,z).distance()<8||IslandLayout.coast(x,z)<93||IslandLayout.mountain(x,z)<38))continue;
                int y=MountainTerrain.height(x,z);
                if(y<66||y>193||MountainTerrain.noise(x,z,110)<-.46)continue;
                int crown=y+5+((h>>>8)&3);
                for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
                    int bx=x+dx,bz=z+dz;
                    if((bx>>4)!=pos.x||(bz>>4)!=pos.z)continue;
                    int distance=Math.abs(dx)+Math.abs(dz);
                    for(int by=y+1;by<=crown+3;by++){
                        boolean trunk=distance==0&&by<=crown;
                        if(!trunk&&(by<crown-2||distance+Math.max(0,by-crown)>4))continue;
                        var at=new BlockPos(bx,by,bz);
                        if(chunk.getBlockState(at).isAir())chunk.setBlockState(at,trunk?(y>125?Blocks.SPRUCE_LOG:Blocks.JUNGLE_LOG).defaultBlockState():
                            (y>125?Blocks.SPRUCE_LEAVES:Blocks.JUNGLE_LEAVES).defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),false);
                    }
                }
            }
    }
    @Override public int getBaseHeight(int x,int z,Heightmap.Types type,LevelHeightAccessor level,RandomState state){
        var c=Column.at(x,z);
        for(int y=Math.max(MountainTerrain.SEA,c.top+1);y>=0;y--)if(type.isOpaque().test(c.block(y).defaultBlockState()))return y+1;
        return 0;
    }
    @Override public NoiseColumn getBaseColumn(int x,int z,LevelHeightAccessor level,RandomState state){
        var c=Column.at(x,z);var blocks=new BlockState[getGenDepth()];
        for(int y=0;y<blocks.length;y++)blocks[y]=c.block(y).defaultBlockState();
        return new NoiseColumn(0,blocks);
    }
    @Override public void applyCarvers(WorldGenRegion region,long seed,RandomState random,BiomeManager biomes,StructureManager structures,ChunkAccess chunk,GenerationStep.Carving step){}
    @Override public void spawnOriginalMobs(WorldGenRegion region){}
    @Override public void addDebugScreenInfo(List<String> lines,RandomState state,BlockPos pos){lines.add("Festival highlands · surface "+MountainTerrain.height(pos.getX(),pos.getZ()));}
}
