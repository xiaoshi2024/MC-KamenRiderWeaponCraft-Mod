package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;

public class HibikiEffect extends AbstractHeiseiRiderEffect {
    @Override
    public String getRiderName() {
        return "Hibiki";
    }

    @Override
    public String getActivationSoundName() {
        return "name_hibiki";
    }

    @Override
    public float getAttackDamage() {
        return 9.0F;
    }

    @Override
    public float getEffectRange() {
        return 10.0F;
    }
}