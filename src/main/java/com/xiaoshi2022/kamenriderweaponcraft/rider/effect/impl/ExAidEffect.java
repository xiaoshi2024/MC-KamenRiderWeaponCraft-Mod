package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;

public class ExAidEffect extends AbstractHeiseiRiderEffect {
    @Override
    public String getRiderName() {
        return "Ex-Aid";
    }

    @Override
    public String getActivationSoundName() {
        return "name_exaid";
    }

    @Override
    public float getAttackDamage() {
        return 10.0F;
    }

    @Override
    public float getEffectRange() {
        return 6.0F;
    }
}