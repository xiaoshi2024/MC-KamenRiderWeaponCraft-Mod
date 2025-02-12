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
        public static final RegistryObject<SoundEvent> MELONENERGY = REGISTRY.register("melonenergy",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "melonenergy")));
        public static final RegistryObject<SoundEvent> OPENDLOCK = REGISTRY.register("opendlock",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "opendlock")));
        public static final RegistryObject<SoundEvent> SONICARROW_SHOOT = REGISTRY.register("sonicarrow_shoot",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "sonicarrow_shoot")));
        public static final RegistryObject<SoundEvent> SONICARROW_BOOT_SOUND = REGISTRY.register("sonicarrow_boot_sound",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "sonicarrow_boot_sound")));
        public static final RegistryObject<SoundEvent> SLASH = REGISTRY.register("slash",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "slash")));
        public static final RegistryObject<SoundEvent> PULL_STANDBY = REGISTRY.register("pull_standby",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "pull_standby")));
        public static final RegistryObject<SoundEvent> LOCK_SEED_PUT_IN = REGISTRY.register("lock_seed_put_in",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "lock_seed_put_in")));
        public static final RegistryObject<SoundEvent> LOCK_ON = REGISTRY.register("lock_on",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "lock_on")));
        public static final RegistryObject<SoundEvent> PROGRISE_HOPPER_BLADE_BOOT = REGISTRY.register("progrise_hopper_blade_boot",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "progrise_hopper_blade_boot")));
        public static final RegistryObject<SoundEvent> PROGRISE_HOPPER_BLADE_PRESS = REGISTRY.register("progrise_hopper_blade_press",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "progrise_hopper_blade_press")));
        public static final RegistryObject<SoundEvent> FINISH_RISE = REGISTRY.register("finish_rise",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "finish_rise")));
        public static final RegistryObject<SoundEvent> PRO_STANDBY_TONE = REGISTRY.register("pro_standby_tone",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "pro_standby_tone")));
        public static final RegistryObject<SoundEvent> PROGRISING_STRASH = REGISTRY.register("progrising_strash",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_weapon_craft", "progrising_strash")));
}
