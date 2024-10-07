package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.OpenLockseedGuiMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, kamen_rider_weapon_craft.MOD_ID);
    public static final RegistryObject<MenuType<OpenLockseedGuiMenu>> OPEN_LOCKSEED_GUI = REGISTRY.register("open_lockseed_gui", () -> IForgeMenuType.create(OpenLockseedGuiMenu::new));
}
