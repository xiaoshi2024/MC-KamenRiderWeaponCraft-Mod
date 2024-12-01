package com.xiaoshi2022.kamen_rider_weapon_craft;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.prop.arrowx.AonicxEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.items.AonicxItem;
import com.xiaoshi2022.kamen_rider_weapon_craft.gui.SonicBowGuiScreen;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModContainers;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModentityR;
import com.xiaoshi2022.kamen_rider_weapon_craft.tab.ModTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import software.bernie.geckolib.GeckoLib;



@Mod("kamen_rider_weapon_craft")
@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft")
public class kamen_rider_weapon_craft {
    public static final String MOD_ID = "kamen_rider_weapon_craft";

    public kamen_rider_weapon_craft(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);

        ModTab.TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        GeckoLib.initialize();

        //初始化音速箭
        AonicxItem.init(modEventBus);
        AonicxEntity.init(modEventBus);

        ModContainers.register(modEventBus); // 注册MenuType

        //初始化MODEntityR
        ModentityR.ENTITIES(modEventBus);
        // 初始化Mixin系统
        MixinBootstrap.init();

        // 配置Mixin环境，这里设置为CLIENT并处于DEFAULT阶段
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
        MixinEnvironment.getEnvironment(MixinEnvironment.Phase.DEFAULT);
    }
}