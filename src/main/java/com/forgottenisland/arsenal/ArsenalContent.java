package com.forgottenisland.arsenal;

import com.forgottenisland.ForgottenIsland;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.RegistryObject;

public final class ArsenalContent {
    public static final RegistryObject<Item> PRISMATIC_CRYSTAL=ForgottenIsland.ITEMS.register("prismatic_crystal",()->new Item(new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> SAPPHIRE=ForgottenIsland.ITEMS.register("sapphire_soul_sword",()->new ArsenalSwordItem(ArsenalSwordItem.Power.SAPPHIRE));
    public static final RegistryObject<Item> PHOENIX=ForgottenIsland.ITEMS.register("phoenix_sword",()->new ArsenalSwordItem(ArsenalSwordItem.Power.PHOENIX));
    public static final RegistryObject<Item> HELMET=armor("crystal_helmet",ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> CHESTPLATE=armor("crystal_chestplate",ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> LEGGINGS=armor("crystal_leggings",ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> BOOTS=armor("crystal_boots",ArmorItem.Type.BOOTS);
    public static final RegistryObject<EntityType<PhoenixFireball>> FIREBALL=ForgottenIsland.ENTITIES.register("phoenix_fireball",()->EntityType.Builder.<PhoenixFireball>of(PhoenixFireball::new,MobCategory.MISC).sized(.5f,.5f).clientTrackingRange(8).updateInterval(1).build(ForgottenIsland.ID+":phoenix_fireball"));
    private static RegistryObject<Item> armor(String id,ArmorItem.Type type){return ForgottenIsland.ITEMS.register(id,()->new CrystalArmorItem(type));}
    public static void init(){}
    public static void creative(CreativeModeTab.Output out){for(var item:java.util.List.of(PRISMATIC_CRYSTAL,SAPPHIRE,PHOENIX,HELMET,CHESTPLATE,LEGGINGS,BOOTS))out.accept(item.get());}
    private ArsenalContent(){}
}
