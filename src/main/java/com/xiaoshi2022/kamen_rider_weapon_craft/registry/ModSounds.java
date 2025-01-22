package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
        public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, kamen_rider_weapon_craft.MOD_ID);

        public static final RegistryObject<SoundEvent> GAVVWHIPIR_START_TONE = REGISTRY.register("gavvwhipir_start_tone",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "gavvwhipir_start_tone")));
       public static final RegistryObject<SoundEvent> RIDERBOOKERSWORD = REGISTRY.register("riderbookersword",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "riderbookersword")));
}
