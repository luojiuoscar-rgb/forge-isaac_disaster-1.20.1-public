package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

/** Player-owned production records, indexed by familiar type and current type-local order. */
public final class ItemGivingFamiliarData {
    private final Map<ResourceLocation, List<ItemGivingSlot>> records = new LinkedHashMap<>();

    @Nullable
    public ItemGivingSlot get(ResourceLocation type, int slot) {
        List<ItemGivingSlot> slots = records.get(type);
        return slots == null || slot < 0 || slot >= slots.size() ? null : slots.get(slot);
    }

    /** Initializes records only when a successfully admitted entity first runs its behavior. */
    @Nullable
    public ItemGivingSlot getOrCreate(FamiliarEntry entry, int slot, IntSupplier initialCooldown, long gameTime) {
        if (slot < 0 || slot >= Math.min(entry.getCount(), entry.getEntityCount())) return null;
        List<ItemGivingSlot> slots = records.computeIfAbsent(entry.getType(), ignored -> new ArrayList<>());
        if (slot < slots.size()) return slots.get(slot);
        var remaining = new LinkedHashMap<>(entry.requirements().getSources());
        for (ItemGivingSlot record : slots) remaining.computeIfPresent(record.source(), (source, count) -> count - 1);
        while (slots.size() <= slot) {
            ResourceLocation source = entry.getEntitySource(entry.getEntityId(slots.size()));
            if (remaining.getOrDefault(source, 0) <= 0) {
                source = remaining.entrySet().stream().filter(contribution -> contribution.getValue() > 0)
                        .map(Map.Entry::getKey).findFirst().orElse(null);
            }
            if (source == null) return null;
            slots.add(new ItemGivingSlot(source, Math.max(1, initialCooldown.getAsInt()), gameTime));
            remaining.put(source, remaining.get(source) - 1);
        }
        return slots.get(slot);
    }

    /** Only lost source contributions delete records; quota loss and runtime reordering preserve them. */
    void reconcile(Map<ResourceLocation, FamiliarEntry> familiars) {
        records.entrySet().removeIf(item -> {
            FamiliarEntry entry = familiars.get(item.getKey());
            if (entry == null) return true;
            var remaining = new LinkedHashMap<>(entry.requirements().getSources());
            item.getValue().removeIf(record -> {
                int allowed = remaining.getOrDefault(record.source(), 0);
                if (allowed == 0) return true;
                remaining.put(record.source(), allowed - 1);
                return false;
            });
            return item.getValue().isEmpty();
        });
    }

    ListTag saveNBT() {
        var result = new ListTag();
        records.forEach((type, slots) -> {
            var tag = new CompoundTag();
            tag.putString("Type", type.toString());
            var saved = new ListTag();
            slots.forEach(slot -> saved.add(slot.saveNBT()));
            tag.put("Slots", saved);
            result.add(tag);
        });
        return result;
    }

    void loadNBT(ListTag tags) {
        records.clear();
        for (Tag value : tags) {
            var tag = (CompoundTag) value;
            ResourceLocation type = ResourceLocation.tryParse(tag.getString("Type"));
            if (type == null) continue;
            List<ItemGivingSlot> slots = new ArrayList<>();
            for (Tag saved : tag.getList("Slots", Tag.TAG_COMPOUND)) {
                ItemGivingSlot slot = ItemGivingSlot.loadNBT((CompoundTag) saved);
                if (slot != null) slots.add(slot);
            }
            if (!slots.isEmpty()) records.putIfAbsent(type, slots);
        }
    }
}
