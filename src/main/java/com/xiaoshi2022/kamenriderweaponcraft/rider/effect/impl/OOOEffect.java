package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ooo.OOOGeoEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class OOOEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            Vec3 normalizedDirection = direction != null && direction.lengthSqr() > 0 ?
                                      direction.normalize() : shooter.getLookAngle().normalize();

            // 30%概率触发恐龙联组的细胞硬币吞噬特效
            if (level.random.nextFloat() < 0.3) {
                OOOGeoEffect.spawnPutotyraCellMedalSwallow(level, shooter, getAttackDamage());
            } else {
                // 普通攻击：生成细胞硬币斩特效
                OOOGeoEffect.spawnCellMedalSlash(level, shooter, normalizedDirection, getAttackDamage(), "taka");
                OOOGeoEffect.spawnCellMedalSlash(level, shooter, normalizedDirection, getAttackDamage(), "tora");
                OOOGeoEffect.spawnCellMedalSlash(level, shooter, normalizedDirection, getAttackDamage(), "batta");
            }

            // 给予玩家增益效果
            if (shooter instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
            }
        }
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
        return 51.0f;
    }

    @Override
    public float getEffectRange() {
        return 14.0f;
    }
}