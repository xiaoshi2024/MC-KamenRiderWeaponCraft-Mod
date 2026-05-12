package com.xiaoshi2022.kamenriderweaponcraft;

import com.mojang.logging.LogUtils;
import com.xiaoshi2022.kamenriderweaponcraft.register.ItemRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(KamenRiderWeaponCraft.MODID)
public class KamenRiderWeaponCraft {
    public static final String MODID = "kamenriderweaponcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KAMEN_RIDER_TAB =
            CREATIVE_MODE_TABS.register("kamen_rider_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kamenriderweaponcraft"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ItemRegister.HEISEISWORD.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ItemRegister.HEISEISWORD.get());
                    }).build());

    public KamenRiderWeaponCraft(IEventBus modEventBus) {
        // 注册物品
        ItemRegister.register(modEventBus);
        // 注册创造模式标签页
        CREATIVE_MODE_TABS.register(modEventBus);

        // 重要：不要在这里添加网络注册的监听！
        // NetworkHandler 已经通过 @EventBusSubscriber 自动注册
    }
}