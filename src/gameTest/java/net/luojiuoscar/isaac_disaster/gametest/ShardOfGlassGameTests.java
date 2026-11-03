package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraData;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.ShardOfGlass;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.ShardOfGlassBloodTear;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_shard_of_glass")
@PrefixGameTestTemplate(false)
public final class ShardOfGlassGameTests {
    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void hurtStageIncludesEnvironmentalAndAbsorbedDamageButRespectsCancellation(GameTestHelper helper) {
        var player = player(helper);
        try {
            Consumer<LivingHurtEvent> cancel = event -> { if (event.getEntity() == player) event.setCanceled(true); };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingHurtEvent.class, cancel);
            try { damage(player, 4); }
            finally { MinecraftForge.EVENT_BUS.unregister(cancel); }
            helper.assertTrue(remaining(player) == null && player.getHealth() == 100, "Cancelled hurt is ignored");
            player.setAbsorptionAmount(10);
            damage(player, 4);
            helper.assertTrue(remaining(player) == 400 && player.getHealth() == 100,
                    "Environmental ON_HURT activates before absorption consumes the hit");
            pulse(player);
            helper.assertTrue(remaining(player) == 398, "Absorption does not appear to be healing");
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void hurtAmountDirectlyControlsTheSingleHeartRoll(GameTestHelper helper) {
        var player = player(helper);
        try {
            double unit = StatManager.MAX_HEALTH.getBonus();
            int before = hearts(player);
            player.getRandom().setSeed(4096);
            MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, player.damageSources().generic(), (float) (unit * 0.1)));
            helper.assertTrue(remaining(player) == 400 && hearts(player) == before,
                    "A seeded roll above five percent misses for a small hurt-stage amount");
            pulse(player);
            player.getRandom().setSeed(4096);
            MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, player.damageSources().generic(), (float) (unit * 0.5)));
            helper.assertTrue(hearts(player) == before + 1 && remaining(player) == 398,
                    "The same roll hits at twenty-five percent without resetting the countdown");
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void invalidHurtAmountsCannotStartBleeding(GameTestHelper helper) {
        var player = player(helper);
        try {
            for (float amount : new float[]{0, -1, Float.NaN, Float.POSITIVE_INFINITY}) {
                MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, player.damageSources().generic(), amount));
            }
            helper.assertTrue(remaining(player) == null, "Only finite positive uncancelled hurt can activate this item");
            helper.assertTrue(!new ShardOfGlass().applyEffect(new ExecutableEffectContext(player)),
                    "Missing hurt event returns false");
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void copiesAndRepeatedHitsDoNotResetTheCostOrMultiplySpray(GameTestHelper helper) {
        var player = player(helper);
        var ability = ModPassiveAbilities.SHARD_OF_GLASS.get();
        ability.handleObtain(player, null);
        try {
            damage(player, 1);
            pulse(player);
            helper.assertTrue(remaining(player) == 398 && tears(player) == 1, "First periodic call emits one blood tear");
            damage(player, 1);
            helper.assertTrue(remaining(player) == 398, "Another hit never resets the cost countdown");
            pulse(player);
            helper.assertTrue(tears(player) == 2, "The second tear fires at tick four");
            pulse(player);
            helper.assertTrue(tears(player) == 2, "Only two tears fire within six ticks despite two copies");
            ability.handleRemove(player, null);
            helper.assertTrue(remaining(player) != null, "Removing one of two copies preserves bleeding");
            ability.handleRemove(player, null);
            helper.assertTrue(remaining(player) == null && tears(player) == 2, "Last removal stops bleeding and keeps old tears");
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void fixedCostOccursAtFourHundredTicksAndLeavesAbsorptionUntouched(GameTestHelper helper) {
        var player = player(helper);
        try {
            damage(player, 1);
            float before = player.getHealth();
            player.setAbsorptionAmount(12);
            player.getAttribute(Attributes.ARMOR).setBaseValue(30);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000, 4));
            player.setInvulnerable(true);
            for (int i = 0; i < 199; i++) pulse(player);
            helper.assertTrue(player.getHealth() == before && remaining(player) == 2, "No cost before tick 400");
            pulse(player);
            close(helper, before - player.getHealth(), (float) (StatManager.MAX_HEALTH.getBonus() * 0.5),
                    "The fixed cost bypasses mitigation and invulnerability");
            helper.assertTrue(player.getAbsorptionAmount() == 12 && remaining(player) == 400,
                    "Absorption survives and the next independent period starts");
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void cancelledSelfNotificationCannotChangeCostAndSkipsTheHeartRoll(GameTestHelper helper) {
        var player = player(helper);
        var notices = new int[1];
        Consumer<LivingHurtEvent> listener = event -> {
            if (event.getEntity() == player && PlayerHelper.isSelfDamage(event.getSource())) {
                notices[0]++;
                event.setAmount(1000);
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingHurtEvent.class, listener);
        try {
            damage(player, 1);
            for (int i = 0; i < 199; i++) pulse(player);
            int beforeHearts = hearts(player);
            float before = player.getHealth();
            player.getRandom().setSeed(4096);
            pulse(player);
            close(helper, before - player.getHealth(), (float) (StatManager.MAX_HEALTH.getBonus() * 0.5),
                    "Event cancellation and edits cannot change the completed cost");
            helper.assertTrue(notices[0] == 1 && hearts(player) == beforeHearts && remaining(player) == 400,
                    "The single cancelled notification suppresses its heart roll without changing the completed cost");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            cleanup(player);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void notificationTriggersAnemicAndItsHealingIsSeenOnTheNextPulse(GameTestHelper helper) {
        var player = player(helper);
        ModPassiveAbilities.ANEMIC.get().handleObtain(player, null);
        Consumer<LivingHurtEvent> heal = event -> {
            if (event.getEntity() == player && PlayerHelper.isSelfDamage(event.getSource())) player.heal(1);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingHurtEvent.class, heal);
        try {
            damage(player, 1);
            for (int i = 0; i < 199; i++) pulse(player);
            pulse(player);
            var data = data(player);
            helper.assertTrue(data.getDouble(ModPeriodicModules.ANEMIC.getId()) == 200,
                    "The existing ON_HURT item receives the fixed-cost self notification");
            pulse(player);
            helper.assertTrue(remaining(player) == null && data.getDouble(ModPeriodicModules.ANEMIC.getId()) == 200,
                    "Healing in the notification stops this item without clearing Anemic");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(heal);
            ModPassiveAbilities.ANEMIC.get().handleRemove(player, null);
            cleanup(player);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void lethalCostIsSkippedAndHealingAndGameModesStopBleeding(GameTestHelper helper) {
        var player = player(helper);
        try {
            player.setHealth((float) (StatManager.MAX_HEALTH.getBonus() * 0.5 + 1));
            damage(player, 1);
            float before = player.getHealth();
            for (int i = 0; i < 200; i++) pulse(player);
            helper.assertTrue(player.getHealth() == before && remaining(player) == null, "Lethal fixed cost is skipped");
            player.setHealth(50);
            damage(player, 1);
            pulse(player);
            player.heal(1);
            pulse(player);
            helper.assertTrue(remaining(player) == null, "Healing stops bleeding within the next two-tick sample");
            for (GameType mode : new GameType[]{GameType.CREATIVE, GameType.SPECTATOR}) {
                player.setGameMode(GameType.SURVIVAL);
                damage(player, 1);
                player.setGameMode(mode);
                pulse(player);
                helper.assertTrue(remaining(player) == null, "Entering " + mode + " stops bleeding");
            }
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void pureBloodTearCapturesAttributesButNoPlayerAttachments(GameTestHelper helper) {
        var player = player(helper);
        try {
            player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
                ability.setHoming(1);
                ability.setPiercing(1);
                ability.setSpectral(1);
                ability.addBulletColor(ModBulletColors.URANUS.getId(), 1);
            });
            ModPassiveAbilities.URANUS.get().handleObtain(player, null);
            player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10);
            player.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(0.8);
            player.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(24);
            player.getAttribute(ModAttributes.BULLET_KNOCKBACK.get()).setBaseValue(0.7);
            var tear = ShardOfGlassBloodTear.createTear(player);
            helper.assertTrue(tear.getDamage() >= 6.7 && tear.getDamage() <= 10.1 && tear.baseSpeed() == 0.4
                    && tear.getAttackContext().getBulletRange() == 24 && tear.getBulletKnockback() == 0.7,
                    "Each pure tear uses current damage, half panel speed, range and knockback");
            helper.assertTrue(tear.position().equals(player.getBoundingBox().getCenter())
                    && !tear.isHoming() && !tear.isPiercing() && !tear.isSpectral() && !tear.isControllable()
                    && tear.getSplitSequence().isEmpty() && tear.visualIds().isEmpty()
                    && tear.getTriggers().getView().toString().equals(new CompositeTrigger().getView().toString())
                    && tear.getColorId().equals(ModBulletColors.BLOOD_TEAR.getId()) && tear.alpha() == 1,
                    "No player color, texture, trigger, split or trajectory flags leak into the tear");
            helper.assertTrue(tear.getTrajectoryRuntime().specs().size() == 1
                    && tear.getTrajectoryRuntime().specs().get(0).id().equals(ModTrajectoryModules.GRAVITY.getId()),
                    "Gravity is the only trajectory module");
            float damage = tear.getDamage();
            double scale = tear.renderScale();
            player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
            player.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(2);
            helper.assertTrue(tear.getDamage() == damage && tear.baseSpeed() == 0.4 && tear.renderScale() == scale,
                    "Later attribute changes cannot alter the saved tear");
            ModPassiveAbilities.URANUS.get().handleRemove(player, null);
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void movementMakesBloodAndUnsupportedGroundOnlySkipsThePuddle(GameTestHelper helper) {
        var player = player(helper);
        try {
            damage(player, 1);
            pulse(player);
            helper.assertTrue(blood(player) == 0, "Stationary bleeding does not make a ground puddle");
            player.setPos(player.position().add(0.15, 0, 0));
            pulse(player);
            helper.assertTrue(blood(player) > 0, "Horizontal movement makes the shared blood type");
            int before = blood(player);
            player.setPos(player.position().add(0.15, 10, 0));
            pulse(player);
            helper.assertTrue(blood(player) == before && tears(player) == 2 && remaining(player) != null,
                    "No ground skips only the puddle; bleeding and spray continue");
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void timersArePlayerLocalPersistAndDeathCleansOnlyThisBleeding(GameTestHelper helper) {
        var first = player(helper);
        var second = player(helper);
        try {
            damage(first, 1);
            damage(second, 1);
            for (int i = 0; i < 10; i++) pulse(first);
            helper.assertTrue(remaining(first) == 380 && remaining(second) == 400, "Players have independent countdowns");
            var nbt = new CompoundTag();
            data(first).saveNBTData(nbt);
            var loaded = new ExtraData();
            loaded.loadNBTData(nbt);
            var copied = new ExtraData();
            copied.copyFrom(data(first));
            helper.assertTrue(loaded.getDouble(ModPeriodicModules.SHARD_OF_GLASS.getId()) == 380
                    && copied.getDouble(ModPeriodicModules.SHARD_OF_GLASS.getId()) == 380,
                    "Ordinary ExtraData persistence and copying retain the countdown");
            data(first).loadNBTData(nbt);
            pulse(first);
            helper.assertTrue(remaining(first) == 378, "The health sampling baseline also survives serialization");
            first.die(first.damageSources().generic());
            helper.assertTrue(remaining(first) == null && remaining(second) == 400, "DEATH clears only its own player's state");
        } finally { cleanup(first); cleanup(second); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void mitigatedHurtDoesNotCauseFalseHealingAtTheFirstPulse(GameTestHelper helper) {
        var player = player(helper);
        try {
            player.getAttribute(Attributes.ARMOR).setBaseValue(20);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
            damage(player, 10);
            pulse(player);
            helper.assertTrue(remaining(player) == 398 && tears(player) == 1,
                    "The hurt-stage amount must not be subtracted to predict post-mitigation health");
            player.heal(0.5F);
            pulse(player);
            helper.assertTrue(remaining(player) == null, "A real health increase stops bleeding on the next sample");
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void uncancelledFixedCostNotificationRollsExactlyOneHeart(GameTestHelper helper) {
        var player = player(helper);
        try {
            damage(player, 1);
            for (int i = 0; i < 199; i++) pulse(player);
            int before = hearts(player);
            player.getRandom().setSeed(4096);
            pulse(player);
            helper.assertTrue(hearts(player) == before + 1 && remaining(player) == 400,
                    "The fixed cost reuses ON_HURT for one heart roll, with no separate manual roll");
        } finally { cleanup(player); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "shard_of_glass")
    public static void sprayKeepsItsThreeTickCadenceAcrossTheFourHundredTickCost(GameTestHelper helper) {
        var player = player(helper);
        try {
            damage(player, 1);
            var queue = player.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow()
                    .getPeriodicModuleQueue();
            float health = player.getHealth();
            for (int tick = 1; tick <= 410; tick++) {
                queue.tickAll(player);
                helper.assertTrue(tears(player) == 1 + (tick - 1) / 3,
                        "One tear every three ticks, including across the fixed-cost reset: tick " + tick);
                helper.assertTrue(remaining(player) == 400 - tick % 400,
                        "The one-tick consumer preserves the four-hundred-tick cost interval");
            }
            close(helper, health - player.getHealth(), (float) (StatManager.MAX_HEALTH.getBonus() * 0.5),
                    "Exactly one fixed cost occurs in 410 ticks");
        } finally { cleanup(player); }
        helper.succeed();
    }

    private static DamagePlayer player(GameTestHelper helper) {
        var player = new DamagePlayer(helper.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        var center = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
        helper.getLevel().setBlockAndUpdate(BlockPos.containing(center).below(), Blocks.STONE.defaultBlockState());
        player.setPos(center);
        player.setNoGravity(true);
        helper.getLevel().addNewPlayer(player);
        DefaultAttributeManager.apply(player);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        player.setHealth(100);
        ObfuscationReflectionHelper.setPrivateValue(ServerPlayer.class, player, 0, "f_8921_");
        ModPassiveAbilities.SHARD_OF_GLASS.get().handleObtain(player, null);
        return player;
    }

    private static void damage(ServerPlayer player, float amount) {
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().generic(), amount);
    }

    private static ExtraData data(ServerPlayer player) {
        return player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).orElseThrow(() -> new IllegalStateException("Missing extra data"));
    }

    private static Double remaining(ServerPlayer player) { return data(player).getDouble(ModPeriodicModules.SHARD_OF_GLASS.getId()); }
    private static void pulse(ServerPlayer player) {
        for (int i = 0; i < 2; i++) ModPeriodicModules.SHARD_OF_GLASS.get().fire(new ExecutableEffectContext(player));
    }
    private static int tears(ServerPlayer player) {
        return (int) BulletRuntime.INSTANCE.manager(player.serverLevel()).activeStates().stream().filter(b -> b.getOwner() == player).count();
    }
    private static int blood(ServerPlayer player) {
        return (int) GroundEffectRuntime.INSTANCE.manager(player.serverLevel()).activeEffects().stream()
                .filter(e -> player.getUUID().equals(e.ownerUuid()) && e.getType() == ModGroundEffects.BLOOD.get()).count();
    }
    private static int hearts(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(0.5)).stream()
                .filter(item -> item.getItem().is(ModItems.RED_HEART.get())).mapToInt(item -> item.getItem().getCount()).sum();
    }
    private static void close(GameTestHelper helper, float actual, float expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 1E-4, message + ": " + actual + " vs " + expected);
    }
    private static void cleanup(ServerPlayer player) {
        ModPeriodicModules.SHARD_OF_GLASS.get().handleRemove(player);
        var bullets = BulletRuntime.INSTANCE.manager(player.serverLevel());
        bullets.activeStates().stream().filter(b -> b.getOwner() == player).toList().forEach(bullets::remove);
        var ground = GroundEffectRuntime.INSTANCE.manager(player.serverLevel());
        ground.activeEffects().stream().filter(e -> player.getUUID().equals(e.ownerUuid())).toList().forEach(ground::remove);
        player.serverLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(0.5)).forEach(Entity::discard);
        player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
    }

    private static final class DamagePlayer extends FakePlayer {
        private DamagePlayer(ServerLevel level) { super(level, new GameProfile(UUID.randomUUID(), "shard-test")); }

        @Override
        public boolean isInvulnerableTo(DamageSource source) {
            return isRemoved() || isInvulnerable() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.isCreativePlayer();
        }

        @Override
        public void die(DamageSource source) { ForgeHooks.onLivingDeath(this, source); }
    }
}
