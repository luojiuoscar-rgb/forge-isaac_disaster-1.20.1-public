package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect;

import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.luojiuoscar.isaac_disaster.system.combat_effect.attachment.EffectAttachmentNode;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.BloodPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectManager;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BloodPuddleTest {
    private static final GroundEffectType<BloodPuddleEffect> BLOOD = new GroundEffectType<>(BloodPuddleEffect::new);

    @Test
    void bloodRetainsItsDamageAndColorWithoutAnAttachmentNode() {
        var blood = BLOOD.create(0, 1, UUID.randomUUID(), Vec3.ZERO, 0.75, 100, 4, 0);
        assertEquals(0.75, blood.radius());
        assertEquals(100, blood.lifetime());
        assertEquals(4, blood.damage());
        assertEquals(0xB82025, blood.getColor());
        assertFalse(EffectAttachmentNode.class.isInstance(blood));
    }

    @Test
    void repeatedPlacementRepaintsButADifferentDamageCreatesANewSnapshot() {
        var manager = new GroundEffectManager();
        var owner = UUID.randomUUID();
        var first = manager.spawn(BLOOD, owner, Vec3.ZERO, 0.75, 100, 4);
        assertSame(first, manager.spawn(BLOOD, owner, Vec3.ZERO, 0.75, 100, 4));
        var next = manager.spawn(BLOOD, owner, Vec3.ZERO, 0.75, 100, 6);
        assertNotSame(first, next);
        assertEquals(4, first.damage());
        assertEquals(6, next.damage());
    }
}
