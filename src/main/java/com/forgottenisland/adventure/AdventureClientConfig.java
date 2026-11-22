package com.forgottenisland.adventure;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AdventureClientConfig {
    public enum Cinematics { FULL, REDUCED, OFF }
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.EnumValue<Cinematics> CINEMATICS;
    static {
        var b=new ForgeConfigSpec.Builder();
        CINEMATICS=b.comment("FULL: moving cinematic camera. REDUCED: your own camera, titles only. OFF: ordinary play throughout. K skips your current scene; O cycles this setting.")
            .defineEnum("cinematics",Cinematics.FULL);
        SPEC=b.build();
    }
}
