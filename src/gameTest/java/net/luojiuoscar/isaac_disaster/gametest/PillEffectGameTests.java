package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder("isaac_disaster_pill")
@PrefixGameTestTemplate(false)
public final class PillEffectGameTests {
    @GameTest(template = "trajectory_empty")
    public static void loadedFullHealthPillHealsBothSizes(GameTestHelper helper) {
        PillEffectManager manager = PillEffectManager.getInstance();
        CompoundTag previousMappings = manager.toTag();
        CompoundTag mappings = new CompoundTag();
        mappings.putString("1", "isaac_disaster:full_health_pill");
        try {
            manager.loadFromTag(mappings);
            ServerPlayer player = player(helper);
            for (boolean horse : new boolean[]{false, true}) {
                player.setHealth(1.0f);
                player.setAbsorptionAmount(0.0f);
                manager.getEffectFromPill(1).get().apply(context(player, horse));
                assertFullHealth(helper, player, horse);
            }
        } finally {
            manager.loadFromTag(previousMappings);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void lowHealthBadTripHealsBothSizes(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        for (boolean horse : new boolean[]{false, true}) {
            player.setHealth(1.0f);
            player.setAbsorptionAmount(0.0f);
            ModExecutableEffects.BAD_TRIP.get().apply(context(player, horse));
            assertFullHealth(helper, player, horse);
        }
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "pill-test"));
        player.getAttribute(ModAttributes.PILL_QUALITY.get()).setBaseValue(0.0);
        return player;
    }

    private static ExecutableEffectContext context(ServerPlayer player, boolean horse) {
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        context.set(ContextKeys.BOOLEAN, List.of(horse));
        return context;
    }

    private static void assertFullHealth(GameTestHelper helper, ServerPlayer player, boolean horse) {
        helper.assertTrue(player.getHealth() == player.getMaxHealth(),
                "Full Health restores all health for normal and horse pills");
        float absorption = horse ? 3.0f * (float) StatManager.MAX_HEALTH.getBonus() : 0.0f;
        helper.assertTrue(player.getAbsorptionAmount() == absorption,
                "Only the horse pill grants three health units of absorption");
    }
}
