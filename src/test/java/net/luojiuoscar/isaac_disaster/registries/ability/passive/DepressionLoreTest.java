package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import com.google.gson.JsonParser;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DepressionLoreTest {
    @Test
    void itemAppendsAndDoesNotReuseWikiId() {
        assertEquals(ItemId.HEADLESS_BABY.ordinal() + 1, ItemId.DEPRESSION.ordinal());
        assertEquals(ItemId.DEPRESSION.ordinal(), ItemId.DEPRESSION.getId());
        assertEquals(1, ItemId.DEPRESSION.getLevel());
    }

    @Test
    void realTranslationParserKeepsSixNaturalLinesAndLiteralPercent() throws Exception {
        try (var input = getClass().getResourceAsStream("/assets/isaac_disaster/lang/en_us.json")) {
            assertNotNull(input);
            var lang = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            String damage = lang.get("item.isaac_disaster.depression.lore.3").getAsString();
            for (String value : new String[]{"0", "4", "6", "2048", "0.5"}) {
                assertEquals("水迹每次造成" + value + "伤害",
                        Component.translatableWithFallback("test.depression.damage", damage, value).getString());
            }
            for (int i = 1; i <= 6; i++) {
                if (i == 3) continue;
                String text = lang.get("item.isaac_disaster.depression.lore." + i).getAsString();
                assertEquals(text, Component.translatableWithFallback("test.depression.line." + i, text).getString());
            }
        }
    }
}
