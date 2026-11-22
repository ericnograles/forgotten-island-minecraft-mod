package com.forgottenisland.adventure;

/** Pure terrain/route plan, shared by construction and route regression checks. */
public final class IslandLayout {
    public static final int MARKET_X=310, MARKET_Z=65, CAVE_X=475,CAVE_Z=190;
    public static final int MOUNTAIN_X=535,MOUNTAIN_Z=-215,COAST_X=820,COAST_Z=-65;
    // x,z,walking surface. Each slope rises less than one block per walking block.
    public static final int[][][] ROUTES={
        {{175,60,64},{240,65,64},{310,65,64},{400,35,64}},
        {{400,35,64},{430,-45,70},{465,-110,82},{500,-155,94},{535,-175,104},{535,-215,104}},
        {{400,35,64},{440,110,66},{475,138,66},{475,190,66}},
        {{400,35,64},{530,15,68},{640,25,65},{735,10,64},{820,10,64},{820,-65,64}}
    };
    public record Path(double distance,int y){}
    public static Path path(int x,int z){
        double best=Double.MAX_VALUE;int y=64;
        for(var route:ROUTES)for(int i=1;i<route.length;i++){
            var a=route[i-1];var b=route[i];double dx=b[0]-a[0],dz=b[1]-a[1];
            double t=Math.max(0,Math.min(1,((x-a[0])*dx+(z-a[1])*dz)/(dx*dx+dz*dz)));
            double d=Math.hypot(x-a[0]-t*dx,z-a[1]-t*dz);
            if(d<best){best=d;y=(int)Math.round(a[2]+t*(b[2]-a[2]));}
        }
        return new Path(best,y);
    }
    public static double edge(int x,int z){return Math.pow((x-600)/450.0,2)+Math.pow((z+10)/350.0,2);}
    public static boolean land(int x,int z){
        // Untouched northern coast remains ocean for the serpent's emergence.
        // Already-expanded chunks are never regenerated or excavated.
        if(x>COAST_X-100&&z<COAST_Z-88)return false;
        return x>=176&&edge(x,z)<1||x>=175&&x<=250&&Math.abs(z-64)<=8;
    }
    public static boolean market(int x,int z){return Math.abs(x-MARKET_X)<68&&Math.abs(z-MARKET_Z)<64;}
    public static double mountain(int x,int z){return Math.hypot(x-MOUNTAIN_X,(z-MOUNTAIN_Z)*1.05);}
    public static double coast(int x,int z){return Math.hypot(x-COAST_X,z-COAST_Z);}
    public static boolean cavern(int x,int z){return Math.pow((x-CAVE_X)/32.0,2)+Math.pow((z-CAVE_Z)/45.0,2)<1;}
    public static boolean caveTunnel(int x,int z){return x>430&&z>=110&&z<=170&&path(x,z).distance()<4.5;}
    public static int walkingHeight(int x,int z){return cavern(x,z)?66:caveTunnel(x,z)?path(x,z).y():height(x,z);}
    public static int height(int x,int z){
        var path=path(x,z);
        double caveShell=Math.pow((x-CAVE_X)/32.0,2)+Math.pow((z-CAVE_Z)/45.0,2);
        if(path.distance<4&&caveShell>1.65)return path.y;
        if(market(x,z)||coast(x,z)<88)return 64;
        double mountain=mountain(x,z);
        if(mountain<34)return 104;
        int natural=64+(int)(4+3*Math.sin(x*.035)*Math.cos(z*.042));
        if(edge(x,z)>.94)natural=63;
        natural=Math.max(natural,64+(int)Math.max(0,(140-mountain)*.39));
        double cave=Math.hypot((x-CAVE_X)*1.2,z-CAVE_Z);
        if(cave<65)natural=Math.max(natural,67+(int)((65-cave)*.52));
        // Blend the verge into the route to avoid sheer walls alongside the walkable road.
        if(caveShell<1.65)natural=Math.max(natural,89+(int)((1.65-caveShell)*7));
        else if(path.distance<10)natural=(int)Math.round(path.y+(natural-path.y)*(path.distance-4)/6);
        return natural;
    }
    private IslandLayout(){}
}
