package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.ItemGivingSlot;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.item_giving.AbstractItemGivingFamiliarEntity;
import net.luojiuoscar.isaac_disaster.event.ForgeEvents;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.Bffs;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.HiveMind;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.FamiliarModifierEntry;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.ModFamiliarModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_item_giver")
@PrefixGameTestTemplate(false)
public final class ItemGivingFamiliarGameTests {
    private static final ResourceLocation COINS = id("sack_of_pennies");
    private static final ResourceLocation BOMBS = id("bomb_bag");
    private static final ResourceLocation RELIC = id("the_relic");
    private static final ResourceLocation SOURCE = id("command");
    private static final List<ResourceLocation> TYPES = List.of(COINS, BOMBS, RELIC);
    private static final ResourceLocation WEAKENING = id("item_giver_test/weakening");
    private static final ResourceLocation FRACTION = id("item_giver_test/fraction");

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, path); }

    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class TestModifiers {
        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(ModFamiliarModifiers.FAMILIAR_MODIFIER_KEY, registry -> {
                registry.register(WEAKENING, new FamiliarModifierEntry(FamiliarModifierEntry.Parameter.ENHANCEMENT, -2, AbstractItemGivingFamiliarEntity.class::isInstance));
                registry.register(FRACTION, new FamiliarModifierEntry(FamiliarModifierEntry.Parameter.ENHANCEMENT, 0.75, AbstractItemGivingFamiliarEntity.class::isInstance));
            });
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void actualRegistrationInitialTimersAndContinuousProduction(GameTestHelper helper) {
        for (var type : TYPES) try (Fixture f = new Fixture(helper)) {
            f.settings.set(GameplayConfig.BASIC_TIME_INTERVAL, 0.05);
            var giver = f.add(type);
            var slot = f.slot(type, 0);
            int first = type.equals(RELIC) ? 12 : 6;
            helper.assertTrue(slot.cooldown() == first, "首次等待使用配置单位 " + type);
            giver.setPos(f.hover().add(10, 0, 0));
            for (int i = 1; i < first; i++) { f.tick(giver); helper.assertTrue(!slot.hasItems(), "首次计时结束前无产出"); }
            f.tick(giver);
            helper.assertTrue(slot.hasItems() && slot.cooldown() >= first && slot.cooldown() < (type.equals(RELIC) ? 18 : 8), "生产后随机冷却左闭右开");
            int before = count(slot);
            int remaining = slot.cooldown();
            for (int i = 0; i < remaining; i++) {
                giver.setPos(f.hover().add(10, 0, 0)); f.tick(giver);
            }
            helper.assertTrue(count(slot) == before + 1, "携货期间继续生产，不等待交付");
            if (type.equals(BOMBS)) helper.assertTrue(inventory(slot).get(0).is(ModItems.BOMB.get()), "炸弹使用现有物品");
            if (type.equals(RELIC)) helper.assertTrue(inventory(slot).get(0).is(ModItems.SOUL_HEART.get()), "圣遗物使用现有魂心");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void emptyFollowAreaAndLoadedDeliveryUseDifferentTargets(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var giver = f.add(BOMBS);
            Vec3 rest = f.hover().add(0.5, 0, 0); giver.setPos(rest); f.tick(giver);
            helper.assertTrue(giver.position().equals(rest), "空载在前驱1格以内允许停留");
            giver.setPos(f.hover().add(0, 5, 0)); double before = giver.position().y; f.tick(giver);
            helper.assertTrue(giver.getY() < before, "空载按纯高度差跟随");
            var slot = f.slot(BOMBS, 0); slot.produce(List.of(new ItemStack(Items.DIAMOND, 70)), 1000);
            giver.setPos(f.owner.position().add(0, 5, 0));
            for (int i = 0; i < 80 && slot.hasItems(); i++) f.tick(giver);
            helper.assertTrue(!slot.hasItems() && f.drops.stream().mapToInt(d -> d.getItem().getCount()).sum() == 70, "携货追向脚部，一次交付全部多栈库存");
            helper.assertTrue(f.drops.stream().allMatch(d -> !d.hasPickUpDelay() && d.position().equals(f.owner.position())), "掉落位于脚边且无拾取延迟");
            helper.assertTrue(slot.cooldown() < 1000 && slot.cooldown() > 900, "交付不重置持续生产计时");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void rejectedDropsRemainAndRetryWithoutDuplicatingAcceptedStacks(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var giver = f.add(BOMBS); var slot = f.slot(BOMBS, 0);
            slot.produce(List.of(new ItemStack(Items.DIAMOND, 64), new ItemStack(Items.EMERALD, 3)), 100);
            Consumer<EntityJoinLevelEvent> reject = event -> {
                if (event.getEntity() instanceof ItemEntity drop && drop.getItem().is(Items.EMERALD)) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(reject);
            try { giver.setPos(f.owner.position()); f.tick(giver); }
            finally { MinecraftForge.EVENT_BUS.unregister(reject); }
            helper.assertTrue(count(slot) == 3 && inventory(slot).get(0).is(Items.EMERALD), "仅拒绝部分保留原库存");
            giver.setPos(f.owner.position()); f.tick(giver);
            helper.assertTrue(!slot.hasItems(), "下一tick交付失败部分");
            helper.assertTrue(f.drops.stream().filter(d -> d.getItem().is(Items.DIAMOND)).count() == 1, "成功部分不会重复交付");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void failedWorldAdmissionDoesNotCreateSlotsOrLoseRequirements(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Consumer<EntityJoinLevelEvent> reject = event -> {
                if (event.getEntity() instanceof AbstractItemGivingFamiliarEntity giver && giver.getOwner() == f.owner) event.setCanceled(true);
            };
            f.data.addCount(BOMBS, SOURCE, 1);
            MinecraftForge.EVENT_BUS.addListener(reject);
            try {
                helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(f.owner), "普通调度世界拒绝回滚");
                helper.assertTrue(FamiliarHelper.spawnFamiliars(f.owner, COINS, SOURCE, f.hover()).isEmpty(), "即时生成世界拒绝回滚");
            } finally { MinecraftForge.EVENT_BUS.unregister(reject); }
            helper.assertTrue(f.data.getItemGivingData().get(BOMBS, 0) == null && f.data.getCount(BOMBS) == 1, "未成功接入不初始化库存slot且保留需求");
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "重试可以正常生成");
            f.entity(BOMBS).tick();
            helper.assertTrue(f.slot(BOMBS, 0).cooldown() == 1200, "首次成功接入才初始化一次");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void recallAndRuntimeRebuildPreserveInventoryAndTimer(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var giver = f.add(RELIC); var slot = f.slot(RELIC, 0);
            slot.produce(List.of(new ItemStack(Items.DIAMOND, 5)), 77);
            giver.setPos(f.hover().add(20, 0, 0)); f.tick(giver); f.tick(giver);
            helper.assertTrue(giver.isRecallPending() && slot.cooldown() == 77 && count(slot) == 5, "召回等待暂停生产和交付");
            helper.assertTrue(giver.recallToOwner() && slot.cooldown() == 77, "召回本身保留计时");
            UUID old = giver.getUUID(); FamiliarHelper.discardAllRuntimeFamiliars(f.owner);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "重建正常补出");
            var rebuilt = f.entity(RELIC);
            helper.assertTrue(!old.equals(rebuilt.getUUID()) && slot == f.slot(RELIC, 0) && count(slot) == 5 && slot.cooldown() == 77, "新UUID按类型内编号读取既有库存，不重置状态");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void runtimeReorderingChangesPresenterWithoutDuplicatingProductionOrDelivery(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(BOMBS);
            var second = f.add(BOMBS);
            var zero = f.slot(BOMBS, 0);
            var one = f.slot(BOMBS, 1);
            zero.produce(List.of(new ItemStack(Items.DIAMOND, 3)), 50);
            one.produce(List.of(new ItemStack(Items.EMERALD, 2)), 70);
            first.setPos(f.hover().add(8, 0, 0));
            f.tick(first);
            int remaining = zero.cooldown();
            first.discardAndUnlink();
            second.setPos(f.owner.position());
            second.tick();
            helper.assertTrue(zero.cooldown() == remaining && count(zero) == 3 && f.drops.isEmpty(),
                    "同tick重排不能再次推进或交付已处理的记录");
            f.tick(second);
            helper.assertTrue(!zero.hasItems() && count(one) == 2 && one.cooldown() == 70,
                    "后继实体改读0号库存，未展示的1号库存与计时保留");
            helper.assertTrue(f.drops.stream().mapToInt(drop -> drop.getItem().getCount()).sum() == 3,
                    "改换展示实体后只交付一次");
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "缺失实体正常重建");
            var replacement = f.entity(BOMBS);
            replacement.setPos(f.owner.position());
            f.tick(replacement);
            helper.assertTrue(!one.hasItems() && f.drops.stream().mapToInt(drop -> drop.getItem().getCount()).sum() == 5,
                    "重建实体读取剩余类型内记录，不复制或重置库存");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void sourceRemovalCompactsPlayerRecordsWithoutKeepingAnEntityBinding(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.add(BOMBS);
            ResourceLocation otherSource = id("item_giver_test/other");
            f.data.addCount(BOMBS, otherSource, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "生成另一来源的同类型袋子");
            var remaining = f.entity(BOMBS); remaining.tick();
            f.slot(BOMBS, 0).produce(List.of(new ItemStack(Items.DIAMOND, 9)), 20);
            var saved = f.slot(BOMBS, 1);
            saved.produce(List.of(new ItemStack(Items.EMERALD, 2)), 60);
            f.data.setCount(BOMBS, SOURCE, 0);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(f.slot(BOMBS, 0) == saved && f.slot(BOMBS, 1) == null
                    && saved.cooldown() == 60 && count(saved) == 2 && f.drops.isEmpty(),
                    "只丢弃被移除来源的库存，其他记录连同冷却顺移");
            remaining.setPos(f.owner.position()); f.tick(remaining);
            helper.assertTrue(!saved.hasItems() && f.drops.size() == 1 && f.drops.get(0).getItem().is(Items.EMERALD),
                    "剩余实体按新编号交付，不错发已删除库存");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void followingPositionKeepsTheWholeOneBlockRestArea(GameTestHelper helper) {
        for (Vec3 offset : List.of(Vec3.ZERO, new Vec3(0.5, 0, 0), new Vec3(0, 0.9, 0), new Vec3(1, 0, 0))) {
            try (Fixture f = new Fixture(helper)) {
                var giver = f.add(BOMBS);
                Vec3 start = f.hover().add(offset);
                giver.setPos(start);
                for (int i = 0; i < 10; i++) f.tick(giver);
                helper.assertTrue(giver.position().distanceToSqr(start) < 1e-12,
                        "1格内不追到前驱中心，也不被推出到边界 " + offset);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void bffsDoublesEachRollAndCoinConfigurationIsRespected(GameTestHelper helper) {
        for (var type : TYPES) try (Fixture f = new Fixture(helper)) {
            f.settings.set(GameplayConfig.COIN_TIER_1_ID, "minecraft:emerald");
            f.settings.set(GameplayConfig.COIN_TIER_2_ID, "minecraft:emerald");
            f.settings.set(GameplayConfig.COIN_TIER_3_ID, "minecraft:emerald");
            var giver = f.add(type); var slot = f.slot(type, 0);
            new Bffs(0, 4).handleObtain(f.owner, null);
            new HiveMind(0, 2).handleObtain(f.owner, null);
            slot.produce(List.of(), 1); giver.setPos(f.hover().add(8, 0, 0)); f.tick(giver);
            helper.assertTrue(count(slot) == 2 && giver.getEnhancement() == 1 && giver.getVisualScale() == 1.25f, "BFFS产量翻倍，Hive Mind不影响给予型");
            if (type.equals(COINS)) helper.assertTrue(inventory(slot).stream().allMatch(s -> s.is(Items.EMERALD)), "按配置币种抽取，非硬编码钱币");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void productionAppliesLootModifiersOnceAndDeliveryDoesNotReroll(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var giver = f.add(COINS); var slot = f.slot(COINS, 0); int[] rolls = {0};
            Consumer<GeneralLootModifyEvent> modify = event -> {
                if (event.getEntity() == f.owner) {
                    rolls[0]++; event.getObjectArrayList().clear(); event.getObjectArrayList().add(new ItemStack(Items.DIAMOND, 3));
                }
            };
            MinecraftForge.EVENT_BUS.addListener(modify);
            try {
                slot.produce(List.of(), 1); giver.setPos(f.hover().add(8, 0, 0)); f.tick(giver);
                helper.assertTrue(rolls[0] == 1 && count(slot) == 3, "生产时保留原有战利品修改语义");
                giver.setPos(f.owner.position()); f.tick(giver);
                helper.assertTrue(rolls[0] == 1 && !slot.hasItems(), "交付不重复抽奖或应用掉落修饰");
            } finally { MinecraftForge.EVENT_BUS.unregister(modify); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void zeroTimeStillProducesAtMostOncePerTickAndNeverDuringRecall(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.settings.set(GameplayConfig.BASIC_TIME_INTERVAL, 0.0);
            var giver = f.add(BOMBS); var slot = f.slot(BOMBS, 0);
            helper.assertTrue(slot.cooldown() == 1, "零首次等待至少1tick");
            for (int i = 1; i <= 4; i++) {
                giver.setPos(f.hover().add(8, 0, 0)); f.tick(giver);
                helper.assertTrue(count(slot) == i && slot.cooldown() == 1, "每tick只推进和生产一次");
            }
            giver.setPos(f.hover().add(20, 0, 0)); f.tick(giver);
            helper.assertTrue(count(slot) == 4 && slot.cooldown() == 1, "召回期间暂停，即使零时间配置");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void fractionalAndNegativeEnhancementOnlyAffectFutureProduction(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var giver = f.add(BOMBS); var slot = f.slot(BOMBS, 0);
            var modifiers = f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).resolve().orElseThrow().getFamiliarModifiers();
            modifiers.add(FRACTION, 1);
            slot.produce(List.of(), 1); giver.setPos(f.hover().add(8, 0, 0)); f.tick(giver);
            helper.assertTrue(count(slot) == 1, "小数增强向下取整");
            modifiers.add(WEAKENING, 1);
            slot.produce(List.of(), 1); giver.setPos(f.hover().add(8, 0, 0)); f.tick(giver);
            helper.assertTrue(count(slot) == 1 && slot.cooldown() > 0, "负产量归零并进入冷却，不改写旧库存");
            modifiers.remove(WEAKENING, 1); new Bffs(0, 4).handleObtain(f.owner, null);
            slot.produce(List.of(), 1); giver.setPos(f.hover().add(8, 0, 0)); f.tick(giver);
            helper.assertTrue(count(slot) == 3, "BFFS只增强新产出，保留先前物品");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void giversShareTheBabyChainAndDoNotAttachOldPeriodicModules(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var periodic = RegistryManager.ACTIVE.getRegistry(ModPeriodicModules.PERIODIC_MODULE_KEY);
            var effects = RegistryManager.ACTIVE.getRegistry(ModExecutableEffects.EXECUTABLE_EFFECT_KEY);
            for (var type : TYPES) helper.assertTrue(!periodic.containsKey(type), "旧周期模块已从注册表删除 " + type);
            for (String name : List.of("bomb_bag", "the_relic", "loot_money"))
                helper.assertTrue(!effects.containsKey(id(name)), "旧的直接给予效果已删除 " + name);
            for (var type : TYPES) f.absorb(type);
            f.data.addCount(id("brother_bobby"), SOURCE, 1); f.data.addCount(id("sister_maggy"), SOURCE, 1);
            for (int i = 0; i < 5; i++) helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "正常生成混合队伍");
            for (var type : TYPES) helper.assertTrue(!f.owner.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow()
                    .getPeriodicModuleQueue().contains(type), "迁移后不再附加旧周期模块 " + type);
            var first = f.data.getEntry(id("brother_bobby")).orElseThrow();
            var second = f.data.getEntry(id("sister_maggy")).orElseThrow();
            var bobby = (AbstractIsaacFamiliarEntity) f.owner.serverLevel().getEntity(first.getEntityId(0));
            var maggy = (AbstractIsaacFamiliarEntity) f.owner.serverLevel().getEntity(second.getEntityId(0));
            helper.assertTrue(bobby.getFormationIndex() == 3 && maggy.getFormationIndex() == 4, "给予型与宝宝按加入顺序共用链槽");
            for (int i = 0; i < TYPES.size(); i++) {
                var giver = f.entity(TYPES.get(i));
                giver.tick();
                helper.assertTrue(giver.getFormationIndex() == i && f.slot(TYPES.get(i), 0) != null, "链槽与各类型库存slot相互独立");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void synergyLinesStayOutOfExtraDescriptions(GameTestHelper helper) {
        for (var type : TYPES) {
            var item = (PassiveItem) ForgeRegistries.ITEMS.getValue(type);
            helper.assertTrue(item.getAbility().getExtraDesc(null, null).isEmpty(), "给予型没有额外说明，协同只属于协同说明 " + type);
        }
        for (String name : List.of("brother_bobby", "sister_maggy", "ghost_baby", "harlequin_baby", "rotten_baby", "loki_baby", "robo_baby", "robo_baby_2", "demon_baby")) {
            var item = (PassiveItem) ForgeRegistries.ITEMS.getValue(id(name));
            helper.assertTrue(item.getAbility().getExtraDesc(null, null).equals(ModSetAbilities.CONJOINED.get().getExtraDesc()),
                    "宝宝保留原有套装额外说明，不混入BFFS协同 " + name);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void deliveryKeepsTheGiverInTheChainAndPullsItsFollowersAlong(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ResourceLocation bobbyType = id("brother_bobby"), maggyType = id("sister_maggy");
            f.data.addCount(bobbyType, SOURCE, 1); FamiliarHelper.spawnNextMissingFamiliar(f.owner);
            var bobby = (AbstractIsaacFamiliarEntity) f.owner.serverLevel().getEntity(f.data.getEntry(bobbyType).orElseThrow().getEntityId(0));
            var giver = f.add(COINS);
            f.data.addCount(maggyType, SOURCE, 1); FamiliarHelper.spawnNextMissingFamiliar(f.owner);
            var maggy = (AbstractIsaacFamiliarEntity) f.owner.serverLevel().getEntity(f.data.getEntry(maggyType).orElseThrow().getEntityId(0));
            var tail = f.add(BOMBS);
            bobby.setPos(f.hover().add(0, 0, 6)); giver.setPos(f.hover().add(6, 0, 0));
            maggy.setPos(f.hover().add(8, 0, 0)); tail.setPos(f.hover().add(10, 0, 0));
            var slot = f.slot(COINS, 0); slot.produce(List.of(new ItemStack(Items.DIAMOND, 4)), 1000);
            f.tick(giver); maggy.tick(); tail.tick();
            helper.assertTrue(Math.abs(maggy.getZ() - f.hover().z) < 1e-6 && maggy.getX() < f.hover().x + 8,
                    "后排追随送货袋子，不跳过它追向前排宝宝");
            double initialTailDistance = tail.distanceTo(f.owner), closestTailDistance = initialTailDistance;
            for (int i = 0; i < 70; i++) {
                f.tick(giver); maggy.tick(); tail.tick();
                closestTailDistance = Math.min(closestTailDistance, tail.distanceTo(f.owner));
                helper.assertTrue(giver.getFormationIndex() == 1 && maggy.getFormationIndex() == 2 && tail.getFormationIndex() == 3,
                        "交付与归队全程保持原链顺序");
            }
            helper.assertTrue(closestTailDistance < initialTailDistance - 2, "整串后排会被带到玩家附近");
            helper.assertTrue(!slot.hasItems() && f.drops.stream().mapToInt(d -> d.getItem().getCount()).sum() == 4, "仅由携货成员交付自身库存");
            helper.assertTrue(giver.distanceToSqr(bobby) < 2 && giver.distanceToSqr(f.owner) > 4, "空载后平滑返回前驱附近");
            helper.assertTrue(f.slot(COINS, 0) == slot && slot.cooldown() < 1000 && slot.cooldown() > 900, "交付和归队不更换库存slot或重置生产计时");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void sharedQuotaWaitsAndRemovingWaitingDemandKeepsEffectiveInventory(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.addCount(BOMBS, SOURCE, 65); f.data.addCount(COINS, SOURCE, 1);
            for (int i = 0; i < 64; i++) helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "生成共享配额内成员");
            helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(f.owner), "给予型共享64上限");
            f.entity(BOMBS).tick();
            var slot = f.slot(BOMBS, 0); slot.produce(List.of(new ItemStack(Items.DIAMOND)), 99);
            f.data.addCount(BOMBS, SOURCE, -1); FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(slot == f.slot(BOMBS, 0) && count(slot) == 1, "只减少等待需求不删除有效库存");
            f.data.addCount(BOMBS, SOURCE, -1); FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(f.data.getItemGivingData().get(BOMBS, 63) == null && count(slot) == 1, "真减需求删除最高编号slot");
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "释放容量后等待的另一类型补出");
            f.entity(COINS).tick();
            helper.assertTrue(f.slot(COINS, 0) != null, "成功实体首次运行初始化生产记录");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void mixedChainQuotaPreservesItemGivingInventoryAndProductionClock(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var bobby = id("brother_bobby");
            var cloud = id("depression");
            f.data.addCount(bobby, SOURCE, 64);
            f.data.addCount(COINS, SOURCE, 1);
            f.data.addCount(cloud, SOURCE, 1);
            for (int i = 0; i < 64; i++) helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "先填满混合链式共享上限");
            helper.assertTrue(f.data.getEffectiveCount(COINS) == 0 && f.data.getEffectiveCount(cloud) == 0,
                    "掉落跟班和云朵都等待宝宝释放容量");
            f.data.addCount(bobby, SOURCE, -1);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "释放的一个位置补出掉落跟班");
            var giver = f.entity(COINS);
            giver.setPos(f.hover().add(6, 0, 0));
            f.tick(giver);
            var slot = f.slot(COINS, 0);
            slot.produce(List.of(new ItemStack(Items.DIAMOND, 3)), 99);
            f.data.addCount(bobby, SOURCE, 1);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(f.data.getEffectiveCount(bobby) == 63 && f.data.getEffectiveCount(COINS) == 1
                    && f.data.getEffectiveCount(cloud) == 0 && f.slot(COINS, 0) == slot
                    && count(slot) == 3 && slot.cooldown() == 99,
                    "等优先级的新需求不抢占已分配位置，也不重建库存或重置计时");
            f.tick(giver);
            helper.assertTrue(f.slot(COINS, 0) == slot && slot.cooldown() == 98 && count(slot) == 3,
                    "共享配额变化后掉落跟班继续原生产进度");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void physicalCuriosAbsorptionSaveReloadAndDeathCopyPreserveStableState(GameTestHelper helper) {
        for (var type : TYPES) try (Fixture original = new Fixture(helper); Fixture restored = new Fixture(helper); Fixture clone = new Fixture(helper)) {
            original.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(4); tickCurios(original.owner);
            original.slots().setStackInSlot(0, new ItemStack(ForgeRegistries.ITEMS.getValue(type))); tickCurios(original.owner); tickCurios(original.owner);
            original.absorb(type); original.absorb(type);
            helper.assertTrue(original.data.getCount(type, type) == 3, "真实装备与两次吸收分别贡献需求");
            FamiliarHelper.spawnNextMissingFamiliar(original.owner);
            original.entity(type).tick();
            original.slot(type, 0).produce(List.of(new ItemStack(Items.DIAMOND, 5)), 77);
            restored.owner.load(original.owner.saveWithoutId(new CompoundTag())); DefaultAttributeManager.apply(restored.owner);
            CurioSlotManager.requestReconcile(restored.owner); tickCurios(restored.owner); tickCurios(restored.owner);
            helper.assertTrue(restored.data.getCount(type) == 3 && count(restored.slot(type, 0)) == 5 && restored.slot(type, 0).cooldown() == 77, "完整玩家保存恢复不重复授予且保留库存计时");
            restored.slots().setStackInSlot(0, ItemStack.EMPTY); tickCurios(restored.owner);
            helper.assertTrue(restored.data.getCount(type) == 2 && count(restored.slot(type, 0)) == 5, "卸下只减一份，低编号库存保留");
            ForgeEvents.onPlayerCloned(new PlayerEvent.Clone(clone.owner, restored.owner, true)); restored.owner.reviveCaps();
            helper.assertTrue(clone.data.getCount(type) == 2 && count(clone.slot(type, 0)) == 5, "死亡复制保留权威slot");
            clone.slot(type, 0).deliver(stack -> true);
            helper.assertTrue(count(restored.slot(type, 0)) == 5, "死亡复制库存深拷贝");
            clone.items.removeFromId(clone.owner, type); helper.assertTrue(clone.data.getCount(type) == 1, "移除吸收副本对称扣减");
            clone.items.clearPlayerPassiveItems(clone.owner); FamiliarHelper.maintainExistingFamiliars(clone.owner);
            helper.assertTrue(clone.data.getCount(type) == 0 && clone.data.getEntry(type).isEmpty(), "清空需求与slot由调度器清理");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void actualDimensionTransferKeepsPerTypeSlotInventory(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.add(BOMBS); var slot = f.slot(BOMBS, 0); slot.produce(List.of(new ItemStack(Items.DIAMOND, 7)), 88);
            var home = f.owner.serverLevel(); var destination = home.getServer().getLevel(Level.NETHER); Vec3 pos = f.owner.position();
            List<AbstractItemGivingFamiliarEntity> entered = new ArrayList<>();
            Consumer<EntityJoinLevelEvent> capture = event -> {
                if (event.getLevel() == destination && event.getEntity() instanceof AbstractItemGivingFamiliarEntity giver && giver.getOwner() == f.owner) entered.add(giver);
            };
            MinecraftForge.EVENT_BUS.addListener(capture);
            try {
                f.owner.teleportTo(destination, pos.x, pos.y, pos.z, 0, 0); FamiliarHelper.maintainExistingFamiliars(f.owner);
                destination.getChunkAt(f.owner.blockPosition());
                helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner) && entered.size() == 1 && slot == f.slot(BOMBS, 0) && count(slot) == 7 && slot.cooldown() == 88, "实际维度切换保留按类型保存的库存与冷却");
                FamiliarHelper.discardAllRuntimeFamiliars(f.owner);
            } finally { MinecraftForge.EVENT_BUS.unregister(capture); entered.forEach(AbstractIsaacFamiliarEntity::discardAndUnlink); f.owner.teleportTo(home, pos.x, pos.y, pos.z, 0, 0); }
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner) && count(slot) == 7, "返回维度仍复用同一slot");
        }
        helper.succeed();
    }

    private static List<ItemStack> inventory(ItemGivingSlot slot) {
        List<ItemStack> items = new ArrayList<>();
        slot.deliver(stack -> { items.add(stack); return false; });
        return items;
    }

    private static int count(ItemGivingSlot slot) { return inventory(slot).stream().mapToInt(ItemStack::getCount).sum(); }
    private static void tickCurios(ServerPlayer owner) {
        CurioSlotManager.tick(owner); new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(owner)); CurioSlotManager.tick(owner);
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final PlayerIsaacItems items;
        final GameTestDatapackSettings.Scope settings;
        final long originalTime;
        final List<ItemEntity> drops = new ArrayList<>();
        final Consumer<EntityJoinLevelEvent> capture;

        Fixture(GameTestHelper helper) {
            this.helper = helper; originalTime = helper.getLevel().getGameTime();
            settings = GameTestDatapackSettings.override(helper);
            settings.set(GameplayConfig.EXTRA_PASSIVE_ITEM_BACKPACK, true); settings.set(GameplayConfig.PASSIVE_ITEM_LIMIT, 256);
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "item-giver-test"));
            owner.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(2, 240, 2)); DefaultAttributeManager.apply(owner);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            items = owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
            capture = event -> { if (event.getEntity() instanceof ItemEntity item && item.position().distanceToSqr(owner.position()) < 1) drops.add(item); };
            MinecraftForge.EVENT_BUS.addListener(capture);
        }

        AbstractItemGivingFamiliarEntity add(ResourceLocation type) {
            data.addCount(type, SOURCE, 1); helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "实际调度生成 " + type);
            var familiar = entity(type); familiar.tick(); return familiar;
        }
        AbstractItemGivingFamiliarEntity entity(ResourceLocation type) {
            var entry = data.getEntry(type).orElseThrow(); return (AbstractItemGivingFamiliarEntity) owner.serverLevel().getEntity(entry.getEntityId(entry.getEntityCount() - 1));
        }
        ItemGivingSlot slot(ResourceLocation type, int slot) { return data.getItemGivingData().get(type, slot); }
        Vec3 hover() { return owner.position().add(0, owner.getBbHeight() * 0.55, 0); }
        void tick(AbstractIsaacFamiliarEntity entity) {
            owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(owner.level().getGameTime() + 1); entity.tick(); entity.tick();
        }
        void absorb(ResourceLocation type) {
            var item = (PassiveItem) ForgeRegistries.ITEMS.getValue(type); owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item)); item.use(owner.level(), owner, InteractionHand.MAIN_HAND);
        }
        IDynamicStackHandler slots() { return CuriosApi.getCuriosInventory(owner).resolve().orElseThrow().getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks(); }
        @Override public void close() {
            MinecraftForge.EVENT_BUS.unregister(capture); drops.forEach(ItemEntity::discard);
            var slots = CuriosApi.getCuriosInventory(owner).resolve().orElseThrow().getStacksHandler(CurioSlotManager.PASSIVE_ITEM);
            if (slots.isPresent()) { var stacks = slots.get().getStacks(); for (int i = 0; i < stacks.getSlots(); i++) stacks.setStackInSlot(i, ItemStack.EMPTY); tickCurios(owner); }
            items.clearPlayerPassiveItems(owner); FamiliarHelper.discardAllRuntimeFamiliars(owner); data.clearRequirements(); owner.discard();
            helper.getLevel().getServer().getWorldData().overworldData().setGameTime(originalTime); settings.close();
        }
    }
}
