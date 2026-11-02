package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.minecraft.server.level.ServerPlayer;

public final class Anemic implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        return context.getEntity() instanceof ServerPlayer player && ModPeriodicModules.ANEMIC.get().activate(player);
    }
}
