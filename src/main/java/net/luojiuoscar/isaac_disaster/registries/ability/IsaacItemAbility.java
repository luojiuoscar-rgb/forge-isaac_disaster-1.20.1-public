package net.luojiuoscar.isaac_disaster.registries.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class IsaacItemAbility {
    private final int id;
    private final int level;

    public IsaacItemAbility(int id, int level){
        this.id = id;
        this.level = level;
    }

    public final int getId() {
        return id;
    }

    public final int getLevel() {
        return level;
    }

    abstract public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player);

    /** 仅用于解释套装效果、药水效果等专有名词，不用于长篇说明道具自身的具体效果。 */
    public List<Component> getExtraDesc(@Nullable ItemStack stack, @Nullable Player player){
        return List.of();
    }

    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player){
        return List.of();
    }
}
