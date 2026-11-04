package net.luojiuoscar.isaac_disaster.entity;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.BlueSpiderEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.BlueFlyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfConquestEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfDeathEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfFamineEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfPestilenceEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfWarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.brother_bobby.BrotherBobbyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.headless_baby.HeadlessBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.sister_maggy.SisterMaggyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.ghost_baby.GhostBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.harlequin_baby.HarlequinBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.rotten_baby.RottenBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.loki_baby.LokiBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.robo_baby.RoboBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.robo_baby_2.RoboBaby2Entity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.demon_baby.DemonBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.sack_of_pennies.SackOfPenniesEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.bomb_bag.BombBagEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.the_relic.TheRelicEntity;
import net.luojiuoscar.isaac_disaster.entity.tnt.GigaBomb;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;



public class ModEntities {
    public static final DeferredRegister<EntityType<?>> MOD_ENTITIES
            = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        MOD_ENTITIES.register(eventBus);
    }

    public static final RegistryObject<EntityType<IsaacBomb>> ISAAC_BOMB = MOD_ENTITIES.register("isaac_bomb",
            () -> EntityType.Builder.<IsaacBomb>of(IsaacBomb::new, MobCategory.MISC)
                    .sized(0.98f, 0.98f) // 碰撞箱大小
                    .build("isaac_bomb")
    );

    public static final RegistryObject<EntityType<IsaacBomb>> GIGA_BOMB = MOD_ENTITIES.register("giga_bomb",
            () -> EntityType.Builder.<IsaacBomb>of(GigaBomb::new, MobCategory.MISC)
                    .sized(2.5f, 2.5f) // 碰撞箱大小
                    .build("giga_bomb")
    );

    public static final RegistryObject<EntityType<MomKnifeEntity>> MOM_KNIFE =
            MOD_ENTITIES.register("mom_knife",
                    () -> EntityType.Builder.<MomKnifeEntity>of(MomKnifeEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("mom_knife"));

    public static final RegistryObject<EntityType<PlayerMomsKnifeEntity>> PLAYER_MOMS_KNIFE =
            MOD_ENTITIES.register("player_moms_knife",
                    () -> EntityType.Builder.<PlayerMomsKnifeEntity>of(PlayerMomsKnifeEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("player_moms_knife"));
    public static final RegistryObject<EntityType<BrotherBobbyEntity>> BROTHER_BOBBY =
            MOD_ENTITIES.register("brother_bobby",
                    () -> EntityType.Builder.<BrotherBobbyEntity>of(BrotherBobbyEntity::new, MobCategory.MISC)
                            .sized(0.875F * AbstractBabyFamiliarEntity.BASE_SCALE, 1.4375F * AbstractBabyFamiliarEntity.BASE_SCALE)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("brother_bobby"));

    public static final RegistryObject<EntityType<SisterMaggyEntity>> SISTER_MAGGY =
            MOD_ENTITIES.register("sister_maggy",
                    () -> EntityType.Builder.<SisterMaggyEntity>of(SisterMaggyEntity::new, MobCategory.MISC)
                            .sized(0.875F * AbstractBabyFamiliarEntity.BASE_SCALE, 1.4375F * AbstractBabyFamiliarEntity.BASE_SCALE)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("sister_maggy"));

    public static final RegistryObject<EntityType<GhostBabyEntity>> GHOST_BABY =
            MOD_ENTITIES.register("ghost_baby",
                    () -> EntityType.Builder.<GhostBabyEntity>of(GhostBabyEntity::new, MobCategory.MISC)
                            .sized(0.875F * AbstractBabyFamiliarEntity.BASE_SCALE, 1.4375F * AbstractBabyFamiliarEntity.BASE_SCALE)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("ghost_baby"));

    public static final RegistryObject<EntityType<HarlequinBabyEntity>> HARLEQUIN_BABY =
            MOD_ENTITIES.register("harlequin_baby",
                    () -> EntityType.Builder.<HarlequinBabyEntity>of(HarlequinBabyEntity::new, MobCategory.MISC)
                            .sized(0.875F * AbstractBabyFamiliarEntity.BASE_SCALE, 1.4375F * AbstractBabyFamiliarEntity.BASE_SCALE)
                            .clientTrackingRange(8).updateInterval(1).build("harlequin_baby"));

    public static final RegistryObject<EntityType<RottenBabyEntity>> ROTTEN_BABY =
            MOD_ENTITIES.register("rotten_baby",
                    () -> EntityType.Builder.<RottenBabyEntity>of(RottenBabyEntity::new, MobCategory.MISC)
                            .sized(0.875F * AbstractBabyFamiliarEntity.BASE_SCALE, 1.4375F * AbstractBabyFamiliarEntity.BASE_SCALE)
                            .clientTrackingRange(8).updateInterval(1).build("rotten_baby"));

    public static final RegistryObject<EntityType<LokiBabyEntity>> LOKI_BABY =
            MOD_ENTITIES.register("loki_baby",
                    () -> EntityType.Builder.<LokiBabyEntity>of(LokiBabyEntity::new, MobCategory.MISC)
                            .sized(0.875F * AbstractBabyFamiliarEntity.BASE_SCALE, 1.4375F * AbstractBabyFamiliarEntity.BASE_SCALE)
                            .clientTrackingRange(8).updateInterval(1).build("loki_baby"));

    public static final RegistryObject<EntityType<RoboBabyEntity>> ROBO_BABY =
            MOD_ENTITIES.register("robo_baby",
                    () -> EntityType.Builder.<RoboBabyEntity>of(RoboBabyEntity::new, MobCategory.MISC)
                            .sized(0.84375F * AbstractBabyFamiliarEntity.BASE_SCALE, 1.34375F * AbstractBabyFamiliarEntity.BASE_SCALE)
                            .clientTrackingRange(8).updateInterval(1).build("robo_baby"));

    public static final RegistryObject<EntityType<RoboBaby2Entity>> ROBO_BABY_2 =
            MOD_ENTITIES.register("robo_baby_2",
                    () -> EntityType.Builder.<RoboBaby2Entity>of(RoboBaby2Entity::new, MobCategory.MISC)
                            .sized(1.0F * AbstractBabyFamiliarEntity.BASE_SCALE, 1.8125F * AbstractBabyFamiliarEntity.BASE_SCALE)
                            .clientTrackingRange(8).updateInterval(1).build("robo_baby_2"));

    public static final RegistryObject<EntityType<DemonBabyEntity>> DEMON_BABY =
            MOD_ENTITIES.register("demon_baby",
                    () -> EntityType.Builder.<DemonBabyEntity>of(DemonBabyEntity::new, MobCategory.MISC)
                            .sized(1.125F * AbstractBabyFamiliarEntity.BASE_SCALE, 1.75F * AbstractBabyFamiliarEntity.BASE_SCALE)
                            .clientTrackingRange(8).updateInterval(1).build("demon_baby"));

    public static final RegistryObject<EntityType<HeadlessBabyEntity>> HEADLESS_BABY =
            MOD_ENTITIES.register("headless_baby", () -> EntityType.Builder.of(HeadlessBabyEntity::new, MobCategory.MISC)
                    .sized(0.75F * HeadlessBabyEntity.BASE_SCALE, 0.6875F * HeadlessBabyEntity.BASE_SCALE)
                    .clientTrackingRange(8).updateInterval(1).build("headless_baby"));

    public static final RegistryObject<EntityType<SackOfPenniesEntity>> SACK_OF_PENNIES =
            MOD_ENTITIES.register("sack_of_pennies", () -> EntityType.Builder.<SackOfPenniesEntity>of(SackOfPenniesEntity::new, MobCategory.MISC)
                    .sized(0.375F, 0.625F).clientTrackingRange(8).updateInterval(1).build("sack_of_pennies"));
    public static final RegistryObject<EntityType<BombBagEntity>> BOMB_BAG =
            MOD_ENTITIES.register("bomb_bag", () -> EntityType.Builder.<BombBagEntity>of(BombBagEntity::new, MobCategory.MISC)
                    .sized(0.375F, 0.625F).clientTrackingRange(8).updateInterval(1).build("bomb_bag"));
    public static final RegistryObject<EntityType<TheRelicEntity>> THE_RELIC =
            MOD_ENTITIES.register("the_relic", () -> EntityType.Builder.<TheRelicEntity>of(TheRelicEntity::new, MobCategory.MISC)
                    .sized(0.5859375F, 0.703125F).clientTrackingRange(8).updateInterval(1).build("the_relic"));

    public static final RegistryObject<EntityType<BlueFlyEntity>> BLUE_FLY =
            MOD_ENTITIES.register("blue_fly",
                    () -> EntityType.Builder.<BlueFlyEntity>of(BlueFlyEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("blue_fly"));

    public static final RegistryObject<EntityType<LocustOfPestilenceEntity>> LOCUST_OF_PESTILENCE =
            MOD_ENTITIES.register("locust_of_pestilence",
                    () -> EntityType.Builder.<LocustOfPestilenceEntity>of(LocustOfPestilenceEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("locust_of_pestilence"));

    public static final RegistryObject<EntityType<LocustOfFamineEntity>> LOCUST_OF_FAMINE =
            MOD_ENTITIES.register("locust_of_famine",
                    () -> EntityType.Builder.<LocustOfFamineEntity>of(LocustOfFamineEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("locust_of_famine"));

    public static final RegistryObject<EntityType<LocustOfDeathEntity>> LOCUST_OF_DEATH =
            MOD_ENTITIES.register("locust_of_death",
                    () -> EntityType.Builder.<LocustOfDeathEntity>of(LocustOfDeathEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("locust_of_death"));

    public static final RegistryObject<EntityType<LocustOfConquestEntity>> LOCUST_OF_CONQUEST =
            MOD_ENTITIES.register("locust_of_conquest",
                    () -> EntityType.Builder.<LocustOfConquestEntity>of(LocustOfConquestEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("locust_of_conquest"));

    public static final RegistryObject<EntityType<LocustOfWarEntity>> LOCUST_OF_WAR =
            MOD_ENTITIES.register("locust_of_war",
                    () -> EntityType.Builder.<LocustOfWarEntity>of(LocustOfWarEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("locust_of_war"));

    public static final RegistryObject<EntityType<BlueSpiderEntity>> BLUE_SPIDER =
            MOD_ENTITIES.register("blue_spider",
                    () -> EntityType.Builder.<BlueSpiderEntity>of(BlueSpiderEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.15F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("blue_spider"));
}
