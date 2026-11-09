package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DepressionTrailTest {
    @Test
    void configuredUnitsAndEnhancementScaleNewSnapshots() {
        assertEquals(4, DepressionTrail.damageFromBonus(2, 1));
        assertEquals(8, DepressionTrail.damageFromBonus(2, 2));
        assertEquals(12, DepressionTrail.damageFromBonus(3, 2));
        assertEquals(1, DepressionTrail.damageFromBonus(0.5, 1));
    }

    @Test
    void invalidInputsAreSafeAndOverflowStaysFinite() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0, DepressionTrail.damageFromBonus(invalid, 1));
            assertEquals(0, DepressionTrail.damageFromBonus(2, invalid));
        }
        assertEquals(Float.MAX_VALUE, DepressionTrail.damageFromBonus(Double.MAX_VALUE, Double.MAX_VALUE));
    }
}
