package com.forgottenisland;

import com.forgottenisland.entity.IslandCreature;
import com.forgottenisland.entity.Species;
import com.forgottenisland.item.GuideItem;
import com.forgottenisland.network.IslandNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.registries.*;
import java.util.EnumMap;

@Mod(ForgottenIsland.ID)
public final class ForgottenIsland {
    public static final String ID = "forgottenisland";
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ID);
    public static final RegistryObject<SimpleParticleType> MEMORY_MOTE = PARTICLES.register("memory_mote",()->new SimpleParticleType(false));
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final EnumMap<Species, RegistryObject<EntityType<IslandCreature>>> TYPES = new EnumMap<>(Species.class);
    public static final EnumMap<Species, RegistryObject<Item>> EGGS = new EnumMap<>(Species.class);
    public static final EnumMap<Species, RegistryObject<SoundEvent>> CALLS = new EnumMap<>(Species.class);
    public static final RegistryObject<Item> GUIDE = ITEMS.register("creature_guide", GuideItem::new);
    public static final RegistryObject<Item> WHISTLE = ITEMS.register("friendship_whistle", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHOWCASE = ITEMS.register("showcase_wand", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    static {
        for (Species s : Species.values()) {
            var type = ENTITIES.register(s.id, () -> {
                var builder=EntityType.Builder.<IslandCreature>of((t,l) -> new IslandCreature(t,l,s), MobCategory.CREATURE)
                    .sized(s.width,s.height).clientTrackingRange(12).updateInterval(2);
                if(s==Species.CHEF) builder.fireImmune();
                return builder.build(ID+":"+s.id);
            });
            TYPES.put(s,type);
            EGGS.put(s, ITEMS.register(s.id+"_spawn_egg", () -> new ForgeSpawnEggItem(type,s.primaryColor,s.accentColor,new Item.Properties())));
            CALLS.put(s, SOUNDS.register(s.id+"_call", () -> SoundEvent.createVariableRangeEvent(id(s.id+"_call"))));
        }
        TABS.register("wild_companions", () -> CreativeModeTab.builder().title(Component.translatable("tab.forgottenisland"))
            .icon(() -> EGGS.get(Species.BAKUNAWA).get().getDefaultInstance()).displayItems((p,o) -> {
                o.accept(GUIDE.get()); o.accept(WHISTLE.get()); o.accept(SHOWCASE.get());
                for (Species s: Species.values()) o.accept(EGGS.get(s).get());
            }).build());
    }
    public ForgottenIsland() {
        var bus=FMLJavaModLoadingContext.get().getModEventBus();
        ENTITIES.register(bus); ITEMS.register(bus); SOUNDS.register(bus); TABS.register(bus); PARTICLES.register(bus);
        bus.addListener(this::attributes);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, IslandConfig.SPEC);
        IslandNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(ShowcaseCommands::register);
    }
    private void attributes(EntityAttributeCreationEvent event) {
        TYPES.forEach((s,t) -> event.put(t.get(), IslandCreature.attributes(s).build()));
    }
    public static ResourceLocation id(String path) { return new ResourceLocation(ID,path); }
}
