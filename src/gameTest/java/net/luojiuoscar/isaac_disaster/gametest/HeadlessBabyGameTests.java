package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.headless_baby.HeadlessBabyEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BloodTrail;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder("isaac_disaster_headless_baby")
@PrefixGameTestTemplate(false)
public final class HeadlessBabyGameTests {
    private static final ResourceLocation TYPE = ModFamiliarEntities.HEADLESS_BABY.getId();

    @GameTest(template = "trajectory_empty")
    public static void copiesAddBabiesAndConjoinedAndRemovalIsSymmetric(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var ability = ModPassiveAbilities.HEADLESS_BABY.get();
            var items = f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
            int before = items.getSetCountFromId(ModSetAbilities.CONJOINED.getId());
            ability.handleObtain(f.owner, null);
            ability.handleObtain(f.owner, null);
            helper.assertTrue(f.data.getCount(TYPE, TYPE) == 2
                    && items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == before + 2,
                    "Each copy contributes one baby and one set membership");
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
    public static void babyGroupRetainsOverflowAndRequirementsSurviveSerialization(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var bobby = ModFamiliarEntities.BROTHER_BOBBY.getId();
            f.data.setCount(bobby, bobby, 64);
            f.data.setCount(TYPE, TYPE, 2);
            helper.assertTrue(f.data.getCount(TYPE) == 2 && f.data.getEffectiveCount(TYPE) == 0,
                    "Headless Baby shares the full BABIES quota while retaining excess demand");
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
            var blood = f.blood().get(0);
            helper.assertTrue(blood.position().distanceToSqr(baby.position()) < 4
                    && blood.ownerUuid().equals(f.owner.getUUID()) && blood.damage() == BloodTrail.damage(),
                    "The familiar projects blood with its player's fixed configured damage");
            var manager = GroundEffectRuntime.INSTANCE.manager(f.owner.serverLevel());
            manager.tick(f.owner.serverLevel());
            helper.assertTrue(blood.age() == 1, "Ground effects age independently");
            f.advance(1);
            baby.tick();
            helper.assertTrue(blood.age() == 1, "Intermediate familiar ticks do not refresh the puddle");
            f.advance(1);
            baby.tick();
            helper.assertTrue(f.blood().size() == 1 && blood.age() == 0,
                    "A stationary familiar refreshes the same blood after two ticks");
            baby.discardAndUnlink();
            for (int i = 0; i < 100; i++) manager.tick(f.owner.serverLevel());
            helper.assertTrue(f.blood().isEmpty(), "Existing blood finishes its lifetime after its familiar disappears");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void bffsChangesOnlyNewBloodSnapshotsAndUsesTheSharedVisualScale(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var baby = f.baby();
            baby.tick();
            var first = f.blood().get(0);
            float base = BloodTrail.damage();
            ModPassiveAbilities.BFFS.get().handleObtain(f.owner, null);
            try {
                f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
                baby.setPos(f.owner.position().add(3, 0.5, 0));
                f.advance(2);
                baby.tick();
                helper.assertTrue(baby.getVisualScale() == 1.25F && baby.getEnhancement() == 1,
                        "Shared BFFS modifiers enlarge and enhance Headless Baby");
                helper.assertTrue(first.damage() == base && f.blood().stream().anyMatch(e -> e.damage() == base * 2),
                        "Only new blood doubles; neither old snapshots nor panel damage are reused");
            } finally { ModPassiveAbilities.BFFS.get().handleRemove(f.owner, null); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void recallAndInvalidRosterCannotLeaveBloodAlongTheTeleport(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var baby = f.baby();
            baby.setPos(f.owner.position().add(100, 0, 0));
            baby.tick();
            helper.assertTrue(baby.isRecallPending() && f.blood().isEmpty(), "Recall suspends blood generation");
            helper.assertTrue(baby.recallToOwner(), "The shared recall restores the familiar near its owner");
            baby.tick();
            helper.assertTrue(!f.blood().isEmpty() && f.blood().stream()
                    .allMatch(e -> e.position().distanceToSqr(f.owner.position()) < 9),
                    "Blood appears only at the recalled destination");
            f.clearBlood();
            f.data.removeEntity(TYPE, baby.getUUID());
            f.advance(2);
            baby.tick();
            helper.assertTrue(f.blood().isEmpty(), "An invalid roster cannot generate during delayed removal");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void bloodTrailDefaultsAndExplicitInputsUseIndependentSnapshots(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var effect = (IAbilityEffect) ModExecutableEffects.BLOOD_TRAIL.get();
            helper.assertTrue(effect.applyEffect(new ExecutableEffectContext(f.owner)), "Default context remains reusable");
            var first = f.blood().get(0);
            helper.assertTrue(first.damage() == BloodTrail.damage()
                    && first.position().distanceToSqr(f.owner.position()) < 0.01, "Defaults preserve the owner position and unit multiplier");
            var explicit = new ExecutableEffectContext(f.owner);
            var position = f.owner.position().add(3, 0, 0);
            explicit.set(ContextKeys.TARGET_POSITION, position);
            explicit.set(ContextKeys.AMPLIFIER, 2.0);
            effect.applyEffect(explicit);
            helper.assertTrue(f.blood().stream().anyMatch(e -> e.position().distanceToSqr(position) < 0.01
                    && e.damage() == BloodTrail.damage() * 2), "Explicit familiar position and multiplier are applied");
            explicit.set(ContextKeys.TARGET_POSITION, f.owner.position().add(-3, 0, 0));
            explicit.set(ContextKeys.AMPLIFIER, Double.NaN);
            helper.assertTrue(effect.applyEffect(explicit) && f.blood().stream().anyMatch(e -> e.damage() == 0),
                    "Invalid multipliers normalize to a safe zero damage snapshot");
            f.owner.setHealth(0);
            helper.assertTrue(!effect.applyEffect(explicit), "Dead sources are not valid contexts");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void explicitProjectionSupportsSlabsStairsAndSkipsHighOrUnloadedPoints(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var effect = (IAbilityEffect) ModExecutableEffects.BLOOD_TRAIL.get();
            var context = new ExecutableEffectContext(f.owner);
            BlockPos support = f.owner.blockPosition().below();
            f.owner.serverLevel().setBlockAndUpdate(support,
                    Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            context.set(ContextKeys.TARGET_POSITION, f.owner.position().add(0, 1, 0));
            effect.applyEffect(context);
            helper.assertTrue(Math.abs(f.blood().get(0).position().y - support.getY() - 0.502) < 1e-5,
                    "Familiar projection uses the collision top of slabs");
            f.clearBlood();
            f.owner.serverLevel().setBlockAndUpdate(support, Blocks.STONE_STAIRS.defaultBlockState());
            effect.applyEffect(context);
            helper.assertTrue(!f.blood().isEmpty(), "Stairs are valid support surfaces");
            f.clearBlood();
            context.set(ContextKeys.TARGET_POSITION, f.owner.position().add(0, 5, 0));
            helper.assertTrue(effect.applyEffect(context) && f.blood().isEmpty(), "High unsupported projection safely skips");
            var remote = new Vec3(27_123_456, f.owner.getY(), 27_123_456);
            context.set(ContextKeys.TARGET_POSITION, remote);
            helper.assertTrue(!f.owner.level().hasChunkAt(BlockPos.containing(remote))
                    && effect.applyEffect(context) && f.blood().isEmpty()
                    && !f.owner.level().hasChunkAt(BlockPos.containing(remote)), "Explicit positions never force chunk loads");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void legacyBleedingCallersDoNotTreatCopyCountAsADamageMultiplier(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var anemic = ModPassiveAbilities.ANEMIC.get();
            var shard = ModPassiveAbilities.SHARD_OF_GLASS.get();
            anemic.handleObtain(f.owner, null);
            shard.handleObtain(f.owner, null);
            try {
                ((IAbilityEffect) ModExecutableEffects.ANEMIC.get()).applyEffect(new ExecutableEffectContext(f.owner));
                helper.assertTrue(ModPeriodicModules.SHARD_OF_GLASS.get().activate(f.owner),
                        "Arm Shard of Glass before testing its periodic blood emission");
                f.owner.setPos(f.owner.position().add(0.3, 0, 0));
                var context = new ExecutableEffectContext(f.owner);
                context.set(ContextKeys.AMPLIFIER, 9.0);
                ModPeriodicModules.ANEMIC.get().fire(context);
                helper.assertTrue(!f.blood().isEmpty() && f.blood().stream().allMatch(e -> e.damage() == BloodTrail.damage()),
                        "Anemic's periodic copy count cannot amplify blood");
                f.clearBlood();
                ModPeriodicModules.SHARD_OF_GLASS.get().fire(context);
                helper.assertTrue(!f.blood().isEmpty() && f.blood().stream().allMatch(e -> e.damage() == BloodTrail.damage()),
                        "Shard of Glass normalizes only its blood multiplier");
                helper.assertTrue(context.getOrDefault(ContextKeys.AMPLIFIER, 0.0) == 9,
                        "Caller contexts are not mutated when normalizing blood");
            } finally {
                anemic.handleRemove(f.owner, null);
                shard.handleRemove(f.owner, null);
            }
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final long originalTime;

        Fixture(GameTestHelper helper) {
            originalTime = helper.getLevel().getGameTime();
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "headless-test"));
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

        HeadlessBabyEntity baby() {
            data.addCount(TYPE, TYPE, 1);
            if (!FamiliarHelper.spawnNextMissingFamiliar(owner)) throw new IllegalStateException("Baby was not admitted");
            var entry = data.getEntry(TYPE).orElseThrow();
            return (HeadlessBabyEntity) owner.serverLevel().getEntity(entry.getEntityId(entry.getEntityCount() - 1));
        }

        List<AbstractGroundEffect> blood() {
            return GroundEffectRuntime.INSTANCE.manager(owner.serverLevel()).activeEffects().stream()
                    .filter(e -> owner.getUUID().equals(e.ownerUuid())).toList();
        }

        void clearBlood() {
            var manager = GroundEffectRuntime.INSTANCE.manager(owner.serverLevel());
            blood().forEach(manager::remove);
        }

        void advance(int ticks) {
            owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(owner.level().getGameTime() + ticks);
        }

        @Override public void close() {
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            data.clearRequirements();
            clearBlood();
            owner.serverLevel().removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
            owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(originalTime);
        }
    }
}
