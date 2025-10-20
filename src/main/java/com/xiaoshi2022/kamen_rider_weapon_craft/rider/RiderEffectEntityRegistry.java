package com.xiaoshi2022.kamen_rider_weapon_craft.rider;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build.BuildRiderEntityRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.drive.DriveRiderEntityRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.fourze.FourzeRocketRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ooo.OOOGeoEntityRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.gaim.GaimLockSeedRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.wizard.WizardRiderEntityRenderer;
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
        
        // 注册铠武锁种特效实体渲染器
        registerGaimRiderRenderers(event);
        
        // 注册Wizard骑士特效实体渲染器
        registerWizardRiderRenderers(event);
        
        // 注册Fourze火箭炮实体渲染器
        registerFourzeRiderRenderers(event);
        
        // 注册OOO细胞硬币斩实体渲染器
        registerOOORiderRenderers(event);
        
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
    
    /**
     * 注册铠武骑士相关的特效实体渲染器
     * @param event 实体渲染器注册事件
     */
    private static void registerGaimRiderRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 使用与项目中其他实体一致的注册方式，保持代码风格统一
        event.registerEntityRenderer(ModEntityTypes.GAIM_LOCK_SEED.get(), GaimLockSeedRenderer::new);
    }
    
    /**
     * 注册Wizard骑士相关的特效实体渲染器
     * @param event 实体渲染器注册事件
     */
    private static void registerWizardRiderRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 使用与项目中其他实体一致的注册方式，保持代码风格统一
        event.registerEntityRenderer(ModEntityTypes.WIZARD_EFFECT.get(), WizardRiderEntityRenderer::new);
    }
    
    /**
     * 注册Fourze骑士相关的特效实体渲染器
     * @param event 实体渲染器注册事件
     */
    private static void registerFourzeRiderRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 使用与项目中其他实体一致的注册方式，保持代码风格统一
        event.registerEntityRenderer(ModEntityTypes.FOURZE_ROCKET.get(), FourzeRocketRenderer::new);
    }
    
    /**
     * 注册OOO骑士相关的特效实体渲染器
     * @param event 实体渲染器注册事件
     */
    private static void registerOOORiderRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 使用与项目中其他实体一致的注册方式，保持代码风格统一
        event.registerEntityRenderer(ModEntityTypes.OOO_GEO_EFFECT.get(), OOOGeoEntityRenderer::new);
    }
}