package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public final class Anemic extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.ANEMIC, Anemic::eligible)
    ));

    public Anemic() { super(TRIGGER); }

    static boolean eligible(ExecutableEffectContext context) {
        return context.getEntity() instanceof ServerPlayer player && player.isAlive()
                && context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event
                && event.getEntity() == player && !event.isCanceled()
                && Float.isFinite(event.getAmount()) && event.getAmount() > 0;
    }
}
