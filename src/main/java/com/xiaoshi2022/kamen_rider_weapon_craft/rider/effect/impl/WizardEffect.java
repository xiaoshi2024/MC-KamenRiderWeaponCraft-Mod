package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.wizard.WizardRiderEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WizardEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动元素魔龙攻击
            // 随机选择一个元素魔龙力量
            WizardRiderEntity.DragonMagicType selectedDragon = WizardRiderEntity.DragonMagicType.values()[level.random.nextInt(WizardRiderEntity.DragonMagicType.values().length)];
            
            // 生成对应的元素魔龙特效实体
            WizardRiderEntity.trySpawnEffect(level, player, direction, getAttackDamage(), selectedDragon);
            
            switch (selectedDragon) {
                case FlameDragon:
                    executeFlameMagic(level, player, direction);
                    break;
                case WaterDragon:
                    executeWaterMagic(level, player, direction);
                    break;
                case HurricaneDragon:
                    executeHurricaneMagic(level, player, direction);
                    break;
                case LandDragon:
                    executeLandMagic(level, player, direction);
                    break;
            }
            
            // 给予玩家魔龙之力相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1)); // 提升伤害加成等级
        }
    }
    
    private void executeFlameMagic(Level level, Player player, Vec3 direction) {
        // 火焰魔龙之力：强化火焰攻击
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(20.0)); // 增加射程
        
        // 使用正确的射线检测方法
        net.minecraft.world.phys.HitResult result = player.pick(20.0, 0.0f, false);
        
        if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 1.2f);
                ((net.minecraft.world.entity.LivingEntity) entity).setSecondsOnFire(10);
                
                // 强大的爆炸效果
                level.explode(player, entity.getX(), entity.getY(), entity.getZ(), 
                    getEffectRange(), Level.ExplosionInteraction.MOB);
            }
        }
        
        // 给予玩家火焰抗性
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 1)); // 提升火焰抗性等级
    }
    
    private void executeWaterMagic(Level level, Player player, Vec3 direction) {
        // 水魔龙之力：强化水流攻击
        // 对前方敌人造成伤害和减速效果
        for (int i = 0; i < 12; i++) { // 增加范围
            Vec3 waterPos = player.getEyePosition(1.0f).add(direction.scale(i + 1));
            
            // 对敌人造成伤害和减速效果
            level.getEntities(player, new net.minecraft.world.phys.AABB(waterPos, waterPos).inflate(2.0))
                .forEach(entity -> {
                    if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                        ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                            level.damageSources().playerAttack(player), getAttackDamage() * 0.4f);
                        ((net.minecraft.world.entity.LivingEntity) entity).addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN, 150, 3)); // 提升减速效果
                        // 灭火效果
                        if (((net.minecraft.world.entity.LivingEntity) entity).isOnFire()) {
                            ((net.minecraft.world.entity.LivingEntity) entity).clearFire();
                        }
                    }
                });
        }
        
        // 给予玩家水下呼吸和抗性提升
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
        
        // 扩大灭火范围
        int rainRange = 8; // 扩大降雨范围
        for (int x = -rainRange; x <= rainRange; x++) {
            for (int z = -rainRange; z <= rainRange; z++) {
                // 从玩家位置上方开始，向下检查
                for (int y = 5; y >= -3; y--) {
                    net.minecraft.core.BlockPos firePos = player.blockPosition().offset(x, y, z);
                    if (level.getBlockState(firePos).getBlock() == net.minecraft.world.level.block.Blocks.FIRE) {
                        level.removeBlock(firePos, false);
                    }
                }
            }
        }
    }
    
    private void executeHurricaneMagic(Level level, Player player, Vec3 direction) {
        // 飓风魔龙之力：制造强大风暴
        // 移除了爆炸效果，避免地形破坏
        
        // 对周围敌人造成伤害和强力击退效果
        level.getEntities(player, player.getBoundingBox().inflate(getEffectRange() * 2.0))
            .forEach(entity -> {
                if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.7f);
                    
                    // 超强力的击退效果
                    Vec3 knockback = entity.position().subtract(player.position()).normalize().scale(4.0);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
                    
                    // 给予悬浮效果
                    ((net.minecraft.world.entity.LivingEntity) entity).addEffect(new MobEffectInstance(
                        MobEffects.LEVITATION, 100, 2));
                }
            });
        
        // 给予玩家飞行能力和速度提升
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2));
    }
    
    private void executeLandMagic(Level level, Player player, Vec3 direction) {
        // 土地魔龙之力：强化土地控制能力
        // 在玩家前方生成更多方块障碍
        for (int i = 0; i < 5; i++) { // 生成多个方块
            Vec3 pos = player.getEyePosition(1.0f).add(direction.scale(2.0 + i));
            net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos((int)pos.x, (int)pos.y, (int)pos.z);
            
            // 尝试放置方块作为障碍物
            if (level.isEmptyBlock(blockPos)) {
                // 随机放置石头或泥土方块
                if (level.random.nextBoolean()) {
                    level.setBlockAndUpdate(blockPos, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                } else {
                    level.setBlockAndUpdate(blockPos, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
                }
            }
        }
        
        // 对周围敌人造成伤害和强力减速效果
        level.getEntities(player, player.getBoundingBox().inflate(getEffectRange() * 1.5))
            .forEach(entity -> {
                if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.8f);
                    ((net.minecraft.world.entity.LivingEntity) entity).addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 240, 4)); // 极高的减速效果
                    // 额外添加虚弱效果
                    ((net.minecraft.world.entity.LivingEntity) entity).addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 200, 2));
                }
            });
        
        // 给予玩家强大的生命恢复和防御增强
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 2));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2));
    }

    @Override
    public String getRiderName() {
        return "Wizard";
    }

    @Override
    public String getActivationSoundName() {
        return "Dragon Form!";
    }

    @Override
    public float getAttackDamage() {
        return 70.0f; // 元素魔龙形态的更高基础攻击力
    }

    @Override
    public float getEffectRange() {
        return 12.0f; // 扩大效果范围
    }
    
    @Override
    public double getEnergyCost() {
        return 20.0; // 使用默认能量消耗
    }
    
    // 元素魔龙特效相关资源需求：
    // 1. 单个统一的geo模型文件：
    //    - dragon_wizard.geo.json (人形魔龙模型)
    // 2. 单个统一的纹理文件：
    //    - dragon_wizard.png (人形魔龙纹理)
    // 3. 四种不同的动画文件，对应四种元素魔龙力量：
    //    - dragon_wizard_flamedragon.animation.json
    //    - dragon_wizard_waterdragon.animation.json
    //    - dragon_wizard_hurricanedragon.animation.json
    //    - dragon_wizard_landdragon.animation.json
}
