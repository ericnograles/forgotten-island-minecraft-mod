package com.forgottenisland;
import com.forgottenisland.entity.CompanionRules;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CompanionRulesTest {
    @Test void strangersCannotControlCompanions() {
        UUID owner=UUID.randomUUID(),stranger=UUID.randomUUID();
        assertFalse(CompanionRules.mayRequest(owner,stranger,true,1));
        assertFalse(CompanionRules.mayRequest(null,stranger,true,1));
        assertFalse(CompanionRules.mayRequest(owner,owner,false,1));
        assertFalse(CompanionRules.mayRequest(owner,owner,true,145));
        assertFalse(CompanionRules.mayRequest(owner,owner,true,Double.NaN));
        assertTrue(CompanionRules.mayRequest(owner,owner,true,144));
    }
    @Test void giftsFromDifferentPlayersDoNotStealProgress() {
        UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        assertEquals(1,CompanionRules.giftProgress(a,b,2));
        assertEquals(3,CompanionRules.giftProgress(a,a,2));
        assertEquals(3,CompanionRules.giftProgress(a,a,500));
    }
    @Test void malformedSavedDurationsRemainBounded() {
        assertEquals(0,CompanionRules.clampTicks(-1));
        assertEquals(24000,CompanionRules.clampTicks(Integer.MAX_VALUE));
        assertEquals(240,CompanionRules.clampTicks(240));
    }
}
