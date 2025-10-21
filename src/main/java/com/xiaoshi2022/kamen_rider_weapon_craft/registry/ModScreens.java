package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.gui.RiderFusionMachinesScreen;
import com.xiaoshi2022.kamen_rider_weapon_craft.gui.SonicBowGuiScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModScreens {
    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModContainers.SSONIC.get(), SonicBowGuiScreen::new);
            MenuScreens.register(ModContainers.RIDER_FUSION_MACHINE.get(), RiderFusionMachinesScreen::new);
        });
    }
}
