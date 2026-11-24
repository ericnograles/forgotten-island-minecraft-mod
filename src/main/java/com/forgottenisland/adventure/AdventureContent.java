package com.forgottenisland.adventure;

import com.forgottenisland.ForgottenIsland;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class AdventureContent {
    public static final ResourceKey<Level> ISLAND = ResourceKey.create(Registries.DIMENSION,ForgottenIsland.id("forgotten_island"));
    public static final ResourceKey<Level> HIGHLANDS=ResourceKey.create(Registries.DIMENSION,ForgottenIsland.id("highlands"));
    private static final DeferredRegister<com.mojang.serialization.Codec<? extends net.minecraft.world.level.chunk.ChunkGenerator>> GENERATORS=DeferredRegister.create(Registries.CHUNK_GENERATOR,ForgottenIsland.ID);
    static { GENERATORS.register("mountains",()->MountainGenerator.CODEC); GENERATORS.register("lunar_craters", () -> LunarGenerator.CODEC); }
    public static final ResourceKey<Level> MOON = ResourceKey.create(Registries.DIMENSION, ForgottenIsland.id("moon"));
    public static final RegistryObject<Item> MOON_ROCKET = ForgottenIsland.ITEMS.register("moon_rocket", MoonRocketItem::new);
    public static final RegistryObject<EntityType<MoonRocket>> ROCKET = ForgottenIsland.ENTITIES.register("moon_rocket",
        () -> EntityType.Builder.<MoonRocket>of(MoonRocket::new, MobCategory.MISC).sized(1.5f, 2.4f)
            .clientTrackingRange(10).updateInterval(1).fireImmune().build("forgottenisland:moon_rocket"));
    public static final ResourceKey<Level> STOMACH = ResourceKey.create(Registries.DIMENSION,ForgottenIsland.id("bakunawa_interior"));
    public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,ForgottenIsland.ID);
    public static final RegistryObject<Block> PAROL_STONE=BLOCKS.register("parol_stone",()->new StarFrameBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS).strength(2).lightLevel(s->7)));
    public static final RegistryObject<Block> STAR_PORTAL=BLOCKS.register("star_portal",()->new StarPortalBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_PORTAL).strength(-1,3600000).lightLevel(s->15).noCollission().noLootTable()));
    public static final RegistryObject<Block> PAROL=BLOCKS.register("parol_lantern",()->new Block(BlockBehaviour.Properties.copy(Blocks.SEA_LANTERN).strength(1).lightLevel(s->15)));
    public static final RegistryObject<Block> TISSUE=BLOCKS.register("stomach_tissue",()->new Block(BlockBehaviour.Properties.copy(Blocks.NETHER_WART_BLOCK).strength(-1,3600000).lightLevel(s->5).noLootTable()));
    public static final RegistryObject<Block> STOMACH_CRYSTAL=BLOCKS.register("stomach_crystal",()->new Block(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK).strength(.8f,3600000).lightLevel(s->15).noLootTable()));
    public static final RegistryObject<Item> PAROL_ITEM=ForgottenIsland.ITEMS.register("parol_stone",ParolStoneItem::new);
    public static final RegistryObject<Item> STAR_NECKLACE=ForgottenIsland.ITEMS.register("star_necklace",StarNecklaceItem::new);
    public static final RegistryObject<Item> LANTERN_ITEM=ForgottenIsland.ITEMS.register("parol_lantern",()->new BlockItem(PAROL.get(),new Item.Properties()));
    public static final RegistryObject<SoundEvent> FESTIVAL=ForgottenIsland.SOUNDS.register("festival_ambience",()->SoundEvent.createVariableRangeEvent(ForgottenIsland.id("festival_ambience")));
    public static final RegistryObject<SoundEvent> BOSS_WARNING=sound("boss_warning"),SHRIEK=sound("manang_shriek"),BOSS_IMPACT=sound("boss_impact"),MOON_DEVOUR=sound("moon_devour"),BOSS_RELEASE=sound("boss_release"),WEAKPOINT=sound("weakpoint");
    public static final RegistryObject<SoundEvent> CAVE_AMBIENCE=sound("cave_ambience"),MOUNTAIN_AMBIENCE=sound("mountain_ambience"),COAST_AMBIENCE=sound("coast_ambience");
    public static final RegistryObject<SoundEvent> BAKUNAWA_ROAR=sound("bakunawa_roar");
    public static final RegistryObject<EntityType<IslandBoss>> MANANG_BOSS=boss(BossKind.MANANG),BAKUNAWA_BOSS=boss(BossKind.BAKUNAWA);
    public static final java.util.List<RegistryObject<Item>> BOSS_EGGS=java.util.List.of(
        ForgottenIsland.ITEMS.register("bakunawa_boss_spawn_egg",()->new BossEggItem(BossKind.BAKUNAWA,false)),
        ForgottenIsland.ITEMS.register("phase_two_bakunawa_boss_spawn_egg",()->new BossEggItem(BossKind.BAKUNAWA,true)),
        ForgottenIsland.ITEMS.register("manang_boss_spawn_egg",()->new BossEggItem(BossKind.MANANG,false)),
        ForgottenIsland.ITEMS.register("separated_manang_boss_spawn_egg",()->new BossEggItem(BossKind.MANANG,true)));
    private static RegistryObject<SoundEvent> sound(String id){return ForgottenIsland.SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ForgottenIsland.id(id)));}
    private static RegistryObject<EntityType<IslandBoss>> boss(BossKind kind) {
        return ForgottenIsland.ENTITIES.register(kind.id,()->EntityType.Builder.<IslandBoss>of((t,l)->new IslandBoss(t,l,kind),MobCategory.MONSTER)
            .sized(kind==BossKind.MANANG?2.5f:8,kind==BossKind.MANANG?4.8f:8).fireImmune().clientTrackingRange(16).updateInterval(1).build(ForgottenIsland.ID+":"+kind.id));
    }
    public static final RegistryObject<Item> CRYSTAL_SWORD=ForgottenIsland.ITEMS.register("crystal_sword",CrystalSwordItem::new);
    public static final RegistryObject<Item> MANANG_WINGS=ForgottenIsland.ITEMS.register("manang_wings",ManangWingsItem::new);
    public static final RegistryObject<Item> BAKUNAWA_SKULL=ForgottenIsland.ITEMS.register("bakunawa_skull",()->new Item(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
    public static void register(IEventBus bus) { BLOCKS.register(bus); GENERATORS.register(bus); }
    public static void creative(net.minecraft.world.item.CreativeModeTab.Output out) {
        out.accept(STAR_NECKLACE.get());out.accept(PAROL_ITEM.get());out.accept(LANTERN_ITEM.get());
        out.accept(MANANG_WINGS.get());out.accept(CRYSTAL_SWORD.get());out.accept(BAKUNAWA_SKULL.get());
        for(var egg:BOSS_EGGS)out.accept(egg.get());
        com.forgottenisland.arsenal.ArsenalContent.creative(out);
    }
    private AdventureContent() {}
}
