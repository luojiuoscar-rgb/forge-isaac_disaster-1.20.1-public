package net.luojiuoscar.isaac_disaster.manager.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeadlessBabyItemIdTest {
    @Test
    void headlessBabyAppendsAfterExistingItemsWithQualityOne() {
        var id = assertDoesNotThrow(() -> ItemId.valueOf("HEADLESS_BABY"));
        assertEquals(1, id.getLevel());
        assertEquals(ItemId.SHARD_OF_GLASS.ordinal() + 1, id.ordinal());
        assertEquals(id.ordinal(), id.getId());
    }
}
