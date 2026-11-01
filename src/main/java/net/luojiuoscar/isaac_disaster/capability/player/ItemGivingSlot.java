package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Persistent production state; its list position selects a presenter, never a persistent entity identity. */
public final class ItemGivingSlot {
    private final ResourceLocation source;
    private final List<ItemStack> inventory = new ArrayList<>();
    private int cooldown;
    private long lastTick;

    ItemGivingSlot(ResourceLocation source, int cooldown, long gameTime) {
        this.source = Objects.requireNonNull(source);
        this.cooldown = Math.max(0, cooldown);
        this.lastTick = gameTime;
    }

    ResourceLocation source() { return source; }
    public int cooldown() { return cooldown; }
    public boolean hasItems() { return !inventory.isEmpty(); }
    /** Advances at most once per game tick, even when runtime reordering changes its presenter. */
    public boolean tick(long gameTime) {
        if (lastTick == gameTime) return false;
        lastTick = gameTime;
        if (cooldown > 0) cooldown--;
        return true;
    }

    public void produce(List<ItemStack> generated, int nextCooldown) {
        for (ItemStack stack : generated) addItems(stack);
        cooldown = Math.max(1, nextCooldown);
    }

    private void addItems(ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemStack remaining = stack.copy();
        for (ItemStack existing : inventory) {
            if (!ItemStack.isSameItemSameTags(existing, remaining)) continue;
            int amount = Math.min(remaining.getCount(), Math.max(0, existing.getMaxStackSize() - existing.getCount()));
            existing.grow(amount);
            remaining.shrink(amount);
            if (remaining.isEmpty()) return;
        }
        while (!remaining.isEmpty()) inventory.add(remaining.split(Math.max(1, remaining.getMaxStackSize())));
    }

    /** A rejected world join leaves the original stack available for the next delivery attempt. */
    public void deliver(Predicate<ItemStack> delivery) {
        for (ItemStack stack : List.copyOf(inventory)) {
            if (delivery.test(stack.copy())) inventory.remove(stack);
        }
    }

    CompoundTag saveNBT() {
        var tag = new CompoundTag();
        tag.putString("Source", source.toString());
        tag.putInt("Cooldown", cooldown);
        var items = new ListTag();
        for (ItemStack stack : inventory) items.add(stack.save(new CompoundTag()));
        tag.put("Items", items);
        return tag;
    }

    @Nullable
    static ItemGivingSlot loadNBT(CompoundTag tag) {
        ResourceLocation source = ResourceLocation.tryParse(tag.getString("Source"));
        if (source == null) return null;
        var result = new ItemGivingSlot(source, tag.getInt("Cooldown"), Long.MIN_VALUE);
        for (Tag value : tag.getList("Items", Tag.TAG_COMPOUND))
            result.addItems(ItemStack.of((CompoundTag) value));
        return result;
    }
}
