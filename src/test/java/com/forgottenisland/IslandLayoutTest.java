package com.forgottenisland;

import com.forgottenisland.adventure.IslandLayout;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IslandLayoutTest {
    @Test void everyRouteHasLandAndWalkableGrades(){
        for(var route:IslandLayout.ROUTES)for(int i=1;i<route.length;i++){
            var a=route[i-1];var b=route[i];int steps=(int)Math.ceil(Math.hypot(b[0]-a[0],b[1]-a[1])*2);int prior=a[2];
            for(int step=0;step<=steps;step++){
                double t=step/(double)steps;int x=(int)Math.round(a[0]+t*(b[0]-a[0])),z=(int)Math.round(a[1]+t*(b[1]-a[1]));
                assertTrue(IslandLayout.land(x,z),"Route leaves land at "+x+","+z);
                int y=IslandLayout.walkingHeight(x,z);assertTrue(Math.abs(y-prior)<=1,"Unwalkable grade at "+x+","+z);prior=y;
            }
        }
    }
    @Test void destinationsHaveDistinctElevationsAndEnoughCombatSpace(){
        assertEquals(104,IslandLayout.height(535,-215));assertTrue(IslandLayout.cavern(475,190));
        assertTrue(IslandLayout.market(310,65));
        for(int dx=-84;dx<=84;dx++)for(int dz=-84;dz<=84;dz++)if(dx*dx+dz*dz<=84*84)assertEquals(64,IslandLayout.height(820+dx,-65+dz));
    }
}
