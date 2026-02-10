package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.daidaimaru.entity.ThrownDaidaimaru;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.needlekunai.entity.ThrownNeedleKunai;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.server.entity.LaserBeamEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.entity.line.denliner;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.Faiz.FaizEmptySetEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build.BuildRiderEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.decade.DecadeRiderEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.den_o.DenOTrainEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.drive.DriveRiderEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid.ExAidSlashEffectEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.fourze.FourzeRocketEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.gaim.GaimLockSeedEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ghost.GhostHeroicSoulEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.hibiki.HibikiDrumEffectEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kiva.KivaBatEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kuuga.KuugaRiderEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ooo.OOOGeoEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.w.WTornadoEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.wizard.WizardRiderEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);

    public static final RegistryObject<EntityType<ThrownDaidaimaru>> THROWN_DAIDAIMARU = ENTITIES.register("thrown_daidaimaru", () ->
            EntityType.Builder.<ThrownDaidaimaru>of(ThrownDaidaimaru::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("thrown_daidaimaru"));

    public static final RegistryObject<EntityType<ThrownNeedleKunai>> THROWN_NEEDLE_KUNAI = ENTITIES.register("thrown_needle_kunai", () ->
            EntityType.Builder.<ThrownNeedleKunai>of(ThrownNeedleKunai::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("thrown_needle_kunai"));

    public static final RegistryObject<EntityType<LaserBeamEntity>> LASER_BEAM =
            ENTITIES.register("laser_beam",
                    () -> EntityType.Builder.<LaserBeamEntity>of(LaserBeamEntity::new, MobCategory.MISC)
                            .sized(0.2F, 0.2F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("laser_beam"));

    // Kamen Rider Build 特效实体
    public static final RegistryObject<EntityType<BuildRiderEntity>> BUILD_RIDER_EFFECT = 
            ENTITIES.register("build_rider_effect", 
                    () -> EntityType.Builder.<BuildRiderEntity>of(BuildRiderEntity::new, MobCategory.MISC)
                            .setTrackingRange(64)
                            .setUpdateInterval(1)
                            .setShouldReceiveVelocityUpdates(false)
                            .build("build_rider_effect"));

    // Kamen Rider Drive 特效实体
    public static final RegistryObject<EntityType<DriveRiderEntity>> DRIVE_RIDER_EFFECT = 
            ENTITIES.register("drive_rider_effect", 
                    () -> EntityType.Builder.<DriveRiderEntity>of(DriveRiderEntity::new, MobCategory.MISC)
                            .setTrackingRange(64)
                            .setUpdateInterval(1)
                            .setShouldReceiveVelocityUpdates(false)
                            .build("drive_rider_effect"));
                             
    // Kamen Rider Ex-Aid 砍击特效实体
    public static final RegistryObject<EntityType<ExAidSlashEffectEntity>> EXAID_SLASH_EFFECT = 
            ENTITIES.register("exaid_slash_effect", 
                    () -> EntityType.Builder.<ExAidSlashEffectEntity>of(ExAidSlashEffectEntity::new, MobCategory.MISC)
                            .setTrackingRange(64)
                            .setUpdateInterval(1)
                            .setShouldReceiveVelocityUpdates(false)
                            .build("exaid_slash_effect"));
    
    // Kamen Rider Ghost 伟人魂实体
    public static final RegistryObject<EntityType<GhostHeroicSoulEntity>> GHOST_HEROIC_SOUL = 
            ENTITIES.register("ghost_heroic_soul", 
                    () -> EntityType.Builder.<GhostHeroicSoulEntity>of(GhostHeroicSoulEntity::new, MobCategory.MISC)
                            .setTrackingRange(64)
                            .setUpdateInterval(1)
                            .setShouldReceiveVelocityUpdates(false)
                            .build("ghost_heroic_soul"));
                            
    // Kamen Rider Gaim 锁种特效实体
    public static final RegistryObject<EntityType<GaimLockSeedEntity>> GAIM_LOCK_SEED = 
            ENTITIES.register("gaim_lock_seed", 
                    () -> EntityType.Builder.<GaimLockSeedEntity>of(GaimLockSeedEntity::new, MobCategory.MISC)
                            .setTrackingRange(64)
                            .setUpdateInterval(1)
                            .setShouldReceiveVelocityUpdates(false)
                            .build("gaim_lock_seed"));

    // Wizard魔法特效实体
    public static final RegistryObject<EntityType<WizardRiderEntity>> WIZARD_EFFECT =
            ENTITIES.register("wizard_effect",
                    () -> EntityType.Builder.<WizardRiderEntity>of(WizardRiderEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("wizard_effect")
            );
            
    // Fourze火箭炮实体
    public static final RegistryObject<EntityType<FourzeRocketEntity>> FOURZE_ROCKET =
            ENTITIES.register("fourze_rocket",
                    () -> EntityType.Builder.<FourzeRocketEntity>of(FourzeRocketEntity::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)  // 火箭炮大小
                            .clientTrackingRange(16)  // 客户端追踪距离
                            .updateInterval(1)  // 更新频率
                            .build("fourze_rocket")
            );
    
    // Kamen Rider OOO 细胞硬币斩实体
    public static final RegistryObject<EntityType<OOOGeoEntity>> OOO_GEO_EFFECT = 
            ENTITIES.register("ooo_geo_effect", 
                    () -> EntityType.Builder.<OOOGeoEntity>of(OOOGeoEntity::new, MobCategory.MISC)
                            .setTrackingRange(64)
                            .setUpdateInterval(1)
                            .setShouldReceiveVelocityUpdates(false)
                            .build("ooo_geo_effect"));

    // Kamen Rider W 龙卷风实体
    public static final RegistryObject<EntityType<WTornadoEntity>> W_TORNADO = 
            ENTITIES.register("w_tornado", 
                    () -> EntityType.Builder.<WTornadoEntity>of(WTornadoEntity::new, MobCategory.MISC)
                            .sized(1.5F, 2.0F)
                            .setTrackingRange(10)
                            .setUpdateInterval(1)
                            .setShouldReceiveVelocityUpdates(true)
                            .build("w_tornado"));
    
    // Kamen Rider Decade 次元踢特效实体
    public static final RegistryObject<EntityType<DecadeRiderEntity>> DECADE_RIDER = 
            ENTITIES.register("decade_rider", 
                    () -> EntityType.Builder.<DecadeRiderEntity>of(DecadeRiderEntity::new, MobCategory.MISC)
                            .setTrackingRange(64)
                            .setUpdateInterval(1)
                            .setShouldReceiveVelocityUpdates(false)
                            .build("decade_rider"));
    
    // Kamen Rider Kiva 蝙蝠群特效实体
    public static final RegistryObject<EntityType<KivaBatEntity>> KIVA_BAT_EFFECT = 
            ENTITIES.register("kiva_bat_effect", 
                    () -> EntityType.Builder.<KivaBatEntity>of(KivaBatEntity::new, MobCategory.MISC)
                            .setTrackingRange(64)
                            .setUpdateInterval(1)
                            .setShouldReceiveVelocityUpdates(true)
                            .build("kiva_bat_effect"));

    // 电王列车武器实体
    public static final RegistryObject<EntityType<DenOTrainEntity>> DEN_O_TRAIN = ENTITIES.register(
            "den_o_train",
            () -> EntityType.Builder.<DenOTrainEntity>
                            of(DenOTrainEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .setTrackingRange(80)
                    .setUpdateInterval(3)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(MOD_ID, "den_o_train").toString())
    );
    
    // Kamen Rider Hibiki 鼓锁定特效实体
    public static final RegistryObject<EntityType<HibikiDrumEffectEntity>> HIBIKI_DRUM_EFFECT = 
            ENTITIES.register("hibiki_drum_effect", 
                    () -> EntityType.Builder.<HibikiDrumEffectEntity>of(HibikiDrumEffectEntity::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)  // 设置合适的大小，鼓的大小
                            .setTrackingRange(64)
                            .setUpdateInterval(1)
                            .setShouldReceiveVelocityUpdates(false)
                            .build("hibiki_drum_effect"));

    // 注册Kuuga特效实体
    public static final RegistryObject<EntityType<KuugaRiderEntity>> KUUGA_RIDER =
            ENTITIES.register("kuuga_rider",
                    () -> EntityType.Builder.<KuugaRiderEntity>
                                    of(KuugaRiderEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F) // 实体大小
                            .setTrackingRange(64) // 跟踪范围
                            .setUpdateInterval(1) // 更新间隔
                            .setShouldReceiveVelocityUpdates(true)
                            .build(new ResourceLocation("kamen_rider_weapon_craft", "kuuga_rider").toString())
            );

    // 注册Faiz空集符号实体
    public static final RegistryObject<EntityType<FaizEmptySetEntity>> FAIZ_EMPTY_SET =
            ENTITIES.register("faiz_empty_set",
                    () -> EntityType.Builder.<FaizEmptySetEntity>
                                    of(FaizEmptySetEntity::new, MobCategory.MISC)
                            .setTrackingRange(64)
                            .setUpdateInterval(1)
                            .setShouldReceiveVelocityUpdates(false)
                            .build("faiz_empty_set")
            );
    
    // 电王电车实体
    public static final RegistryObject<EntityType<denliner>> DENLINER = ENTITIES.register(
            "denliner",
            () -> EntityType.Builder.<denliner>
                            of(denliner::new, MobCategory.MISC)
                    .sized(2.0F, 1.0F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("denliner")
    );

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}