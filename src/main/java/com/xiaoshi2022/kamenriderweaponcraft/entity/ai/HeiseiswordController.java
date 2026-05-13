package com.xiaoshi2022.kamenriderweaponcraft.entity.ai;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.entity.ai.goal.HeiseiswordGoal;
import com.xiaoshi2022.kamenriderweaponcraft.register.ItemRegister;
import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.HeiseiRiderEffectManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Heiseisword控制器 - 负责为实体添加Heiseisword相关的AI行为
 * 支持所有生物类型使用Heiseisword武器
 */
public class HeiseiswordController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KamenRiderWeaponCraft.MODID + "/HeiseiswordController");

    // 用于标识生命值修改的ResourceLocation
    private static final ResourceLocation HEISEISWORD_HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "heiseisword_health_boost");

    // 目标血量
    private static final double TARGET_HEALTH = 300.0;

    // 调试开关
    private static final boolean DEBUG = false;

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

    /**
     * 为实体增加生命值
     */
    private static void boostHealth(LivingEntity entity) {
        if (entity == null) return;

        AttributeInstance healthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        // 检查是否已经应用过生命值加成
        if (healthAttribute.getModifier(HEISEISWORD_HEALTH_MODIFIER_ID) != null) {
            if (DEBUG) {
                LOGGER.debug("[{}] 已经应用过生命值加成，跳过", entity.getName().getString());
            }
            return;
        }

        // 获取当前最大生命值
        double currentMaxHealth = healthAttribute.getBaseValue();

        // 如果当前生命值已经是目标值或更高，不需要修改
        if (currentMaxHealth >= TARGET_HEALTH) {
            if (DEBUG) {
                LOGGER.debug("[{}] 当前生命值({})已达到或超过目标值({})，跳过",
                        entity.getName().getString(), currentMaxHealth, TARGET_HEALTH);
            }
            return;
        }

        // 计算需要增加的血量
        double healthIncrease = TARGET_HEALTH - currentMaxHealth;

        // 添加永久属性修饰符
        AttributeModifier modifier = new AttributeModifier(
                HEISEISWORD_HEALTH_MODIFIER_ID,
                healthIncrease,
                AttributeModifier.Operation.ADD_VALUE
        );

        healthAttribute.addPermanentModifier(modifier);

        // 同时恢复当前生命值到满血
        entity.setHealth(entity.getMaxHealth());

        if (DEBUG) {
            LOGGER.debug("[{}] 生命值已提升: {} -> {}",
                    entity.getName().getString(), currentMaxHealth, entity.getMaxHealth());
        }
    }

    /**
     * 移除实体的生命值加成（当不再持有平成剑时）
     */
    private static void removeHealthBoost(LivingEntity entity) {
        if (entity == null) return;

        AttributeInstance healthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        AttributeModifier modifier = healthAttribute.getModifier(HEISEISWORD_HEALTH_MODIFIER_ID);
        if (modifier != null) {
            healthAttribute.removeModifier(modifier);

            // 确保当前生命值不超过新的最大生命值
            if (entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }

            if (DEBUG) {
                LOGGER.debug("[{}] 生命值加成已移除，当前最大生命值: {}",
                        entity.getName().getString(), entity.getMaxHealth());
            }
        }
    }

    /**
     * 为实体添加平成剑AI和武器，并提升生命值
     */
    public static void addHeiseiswordGoal(LivingEntity entity) {
        if (entity == null || !(entity instanceof Mob mob)) {
            return;
        }

        // 提升生命值
        boostHealth(entity);

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

        if (DEBUG) {
            LOGGER.debug("[{}] 已添加平成剑AI，当前生命值: {}/{}",
                    entity.getName().getString(), entity.getHealth(), entity.getMaxHealth());
        }
    }

    /**
     * 移除实体的平成剑AI和生命值加成
     */
    public static void removeHeiseiswordGoal(LivingEntity entity) {
        if (entity == null || !(entity instanceof Mob mob)) {
            return;
        }

        // 移除生命值加成
        removeHealthBoost(entity);

        GoalSelector goalSelector = mob.goalSelector;
        Set<WrappedGoal> goals = goalSelector.getAvailableGoals();

        goals.removeIf(goal -> goal.getGoal() instanceof HeiseiswordGoal);

        if (DEBUG) {
            LOGGER.debug("[{}] 已移除平成剑AI", entity.getName().getString());
        }
    }
}