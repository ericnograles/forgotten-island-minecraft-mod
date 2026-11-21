package com.forgottenisland.client;

import com.forgottenisland.*;
import com.forgottenisland.entity.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

public class CreatureGuideScreen extends Screen {
    private int page;
    private IslandCreature preview;
    public CreatureGuideScreen(){super(Component.literal("The Wild Companion Field Guide"));}
    @Override protected void init() {
        int left=width/2-170,top=height/2-112;
        addRenderableWidget(Button.builder(Component.literal("←"),b->{page=(page+Species.values().length-1)%Species.values().length;preview=null;}).bounds(left+10,top+199,32,20).build());
        addRenderableWidget(Button.builder(Component.literal("→"),b->{page=(page+1)%Species.values().length;preview=null;}).bounds(left+298,top+199,32,20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(left+140,top+199,60,20).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
        renderBackground(g);
        int left=width/2-170,top=height/2-112;
        Species s=Species.values()[page];
        g.fill(left-2,top-2,left+342,top+226,0xffd4b47c);
        g.fill(left,top,left+340,top+224,0xff152d39);
        g.fill(left+7,top+7,left+333,top+36,0xff244753);
        g.drawString(font,"NAKALI  /  FIELD NOTES",left+17,top+13,0xffa4dbcc,false);
        g.drawString(font,String.format("%02d / %02d",page+1,Species.values().length),left+280,top+13,0xffead8b1,false);
        g.drawString(font,s.displayName,left+142,top+46,0xffffdf9a,false);
        g.drawString(font,s.role,left+142,top+60,0xff98cbbf,false);
        if(preview==null&&minecraft.level!=null){preview=ForgottenIsland.TYPES.get(s).get().create(minecraft.level);}
        if(preview!=null) {
            preview.tickCount=(int)(System.currentTimeMillis()/50%100000);
            int scale=s==Species.BAKUNAWA?14:s==Species.DATU?27:s==Species.SARANGAY?32:40;
            InventoryScreen.renderEntityInInventoryFollowsMouse(g,left+72,top+160,scale,left+72-mx,top+90-my,preview);
        }
        String detail="Befriend: 3 × "+s.gift.getDescription().getString()+"\n\n"+s.ability+"\n"+description(s);
        int y=top+80;
        for(var line:font.split(Component.literal(detail),180)){g.drawString(font,line,left+142,y,0xffece3cb,false);y+=10;}
        String controls=s==Species.DATU?"Ride: empty hand • WASD ground travel • Shift exit"
            :s.mount?"Ride: empty hand • WASD • Space up / C down • Shift exit"
            :"Creative wand: befriend, then use again to demonstrate.";
        g.drawWordWrap(font,Component.literal("Sneak + empty hand: Follow / Stay\nWhistle: ability • Sneak + whistle: Passive / Defend\n"+controls),left+14,top+166,310,0xff9bc4c2);
        super.render(g,mx,my,partial);
    }
    private String description(Species s) {return switch(s){
        case BAKUNAWA->"A local eclipse shields friends. G unleashes a dramatic roar. Flies and swims.";
        case RAWW->"A joyful dog transformation lends friends speed and reveals nearby hostiles.";
        case MANANG->"Her upper body rises on broad wings, weakening hostiles and softening falls.";
        case SARIMANOK->"A colorful gust pushes hostiles away and gives friends a soft landing.";
        case DATU->"A leafy guard grants nearby allies extra resistance.";
        case DUWENDE->"A tiny mushroom jubilee regenerates nearby friends.";
        case BUNGI->"A playful stomp scatters hostiles and helps friends jump.";
        case BATIBA->"Floating memories reveal hostiles and brighten your cave vision.";
        case MERMAHN->"A tidal song gives friends water breathing and swifter swimming.";
        case ZIPPY->"A bubbling remedy removes poison and hunger, and lends friends mining haste.";
        case TIKBALANG->"A playful trail dance slows nearby hostiles and helps friends move quickly.";
        case SARANGAY->"A glittering jewel ward gives nearby friends four temporary extra hearts.";
        case CHEF->"Original gameplay design. A sizzling supper extinguishes allies and wards fire for 10 seconds.";
    };}
    @Override public boolean isPauseScreen(){return false;}
}
