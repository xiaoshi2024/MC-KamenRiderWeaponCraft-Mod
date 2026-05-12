package com.xiaoshi2022.kamenriderweaponcraft.rider.energy;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.network.HeiseiswordEnergySyncPacket;
import com.xiaoshi2022.kamenriderweaponcraft.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeiseiswordEnergyManager {
    private static final String TAG_CURRENT_ENERGY = "kamenriderweaponcraft_current_energy";
    private static final String TAG_MAX_ENERGY = "kamenriderweaponcraft_max_energy";

    private static final double DEFAULT_MAX_ENERGY = 100.0;
    private static final double ENERGY_REGEN_RATE = 2.0;

    // 客户端专用能量缓存
    public static final Map<UUID, EnergyData> CLIENT_ENERGY_DATA = new HashMap<>();

    public static class EnergyData {
        public double currentEnergy;
        public double maxEnergy;

        public EnergyData(double currentEnergy, double maxEnergy) {
            this.currentEnergy = currentEnergy;
            this.maxEnergy = maxEnergy;
        }
    }

    // ==================== 核心方法 (使用 Player) ====================

    public static double getCurrentEnergy(Player player) {
        if (player == null) return DEFAULT_MAX_ENERGY;

        if (player.level().isClientSide) {
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null) return data.currentEnergy;
            return DEFAULT_MAX_ENERGY;
        }

        CompoundTag persistedData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag energyData = persistedData.getCompound("heiseisword_energy");

        if (energyData.isEmpty()) {
            initEnergyData(player, energyData, persistedData);
        }

        return energyData.getDouble(TAG_CURRENT_ENERGY);
    }

    public static double getMaxEnergy(Player player) {
        if (player == null) return DEFAULT_MAX_ENERGY;

        if (player.level().isClientSide) {
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null) return data.maxEnergy;
            return DEFAULT_MAX_ENERGY;
        }

        CompoundTag persistedData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag energyData = persistedData.getCompound("heiseisword_energy");

        if (energyData.isEmpty()) {
            initEnergyData(player, energyData, persistedData);
        }

        return energyData.getDouble(TAG_MAX_ENERGY);
    }

    private static void initEnergyData(Player player, CompoundTag energyData, CompoundTag persistedData) {
        energyData.putDouble(TAG_CURRENT_ENERGY, DEFAULT_MAX_ENERGY);
        energyData.putDouble(TAG_MAX_ENERGY, DEFAULT_MAX_ENERGY);
        persistedData.put("heiseisword_energy", energyData);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistedData);
        syncToClient(player);
    }

    public static void setCurrentEnergy(Player player, double energy) {
        if (player == null) return;

        double maxEnergy = getMaxEnergy(player);
        energy = Math.max(0, Math.min(energy, maxEnergy));

        if (player.level().isClientSide) {
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null) {
                data.currentEnergy = energy;
            } else {
                CLIENT_ENERGY_DATA.put(player.getUUID(), new EnergyData(energy, maxEnergy));
            }
            return;
        }

        CompoundTag persistedData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag energyData = persistedData.getCompound("heiseisword_energy");
        energyData.putDouble(TAG_CURRENT_ENERGY, energy);
        energyData.putDouble(TAG_MAX_ENERGY, maxEnergy);
        persistedData.put("heiseisword_energy", energyData);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistedData);
        syncToClient(player);
    }

    public static void setMaxEnergy(Player player, double maxEnergy) {
        if (player == null) return;

        maxEnergy = Math.max(1.0, maxEnergy);
        double currentEnergy = Math.min(getCurrentEnergy(player), maxEnergy);

        if (player.level().isClientSide) {
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null) {
                data.maxEnergy = maxEnergy;
                data.currentEnergy = currentEnergy;
            } else {
                CLIENT_ENERGY_DATA.put(player.getUUID(), new EnergyData(currentEnergy, maxEnergy));
            }
            return;
        }

        CompoundTag persistedData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag energyData = persistedData.getCompound("heiseisword_energy");
        energyData.putDouble(TAG_CURRENT_ENERGY, currentEnergy);
        energyData.putDouble(TAG_MAX_ENERGY, maxEnergy);
        persistedData.put("heiseisword_energy", energyData);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistedData);
        syncToClient(player);
    }

    public static boolean consumeEnergy(Player player, double amount) {
        if (player == null) return false;

        double current = getCurrentEnergy(player);
        if (current >= amount) {
            setCurrentEnergy(player, current - amount);
            return true;
        }
        return false;
    }

    public static void recoverEnergy(Player player, double amount) {
        setCurrentEnergy(player, getCurrentEnergy(player) + amount);
    }

    public static void recoverEnergyByDamage(Player player, float damage) {
        recoverEnergy(player, damage * 0.25);
    }

    public static void updateEnergyRegen(Player player) {
        if (player == null || player.isCreative()) return;

        if (player.getMainHandItem().getItem() instanceof Heiseisword) {
            recoverEnergy(player, ENERGY_REGEN_RATE);
        }
    }

    public static void resetEnergy(Player player) {
        setCurrentEnergy(player, getMaxEnergy(player));
    }

    // ==================== 同步方法 ====================

    // 第 169 行附近，将 NetworkHandler.sendToClient 改为正确调用
    private static void syncToClient(Player player) {
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToClient(
                    new HeiseiswordEnergySyncPacket(player.getUUID(), getCurrentEnergy(player), getMaxEnergy(player)),
                    serverPlayer
            );
        }
    }

    // ==================== 客户端更新方法 ====================

    public static void updateClientEnergy(UUID playerUUID, double currentEnergy, double maxEnergy) {
        CLIENT_ENERGY_DATA.put(playerUUID, new EnergyData(currentEnergy, maxEnergy));
    }
}