package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GhostEffect extends AbstractHeiseiRiderEffect {
    
    // 随机数生成器
    private final Random random = new Random();
    
    // 用于跟踪已使用的伟人魂，实现不重复选择
    private final List<HeroicSoul> usedHeroicSouls = new ArrayList<>();
    
    // 伟人魂枚举，包含三种不同的技能效果和颜色
    private enum HeroicSoul {
        // 武藏魂：火属性双刀流
        MUSASHI("Musashi", 0xFF4500, 1.2f, 18, true),
        // 爱迪生魂：电磁力
        EDISON("Edison", 0xFFFF00, 1.1f, 18, false),
        // 牛顿魂：引力
        NEWTON("Newton", 0x1E90FF, 1.0f, 18, false);
        
        private final String name;
        private final int color;
        private final float damageMultiplier;
        private final int entityModelId;
        private final boolean isFireDamage;
        
        HeroicSoul(String name, int color, float damageMultiplier, int entityModelId, boolean isFireDamage) {
            this.name = name;
            this.color = color;
            this.damageMultiplier = damageMultiplier;
            this.entityModelId = entityModelId;
            this.isFireDamage = isFireDamage;
        }
        
        public String getName() {
            return name;
        }
        
        public int getColor() {
            return color;
        }
        
        public float getDamageMultiplier() {
            return damageMultiplier;
        }
        
        public int getEntityModelId() {
            return entityModelId;
        }
        
        public boolean isFireDamage() {
            return isFireDamage;
        }
    }

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 获取未使用的伟人魂列表
            List<HeroicSoul> availableSouls = new ArrayList<>();
            for (HeroicSoul soul : HeroicSoul.values()) {
                if (!usedHeroicSouls.contains(soul)) {
                    availableSouls.add(soul);
                }
            }
            
            // 如果所有伟人魂都被使用过，重置列表
            if (availableSouls.isEmpty()) {
                usedHeroicSouls.clear();
                availableSouls.addAll(List.of(HeroicSoul.values()));
            }
            
            // 随机选择一个未使用的伟人魂
            HeroicSoul selectedSoul = availableSouls.get(random.nextInt(availableSouls.size()));
            usedHeroicSouls.add(selectedSoul);
            
            // 创建离体的伟人魂实体（GeoModel方式）
            spawnHeroicSoulEntity(level, player, direction, selectedSoul);
            
            // 执行对应伟人魂的技能
            executeHeroicSoulAbility(level, player, direction, selectedSoul);
            
            // 给予玩家幽灵相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
        }
    }
    
    // 执行伟人魂技能的主方法
    private void executeHeroicSoulAbility(Level level, Player player, Vec3 direction, HeroicSoul soul) {
        switch (soul) {
            case MUSASHI:
                executeMusashiAbility(level, player, direction, getAttackDamage() * soul.getDamageMultiplier());
                break;
            case EDISON:
                executeEdisonAbility(level, player, direction, getAttackDamage() * soul.getDamageMultiplier());
                break;
            case NEWTON:
                executeNewtonAbility(level, player, direction, getAttackDamage() * soul.getDamageMultiplier());
                break;
        }
    }
    
    // 武藏魂：火属性双刀流
    private void executeMusashiAbility(Level level, Player player, Vec3 direction, float damage) {
        // 向前方发射两道火焰剑气
        Vec3 start = player.getEyePosition(1.0f);
        
        // 生成左右两个方向的火焰剑气
        Vec3 rightDirection = direction.yRot((float) Math.toRadians(20));
        Vec3 leftDirection = direction.yRot((float) Math.toRadians(-20));
        
        // 右剑气
        Vec3 rightEnd = start.add(rightDirection.scale(15.0));
        level.getEntitiesOfClass(LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, rightEnd).inflate(1.5),
                entity -> entity != player)
            .forEach(entity -> {
                entity.hurt(level.damageSources().playerAttack(player), damage);
                entity.setSecondsOnFire(8);
            });
        
        // 左剑气
        Vec3 leftEnd = start.add(leftDirection.scale(15.0));
        level.getEntitiesOfClass(LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, leftEnd).inflate(1.5),
                entity -> entity != player)
            .forEach(entity -> {
                entity.hurt(level.damageSources().playerAttack(player), damage);
                entity.setSecondsOnFire(8);
            });
    }
    
    // 爱迪生魂：电磁力
    private void executeEdisonAbility(Level level, Player player, Vec3 direction, float damage) {
        // 制造电磁力场，对周围敌人造成伤害并附加闪电效果
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(12.0));
        
        // 对路径上的敌人造成伤害
        level.getEntitiesOfClass(LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, end).inflate(2.0),
                entity -> entity != player)
            .forEach(entity -> {
                entity.hurt(level.damageSources().playerAttack(player), damage);
                // 给予敌人虚弱效果
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                // 模拟电磁效果，使用粒子效果替代闪电
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            });
    }
    
    // 牛顿魂：引力
    private void executeNewtonAbility(Level level, Player player, Vec3 direction, float damage) {
        // 创建引力场，将敌人吸引到中心点并造成伤害
        Vec3 center = player.getEyePosition(1.0f).add(direction.scale(6.0));
        
        // 查找范围内的敌人
        level.getEntitiesOfClass(LivingEntity.class, 
                new net.minecraft.world.phys.AABB(center.x - 5, center.y - 5, center.z - 5, center.x + 5, center.y + 5, center.z + 5),
                entity -> entity != player)
            .forEach(entity -> {
                // 计算引力方向（指向中心点）
                Vec3 attraction = center.subtract(entity.position()).normalize().scale(0.3);
                
                // 应用引力效果
                entity.setDeltaMovement(entity.getDeltaMovement().add(attraction));
                
                // 造成伤害
                entity.hurt(level.damageSources().playerAttack(player), damage);
                
                // 给予缓慢效果
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
            });
    }
    
    // 生成伟人魂Geo实体
    private void spawnHeroicSoulEntity(Level level, Player player, Vec3 direction, HeroicSoul soul) {
        // 调用GhostHeroicSoulEntity的静态方法生成实体
        com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ghost.GhostHeroicSoulEntity.trySpawnEffect(
            level, 
            player, 
            direction, 
            soul.getColor(), 
            getAttackDamage() * soul.getDamageMultiplier(), 
            soul.isFireDamage(),
            soul.name() // 传递灵魂类型名称
        );
    }

    @Override
    public String getRiderName() {
        return "Ghost";
    }

    @Override
    public String getActivationSoundName() {
        return "Omega Drive!";
    }

    @Override
    public float getAttackDamage() {
        return 49.0f;
    }

    @Override
    public float getEffectRange() {
        return 15.0f; // 扩大伟人魂技能的效果范围
    }
}
