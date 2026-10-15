package com.xiaoshi2022.kamenriderweaponcraft.register;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.world.inventory.SonicBowMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuRegister {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, KamenRiderWeaponCraft.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SonicBowMenu>> SONIC_BOW =
            MENUS.register("sonic_bow", () -> IMenuTypeExtension.create(SonicBowMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
