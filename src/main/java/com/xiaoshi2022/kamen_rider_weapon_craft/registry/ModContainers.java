package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.RiderFusionMachineContainer;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.SonicBowContainer;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.MenuType;


public class ModContainers {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, kamen_rider_weapon_craft.MOD_ID);
    public static final RegistryObject<MenuType<SonicBowContainer>> SSONIC = REGISTRY.register("ssonic", () -> IForgeMenuType.create(SonicBowContainer::new));
    // 注册 Rider Fusion Machine 容器
    public static final RegistryObject<MenuType<RiderFusionMachineContainer>> RIDER_FUSION_MACHINE = REGISTRY.register("rider_fusion_machine", () -> IForgeMenuType.create(RiderFusionMachineContainer::new));
}
