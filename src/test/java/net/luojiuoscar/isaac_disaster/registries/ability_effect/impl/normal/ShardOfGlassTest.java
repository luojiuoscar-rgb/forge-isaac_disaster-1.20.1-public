package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShardOfGlassTest {
    @Test
    void heartChanceUsesConfiguredHealthUnitAndCapsAtOneQuarter() {
        assertEquals(0.05, ShardOfGlass.dropChance(1, 10), 1E-9);
        assertEquals(0.25, ShardOfGlass.dropChance(5, 10));
        assertEquals(0.25, ShardOfGlass.dropChance(100, 10));
        assertEquals(0.125, ShardOfGlass.dropChance(5, 20));
        assertEquals(0.25, ShardOfGlass.dropChance(0.25, 0.5));
    }

    @Test
    void invalidOrAbsentHealthDamageCannotRollAHeart() {
        for (double value : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0, ShardOfGlass.dropChance(value, 10));
            assertEquals(0, ShardOfGlass.dropChance(5, value));
        }
    }

    @Test
    void sprayAlwaysPointsUpWithinTheApprovedConeAndHasUnitLength() {
        for (int i = 0; i <= 100; i++) {
            var direction = ShardOfGlassBloodTear.direction(i / 100.0, (100 - i) / 100.0);
            assertEquals(1, direction.length(), 1E-9);
            assertTrue(direction.y >= Math.sin(Math.toRadians(60)) - 1E-9);
            assertTrue(direction.y <= Math.sin(Math.toRadians(85)) + 1E-9);
        }
        assertTrue(ShardOfGlassBloodTear.direction(0, 0).x > 0);
        assertTrue(ShardOfGlassBloodTear.direction(0.5, 0).x < 0);
    }

    @Test
    void eachTearDamageIsScaledFromItsOwnPanelSnapshot() {
        assertEquals(6.7, ShardOfGlassBloodTear.damage(10, 0), 1E-6);
        assertEquals(10.1, ShardOfGlassBloodTear.damage(10, 1), 1E-6);
        assertEquals(17.684, ShardOfGlassBloodTear.damage(20, 0.63), 1E-6);
        assertEquals(0, ShardOfGlassBloodTear.damage(Double.NaN, 0.5));
        assertEquals(0, ShardOfGlassBloodTear.damage(-1, 0.5));
    }
}
