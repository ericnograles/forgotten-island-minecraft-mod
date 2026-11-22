package com.forgottenisland.egg;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.adventure.AdventureContent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;

public final class EggContent {
    public static final RegistryObject<Block> EGG=AdventureContent.BLOCKS.register("bakunawa_egg",BakunawaEggBlock::new);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,ForgottenIsland.ID);
    public static final RegistryObject<BlockEntityType<BakunawaEggEntity>> EGG_ENTITY=ENTITIES.register("bakunawa_egg",()->BlockEntityType.Builder.of(BakunawaEggEntity::new,EGG.get()).build(null));
    public static void register(IEventBus bus){ENTITIES.register(bus);}
    private EggContent(){}
}
