package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;

public class FourzeEffect extends AbstractHeiseiRiderEffect {
    @Override
    public String getRiderName() {
        return "Fourze";
    }

    @Override
    public String getActivationSoundName() {
        return "name_fourze";
    }

    @Override
    public float getAttackDamage() {
        return 9.0F;
    }

    @Override
    public float getEffectRange() {
        return 8.0F;
    }
}