package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.daidaimaru.entity.ThrownDaidaimaru;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.entity.LaserBeamEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build.BuildRiderEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.drive.DriveRiderEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid.ExAidSlashEffectEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.gaim.GaimLockSeedEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ghost.GhostHeroicSoulEntity;
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

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}