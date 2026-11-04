package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DepressionPuddleEffectTest {
    @Test
    void probabilityHasAnExclusiveOnePercentBoundary() {
        assertTrue(DepressionPuddleEffect.procs(0));
        assertTrue(DepressionPuddleEffect.procs(Math.nextDown(0.01)));
        for (double roll : new double[]{0.01, 0.5, 1, -0.01, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertFalse(DepressionPuddleEffect.procs(roll));
        }
    }

    @Test
    void holyDamageUsesThreeTimesPanelWithSafeBounds() {
        assertEquals(12, DepressionPuddleEffect.holyDamage(4));
        assertEquals(30, DepressionPuddleEffect.holyDamage(10));
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0, DepressionPuddleEffect.holyDamage(invalid));
        }
        assertEquals(Float.MAX_VALUE, DepressionPuddleEffect.holyDamage(Double.MAX_VALUE));
    }
}
