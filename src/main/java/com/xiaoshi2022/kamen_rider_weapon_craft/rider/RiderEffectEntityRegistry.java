package com.xiaoshi2022.kamen_rider_weapon_craft.rider;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build.BuildRiderEntityRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.drive.DriveRiderEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

/**
 * 骑士特效实体渲染器注册类
 * 用于统一管理所有骑士相关的特效实体渲染器的注册
 */
@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RiderEffectEntityRegistry {
    
    /**
     * 注册骑士特效实体渲染器到Mod事件总线
     * @param modEventBus Mod事件总线
     */
    public static void register(IEventBus modEventBus) {
        // 这个方法会被主类调用，确保这个类被正确加载
        // 由于我们使用了@Mod.EventBusSubscriber注解，不需要手动注册事件监听器
        System.out.println("RiderEffectEntityRegistry registered");
    }

    /**
     * 注册骑士特效实体渲染器
     * @param event 实体渲染器注册事件
     */
    @SubscribeEvent
    public static void registerRiderEffectEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 注册Build骑士特效实体渲染器
        registerBuildRiderRenderers(event);
        
        // 注册Drive骑士特效实体渲染器
        registerDriveRiderRenderers(event);
        
        // 这里可以添加更多骑士特效实体的注册方法
    }

    /**
     * 注册Build骑士相关的特效实体渲染器
     * @param event 实体渲染器注册事件
     */
    private static void registerBuildRiderRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 使用与项目中其他实体一致的注册方式，保持代码风格统一
        event.registerEntityRenderer(ModEntityTypes.BUILD_RIDER_EFFECT.get(), BuildRiderEntityRenderer::new);
    }
    
    /**
     * 注册Drive骑士相关的特效实体渲染器
     * @param event 实体渲染器注册事件
     */
    private static void registerDriveRiderRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 使用与项目中其他实体一致的注册方式，保持代码风格统一
        event.registerEntityRenderer(ModEntityTypes.DRIVE_RIDER_EFFECT.get(), DriveRiderEntityRenderer::new);
    }
}