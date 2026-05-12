package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;

public class DecadeEffect extends AbstractHeiseiRiderEffect {
    @Override
    public String getRiderName() {
        return "Decade";
    }

    @Override
    public String getActivationSoundName() {
        return "name_decade";
    }

    @Override
    public float getAttackDamage() {
        return 13.0F;
    }

    @Override
    public float getEffectRange() {
        return 9.0F;
    }
}