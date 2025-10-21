package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 平成骑士效果的抽象基类，提供getEnergyCost()方法的默认实现
 */
public abstract class AbstractHeiseiRiderEffect implements HeiseiRiderEffect {
    
    /**
     * 默认的骑士能量消耗值
     * 子类可以重写此方法以提供自定义的能量消耗值
     */
    @Override
    public double getEnergyCost() {
        return 20.0; // 默认消耗20点骑士能量
    }
    
    /**
     * 执行特殊攻击效果
     * 子类必须实现此方法
     */
    @Override
    public abstract void executeSpecialAttack(Level level, Player player, Vec3 direction);
    
    /**
     * 返回骑士名称
     * 子类必须实现此方法
     */
    @Override
    public abstract String getRiderName();
    
    /**
     * 获取激活音效名称
     * 子类必须实现此方法
     */
    @Override
    public abstract String getActivationSoundName();
    
    /**
     * 攻击伤害值
     * 子类必须实现此方法
     */
    @Override
    public abstract float getAttackDamage();
    
    /**
     * 特效范围
     * 子类必须实现此方法
     */
    @Override
    public abstract float getEffectRange();
}