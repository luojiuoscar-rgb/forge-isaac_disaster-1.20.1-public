package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class DepressionItemIdTest {
    @Test
    void itemAppendsAndDoesNotReuseWikiId() {
        assertEquals(ItemId.HEADLESS_BABY.ordinal() + 1, ItemId.DEPRESSION.ordinal());
        assertEquals(ItemId.DEPRESSION.ordinal(), ItemId.DEPRESSION.getId());
        assertEquals(1, ItemId.DEPRESSION.getLevel());
    }

}
