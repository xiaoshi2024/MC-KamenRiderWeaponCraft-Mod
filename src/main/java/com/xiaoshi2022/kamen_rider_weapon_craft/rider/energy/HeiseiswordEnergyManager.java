package com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.HeiseiswordEnergySyncPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.NetworkHandler;

/**
 * 平成嘿嘿剑自定义能量管理器
 * 用于替代外部boss模组的蓝条系统，避免与其他模组冲突
 */
public class HeiseiswordEnergyManager {
    // NBT标签常量
    private static final String TAG_CURRENT_ENERGY = "heiseisword_current_energy";
    private static final String TAG_MAX_ENERGY = "heiseisword_max_energy";
    
    // 默认最大能量值
    private static final double DEFAULT_MAX_ENERGY = 100.0;
    
    // 能量恢复速率（每秒恢复的能量）- 降低恢复速率以提高平衡性
    private static final double ENERGY_REGEN_RATE = 2.0;
    
    /**
     * 获取玩家的当前能量值
     */
    public static double getCurrentEnergy(Player player) {
        if (player == null || player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("heiseisword_energy").isEmpty()) {
            // 初始化能量
            setCurrentEnergy(player, DEFAULT_MAX_ENERGY);
            return DEFAULT_MAX_ENERGY;
        }
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("heiseisword_energy").getDouble(TAG_CURRENT_ENERGY);
    }
    
    /**
     * 获取玩家的最大能量值
     */
    public static double getMaxEnergy(Player player) {
        if (player == null || player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("heiseisword_energy").isEmpty()) {
            return DEFAULT_MAX_ENERGY;
        }
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("heiseisword_energy").getDouble(TAG_MAX_ENERGY);
    }
    
    /**
     * 设置玩家的当前能量值
     */
    public static void setCurrentEnergy(Player player, double energy) {
        if (player == null) return;
        
        double maxEnergy = getMaxEnergy(player);
        // 确保能量值在合理范围内
        energy = Math.max(0.0, Math.min(energy, maxEnergy));
        
        CompoundTag persistedData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag energyData = persistedData.getCompound("heiseisword_energy");
        energyData.putDouble(TAG_CURRENT_ENERGY, energy);
        energyData.putDouble(TAG_MAX_ENERGY, maxEnergy); // 确保最大能量也被保存
        persistedData.put("heiseisword_energy", energyData);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistedData);
        
        // 发送能量同步包到客户端
        syncEnergyToClient(player);
    }
    
    /**
     * 设置玩家的最大能量值
     */
    public static void setMaxEnergy(Player player, double maxEnergy) {
        if (player == null) return;
        
        maxEnergy = Math.max(1.0, maxEnergy); // 确保最大能量至少为1
        
        CompoundTag persistedData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag energyData = persistedData.getCompound("heiseisword_energy");
        double currentEnergy = Math.min(energyData.getDouble(TAG_CURRENT_ENERGY), maxEnergy); // 确保当前能量不超过新的最大能量
        energyData.putDouble(TAG_CURRENT_ENERGY, currentEnergy);
        energyData.putDouble(TAG_MAX_ENERGY, maxEnergy);
        persistedData.put("heiseisword_energy", energyData);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistedData);
        
        // 发送能量同步包到客户端
        syncEnergyToClient(player);
    }
    
    /**
     * 检查玩家是否有足够的能量使用
     */
    public static boolean canUseEnergy(Player player, double cost) {
        return getCurrentEnergy(player) >= cost;
    }
    
    /**
     * 消耗玩家的能量
     * @return 是否成功消耗能量
     */
    public static boolean consumeEnergy(Player player, double cost) {
        if (player == null) {
            return false;
        }
        
        // 检查能量是否足够
        if (getCurrentEnergy(player) >= cost) {
            // 在服务器端实际扣除能量并保存
            if (!player.level().isClientSide && player instanceof ServerPlayer) {
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
    public static void recoverEnergy(Player player, double amount) {
        if (player == null) return;
        
        // 在服务器端实际恢复能量
        if (!player.level().isClientSide && player instanceof ServerPlayer) {
            setCurrentEnergy(player, getCurrentEnergy(player) + amount);
        }
    }
    
    /**
     * 发送能量同步包到客户端
     * 当服务器端的能量值变化时调用此方法
     */
    private static void syncEnergyToClient(Player player) {
        if (player != null && !player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            double currentEnergy = getCurrentEnergy(player);
            double maxEnergy = getMaxEnergy(player);
            // 发送同步包到客户端，添加NetworkDirection参数
            NetworkHandler.INSTANCE.sendTo(new HeiseiswordEnergySyncPacket(player, currentEnergy, maxEnergy), serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        }
    }
    
    /**
     * 根据攻击伤害恢复能量
     * @param player 玩家
     * @param damage 攻击伤害值
     */
    public static void recoverEnergyByDamage(Player player, float damage) {
        if (player == null) return;
        
        // 计算恢复的能量（降低恢复比例到伤害的25%以提高平衡性）
        double recoveryAmount = damage * 0.25;
        recoverEnergy(player, recoveryAmount);
    }
    
    /**
     * 更新玩家的能量恢复（每秒调用一次）
     */
    public static void updateEnergyRegen(Player player) {
        if (player == null || player.isCreative()) {
            return;
        }
        
        // 只有当玩家持有平成嘿嘿剑时才恢复能量
        if (player.getMainHandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword) {
            recoverEnergy(player, ENERGY_REGEN_RATE);
        }
    }
    
    /**
     * 重置玩家的能量到最大值
     */
    public static void resetEnergy(Player player) {
        setCurrentEnergy(player, getMaxEnergy(player));
    }
}