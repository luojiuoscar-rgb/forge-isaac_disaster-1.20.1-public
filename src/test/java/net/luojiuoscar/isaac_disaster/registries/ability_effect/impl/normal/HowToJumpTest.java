package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HowToJumpTest {
    @Test
    void followsFullViewWithASeparateUpwardImpulse() {
        assertEquals(new Vec3(0, .75, 1.5), HowToJump.launchVelocity(new Vec3(0, 0, 1)));
        assertEquals(new Vec3(1.5, .75, 0), HowToJump.launchVelocity(new Vec3(1, 0, 0)));
        assertEquals(new Vec3(0, 2.25, 0), HowToJump.launchVelocity(new Vec3(0, 1, 0)));
        assertEquals(new Vec3(0, -.75, 0), HowToJump.launchVelocity(new Vec3(0, -1, 0)));
        Vec3 diagonal = new Vec3(1, 1, 1).normalize();
        assertEquals(diagonal.scale(1.5).add(0, .75, 0), HowToJump.launchVelocity(diagonal));
    }

    @Test
    void registrationUsesANewOrdinalAndQualityOne() {
        assertEquals(ItemId.URANUS.ordinal() + 1, ItemId.HOW_TO_JUMP.getId());
        assertEquals(ItemId.HOW_TO_JUMP.ordinal(), ItemId.HOW_TO_JUMP.getId());
        assertEquals(1, ItemId.HOW_TO_JUMP.getLevel());
        var ability = new net.luojiuoscar.isaac_disaster.registries.ability.active.impl.HowToJump(0, 1);
        assertEquals(1, ability.getNormalAmplifier());
        assertEquals(1, ability.getStrongerAmplifier());
    }
}
