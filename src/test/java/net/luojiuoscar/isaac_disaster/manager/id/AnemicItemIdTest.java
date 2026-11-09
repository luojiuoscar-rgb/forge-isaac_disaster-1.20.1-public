package net.luojiuoscar.isaac_disaster.manager.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnemicItemIdTest {
    @Test
    void anemicHasQualityOneAndAppendsWithoutReassigningEarlierItems() {
        var anemic = assertDoesNotThrow(() -> ItemId.valueOf("ANEMIC"));
        assertEquals(1, anemic.getLevel());
        assertEquals(anemic.ordinal(), anemic.getId());
        assertEquals(ItemId.HOW_TO_JUMP.ordinal() + 1, anemic.ordinal());
    }
}
