package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.DepressionTrail;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public final class Depression extends PassiveAbility {
    public Depression(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {}

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        var familiarId = ModFamiliarEntities.DEPRESSION.getId();
        StatManager.addFamiliar(player, familiarId, familiarId, 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        var familiarId = ModFamiliarEntities.DEPRESSION.getId();
        StatManager.addFamiliar(player, familiarId, familiarId, -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(Component.translatable("item.isaac_disaster.depression.lore.1"),
                Component.translatable("item.isaac_disaster.depression.lore.2"),
                Component.translatable("item.isaac_disaster.depression.lore.3", DescriptionHelper.dynamicNumber(DepressionTrail.damage())),
                Component.translatable("item.isaac_disaster.depression.lore.4"),
                Component.translatable("item.isaac_disaster.depression.lore.5"));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.BFFS.getId()) <= 0) return List.of();
        return List.of(DescriptionHelper.getSynergyDesc(Component.translatable("item.isaac_disaster.bffs"),
                Component.translatable("item.isaac_disaster.depression.synergy.bffs")));
    }
}
