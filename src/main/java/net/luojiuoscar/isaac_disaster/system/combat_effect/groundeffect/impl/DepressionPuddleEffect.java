package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl;

import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.HolyLight;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class DepressionPuddleEffect extends AbstractGroundEffect {
    public static final double RADIUS = 0.75;
    public static final int LIFETIME = 100;

    public DepressionPuddleEffect(GroundEffectType<DepressionPuddleEffect> type, int slot, int generation,
                                  @Nullable UUID owner, Vec3 position, double radius, int lifetime,
                                  float damage, long order, int animationDurationTicks) {
        super(type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks);
    }

    @Override
    public int getColor() { return 0x3C9BE8; }

    @Override
    public void onContactDamage(GroundEffectContact contact) {
        var target = contact.target();
        if (!(contact.owner() instanceof ServerPlayer owner) || !owner.isAlive() || owner.level() != contact.level()
                || !target.isAlive() || target.isInvulnerable() || damage() <= 0) return;
        Vec3 position = target.position();
        if (!target.hurt(ModDamageSources.water(contact.level(), owner), damage())) return;
        if (owner.isAlive() && owner.level() == contact.level()
                //&& procs(owner.getRandom().nextDouble())
        ) {
            HolyLight.strike(contact.level(), owner, position,
                    holyDamage(owner.getAttributeValue(Attributes.ATTACK_DAMAGE)), true);
        }
    }

    static boolean procs(double roll) { return roll >= 0 && roll < 0.01; }

    static float holyDamage(double damage) {
        return Double.isFinite(damage) && damage > 0 ? (float) Math.min(Float.MAX_VALUE, damage * 3) : 0;
    }
}
