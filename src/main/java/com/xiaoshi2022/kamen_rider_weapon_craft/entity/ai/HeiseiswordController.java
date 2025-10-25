package com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.goal.HeiseiswordGoal;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

/**
 * Heiseisword控制器 - 负责为实体添加Heiseisword相关的AI行为
 * 支持所有生物类型使用Heiseisword武器
 */
public class HeiseiswordController {

    // 检查实体是否已经有HeiseiswordGoal
    private static boolean hasHeiseiswordGoal(Mob mob) {
        if (mob == null || mob.goalSelector == null) {
            return false;
        }
        
        GoalSelector goalSelector = mob.goalSelector;
        Set<WrappedGoal> goals = goalSelector.getAvailableGoals();
        
        for (WrappedGoal goal : goals) {
            if (goal.getGoal() instanceof HeiseiswordGoal) {
                return true;
            }
        }
        
        return false;
    }

    // 为实体添加HeiseiswordGoal
    public static void addHeiseiswordGoal(LivingEntity entity) {
        // 检查实体是否为null或是否为Mob类
        if (entity == null || !(entity instanceof Mob mob)) {
            return;
        }
        
        // 检查是否已经有HeiseiswordGoal，避免重复添加
        if (hasHeiseiswordGoal(mob)) {
            return;
        }
        
        // 创建Heiseisword物品（如果实体没有）
        ItemStack mainHand = entity.getMainHandItem();
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof Heiseisword)) {
            mainHand = new ItemStack(ModItems.HEISEISWORD.get());
            entity.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, mainHand);
            
            // 为非玩家实体设置默认骑士，确保能立即使用特效
            if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
                // 随机选择一个骑士作为默认值
                List<String> riders = HeiseiRiderEffectManager.getRiderOrder();
                if (!riders.isEmpty()) {
                    String defaultRider = riders.get(entity.level().random.nextInt(riders.size()));
                    Heiseisword.setSelectedRiderStatic(mainHand, defaultRider);
                }
            }
        } else {
            // 如果已有Heiseisword，但没有选择骑士，也设置一个默认值
            String selectedRider = Heiseisword.getSelectedRiderStatic(mainHand);
            if (selectedRider == null || selectedRider.isEmpty()) {
                List<String> riders = HeiseiRiderEffectManager.getRiderOrder();
                if (!riders.isEmpty()) {
                    String defaultRider = riders.get(entity.level().random.nextInt(riders.size()));
                    Heiseisword.setSelectedRiderStatic(mainHand, defaultRider);
                }
            }
        }
        
        // 添加HeiseiswordGoal到AI目标选择器
        mob.goalSelector.addGoal(3, new HeiseiswordGoal(entity));
    }

    // 移除实体的HeiseiswordGoal
    public static void removeHeiseiswordGoal(LivingEntity entity) {
        // 检查实体是否为null或是否为Mob类
        if (entity == null || !(entity instanceof Mob mob)) {
            return;
        }
        
        GoalSelector goalSelector = mob.goalSelector;
        Set<WrappedGoal> goals = goalSelector.getAvailableGoals();
        
        goals.removeIf(goal -> goal.getGoal() instanceof HeiseiswordGoal);
    }
}