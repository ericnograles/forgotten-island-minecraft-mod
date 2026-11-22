package com.forgottenisland.client;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.entity.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Editable JSON skeletons; one texture atlas per species, authored in assets-src. */
public class CreatureModel extends EntityModel<IslandCreature> {
    private final ModelPart root;
    private final Map<String,ModelPart> parts=new HashMap<>();
    public CreatureModel(ModelPart root) {this.root=root; index("root",root);}
    private void index(String name,ModelPart part) {
        parts.put(name,part);
        // Names are loaded from the skeleton separately because ModelPart children are private.
    }
    public static ModelLayerLocation layer(Species s) {return new ModelLayerLocation(ForgottenIsland.id(s.id),"main");}
    public static LayerDefinition bake(Species s) {
        return bake(s,false);
    }
    public static LayerDefinition bake(Species s,boolean boss) {
        MeshDefinition mesh=new MeshDefinition();
        Map<String,PartDefinition> bones=new HashMap<>();
        bones.put("root",mesh.getRoot().addOrReplaceChild("body_root",CubeListBuilder.create(),PartPose.offset(0,24,0)));
        String modelId=boss&&s==Species.BAKUNAWA?"bakunawa_boss":s.id;
        try(var reader=new InputStreamReader(Minecraft.getInstance().getResourceManager().getResourceOrThrow(ForgottenIsland.id("models/creature/"+modelId+".json")).open(),StandardCharsets.UTF_8)) {
            JsonObject model=JsonParser.parseReader(reader).getAsJsonObject();
            for(JsonElement element:model.getAsJsonArray("bones")) {
                JsonObject bone=element.getAsJsonObject();String name=bone.get("name").getAsString();
                String parent=bone.has("parent")?bone.get("parent").getAsString():"root";
                JsonArray pivot=bone.getAsJsonArray("pivot"),rotation=bone.getAsJsonArray("rotation");
                CubeListBuilder cubes=CubeListBuilder.create();
                for(JsonElement boxElement:bone.getAsJsonArray("boxes")) {
                    JsonArray b=boxElement.getAsJsonArray();
                    boolean belly=boss&&s==Species.BAKUNAWA&&name.startsWith("segment")&&b.get(6).getAsInt()==7;
                    cubes.texOffs(b.get(6).getAsInt()*128,0).addBox(f(b,0),f(b,1),f(b,2)-(belly?.03f:0),f(b,3),f(b,4),f(b,5)+(belly?.06f:0));
                }
                PartPose pose=PartPose.offsetAndRotation(f(pivot,0),f(pivot,1),f(pivot,2),f(rotation,0),f(rotation,1),f(rotation,2));
                bones.put(name,bones.get(parent).addOrReplaceChild(name,cubes,pose));
            }
        } catch(Exception ex) {throw new IllegalStateException("Cannot load original creature model "+s.id,ex);}
        return LayerDefinition.create(mesh,1024,128);
    }
    private static float f(JsonArray a,int i) {return a==null?0:a.get(i).getAsFloat();}
    private ModelPart p(String path) {
        return parts.computeIfAbsent(path,key->{
            ModelPart part=root.getChild("body_root");
            for(String name:key.split("/"))part=part.getChild(name);
            return part;
        });
    }
    private void x(String key,float x) {p(key).xRot+=x;}
    private void y(String key,float y) {p(key).yRot+=y;}
    private void z(String key,float z) {p(key).zRot+=z;}
    @Override public void setupAnim(IslandCreature e,float limbSwing,float amount,float age,float yaw,float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float walk=Mth.sin(limbSwing*.65f)*amount, idle=Mth.sin(age*.075f), act=e.activeTicks()>0?1:0;
        float celebrate=e.reactionTicks>0?Mth.sin(age*.7f)*.2f:0;
        ModelPart base=root.getChild("body_root");
        base.y+=e.isOrderedToSit()?2:idle*.18f;
        switch(e.species) {
            case BAKUNAWA -> {
                x("head",pitch*.01f-act*.2f);y("head",yaw*.012f);
                x("head/jaw",act*(.24f+Mth.sin(age*.35f)*.12f));
                String part="segment0";
                for(int i=0;i<9;i++) {
                    y(part,Mth.sin(age*(e.isInWater()?.16f:.09f)-i*.52f)*(.12f+amount*.08f)+Mth.clamp(Mth.wrapDegrees(e.getYRot()-e.yRotO),-15,15)*.007f);
                    x(part,Mth.sin(age*.08f-i*.45f)*.045f+(e.flying()?.018f:0));
                    if(i<8)part+="/segment"+(i+1);
                }
                z("fin_left",Mth.sin(age*.14f)*.2f);z("fin_right",-Mth.sin(age*.14f)*.2f);
            }
            case RAWW -> {
                p("upright").visible=!e.transformed();p("dog").visible=e.transformed();
                x("upright/leg_left",walk*.9f);x("upright/leg_right",-walk*.9f);
                x("upright/arm_left",-walk*.7f+celebrate);x("upright/arm_right",walk*.7f-celebrate);
                y("upright/head",yaw*.014f);x("upright/head",pitch*.012f+idle*.035f);
                z("upright/head/ear_left",idle*.08f);z("upright/head/ear_right",-idle*.08f);
                y("upright/tail",Mth.sin(age*.25f)*.4f);
                for(String leg:List.of("front_left","back_right"))x("dog/"+leg,walk);
                for(String leg:List.of("front_right","back_left"))x("dog/"+leg,-walk);
                y("dog/tail",Mth.sin(age*.4f)*.6f);x("dog/head",-act*.2f);
            }
            case SARIMANOK -> {
                float wing=e.flying()?Mth.sin(age*.65f)*.65f:act>0?Mth.sin(age*.45f)*.55f:-.85f+idle*.04f;
                z("wing_left",wing);z("wing_right",-wing);
                x("leg_left",walk);x("leg_right",-walk); y("head",yaw*.015f);x("head",pitch*.01f+idle*.09f);
                y("tail",idle*.12f);x("tail",act*.3f);
            }
            case MANANG -> {
                ManangFace.animate(p("upper"),age,act,Mth.clamp(yaw*.009f,-.22f,.22f));
                p("upper").y-=act*9; p("lower").y+=act*2;
                z("upper/wing_left",Mth.sin(age*.25f)*.3f+act*.35f);
                z("upper/wing_right",-Mth.sin(age*.25f)*.3f-act*.35f);
                y("upper/head",yaw*.013f);x("upper/head",pitch*.01f-act*.2f);
                x("lower/leg_left",walk*.5f);x("lower/leg_right",-walk*.5f);
                x("upper/arm_left",-act*.9f);x("upper/arm_right",-act*.9f);
            }
            case DATU, DUWENDE, BUNGI, BATIBA -> {
                float pace=e.species==Species.DATU?.5f:1;
                x("leg_left",walk*pace);x("leg_right",-walk*pace);
                x("arm_left",-walk*.6f-act*.8f+celebrate);x("arm_right",walk*.6f-act*.8f-celebrate);
                y("head",yaw*.012f);x("head",pitch*.01f+idle*.04f);
                if(e.species==Species.DUWENDE){p("head/crown").visible=e.variant()==0; base.y+=act*Mth.sin(age*.45f)*2;}
                if(e.species==Species.DATU)p("head/crown").visible=e.variant()==0;
                if(e.species==Species.BUNGI)base.y-=act*Math.abs(Mth.sin(age*.3f))*3;
                if(e.species==Species.BATIBA){z("head/ear_left",idle*.12f);z("head/ear_right",-idle*.12f);}
            }
            case MERMAHN -> {
                y("tail",Mth.sin(age*(e.isInWater()?.4f:.1f))*.4f);x("tail",e.isInWater()?.3f:-.3f);
                y("head",yaw*.015f);x("arm_left",-act*.9f+idle*.08f);x("arm_right",-act*.9f-idle*.08f);
                p("head/hair").zRot+=act*Mth.sin(age*.2f)*.15f;
                if(!e.isInWater()){base.y+=2;p("head").xScale=.85f;p("head").yScale=.85f;}
            }
            case ZIPPY -> {
                base.y-=2+Mth.sin(age*.16f)*.7f;
                y("head",yaw*.012f);x("head",pitch*.01f+idle*.08f);
                z("wing_left",Mth.sin(age*.95f)*.65f);z("wing_right",-Mth.sin(age*.95f)*.65f);
                x("arm_left",-.2f-act*1.1f+celebrate);x("arm_right",-.2f-act*.7f-celebrate);
                x("leg_left",walk*.6f+.25f);x("leg_right",-walk*.6f+.15f);
                z("head/crest",idle*.1f);y("bottle",age*.025f*act);
            }
            case TIKBALANG -> {
                x("leg_left",walk*.9f);x("leg_right",-walk*.9f);
                x("arm_left",-walk*.7f-act);x("arm_right",walk*.7f-act*.5f);
                y("head",yaw*.014f);x("head",pitch*.01f+idle*.08f);
                y("tail",Mth.sin(age*.12f)*.3f);z("head/ear_left",idle*.07f);
                base.y-=act*Math.abs(Mth.sin(age*.25f))*2;
                y("body",act*Mth.sin(age*.2f)*.2f);
            }
            case SARANGAY -> {
                x("leg_left",walk*.55f);x("leg_right",-walk*.55f);
                x("arm_left",-walk*.5f-act*.95f+celebrate);x("arm_right",walk*.5f-act*.95f-celebrate);
                z("arm_left",act*-.25f);z("arm_right",act*.25f);
                y("head",yaw*.01f);x("head",pitch*.008f+idle*.025f+act*.15f);
                z("head/ear_left",idle*.08f);z("head/ear_right",-idle*.08f);
                y("tail",Mth.sin(age*.1f)*.25f);y("jewel",act*age*.08f);
                p("body").y+=idle*.2f; base.y-=act*Math.abs(Mth.sin(age*.15f))*.7f;
            }
            case CHEF -> {
                x("leg_left",walk*.75f);x("leg_right",-walk*.75f);
                y("head",yaw*.012f);x("head",pitch*.01f+idle*.045f);
                z("head/ear_left",idle*.1f+celebrate);z("head/ear_right",-idle*.1f-celebrate);
                // Hold the pan level; the other hand stirs, then raises its spatula to serve.
                x("arm_left",-.9f-walk*.12f-act*.2f+celebrate*.25f);
                x("arm_left/pan",.9f+walk*.12f+act*.2f);
                x("arm_right",-walk*.45f-act*(1.05f+Mth.sin(age*.35f)*.25f)-celebrate);
                z("arm_right",act*(-.4f+Mth.cos(age*.35f)*.2f));
                y("arm_right",act*Mth.sin(age*.35f)*.35f);
                p("body").y+=idle*.25f;
                for(int i=0;i<3;i++) {
                    String flame="head/flame"+i;
                    z(flame,Mth.sin(age*.18f+i*1.7f)*(.1f+act*.08f));
                    p(flame).yScale=1+Mth.sin(age*.23f+i)*.12f+act*.15f;
                }
            }
        }
    }
    @Override public void renderToBuffer(PoseStack stack,VertexConsumer vertices,int light,int overlay,float r,float g,float b,float a) {
        root.render(stack,vertices,light,overlay,r,g,b,a);
    }
}
