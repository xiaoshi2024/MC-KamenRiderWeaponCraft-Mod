package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.hibiki;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 响鬼骑手机能效果管理类
 * 负责处理响鬼骑士的鼓锁定特效和必杀技
 */
public class HibikiRiderEffect {
    
    /**
     * 生成响鬼鼓锁定特效
     * @param level 世界对象
     * @param owner 技能施放者
     * @param target 目标实体
     */
    public static void spawnDrumEffect(Level level, Entity owner, LivingEntity target) {
        // 调用实体类中的静态方法生成特效
        HibikiDrumEffectEntity.spawnEffect(level, owner, target);
    }
    
    /**
     * 检查目标是否可以被鼓锁定
     * @param target 目标实体
     * @return 是否可以锁定
     */
    public static boolean canLockTarget(LivingEntity target) {
        if (target == null || target.isRemoved() || target.isDeadOrDying()) {
            return false;
        }
        
        // 检查目标是否是玩家，如果是，可能需要特殊处理（如PVP限制）
        if (target instanceof net.minecraft.world.entity.player.Player) {
            // 这里可以添加PVP限制逻辑
            // 例如检查服务器PVP设置，或者目标是否为同一队伍等
            return true; // 暂时允许锁定玩家
        }
        
        // 对于非玩家实体，只要存在且活着就可以锁定
        return true;
    }
    
    /**
     * 获取响鬼鼓特效的伤害倍率
     * @param level 技能等级/强化等级
     * @return 伤害倍率
     */
    public static double getDrumDamageMultiplier(int level) {
        // 简单的伤害倍率计算，可以根据需要调整
        return 1.0 + (level - 1) * 0.2;
    }
    
    /**
     * 在主世界中查找并锁定最近的目标
     * 这个方法可以用于自动锁定功能
     * @param owner 技能施放者
     * @param maxDistance 最大锁定距离
     * @return 找到的目标实体，如果没有找到返回null
     */
    public static LivingEntity findNearestTarget(Entity owner, double maxDistance) {
        if (owner == null || owner.level().isClientSide) {
            return null;
        }
        
        // 使用射线检测查找最近的实体
        LivingEntity closestTarget = null;
        double closestDistance = maxDistance;
        
        // 获取周围的所有生物
        for (LivingEntity entity : owner.level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(maxDistance))) {
            // 跳过自己和无法锁定的目标
            if (entity == owner || !canLockTarget(entity)) {
                continue;
            }
            
            // 计算距离
            double distance = owner.distanceTo(entity);
            
            // 检查是否在视线范围内
            if (distance < closestDistance) {
                // 只有LivingEntity才有hasLineOfSight方法
                if (owner instanceof LivingEntity livingOwner && livingOwner.hasLineOfSight(entity)) {
                    closestDistance = distance;
                    closestTarget = entity;
                } else {
                    // 对于非LivingEntity，我们仍然允许锁定，但优先级较低
                    closestDistance = distance;
                    closestTarget = entity;
                }
            }
        }
        
        return closestTarget;
    }
    
    /**
     * 检查是否已经有响鬼鼓特效锁定了目标
     * 防止多个鼓同时锁定同一个目标
     * @param level 世界对象
     * @param target 目标实体
     * @return 是否已经有鼓特效锁定了该目标
     */
    public static boolean isTargetAlreadyLocked(Level level, LivingEntity target) {
        if (target == null) {
            return false;
        }
        
        // 遍历世界中的所有响鬼鼓特效实体
        for (HibikiDrumEffectEntity drum : level.getEntitiesOfClass(HibikiDrumEffectEntity.class, target.getBoundingBox().inflate(20.0D))) {
            // 检查这个鼓是否锁定了相同的目标
            if (drum.getTargetEntity() == target) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 执行响鬼必杀技
     * 这个方法可以作为武器右键或特定组合键的响应
     * @param owner 技能施放者
     * @param target 目标实体
     * @return 是否成功施放技能
     */
    public static boolean performDrumLockAttack(Entity owner, LivingEntity target) {
        if (owner == null || target == null || owner.level().isClientSide) {
            return false;
        }
        
        Level level = owner.level();
        
        // 检查目标是否可以被锁定
        if (!canLockTarget(target)) {
            return false;
        }
        
        // 检查目标是否已经被锁定
        if (isTargetAlreadyLocked(level, target)) {
            return false;
        }
        
        // 生成鼓特效
        spawnDrumEffect(level, owner, target);
        
        // 播放音效，使用适合的音效替代不存在的DRUM音效
        level.playSound(null, owner.blockPosition(), net.minecraft.sounds.SoundEvents.WOODEN_BUTTON_CLICK_ON, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
        
        return true;
    }
}

// 注意：在实际游戏中，需要确保以下几点：
// 1. 在resources/assets/kamen_rider_weapon_craft/geo/rider/hibiki/目录下创建hibiki_drum.geo.json模型文件
// 2. 在resources/assets/kamen_rider_weapon_craft/textures/rider/hibiki/目录下创建hibiki_drum.png纹理文件
// 3. 在resources/assets/kamen_rider_weapon_craft/animations/rider/hibiki/目录下创建hibiki_drum.animation.json动画文件
// 4. 动画文件中需要包含idle、charge和explosion三个动画片段
// 5. 如果没有现成的模型和纹理，可以使用占位符，后续再替换为实际的资源
// 6. 确保HibikiDrumEffectEntity类中的动画名称与动画文件中的保持一致