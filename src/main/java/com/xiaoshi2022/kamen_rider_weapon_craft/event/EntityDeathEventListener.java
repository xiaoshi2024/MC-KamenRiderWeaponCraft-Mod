package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.sound.RiderSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 实体死亡事件监听器，用于在玩家击败实体时播放特殊音效
 * 适配 Fabric 1.21+ API
 */
public class EntityDeathEventListener {
    // 用于跟踪最近对实体造成伤害的玩家
    private static final Map<UUID, UUID> RECENT_ATTACKERS = new HashMap<>();

    // 用于跟踪实体死亡时的伤害来源
    private static final Map<UUID, DamageSource> ENTITY_DEATH_SOURCES = new HashMap<>();

    /**
     * 注册实体死亡事件监听器
     */
    public static void register() {
        // 监听实体受到攻击的事件，记录攻击者
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof LivingEntity && !world.isClient()) {
                RECENT_ATTACKERS.put(entity.getUuid(), player.getUuid());
            }
            return ActionResult.PASS;
        });

        // 监听实体加载事件，设置死亡监听
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof LivingEntity livingEntity) {
                setupDeathListener(livingEntity);
            }
        });

        // 监听实体卸载事件，清理缓存
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            UUID entityUuid = entity.getUuid();
            RECENT_ATTACKERS.remove(entityUuid);
            ENTITY_DEATH_SOURCES.remove(entityUuid);
        });
    }

    /**
     * 设置实体死亡监听器
     */
    private static void setupDeathListener(LivingEntity entity) {
        // 在实际实现中，这里可以通过 Mixin 来监听实体死亡
        // 目前使用记录伤害来源的方式
    }

    /**
     * 记录实体受到的伤害来源（在实体受到伤害时调用）
     */
    public static void recordDamageSource(LivingEntity entity, DamageSource damageSource) {
        if (!entity.getWorld().isClient()) {
            ENTITY_DEATH_SOURCES.put(entity.getUuid(), damageSource);
        }
    }

    /**
     * 处理实体死亡
     * 这个方法应该通过 Mixin 在实体死亡时被调用
     */
    public static void onEntityDeath(LivingEntity entity, DamageSource damageSource) {
        World world = entity.getWorld();

        if (world.isClient()) {
            return;
        }

        UUID entityUuid = entity.getUuid();

        // 尝试从不同来源获取玩家
        PlayerEntity killingPlayer = null;

        // 1. 首先从伤害来源获取攻击者
        if (damageSource.getAttacker() instanceof PlayerEntity player) {
            killingPlayer = player;
        }
        // 2. 如果伤害来源没有玩家，从最近攻击者缓存中查找
        else if (RECENT_ATTACKERS.containsKey(entityUuid)) {
            UUID playerUuid = RECENT_ATTACKERS.get(entityUuid);
            if (world instanceof ServerWorld serverWorld) {
                PlayerEntity player = serverWorld.getPlayerByUuid(playerUuid);
                if (player != null) {
                    killingPlayer = player;
                }
            }
        }

        // 如果有击杀玩家，播放音效
        if (killingPlayer != null) {
            playKillSound(world, killingPlayer);
        }

        // 清理缓存
        RECENT_ATTACKERS.remove(entityUuid);
        ENTITY_DEATH_SOURCES.remove(entityUuid);
    }

    /**
     * 根据玩家的当前攻击模式播放相应的击败音效
     */
    public static void playKillSound(World world, PlayerEntity player) {
        // 安全检查
        if (world.isClient() || player == null) {
            return;
        }

        // 获取玩家主手物品
        ItemStack mainHandStack = player.getMainHandStack();

        // 检查是否是Heiseisword
        if (mainHandStack.getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.items.custom.Heiseisword) {
            try {
                // 从物品组件中获取当前模式
                boolean isUltimateMode = mainHandStack.getOrDefault(
                        com.xiaoshi2022.kamen_rider_weapon_craft.component.ridermodComponents.IS_ULTIMATE_MODE,
                        false
                );
                List<String> scrambleRiders = mainHandStack.getOrDefault(
                        com.xiaoshi2022.kamen_rider_weapon_craft.component.ridermodComponents.SCRAMBLE_RIDERS,
                        java.util.Collections.emptyList()
                );

                // 根据不同模式选择不同音效
                if (isUltimateMode) {
                    // 超必杀模式 - 播放ULTIMATE_TIME_BREAK
                    RiderSounds.playSound(world, player, RiderSounds.ULTIMATE_TIME_BREAK);
                } else if (!scrambleRiders.isEmpty()) {
                    // 混乱模式 - 播放SCRAMBLE_TIME_BREAK
                    RiderSounds.playSound(world, player, RiderSounds.SCRAMBLE_TIME_BREAK);
                } else {
                    // 普通模式 - 播放DUAL_TIME_BREAK
                    RiderSounds.playSound(world, player, RiderSounds.DUAL_TIME_BREAK);
                }

                // 记录日志（可选）
                System.out.println("播放击败音效 - 玩家: " + player.getName().getString() +
                        ", 终极模式: " + isUltimateMode +
                        ", 混乱骑手数量: " + scrambleRiders.size());

            } catch (Exception e) {
                System.err.println("播放击败音效时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
        // 非Heiseisword时，可以选择默认播放或不播放音效
    }

    /**
     * 播放指定的特殊音效
     */
    public static void playSpecialKillSound(World world, PlayerEntity player, SoundEvent soundEvent) {
        if (!world.isClient() && player != null && soundEvent != null) {
            RiderSounds.playSound(world, player, soundEvent);
        }
    }

    /**
     * 获取最近攻击者缓存的大小（用于调试）
     */
    public static int getRecentAttackersSize() {
        return RECENT_ATTACKERS.size();
    }

    /**
     * 清理所有缓存（用于重新加载时）
     */
    public static void clearAllCaches() {
        RECENT_ATTACKERS.clear();
        ENTITY_DEATH_SOURCES.clear();
    }
}