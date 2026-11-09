package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HeadlessBabyLoreTest {
    @Test
    void configuredDamageUsesRealTranslationParserAndIndependentLines() throws Exception {
        try (var input = getClass().getResourceAsStream("/assets/isaac_disaster/lang/en_us.json")) {
            assertNotNull(input);
            var lang = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            var damage = lang.get("item.isaac_disaster.headless_baby.lore.3").getAsString();
            for (String value : new String[]{"0", "4", "6", "2048", "0.5"}) {
                assertEquals("血迹每次造成" + value + "伤害",
                        Component.translatableWithFallback("test.headless_baby.damage", damage, value).getString());
            }
            assertEquals("获得一个无头宝宝", lang.get("item.isaac_disaster.headless_baby.lore.1").getAsString());
            assertEquals("持续留下伤害血迹", lang.get("item.isaac_disaster.headless_baby.lore.2").getAsString());
        }
    }
}
