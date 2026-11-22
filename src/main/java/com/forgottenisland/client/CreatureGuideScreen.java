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
    private int panelHeight(){return Math.min(320,height-12);}
    public CreatureGuideScreen(){super(Component.literal("The Wild Companion Field Guide"));}
    @Override protected void init() {
        int left=width/2-170,top=(height-panelHeight())/2,bottom=top+panelHeight()-25;
        addRenderableWidget(Button.builder(Component.literal("←"),b->{page=(page+Species.values().length+ADVENTURE_PAGES-1)%(Species.values().length+ADVENTURE_PAGES);preview=null;}).bounds(left+10,bottom,32,20).build());
        addRenderableWidget(Button.builder(Component.literal("→"),b->{page=(page+1)%(Species.values().length+ADVENTURE_PAGES);preview=null;}).bounds(left+298,bottom,32,20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(left+140,bottom,60,20).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
        renderBackground(g);
        int left=width/2-170,top=(height-panelHeight())/2;
        g.fill(left-2,top-2,left+342,top+panelHeight()+2,0xffd4b47c);
        g.fill(left,top,left+340,top+panelHeight(),0xff152d39);
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
        String[] titles={"01 / THE HOMEWARD STAR NECKLACE","02 / FORM THE STAR","03 / SAFE PASSAGE","04 / FOUR ISLAND DESTINATIONS","05 / MANANG'S TWILIGHT SUMMIT","06 / BAKUNAWA'S MOONBREAK COAST","07 / INSIDE THE DEVOURER","08 / REWARDS & SHARED ADVENTURES"};
        String[] pages={
            "Craft a Homeward Star Necklace:\n\nS . S    S = string (2)\n. C .    C = copper ingot (1)\n. T .    T = torch (1)\n\nUse it on the top of dry Overworld ground. A luminous floor star forms in two seconds. No frame, compass direction, amethyst or glow ink is needed.\n\nEach necklace makes 64 stars. Craft this field guide with a book and a feather.",
            "Choose open ground with three blocks of space around you. Small slopes work; the star can float one block higher. No large flat platform is required.\n\nStay near the star and keep the necklace in hand while it forms. Jump down into the light when it is ready.\n\nWalls, trees, water and protected land block placement. If something obstructs formation, no charge is spent and no blocks change.\n\nOld Parol Stone stars remain usable with glow ink.",
            "The arrival star returns each traveler to their own last Overworld entrance. Travel has a five-second cooldown.\n\nIf home is blocked, the star finds dry ground nearby, then at world spawn. Death uses your normal Overworld bed or spawn.\n\nEmergency recovery: /island home, including from the stomach. Use crystals for a normal escape.\n\nBeds and anchors cannot change island respawn. Your home link survives logout and server restart.",
            "From the old arrival harbor, follow the eastern shore to the larger festival at X 310, Z 65. Greet hosts, browse 16 stalls and find the performance stage.\n\nAt the lantern junction: north climbs Twilight Mountain (535, -215); south enters Batiba's Memory Hollow (475, 190); east reaches Moonbreak Coast (820, -65).\n\nLook for cave alcoves, ridge supplies and the sea-arch jetty. Both bosses are independent.\n\nOlder saves retain builds, residents, victories and rewards. Old arenas become historic courts.",
            "Follow the stone steps and lanterns up the northern ridge. Manang waits at the summit, forty blocks above the coast. Bring enchanted armor, a sword, food and healing.\n\nLeave the Shadow Dive circle. Jump over the Shriek ring. Move behind Wing Cut. Strike the lower body when her wings fold.\n\nAt half health, her attacks quicken. Retreat down the mountain at any time. If every fighter leaves or dies for ten seconds, the fight resets.\n\nVictory earns Manang Wings: equip in the chest slot, jump again while falling to glide.",
            "Bite and Eat: leave the amber circle before the head lunges and jaws snap. A connected bite swallows you.\n\nTentacles: leave the five blue ground circles. The limbs rise, slam and withdraw. A strike deals 7 damage before armor.\n\nHoly Beam: sidestep the white aim line. Two separate unblocked hits kill a 20-HP player, even with ordinary armor or Protection. Shields, Resistance, absorption, healing and Totems can help.\n\nMoonfall, Lunar Tide and Eclipse Breath still alternate into the fight. Hit gold anchors during recovery.",
            "Inside Bakunawa, explore the rising passage through the ribs. Break all three luminous Memory Crystals. No special pickaxe or boss reward is needed.\n\nThe objective is shared: the last crystal ejects every swallowed fighter safely back into the battle. There is no ordinary timed exit.\n\nSwallowed players stay in the encounter and retain reward eligibility, even when everyone is inside.\n\nDeath, reset, defeat and stale reconnects have recovery safeguards. /island home is an emergency exception. Blocks and teleport items cannot bypass the crystals.",
            "K skips only your scene. O cycles Full / Reduced / Off. Your skip never skips a friend's scene.\n\nBring enchanted diamond armor, a sword, a shield, food and healing. Neither boss reward is required for the other fight.\n\nManang Wings: 648 durability; repair with membranes. Bakunawa drops a skull and the Crystal Sword: 6,000 damage on a charged unenchanted hit, 4,096 durability; repair with netherite.\n\nPersonal rewards wait on the south dais after the defeat scene. Victories and collected rewards persist."
        };
        g.drawString(font,titles[index],left+15,top+45,0xffffdf9a,false);
        text(g,pages[index],left+16,top+64,308);
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
