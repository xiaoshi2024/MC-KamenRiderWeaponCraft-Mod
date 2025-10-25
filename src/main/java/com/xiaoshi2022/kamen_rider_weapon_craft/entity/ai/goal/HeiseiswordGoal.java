package com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.goal;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Random;

public class HeiseiswordGoal extends Goal {

    protected final LivingEntity entity;
    protected final Level level;
    protected final Random random;
    protected LivingEntity target;
    protected int attackCooldown = 0;
    protected int maxAttackCooldown = 20; // 攻击冷却时间（刻）

    public HeiseiswordGoal(LivingEntity entity) {
        this.entity = entity;
        this.level = entity.level();
        this.random = new Random(); // 使用独立的Random实例，避免依赖实体方法
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        // 检查实体是否手持Heiseisword
        ItemStack mainHand = entity.getMainHandItem();
        if (!(mainHand.getItem() instanceof Heiseisword)) {
            return false;
        }

        // 获取攻击目标
        this.target = getAttackTarget(entity);
        
        // 只有当有目标且目标距离在可攻击范围内时才执行此目标
        return target != null && isWithinAttackRange(target);
    }

    @Override
    public boolean canContinueToUse() {
        // 检查是否仍然有Heiseisword且目标仍然存在且在范围内
        ItemStack mainHand = entity.getMainHandItem();
        if (!(mainHand.getItem() instanceof Heiseisword) || target == null || !target.isAlive()) {
            return false;
        }
        
        // 如果是怪物，检查其是否仍然以target为攻击目标
        if (entity instanceof Monster monster) {
            return monster.getTarget() == target && isWithinAttackRange(target);
        }
        
        return isWithinAttackRange(target);
    }

    @Override
    public void start() {
        // 开始执行目标时的初始化
        attackCooldown = 0;
    }

    @Override
    public void tick() {
        // 更新攻击冷却时间
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // 如果有目标，朝向目标（仅对Mob类）
        if (target != null && entity instanceof Mob mob) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // 尝试攻击
        if (canAttack() && attackCooldown <= 0) {
            performAttack();
            attackCooldown = maxAttackCooldown;
        }
    }

    // 判断是否可以攻击
    protected boolean canAttack() {
        return target != null && target.isAlive() && isWithinAttackRange(target);
    }

    // 执行攻击
    protected void performAttack() {
        ItemStack mainHand = entity.getMainHandItem();
        if (!(mainHand.getItem() instanceof Heiseisword heiseisword)) {
            return;
        }

        // 计算攻击方向
        Vec3 direction = target.position().subtract(entity.position()).normalize();
        
        // 获取选中的骑士
        String selectedRider = Heiseisword.getSelectedRiderStatic(mainHand);
        
        if (selectedRider != null && !selectedRider.isEmpty()) {
            // 获取骑士特效对象
            HeiseiRiderEffect riderEffect = HeiseiRiderEffectManager.getRiderEffect(selectedRider);
            
            // 70%概率执行普通攻击，30%概率执行远程攻击
            if (random.nextDouble() < 0.7) {
                // 近战攻击
                if (entity.distanceTo(target) < 2.5) {
                    // 执行骑士的特殊攻击效果
                    riderEffect.executeSpecialAttack(level, entity, direction);
                    
                    // 播放攻击音效
                    HeiseiRiderEffectManager.playAttackSound(level, entity, selectedRider);
                    
                    // 更新攻击时间
                    mainHand.getOrCreateTag().putLong("lastAttackTime", level.getGameTime());
                }
            } else {
                // 远程攻击
                float chargeTime = 0.5F + random.nextFloat() * 0.5F; // 半满到满蓄力
                riderEffect.executeSpecialAttack(level, entity, direction.scale(chargeTime * 2.0));
                
                // 播放攻击音效
                HeiseiRiderEffectManager.playAttackSound(level, entity, selectedRider);
                
                // 更新攻击时间
                mainHand.getOrCreateTag().putLong("lastAttackTime", level.getGameTime());
            }
        }
    }

    // 获取实体的攻击目标
    protected LivingEntity getAttackTarget(LivingEntity entity) {
        if (entity instanceof Monster monster) {
            return monster.getTarget();
        }
        
        // 对于非怪物实体，我们可以简单地返回附近的玩家或敌对实体
        // 这里可以扩展以支持更多的目标选择逻辑
        return level.getNearestPlayer(entity, 16.0D);
    }

    // 判断目标是否在攻击范围内
    protected boolean isWithinAttackRange(LivingEntity target) {
        double distance = entity.distanceTo(target);
        return distance <= 8.0D; // 8格范围内
    }

    // 检查实体是否手持Heiseisword
    protected boolean isHoldingHeiseisword() {
        ItemStack mainHand = entity.getMainHandItem();
        return mainHand.getItem() instanceof Heiseisword;
    }
}