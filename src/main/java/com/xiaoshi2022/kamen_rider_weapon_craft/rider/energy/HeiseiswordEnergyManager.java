package com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 平成嘿嘿剑自定义能量管理器
 * 用于替代外部boss模组的蓝条系统，避免与其他模组冲突
 * Fabric 1.21.6版本
 */
public class HeiseiswordEnergyManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeiseiswordEnergyManager.class);
    
    // 默认最大能量值
    private static final double DEFAULT_MAX_ENERGY = 100.0;

    // 能量恢复速率（每秒恢复的能量）- 降低恢复速率以提高平衡性
    private static final double ENERGY_REGEN_RATE = 2.0;
    
    // 临时存储玩家能量数据的Map，用于兼容不同版本
    private static final Map<UUID, NbtCompound> playerEnergyCache = new HashMap<>();

    /**
     * 获取玩家的能量数据，如果不存在则初始化
     */
    private static NbtCompound getOrCreateEnergyData(PlayerEntity player) {
        if (player == null) return new NbtCompound();
        
        UUID playerId = player.getUuid();
        
        // 优先使用本地缓存存储数据
        return playerEnergyCache.computeIfAbsent(playerId, id -> {
            NbtCompound energyData = new NbtCompound();
            energyData.putDouble("current_energy", DEFAULT_MAX_ENERGY);
            energyData.putDouble("max_energy", DEFAULT_MAX_ENERGY);
            return energyData;
        });
    }

    /**
     * 获取玩家的当前能量值
     */
    public static double getCurrentEnergy(PlayerEntity player) {
        if (player == null) return 0.0;
        
        NbtCompound energyData = getOrCreateEnergyData(player);
        // 使用orElse处理Optional<Double>返回值
        return energyData.contains("current_energy") ? energyData.getDouble("current_energy").orElse(0.0) : 0.0;
    }

    /**
     * 获取玩家的最大能量值
     */
    public static double getMaxEnergy(PlayerEntity player) {
        if (player == null) return DEFAULT_MAX_ENERGY;
        
        NbtCompound energyData = getOrCreateEnergyData(player);
        // 使用orElse处理Optional<Double>返回值
        return energyData.contains("max_energy") ? energyData.getDouble("max_energy").orElse(DEFAULT_MAX_ENERGY) : DEFAULT_MAX_ENERGY;
    }

    /**
     * 设置玩家的当前能量值
     */
    public static void setCurrentEnergy(PlayerEntity player, double energy) {
        if (player == null) return;
        
        double maxEnergy = getMaxEnergy(player);
        // 确保能量值在合理范围内
        energy = Math.max(0.0, Math.min(energy, maxEnergy));
        
        UUID playerId = player.getUuid();
        NbtCompound energyData = getOrCreateEnergyData(player);
        energyData.putDouble("current_energy", energy);
        energyData.putDouble("max_energy", maxEnergy);
        
        // 存储到缓存
        playerEnergyCache.put(playerId, energyData);
        
        // 发送能量同步包到客户端
        syncEnergyToClient(player);
    }

    /**
     * 设置玩家的最大能量值
     */
    public static void setMaxEnergy(PlayerEntity player, double maxEnergy) {
        if (player == null) return;
        
        maxEnergy = Math.max(1.0, maxEnergy); // 确保最大能量至少为1
        
        UUID playerId = player.getUuid();
        NbtCompound energyData = getOrCreateEnergyData(player);
        double currentEnergy = Math.min(getCurrentEnergy(player), maxEnergy);
        energyData.putDouble("current_energy", currentEnergy);
        energyData.putDouble("max_energy", maxEnergy);
        
        // 存储到缓存
        playerEnergyCache.put(playerId, energyData);
        
        // 发送能量同步包到客户端
        syncEnergyToClient(player);
    }

    /**
     * 检查玩家是否有足够的能量使用
     */
    public static boolean canUseEnergy(PlayerEntity player, double cost) {
        return getCurrentEnergy(player) >= cost;
    }

    /**
     * 消耗玩家的能量
     * @return 是否成功消耗能量
     */
    public static boolean consumeEnergy(PlayerEntity player, double cost) {
        if (player == null) {
            return false;
        }
        
        // 检查能量是否足够
        if (getCurrentEnergy(player) >= cost) {
            // 在服务器端实际扣除能量并保存
            if (!player.getWorld().isClient && player instanceof ServerPlayerEntity) {
                setCurrentEnergy(player, getCurrentEnergy(player) - cost);
            }
            // 注意：在客户端，我们仍然返回true，因为我们希望客户端能够显示能量消耗的视觉效果
            // 即使客户端不保存能量值，也需要返回true以允许攻击效果在视觉上显示
            return true;
        }
        return false;
    }

    /**
     * 恢复玩家的能量
     */
    public static void recoverEnergy(PlayerEntity player, double amount) {
        if (player == null) return;
        
        // 在服务器端实际恢复能量
        if (!player.getWorld().isClient && player instanceof ServerPlayerEntity) {
            setCurrentEnergy(player, getCurrentEnergy(player) + amount);
        }
    }

    /**
     * 发送能量同步包到客户端
     * 当服务器端的能量值变化时调用此方法
     */
    private static void syncEnergyToClient(PlayerEntity player) {
        // 使用本地缓存替代组件系统
        if (player != null && !player.getWorld().isClient && player instanceof ServerPlayerEntity) {
            // 在实际使用中，这里应该发送自定义数据包到客户端
            // 为了简化，我们暂时只使用本地缓存
        }
    }

    /**
     * 根据攻击伤害恢复能量
     * @param player 玩家
     * @param damage 攻击伤害值
     */
    public static void recoverEnergyByDamage(PlayerEntity player, float damage) {
        if (player == null) return;
        
        // 计算恢复的能量（降低恢复比例到伤害的25%以提高平衡性）
        double recoveryAmount = damage * 0.25;
        recoverEnergy(player, recoveryAmount);
    }

    /**
     * 更新玩家的能量恢复（每秒调用一次）
     */
    public static void updateEnergyRegen(PlayerEntity player) {
        if (player == null || player.isCreative()) {
            return;
        }
        
        // 只有当玩家持有平成嘿嘿剑时才恢复能量
        if (player.getMainHandStack().getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.items.custom.Heiseisword) {
            recoverEnergy(player, ENERGY_REGEN_RATE);
        }
    }

    /**
     * 重置玩家的能量到最大值
     */
    public static void resetEnergy(PlayerEntity player) {
        setCurrentEnergy(player, getMaxEnergy(player));
    }

    /**
     * 初始化玩家的能量数据
     */
    public static void initializePlayerEnergy(PlayerEntity player) {
        if (player == null) return;
        
        try {
            UUID playerId = player.getUuid();
            
            // 检查缓存中是否已有数据
            if (!playerEnergyCache.containsKey(playerId)) {
                // 创建新的能量数据
                NbtCompound energyData = new NbtCompound();
                energyData.putDouble("current_energy", DEFAULT_MAX_ENERGY);
                energyData.putDouble("max_energy", DEFAULT_MAX_ENERGY);
                
                // 存储到缓存
                playerEnergyCache.put(playerId, energyData);
            }
        } catch (Exception e) {
            // 捕获任何异常，确保游戏不会崩溃
            LOGGER.error("Failed to initialize player energy data: {}", e.getMessage());
        }
    }
    
    /**
     * 清除玩家能量数据缓存（当玩家离开时调用）
     */
    public static void clearPlayerData(PlayerEntity player) {
        if (player != null) {
            playerEnergyCache.remove(player.getUuid());
        }
    }
}