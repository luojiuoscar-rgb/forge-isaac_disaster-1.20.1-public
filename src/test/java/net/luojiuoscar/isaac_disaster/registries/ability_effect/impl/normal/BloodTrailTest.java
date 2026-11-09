package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BloodTrailTest {
    @Test
    void explicitEnhancementScalesConfiguredDamageAndNormalizesInvalidMultipliers() {
        assertEquals(4F, BloodTrail.damageFromBonus(2, 1));
        assertEquals(8F, BloodTrail.damageFromBonus(2, 2));
        assertEquals(12F, BloodTrail.damageFromBonus(3, 2));
        for (double amplifier : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0F, BloodTrail.damageFromBonus(2, amplifier));
        }
        assertEquals(Float.MAX_VALUE, BloodTrail.damageFromBonus(Double.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    void damageUsesTwoConfiguredUnitsAndDoesNotCancelConfigurationScaling() {
        assertEquals(4, BloodTrail.damageFromBonus(2));
        assertEquals(6, BloodTrail.damageFromBonus(3));
        assertEquals(1, BloodTrail.damageFromBonus(0.5));
        assertEquals(0, BloodTrail.damageFromBonus(0));
    }

    @Test
    void invalidDamageCannotProduceNegativeOrNonFiniteSnapshots() {
        for (double value : new double[]{-1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            assertEquals(0, BloodTrail.damageFromBonus(value));
        }
        assertEquals(Float.MAX_VALUE, BloodTrail.damageFromBonus(Double.MAX_VALUE));
    }
}
