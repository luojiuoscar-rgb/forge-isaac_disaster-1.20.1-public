package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public final class ShardOfGlassBloodTear implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return false;
        var state = createTear(player);
        if (BulletRuntime.INSTANCE.spawn(player.serverLevel(), state) == null) return false;
        return true;
    }

    /** Captures one pure tear without running the owner's attack or attachment pipeline. */
    public static BulletState createTear(ServerPlayer player) {
        var random = player.getRandom();
        var bullet = ModAttackTypes.BULLET.get();
        var attack = AttackContext.builder(player, player).attackType(bullet)
                .position(player.getBoundingBox().getCenter())
                .mainAxis(direction(random.nextDouble(), random.nextDouble()))
                .damage(damage(player.getAttributeValue(Attributes.ATTACK_DAMAGE), random.nextDouble()))
                .speed(0.4).range(bullet.getRange(player))
                .color(ModBulletColors.BLOOD_TEAR.getId()).build();
        attack.addTrajectoryModule(ModTrajectoryModules.GRAVITY.getId(), 1);
        return BulletState.from(attack).renderScale(attack.getBulletScale())
                .collisionWidth(attack.getBulletScale() * 0.2).collisionHeight(attack.getBulletScale() * 0.2)
                .lifetime((int) Math.max(1, Math.min(200, attack.getBulletRange() / attack.getBulletSpeed())))
                .build();
    }

    static Vec3 direction(double horizontalRoll, double elevationRoll) {
        double yaw = horizontalRoll * Math.PI * 2;
        double pitch = Math.toRadians(60 + elevationRoll * 25);
        return new Vec3(Math.cos(yaw) * Math.cos(pitch), Math.sin(pitch), Math.sin(yaw) * Math.cos(pitch));
    }

    static double damage(double panelDamage, double roll) {
        return Double.isFinite(panelDamage) ? Math.min(Float.MAX_VALUE, Math.max(0, panelDamage) * (0.67 + 0.34 * roll)) : 0;
    }
}
