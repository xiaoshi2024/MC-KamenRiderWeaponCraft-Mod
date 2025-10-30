package com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.zombie;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Random;

public class ZombieSonicArrowGoal extends Goal {
    private final Zombie zombie;
    private final Random random = new Random();
    private int cooldown = 0;
    private static final int MIN_COOLDOWN = 20; // 最小冷却时间（1秒）
    private static final int MAX_COOLDOWN = 60; // 最大冷却时间（3秒）
    private static final double MAX_ATTACK_DISTANCE = 15.0; // 最大攻击距离
    private static final double MIN_ATTACK_DISTANCE = 3.0;  // 最小攻击距离

    public ZombieSonicArrowGoal(Zombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 只有当僵尸手持音速弓且有目标时才激活此AI
        ItemStack mainHandItem = zombie.getMainHandItem();
        return mainHandItem.getItem() instanceof sonicarrow && 
               zombie.getTarget() != null && 
               zombie.isAlive();
    }
    
    @Override
    public boolean canContinueToUse() {
        // 与canUse()条件相同，确保AI持续运行
        return canUse();
    }

    @Override
    public void tick() {
        super.tick();
        
        ItemStack mainHandItem = zombie.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof sonicarrow sonicArrow)) {
            return;
        }

        Level level = zombie.level();
        LivingEntity target = zombie.getTarget();
        
        if (target == null || !target.isAlive()) {
            return;
        }

        // 更新冷却时间
        if (cooldown > 0) {
            cooldown--;
        }

        // 让僵尸看向目标
        zombie.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 计算与目标的距离
        double distanceToTarget = zombie.distanceTo(target);

        // 控制移动：如果太远则靠近，太近则后退
        if (distanceToTarget > MAX_ATTACK_DISTANCE * 0.8) {
            // 目标太远，向目标移动
            Vec3 moveDirection = target.position().subtract(zombie.position()).normalize();
            zombie.getMoveControl().setWantedPosition(
                    zombie.getX() + moveDirection.x * 0.3, 
                    zombie.getY(), 
                    zombie.getZ() + moveDirection.z * 0.3,
                    0.25 // 移动速度
            );
        } else if (distanceToTarget < MIN_ATTACK_DISTANCE) {
            // 目标太近，向后退
            Vec3 moveDirection = zombie.position().subtract(target.position()).normalize();
            zombie.getMoveControl().setWantedPosition(
                    zombie.getX() + moveDirection.x * 0.2, 
                    zombie.getY(), 
                    zombie.getZ() + moveDirection.z * 0.2,
                    0.2 // 后退速度
            );
        } else {
            // 距离适中，站立不动并准备射击
            zombie.getMoveControl().setWantedPosition(zombie.getX(), zombie.getY(), zombie.getZ(), 0.0);
        }

        // 当距离合适且冷却完成时射击
        if (distanceToTarget >= MIN_ATTACK_DISTANCE && 
            distanceToTarget <= MAX_ATTACK_DISTANCE && 
            cooldown <= 0) {
            
            // 执行射击
            sonicArrow.shootProjectile(level, zombie, mainHandItem);
            
            // 设置新的冷却时间
            cooldown = random.nextInt(MAX_COOLDOWN - MIN_COOLDOWN + 1) + MIN_COOLDOWN;
        }
    }
}