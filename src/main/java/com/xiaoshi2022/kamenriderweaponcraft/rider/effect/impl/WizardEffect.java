package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;

public class WizardEffect extends AbstractHeiseiRiderEffect {
    @Override
    public String getRiderName() {
        return "Wizard";
    }

    @Override
    public String getActivationSoundName() {
        return "name_wizard";
    }

    @Override
    public float getAttackDamage() {
        return 10.0F;
    }

    @Override
    public float getEffectRange() {
        return 7.0F;
    }
}