package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.event.ServerTickEvent;
import net.luojiuoscar.isaac_disaster.item.ModActiveItems;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.ModTrinkets;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.system.flight.IsaacFlightController;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.UUID;

@GameTestHolder("isaac_disaster_how_to_jump")
@PrefixGameTestTemplate(false)
public final class HowToJumpGameTests {
    @GameTest(template = "trajectory_empty", batch = "how_to_jump")
    public static void registeredActiveUsesTwoCharges(GameTestHelper helper) {
        var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "how_to_jump"));
        helper.assertTrue(item instanceof ActiveItem, "How to Jump must be registered as a normal active item");
        var active = (ActiveItem) item;
        helper.assertTrue(new ItemStack(item).getMaxDamage() == 200 && active.getOriginalDamagePerUse() == 200,
                "How to Jump must consume and store two charges");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "how_to_jump")
    public static void useConsumesChargeAndCountsTheBookOnlyOnce(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var stack = new ItemStack(ModActiveItems.HOW_TO_JUMP.get());
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, stack);
            f.owner.setXRot(0);
            f.owner.setYRot(0);
            f.owner.setOnGround(true);
            f.owner.fallDistance = 20;
            stack.getItem().use(f.level, f.owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(stack.getDamageValue() == 200, "Normal use costs two bars");
            helper.assertTrue(f.owner.getCooldowns().isOnCooldown(stack.getItem()), "Shared use cooldown is active");
            helper.assertTrue(f.owner.getDeltaMovement().distanceTo(new Vec3(0, .75, 1.5)) < 1e-6, "Actual trigger launches along view");
            helper.assertTrue(f.owner.fallDistance == 20, "A normal leap does not grant protection by clearing fall distance");
            helper.assertTrue(f.items().getSetCountFromId(ModSetAbilities.BOOK.getId()) == 1, "First use counts as a book");
            stack.setDamageValue(0);
            f.owner.getCooldowns().removeCooldown(stack.getItem());
            f.items().addItem(f.owner, new ItemStack(ModPassiveItems.CAR_BATTERY.get()));
            stack.getItem().use(f.level, f.owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(f.owner.getDeltaMovement().distanceTo(new Vec3(0, .75, 1.5)) < 1e-6, "Car Battery neither stacks nor strengthens velocity");
            helper.assertTrue(f.items().getSetCountFromId(ModSetAbilities.BOOK.getId()) == 1, "Same stack never counts twice");
            helper.assertTrue(ModActiveAbilities.HOW_TO_JUMP.get().getSynergyDesc(stack, f.owner).stream()
                    .map(Component::getString).anyMatch(line -> line.contains("无额外效果")), "Car Battery description is conditional and translated");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "how_to_jump")
    public static void airborneUseReplacesVelocityAndCanDive(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            f.owner.setOnGround(false);
            f.owner.setDeltaMovement(20, -10, 30);
            f.owner.setXRot(90);
            var context = new ExecutableEffectContext(f.owner);
            context.set(ContextKeys.AMPLIFIER, 100.0);
            helper.assertTrue(new HowToJump().applyEffect(context), "No held item, target, event or ground is required");
            Vec3 velocity = f.owner.getDeltaMovement();
            helper.assertTrue(Math.abs(velocity.y + .75) < 1e-6 && velocity.horizontalDistanceSqr() < 1e-10, "Full downward view dives and replaces old velocity");
            helper.assertTrue(new HowToJump().applyEffect(context) && f.owner.getDeltaMovement().equals(velocity), "Repeated use never accumulates velocity");
            helper.assertTrue(!f.fall().isCanceled(), "Repeated use never grants landing protection");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "how_to_jump")
    public static void actualFallAndOtherDamageRemainEnabled(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            f.jump();
            float health = f.owner.getHealth();
            helper.assertTrue(f.owner.hurt(f.level.damageSources().generic(), 2), "Other damage remains enabled");
            helper.assertTrue(f.owner.getHealth() < health, "Leap is not global invulnerability");
            f.owner.invulnerableTime = 0;
            health = f.owner.getHealth();
            f.owner.causeFallDamage(8, 1, f.level.damageSources().fall());
            helper.assertTrue(f.owner.getHealth() < health, "The very first real fall damages normally");
            f.owner.invulnerableTime = 0;
            health = f.owner.getHealth();
            f.owner.causeFallDamage(8, 1, f.level.damageSources().fall());
            helper.assertTrue(f.owner.getHealth() < health, "Next fall damages normally");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "how_to_jump")
    public static void waterDoesNotBlockTheImpulse(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            BlockPos water = BlockPos.containing(f.owner.position());
            f.level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
            try {
                f.owner.baseTick();
                helper.assertTrue(f.owner.isInWater(), "Fixture is actually in water");
                helper.assertTrue(f.jump(), "Water does not prevent use");
                helper.assertTrue(f.owner.getDeltaMovement().lengthSqr() > 1, "Water receives the initial velocity");
            } finally {
                f.level.setBlockAndUpdate(water, Blocks.AIR.defaultBlockState());
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "how_to_jump")
    public static void useDismountsAndStopsGlidingAndBothFlightControllers(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var pig = EntityType.PIG.create(f.level);
            pig.setPos(f.owner.position());
            pig.setNoAi(true);
            f.level.addFreshEntity(pig);
            try {
                helper.assertTrue(f.owner.startRiding(pig, true), "Fixture rides a real entity");
                f.jump();
                helper.assertTrue(!f.owner.isPassenger(), "Use dismounts first");
            } finally {
                pig.discard();
            }
            f.owner.startFallFlying();
            f.jump();
            helper.assertTrue(!f.owner.isFallFlying(), "Gliding ends");
            f.owner.getAbilities().mayfly = true;
            f.owner.getAbilities().flying = true;
            f.jump();
            helper.assertTrue(!f.owner.getAbilities().flying && f.owner.getAbilities().mayfly, "Vanilla flight stops but permission remains");
            var flight = IsaacFlightController.getState(f.owner).orElseThrow();
            flight.setEnabled(true);
            flight.setThrusting(true);
            f.owner.setNoGravity(true);
            f.jump();
            helper.assertTrue(!flight.isThrusting() && !flight.isEnabled() && !f.owner.isNoGravity(), "Isaac flight pauses with gravity restored");
            Vec3 velocity = f.owner.getDeltaMovement();
            IsaacFlightController.receiveInput(f.owner, true, f.level.getGameTime());
            IsaacFlightController.tickServer(f.owner);
            helper.assertTrue(!flight.isThrusting() && f.owner.getDeltaMovement().equals(velocity), "Held flight input cannot immediately overwrite launch");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "how_to_jump")
    public static void nonPlayersAreRejectedAndActualTranslationsResolve(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var cow = EntityType.COW.create(f.level);
            helper.assertTrue(!new HowToJump().applyEffect(new ExecutableEffectContext(cow)), "Non-player contexts return false");
            var ability = ModActiveAbilities.HOW_TO_JUMP.get();
            var stack = new ItemStack(ModActiveItems.HOW_TO_JUMP.get());
            helper.assertTrue(ability.getDesc(stack, f.owner).size() == 1 && stack.getHoverName().getString().equals("跳跃教程"), "Name and concise ordinary lore resolve");
            var lines = new ArrayList<>(ability.getDesc(stack, f.owner));
            lines.addAll(ability.getExtraDesc(stack, f.owner));
            lines.addAll(ability.getSynergyDesc(stack, f.owner));
            helper.assertTrue(lines.stream().map(Component::getString).noneMatch(line -> line.contains("item.isaac_disaster.")
                    || line.contains("set.isaac_disaster.") || line.contains("%s")), "Actual Minecraft parser resolves all lore and set descriptions");
            helper.assertTrue(ability.getSynergyDesc(stack, f.owner).stream().noneMatch(line -> line.getString().contains("无额外效果")), "Car Battery text is hidden without ownership");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "how_to_jump_charge")
    public static void existingChargeSynergiesAndDefaultTenSecondRecoveryRemainIntact(GameTestHelper helper) {
        try (var settings = GameTestDatapackSettings.override(helper); var f = new Fixture(helper)) {
            settings.set(GameplayConfig.ACTIVE_ITEM_AUTO_RESTORE, true);
            settings.set(GameplayConfig.ACTIVE_ITEM_DURABILITY_RESTORE_RATE, 4);
            settings.set(GameplayConfig.LIMITED_ACTIVE_ITEM_DURABILITY_RESTORE, true);
            var stack = new ItemStack(ModActiveItems.HOW_TO_JUMP.get());
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, stack);
            stack.setDamageValue(200);
            for (int tick = 1; tick <= 200; tick++) {
                f.owner.tickCount = tick;
                ServerTickEvent.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, f.owner));
                if (tick == 196) helper.assertTrue(stack.getDamageValue() == 4, "Two charges require the full default 200 ticks");
            }
            helper.assertTrue(stack.getDamageValue() == 0, "Default recovery completes at ten seconds");
            f.items().addItem(f.owner, new ItemStack(ModPassiveItems.VOLT_9.get()));
            stack.getItem().use(f.level, f.owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(stack.getDamageValue() == 160, "9 Volt returns twenty percent of the base cost");
            f.items().addToList(new ItemStack(ModTrinkets.AAA_BATTERY.get()));
            stack.setDamageValue(0);
            f.owner.getCooldowns().removeCooldown(stack.getItem());
            stack.getItem().use(f.level, f.owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(stack.getDamageValue() == 120, "AAA Battery reduces cost through the existing swallowed-trinket path");
            f.items().addItem(f.owner, new ItemStack(ModPassiveItems.VOLT_4P5.get()));
            f.owner.tickCount = 204;
            ServerTickEvent.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, f.owner));
            helper.assertTrue(stack.getDamageValue() == 120, "4.5 Volt disables automatic recovery");
            f.items().addItem(f.owner, new ItemStack(ModPassiveItems.THE_BATTERY.get()));
            ActiveItem.fullCharge(stack, true);
            helper.assertTrue(ActiveItem.getOverCharged(stack), "The Battery supports the existing overcharge state");
            f.owner.getCooldowns().removeCooldown(stack.getItem());
            stack.getItem().use(f.level, f.owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(!ActiveItem.getOverCharged(stack) && stack.getDamageValue() == 0, "Overcharge spends one use through the existing cost pipeline");
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final ServerLevel level;
        final ServerPlayer owner;

        Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            owner = new DamagePlayer(level);
            owner.setPos(helper.absoluteVec(new Vec3(3.5, 3, 3.5)));
            owner.setGameMode(GameType.SURVIVAL);
            level.addNewPlayer(owner);
            ObfuscationReflectionHelper.setPrivateValue(ServerPlayer.class, owner, 0, "f_8921_");
        }

        PlayerIsaacItems items() {
            return owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).orElseThrow(AssertionError::new);
        }

        boolean jump() {
            return new HowToJump().applyEffect(new ExecutableEffectContext(owner));
        }

        LivingFallEvent fall() {
            var event = new LivingFallEvent(owner, 12, 1);
            MinecraftForge.EVENT_BUS.post(event);
            return event;
        }

        @Override
        public void close() {
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
    }

    private static final class DamagePlayer extends FakePlayer {
        DamagePlayer(ServerLevel level) {
            super(level, new GameProfile(UUID.randomUUID(), "how_to_jump"));
        }

        @Override
        public boolean isInvulnerableTo(DamageSource source) {
            return isRemoved() || isInvulnerable() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                    && !source.isCreativePlayer();
        }
    }
}
