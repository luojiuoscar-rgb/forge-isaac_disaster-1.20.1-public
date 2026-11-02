package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.BloodPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.minecraft.server.level.ServerPlayer;

public final class BloodTrail implements IAbilityEffect {
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
