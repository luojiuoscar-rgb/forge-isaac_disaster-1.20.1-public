package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarLimitGroup;
import net.luojiuoscar.isaac_disaster.test.MinecraftBootstrapExtension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MinecraftBootstrapExtension.class)
class FamiliarGivingSlotsTest {
    private static final ResourceLocation BAG = ResourceLocation.parse("test:bag");
    private static final ResourceLocation OTHER = ResourceLocation.parse("test:other");
    private static final ResourceLocation A = ResourceLocation.parse("test:a");
    private static final ResourceLocation B = ResourceLocation.parse("test:b");

    @Test
    void removingFirstSourceCompactsRemainingInventoryAndCooldownTogether() {
        var data = data(4);
        data.setCount(BAG, A, 1);
        data.setCount(BAG, B, 1);
        add(data, BAG, A, 10);
        var remaining = add(data, BAG, B, 20);
        remaining.produce(List.of(new ItemStack(Items.DIAMOND, 3)), 17);
        data.setCount(BAG, A, 0);
        assertSame(remaining, state(data, BAG, 0));
        assertNull(state(data, BAG, 1));
        assertEquals(17, remaining.cooldown());
        assertEquals(3, inventory(remaining).get(0).getCount());
    }

    @Test
    void newRecordsUseTheAdmittedSourceRatherThanAnUnspawnedContribution() {
        var data = data(2);
        data.setCount(BAG, A, 2);
        data.setCount(BAG, B, 1);
        var active = add(data, BAG, B, 10);
        active.produce(List.of(new ItemStack(Items.EMERALD, 2)), 17);
        data.setCount(BAG, A, 0);
        assertSame(active, state(data, BAG, 0), "Removing an unspawned source must not delete another source's inventory");
        assertEquals(B, active.source());
        assertEquals(17, active.cooldown());
        assertEquals(2, inventory(active).get(0).getCount());
    }

    @Test
    void immediateQuotaPreviewLeavesProductionRecordsUntouched() {
        var data = data(4);
        data.setCount(BAG, A, 1);
        var slot = add(data, BAG, A, 10);
        slot.produce(List.of(new ItemStack(Items.DIAMOND, 3)), 7);
        try (var reservation = data.reserveSpawn(OTHER, B, UUID.randomUUID())) {
            assertNotNull(reservation);
            assertTrue(reservation.commit());
        }
        assertSame(slot, state(data, BAG, 0));
        assertEquals(7, slot.cooldown());
        assertEquals(3, inventory(slot).get(0).getCount());
    }

    @Test
    void playerRoundTripPreservesProductionWithoutRuntimeEntities() {
        var data = data(4);
        data.setCount(BAG, A, 1);
        var slot = add(data, BAG, A, 8);
        slot.produce(List.of(new ItemStack(Items.DIAMOND, 3)), 42);
        CompoundTag saved = new CompoundTag();
        data.saveNBTData(saved);
        var restored = data(4);
        restored.loadNBTData(saved);
        var record = state(restored, BAG, 0);
        assertEquals(0, restored.getEntry(BAG).orElseThrow().getEntityCount());
        assertEquals(42, record.cooldown());
        assertEquals(3, inventory(record).get(0).getCount());
        assertTrue(record.tick(0), "Tick guards are runtime-only and do not advance offline production");
    }

    @Test
    void runtimeRebuildCanAssignDifferentEntitiesToTheSameRecords() {
        var data = data(4);
        data.setCount(BAG, A, 2);
        var first = add(data, BAG, A, 8);
        var second = add(data, BAG, A, 9);
        first.produce(List.of(new ItemStack(Items.DIAMOND, 3)), 7);
        second.produce(List.of(new ItemStack(Items.EMERALD, 2)), 6);
        data.clearRuntimeEntities();
        UUID replacement = UUID.randomUUID();
        data.registerEntity(BAG, A, replacement);
        assertSame(first, data.getItemGivingData().getOrCreate(data.getEntry(BAG).orElseThrow(), 0,
                () -> fail("Existing records retain their timers"), 100));
        assertSame(second, state(data, BAG, 1));
        assertEquals(7, first.cooldown());
        assertEquals(3, inventory(first).get(0).getCount());
        data.registerEntity(BAG, A, UUID.randomUUID());
        data.removeEntity(BAG, replacement);
        assertSame(first, state(data, BAG, 0), "Runtime order changes do not move or delete player inventory");
        assertSame(second, state(data, BAG, 1));
    }

    @Test
    void typesStayIsolatedAndNewRecordsStartEmptyAfterCompaction() {
        var data = data(8);
        data.setCount(BAG, A, 1); data.setCount(BAG, B, 1); data.setCount(OTHER, A, 1);
        add(data, BAG, A, 10);
        var second = add(data, BAG, B, 20);
        var other = add(data, OTHER, A, 30);
        second.produce(List.of(new ItemStack(Items.GOLD_INGOT)), 9);
        UUID removed = data.getEntry(BAG).orElseThrow().getEntityId(0);
        data.setCount(BAG, A, 0);
        data.removeEntity(BAG, removed);
        assertSame(second, state(data, BAG, 0));
        assertSame(other, state(data, OTHER, 0));
        data.setCount(BAG, A, 1);
        var fresh = add(data, BAG, A, 14);
        assertSame(fresh, state(data, BAG, 1));
        assertFalse(fresh.hasItems()); assertEquals(14, fresh.cooldown());
        assertEquals(1, inventory(second).get(0).getCount());
        assertEquals(A, fresh.source());
    }

    @Test
    void highestSourceRecordsAreDeletedOnlyWhenRealDemandFallsBelowTheirNumber() {
        var data = data(3);
        data.setCount(BAG, A, 100);
        var zero = add(data, BAG, A, 10);
        var one = add(data, BAG, A, 10);
        var two = add(data, BAG, A, 10);
        two.produce(List.of(new ItemStack(Items.DIAMOND, 2)), 5);
        data.addCount(BAG, A, -1);
        assertSame(two, state(data, BAG, 2));
        data.setCount(BAG, A, 2);
        assertNull(state(data, BAG, 2));
        assertSame(zero, state(data, BAG, 0));
        assertSame(one, state(data, BAG, 1));
        data.clearRequirements();
        assertNull(state(data, BAG, 0));
    }

    @Test
    void quotaLossPreservesRecordsAndCopyDeepCopiesInventory() {
        FamiliarLimitGroup group = new FamiliarLimitGroup(1);
        var low = new FamiliarEntityType(() -> null, 64, () -> group, 0, true);
        var high = new FamiliarEntityType(() -> null, 64, () -> group, 1, true);
        var types = Map.of(BAG, low, OTHER, high);
        var data = new PlayerFamiliarData(types::get, List.of(group));
        data.setCount(BAG, A, 1);
        var slot = add(data, BAG, A, 6);
        slot.produce(List.of(new ItemStack(Items.DIAMOND, 3)), 8);
        data.setCount(OTHER, A, 1);
        assertEquals(0, data.getEffectiveCount(BAG));
        assertSame(slot, state(data, BAG, 0));
        var clone = new PlayerFamiliarData(types::get, List.of(group));
        clone.copyFrom(data);
        var copied = state(clone, BAG, 0);
        assertNotSame(slot, copied); assertEquals(8, copied.cooldown());
        assertEquals(3, inventory(copied).get(0).getCount());
        slot.produce(List.of(new ItemStack(Items.DIAMOND)), 2);
        assertEquals(3, inventory(copied).get(0).getCount());
    }

    @Test
    void compactionAndNewPresentersCannotAdvanceOneRecordTwiceInATick() {
        var data = data(4);
        data.setCount(BAG, A, 1); data.setCount(BAG, B, 1);
        var a = add(data, BAG, A, 2);
        var b = add(data, BAG, B, 5);
        assertTrue(a.tick(100)); assertFalse(a.tick(100));
        assertTrue(b.tick(100)); assertEquals(4, b.cooldown());
        data.setCount(BAG, A, 0);
        assertSame(b, state(data, BAG, 0));
        assertFalse(state(data, BAG, 0).tick(100));
        assertTrue(b.tick(101)); assertEquals(3, b.cooldown());
        b.produce(List.of(new ItemStack(Items.DIAMOND)), 0);
        assertEquals(1, b.cooldown());
        assertTrue(b.tick(102)); assertEquals(0, b.cooldown());
        assertTrue(b.hasItems());
    }

    @Test
    void onlyAdmittedTypeLocalIndexesCanInitializeProduction() {
        var data = data(2); data.setCount(BAG, A, 2);
        var entry = data.getEntry(BAG).orElseThrow();
        assertNull(data.getItemGivingData().getOrCreate(entry, 0, () -> 8, 100));
        data.registerEntity(BAG, A, UUID.randomUUID());
        assertNull(data.getItemGivingData().getOrCreate(entry, -1, () -> 8, 100));
        assertNull(data.getItemGivingData().getOrCreate(entry, 1, () -> 8, 100));
        var slot = data.getItemGivingData().getOrCreate(entry, 0, () -> 0, 100);
        assertEquals(1, slot.cooldown());
        assertFalse(slot.tick(100));
        assertTrue(slot.tick(101));
    }

    @Test
    void inventoryStacksRespectNbtAndRejectedDeliveryKeepsUnacceptedStacks() {
        var data = data(1); data.setCount(BAG, A, 1);
        var slot = add(data, BAG, A, 1);
        var named = new ItemStack(Items.DIAMOND, 3);
        named.getOrCreateTag().putString("name", "other");
        slot.produce(List.of(new ItemStack(Items.DIAMOND, 63)), 2);
        slot.produce(List.of(new ItemStack(Items.DIAMOND, 4), named), 3);
        assertEquals(List.of(64, 3, 3), inventory(slot).stream().map(ItemStack::getCount).toList());
        slot.deliver(stack -> { stack.setCount(1); return false; });
        assertEquals(List.of(64, 3, 3), inventory(slot).stream().map(ItemStack::getCount).toList());
        slot.deliver(stack -> !stack.hasTag());
        assertEquals(1, inventory(slot).size()); assertEquals(3, inventory(slot).get(0).getCount());
        slot.deliver(stack -> true);
        assertFalse(slot.hasItems()); assertEquals(3, slot.cooldown());
    }

    private static List<ItemStack> inventory(ItemGivingSlot slot) {
        List<ItemStack> items = new ArrayList<>();
        slot.deliver(stack -> { items.add(stack); return false; });
        return items;
    }

    private static PlayerFamiliarData data(int capacity) {
        FamiliarLimitGroup group = new FamiliarLimitGroup(capacity);
        var type = new FamiliarEntityType(() -> null, 64, () -> group, 0, true);
        return new PlayerFamiliarData(Map.of(BAG, type, OTHER, type)::get, List.of(group));
    }

    private static ItemGivingSlot state(PlayerFamiliarData data, ResourceLocation type, int index) {
        return data.getItemGivingData().get(type, index);
    }

    private static ItemGivingSlot add(PlayerFamiliarData data, ResourceLocation type, ResourceLocation source, int cooldown) {
        data.registerEntity(type, source, UUID.randomUUID());
        var entry = data.getEntry(type).orElseThrow();
        return data.getItemGivingData().getOrCreate(entry, entry.getEntityCount() - 1, () -> cooldown, 0);
    }
}
