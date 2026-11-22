package com.forgottenisland;

import com.forgottenisland.adventure.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MountainsAndFormsTest {
    @Test void footprintAndNegativeCoordinateSeams(){
        assertEquals(100000,MountainTerrain.MAX_X-MountainTerrain.MIN_X+1);
        assertEquals(80000,MountainTerrain.MAX_Z-MountainTerrain.MIN_Z+1);
        int high=0;
        var random=new Random(2070);
        for(int n=0;n<20000;n++){
            int x=(random.nextInt(6249)-3105)*16,z=random.nextInt(80000)+MountainTerrain.MIN_Z;
            int y=MountainTerrain.height(x,z);high=Math.max(y,high);
            assertTrue(y>=0&&y<310);
            assertTrue(Math.abs(y-MountainTerrain.height(x-1,z))<15,"seam at "+x+","+z);
            assertEquals(y,MountainTerrain.height(x,z));
        }
        assertTrue(high>230);
        assertEquals(38,MountainTerrain.height(MountainTerrain.MAX_X+1,0));
        assertEquals(64,MountainTerrain.height(310,65));
        assertEquals(104,MountainTerrain.height(535,-215));
        for(int x=807;x<=833;x++)for(int z=-164;z<=-88;z++)assertTrue(MountainTerrain.height(x,z)<=54,"blocked breach at "+x+","+z);
    }
    @Test void fullTwoBarFightWithIronAndEachLegendarySword(){
        double[] previous={0,0};
        for(float weapon:new float[]{6,1000,2600,6000}){
            var state=new EncounterState();var player=UUID.randomUUID();
            state.start(400,List.of(player),UUID.randomUUID());state.tick(1,1);
            for(int form=1;form<=2;form++){
                float hit=BossBalance.damage(weapon,true,form==2);
                assertTrue(hit>previous[form-1]&&hit<state.health()/4);
                previous[form-1]=hit;
                int swings=0;
                while(state.stage()==EncounterState.Stage.COMBAT){state.recordHealth(state.health()-hit,true);assertTrue(++swings<100);}
                if(form==1){
                    assertEquals(EncounterState.Stage.TRANSFORM,state.stage());assertEquals(500,state.health());
                    state=EncounterState.restore(state.snapshot());assertFalse(state.mayDeliver(player,"loot"));
                    for(int i=0;i<60;i++)state.tick(1,1);
                }
            }
            assertEquals(EncounterState.Stage.DEFEAT,state.stage());state.tick(1,1);
            assertTrue(state.delivered(player,"loot"));assertFalse(state.delivered(player,"loot"));
        }
    }
}
