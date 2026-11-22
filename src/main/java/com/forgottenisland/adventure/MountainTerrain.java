package com.forgottenisland.adventure;

/** Continuous world-space fields. No chunk cache, random traversal order or save journal. */
public final class MountainTerrain {
    public static final int MIN_X=-49696, MAX_X=50303, MIN_Z=-39936, MAX_Z=40063, SEA=62;
    public static boolean inBounds(int x,int z){return x>=MIN_X&&x<=MAX_X&&z>=MIN_Z&&z<=MAX_Z;}
    static double clamp(double n){return Math.max(0,Math.min(1,n));}
    static double blend(double a,double b,double t){t=clamp(t);return a+(b-a)*t*t*(3-2*t);}
    static int hash(int x,int z){
        int h=x*0x1f1f1f1f ^ z*0x6c8e9cf5;
        h=(h^(h>>>16))*0x7feb352d; h=(h^(h>>>15))*0x846ca68b; return h^(h>>>16);
    }
    static double noise(int x,int z,int scale){
        int i=Math.floorDiv(x,scale),j=Math.floorDiv(z,scale);
        double u=Math.floorMod(x,scale)/(double)scale,v=Math.floorMod(z,scale)/(double)scale;
        return blend(blend(hash(i,j),hash(i+1,j),u),blend(hash(i,j+1),hash(i+1,j+1),u),v)/2147483648.0;
    }
    static boolean hub(int x,int z){return x>=168&&x<=915&&z>=-310&&z<=258;}
    static double river(int x,int z){
        return Math.abs(z-(320+110*Math.sin(x*.004)+84*Math.sin(x*.011)));
    }
    public static int height(int x,int z){
        if(!inBounds(x,z))return 38;
        double rim=Math.min(Math.min(x-MIN_X,MAX_X-x),Math.min(z-MIN_Z,MAX_Z-z));
        double grain=noise(x,z,42),fold=noise(x,z,340);
        double ridge=1-Math.abs(Math.sin(x*.007+z*.003+fold*2.5+grain*.12));
        double h=81+ridge*ridge*(88+72*(.5+.5*noise(x,z,710)))+16*grain+16*fold;
        h=blend(54,h,(river(x,z)-5)/52);
        // A long saltwater inlet connects the market's eastern shore with the ocean.
        if(x>675){
            double north=-170-125*Math.sin((x-820)*.0015);
            double width=100+Math.min(370,Math.max(0,(x-820)*.12));
            double distance=Math.abs(z-north)-width;
            double bay=blend(43,h,(distance+8)/56);
            h=blend(h,bay,(x-675)/100.);
        }
        // Steep needles frame the festival at normal chunk distance. The gaps are the trail passes.
        if(x>140&&x<465&&z>-126&&z<240){
            h=Math.max(h,spire(x,z,224,30,50,84));
            h=Math.max(h,spire(x,z,356,-26,43,104));
            h=Math.max(h,spire(x,z,423,129,40,91));
            h=Math.max(h,spire(x,z,251,151,34,81));
        }
        if(hub(x,z)){
            double d=Math.max(Math.abs(x-310)-62,Math.abs(z-65)-53);
            h=blend(64,h,(d-5)/45);
            double summit=IslandLayout.mountain(x,z);
            h=blend(104,h,(summit-34)/40);
            double hollow=Math.hypot((x-475)*1.1,z-190);
            h=blend(95,h,(hollow-44)/28);
            double coast=IslandLayout.coast(x,z);
            if(coast<120){
                double shore=blend(45,64,(z+86)/20.);
                if(z>-49)shore+=Math.min(8,Math.max(0,(z+49)/4.));
                h=blend(shore,h,(coast-81)/39);
            }
            var road=IslandLayout.path(x,z);
            if(hollow>45&&!IslandLayout.caveTunnel(x,z)){
                double roadY=road.y();
                if(x>780&&Math.abs(x-820)<25&&z>-49)roadY+=8*clamp((z+49)/32.)*clamp((35-z)/25.);
                h=blend(roadY,h,(road.distance()-4)/18);
            }
        }
        return (int)Math.round(blend(38,h,(rim-32-22*grain)/108));
    }
    private static double spire(int x,int z,int cx,int cz,double radius,double lift){
        double slope=clamp(1-Math.hypot(x-cx,(z-cz)*.84)/radius);
        return 61+lift*slope*slope*(3-2*slope);
    }
    /** Elliptic galleries cut across neighboring chunks at a continuous elevation. */
    static int caveRoof(int x,int z,int top){
        if(hub(x,z)){
            if(IslandLayout.cavern(x,z))return 84+(int)(4*Math.sin(x*.1)*Math.cos(z*.1));
            if(IslandLayout.caveTunnel(x,z))return 77;
        }
        if(top<76||Math.abs(Math.sin(x*.029+z*.008+Math.sin(z*.033)))>.30)return 0;
        return 85+(int)(18*Math.sin(z*.011)+8*Math.cos(x*.027));
    }
    static int caveFloor(int x,int z,int roof){
        if(hub(x,z)&&(IslandLayout.cavern(x,z)||IslandLayout.caveTunnel(x,z)))return 66;
        return roof-7-(int)(2*Math.sin(z*.035));
    }
    private MountainTerrain(){}
}
