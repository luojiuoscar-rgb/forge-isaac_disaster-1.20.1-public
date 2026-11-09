package net.luojiuoscar.isaac_disaster.registries.periodic_module.impl;

import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModule;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BloodTrail;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class Anemic extends PeriodicModule {
    public static final int DURATION = 200;
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.BLOOD_TRAIL)
    ));
    // Compare server positions, since remote players can have zero horizontal velocity.
    private final Map<LivingEntity, Vec3> lastPositions = new WeakHashMap<>();

    public Anemic() { super(TRIGGER); }

    public boolean activate(ServerPlayer player) {
        if (player == null || !player.isAlive() || !player.getCapability(EffectModulesProvider.EFFECT_MODULES)
                .map(modules -> modules.getPeriodicModuleQueue().contains(ModPeriodicModules.ANEMIC.getId()))
                .orElse(false)) return false;
        return player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).map(data -> {
            data.setDouble(ModPeriodicModules.ANEMIC.getId(), DURATION);
            lastPositions.putIfAbsent(player, player.position());
            return true;
        }).orElse(false);
    }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, PeriodicModuleQueue queue) { return 2; }

    @Override
    public void fire(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player) || !player.isAlive()) {
            handleRemove(context.getEntity());
            return;
        }
        player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(data -> {
            var key = ModPeriodicModules.ANEMIC.getId();
            Double remaining = data.getDouble(key);
            if (remaining == null || remaining <= 0) {
                lastPositions.remove(player);
                return;
            }
            if (remaining <= 2) data.removeDouble(key);
            else data.setDouble(key, remaining - 2);
            BloodTrail.sendParticles(player);
            Vec3 previous = lastPositions.put(player, player.position());
            if (previous != null && previous.subtract(player.position()).horizontalDistanceSqr() > 1.0E-8D) {
                var blood = context.copy(player);
                blood.set(ContextKeys.AMPLIFIER, 1.0);
                super.fire(blood);
            }
        });
    }

    @Override
    public void handleRemove(LivingEntity entity) {
        lastPositions.remove(entity);
        entity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                .ifPresent(data -> data.removeDouble(ModPeriodicModules.ANEMIC.getId()));
    }
}
