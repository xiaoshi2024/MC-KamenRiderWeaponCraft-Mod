package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;

public class DriveEffect extends AbstractHeiseiRiderEffect {
    @Override
    public String getRiderName() {
        return "Drive";
    }

    @Override
    public String getActivationSoundName() {
        return "name_drive";
    }

    @Override
    public float getAttackDamage() {
        return 11.0F;
    }

    @Override
    public float getEffectRange() {
        return 9.0F;
    }
}