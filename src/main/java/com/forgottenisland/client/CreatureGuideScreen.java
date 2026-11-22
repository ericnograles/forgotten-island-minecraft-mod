package com.forgottenisland.client;

import com.forgottenisland.*;
import com.forgottenisland.entity.*;
import com.forgottenisland.adventure.StarPattern;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

public class CreatureGuideScreen extends Screen {
    private int page;
    private static final int ADVENTURE_PAGES=8;
    private IslandCreature preview;
    public CreatureGuideScreen(){super(Component.literal("The Wild Companion Field Guide"));}
    @Override protected void init() {
        int left=width/2-170,top=height/2-112;
        addRenderableWidget(Button.builder(Component.literal("←"),b->{page=(page+Species.values().length+ADVENTURE_PAGES-1)%(Species.values().length+ADVENTURE_PAGES);preview=null;}).bounds(left+10,top+199,32,20).build());
        addRenderableWidget(Button.builder(Component.literal("→"),b->{page=(page+1)%(Species.values().length+ADVENTURE_PAGES);preview=null;}).bounds(left+298,top+199,32,20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(left+140,top+199,60,20).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
        renderBackground(g);
        int left=width/2-170,top=height/2-112;
        g.fill(left-2,top-2,left+342,top+226,0xffd4b47c);
        g.fill(left,top,left+340,top+224,0xff152d39);
        g.fill(left+7,top+7,left+333,top+36,0xff244753);
        g.drawString(font,"NAKALI  /  FIELD NOTES",left+17,top+13,0xffa4dbcc,false);
        g.drawString(font,String.format("%02d / %02d",page+1,Species.values().length+ADVENTURE_PAGES),left+280,top+13,0xffead8b1,false);
        if(page>=Species.values().length){adventure(g,left,top,page-Species.values().length);super.render(g,mx,my,partial);return;}
        Species s=Species.values()[page];
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
    private void adventure(GuiGraphics g,int left,int top,int index) {
        String title=switch(index){case 0->"01 / BUILD A HOMEWARD STAR";case 1->"02 / LIGHT THE STAR";case 2->"03 / SAFE PASSAGE";case 3->"04 / THE FESTIVAL ISLAND";case 4->"05 / MANANG'S TWILIGHT ROOST";case 5->"06 / BAKUNAWA'S MOONBREAK COAST";case 6->"07 / VICTORY REWARDS";default->"08 / A SHARED ADVENTURE";};
        g.drawString(font,title,left+15,top+45,0xffffdf9a,false);
        if(index==0) {
            g.drawString(font,"NORTH (-Z)",left+20,top+65,0xff9be2d0,false);
            for(int z=0;z<StarPattern.PLAN.size();z++)for(int x=0;x<9;x++) {
                char c=StarPattern.PLAN.get(z).charAt(x);
                if(c==' ')continue;
                int px=left+20+x*10,py=top+80+z*10;
                g.fill(px,py,px+9,py+9,c=='#'?0xffdba95f:c=='C'?0xff8be4d7:0xff45506b);
            }
            text(g,"Craft 32 Parol Stone:\nS C S\nC A C\nS C S\nS = cobblestone\nC = copper ingot\nA = amethyst shard\n\nLay 27 gold border stones and the teal center flat. Leave dark cells empty.",left+133,top+65,190);
        } else if(index==1) {
            text(g,"Build one layer above flat solid ground in the Overworld. Clear the opening and two blocks above the star.\n\nOptional: hold 28 Parol Stone and crouch-use the ground beneath the intended center to lay the whole north-facing frame. Failed placement costs nothing.\n\nUse one glow ink sac on the center stone, then jump down into the light.\n\nMaterials: cobblestone, copper, amethyst and glow squid. The pattern fits 9 by 10 blocks. Keep the gold frame intact.",left+16,top+64,308);
        } else if(index==2) {
            text(g,"The arrival star returns each traveler to their own last Overworld entrance. Jump into it to retreat at any time. Travel has a 5-second cooldown.\n\nIf home is blocked or destroyed, the star finds safe ground nearby, then at world spawn.\n\nEmergency retreat: /island home. No operator permission needed. Death keeps your link and uses your normal Overworld respawn.\n\nBeds and respawn anchors cannot activate on the island. Logging out never erases your home link.",left+16,top+64,308);
        } else if(index==3) {
            text(g,"Greet a market host with empty-hand use. Residents stay here; your ordinary companions work as before.\n\nFollow the north boulevard. West: Manang's Twilight Roost. East: Bakunawa's Moonbreak Coast. Both routes are open from arrival.\n\nThe retreat star is behind the arrival dais. No boss reward is needed to leave.\n\nParol Lantern: Parol Stone + torch + paper. The market and your changes persist across reloads.",left+16,top+64,308);
        } else if(index==4) {
            text(g,"West trail: enter the dark circular roost to awaken Manang. Bring enchanted armor, a sword, a bow and food.\n\nLeave the flame circle before Shadow Dive. Jump over the blue Shriek ring. Move behind her when Wing Cut is announced.\n\nHer upper body separates and flies. When the wings fold, strike the exposed lower body for extra damage. Below half health, her attacks quicken.\n\nRetreat south at any time. After all fighters leave or die for 10 seconds, the encounter resets for another attempt.",left+16,top+64,308);
        } else if(index==5) {
            text(g,"East trail: walk into the huge white moon court. This fight is open even before Manang. Bring enchanted armor, a sword and food; wings are optional.\n\nDodge Moonfall circles. Jump the Lunar Tide ring. Step sideways out of Eclipse Breath. At half health the moon darkens and warnings quicken.\n\nThree floating moon anchors stand near the southern head. Hit their gold crystals after an attack; arrows can also strike the body.\n\nVictory opens a permanent homeward star on the south reward dais.",left+16,top+64,308);
        } else if(index==6) {
            text(g,"Manang Wings: chest-slot Elytra gliding. Jump again while falling; fireworks provide thrust.\n\n648 durability, 1 per second of flight. Repair with phantom membrane or Mending. Unequip them normally to wear chest armor.\n\nBakunawa: Crystal Sword and a collectible skull. An ordinary, fully charged, unenchanted sword hit deals 6,000 damage before armor. Critical hits and enchantments can increase it.\n\nThe sword has 4,096 durability. Repair with netherite ingots or Mending.",left+16,top+64,308);
        } else {
            text(g,"K skips only your scene. O cycles Full / Reduced / Off. Reduced keeps your own camera. Late arrivals watch the remaining scene; everyone in the arena is protected during it.\n\nEvery Survival player who enters before the defeat scene gets personal drops on the south dais. Drops never expire and only their owner can collect them. Share normally after pickup.\n\nVictories persist. Empty encounters reset after 10 seconds for another attempt. Starting health scales with the party present when the boss awakens, up to four players. Joining later does not heal the boss.",left+16,top+64,308);
        }
    }
    private void text(GuiGraphics g,String text,int x,int y,int width) {
        for(var line:font.split(Component.literal(text),width)){g.drawString(font,line,x,y,0xffece3cb,false);y+=9;}
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
