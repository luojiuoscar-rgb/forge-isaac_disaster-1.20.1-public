package net.luojiuoscar.isaac_disaster.entity.familiar.base.item_giving;

import net.luojiuoscar.isaac_disaster.capability.player.FamiliarEntry;
import net.luojiuoscar.isaac_disaster.capability.player.ItemGivingSlot;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.chain.AbstractChainFamiliarEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractItemGivingFamiliarEntity extends AbstractChainFamiliarEntity {
    protected AbstractItemGivingFamiliarEntity(EntityType<?> type, Level level) { super(type, level); }

    protected abstract int initialProductionTicks();
    protected abstract int nextProductionTicks();
    protected abstract List<ItemStack> generateItems(ServerPlayer owner, int quantity);

    @Nullable
    private ItemGivingSlot slotState() {
        if (getOwner() == null) return null;
        var data = getOwner().getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElse(null);
        FamiliarEntry entry = data == null ? null : data.getEntry(getFamiliarType()).orElse(null);
        return entry == null ? null
                : data.getItemGivingData().get(getFamiliarType(), entry.getEntityIds().indexOf(getUUID()));
    }

    @Override
    protected final void tickFamiliar() {
        if (level().isClientSide || !(getOwner() instanceof ServerPlayer owner) || !owner.isAlive()) {
            super.tickFamiliar();
            return;
        }
        var data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElse(null);
        FamiliarEntry entry = data == null ? null : data.getEntry(getFamiliarType()).orElse(null);
        if (entry == null) return;
        int index = entry.getEntityIds().indexOf(getUUID());
        if (index < 0 || index >= data.getEffectiveCount(getFamiliarType())) return;
        ItemGivingSlot state = data.getItemGivingData().getOrCreate(entry, index,
                this::initialProductionTicks, level().getGameTime());
        if (state == null || !state.tick(level().getGameTime())) return;
        if (state.cooldown() == 0) {
            double enhanced = 1 + getEnhancement();
            int quantity = Double.isFinite(enhanced) ? (int) Math.max(0, Math.min(Integer.MAX_VALUE, Math.floor(enhanced))) : 0;
            List<ItemStack> generated = generateItems(owner, quantity);
            if (slotState() != state || isRemoved()) return;
            state.produce(generated, nextProductionTicks());
        }
        super.tickFamiliar();
    }

    @Override
    protected final Vec3 followingPosition(ServerPlayer owner) {
        ItemGivingSlot state = slotState();
        return state != null && state.hasItems() ? owner.position() : super.followingPosition(owner);
    }

    @Override
    protected final void tickChainServer(ServerPlayer owner, Vec3 movement) {
        super.tickChainServer(owner, movement);
        ItemGivingSlot state = slotState();
        if (state != null && state.hasItems() && position().distanceToSqr(owner.position()) < 1) {
            state.deliver(stack -> {
                if (isRemoved() || isRecallPending() || !owner.isAlive() || slotState() != state) return false;
                ItemEntity drop = new ItemEntity(owner.serverLevel(), owner.getX(), owner.getY(), owner.getZ(), stack);
                drop.setNoPickUpDelay();
                return owner.serverLevel().addFreshEntity(drop);
            });
        }
    }
}
