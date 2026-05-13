package com.xiaoshi2022.kamenriderweaponcraft.rider.effect;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExternalRiderRegistry {

    private static final Logger LOGGER = KamenRiderWeaponCraft.LOGGER;
    private static final Map<String, ExternalRiderEffectProvider> EXTERNAL_RIDERS = new ConcurrentHashMap<>();
    private static final Map<String, SoundEvent> EXTERNAL_RIDER_SOUNDS = new ConcurrentHashMap<>();
    private static final List<String> EXTERNAL_RIDER_ORDER = Collections.synchronizedList(new ArrayList<>());

    public static void registerExternalRider(String modId, ExternalRiderEffectProvider provider) {
        String riderId = modId + ":" + provider.getExternalRiderId();

        if (EXTERNAL_RIDERS.containsKey(riderId)) {
            LOGGER.warn("External rider {} is already registered, skipping...", riderId);
            return;
        }

        EXTERNAL_RIDERS.put(riderId, provider);
        EXTERNAL_RIDER_ORDER.add(riderId);
        LOGGER.info("Registered external rider: {} from mod {}", riderId, modId);
    }

    public static void registerExternalRiderSound(String riderId, SoundEvent sound) {
        EXTERNAL_RIDER_SOUNDS.put(riderId, sound);
    }

    public static ExternalRiderEffectProvider getExternalRider(String riderId) {
        return EXTERNAL_RIDERS.get(riderId);
    }

    public static boolean isExternalRider(String riderId) {
        return EXTERNAL_RIDERS.containsKey(riderId);
    }

    public static Collection<ExternalRiderEffectProvider> getAllExternalRiders() {
        return EXTERNAL_RIDERS.values();
    }

    public static List<String> getExternalRiderOrder() {
        return Collections.unmodifiableList(EXTERNAL_RIDER_ORDER);
    }

    public static SoundEvent getExternalRiderSound(String riderId) {
        return EXTERNAL_RIDER_SOUNDS.get(riderId);
    }

    public static double getExternalRiderEnergyCost(String riderId) {
        ExternalRiderEffectProvider provider = EXTERNAL_RIDERS.get(riderId);
        if (provider != null) {
            return provider.getEnergyCost();
        }
        return 20.0;
    }

    public static void playExternalRiderSelectionSound(Level level, LivingEntity shooter, String riderId) {
        SoundEvent sound = EXTERNAL_RIDER_SOUNDS.get(riderId);
        if (sound != null) {
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    sound, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void playExternalRiderAttackSound(Level level, LivingEntity shooter, String riderId) {
        SoundEvent sound = EXTERNAL_RIDER_SOUNDS.get(riderId);
        if (sound != null) {
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    sound, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static Set<String> getRegisteredRiderIds() {
        return new HashSet<>(EXTERNAL_RIDERS.keySet());
    }

    public static void clear() {
        EXTERNAL_RIDERS.clear();
        EXTERNAL_RIDER_SOUNDS.clear();
        EXTERNAL_RIDER_ORDER.clear();
        LOGGER.info("Cleared all external rider registrations");
    }

    public static int getExternalRiderCount() {
        return EXTERNAL_RIDERS.size();
    }
}