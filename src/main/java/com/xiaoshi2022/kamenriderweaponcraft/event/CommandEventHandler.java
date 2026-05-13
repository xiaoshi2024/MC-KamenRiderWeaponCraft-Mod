package com.xiaoshi2022.kamenriderweaponcraft.event;

import com.xiaoshi2022.kamenriderweaponcraft.command.SummonWithHeiseiswordCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft.MODID;

/**
 * 命令注册事件处理器
 */
@EventBusSubscriber(modid = MODID)
public class CommandEventHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SummonWithHeiseiswordCommand.register(event.getDispatcher());
    }
}