package com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.zombie;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;

public class ZombieSonicArrowController {
    
    // 将ZombieSonicArrowGoal添加到僵尸的目标选择器中
    public static void addSonicArrowGoal(Zombie zombie) {
        GoalSelector goalSelector = zombie.goalSelector;
        
        // 创建我们的AI目标
        ZombieSonicArrowGoal sonicArrowGoal = new ZombieSonicArrowGoal(zombie);
        
        // 添加到优先级列表中，优先级设为2（高于普通攻击，但低于追踪目标）
        goalSelector.addGoal(2, sonicArrowGoal);
    }
    
    // 检查僵尸是否已经有SonicArrowGoal
    public static boolean hasSonicArrowGoal(Zombie zombie) {
        GoalSelector goalSelector = zombie.goalSelector;
        // 处理Set<WrappedGoal>
        return goalSelector.getAvailableGoals().stream()
                .map(WrappedGoal::getGoal)
                .anyMatch(goal -> goal instanceof ZombieSonicArrowGoal);
    }
    
    // 移除僵尸的SonicArrowGoal（如果存在）
    public static void removeSonicArrowGoal(Zombie zombie) {
        GoalSelector goalSelector = zombie.goalSelector;
        
        // 找到并移除ZombieSonicArrowGoal
        goalSelector.getAvailableGoals().stream()
                .filter(wrappedGoal -> wrappedGoal.getGoal() instanceof ZombieSonicArrowGoal)
                .findFirst()
                .ifPresent(wrappedGoal -> goalSelector.removeGoal(wrappedGoal.getGoal()));
    }
    
    // 重新添加SonicArrowGoal（用于刷新）
    public static void refreshSonicArrowGoal(Zombie zombie) {
        removeSonicArrowGoal(zombie);
        addSonicArrowGoal(zombie);
    }
    
    // 检查僵尸是否手持音速弓并更新AI
    public static void updateSonicArrowGoalForZombie(Zombie zombie) {
        ItemStack mainHandItem = zombie.getMainHandItem();
        boolean isHoldingSonicArrow = mainHandItem.getItem() instanceof sonicarrow;
        boolean hasGoal = hasSonicArrowGoal(zombie);
        
        if (isHoldingSonicArrow && !hasGoal) {
            // 手持音速弓但没有对应AI，添加AI
            addSonicArrowGoal(zombie);
        } else if (!isHoldingSonicArrow && hasGoal) {
            // 不再手持音速弓但有对应AI，移除AI
            removeSonicArrowGoal(zombie);
        }
    }
}