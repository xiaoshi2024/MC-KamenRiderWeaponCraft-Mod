package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.SonicBowContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModContainers {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, kamen_rider_weapon_craft.MOD_ID);

    public static final RegistryObject<MenuType<SonicBowContainer>> SONIC_BOW_CONTAINER = CONTAINERS.register("sonic_bow_container", () -> IForgeMenuType.create(SonicBowContainer::new));

    public static void register(IEventBus modEventBus) {
        CONTAINERS.register(modEventBus);
    }
}
