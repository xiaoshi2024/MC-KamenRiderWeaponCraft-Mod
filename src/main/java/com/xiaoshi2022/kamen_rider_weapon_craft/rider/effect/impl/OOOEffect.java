package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class OOOEffect implements HeiseiRiderEffect {

    // OOO的基本联组
    private enum OOOCombo {
        TATOBA(1.0, 0.8, 0.0), // 鹰虎蝗（金色）
        LATORARTAR(0.0, 0.5, 0.0), // 狮虎豹（绿色）
        SHAUTA(0.0, 0.0, 1.0), // 鲨鳗章（蓝色）
        PUTOTYRA(0.8, 0.0, 0.0);  // 翼角暴（紫色）
        
        private final double r, g, b;
        
        OOOCombo(double r, double g, double b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
        
        public double getR() { return r; }
        public double getG() { return g; }
        public double getB() { return b; }
    }

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Scanning Charge，使用不同的联组能力
            // 随机选择一个联组
            OOOCombo selectedCombo = OOOCombo.values()[level.random.nextInt(OOOCombo.values().length)];
            
            // 根据所选联组执行不同的攻击
            executeComboAttack(level, player, direction, selectedCombo);
            
            // 给予玩家硬币相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeComboAttack(Level level, Player player, Vec3 direction, OOOCombo combo) {
        // 根据所选联组执行不同的攻击效果
        switch (combo) {
            case TATOBA:
                // 鹰虎蝗联组：平衡的攻击，对前方造成伤害
                executeBalancedAttack(level, player, direction);
                break;
            case LATORARTAR:
                // 狮虎豹联组：力量型攻击，对周围造成伤害
                executePowerAttack(level, player);
                break;
            case SHAUTA:
                // 鲨鳗章联组：水属性攻击，远程打击
                executeWaterAttack(level, player, direction);
                break;
            case PUTOTYRA:
                // 翼角暴联组：全能型攻击，对大范围造成伤害
                executeFullPowerAttack(level, player);
                break;
        }
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
