package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ooo.OOOGeoEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OOOEffect extends AbstractHeiseiRiderEffect {

    // 只保留恐龙联组(Putotyra)
    private static final String PUTOTYRA = "putotyra"; // 翼角暴（紫色）
    private static final double R = 0.8, G = 0.0, B = 0.8; // 紫色RGB值

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Scanning Charge，仅使用恐龙联组
            
            // 执行恐龙联组的攻击效果
            executeFullPowerAttack(level, player);
            
            // 执行细胞硬币吞噬效果，传入方向向量
            executeCellMedalSlash(level, player, direction);
            
            // 给予玩家硬币相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    /**
     * 为非玩家实体（如僵尸）实现OOO特有的攻击效果
     */
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：为非玩家实体执行恐龙联组攻击
            
            // 执行范围攻击
            executeNonPlayerRangeAttack(level, shooter);
            
            // 执行细胞硬币吞噬效果，传入方向向量
            executeNonPlayerCellMedalSlash(level, shooter, direction);
        }
    }
    
    /**
     * 为非玩家实体执行范围攻击
     */
    private void executeNonPlayerRangeAttack(Level level, LivingEntity shooter) {
        // 对周围敌人造成伤害
        float attackDamage = getAttackDamage() * 0.7f;
        DamageSource damageSource = level.damageSources().mobAttack(shooter);
        
        level.getEntitiesOfClass(LivingEntity.class, 
                shooter.getBoundingBox().inflate(getEffectRange() / 2),
                entity -> entity != shooter && entity.isAlive())
            .forEach(entity -> {
                entity.hurt(damageSource, attackDamage);
                
                // 给予敌人虚弱效果
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 150, 0));
            });
    }
    
    /**
     * 为非玩家实体执行细胞硬币吞噬攻击
     */
    private void executeNonPlayerCellMedalSlash(Level level, LivingEntity shooter, Vec3 direction) {
        float attackDamage = getAttackDamage() * 1.5f;
        
        // 首先尝试查找攻击方向上最近的敌对实体
        LivingEntity target = findNearestTargetInDirection(level, shooter, direction, 10.0);
        
        // 执行恐龙联组的细胞硬币吞噬效果
        // 使用非玩家专用的静态方法
        OOOGeoEffect.spawnCellMedalSwallow(level, shooter, direction, attackDamage, target);
    }
    
    /**
     * 在指定方向上查找最近的敌对实体（支持任何LivingEntity）
     */
    private LivingEntity findNearestTargetInDirection(Level level, LivingEntity shooter, Vec3 direction, double maxRange) {
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(maxRange));
        
        // 创建一个沿着方向的AABB来搜索实体
        net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(start, end).inflate(2.0);
        
        // 查找最近的敌对实体
        return level.getEntitiesOfClass(LivingEntity.class, searchBox, 
                entity -> entity != shooter && entity.isAlive())
                .stream()
                .min((e1, e2) -> {
                    double d1 = e1.distanceToSqr(start);
                    double d2 = e2.distanceToSqr(start);
                    return Double.compare(d1, d2);
                })
                .orElse(null);
    }
    
    private void executeBalancedAttack(Level level, Player player, Vec3 direction) {
        // 平衡型攻击，对前方直线上的敌人造成伤害
        double reach = 8.0;
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, end).inflate(1.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 1.0f);
            });
    }
    
    private void executePowerAttack(Level level, Player player) {
        // 力量型攻击，对周围敌人造成伤害
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                player.getBoundingBox().inflate(5.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 0.8f);
                
                // 击退敌人
                Vec3 knockback = entity.position().subtract(player.position()).normalize().scale(1.0);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
    }
    
    private void executeWaterAttack(Level level, Player player, Vec3 direction) {
        // 水属性攻击，远程打击
        for (int i = 0; i < 3; i++) {
            double spreadX = (level.random.nextDouble() - 0.5) * 0.2;
            double spreadY = (level.random.nextDouble() - 0.5) * 0.2;
            double spreadZ = (level.random.nextDouble() - 0.5) * 0.2;
            Vec3 waterDir = new Vec3(
                direction.x + spreadX,
                direction.y + spreadY,
                direction.z + spreadZ
            ).normalize();
            
            // 发射水弹
            double waterReach = 12.0;
            Vec3 waterStart = player.getEyePosition(1.0f);
            Vec3 waterEnd = waterStart.add(waterDir.scale(waterReach));
            
            // 优化：使用更高效的实体查找方式
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(waterStart, waterEnd).inflate(0.5),
                    entity -> entity != player) // 提前过滤掉玩家自己
                .forEach(entity -> {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.6f);
                });
        }
    }
    
    private void executeFullPowerAttack(Level level, Player player) {
        // 全能型攻击，对大范围造成伤害
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                player.getBoundingBox().inflate(7.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 0.7f);
                
                // 给予敌人虚弱效果
                ((net.minecraft.world.entity.LivingEntity) entity).addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, 150, 0));
            });
    }
    
    private void executeDefenseAttack(Level level, Player player) {
        // 防御型攻击，对周围敌人造成伤害并提供防御增益
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                player.getBoundingBox().inflate(6.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 0.8f);
            });
        
        // 给予玩家防御增益
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
    }
    
    /**
     * 执行细胞硬币斩攻击
     * @param level 世界对象
     * @param player 玩家
     * @param direction 攻击方向（用于实现跟随挥舞效果）
     * @param combo 联组类型
     */
    /**
     * 执行细胞硬币吞噬攻击 - 恐龙联组专属效果
     * @param level 世界对象
     * @param player 玩家
     * @param direction 攻击方向
     */
    private void executeCellMedalSlash(Level level, Player player, Vec3 direction) {
        float attackDamage = getAttackDamage();
        
        // 首先尝试查找攻击方向上最近的敌对实体
        LivingEntity target = findNearestTargetInDirection(level, player, direction, 10.0);
        
        // 执行恐龙联组的细胞硬币吞噬效果
        // 如果有目标，传入目标信息；如果没有，仍然生成但会自动寻找目标
        OOOGeoEffect.spawnCellMedalSwallow(level, player, direction, attackDamage * 1.5f, target);
    }
    
    /**
     * 在指定方向上查找最近的敌对实体
     */
    private LivingEntity findNearestTargetInDirection(Level level, Player player, Vec3 direction, double maxRange) {
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(maxRange));
        
        // 创建一个沿着方向的AABB来搜索实体
        AABB searchBox = new AABB(start, end).inflate(2.0);
        
        // 查找最近的敌对实体
        return level.getEntitiesOfClass(LivingEntity.class, searchBox, 
                entity -> entity != player && entity.isAlive() && player.canAttack(entity))
                .stream()
                .min((e1, e2) -> {
                    double d1 = e1.distanceToSqr(start);
                    double d2 = e2.distanceToSqr(start);
                    return Double.compare(d1, d2);
                })
                .orElse(null);
    }

    @Override
    public String getRiderName() {
        return "OOO";
    }

    @Override
    public String getActivationSoundName() {
        return "Scanning Charge!";
    }

    @Override
    public float getAttackDamage() {
        // OOO骑士，伤害高
        return 51.0f;
    }

    @Override
    public float getEffectRange() {
        return 14.0f;
    }
}
