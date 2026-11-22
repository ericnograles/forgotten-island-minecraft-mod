package com.forgottenisland;

import com.forgottenisland.adventure.EncounterState;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class EncounterStateTest {
    @Test void lethalBlowsWaitForDefeatSceneAndCannotAwardTwice() {
        var state=new EncounterState();UUID player=UUID.randomUUID();
        assertTrue(state.start(200,List.of(player),UUID.randomUUID()));
        String token=state.rewardToken("manang",player,"wings");
        assertFalse(state.mayDeliver(player,token));
        assertFalse(state.recordHealth(0)); // immunity during entrance
        for(int i=0;i<119;i++)state.tick(120,100);
        assertEquals(EncounterState.Stage.ENTRANCE,state.stage());
        state.tick(120,100);assertEquals(EncounterState.Stage.COMBAT,state.stage());
        state.recordHealth(90);assertEquals(2,state.phase());
        assertTrue(state.recordHealth(0));assertFalse(state.recordHealth(0)); // concurrent final blow
        assertFalse(state.delivered(player,token));
        state=EncounterState.restore(state.snapshot()); // restart halfway through victory
        for(int i=0;i<100;i++)state.tick(120,100);
        assertTrue(state.delivered(player,token));assertFalse(state.delivered(player,token));
        var reloaded=EncounterState.restore(state.snapshot());
        assertFalse(reloaded.delivered(player,token));assertTrue(reloaded.collect(token));assertFalse(reloaded.collect(token));
        assertFalse(reloaded.retry());assertFalse(reloaded.start(200,List.of(player),UUID.randomUUID()));
    }
    @Test void bossesAreIndependentAndFailedAttemptsCanRetry() {
        var manang=new EncounterState();var bakunawa=new EncounterState();UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        assertTrue(bakunawa.start(360,List.of(a,b),UUID.randomUUID()));
        assertEquals(540,bakunawa.maxHealth());assertEquals(EncounterState.Stage.READY,manang.stage());
        bakunawa.tick(1,1);bakunawa.recordHealth(180);
        assertTrue(bakunawa.retry());assertTrue(bakunawa.participants().isEmpty());
        assertTrue(bakunawa.start(360,List.of(b),UUID.randomUUID()));
        assertEquals(360,bakunawa.health());assertEquals(2,bakunawa.attempt());
        bakunawa.tick(1,1);bakunawa.recordHealth(0);bakunawa.tick(1,1);
        assertFalse(bakunawa.mayDeliver(a,bakunawa.rewardToken("bakunawa",a,"sword")));
        assertTrue(bakunawa.mayDeliver(b,bakunawa.rewardToken("bakunawa",b,"sword")));
        assertTrue(manang.start(200,List.of(a),UUID.randomUUID()));
    }
    @Test void sceneJoinDoesNotRestartGlobalPlaybackOrChangeExistingSnapshots() {
        var state=new EncounterState();UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        state.start(200,List.of(a),UUID.randomUUID());for(int i=0;i<40;i++)state.tick(120,100);
        var saved=state.snapshot();state.join(b);
        assertEquals(40,state.ticks());assertFalse(saved.participants().contains(b));
        var restored=EncounterState.restore(saved);assertEquals(40,restored.ticks());
        assertTrue(restored.cinematic());assertThrows(UnsupportedOperationException.class,()->saved.participants().clear());
    }
}
