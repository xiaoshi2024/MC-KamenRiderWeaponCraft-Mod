package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.effects.HelmheimPowerEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectInit {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,"kamen_rider_weapon_craft");


    // 注册赫尔海姆之力效果
    public static final RegistryObject<MobEffect> HELMHEIM_POWER = EFFECTS.register("helmheim_power", () -> {
        return new HelmheimPowerEffect(MobEffectCategory.BENEFICIAL, 0xFFA500); // 橙色效果
    });
}
