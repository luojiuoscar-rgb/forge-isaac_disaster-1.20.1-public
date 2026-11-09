package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import com.google.gson.JsonParser;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ShardOfGlassLoreTest {
    @Test
    void itemAppendsAfterAnemicWithQualityOneAndOrdinalId() {
        assertEquals(1, ItemId.SHARD_OF_GLASS.getLevel());
        assertEquals(ItemId.ANEMIC.ordinal() + 1, ItemId.SHARD_OF_GLASS.ordinal());
        assertEquals(ItemId.SHARD_OF_GLASS.ordinal(), ItemId.SHARD_OF_GLASS.getId());
    }

    @Test
    void realParserHandlesLiteralPercentagesAndPlainConfigurationArguments() throws Exception {
        try (var stream = getClass().getResourceAsStream("/assets/isaac_disaster/lang/en_us.json")) {
            assertNotNull(stream);
            var lang = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (String health : new String[]{"0.00", "0.25", "5.00", "10.00", "512.00"}) {
                var cost = Component.translatableWithFallback("test.shard.cost",
                        lang.get("item.isaac_disaster.shard_of_glass.lore.6").getAsString(), health);
                assertEquals("每20秒失去" + health + "生命，不会致死", cost.getString());
                assertEquals("概率随受到的伤害量的增加而增加", Component.translatableWithFallback("test.shard.chance_detail",
                        lang.get("item.isaac_disaster.shard_of_glass.lore.5").getAsString(), health).getString());
                assertFalse(cost.getString().contains("%s"));
            }
            assertEquals("受伤有最高25%概率掉落红心", Component.translatableWithFallback("test.shard.chance",
                    lang.get("item.isaac_disaster.shard_of_glass.lore.4").getAsString()).getString());
            assertEquals("血泪造成67%～101%面板伤害", Component.translatableWithFallback("test.shard.damage",
                    lang.get("item.isaac_disaster.shard_of_glass.lore.2").getAsString()).getString());
        }
    }
}
