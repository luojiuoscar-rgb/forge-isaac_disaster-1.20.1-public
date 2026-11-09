package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraData;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BloodTrail;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectManager;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder("isaac_disaster_anemic")
@PrefixGameTestTemplate(false)
public final class AnemicGameTests {
    @GameTest(template = "trajectory_empty", batch = "anemic", timeoutTicks = 40)
    public static void copiesStackRangeButKeepOneModuleAndLastRemovalStopsTheTrail(GameTestHelper helper) {
        var player = player(helper);
        var ability = ModPassiveAbilities.ANEMIC.get();
        double original = player.getAttributeValue(ModAttributes.BULLET_RANGE.get());
        try {
            helper.assertTrue(!activate(player), "Activation requires the periodic consumer");
            ability.handleObtain(player, null);
            ability.handleObtain(player, null);
            helper.assertTrue(Math.abs(player.getAttributeValue(ModAttributes.BULLET_RANGE.get())
                    - original - 2 * StatManager.RANGE.getBonus()) < 1.0E-6, "Each copy adds one configured range unit");
            var queue = player.getCapability(EffectModulesProvider.EFFECT_MODULES)
                    .orElseThrow(() -> new IllegalStateException("Missing effect modules")).getPeriodicModuleQueue();
            var module = queue.get(ModPeriodicModules.ANEMIC.getId());
            helper.assertTrue(module.stacks == 2, "Copies increment one periodic consumer");
            helper.assertTrue(activate(player), "A valid consumer accepts activation");
            module.coolDown = 1;
            helper.assertTrue(activate(player) && module.coolDown == 1, "Refreshing cannot reset the two-tick cadence");
            ability.handleRemove(player, null);
            moveAndPulse(player);
            helper.assertTrue(!blood(player).isEmpty() && module.stacks == 1, "Removing one copy leaves the trail active");
            int before = blood(player).size();
            ability.handleRemove(player, null);
            moveAndPulse(player);
            helper.assertTrue(blood(player).size() == before && !activate(player), "Last removal stops new blood only");
            helper.assertTrue(Math.abs(player.getAttributeValue(ModAttributes.BULLET_RANGE.get()) - original) < 1.0E-6,
                    "Removing both copies restores the original range");
            helper.assertTrue(!blood(player).isEmpty(), "Existing blood remains after removal");
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "anemic", timeoutTicks = 40)
    public static void onlyFinitePositiveUncancelledHurtArmsTheTrailAndSelfDamageCounts(GameTestHelper helper) {
        var player = player(helper);
        ModPassiveAbilities.ANEMIC.get().handleObtain(player, null);
        try {
            for (float amount : new float[]{0, -1, Float.NaN, Float.POSITIVE_INFINITY}) {
                MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, player.damageSources().generic(), amount));
                moveAndPulse(player);
            }
            var cancelled = new LivingHurtEvent(player, player.damageSources().generic(), 1);
            cancelled.setCanceled(true);
            MinecraftForge.EVENT_BUS.post(cancelled);
            moveAndPulse(player);
            helper.assertTrue(blood(player).isEmpty(), "Invalid and cancelled hurt cannot start blood");
            var selfDamage = PlayerHelper.getSelfDamageSource(player);
            helper.assertTrue(PlayerHelper.isSelfDamage(selfDamage), "The source is the project's actual self-damage type");
            MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, selfDamage, 1));
            moveAndPulse(player);
            helper.assertTrue(!blood(player).isEmpty(), "Project self-damage hurt events arm the trail");
            int before = blood(player).size();
            pulse(player);
            player.setPos(player.position().add(0, 0.5, 0));
            pulse(player);
            helper.assertTrue(blood(player).size() == before, "Stationary or purely vertical motion does not generate blood");
        } finally {
            ModPassiveAbilities.ANEMIC.get().handleRemove(player, null);
            cleanup(player);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "anemic", timeoutTicks = 340)
    public static void repeatedHurtRefreshesInsteadOfAccumulatingAndMilkCannotClearTheTimer(GameTestHelper helper) {
        var player = player(helper);
        ModPassiveAbilities.ANEMIC.get().handleObtain(player, null);
        activate(player);
        // FakePlayer is not driven by the server player list; advance the periodic countdown explicitly.
        for (int tick = 2; tick <= 304; tick += 2) helper.runAtTickTime(tick, () -> pulse(player));
        helper.runAtTickTime(100, () -> {
            MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, player.damageSources().generic(), 1));
            player.removeAllEffects();
        });
        helper.runAtTickTime(220, () -> {
            moveAndPulse(player);
            helper.assertTrue(!blood(player).isEmpty(), "Refreshing survives the initial expiry and potion clearing");
        });
        helper.runAtTickTime(305, () -> {
            try {
                var existing = blood(player);
                moveAndPulse(player);
                var data = player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                        .orElseThrow(() -> new IllegalStateException("Missing extra data"));
                helper.assertTrue(data.getDouble(ModPeriodicModules.ANEMIC.getId()) == null,
                        "The refreshed countdown expires after another 200 ticks");
                helper.assertTrue(blood(player).equals(existing), "The refreshed deadline is 200 ticks later, not additive");
                helper.assertTrue(!existing.isEmpty(), "Existing puddles finish their own independent lifetime");
            } finally {
                ModPassiveAbilities.ANEMIC.get().handleRemove(player, null);
                cleanup(player);
            }
            helper.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", batch = "anemic", timeoutTicks = 40)
    public static void bloodGenerationIsReusableFixedDamageAndIndependentOfPanelDamage(GameTestHelper helper) {
        var player = player(helper);
        try {
            float expected = BloodTrail.damage();
            player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
            helper.assertTrue(generate(player), "Direct blood generation needs no item or original hurt event");
            var first = blood(player).get(0);
            player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1);
            player.setPos(player.position().add(0.5, 0, 0));
            generate(player);
            helper.assertTrue(first.damage() == expected && blood(player).stream().allMatch(e -> e.damage() == expected),
                    "Panel changes cannot modify fixed damage or earlier snapshots");
            helper.assertTrue(first.getColor() == 0xB82025 && first.radius() == 0.75 && first.lifetime() == 100,
                    "The blood type has fixed color, size and lifetime");
            helper.assertTrue(first.nextPosition(helper.getLevel(), ignored -> List.of()).equals(first.position()),
                    "Blood has no homing behavior");
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "anemic", timeoutTicks = 40)
    public static void bloodAndAquariusShareCooldownAndSelectTheLatestLiquid(GameTestHelper helper) {
        var owner = player(helper);
        var center = owner.position();
        var target = zombie(helper, center);
        var manager = new GroundEffectManager();
        try {
            manager.spawn(ModGroundEffects.AQUARIUS.get(), owner.getUUID(), center, 0.75, 100, 13);
            manager.spawn(ModGroundEffects.BLOOD.get(), owner.getUUID(), center.add(0.1, 0, 0), 0.75, 100, 4);
            manager.tick(helper.getLevel());
            helper.assertTrue(target.getHealth() == 16, "Latest blood replaces Aquarius damage in the overlap");
            manager.spawn(ModGroundEffects.BLOOD.get(), owner.getUUID(), center.add(0.2, 0, 0), 0.75, 100, 4);
            for (int i = 0; i < 9; i++) manager.tick(helper.getLevel());
            helper.assertTrue(target.getHealth() == 16, "New puddles do not reset the shared damage cooldown");
            manager.tick(helper.getLevel());
            helper.assertTrue(target.getHealth() == 12, "A second single pulse occurs ten ticks later");
            for (int i = 0; i < 100; i++) manager.tick(helper.getLevel());
            helper.assertTrue(manager.activeCount() == 0, "All blood and Aquarius instances expire independently");
        } finally {
            manager.clear();
            target.discard();
            cleanup(owner);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "anemic", timeoutTicks = 40)
    public static void bloodExcludesOwnerFriendlyAndFrozenTargetsAndCannotBecomeOwnerlessDamage(GameTestHelper helper) {
        var owner = player(helper);
        var center = owner.position();
        var target = zombie(helper, center);
        var frozen = zombie(helper, center);
        frozen.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), -1));
        var friend = new Wolf(EntityType.WOLF, helper.getLevel());
        friend.setTame(true);
        friend.setOwnerUUID(owner.getUUID());
        friend.setPos(center);
        helper.getLevel().addFreshEntity(friend);
        var manager = new GroundEffectManager();
        try {
            float friendHealth = friend.getHealth(), ownerHealth = owner.getHealth();
            manager.spawn(ModGroundEffects.BLOOD.get(), owner.getUUID(), center, 0.75, 100, 4);
            manager.tick(helper.getLevel());
            helper.assertTrue(target.getHealth() == 16 && frozen.getHealth() == 20
                    && friend.getHealth() == friendHealth && owner.getHealth() == ownerHealth,
                    "Only the ordinary hostile target is hurt");
            float previous = target.getHealth();
            helper.getLevel().removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
            for (int i = 0; i < 10; i++) manager.tick(helper.getLevel());
            helper.assertTrue(target.getHealth() == previous, "Unresolvable sources cannot turn into ownerless attacks");
        } finally {
            manager.clear();
            target.discard();
            frozen.discard();
            friend.discard();
            cleanup(owner);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "anemic", timeoutTicks = 40)
    public static void bloodUsesSlabGroundProjectionAndSafelySkipsUnsupportedOrUnloadedPositions(GameTestHelper helper) {
        var player = player(helper);
        try {
            Vec3 center = player.position();
            BlockPos support = BlockPos.containing(center).below();
            helper.getLevel().setBlockAndUpdate(support,
                    Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            helper.assertTrue(generate(player), "A player up to two blocks above a slab can generate blood");
            var first = blood(player).get(0);
            helper.assertTrue(Math.abs(first.position().y - support.getY() - 0.502) < 1.0E-5,
                    "Projection uses the actual slab surface");
            int before = blood(player).size();
            player.setPos(center.add(0, 5, 0));
            helper.assertTrue(generate(player) && blood(player).size() == before, "Unsupported spawn is a safe no-op");
            Vec3 unloaded = new Vec3(27_123_456, center.y, 27_123_456);
            helper.assertTrue(!helper.getLevel().hasChunkAt(BlockPos.containing(unloaded)), "The distant chunk starts unloaded");
            var skipped = GroundEffectRuntime.INSTANCE.spawnOnGround(ModGroundEffects.BLOOD.get(), player,
                    unloaded, 0.75, 100, 4);
            helper.assertTrue(skipped == null && !helper.getLevel().hasChunkAt(BlockPos.containing(unloaded)),
                    "Generation does not load the distant chunk");
            helper.getLevel().setBlockAndUpdate(support, Blocks.AIR.defaultBlockState());
            var isolated = new GroundEffectManager();
            isolated.spawn(ModGroundEffects.BLOOD.get(), player.getUUID(), first.position(), 0.75, 100, 4);
            for (int i = 0; i < 10; i++) isolated.tick(helper.getLevel());
            helper.assertTrue(isolated.activeCount() == 0, "The existing support checks remove blood within ten ticks");
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "anemic", timeoutTicks = 40)
    public static void countdownsArePlayerLocalRefreshToTwoHundredAndPersistWithPlayerData(GameTestHelper helper) {
        var first = player(helper);
        var second = player(helper);
        var ability = ModPassiveAbilities.ANEMIC.get();
        ability.handleObtain(first, null);
        ability.handleObtain(second, null);
        try {
            activate(first);
            activate(second);
            var key = ModPeriodicModules.ANEMIC.getId();
            var firstData = first.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                    .orElseThrow(() -> new IllegalStateException("Missing extra data"));
            var secondData = second.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                    .orElseThrow(() -> new IllegalStateException("Missing extra data"));
            helper.assertTrue(firstData.getDouble(key) == 200, "Hurt arms a 200-tick countdown");
            for (int i = 0; i < 40; i++) pulse(first);
            helper.assertTrue(firstData.getDouble(key) == 120 && secondData.getDouble(key) == 200,
                    "Only the pulsed player's countdown decreases, even when stationary");
            activate(first);
            helper.assertTrue(firstData.getDouble(key) == 200, "Refresh replaces remaining time instead of adding");
            for (int i = 0; i < 100; i++) pulse(first);
            moveAndPulse(first);
            moveAndPulse(second);
            helper.assertTrue(firstData.getDouble(key) == null && blood(first).isEmpty()
                    && !blood(second).isEmpty(), "After 200 ticks only the expired player's blood generation stops");
            activate(first);
            var nbt = new CompoundTag();
            firstData.saveNBTData(nbt);
            var clone = new ExtraData();
            clone.copyFrom(firstData);
            helper.assertTrue(clone.getDouble(key) == 200, "The countdown follows normal ExtraData copying");
            firstData.loadNBTData(nbt);
            moveAndPulse(first);
            helper.assertTrue(firstData.getDouble(key) == 198 && !blood(first).isEmpty(),
                    "Reloading player data preserves the countdown and blood generation resumes");
        } finally {
            ability.handleRemove(first, null);
            ability.handleRemove(second, null);
            cleanup(first);
            cleanup(second);
        }
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "anemic-test"));
        var center = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
        for (int x = -1; x <= 2; x++) {
            helper.getLevel().setBlockAndUpdate(BlockPos.containing(center).offset(x, -1, 0), Blocks.STONE.defaultBlockState());
        }
        player.setPos(center);
        player.setNoGravity(true);
        player.setGameMode(GameType.SURVIVAL);
        helper.getLevel().addNewPlayer(player);
        DefaultAttributeManager.apply(player);
        return player;
    }

    private static Zombie zombie(GameTestHelper helper, Vec3 position) {
        var zombie = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        zombie.setNoAi(true);
        zombie.setNoGravity(true);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);
        zombie.setPos(position);
        helper.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    private static boolean activate(ServerPlayer player) {
        return ((IAbilityEffect) ModExecutableEffects.ANEMIC.get()).applyEffect(new ExecutableEffectContext(player));
    }

    private static boolean generate(ServerPlayer player) {
        return ((IAbilityEffect) ModExecutableEffects.BLOOD_TRAIL.get()).applyEffect(new ExecutableEffectContext(player));
    }

    private static void pulse(ServerPlayer player) { ModPeriodicModules.ANEMIC.get().fire(new ExecutableEffectContext(player)); }

    private static void moveAndPulse(ServerPlayer player) {
        player.setPos(player.position().add(0.15, 0, 0));
        pulse(player);
    }

    private static List<AbstractGroundEffect> blood(ServerPlayer player) {
        return GroundEffectRuntime.INSTANCE.manager(player.serverLevel()).activeEffects().stream()
                .filter(e -> player.getUUID().equals(e.ownerUuid()) && e.getType() == ModGroundEffects.BLOOD.get()).toList();
    }

    private static void cleanup(ServerPlayer player) {
        ModPeriodicModules.ANEMIC.get().handleRemove(player);
        var manager = GroundEffectRuntime.INSTANCE.manager(player.serverLevel());
        manager.activeEffects().stream().filter(e -> player.getUUID().equals(e.ownerUuid())).toList().forEach(manager::remove);
        player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
    }
}
