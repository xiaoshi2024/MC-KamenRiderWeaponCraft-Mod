package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;

public class KabutoEffect extends AbstractHeiseiRiderEffect {
    @Override
    public String getRiderName() {
        return "Kabuto";
    }

    @Override
    public String getActivationSoundName() {
        return "name_kabuto";
    }

    @Override
    public float getAttackDamage() {
        return 12.0F;
    }

    @Override
    public float getEffectRange() {
        return 8.0F;
    }
}