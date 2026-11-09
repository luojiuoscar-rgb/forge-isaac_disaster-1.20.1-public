package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class ShardOfGlassItemIdTest {
    @Test
    void itemAppendsAfterAnemicWithQualityOneAndOrdinalId() {
        assertEquals(1, ItemId.SHARD_OF_GLASS.getLevel());
        assertEquals(ItemId.ANEMIC.ordinal() + 1, ItemId.SHARD_OF_GLASS.ordinal());
        assertEquals(ItemId.SHARD_OF_GLASS.ordinal(), ItemId.SHARD_OF_GLASS.getId());
    }

}
