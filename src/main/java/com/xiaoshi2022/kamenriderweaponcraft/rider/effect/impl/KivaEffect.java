package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;

public class KivaEffect extends AbstractHeiseiRiderEffect {
    @Override
    public String getRiderName() {
        return "Kiva";
    }

    @Override
    public String getActivationSoundName() {
        return "name_kiva";
    }

    @Override
    public float getAttackDamage() {
        return 11.0F;
    }

    @Override
    public float getEffectRange() {
        return 7.0F;
    }
}