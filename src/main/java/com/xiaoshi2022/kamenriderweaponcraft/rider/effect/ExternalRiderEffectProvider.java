package com.xiaoshi2022.kamenriderweaponcraft.rider.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public interface ExternalRiderEffectProvider {

    String getExternalRiderId();

    String getExternalRiderName();

    float getAttackDamage();

    float getEffectRange();

    double getEnergyCost();

    String getActivationSoundName();

    void executeSkill(Level level, LivingEntity shooter, Vec3 direction);

    @Nullable
    ResourceLocation getExternalModelLocation();

    @Nullable
    Supplier<ResourceLocation> getExternalAnimController();

    boolean isExternal();

    default boolean supportsScrambleMode() {
        return true;
    }

    default int getScrambleModeMaxLayers() {
        return 4;
    }
}