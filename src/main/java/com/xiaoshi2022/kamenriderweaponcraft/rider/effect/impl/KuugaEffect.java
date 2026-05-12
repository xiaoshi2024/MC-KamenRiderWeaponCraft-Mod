package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;

public class KuugaEffect extends AbstractHeiseiRiderEffect {
    @Override
    public String getRiderName() {
        return "Kuuga";
    }

    @Override
    public String getActivationSoundName() {
        return "name_kuuga";
    }

    @Override
    public float getAttackDamage() {
        return 9.0F;
    }

    @Override
    public float getEffectRange() {
        return 7.0F;
    }
}