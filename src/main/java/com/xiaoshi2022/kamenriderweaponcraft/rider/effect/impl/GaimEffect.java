package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;

public class GaimEffect extends AbstractHeiseiRiderEffect {
    @Override
    public String getRiderName() {
        return "Gaim";
    }

    @Override
    public String getActivationSoundName() {
        return "name_gaim";
    }

    @Override
    public float getAttackDamage() {
        return 10.0F;
    }

    @Override
    public float getEffectRange() {
        return 8.0F;
    }
}