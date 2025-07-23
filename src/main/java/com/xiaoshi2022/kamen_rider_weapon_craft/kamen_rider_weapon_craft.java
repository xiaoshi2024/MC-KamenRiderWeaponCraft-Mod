package com.xiaoshi2022.kamen_rider_weapon_craft;

import com.xiaoshi2022.kamen_rider_weapon_craft.event.LivingHurtHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.event.WitherSpawnHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.procedures.KRWBoot;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.*;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.procedures.PullSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.procedures.PullSoundsClient;
import com.xiaoshi2022.kamen_rider_weapon_craft.recipe.ModRecipes;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.*;
import com.xiaoshi2022.kamen_rider_weapon_craft.tab.ModTab;
import com.xiaoshi2022.kamen_rider_weapon_craft.villagers.TimeTravelerProfession;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.ModFoliagePlacers;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.ModTrunkPlacerTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
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
        ModParticles.REGISTRY.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        GeckoLib.initialize();

        MinecraftForge.EVENT_BUS.register(LivingHurtHandler.class);

        MinecraftForge.EVENT_BUS.register(WitherSpawnHandler.class);

        /// 注册配方
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(PullSounds.class);
        MinecraftForge.EVENT_BUS.register(KRWBoot.class);
        MinecraftForge.EVENT_BUS.register(PullSoundsClient.class);

        //树苗的注册
        ModFoliagePlacers.register(modEventBus);
        ModTrunkPlacerTypes.register(modEventBus);

        // 自定义村民职业
        TimeTravelerProfession.POI_TYPE.register(modEventBus);
        TimeTravelerProfession.PROFESSION.register(modEventBus);

        // 初始化容器
        ModContainers.REGISTRY.register(modEventBus);

        // 初始化 MODEntity
        ModEntityTypes.ENTITIES.register(modEventBus);

        // 初始化 Mixin 系统
        MixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
        MixinEnvironment.getEnvironment(MixinEnvironment.Phase.DEFAULT);



        // 注册网络包
        registerNetworkMessages();

        // 注册 NetworkHandler
        NetworkHandler.register();

        EffectInit.EFFECTS.register(modEventBus);
    }

//    private void commonSetup(final FMLCommonSetupEvent event) {
//        event.enqueueWork(() -> {
//            // 注册地表规则
//            SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MOD_ID, ModSurfaceRules.makeRules());
//        });
//    }


    private void registerNetworkMessages() {
        int id = 0;
        PACKET_HANDLER.registerMessage(id++, LockseedManager.class, LockseedManager::buffer, LockseedManager::new, LockseedManager::handler);
        PACKET_HANDLER.registerMessage(
                id++,
                CloseMapPacket.class,
                CloseMapPacket::encode,
                CloseMapPacket::decode,
                CloseMapPacket::handle
        );
        // 注册 SeverSound 数据包
//        PACKET_HANDLER.registerMessage(
//                id++,
//                ServerSound.class,
//                ServerSound::encode,
//                ServerSound::decode,
//                ServerSound::handle
//        );
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
            // 处理任务队列
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