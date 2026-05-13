package com.xiaoshi2022.kamenriderweaponcraft.rider.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ExternalRiderEffectWrapper extends AbstractHeiseiRiderEffect {

    private final ExternalRiderEffectProvider provider;
    private final String riderId;

    public ExternalRiderEffectWrapper(String riderId, ExternalRiderEffectProvider provider) {
        this.riderId = riderId;
        this.provider = provider;
    }

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        provider.executeSkill(level, shooter, direction);
    }

    @Override
    public String getRiderName() {
        return provider.getExternalRiderName();
    }

    @Override
    public String getActivationSoundName() {
        return provider.getActivationSoundName();
    }

    @Override
    public float getAttackDamage() {
        return provider.getAttackDamage();
    }

    @Override
    public float getEffectRange() {
        return provider.getEffectRange();
    }

    @Override
    public double getEnergyCost() {
        return provider.getEnergyCost();
    }

    @Nullable
    @Override
    public ResourceLocation getExternalModelLocation() {
        return provider.getExternalModelLocation();
    }

    @Nullable
    @Override
    public Supplier<ResourceLocation> getExternalAnimController() {
        return provider.getExternalAnimController();
    }

    public String getRiderId() {
        return riderId;
    }

    public ExternalRiderEffectProvider getProvider() {
        return provider;
    }

    public boolean supportsScrambleMode() {
        return provider.supportsScrambleMode();
    }

    public int getScrambleModeMaxLayers() {
        return provider.getScrambleModeMaxLayers();
    }
}