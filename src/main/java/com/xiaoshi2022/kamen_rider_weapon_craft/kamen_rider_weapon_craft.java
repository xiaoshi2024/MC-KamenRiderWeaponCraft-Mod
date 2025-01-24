package com.xiaoshi2022.kamen_rider_weapon_craft;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.prop.arrowx.AonicxEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.items.AonicxItem;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.CloseMapPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.LockseedManager;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.*;
import com.xiaoshi2022.kamen_rider_weapon_craft.tab.ModTab;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import software.bernie.geckolib.GeckoLib;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.openjdk.nashorn.internal.runtime.regexp.joni.constants.OPSize.INDEX;


@Mod("kamen_rider_weapon_craft")
@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft")
public class kamen_rider_weapon_craft {
    public static final String MOD_ID = "kamen_rider_weapon_craft";
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, MOD_ID),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public kamen_rider_weapon_craft() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModSounds.REGISTRY.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModTab.TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        GeckoLib.initialize();

        // 初始化音速箭
        AonicxItem.init(modEventBus);
        AonicxEntity.init(modEventBus);

        // 初始化容器
        ModContainers.REGISTRY.register(modEventBus);

        // 初始化MODEntityR
        ModentityR.ENTITIES(modEventBus);

        // 初始化Mixin系统
        MixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
        MixinEnvironment.getEnvironment(MixinEnvironment.Phase.DEFAULT);

        // 注册网络包
        registerNetworkMessages();
    }

    private void registerNetworkMessages() {
        int id = 0;
        PACKET_HANDLER.registerMessage(id++, LockseedManager.class, LockseedManager::buffer, LockseedManager::new, LockseedManager::handler);
        PACKET_HANDLER.registerMessage(
                INDEX,
                CloseMapPacket.class,
                CloseMapPacket::encode,
                CloseMapPacket::decode,
                CloseMapPacket::handle
        );
    }

    // 任务队列
    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
            workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
            workQueue.forEach(work -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() == 0) {
                    actions.add(work);
                }
            });
            actions.forEach(e -> e.getKey().run());
            workQueue.removeAll(actions);
        }
    }
}