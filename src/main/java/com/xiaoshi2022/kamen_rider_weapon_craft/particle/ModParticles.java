package com.xiaoshi2022.kamen_rider_weapon_craft.particle;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, kamen_rider_weapon_craft.MOD_ID);

    public static final RegistryObject<SimpleParticleType> AONICX_PARTICLE = REGISTRY.register("aonicx_particle", () -> new SimpleParticleType(true));
}
