package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AnemicLoreTest {
    @Test
    void realTranslationParserSubstitutesConfiguredDamageWithoutBreakingLineBoundaries() throws Exception {
        try (var input = getClass().getResourceAsStream("/assets/isaac_disaster/lang/en_us.json")) {
            assertNotNull(input);
            var lang = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            var damage = lang.get("item.isaac_disaster.anemic.lore.3").getAsString();
            for (int value : new int[]{0, 4, 6, 2048}) {
                assertEquals("血迹每次造成" + value + "伤害",
                        Component.translatableWithFallback("test.anemic.damage", damage, value).getString());
            }
            assertEquals("受伤后10秒内", Component.translatableWithFallback("test.anemic.duration",
                    lang.get("item.isaac_disaster.anemic.lore.1").getAsString()).getString());
            assertEquals("移动时留下伤害血迹", Component.translatableWithFallback("test.anemic.movement",
                    lang.get("item.isaac_disaster.anemic.lore.2").getAsString()).getString());
        }
    }
}
