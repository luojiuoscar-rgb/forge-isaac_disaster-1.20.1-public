package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl;

import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class BloodPuddleEffect extends AbstractGroundEffect {
    public static final double RADIUS = 0.75D;
    public static final int LIFETIME = 100;

    public BloodPuddleEffect(GroundEffectType<BloodPuddleEffect> type, int slot, int generation,
                             @Nullable UUID owner, Vec3 position, double radius, int lifetime,
                             float damage, long order, int animationDurationTicks) {
        super(type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks);
    }

    @Override
    public int getColor() { return 0xB82025; }

    @Override
    public void onContactDamage(GroundEffectContact contact) {
        var owner = contact.owner();
        var target = contact.target();
        if (owner == null || !owner.isAlive() || owner.level() != contact.level()
                || !target.isAlive() || target.isInvulnerable() || damage() <= 0) return;
        target.hurt(ModDamageSources.water(contact.level(), owner), damage());
    }
}
