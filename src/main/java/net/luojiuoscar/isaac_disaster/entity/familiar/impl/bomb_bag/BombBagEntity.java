package net.luojiuoscar.isaac_disaster.entity.familiar.impl.bomb_bag;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.item_giving.AbstractItemGivingFamiliarEntity;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public final class BombBagEntity extends AbstractItemGivingFamiliarEntity {
    public BombBagEntity(EntityType<? extends BombBagEntity> type, Level level) { super(type, level); }
    @Override protected int initialProductionTicks() { return StatManager.getTimeInterval(6); }
    @Override protected int nextProductionTicks() { return StatManager.getTimeInterval(6, 8, random); }
    @Override protected List<ItemStack> generateItems(ServerPlayer owner, int quantity) {
        return quantity > 0 ? List.of(new ItemStack(ModItems.BOMB.get(), quantity)) : List.of();
    }
}
