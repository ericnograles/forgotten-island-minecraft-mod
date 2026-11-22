package com.forgottenisland.arsenal;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import java.util.List;
import javax.annotation.Nullable;

public final class CrystalArmorItem extends ArmorItem {
    public static int strength(Type type){return switch(type){case HELMET->100;case CHESTPLATE->900;case LEGGINGS->250;case BOOTS->69;};}
    public static final ArmorMaterial MATERIAL=new ArmorMaterial(){
        public int getDurabilityForType(Type type){return switch(type){case HELMET->2048;case CHESTPLATE->4096;case LEGGINGS->3584;case BOOTS->1792;};}
        public int getDefenseForType(Type type){return strength(type);}
        public int getEnchantmentValue(){return 20;}
        public SoundEvent getEquipSound(){return SoundEvents.ARMOR_EQUIP_DIAMOND;}
        public Ingredient getRepairIngredient(){return Ingredient.of(ArsenalContent.PRISMATIC_CRYSTAL.get());}
        public String getName(){return "forgottenisland:crystal";}
        public float getToughness(){return 4;}
        public float getKnockbackResistance(){return .1f;}
    };
    public CrystalArmorItem(Type type){super(MATERIAL,type,new Item.Properties().fireResistant().rarity(Rarity.EPIC));}
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){
            @Override public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot slot,net.minecraft.client.model.HumanoidModel<?> original){
                return com.forgottenisland.client.CrystalArmorModel.fitted(slot,original);
            }
        });
    }
    @Override public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){return "forgottenisland:textures/models/armor/crystal_layer_"+(slot==EquipmentSlot.LEGS?2:1)+".png";}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> lines,TooltipFlag flag){
        lines.add(Component.literal("Crystal armor strength: "+strength(getType())));
        lines.add(Component.literal("Uses full strength beyond the normal attribute cap."));
        lines.add(Component.literal("Armor reduces damage by at most 80%; bypass attacks still bypass it."));
        lines.add(Component.literal("Repair with Prismatic Crystals, or Mending."));
    }
}
