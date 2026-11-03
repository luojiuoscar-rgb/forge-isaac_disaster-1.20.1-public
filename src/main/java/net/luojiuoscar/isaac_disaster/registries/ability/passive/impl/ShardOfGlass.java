package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public final class ShardOfGlass extends PassiveAbility {
    public ShardOfGlass(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {}

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addPeriodicModule(player, ModPeriodicModules.SHARD_OF_GLASS.getId(), 1);
        StatManager.addTriggerModule(player, ModTriggerModules.SHARD_OF_GLASS.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.SHARD_OF_GLASS.getId(), -1);
        StatManager.addPeriodicModule(player, ModPeriodicModules.SHARD_OF_GLASS.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        String halfHealth = String.format(Locale.ROOT, "%.2f", Math.max(0, StatManager.MAX_HEALTH.getBonus() * 0.5));
        return List.of(Component.translatable("item.isaac_disaster.shard_of_glass.lore.1"),
                Component.translatable("item.isaac_disaster.shard_of_glass.lore.2"),
                Component.translatable("item.isaac_disaster.shard_of_glass.lore.3"),
                Component.translatable("item.isaac_disaster.shard_of_glass.lore.4"),
                Component.translatable("item.isaac_disaster.shard_of_glass.lore.5", halfHealth),
                Component.translatable("item.isaac_disaster.shard_of_glass.lore.6", halfHealth));
    }
}
