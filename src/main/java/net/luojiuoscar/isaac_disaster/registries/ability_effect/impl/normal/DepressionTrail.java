package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.DepressionPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.minecraft.server.level.ServerPlayer;

public final class DepressionTrail implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return false;
        GroundEffectRuntime.INSTANCE.spawnOnGround(ModGroundEffects.DEPRESSION.get(), player,
                context.getOrDefault(ContextKeys.TARGET_POSITION, player.position()),
                DepressionPuddleEffect.RADIUS, DepressionPuddleEffect.LIFETIME,
                damageFromBonus(StatManager.DAMAGE.getBonus(), context.getOrDefault(ContextKeys.AMPLIFIER, 1.0)));
        return true;
    }

    public static float damage() { return damageFromBonus(StatManager.DAMAGE.getBonus(), 1); }

    static float damageFromBonus(double bonus, double amplifier) {
        if (!Double.isFinite(bonus) || bonus <= 0 || !Double.isFinite(amplifier) || amplifier <= 0) return 0;
        return (float) Math.min(Float.MAX_VALUE, 2 * bonus * amplifier);
    }
}
