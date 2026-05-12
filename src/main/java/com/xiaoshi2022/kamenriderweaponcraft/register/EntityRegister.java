package com.xiaoshi2022.kamenriderweaponcraft.register;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.faiz.FaizEmptySetEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.decade.DecadeRiderEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.den_o.DenOTrainEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.drive.DriveRiderEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.fourze.FourzeRocketEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.gaim.GaimLockSeedEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.hibiki.HibikiDrumEffectEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kiva.KivaBatEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kuuga.KuugaRiderEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ooo.OOOGeoEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.w.WTornadoEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.wizard.WizardRiderEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityRegister {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, KamenRiderWeaponCraft.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<DecadeRiderEntity>> DECADE_RIDER =
            ENTITY_TYPES.register("decade_rider", () -> EntityType.Builder.<DecadeRiderEntity>of(
                    DecadeRiderEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("decade_rider"));

    public static final DeferredHolder<EntityType<?>, EntityType<KuugaRiderEntity>> KUUGA_RIDER =
            ENTITY_TYPES.register("kuuga_rider", () -> EntityType.Builder.<KuugaRiderEntity>of(
                    KuugaRiderEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("kuuga_rider"));

    public static final DeferredHolder<EntityType<?>, EntityType<WizardRiderEntity>> WIZARD_RIDER =
            ENTITY_TYPES.register("wizard_rider", () -> EntityType.Builder.<WizardRiderEntity>of(
                    WizardRiderEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("wizard_rider"));

    public static final DeferredHolder<EntityType<?>, EntityType<DriveRiderEntity>> DRIVE_RIDER =
            ENTITY_TYPES.register("drive_rider", () -> EntityType.Builder.<DriveRiderEntity>of(
                    DriveRiderEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("drive_rider"));

    public static final DeferredHolder<EntityType<?>, EntityType<DenOTrainEntity>> DEN_O_TRAIN =
            ENTITY_TYPES.register("den_o_train", () -> EntityType.Builder.<DenOTrainEntity>of(
                    DenOTrainEntity::new, MobCategory.MISC)
                    .sized(2.0F, 1.0F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("den_o_train"));

    public static final DeferredHolder<EntityType<?>, EntityType<FourzeRocketEntity>> FOURZE_ROCKET =
            ENTITY_TYPES.register("fourze_rocket", () -> EntityType.Builder.<FourzeRocketEntity>of(
                    FourzeRocketEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("fourze_rocket"));

    public static final DeferredHolder<EntityType<?>, EntityType<GaimLockSeedEntity>> GAIM_LOCK_SEED =
            ENTITY_TYPES.register("gaim_lock_seed", () -> EntityType.Builder.<GaimLockSeedEntity>of(
                    GaimLockSeedEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("gaim_lock_seed"));

    public static final DeferredHolder<EntityType<?>, EntityType<HibikiDrumEffectEntity>> HIBIKI_DRUM_EFFECT =
            ENTITY_TYPES.register("hibiki_drum_effect", () -> EntityType.Builder.<HibikiDrumEffectEntity>of(
                    HibikiDrumEffectEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("hibiki_drum_effect"));

    public static final DeferredHolder<EntityType<?>,EntityType<KivaBatEntity>> KIVA_BAT_EFFECT = ENTITY_TYPES.register("kiva_bat_effect",
            () -> EntityType.Builder.<KivaBatEntity>of(KivaBatEntity::new, MobCategory.MISC)
                    .sized(0.8f, 0.8f)
                    .build("kiva_bat_effect"));

    public static final DeferredHolder<EntityType<?>,EntityType<OOOGeoEntity>> OOO_GEO_EFFECT = ENTITY_TYPES.register("ooo_geo_effect",
            () -> EntityType.Builder.<OOOGeoEntity>of(OOOGeoEntity::new, MobCategory.MISC)
                    .sized(0.8f, 0.8f)
                    .build("ooo_geo_effect"));

    public static final DeferredHolder<EntityType<?>, EntityType<WTornadoEntity>> W_TORNADO =
            ENTITY_TYPES.register("w_tornado", () -> EntityType.Builder.<WTornadoEntity>of(
                    WTornadoEntity::new, MobCategory.MISC)
                    .sized(1.5F, 2.0F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("w_tornado"));

    public static final DeferredHolder<EntityType<?>, EntityType<FaizEmptySetEntity>> FAIZ_EMPTY_SET =
            ENTITY_TYPES.register("faiz_empty_set", () -> EntityType.Builder.<FaizEmptySetEntity>of(
                    FaizEmptySetEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("faiz_empty_set"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}