package com.forgottenisland;
import net.minecraftforge.common.ForgeConfigSpec;

public final class IslandConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue COOLDOWN_SECONDS, ECLIPSE_SECONDS;
    public static final ForgeConfigSpec.DoubleValue ABILITY_RANGE, MOUNT_SPEED;
    public static final ForgeConfigSpec.BooleanValue DEFENSE_DAMAGE;
    static {
        var b = new ForgeConfigSpec.Builder();
        COOLDOWN_SECONDS = b.comment("Minimum seconds between owner-requested abilities.").defineInRange("abilityCooldownSeconds", 12, 3, 120);
        ECLIPSE_SECONDS = b.comment("Local eclipse length. Never changes time or weather.").defineInRange("eclipseSeconds", 12, 4, 30);
        ABILITY_RANGE = b.defineInRange("abilityRange", 8.0, 2.0, 16.0);
        MOUNT_SPEED = b.defineInRange("mountSpeedMultiplier", 1.0, .25, 2.0);
        DEFENSE_DAMAGE = b.comment("Whether companions can damage hostile mobs while defending. No terrain destruction or fire.").define("defenseDamage", true);
        SPEC = b.build();
    }
}
