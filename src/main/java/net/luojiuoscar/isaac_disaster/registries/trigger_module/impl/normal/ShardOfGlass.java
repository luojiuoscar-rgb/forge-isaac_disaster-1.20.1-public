package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;

import java.util.List;

public final class ShardOfGlass extends TriggerModule {
    private static final IAbilityEffect STOP = context -> {
        ModPeriodicModules.SHARD_OF_GLASS.get().handleRemove(context.getEntity());
        return true;
    };
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.SHARD_OF_GLASS),
            new SimpleTrigger(ModTriggerTypes.DEATH, () -> STOP)
    ));

    public ShardOfGlass() { super(TRIGGER); }
}
