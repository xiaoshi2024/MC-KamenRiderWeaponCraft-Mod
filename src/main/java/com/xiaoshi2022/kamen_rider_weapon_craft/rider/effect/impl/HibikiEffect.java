package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.hibiki.HibikiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HibikiEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        executeDrumLockAttack(level, player);
    }
    
    /**
     * 允许任何LivingEntity（包括僵尸）使用响鬼鼓锁定特效
     * @param attacker 使用技能的实体
     * @param target 目标实体
     * @return 是否成功执行攻击
     */
    public boolean executeEntityDrumAttack(LivingEntity attacker, LivingEntity target) {
        if (attacker == null || target == null || attacker.level().isClientSide) {
            return false;
        }
        
        Level level = attacker.level();
        
        // 检查目标是否可以被锁定且未被其他鼓锁定
        if (HibikiRiderEffect.canLockTarget(target) && !HibikiRiderEffect.isTargetAlreadyLocked(level, target)) {
            // 执行鼓锁定攻击
            boolean attackSuccess = HibikiRiderEffect.performDrumLockAttack(attacker, target);
            
            // 给予攻击者抗性效果（如果是LivingEntity）
            if (attackSuccess && attacker instanceof LivingEntity) {
                attacker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
            }
            
            return attackSuccess;
        }
        
        return false;
    }
    
    /**
     * 执行鼓锁定攻击的通用方法
     * @param level 世界对象
     * @param owner 技能施放者
     */
    private void executeDrumLockAttack(Level level, Entity owner) {
        if (!level.isClientSide) {
            // 查找最近的目标
            LivingEntity target = HibikiRiderEffect.findNearestTarget(owner, getEffectRange());
            
            // 如果找到了目标，执行鼓锁定攻击
            if (target != null) {
                boolean attackSuccess = HibikiRiderEffect.performDrumLockAttack(owner, target);
                
                // 如果攻击成功且施放者是LivingEntity，给予抗性效果
                if (attackSuccess && owner instanceof LivingEntity livingOwner) {
                    livingOwner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
                }
            }
        }
    }

    @Override
    public String getRiderName() {
        return "Hibiki";
    }

    @Override
    public String getActivationSoundName() {
        return "Oni no Koe!";
    }

    @Override
    public float getAttackDamage() {
        return 47.0f; // 普通骑士 - Hibiki使用音击之技，是平成第七位骑士，伤害略高于基础值
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
    
    /**
     * 为非玩家实体（如僵尸）执行响鬼特有的鼓锁定攻击
     * 重写此方法确保僵尸等生物使用响鬼技能时也能显示鼓锁定特效
     */
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        // 调用响鬼特有的鼓锁定攻击方法
        executeDrumLockAttack(level, shooter);
    }
}
