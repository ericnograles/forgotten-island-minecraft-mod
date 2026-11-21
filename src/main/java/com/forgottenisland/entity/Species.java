package com.forgottenisland.entity;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum Species {
    BAKUNAWA("bakunawa", "Bakunawa", 2.8f, 2.3f, 100, 0x183e64, 0xf5d079, true, true, Items.PRISMARINE_CRYSTALS, "Moon guardian", "Moon-devouring eclipse", "Roar"),
    RAWW("raww", "Raww", .8f, 1.9f, 30, 0xb77b42, 0xc63e4d, false, false, Items.COOKED_BEEF, "Trail scout", "Pack transformation", "Pack transformation"),
    MANANG("manang", "Manang", .8f, 2.2f, 40, 0x563b70, 0xe6bb45, true, false, Items.SWEET_BERRIES, "Aerial ward", "Moonlit separation", "Moonlit separation"),
    SARIMANOK("sarimanok", "Sarimanok", 1.4f, 2.3f, 40, 0xc73939, 0xf5ca54, true, true, Items.WHEAT_SEEDS, "Sky explorer", "Festival wingbeat", "Festival wingbeat"),
    DATU("datu", "Datu", 1.6f, 3.7f, 70, 0x79553d, 0x51964e, false, true, Items.OAK_SAPLING, "Grove guardian", "Root guard", "Root guard"),
    DUWENDE("duwende", "Duwende", .6f, .9f, 20, 0xd65d46, 0xffe4b3, false, false, Items.RED_MUSHROOM, "Tiny healer", "Mushroom jubilee", "Mushroom jubilee"),
    BUNGI("bungi", "Bungi", 1.3f, 2.8f, 50, 0xae79b0, 0xf6d8a8, false, false, Items.MELON_SLICE, "Playful protector", "Giggle stomp", "Giggle stomp"),
    BATIBA("batiba", "Batiba", 1.1f, 1.7f, 30, 0x9d75ab, 0x82f5d7, false, false, Items.GLOW_BERRIES, "Memory lantern", "Memory bloom", "Memory bloom"),
    MERMAHN("mermahn", "Prince Mermahn", .9f, 1.9f, 30, 0x2b9a9a, 0xefc091, false, false, Items.TROPICAL_FISH, "Reef companion", "Tidal serenade", "Tidal serenade"),
    ZIPPY("zippy", "Zippy the Potion Fairy", .45f, .8f, 18, 0x75d39d, 0xaa49b5, true, false, Items.SUGAR, "Potion helper", "Bubbling remedy", "Bubbling remedy"),
    TIKBALANG("tikbalang", "Tikbalang", .85f, 2.7f, 36, 0x87634e, 0xe4c791, false, false, Items.APPLE, "Trail trickster", "Misdirection dance", "Misdirection dance"),
    SARANGAY("sarangay", "Sarangay", 1.7f, 3.0f, 65, 0x874975, 0xe3b15a, false, false, Items.AMETHYST_SHARD, "Jewel guardian", "Jewel aegis", "Jewel aegis"),
    CHEF("flamingchef", "Flaming Aswang Chef", .95f, 2.3f, 36, 0x563d58, 0xffb445, false, false, Items.GLOWSTONE_DUST, "Hearth companion", "Sizzling supper", "Sizzling supper");

    public final String id, displayName, role, ability, secondary;
    public final float width, height;
    public final int health, primaryColor, accentColor;
    public final boolean flies, mount;
    public final Item gift;
    Species(String id, String name, float width, float height, int health, int primary, int accent,
            boolean flies, boolean mount, Item gift, String role, String ability, String secondary) {
        this.id=id; this.displayName=name; this.width=width; this.height=height; this.health=health;
        this.primaryColor=primary; this.accentColor=accent; this.flies=flies; this.mount=mount;
        this.gift=gift; this.role=role; this.ability=ability; this.secondary=secondary;
    }
    public boolean aquatic() { return this == BAKUNAWA || this == MERMAHN; }
    public boolean hasVariants() { return this == DATU || this == DUWENDE; }
    public String variantName(int variant) {
        return switch(this) {
            case DATU -> variant == 0 ? "Datu" : "Grove Kapre";
            case DUWENDE -> variant == 0 ? "King Truffle Wuffle" : "Duwende Warrior";
            default -> displayName;
        };
    }
}
