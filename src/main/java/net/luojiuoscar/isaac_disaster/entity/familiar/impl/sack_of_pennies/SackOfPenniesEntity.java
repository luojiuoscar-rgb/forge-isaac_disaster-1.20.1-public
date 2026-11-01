package net.luojiuoscar.isaac_disaster.entity.familiar.impl.sack_of_pennies;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.item_giving.AbstractItemGivingFamiliarEntity;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationMode;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public final class SackOfPenniesEntity extends AbstractItemGivingFamiliarEntity {
    public SackOfPenniesEntity(EntityType<? extends SackOfPenniesEntity> type, Level level) { super(type, level); }
    @Override protected int initialProductionTicks() { return StatManager.getTimeInterval(6); }
    @Override protected int nextProductionTicks() { return StatManager.getTimeInterval(6, 8, random); }

    @Override
    protected List<ItemStack> generateItems(ServerPlayer owner, int quantity) {
        LootParams params = new LootParams.Builder(owner.serverLevel())
                .withParameter(LootContextParams.ORIGIN, owner.position())
                .withParameter(LootContextParams.THIS_ENTITY, owner).create(LootContextParamSets.EMPTY);
        return LootHelper.getLoot(owner.serverLevel(), ModLootTables.RANDOM_COINS, params, LootGenerationMode.SPAWN_DROP, quantity);
    }
}
