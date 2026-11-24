package com.forgottenisland.client;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.adventure.AdventureClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** A personal vision at respawn, also visible from underground beds and daylight spawns. */
public final class MoonriseScreen extends Screen {
    private static final ResourceLocation SKY = ForgottenIsland.id("textures/gui/moonrise_sky.png");
    private static final ResourceLocation SERPENT = ForgottenIsland.id("textures/gui/moonrise_serpent.png");
    private static final ResourceLocation MOON = ForgottenIsland.id("textures/entity/encounter_moon.png");
    private int age;
    private MoonriseScreen() { super(Component.literal("Bakunawa pursues the Moon")); }
    public static void show() {
        var mc=Minecraft.getInstance();
        if (mc.player!=null && AdventureClientConfig.CINEMATICS.get()==AdventureClientConfig.Cinematics.FULL)
            mc.setScreen(new MoonriseScreen());
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void tick() { if (++age >= 124 || minecraft.player==null || !minecraft.player.isAlive()) onClose(); }
    @Override public boolean keyPressed(int key,int scan,int mods) {
        if (BossPresentation.SKIP.matches(key,scan)) { onClose(); return true; }
        return super.keyPressed(key,scan,mods);
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial) {
        float t=Mth.clamp((age+partial)/124f,0,1);
        int h=Math.min(height,(int)(width*.56)), y=(height-h)/2;
        g.fill(0,0,width,height,0xff080e20);
        g.blit(SKY,0,y,width,h,0,0,480,270,480,270);
        int moonSize=(int)(h*.34),mx=(int)(width*.76),my=y+(int)(h*.22);
        g.blit(MOON,mx-moonSize/2,my-moonSize/2,moonSize,moonSize,0,0,64,64,64,64);
        float ease=t*t*(3-2*t);
        int sw=(int)(width*(.37-.17*ease)),sh=sw*106/160;
        int x=(int)(width*(.07+.69*ease))-sw/2;
        int sy=y+(int)(h*(.63-.40*ease)+Math.sin(t*8)*h*.02)-sh/2;
        g.blit(SERPENT,x,sy,sw,sh,0,0,160,106,160,106);
        g.fill(0,height-35,width,height,0xf0121424);
        g.drawCenteredString(font,t<.56?"Bakunawa rises beyond the clouds.":"It means to swallow the Moon.",width/2,height-27,0xffffe6ac);
        g.drawCenteredString(font,"Use your Moon Rocket to follow   ·   K to skip",width/2,height-14,0xffa7cddb);
    }
}
