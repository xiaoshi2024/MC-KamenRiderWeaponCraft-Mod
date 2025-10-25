package com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.zombie;

import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ZombieHeiseiswordController {
    
    // 将ZombieHeiseiswordGoal添加到僵尸的目标选择器中
    public static void addHeiseiswordGoal(Zombie zombie) {
        GoalSelector goalSelector = zombie.goalSelector;
        
        // 创建我们的AI目标
        ZombieHeiseiswordGoal heiseiswordGoal = new ZombieHeiseiswordGoal(zombie);
        
        // 添加到优先级列表中，优先级设为2（高于普通攻击，但低于追踪目标）
        goalSelector.addGoal(2, heiseiswordGoal);
    }
    
    // 检查僵尸是否已经有HeiseiswordGoal
    public static boolean hasHeiseiswordGoal(Zombie zombie) {
        GoalSelector goalSelector = zombie.goalSelector;
        // 不再强制转换为List，直接处理Set
        Set<WrappedGoal> goals = goalSelector.getAvailableGoals();
        
        // 检查是否已有ZombieHeiseiswordGoal
        return goals.stream()
                .map(WrappedGoal::getGoal)
                .anyMatch(goal -> goal instanceof ZombieHeiseiswordGoal);
    }
    
    // 移除僵尸的HeiseiswordGoal（如果存在）
    public static void removeHeiseiswordGoal(Zombie zombie) {
        GoalSelector goalSelector = zombie.goalSelector;
        // 不再强制转换为List，直接处理Set
        Set<WrappedGoal> goals = goalSelector.getAvailableGoals();
        
        // 找到并移除ZombieHeiseiswordGoal
        goals.stream()
                .filter(wrappedGoal -> wrappedGoal.getGoal() instanceof ZombieHeiseiswordGoal)
                .findFirst()
                .ifPresent(wrappedGoal -> goalSelector.removeGoal(wrappedGoal.getGoal()));
    }
    
    // 重新添加HeiseiswordGoal（用于刷新）
    public static void refreshHeiseiswordGoal(Zombie zombie) {
        removeHeiseiswordGoal(zombie);
        addHeiseiswordGoal(zombie);
    }
}