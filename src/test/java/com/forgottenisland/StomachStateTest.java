package com.forgottenisland;

import com.forgottenisland.adventure.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StomachStateTest {
    @Test void cooperativeCrystalsAreSharedAndOnlyTheLastOneReleases(){
        var s=new StomachState();var a=UUID.randomUUID();var b=UUID.randomUUID();
        assertTrue(s.swallow(a,1));assertFalse(s.swallow(b,1));
        assertFalse(s.breakCrystal(UUID.randomUUID(),0,1));
        assertTrue(s.breakCrystal(a,0,1));assertFalse(s.breakCrystal(a,0,1));
        assertTrue(s.active(a,1));assertTrue(s.active(b,1));
        assertTrue(s.breakCrystal(b,1,1));assertTrue(s.breakCrystal(a,2,1));
        assertFalse(s.active(a,1));assertTrue(s.contains(a));assertTrue(s.contains(b));
        assertEquals(-1,s.guests().get(b)); // Offline recovery survives the release.
    }
    @Test void restartResetAndDeathCannotReviveStaleObjectives(){
        var s=new StomachState();var a=UUID.randomUUID();var b=UUID.randomUUID();
        s.swallow(a,2);s.breakCrystal(a,1,2);
        var restored=new StomachState();restored.restore(s.attempt(),s.crystals(),s.guests());
        assertEquals(5,restored.crystals());assertTrue(restored.active(a,2));
        restored.release();assertFalse(restored.active(a,2));
        assertTrue(restored.swallow(b,3));assertFalse(restored.active(a,3));
        assertFalse(restored.breakCrystal(a,0,3));restored.remove(a);assertFalse(restored.contains(a));
        assertTrue(restored.active(b,3));
    }
    @Test void attackAnimationsHaveBoundedWindupImpactAndWithdrawal(){
        assertEquals(0,BakunawaMoves.lunge(34));assertEquals(1,BakunawaMoves.lunge(60));assertEquals(0,BakunawaMoves.lunge(95));
        assertEquals(0,BakunawaMoves.tentacleHeight(39));assertEquals(1,BakunawaMoves.tentacleHeight(60));assertEquals(0,BakunawaMoves.tentacleHeight(92));
        assertEquals(7,BakunawaMoves.TENTACLE_DAMAGE);assertEquals(20,2*BakunawaMoves.BEAM_DAMAGE);
    }
}
