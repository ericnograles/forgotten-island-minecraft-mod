package com.forgottenisland.client;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.adventure.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** Per-client camera ownership. Skipping never sends a state-changing server message. */
@Mod.EventBusSubscriber(modid=ForgottenIsland.ID,value=Dist.CLIENT)
public final class BossPresentation {
    public static final KeyMapping SKIP=new KeyMapping("key.forgottenisland.skip_scene",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_K,"key.categories.forgottenisland");
    public static final KeyMapping MODE=new KeyMapping("key.forgottenisland.scene_mode",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_O,"key.categories.forgottenisland");
    private static final Set<String> skipped=new HashSet<>();
    private static Scene scene;
    private static IslandBoss nearby;
    private static final class Scene {
        final IslandBoss boss;final String key;final ClientLevel level;final ArmorStand camera;
        final Entity previousCamera;final CameraType perspective;final boolean hidden;final float yaw,pitch;
        final AdventureClientConfig.Cinematics mode;
        int elapsed;
        Scene(Minecraft mc,IslandBoss boss) {
            this.boss=boss;key=key(boss);level=mc.level;previousCamera=mc.getCameraEntity();perspective=mc.options.getCameraType();hidden=mc.options.hideGui;yaw=mc.player.getYRot();pitch=mc.player.getXRot();
            mode=AdventureClientConfig.CINEMATICS.get();camera=new ArmorStand(level,mc.player.getX(),mc.player.getY(),mc.player.getZ());camera.setInvisible(true);camera.setNoGravity(true);
            if(mode==AdventureClientConfig.Cinematics.FULL){mc.options.setCameraType(CameraType.FIRST_PERSON);mc.options.hideGui=false;mc.setCameraEntity(camera);}
        }
    }
    private static String key(IslandBoss b){return b.getUUID()+":"+b.attempt()+":"+b.stage();}
    public static void cycleMode() {
        var values=AdventureClientConfig.Cinematics.values();var next=values[(AdventureClientConfig.CINEMATICS.get().ordinal()+1)%values.length];
        AdventureClientConfig.CINEMATICS.set(next);AdventureClientConfig.SPEC.save();
        var mc=Minecraft.getInstance();if(mc.player!=null)mc.player.displayClientMessage(Component.literal("Boss cinematics: "+next+" · K skips the current scene"),true);
        if(scene!=null){skipped.add(scene.key);restore(mc);}
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        while(MODE.consumeClick())cycleMode();
        while(SKIP.consumeClick())if(scene!=null){skipped.add(scene.key);restore(mc);}
        if(mc.level==null||mc.player==null){restore(mc);skipped.clear();nearby=null;return;}
        nearby=null;
        for(var entity:mc.level.entitiesForRendering())if(entity instanceof IslandBoss boss&&boss.inside(mc.player.position())) {
            if(nearby==null||boss.distanceToSqr(mc.player)<nearby.distanceToSqr(mc.player))nearby=boss;
        }
        if(scene!=null&&(mc.screen!=null||scene.level!=mc.level||!mc.player.isAlive()||scene.boss.isRemoved()||scene.boss.awaitingMoonStrike()||!scene.key.equals(key(scene.boss))||!scene.boss.inside(mc.player.position())||++scene.elapsed>Math.max(scene.boss.kind.entranceLength,scene.boss.kind.defeatLength)+40)){skipped.add(scene.key);restore(mc);}
        if(scene==null&&mc.screen==null&&nearby!=null&&mc.player.isAlive()&&!mc.player.isSpectator()
            &&(nearby.stage()==EncounterState.Stage.ENTRANCE||nearby.stage()==EncounterState.Stage.DEFEAT)
            &&!nearby.awaitingMoonStrike()&&AdventureClientConfig.CINEMATICS.get()!=AdventureClientConfig.Cinematics.OFF&&!skipped.contains(key(nearby)))scene=new Scene(mc,nearby);
        if(scene!=null&&scene.mode==AdventureClientConfig.Cinematics.FULL)camera(mc,scene,0);
        if(skipped.size()>128)skipped.clear();
    }
    private static void restore(Minecraft mc) {
        if(scene==null)return;
        if(scene.mode==AdventureClientConfig.Cinematics.FULL) {
            mc.setCameraEntity(scene.previousCamera!=null&&!scene.previousCamera.isRemoved()&&scene.previousCamera.level()==mc.level?scene.previousCamera:mc.player);
            mc.options.setCameraType(scene.perspective);mc.options.hideGui=scene.hidden;
            if(mc.player!=null&&mc.level==scene.level){mc.player.setYRot(scene.yaw);mc.player.setXRot(scene.pitch);}
        }
        scene=null;
    }
    @SubscribeEvent public static void renderTick(TickEvent.RenderTickEvent e) {
        if(e.phase==TickEvent.Phase.START&&scene!=null&&scene.mode==AdventureClientConfig.Cinematics.FULL)camera(Minecraft.getInstance(),scene,e.renderTickTime);
    }
    private static void camera(Minecraft mc,Scene s,float partial) {
        var b=s.boss;boolean entrance=b.stage()==EncounterState.Stage.ENTRANCE;
        double t=Mth.clamp((b.clock()+partial)/(entrance?b.kind.entranceLength:b.kind.defeatLength),0,1),ease=t*t*(3-2*t);
        Vec3 center=Vec3.atBottomCenterOf(b.center()),position,look;
        if(b.kind==BossKind.MANANG) {
            if(entrance){double a=-.9+ease*1.45;position=center.add(Math.sin(a)*(13-ease*4),4.8+ease*2,Math.cos(a)*(13-ease*4));look=center.add(0,3+b.separation()*.5,0);}
            else {position=center.add(8-ease*4,5+ease*2,10+ease*4);look=center.add(0,3-ease,0);}
        } else {
            double clock=b.clock()+partial;var origin=b.origin();
            if(entrance && b.lunar()) {
                double sweep=BakunawaMoves.smooth(clock/MoonPursuit.BITE_TICKS);
                position=center.add(35-17*sweep,13-6*sweep,24);
                look=b.headPoint(partial).add(0,-1,6);
            }else if(entrance&&clock<40){position=origin.add(28,7,35);look=b.headPoint(partial);}
            else if(entrance&&clock<174){
                double reveal=BakunawaMoves.smooth((clock-40)/100);position=origin.add(48-reveal*8,12+reveal*12,85+reveal*15);look=b.headPoint(partial).add(0,-3,0);
            }else if(entrance&&clock<200){
                position=mc.player.getEyePosition().add(mc.player.getLookAngle().multiply(1,0,1).normalize().scale(4)).add(0,.25,0);look=mc.player.getEyePosition().add(0,-.2,0);
            }else if(entrance){position=origin.add(34,18,110);look=b.headPoint(partial);}
            else if(clock<170){position=origin.add(78-ease*6,29+ease*5,26);look=b.bodyPoint(3,partial).add(0,2,-3);}
            else {position=mc.player.position().add(5,3,7);look=mc.player.getEyePosition();}
            if(entrance&&clock<65){double shake=Math.sin(clock*2.3)*.08;position=position.add(shake,Math.cos(clock*1.8)*.05,0);}
        }
        var delta=look.subtract(position);float yaw=(float)(Mth.atan2(-delta.x,delta.z)*180/Math.PI),pitch=(float)(-Mth.atan2(delta.y,delta.horizontalDistance())*180/Math.PI);
        s.camera.setPos(position.x,position.y-s.camera.getEyeHeight(),position.z);s.camera.xo=s.camera.getX();s.camera.yo=s.camera.getY();s.camera.zo=s.camera.getZ();
        s.camera.setYRot(yaw);s.camera.yRotO=yaw;s.camera.setXRot(pitch);s.camera.xRotO=pitch;
        s.camera.setYHeadRot(yaw);s.camera.yHeadRotO=yaw;s.camera.yBodyRot=yaw;s.camera.yBodyRotO=yaw;
        if(mc.getCameraEntity()!=s.camera)mc.setCameraEntity(s.camera);
    }
    @SubscribeEvent public static void input(MovementInputUpdateEvent e) {
        if(scene==null||scene.mode!=AdventureClientConfig.Cinematics.FULL)return;
        var i=e.getInput();i.forwardImpulse=i.leftImpulse=0;i.jumping=i.shiftKeyDown=i.up=i.down=i.left=i.right=false;
    }
    @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered e) {
        if(scene!=null&&scene.mode==AdventureClientConfig.Cinematics.FULL){e.setCanceled(true);e.setSwingHand(false);}
    }
    @SubscribeEvent public static void vanillaHud(RenderGuiOverlayEvent.Pre e) {
        if(scene!=null&&scene.mode==AdventureClientConfig.Cinematics.FULL)e.setCanceled(true);
    }
    @SubscribeEvent public static void hands(RenderHandEvent e) {
        if(scene!=null&&scene.mode==AdventureClientConfig.Cinematics.FULL)e.setCanceled(true);
    }
    @SubscribeEvent public static void bossMeter(CustomizeGuiOverlayEvent.BossEventProgress e) {
        String title=e.getBossEvent().getName().getString();boolean manang=title.startsWith(BossKind.MANANG.title);
        if(!manang&&!title.startsWith(BossKind.BAKUNAWA.title))return;
        e.setCanceled(true);e.setIncrement(40);var mc=Minecraft.getInstance();var g=e.getGuiGraphics();
        int width=Math.min(256,mc.getWindow().getGuiScaledWidth()-20),x=(mc.getWindow().getGuiScaledWidth()-width)/2,y=e.getY()-8;
        int accent=manang?0xffef7cae:0xffed734d;
        g.fill(x+22,y+17,x+width-22,y+26,0xe5101628);
        g.fill(x+24,y+19,x+24+(int)((width-48)*e.getBossEvent().getProgress()),y+24,accent);
        g.blit(ForgottenIsland.id("textures/gui/"+(manang?"manang_meter":"bakunawa_meter")+".png"),x,y,0,0,width,32,256,32);
        String[] lines=title.split("  \\|  ",2);
        g.drawCenteredString(mc.font,lines[0],x+width/2,y+4,0xfff6e7c4);
        g.drawCenteredString(mc.font,(lines.length==2?lines[1]:"")+" · "+Math.round(e.getBossEvent().getProgress()*100)+"%",x+width/2,y+29,accent);
    }
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e) {
        if(nearby==null||nearby.kind!=BossKind.BAKUNAWA||nearby.phase()!=2||AdventureClientConfig.CINEMATICS.get()==AdventureClientConfig.Cinematics.OFF)return;
        e.setRed(e.getRed()*.45f);e.setGreen(e.getGreen()*.48f);e.setBlue(e.getBlue()*.75f);
    }
    @SubscribeEvent public static void visibility(ViewportEvent.RenderFog e) {
        var mc=Minecraft.getInstance();
        if(nearby==null||nearby.kind!=BossKind.BAKUNAWA||mc.player==null
            ||e.getType()!=net.minecraft.world.level.material.FogType.NONE
            ||mc.player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
            ||mc.player.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS))return;
        // The serpent extends beyond vanilla's short biome fog. Keep its warnings readable.
        e.setNearPlaneDistance(Math.max(e.getNearPlaneDistance(),150));
        e.setFarPlaneDistance(Math.max(e.getFarPlaneDistance(),280));
        e.setCanceled(true);
    }
    @SubscribeEvent public static void overlay(RenderGuiEvent.Post e) {
        var mc=Minecraft.getInstance();if(mc.player==null)return;var g=e.getGuiGraphics();int w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight();
        if(scene==null&&mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit&&hit.getEntity() instanceof BossPart part&&part.weak) {
            g.drawCenteredString(mc.font,(part.getParent().kind==BossKind.MANANG?"Lower body":"Jaw")+(part.getParent().exposed()?" · EXPOSED":" · SHIELDED"),w/2,h/2+18,part.getParent().exposed()?0xffffdf8b:0xffbbbcdf);
        }
        if(scene!=null) {
            boolean entrance=scene.boss.stage()==EncounterState.Stage.ENTRANCE;
            if(scene.mode==AdventureClientConfig.Cinematics.FULL&&scene.boss.kind==BossKind.BAKUNAWA&&!entrance&&scene.boss.clock()>=70&&scene.boss.clock()<80){
                int alpha=(int)(Math.sin((scene.boss.clock()-70)/10.0*Math.PI)*150);g.fill(0,0,w,h,(alpha<<24)|0xffedda);
            }
            if(scene.mode==AdventureClientConfig.Cinematics.FULL){g.fill(0,0,w,29,0xef080e1d);g.fill(0,h-43,w,h,0xef080e1d);}
            String name=scene.boss.kind==BossKind.MANANG?"MANANG":"BAKUNAWA";
            int captionY=scene.mode==AdventureClientConfig.Cinematics.FULL?h-34:h-102;
            g.drawCenteredString(mc.font,scene.boss.lunar() && entrance ? "BAKUNAWA · THE MOON IN ITS JAWS" : name+(entrance?" AWAKENS":" · THE ISLAND REMEMBERS"),w/2,captionY,0xfff5d68d);
            g.drawCenteredString(mc.font,"K · Skip your scene    O · Camera mode",w/2,captionY+16,0xffb4d9d8);
        }
        if(nearby!=null&&!nearby.lunar()&&nearby.kind==BossKind.BAKUNAWA&&AdventureClientConfig.CINEMATICS.get()!=AdventureClientConfig.Cinematics.OFF
            &&(nearby.phase()==2||scene!=null)) {
            double progress=scene!=null?Mth.clamp(nearby.clock()/(float)nearby.kind.entranceLength,0,1):.72;
            int x=w-58,y=83;disc(g,x,y,23,0xfff4dfaf);disc(g,x-37+(int)(progress*40),y,24,0xff15162e);
        }
    }
    private static void disc(GuiGraphics g,int cx,int cy,int r,int color){for(int y=-r;y<=r;y++){int x=(int)Math.sqrt(r*r-y*y);g.fill(cx-x,cy+y,cx+x+1,cy+y+1,color);}}
}
