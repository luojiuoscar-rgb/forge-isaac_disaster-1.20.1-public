package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.depression.DepressionEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.DepressionTrail;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.DepressionPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.server.HolyLightRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_depression")
@PrefixGameTestTemplate(false)
public final class DepressionGameTests {
    private static final ResourceLocation TYPE = ModFamiliarEntities.DEPRESSION.getId();

    @GameTest(template = "trajectory_empty")
    public static void copiesAddCloudsWithoutConjoinedAndRemovalIsSymmetric(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var ability = ModPassiveAbilities.DEPRESSION.get();
            var items = f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
            int before = items.getSetCountFromId(ModSetAbilities.CONJOINED.getId());
            ability.handleObtain(f.owner, null);
            ability.handleObtain(f.owner, null);
            helper.assertTrue(f.data.getCount(TYPE, TYPE) == 2
                    && items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == before,
                    "Each copy contributes one cloud without set membership");
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner)
                    && FamiliarHelper.spawnNextMissingFamiliar(f.owner), "Both requested babies are admitted");
            ability.handleRemove(f.owner, null);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(f.data.getEntityCount(TYPE, TYPE) == 1, "One removal only removes one familiar");
            ability.handleRemove(f.owner, null);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(f.data.getEntityCount(TYPE, TYPE) == 0 && f.data.getCount(TYPE) == 0
                    && items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == before,
                    "Final removal restores the original roster and set count");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void chainGroupRetainsOverflowAndRequirementsSurviveSerialization(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var bobby = ModFamiliarEntities.BROTHER_BOBBY.getId();
            f.data.setCount(bobby, bobby, 64);
            f.data.setCount(TYPE, TYPE, 2);
            helper.assertTrue(f.data.getCount(TYPE) == 2 && f.data.getEffectiveCount(TYPE) == 0,
                    "Headless Baby shares the full chain familiar quota while retaining excess demand");
            var tag = new CompoundTag();
            f.data.saveNBTData(tag);
            f.data.clearRequirements();
            f.data.loadNBTData(tag);
            f.data.setCount(bobby, bobby, 0);
            helper.assertTrue(f.data.getEffectiveCount(TYPE) == 2 && FamiliarHelper.spawnNextMissingFamiliar(f.owner),
                    "Serialized requirements replenish when group capacity returns");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void stationaryFamiliarRefreshesOnlyEveryTwoTicksAtItsOwnPosition(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var baby = f.baby();
            baby.tick();
            var trail = f.trail().get(0);
            helper.assertTrue(trail.position().distanceToSqr(baby.position()) < 4
                    && trail.ownerUuid().equals(f.owner.getUUID()) && trail.damage() == DepressionTrail.damage(),
                    "The familiar projects trail with its player's fixed configured damage");
            var manager = GroundEffectRuntime.INSTANCE.manager(f.owner.serverLevel());
            manager.tick(f.owner.serverLevel());
            helper.assertTrue(trail.age() == 1, "Ground effects age independently");
            f.advance(1);
            baby.tick();
            helper.assertTrue(trail.age() == 1, "Intermediate familiar ticks do not refresh the puddle");
            f.advance(1);
            baby.tick();
            helper.assertTrue(f.trail().size() == 1 && trail.age() == 0,
                    "A stationary familiar refreshes the same trail after two ticks");
            baby.discardAndUnlink();
            for (int i = 0; i < 100; i++) manager.tick(f.owner.serverLevel());
            helper.assertTrue(f.trail().isEmpty(), "Existing trail finishes its lifetime after its familiar disappears");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void bffsChangesOnlyNewTrailSnapshotsAndUsesTheSharedVisualScale(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var baby = f.baby();
            baby.tick();
            var first = f.trail().get(0);
            float base = DepressionTrail.damage();
            ModPassiveAbilities.BFFS.get().handleObtain(f.owner, null);
            try {
                f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
                baby.setPos(f.owner.position().add(3, 0.5, 0));
                f.advance(2);
                baby.tick();
                helper.assertTrue(baby.getVisualScale() == 1.25F && baby.getEnhancement() == 1,
                        "Shared BFFS modifiers enlarge and enhance Headless Baby");
                helper.assertTrue(first.damage() == base && f.trail().stream().anyMatch(e -> e.damage() == base * 2),
                        "Only new trail doubles; neither old snapshots nor panel damage are reused");
            } finally { ModPassiveAbilities.BFFS.get().handleRemove(f.owner, null); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void recallAndInvalidRosterCannotLeaveTrailAlongTheTeleport(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var baby = f.baby();
            baby.setPos(f.owner.position().add(100, 0, 0));
            baby.tick();
            helper.assertTrue(baby.isRecallPending() && f.trail().isEmpty(), "Recall suspends trail generation");
            helper.assertTrue(baby.recallToOwner(), "The shared recall restores the familiar near its owner");
            baby.tick();
            helper.assertTrue(!f.trail().isEmpty() && f.trail().stream()
                    .allMatch(e -> e.position().distanceToSqr(f.owner.position()) < 9),
                    "Trail appears only at the recalled destination");
            f.clearTrail();
            f.data.removeEntity(TYPE, baby.getUUID());
            f.advance(2);
            baby.tick();
            helper.assertTrue(f.trail().isEmpty(), "An invalid roster cannot generate during delayed removal");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void trailTrailDefaultsAndExplicitInputsUseIndependentSnapshots(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var effect = (IAbilityEffect) ModExecutableEffects.DEPRESSION_TRAIL.get();
            helper.assertTrue(effect.applyEffect(new ExecutableEffectContext(f.owner)), "Default context remains reusable");
            var first = f.trail().get(0);
            helper.assertTrue(first.damage() == DepressionTrail.damage()
                    && first.position().distanceToSqr(f.owner.position()) < 0.01, "Defaults preserve the owner position and unit multiplier");
            var explicit = new ExecutableEffectContext(f.owner);
            var position = f.owner.position().add(3, 0, 0);
            explicit.set(ContextKeys.TARGET_POSITION, position);
            explicit.set(ContextKeys.AMPLIFIER, 2.0);
            effect.applyEffect(explicit);
            helper.assertTrue(f.trail().stream().anyMatch(e -> e.position().distanceToSqr(position) < 0.01
                    && e.damage() == DepressionTrail.damage() * 2), "Explicit familiar position and multiplier are applied");
            explicit.set(ContextKeys.TARGET_POSITION, f.owner.position().add(-3, 0, 0));
            explicit.set(ContextKeys.AMPLIFIER, Double.NaN);
            helper.assertTrue(effect.applyEffect(explicit) && f.trail().stream().anyMatch(e -> e.damage() == 0),
                    "Invalid multipliers normalize to a safe zero damage snapshot");
            f.owner.setHealth(0);
            helper.assertTrue(!effect.applyEffect(explicit), "Dead sources are not valid contexts");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void explicitProjectionSupportsSlabsStairsAndSkipsHighOrUnloadedPoints(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var effect = (IAbilityEffect) ModExecutableEffects.DEPRESSION_TRAIL.get();
            var context = new ExecutableEffectContext(f.owner);
            BlockPos support = f.owner.blockPosition().below();
            f.owner.serverLevel().setBlockAndUpdate(support,
                    Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            context.set(ContextKeys.TARGET_POSITION, f.owner.position().add(0, 1, 0));
            effect.applyEffect(context);
            helper.assertTrue(Math.abs(f.trail().get(0).position().y - support.getY() - 0.502) < 1e-5,
                    "Familiar projection uses the collision top of slabs");
            f.clearTrail();
            f.owner.serverLevel().setBlockAndUpdate(support, Blocks.STONE_STAIRS.defaultBlockState());
            effect.applyEffect(context);
            helper.assertTrue(!f.trail().isEmpty(), "Stairs are valid support surfaces");
            f.clearTrail();
            context.set(ContextKeys.TARGET_POSITION, f.owner.position().add(0, 5, 0));
            helper.assertTrue(effect.applyEffect(context) && f.trail().isEmpty(), "High unsupported projection safely skips");
            var remote = new Vec3(27_123_456, f.owner.getY(), 27_123_456);
            context.set(ContextKeys.TARGET_POSITION, remote);
            helper.assertTrue(!f.owner.level().hasChunkAt(BlockPos.containing(remote))
                    && effect.applyEffect(context) && f.trail().isEmpty()
                    && !f.owner.level().hasChunkAt(BlockPos.containing(remote)), "Explicit positions never force chunk loads");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void holyStrikeUsesCurrentPanelAndSharedUndeadMultiplierWithoutBffs(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var ordinary = f.target(false, f.owner.position());
            var undead = f.target(true, f.owner.position().add(0.1, 0, 0));
            ModPassiveAbilities.BFFS.get().handleObtain(f.owner, null);
            try {
                var puddle = f.puddle(8);
                f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
                int before = f.strikes();
                f.forceProc();
                puddle.onContactDamage(new GroundEffectContact(helper.getLevel(), ordinary, f.owner));
                helper.assertTrue(f.strikes() == before + 1, "A successful pulse can immediately create one holy strike");
                helper.assertTrue(ordinary.getHealth() == 77 && undead.getHealth() == 70,
                        "Normal receives water 8 plus holy 15; undead receives holy 30; BFFS adds no holy multiplier");
                helper.assertTrue(puddle.damage() == 8, "Holy damage reads current panel without mutating water snapshots");
            } finally {
                ModPassiveAbilities.BFFS.get().handleRemove(f.owner, null);
                ordinary.discard(); undead.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void rejectedDamageDoesNotDrawProbabilityOrCreateHolyLight(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var target = f.target(false, f.owner.position());
            var puddle = f.puddle(4);
            long seed = f.forceProc();
            double expected = f.owner.getRandom().nextDouble();
            f.owner.getRandom().setSeed(seed);
            int before = f.strikes();
            Consumer<LivingAttackEvent> cancel = event -> { if (event.getEntity() == target) event.setCanceled(true); };
            MinecraftForge.EVENT_BUS.addListener(cancel);
            try { puddle.onContactDamage(new GroundEffectContact(helper.getLevel(), target, f.owner)); }
            finally { MinecraftForge.EVENT_BUS.unregister(cancel); }
            helper.assertTrue(target.getHealth() == 100 && f.strikes() == before
                    && f.owner.getRandom().nextDouble() == expected,
                    "Cancelled damage cannot consume a proc roll or generate a strike");
            f.owner.getRandom().setSeed(seed);
            target.setInvulnerable(true);
            puddle.onContactDamage(new GroundEffectContact(helper.getLevel(), target, f.owner));
            helper.assertTrue(f.owner.getRandom().nextDouble() == expected && f.strikes() == before,
                    "Invulnerable targets also leave the roll untouched");
            target.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void lethalWaterCanProcAndOrdinaryWaterCannot(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var victim = f.target(false, f.owner.position());
            var nearby = f.target(false, f.owner.position().add(0.1, 0, 0));
            try {
                f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
                var puddle = f.puddle(4);
                int before = f.strikes();
                victim.setHealth(1);
                f.forceProc();
                puddle.onContactDamage(new GroundEffectContact(helper.getLevel(), victim, f.owner));
                helper.assertTrue(!victim.isAlive() && f.strikes() == before + 1 && nearby.getHealth() == 85,
                        "Lethal water still procs at the original position and holy damage cannot recurse");
                f.forceProc();
                var ordinary = GroundEffectRuntime.INSTANCE.manager(f.owner.serverLevel()).spawn(
                        ModGroundEffects.WATER.get(), f.owner.getUUID(), f.owner.position(), 0.75, 100, 4);
                ordinary.onContactDamage(new GroundEffectContact(helper.getLevel(), nearby, f.owner));
                helper.assertTrue(f.strikes() == before + 1, "Other water types never inherit Depression's proc");
            } finally { victim.discard(); nearby.discard(); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void newestLiquidWinsAndMultipleCloudsShareOneDamageAndRoll(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var first = f.baby();
            var second = f.baby();
            first.setPos(f.owner.position()); second.setPos(f.owner.position());
            first.tick(); second.tick();
            var target = f.target(false, f.owner.position());
            try {
                var manager = GroundEffectRuntime.INSTANCE.manager(f.owner.serverLevel());
                manager.spawn(ModGroundEffects.AQUARIUS.get(), f.owner.getUUID(), f.owner.position().add(0.01, 0.002, 0), 0.75, 100, 13);
                manager.spawn(ModGroundEffects.BLOOD.get(), f.owner.getUUID(), f.owner.position().add(0.02, 0.002, 0), 0.75, 100, 7);
                f.puddle(4);
                f.owner.getRandom().setSeed(0);
                f.owner.getRandom().nextDouble();
                double expectedSecond = f.owner.getRandom().nextDouble();
                f.owner.getRandom().setSeed(0);
                manager.tick(helper.getLevel());
                helper.assertTrue(target.getHealth() == 96 && f.owner.getRandom().nextDouble() == expectedSecond,
                        "Latest Depression water deals one pulse and draws one roll despite other clouds and liquids");
                f.puddle(4);
                for (int i = 0; i < 9; i++) manager.tick(helper.getLevel());
                helper.assertTrue(target.getHealth() == 96, "Refreshing puddles cannot bypass shared contact cooldown");
                f.owner.getRandom().setSeed(0);
                manager.tick(helper.getLevel());
                helper.assertTrue(target.getHealth() == 92, "The next pulse occurs ten ticks later");
            } finally { target.discard(); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void cloudsBabiesAndItemGiversShareSixtyFourWithPlayerLocalOverflow(GameTestHelper helper) {
        try (var f = new Fixture(helper); var other = new Fixture(helper)) {
            var bobby = ModFamiliarEntities.BROTHER_BOBBY.getId();
            var coins = ModFamiliarEntities.SACK_OF_PENNIES.getId();
            f.data.setCount(bobby, bobby, 63);
            f.data.setCount(coins, coins, 1);
            f.data.setCount(TYPE, TYPE, 3);
            other.data.setCount(TYPE, TYPE, 3);
            helper.assertTrue(f.data.getEffectiveCount(coins) == 1 && f.data.getEffectiveCount(TYPE) == 0
                    && f.data.getCount(TYPE) == 3 && other.data.getEffectiveCount(TYPE) == 3,
                    "All chain categories share 64 per player, retaining overflow independently");
            f.data.setCount(bobby, bobby, 61);
            helper.assertTrue(f.data.getEffectiveCount(TYPE) == 2
                    && f.data.getEffectiveCount(bobby) + f.data.getEffectiveCount(coins) + f.data.getEffectiveCount(TYPE) == 64,
                    "Freed baby capacity admits clouds without exceeding the combined limit");
            var tag = new CompoundTag(); f.data.saveNBTData(tag);
            f.data.clearRequirements(); f.data.loadNBTData(tag);
            helper.assertTrue(f.data.getCount(TYPE) == 3 && f.data.getEffectiveCount(TYPE) == 2,
                    "Requirements reload into the new shared group without a compatibility alias");
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final long originalTime;

        Fixture(GameTestHelper helper) {
            originalTime = helper.getLevel().getGameTime();
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "depression-test"));
            var center = helper.absoluteVec(new Vec3(2.5, 220, 2.5));
            for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++)
                helper.getLevel().setBlockAndUpdate(BlockPos.containing(center).offset(x, -1, z), Blocks.STONE.defaultBlockState());
            owner.setPos(center);
            owner.setNoGravity(true);
            owner.setGameMode(GameType.SURVIVAL);
            helper.getLevel().addNewPlayer(owner);
            DefaultAttributeManager.apply(owner);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        }

        DepressionEntity baby() {
            data.addCount(TYPE, TYPE, 1);
            if (!FamiliarHelper.spawnNextMissingFamiliar(owner)) throw new IllegalStateException("Baby was not admitted");
            var entry = data.getEntry(TYPE).orElseThrow();
            return (DepressionEntity) owner.serverLevel().getEntity(entry.getEntityId(entry.getEntityCount() - 1));
        }

        Mob target(boolean undead, Vec3 position) {
            Mob target = undead ? new Zombie(EntityType.ZOMBIE, owner.level()) : new Creeper(EntityType.CREEPER, owner.level());
            target.setNoAi(true); target.setNoGravity(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100); target.setHealth(100);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.setPos(position); owner.serverLevel().addFreshEntity(target);
            return target;
        }

        DepressionPuddleEffect puddle(float damage) {
            return GroundEffectRuntime.INSTANCE.manager(owner.serverLevel()).spawn(ModGroundEffects.DEPRESSION.get(),
                    owner.getUUID(), owner.position().add(0, 0.002, 0), 0.75, 100, damage);
        }

        int strikes() { return HolyLightRuntime.INSTANCE.manager(owner.serverLevel()).snapshots().size(); }

        long forceProc() {
            for (long seed = 0; seed < 100_000; seed++) {
                owner.getRandom().setSeed(seed);
                if (owner.getRandom().nextDouble() < 0.01) {
                    owner.getRandom().setSeed(seed);
                    return seed;
                }
            }
            throw new IllegalStateException("No deterministic proc seed found");
        }

        List<AbstractGroundEffect> trail() {
            return GroundEffectRuntime.INSTANCE.manager(owner.serverLevel()).activeEffects().stream()
                    .filter(e -> owner.getUUID().equals(e.ownerUuid())).toList();
        }

        void clearTrail() {
            var manager = GroundEffectRuntime.INSTANCE.manager(owner.serverLevel());
            trail().forEach(manager::remove);
        }

        void advance(int ticks) {
            owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(owner.level().getGameTime() + ticks);
        }

        @Override public void close() {
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            data.clearRequirements();
            clearTrail();
            owner.serverLevel().removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
            owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(originalTime);
        }
    }
}
