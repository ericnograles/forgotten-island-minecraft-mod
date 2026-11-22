package com.forgottenisland.adventure;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.entity.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** A composed island built exactly once per chunk. All writes are clipped to that chunk. */
public final class IslandWorld {
    public static final BlockPos MANANG=new BlockPos(-112,65,-72), BAKUNAWA=new BlockPos(112,65,-72);
    private static final Species[] RESIDENTS={Species.RAWW,Species.DUWENDE,Species.BUNGI,Species.BATIBA,Species.ZIPPY,Species.TIKBALANG,Species.SARANGAY,Species.CHEF,Species.DATU,Species.SARIMANOK};
    private static final Block[] COLORS={Blocks.RED_WOOL,Blocks.YELLOW_WOOL,Blocks.CYAN_WOOL,Blocks.MAGENTA_WOOL,Blocks.LIME_WOOL,Blocks.ORANGE_WOOL};
    public static void prepareArrival(ServerLevel level) {
        // Arrival, retreat portal and the first market sightline must be ready before the player enters.
        for(int x=-2;x<=1;x++)for(int z=-2;z<=3;z++) initialize(level,level.getChunk(x,z));
    }
    public static void initialize(ServerLevel level,LevelChunk chunk) {
        if(level.dimension()!=AdventureContent.ISLAND)return;
        var data=IslandSavedData.get(level.getServer());
        if(!data.expanded(chunk.getPos().toLong()))IslandExpansion.initialize(level,chunk);
        if(data.chunkBuilt(chunk.getPos().toLong()))return;
        var w=new Writer(level,chunk.getPos());
        int minX=chunk.getPos().getMinBlockX(),minZ=chunk.getPos().getMinBlockZ();
        for(int x=minX;x<minX+16;x++)for(int z=minZ;z<minZ+16;z++) {
            double edge=x*x/(218.0*218)+z*z/(175.0*175);
            if(edge>1)continue;
            boolean beach=edge>.88;
            int top=beach?63:64;
            for(int y=58;y<top;y++)w.set(x,y,z,y<62?Blocks.STONE:Blocks.SAND);
            Block surface=beach?Blocks.SAND:Blocks.GRASS_BLOCK;
            if(Math.abs(x)<34&&z>=-29&&z<=34)surface=(x+z)%7==0?Blocks.YELLOW_TERRACOTTA:Blocks.SMOOTH_SANDSTONE;
            if(road(x,z))surface=Blocks.CUT_SANDSTONE;
            double m=distance(x,z,MANANG),b=distance(x,z,BAKUNAWA);
            if(m<31)surface=m>29?Blocks.POLISHED_BLACKSTONE_BRICKS:((x+z)%9==0?Blocks.PURPLE_TERRACOTTA:Blocks.DEEPSLATE_TILES);
            if(b<65)surface=b>63?Blocks.SEA_LANTERN:((x*x+z*z)%17==0?Blocks.PRISMARINE_BRICKS:Blocks.SMOOTH_QUARTZ);
            w.set(x,top,z,surface);
            if(!beach&&surface==Blocks.GRASS_BLOCK&&Math.floorMod(x*7349+z*9151,47)==0)w.set(x,65,z,(x&1)==0?Blocks.FERN:Blocks.AZALEA);
        }
        // Bright arrival dais with a permanent return aperture behind the landing point.
        for(int x=-9;x<=9;x++)for(int z=35;z<=57;z++)w.set(x,64,z,(x==-9||x==9)?Blocks.PRISMARINE_BRICKS:Blocks.SMOOTH_SANDSTONE);
        star(w,IslandTravel.RETURN_STAR);
        w.sign(-6,65,38,"MALIGAYANG","PAGDATING!","Festival market","Walk north ↑");
        w.sign(6,65,44,"HOMEWARD STAR","Jump into light","Returns each guest","to their own home");
        for(int row=0;row<3;row++)for(int side:new int[]{-1,1}) {
            int x=side*17,z=-18+row*18;
            stall(w,x,z,COLORS[row*2+(side==1?1:0)],row*2+(side==1?1:0));
        }
        // Lantern strings, pennants and a central flower-and-water fountain.
        for(int z=-27;z<=27;z+=18) {
            for(int x=-9;x<=9;x++)w.state(x,72,z,Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST,true).setValue(FenceBlock.WEST,true));
            for(int x=-9;x<=9;x+=3){w.set(x,71,z,COLORS[Math.floorMod(x+z,6)]);w.set(x,70,z,AdventureContent.PAROL.get());}
            for(int side:new int[]{-1,1})for(int y=65;y<=72;y++)w.set(side*10,y,z,Blocks.STRIPPED_JUNGLE_LOG);
        }
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++) {
            w.set(x,65,z,Math.abs(x)==3||Math.abs(z)==3?Blocks.PRISMARINE_BRICKS:Blocks.WATER);
        }
        for(int y=65;y<69;y++)w.set(0,y,0,Blocks.CHISELED_QUARTZ_BLOCK);
        w.set(0,69,0,AdventureContent.PAROL.get());
        w.sign(-5,65,-34,"OLD TWILIGHT ROOST","New mountain: east","Past festival quarter","Bring armor + food");
        w.sign(5,65,-34,"FESTIVAL QUARTER →","Follow eastern shore","Cave / summit / coast","All trails are open");
        // Sheltered waystations and foliage lead to two independent arenas.
        for(int side:new int[]{-1,1})for(int i=0;i<4;i++) {
            int x=side*(35+i*20),z=-42-i*5;
            for(int y=65;y<69;y++)w.set(x,y,z+6,Blocks.OAK_FENCE);
            w.set(x,69,z+6,AdventureContent.PAROL.get());
        }
        for(int i=0;i<28;i++) {
            double a=i*2.399963;int x=(int)(Math.cos(a)*(65+(i%4)*35)),z=(int)(Math.sin(a)*(70+(i%3)*35));
            if(distance(x,z,MANANG)<36||distance(x,z,BAKUNAWA)<70||road(x,z)||Math.abs(x)<37&&Math.abs(z)<60)continue;
            palm(w,x,z);
        }
        // Open sky and low walls keep the immense dragon visible and the camera unobstructed.
        for(int i=0;i<12;i++) {
            double a=i*Math.PI/6;
            int x=MANANG.getX()+(int)(Math.cos(a)*30),z=MANANG.getZ()+(int)(Math.sin(a)*30);
            for(int y=65;y<=69;y++)w.set(x,y,z,Blocks.POLISHED_BLACKSTONE_BRICKS);
            w.set(x,70,z,AdventureContent.PAROL.get());
        }
        w.sign(-112,65,-39,"OLD TWILIGHT ROOST","Manang: new summit","East of the festival","Your history remains");
        w.sign(112,65,-5,"OLD MOON COURT","New coast: far east","Via festival quarter","Your history remains");
        // A shallow lagoon gives Mermahn an appropriate home, away from combat.
        for(int x=25;x<=32;x++)for(int z=22;z<=30;z++) {
            w.set(x,63,z,Blocks.PRISMARINE_BRICKS);w.set(x,64,z,Blocks.WATER);
        }
        for(int i=0;i<20;i++) {
            int x=(i%2==0?-1:1)*(9+(i/10)*18),z=-22+(i/2%5)*11;
            resident(level,chunk,i,RESIDENTS[i%RESIDENTS.length],new BlockPos(x,65,z));
        }
        resident(level,chunk,20,Species.MERMAHN,new BlockPos(28,64,26));
        data.markChunkBuilt(chunk.getPos().toLong());chunk.setUnsaved(true);
    }
    private static double distance(int x,int z,BlockPos p) {return Math.hypot(x-p.getX(),z-p.getZ());}
    private static boolean road(int x,int z) {
        if(Math.abs(x)<=3&&z>=-36&&z<=50)return true;
        for(int side:new int[]{-1,1}) {
            double ax=side*6,az=-32,bx=side*112,bz=-30;
            double t=Math.max(0,Math.min(1,((x-ax)*(bx-ax)+(z-az)*(bz-az))/((bx-ax)*(bx-ax)+(bz-az)*(bz-az))));
            if(Math.hypot(x-(ax+t*(bx-ax)),z-(az+t*(bz-az)))<3)return true;
            if(Math.abs(x-side*112)<=3&&z<=-30&&z>=-72)return true;
        }
        return false;
    }
    private static void star(Writer w,BlockPos center) {
        for(var c:StarPattern.FRAME)w.set(center.getX()+c.x(),center.getY(),center.getZ()+c.z(),AdventureContent.PAROL_STONE.get());
        for(var c:StarPattern.OPENING)w.set(center.getX()+c.x(),center.getY(),center.getZ()+c.z(),AdventureContent.STAR_PORTAL.get());
    }
    private static void stall(Writer w,int cx,int cz,Block color,int index) {
        for(int x=-5;x<=5;x++)for(int z=-4;z<=4;z++) {
            w.set(cx+x,64,cz+z,Blocks.JUNGLE_PLANKS);
            w.set(cx+x,70+(Math.abs(x)<4?1:0),cz+z,(x&1)==0?color:Blocks.WHITE_WOOL);
        }
        for(int x:new int[]{-5,5})for(int z:new int[]{-4,4})for(int y=65;y<=70;y++)w.set(cx+x,y,cz+z,Blocks.STRIPPED_JUNGLE_LOG);
        for(int x=-4;x<=4;x++) {
            w.set(cx+x,65,cz,Blocks.BARREL);
            if((x&1)==0)w.set(cx+x,66,cz,index%2==0?Blocks.CAKE:Blocks.MELON);
        }
        w.set(cx,69,cz-3,AdventureContent.PAROL.get());
        w.sign(cx,65,cz-5,index%2==0?"MERIENDA":"FIESTA FINDS","Kain tayo!","Greet our hosts","Empty-hand use");
    }
    private static void palm(Writer w,int x,int z) {
        for(int y=65;y<=72;y++)w.set(x,y,z,Blocks.JUNGLE_LOG);
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)if(Math.abs(dx)<=1||Math.abs(dz)<=1)
            w.state(x+dx,72+(Math.abs(dx)+Math.abs(dz)<3?1:0),z+dz,Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
    }
    static void resident(ServerLevel level,LevelChunk chunk,int slot,Species species,BlockPos pos) {
        if(!chunk.getPos().equals(new ChunkPos(pos)))return;
        UUID id=UUID.nameUUIDFromBytes(("forgottenisland:market:"+level.getSeed()+":"+slot).getBytes(StandardCharsets.UTF_8));
        if(level.getEntity(id)!=null)return;
        IslandCreature creature=ForgottenIsland.TYPES.get(species).get().create(level);
        if(creature==null)throw new IllegalStateException("Missing market creature: "+species);
        creature.setUUID(id);creature.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,slot*47,0);
        creature.setMarketResident();
        creature.getPersistentData().putLong("IslandMarketHome",pos.asLong());
        creature.setPersistenceRequired();creature.setInvulnerable(true);
        level.addFreshEntity(creature);
    }
    private record Writer(ServerLevel level,ChunkPos chunk) {
        void set(int x,int y,int z,Block block) {state(x,y,z,block.defaultBlockState());}
        void state(int x,int y,int z,BlockState state) {
            if(x>>4==chunk.x&&z>>4==chunk.z)level.setBlock(new BlockPos(x,y,z),state,2|16);
        }
        void sign(int x,int y,int z,String a,String b,String c,String d) {
            if(x>>4!=chunk.x||z>>4!=chunk.z)return;
            set(x,y,z,Blocks.OAK_SIGN);
            if(level.getBlockEntity(new BlockPos(x,y,z)) instanceof SignBlockEntity sign) {
                SignText text=new SignText();String[] lines={a,b,c,d};
                for(int i=0;i<4;i++)text=text.setMessage(i,Component.literal(lines[i]));
                sign.setText(text,true);sign.setText(text,false);sign.setWaxed(true);
            }
        }
    }
    private IslandWorld() {}
}
