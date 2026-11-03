package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.BloodPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

public final class BloodTrail implements IAbilityEffect {
    private static final DustParticleOptions BLOOD_PARTICLE =
            new DustParticleOptions(new Vector3f(0.72F, 0.04F, 0.06F), 0.7F);

    /** Sends the shared bleeding particle even when no ground trail can be created. */
    public static void sendParticles(ServerPlayer player) {
        player.serverLevel().sendParticles(BLOOD_PARTICLE,
                player.getX(), player.getY() + player.getBbHeight() * 0.4, player.getZ(),
                1, player.getBbWidth() * 0.3, player.getBbHeight() * 0.2, player.getBbWidth() * 0.3, 0.01);
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return false;
        GroundEffectRuntime.INSTANCE.spawnOnGround(ModGroundEffects.BLOOD.get(), player, player.position(),
                BloodPuddleEffect.RADIUS, BloodPuddleEffect.LIFETIME, damage());
        return true;
    }

    public static float damage() { return damageFromBonus(StatManager.DAMAGE.getBonus()); }

    static float damageFromBonus(double bonus) {
        return Double.isFinite(bonus) ? (float) Math.min(Float.MAX_VALUE, Math.max(0, 2 * bonus)) : 0;
    }
}
