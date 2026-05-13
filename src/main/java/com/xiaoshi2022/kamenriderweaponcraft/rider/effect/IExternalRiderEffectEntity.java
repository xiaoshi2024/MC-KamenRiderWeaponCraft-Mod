package com.xiaoshi2022.kamenriderweaponcraft.rider.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public interface IExternalRiderEffectEntity extends GeoEntity {

    String getExternalEntityId();

    void initializeEffectEntity(Level level, LivingEntity owner, Vec3 direction, float damage);

    void setOwnerUUID(UUID uuid);

    UUID getOwnerUUID();

    float getEffectDamage();

    double getEffectRange();

    int getMaxLifetime();

    default void onEffectHit(Entity target, Level level) {
    }

    default void onEffectTick(Level level) {
    }

    default void onEffectExpired(Level level) {
    }

    @Nullable
    default ResourceLocation getCustomHitParticleType() {
        return null;
    }

    @Nullable
    default Supplier<ResourceLocation> getCustomAnimationController() {
        return null;
    }

    default boolean isAffectedByKnockback() {
        return true;
    }

    default float getKnockbackStrength() {
        return 1.0f;
    }

    static IExternalRiderEffectEntity createDefaultInstance() {
        return null;
    }
}