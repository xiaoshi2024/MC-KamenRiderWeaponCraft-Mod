package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.daidaimaru.entity.ThrownDaidaimaru;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.entity.LaserBeamEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.ThrownTrident;
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

    public static void register(IEventBus modEventBus) {
    }
}