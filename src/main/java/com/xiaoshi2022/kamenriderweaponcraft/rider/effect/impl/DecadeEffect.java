package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.decade.DecadeRiderEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DecadeEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            Vec3 normalizedDirection = direction != null && direction.lengthSqr() > 0 ?
                                      direction.normalize() : shooter.getLookAngle().normalize();

            // 为实体生成Decade特效实体
            DecadeRiderEntity.trySpawnEffect(level, shooter, normalizedDirection, getAttackDamage());

            // 给予实体增益效果
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 1));
            shooter.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 0));

            // 对前方敌人造成伤害
            double range = getEffectRange();
            Vec3 start = shooter.getEyePosition(1.0f);
            Vec3 end = start.add(normalizedDirection.scale(range));
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 2, 2.0, range / 2);

            level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
                if (entity == shooter) return false;
                Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
                return toEntity.dot(normalizedDirection) > 0.5;
            }).forEach(entity -> {
                if (shooter instanceof Player) {
                    entity.hurt(level.damageSources().playerAttack((Player) shooter), getAttackDamage());
                } else {
                    entity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage());
                }
            });
        }
    }

    @Override
    public String getRiderName() {
        return "Decade";
    }

    @Override
    public String getActivationSoundName() {
        return "Dimension Kick!";
    }

    @Override
    public float getAttackDamage() {
        return 52.0f;
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}