//package com.xiaoshi2022.kamen_rider_weapon_craft.rider;
//
//import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
//import net.fabricmc.api.EnvType;
//import net.fabricmc.api.Environment;
//import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
//import net.minecraft.client.render.entity.EntityRendererFactory;
//
///**
// * 骑士特效实体渲染器注册类
// * 用于统一管理所有骑士相关的特效实体渲染器的注册
// * Fabric版本 - 适用于1.21.6
// */
//@Environment(EnvType.CLIENT)
//public class RiderEffectEntityRegistry {
//
//    /**
//     * 注册所有骑士特效实体渲染器
//     * 在客户端初始化时调用
//     */
//    public static void registerRiderEffectEntityRenderers() {
//        System.out.println("Registering Rider Effect Entity Renderers for Fabric 1.21.6");
//
//        // 注册Build骑士特效实体渲染器
//        registerBuildRiderRenderers();
//
//        // 注册Drive骑士特效实体渲染器
//        registerDriveRiderRenderers();
//
//        // 注册铠武锁种特效实体渲染器
//        registerGaimRiderRenderers();
//
//        // 注册Wizard骑士特效实体渲染器
//        registerWizardRiderRenderers();
//
//        // 注册Fourze火箭炮实体渲染器
//        registerFourzeRiderRenderers();
//
//        // 注册OOO细胞硬币斩实体渲染器
//        registerOOORiderRenderers();
//
//        // 注册W骑士龙卷风实体渲染器
//        registerWRiderRenderers();
//
//        // 注册Decade骑士次元踢实体渲染器
//        registerDecadeRiderRenderers();
//
//        System.out.println("All Rider Effect Entity Renderers registered successfully");
//    }
//
//    /**
//     * 注册Build骑士相关的特效实体渲染器
//     */
//    private static void registerBuildRiderRenderers() {
//        EntityRendererRegistry.register(ModEntityTypes.BUILD_RIDER_EFFECT,
//                (EntityRendererFactory.Context context) -> new BuildRiderEntityRenderer(context));
//    }
//
//    /**
//     * 注册Drive骑士相关的特效实体渲染器
//     */
//    private static void registerDriveRiderRenderers() {
//        EntityRendererRegistry.register(ModEntityTypes.DRIVE_RIDER_EFFECT,
//                (EntityRendererFactory.Context context) -> new DriveRiderEntityRenderer(context));
//    }
//
//    /**
//     * 注册铠武骑士相关的特效实体渲染器
//     */
//    private static void registerGaimRiderRenderers() {
//        EntityRendererRegistry.register(ModEntityTypes.GAIM_LOCK_SEED,
//                (EntityRendererFactory.Context context) -> new GaimLockSeedRenderer(context));
//    }
//
//    /**
//     * 注册Wizard骑士相关的特效实体渲染器
//     */
//    private static void registerWizardRiderRenderers() {
//        EntityRendererRegistry.register(ModEntityTypes.WIZARD_EFFECT,
//                (EntityRendererFactory.Context context) -> new WizardRiderEntityRenderer(context));
//    }
//
//    /**
//     * 注册Fourze骑士相关的特效实体渲染器
//     */
//    private static void registerFourzeRiderRenderers() {
//        EntityRendererRegistry.register(ModEntityTypes.FOURZE_ROCKET,
//                (EntityRendererFactory.Context context) -> new FourzeRocketRenderer(context));
//    }
//
//    /**
//     * 注册OOO骑士相关的特效实体渲染器
//     */
//    private static void registerOOORiderRenderers() {
//        EntityRendererRegistry.register(ModEntityTypes.OOO_GEO_EFFECT,
//                (EntityRendererFactory.Context context) -> new OOOGeoEntityRenderer(context));
//    }
//
//    /**
//     * 注册W骑士相关的特效实体渲染器
//     */
//    private static void registerWRiderRenderers() {
//        EntityRendererRegistry.register(ModEntityTypes.W_TORNADO,
//                (EntityRendererFactory.Context context) -> new WTornadoRenderer(context));
//    }
//
//    /**
//     * 注册Decade骑士相关的特效实体渲染器
//     */
//    private static void registerDecadeRiderRenderers() {
//        EntityRendererRegistry.register(ModEntityTypes.DECADE_RIDER,
//                (EntityRendererFactory.Context context) -> new DecadeRiderEntityRenderer(context));
//    }
//}