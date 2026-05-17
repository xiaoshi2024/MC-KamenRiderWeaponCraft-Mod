package com.xiaoshi2022.kamenriderweaponcraft.rider.energy;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.network.HeiseiswordEnergySyncPacket;
import com.xiaoshi2022.kamenriderweaponcraft.network.NetworkHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeiseiswordEnergyManager {
    private static final String TAG_CURRENT_ENERGY = "kamenriderweaponcraft_current_energy";
    private static final String TAG_MAX_ENERGY = "kamenriderweaponcraft_max_energy";

    private static final double DEFAULT_MAX_ENERGY = 100.0;
    private static final double ENERGY_REGEN_RATE = 2.0;

    // 客户端专用能量缓存（用于显示）
    public static final Map<UUID, EnergyData> CLIENT_ENERGY_DATA = new HashMap<>();

    public static class EnergyData {
        public double currentEnergy;
        public double maxEnergy;

        public EnergyData(double currentEnergy, double maxEnergy) {
            this.currentEnergy = currentEnergy;
            this.maxEnergy = maxEnergy;
        }
    }

    // ==================== 物品栈级别的能量方法 ====================

    private static CompoundTag getOrCreateItemTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag();
    }

    private static void setItemTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static double getCurrentEnergy(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Heiseisword)) {
            return DEFAULT_MAX_ENERGY;
        }

        CompoundTag tag = getOrCreateItemTag(stack);
        if (!tag.contains(TAG_CURRENT_ENERGY)) {
            initItemEnergy(stack);
            return DEFAULT_MAX_ENERGY;
        }
        return tag.getDouble(TAG_CURRENT_ENERGY);
    }

    public static double getMaxEnergy(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Heiseisword)) {
            return DEFAULT_MAX_ENERGY;
        }

        CompoundTag tag = getOrCreateItemTag(stack);
        if (!tag.contains(TAG_MAX_ENERGY)) {
            initItemEnergy(stack);
            return DEFAULT_MAX_ENERGY;
        }
        return tag.getDouble(TAG_MAX_ENERGY);
    }

    private static void initItemEnergy(ItemStack stack) {
        CompoundTag tag = getOrCreateItemTag(stack);
        tag.putDouble(TAG_CURRENT_ENERGY, DEFAULT_MAX_ENERGY);
        tag.putDouble(TAG_MAX_ENERGY, DEFAULT_MAX_ENERGY);
        setItemTag(stack, tag);
    }

    public static void setCurrentEnergy(ItemStack stack, double energy) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Heiseisword)) {
            return;
        }

        double maxEnergy = getMaxEnergy(stack);
        energy = Math.max(0, Math.min(energy, maxEnergy));

        CompoundTag tag = getOrCreateItemTag(stack);
        tag.putDouble(TAG_CURRENT_ENERGY, energy);
        setItemTag(stack, tag);
    }

    public static void setMaxEnergy(ItemStack stack, double maxEnergy) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Heiseisword)) {
            return;
        }

        maxEnergy = Math.max(1.0, maxEnergy);
        double currentEnergy = Math.min(getCurrentEnergy(stack), maxEnergy);

        CompoundTag tag = getOrCreateItemTag(stack);
        tag.putDouble(TAG_CURRENT_ENERGY, currentEnergy);
        tag.putDouble(TAG_MAX_ENERGY, maxEnergy);
        setItemTag(stack, tag);
    }

    public static boolean consumeEnergy(ItemStack stack, double amount) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Heiseisword)) {
            return false;
        }

        double current = getCurrentEnergy(stack);
        if (current >= amount) {
            setCurrentEnergy(stack, current - amount);
            return true;
        }
        return false;
    }

    public static void recoverEnergy(ItemStack stack, double amount) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Heiseisword)) {
            return;
        }
        setCurrentEnergy(stack, getCurrentEnergy(stack) + amount);
    }

    public static void recoverEnergyByDamage(ItemStack stack, float damage) {
        recoverEnergy(stack, damage * 0.25);
    }

    // ==================== 兼容旧API的方法（使用Player） ====================

    public static double getCurrentEnergy(Player player) {
        if (player == null) return DEFAULT_MAX_ENERGY;

        if (player.level().isClientSide) {
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null) return data.currentEnergy;
            return DEFAULT_MAX_ENERGY;
        }

        ItemStack heiseisword = Heiseisword.getHeiseiswordFromPlayer(player);
        if (!heiseisword.isEmpty()) {
            return getCurrentEnergy(heiseisword);
        }

        return DEFAULT_MAX_ENERGY;
    }

    public static double getMaxEnergy(Player player) {
        if (player == null) return DEFAULT_MAX_ENERGY;

        if (player.level().isClientSide) {
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null) return data.maxEnergy;
            return DEFAULT_MAX_ENERGY;
        }

        ItemStack heiseisword = Heiseisword.getHeiseiswordFromPlayer(player);
        if (!heiseisword.isEmpty()) {
            return getMaxEnergy(heiseisword);
        }

        return DEFAULT_MAX_ENERGY;
    }

    public static void setCurrentEnergy(Player player, double energy) {
        if (player == null) return;

        if (player.level().isClientSide) {
            double maxEnergy = getMaxEnergy(player);
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null) {
                data.currentEnergy = Math.max(0, Math.min(energy, maxEnergy));
            } else {
                CLIENT_ENERGY_DATA.put(player.getUUID(), new EnergyData(Math.max(0, Math.min(energy, maxEnergy)), maxEnergy));
            }
            return;
        }

        ItemStack heiseisword = Heiseisword.getHeiseiswordFromPlayer(player);
        if (!heiseisword.isEmpty()) {
            setCurrentEnergy(heiseisword, energy);
            syncToClient(player, heiseisword);
        }
    }

    public static void setMaxEnergy(Player player, double maxEnergy) {
        if (player == null) return;

        if (player.level().isClientSide) {
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null) {
                data.maxEnergy = Math.max(1.0, maxEnergy);
                data.currentEnergy = Math.min(data.currentEnergy, data.maxEnergy);
            } else {
                CLIENT_ENERGY_DATA.put(player.getUUID(), new EnergyData(Math.max(1.0, maxEnergy), Math.max(1.0, maxEnergy)));
            }
            return;
        }

        ItemStack heiseisword = Heiseisword.getHeiseiswordFromPlayer(player);
        if (!heiseisword.isEmpty()) {
            setMaxEnergy(heiseisword, maxEnergy);
            syncToClient(player, heiseisword);
        }
    }

    public static boolean consumeEnergy(Player player, double amount) {
        if (player == null) return false;

        if (player.level().isClientSide) {
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null && data.currentEnergy >= amount) {
                data.currentEnergy -= amount;
                return true;
            }
            return false;
        }

        ItemStack heiseisword = Heiseisword.getHeiseiswordFromPlayer(player);
        if (!heiseisword.isEmpty()) {
            boolean success = consumeEnergy(heiseisword, amount);
            if (success) {
                syncToClient(player, heiseisword);
            }
            return success;
        }
        return false;
    }

    public static void recoverEnergy(Player player, double amount) {
        if (player == null) return;

        if (player.level().isClientSide) {
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null) {
                data.currentEnergy = Math.min(data.currentEnergy + amount, data.maxEnergy);
            }
            return;
        }

        ItemStack heiseisword = Heiseisword.getHeiseiswordFromPlayer(player);
        if (!heiseisword.isEmpty()) {
            recoverEnergy(heiseisword, amount);
            syncToClient(player, heiseisword);
        }
    }

    public static void recoverEnergyByDamage(Player player, float damage) {
        if (player == null) return;

        if (player.level().isClientSide) {
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null) {
                data.currentEnergy = Math.min(data.currentEnergy + damage * 0.25, data.maxEnergy);
            }
            return;
        }

        ItemStack heiseisword = Heiseisword.getHeiseiswordFromPlayer(player);
        if (!heiseisword.isEmpty()) {
            recoverEnergyByDamage(heiseisword, damage);
            syncToClient(player, heiseisword);
        }
    }

    /**
     * 更新能量恢复 - 扫描玩家背包中的所有平成剑并恢复能量
     */
    public static void updateEnergyRegen(Player player) {
        if (player == null || player.isCreative()) return;

        if (player.level().isClientSide) {
            return;
        }

        // 扫描玩家背包中的所有平成剑并恢复能量
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof Heiseisword) {
                double current = getCurrentEnergy(stack);
                double max = getMaxEnergy(stack);

                if (current < max) {
                    recoverEnergy(stack, ENERGY_REGEN_RATE);
                }
            }
        }

        // 更新手持平成剑的客户端显示
        ItemStack heiseisword = Heiseisword.getHeiseiswordFromPlayer(player);
        if (!heiseisword.isEmpty()) {
            syncToClient(player, heiseisword);
        }
    }

    public static void resetEnergy(Player player) {
        if (player == null) return;

        if (player.level().isClientSide) {
            EnergyData data = CLIENT_ENERGY_DATA.get(player.getUUID());
            if (data != null) {
                data.currentEnergy = data.maxEnergy;
            }
            return;
        }

        ItemStack heiseisword = Heiseisword.getHeiseiswordFromPlayer(player);
        if (!heiseisword.isEmpty()) {
            setCurrentEnergy(heiseisword, getMaxEnergy(heiseisword));
            syncToClient(player, heiseisword);
        }
    }

    // ==================== 同步方法 ====================

    private static void syncToClient(Player player, ItemStack heiseisword) {
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToClient(
                    new HeiseiswordEnergySyncPacket(player.getUUID(), getCurrentEnergy(heiseisword), getMaxEnergy(heiseisword)),
                    serverPlayer
            );
        }
    }

    // ==================== 客户端更新方法 ====================

    public static void updateClientEnergy(UUID playerUUID, double currentEnergy, double maxEnergy) {
        CLIENT_ENERGY_DATA.put(playerUUID, new EnergyData(currentEnergy, maxEnergy));
    }
}