package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.sound.RiderSounds;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final RegistryKey<Registry<SoundEvent>> SOUND_EVENTS_KEY = RegistryKey.ofRegistry(
            Identifier.of("kamen_rider_weapon_craft", "sound_events")
    );

    public static final Registry<SoundEvent> SOUND_EVENTS = FabricRegistryBuilder.createSimple(SOUND_EVENTS_KEY).buildAndRegister();

    public static void initialize() {
        // 初始化所有音效
        RiderSounds.registerSounds();
    }
}