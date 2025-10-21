package com.xiaoshi2022.kamen_rider_weapon_craft.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BaseEffect extends MobEffect {
    private boolean instant; // 是否是瞬时效果
    private boolean isRegistered = false; // 是否已注册
    protected float resistancePerLevel = 0.5f; // 每级效果的抗性值

    /**
     * 构造函数
     *
     * @param type  效果类型（有益、有害、中性）
     * @param color 效果的颜色
     */
    public BaseEffect(MobEffectCategory type, int color) {
        super(type, color);
        this.instant = false;
    }

    /**
     * 检查是否是瞬时效果
     * @return 是否是瞬时效果
     */
    public boolean isInstantaneous() {
        return instant;
    }

    /**
     * 检查效果是否在指定的tick生效
     * @param remainingTicks 剩余的tick数
     * @param amplifier 效果等级
     * @return 是否生效
     */
    @Override
    public boolean isDurationEffectTick(int remainingTicks, int amplifier) {
        if (isInstantaneous()) {
            return true; // 瞬时效果始终生效
        }
        return canApplyEffect(remainingTicks, amplifier); // 非瞬时效果根据子类逻辑判断
    }

    /**
     * 子类可以覆盖此方法来自定义效果的生效逻辑
     * @param remainingTicks 剩余的tick数
     * @param amplifier 效果等级
     * @return 是否生效
     */
    protected boolean canApplyEffect(int remainingTicks, int amplifier) {
        // 默认实现：非瞬时效果不生效
        return false;
    }

    /**
     * 应用效果
     * @param entity 受影响的生物
     * @param amplifier 效果等级
     */
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (isInstantaneous()) {
            applyInstantaneousEffect(null, null, entity, amplifier, 1.0); // 调用瞬时效果逻辑
        }
    }

    /**
     * 应用瞬时效果的默认实现
     * 子类可以覆盖此方法来自定义瞬时效果的逻辑
     * @param source 效果来源（可选）
     * @param indirectSource 间接来源（可选）
     * @param entity 受影响的生物
     * @param amplifier 效果等级
     * @param chance 生效概率
     */
    public void applyInstantaneousEffect(LivingEntity source, LivingEntity indirectSource, LivingEntity entity, int amplifier, double chance) {
        // 默认实现：无操作
    }

    /**
     * 标记效果为已注册
     * @return 当前效果实例
     */
    public BaseEffect onRegister() {
        isRegistered = true;
        return this;
    }

    /**
     * 检查效果是否已注册
     * @return 是否已注册
     */
    public boolean isRegistered() {
        return isRegistered;
    }
}