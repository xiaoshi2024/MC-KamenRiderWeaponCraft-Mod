package com.xiaoshi2022.kamenriderweaponcraft.entity.ai;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.entity.ai.goal.HeiseiswordGoal;
import com.xiaoshi2022.kamenriderweaponcraft.register.ItemRegister;
import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.HeiseiRiderEffectManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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

    public static void addHeiseiswordGoal(LivingEntity entity) {
        if (entity == null || !(entity instanceof Mob mob)) {
            return;
        }
        
        if (hasHeiseiswordGoal(mob)) {
            return;
        }
        
        ItemStack mainHand = entity.getMainHandItem();
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof Heiseisword)) {
            mainHand = new ItemStack(ItemRegister.HEISEISWORD.get());
            entity.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, mainHand);
            
            if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
                List<String> riders = HeiseiRiderEffectManager.getRiderOrder();
                if (!riders.isEmpty()) {
                    String defaultRider = riders.get(entity.level().random.nextInt(riders.size()));
                    Heiseisword.setSelectedRiderStatic(mainHand, defaultRider);
                }
            }
        } else {
            String selectedRider = Heiseisword.getSelectedRiderStatic(mainHand);
            if (selectedRider == null || selectedRider.isEmpty()) {
                List<String> riders = HeiseiRiderEffectManager.getRiderOrder();
                if (!riders.isEmpty()) {
                    String defaultRider = riders.get(entity.level().random.nextInt(riders.size()));
                    Heiseisword.setSelectedRiderStatic(mainHand, defaultRider);
                }
            }
        }
        
        mob.goalSelector.addGoal(3, new HeiseiswordGoal(entity));
    }

    public static void removeHeiseiswordGoal(LivingEntity entity) {
        if (entity == null || !(entity instanceof Mob mob)) {
            return;
        }
        
        GoalSelector goalSelector = mob.goalSelector;
        Set<WrappedGoal> goals = goalSelector.getAvailableGoals();
        
        goals.removeIf(goal -> goal.getGoal() instanceof HeiseiswordGoal);
    }
}