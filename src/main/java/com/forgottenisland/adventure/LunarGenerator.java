package com.forgottenisland.adventure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/** Impact rims and ejecta above the dimension's continuous basalt and tuff bed. */
public final class LunarGenerator extends FlatLevelSource {
    static final int BASIN_SPACING = 256;
    public static final Codec<LunarGenerator> CODEC = FlatLevelGeneratorSettings.CODEC.fieldOf("settings")
        .xmap(LunarGenerator::new, LunarGenerator::settings).codec();
    public LunarGenerator(FlatLevelGeneratorSettings settings) { super(settings); }
    @Override protected Codec<LunarGenerator> codec() { return CODEC; }

    @Override public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState random, ChunkAccess chunk) {
        var at = new BlockPos.MutableBlockPos();
        var pos = chunk.getPos();
        for (int x=pos.getMinBlockX(); x<=pos.getMaxBlockX(); x++) for (int z=pos.getMinBlockZ(); z<=pos.getMaxBlockZ(); z++) {
            double radius=Math.hypot(Math.floorMod(x+BASIN_SPACING/2,BASIN_SPACING)-BASIN_SPACING/2,z);
            double rim=11*Math.exp(-Math.pow((radius-54)/8,2));
            double hills=7+6*MountainTerrain.noise(x,z,27)+5*Math.sin(x*.039)*Math.cos(z*.048);
            double outside=BakunawaMoves.smooth((radius-42)/32);
            int top=64+(int)Math.round(rim+hills*outside);
            double dust=MountainTerrain.noise(x,z,8)+.18*MountainTerrain.noise(x,z,2);
            for (int y=64;y<=top;y++) {
                var block=y<top?Blocks.TUFF:dust>.46?Blocks.CALCITE:dust<-.4?Blocks.TUFF:Blocks.ANDESITE;
                chunk.setBlockState(at.set(x,y,z),block.defaultBlockState(),false);
            }
        }
    }
}
