package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.breakamnuster;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * BreakamnusterGun 联动接口示例类
 * 展示如何使用 BreakamnusterGun 提供的公开 API 进行模组联动
 * 
 * 其他模组（如假面骑士BOSS模组）可以参考此示例来扩展武器功能
 */
public class BreakamnusterGunAPIExample {
    
    /**
     * 示例1：注册盔甲套装伤害加成
     * 当玩家穿戴特定盔甲时，增加武器伤害
     */
    public static void registerArmorSetBonus() {
        // 注册一个伤害修改器，当玩家穿戴全套特定盔甲时，伤害翻倍
        BreakamnusterGun.registerDamageModifier(
            new BreakamnusterGun.DamageModifier(
                "kamen_rider_armor_bonus", // 修改器名称
                (player) -> hasFullKamenRiderArmor(player), // 触发条件
                2.0f // 伤害倍率
            )
        );
    }
    
    /**
     * 示例2：注册特殊目标效果
     * 当击中特定实体时，应用额外效果
     */
    public static void registerSpecialTargetEffect() {
        // 注册一个击中效果，当击中敌方玩家时，移除其一个随机物品
        BreakamnusterGun.registerHitEffect(
            new BreakamnusterGun.HitEffect(
                "kamen_rider_steal_item", // 效果名称
                (target) -> target instanceof Player && !target.isAlliedTo(target), // 仅对敌方玩家生效
                (target, chargeRatio) -> {
                    if (target instanceof Player player) {
                        // 实现移除随机物品的逻辑
                        removeRandomItem(player);
                    }
                }
            )
        );
    }
    
    /**
     * 示例3：调整基础伤害
     */
    public static void adjustBaseDamage() {
        // 提高基础伤害到15.0f
        BreakamnusterGun.setBaseDamage(15.0f);
    }
    
    /**
     * 检查玩家是否穿戴全套假面骑士盔甲
     * @param player 玩家实体
     * @return 是否穿戴全套特定盔甲
     */
    private static boolean hasFullKamenRiderArmor(Player player) {
        // 检查玩家的4个盔甲槽
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) {
                return false;
            }
            
            // 检查盔甲的材质或标签，这里假设盔甲有特定的物品标签
            // 实际使用时，需要根据目标模组的盔甲物品ID或标签进行调整
            if (!stack.getTags().anyMatch(tag -> tag.location().toString().contains("kamen_rider_armor"))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 移除玩家的一个随机物品
     * @param player 玩家实体
     */
    private static void removeRandomItem(Player player) {
        // 遍历玩家的物品栏，找到一个非空物品并移除它
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty()) {
                stack.shrink(1);
                break;
            }
        }
    }
    
    /**
     * 示例：初始化所有联动效果
     * 其他模组可以在适当的时候调用此方法来注册自己的联动效果
     */
    public static void initializeKamenRiderBossModIntegration() {
        // 注册盔甲套装加成
        registerArmorSetBonus();
        
        // 注册特殊目标效果
        registerSpecialTargetEffect();
        
        // 调整基础伤害
        adjustBaseDamage();
        
        System.out.println("BreakamnusterGun 联动接口初始化完成！");
    }
}