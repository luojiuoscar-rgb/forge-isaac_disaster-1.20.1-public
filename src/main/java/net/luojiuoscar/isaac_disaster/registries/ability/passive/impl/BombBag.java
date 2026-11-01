package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BombBag extends PassiveAbility {
    public BombBag(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addFamiliar(player, ModFamiliarEntities.BOMB_BAG.getId(), ModPassiveItems.BOMB_BAG.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addFamiliar(player, ModFamiliarEntities.BOMB_BAG.getId(), ModPassiveItems.BOMB_BAG.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(Component.translatable("item.isaac_disaster.bomb_bag.lore.1"),
                Component.translatable("item.isaac_disaster.bomb_bag.lore.2"));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.BFFS.getId()) > 0) {
            return List.of(DescriptionHelper.getSynergyDesc(Component.translatable("item.isaac_disaster.bffs"),
                    Component.translatable("item.isaac_disaster.familiar.synergy.double_production")));
        }
        return List.of();
    }

}
