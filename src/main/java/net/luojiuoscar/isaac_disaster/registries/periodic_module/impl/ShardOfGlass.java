package net.luojiuoscar.isaac_disaster.registries.periodic_module.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BloodTrail;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModule;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class ShardOfGlass extends PeriodicModule {
    private static final ResourceLocation HEALTH = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "shard_of_glass_health");
    private static final ResourceLocation SHOT_COOLDOWN = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "shard_of_glass_shot_cooldown");
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.SHARD_OF_GLASS_BLOOD_TEAR)
    ));
    private final Map<LivingEntity, Vec3> lastPositions = new WeakHashMap<>();

    public ShardOfGlass() { super(TRIGGER); }

    public boolean activate(ServerPlayer player) {
        if (!player.getCapability(EffectModulesProvider.EFFECT_MODULES)
                .map(modules -> modules.getPeriodicModuleQueue().contains(ModPeriodicModules.SHARD_OF_GLASS.getId()))
                .orElse(false)) return false;
        return player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).map(data -> {
            if (data.getDouble(ModPeriodicModules.SHARD_OF_GLASS.getId()) == null) {
                data.setDouble(ModPeriodicModules.SHARD_OF_GLASS.getId(), 400);
                data.setDouble(HEALTH, player.getHealth());
                data.setDouble(SHOT_COOLDOWN, 0);
                lastPositions.put(player, player.position());
            }
            return true;
        }).orElse(false);
    }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, PeriodicModuleQueue queue) { return 1; }

    @Override
    public void fire(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player) || !player.isAlive()
                || player.isCreative() || player.isSpectator()) {
            handleRemove(context.getEntity());
            return;
        }
        player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(data -> {
            var key = ModPeriodicModules.SHARD_OF_GLASS.getId();
            Double remaining = data.getDouble(key);
            if (remaining == null) return;
            Double previousHealth = data.getDouble(HEALTH);
            Double shotCooldown = data.getDouble(SHOT_COOLDOWN);
            boolean sampleHealth = remaining.intValue() % 2 == 0;
            if (!Double.isFinite(remaining) || remaining <= 0 || previousHealth == null
                    || !Double.isFinite(previousHealth) || shotCooldown == null || !Double.isFinite(shotCooldown)
                    || sampleHealth && player.getHealth() > previousHealth) {
                handleRemove(player);
                return;
            }
            if (sampleHealth) data.setDouble(HEALTH, player.getHealth());
            if (remaining <= 1) {
                double cost = StatManager.MAX_HEALTH.getBonus() * 0.5;
                if (Double.isFinite(cost) && cost > 0) {
                    if (player.getHealth() <= cost) {
                        handleRemove(player);
                        return;
                    }
                    float before = player.getHealth();
                    player.setHealth((float) (before - cost));
                    float lost = before - player.getHealth();
                    // Baseline precedes the notification so healing from a synergy is detectable next pulse.
                    data.setDouble(HEALTH, player.getHealth());
                    data.setDouble(key, 400);
                    MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, PlayerHelper.getSelfDamageSource(player), lost));
                    if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
                        handleRemove(player);
                        return;
                    }
                    if (data.getDouble(key) == null) return;
                } else data.setDouble(key, 400);
            } else {
                data.setDouble(key, remaining - 1);
            }
            if (shotCooldown <= 1) {
                data.setDouble(SHOT_COOLDOWN, 3);
                super.fire(context);
            } else data.setDouble(SHOT_COOLDOWN, shotCooldown - 1);
            if (!sampleHealth) return;
            BloodTrail.sendParticles(player);
            Vec3 previous = lastPositions.put(player, player.position());
            if (previous != null && previous.subtract(player.position()).horizontalDistanceSqr() > 1.0E-8) {
                var blood = context.copy(player);
                blood.set(ContextKeys.AMPLIFIER, 1.0);
                ModExecutableEffects.BLOOD_TRAIL.get().apply(blood);
            }
        });
    }

    @Override
    public void handleRemove(LivingEntity entity) {
        lastPositions.remove(entity);
        entity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(data -> {
            data.removeDouble(ModPeriodicModules.SHARD_OF_GLASS.getId());
            data.removeDouble(HEALTH);
            data.removeDouble(SHOT_COOLDOWN);
        });
    }
}
