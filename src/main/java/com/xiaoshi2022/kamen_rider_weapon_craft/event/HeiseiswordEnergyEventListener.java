package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy.HeiseiswordEnergyManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

/**
 * 平成驾驭剑能量恢复事件监听器
 * 负责处理能量的被动恢复和攻击恢复机制
 */
public class HeiseiswordEnergyEventListener {
    private static int tickCounter = 0;
    private static final int REGEN_TICK_INTERVAL = 20; // 每秒恢复一次（20ticks）

    /**
     * 注册能量事件监听器
     */
    public static void register() {
        // 注册服务器tick事件监听器（用于被动能量恢复）
        ServerTickEvents.END_SERVER_TICK.register(HeiseiswordEnergyEventListener::onServerTick);

        // 注册攻击事件监听器（用于攻击时能量恢复）
        AttackEntityCallback.EVENT.register(HeiseiswordEnergyEventListener::onPlayerAttack);

        System.out.println("平成驾驭剑能量事件监听器已注册");
    }

    /**
     * 服务器tick事件处理方法
     * 每隔一秒为手持平成驾驭剑的玩家恢复能量
     */
    private static void onServerTick(MinecraftServer server) {
        tickCounter++;

        // 每秒执行一次能量恢复
        if (tickCounter % REGEN_TICK_INTERVAL == 0) {
            // 遍历所有在线玩家
            server.getPlayerManager().getPlayerList().forEach(player -> {
                try {
                    // 为每个玩家初始化能量数据（如果尚未初始化）
                    HeiseiswordEnergyManager.initializePlayerEnergy(player);

                    // 执行能量恢复
                    HeiseiswordEnergyManager.updateEnergyRegen(player);
                } catch (Exception e) {
                    // 捕获异常，确保不会影响服务器运行
                    System.err.println("能量恢复时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 玩家攻击事件处理方法
     * 当玩家使用平成驾驭剑攻击实体时，根据伤害值恢复能量
     */
    private static ActionResult onPlayerAttack(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        // 只在服务器端处理
        if (world.isClient || !(entity instanceof LivingEntity)) {
            return ActionResult.PASS;
        }

        // 检查是否手持平成驾驭剑
        if (player.getMainHandStack().getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.items.custom.Heiseisword) {
            // 立即处理能量恢复，而不是等待实体死亡
            try {
                // 获取玩家当前攻击的伤害值
                float damage = player.getAttackCooldownProgress(0.5F);

                // 如果攻击冷却完成，给予完整的能量恢复
                if (damage > 0.9F) {
                    // 根据基础伤害值恢复能量（可以根据需要调整系数）
                    float energyRecovery = damage * 5.0F; // 示例：伤害值的5倍作为能量恢复
                    HeiseiswordEnergyManager.recoverEnergy(player, energyRecovery);
                }
            } catch (Exception e) {
                System.err.println("攻击能量恢复时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return ActionResult.PASS;
    }

    /**
     * 替代方案：使用实体伤害事件来更精确地获取伤害值
     * 这个方法需要在其他地方注册（比如使用EntityDamageCallback）
     */
    public static void onEntityDamaged(Entity entity, DamageSource source, float amount) {
        if (entity instanceof LivingEntity && source.getAttacker() instanceof PlayerEntity player) {
            // 检查攻击者是否手持平成驾驭剑
            if (player.getMainHandStack().getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.items.custom.Heiseisword) {
                try {
                    // 根据实际造成的伤害恢复能量
                    float energyRecovery = amount * 2.0F; // 示例：伤害值的2倍作为能量恢复
                    HeiseiswordEnergyManager.recoverEnergy(player, energyRecovery);
                } catch (Exception e) {
                    System.err.println("伤害能量恢复时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 手动触发能量恢复（用于调试或特殊情况）
     */
    public static void forceEnergyRecovery(PlayerEntity player, double amount) {
        HeiseiswordEnergyManager.recoverEnergy(player, amount);
    }
}