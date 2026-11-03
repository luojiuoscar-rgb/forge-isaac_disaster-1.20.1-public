package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class ShardOfGlass implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)
                || !(context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event)
                || event.getEntity() != player || event.isCanceled()
                || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0) return false;
        if (!player.isAlive() || player.isCreative() || player.isSpectator()) return true;
        if (!ModPeriodicModules.SHARD_OF_GLASS.get().activate(player)) return false;
        dropHeart(player, event.getAmount());
        return true;
    }

    public static void dropHeart(ServerPlayer player, double damage) {
        double chance = dropChance(damage, StatManager.MAX_HEALTH.getBonus());
        if (chance > 0 && player.getRandom().nextDouble() < chance) {
            player.spawnAtLocation(new ItemStack(ModItems.RED_HEART.get()));
        }
    }

    static double dropChance(double damage, double healthUnit) {
        if (!Double.isFinite(damage) || damage <= 0 || !Double.isFinite(healthUnit) || healthUnit <= 0) return 0;
        return 0.25 * Math.min(1, damage / (0.5 * healthUnit));
    }
}
