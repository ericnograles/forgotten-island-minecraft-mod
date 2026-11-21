package com.forgottenisland.client;

import com.forgottenisland.*;
import com.forgottenisland.entity.*;
import com.forgottenisland.network.IslandNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid=ForgottenIsland.ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ClientHooks {
    public static final KeyMapping ABILITY=new KeyMapping("key.forgottenisland.ability",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_R,"key.categories.forgottenisland");
    public static final KeyMapping ROAR=new KeyMapping("key.forgottenisland.roar",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G,"key.categories.forgottenisland");
    public static final KeyMapping DESCEND=new KeyMapping("key.forgottenisland.descend",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_C,"key.categories.forgottenisland");
    public static void openGuide() {Minecraft.getInstance().setScreen(new CreatureGuideScreen());}
    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(ABILITY);e.register(ROAR);e.register(DESCEND);}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){for(Species s:Species.values())e.registerLayerDefinition(CreatureModel.layer(s),()->CreatureModel.bake(s));}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){ForgottenIsland.TYPES.forEach((s,t)->e.registerEntityRenderer(t.get(),c->new CreatureRenderer(c,s)));}
    @SubscribeEvent public static void particles(RegisterParticleProvidersEvent e){e.registerSpriteSet(ForgottenIsland.MEMORY_MOTE.get(),MemoryMoteParticle.Provider::new);}
    @SubscribeEvent public static void overlays(RegisterGuiOverlaysEvent e){e.registerAboveAll("companion",(gui,g,partial,w,h)->Events.overlay(g,w,h,partial));}

    @Mod.EventBusSubscriber(modid=ForgottenIsland.ID,value=Dist.CLIENT)
    public static final class Events {
        private static float eclipse;
        private static float eclipseProgress;
        @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
            if(event.phase!=TickEvent.Phase.END)return;
            var mc=Minecraft.getInstance();
            if(mc.level==null||mc.player==null){eclipse=0;return;}
            if(mc.screen==null) {
                IslandCreature target=mc.player.getVehicle() instanceof IslandCreature c?c:mc.hitResult instanceof EntityHitResult hit&&hit.getEntity() instanceof IslandCreature c?c:null;
                while(ABILITY.consumeClick())if(target!=null)IslandNetwork.CHANNEL.sendToServer(new IslandNetwork.Request(target.getId(),false));
                while(ROAR.consumeClick())if(target!=null)IslandNetwork.CHANNEL.sendToServer(new IslandNetwork.Request(target.getId(),true));
            }
            if(mc.player.getVehicle() instanceof IslandCreature c && mc.player.tickCount%3==0) {
                int v=mc.screen==null?(mc.options.keyJump.isDown()?1:0)-(DESCEND.isDown()?1:0):0;
                c.setRideInput(v);IslandNetwork.CHANNEL.sendToServer(new IslandNetwork.Ride(v));
            }
            float strongest=0,progress=0;
            for(var e:mc.level.entitiesForRendering())if(e instanceof IslandCreature c&&c.species==Species.BAKUNAWA&&c.isAlive()&&c.activeTicks()>0&&c.abilityKind()==1&&c.distanceToSqr(mc.player)<96*96) {
                float duration=IslandConfig.ECLIPSE_SECONDS.get()*20f;
                float t=1-c.activeTicks()/duration;
                float strength=Math.min(1,Math.min(t*6,(1-t)*6));
                if(strength>strongest){strongest=strength;progress=t;}
            }
            eclipse=Mth.clamp(strongest,0,1);eclipseProgress=progress;
        }
        @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor event) {
            if(eclipse<=0)return;
            event.setRed(event.getRed()*(1-eclipse*.7f));event.setGreen(event.getGreen()*(1-eclipse*.78f));event.setBlue(event.getBlue()*(1-eclipse*.4f));
        }
        static void overlay(GuiGraphics g,int w,int h,float partial) {
            var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui)return;
            if(eclipse>0) {
                int alpha=(int)(eclipse*90);
                g.fillGradient(0,0,w,h,(alpha<<24)|0x10142c,((alpha/3)<<24)|0x302650);
                int cx=w/2,cy=55,r=23;
                disc(g,cx,cy,r,((int)(eclipse*255)<<24)|0xf9e8b4);
                int shadow=cx-55+(int)(eclipseProgress*110);
                disc(g,shadow,cy,r+2,((int)(eclipse*255)<<24)|0x15162e);
                g.drawCenteredString(mc.font,"THE MOON REMEMBERS",cx,cy+34,0xffeddbb6);
            }
            IslandCreature c=mc.player.getVehicle() instanceof IslandCreature ride?ride:mc.hitResult instanceof EntityHitResult hit&&hit.getEntity() instanceof IslandCreature look?look:null;
            if(c==null)return;
            String text=c.getName().getString()+"  •  "+(c.isTame()?(c.isOrderedToSit()?"Stay":"Follow")+" / "+(c.defending()?"Defend":"Passive"):"Gift: "+c.species.gift.getDescription().getString()+" "+c.friendship()+"/3");
            int width=mc.font.width(text)+24;
            String hint=c.cooldown()>0?"Ability ready in "+((c.cooldown()+19)/20)+"s":"R • "+c.species.ability+(c.species==Species.BAKUNAWA?"   G • Roar":"");
            width=Math.max(width,mc.font.width(hint)+24);
            g.fill(w/2-width/2, h-116,w/2+width/2,h-83,0xd9162430);
            g.drawCenteredString(mc.font,text,w/2,h-110,0xfff7dfb2);
            if(c.isOwnedBy(mc.player))g.drawCenteredString(mc.font,hint,w/2,h-96,0xff9be2d0);
        }
        private static void disc(GuiGraphics g,int cx,int cy,int r,int color) {
            for(int y=-r;y<=r;y++){int x=(int)Math.sqrt(r*r-y*y);g.fill(cx-x,cy+y,cx+x+1,cy+y+1,color);}
        }
    }
}
