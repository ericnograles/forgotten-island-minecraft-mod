package com.forgottenisland.entity;

import java.util.UUID;

/** Pure rules shared by interactions and network requests. No client authority. */
public final class CompanionRules {
    private CompanionRules() { }
    public static boolean mayRequest(UUID owner, UUID requester, boolean alive, double distanceSquared) {
        return alive && owner != null && owner.equals(requester) && Double.isFinite(distanceSquared) && distanceSquared <= 144;
    }
    public static int clampTicks(int ticks) { return Math.max(0, Math.min(ticks, 24000)); }
    public static int giftProgress(UUID current, UUID giver, int previous) {
        return current != null && current.equals(giver) ? Math.min(3, Math.max(0,previous)+1) : 1;
    }
}
