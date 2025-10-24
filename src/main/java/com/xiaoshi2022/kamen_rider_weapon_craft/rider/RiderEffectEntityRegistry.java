package com.xiaoshi2022.kamen_rider_weapon_craft.rider;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid.ExAidSlashEffectRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.builds.BuildRiderRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

/**
 * 骑士特效实体渲染器注册类
 * 用于统一管理所有骑士相关的特效实体渲染器的注册
 * Fabric版本 - 适用于1.21.6
 */
@Environment(EnvType.CLIENT)
public class RiderEffectEntityRegistry {

    /**
     * 注册所有骑士特效实体渲染器
     * 在客户端初始化时调用
     */
    public static void registerRiderEffectEntityRenderers() {
        System.out.println("Registering Rider Effect Entity Renderers for Fabric 1.21.6");

        // 注册Build骑士特效实体渲染器
        registerBuildRiderRenderers();
        registerExaidSlashEffectRenderer();

        // 暂时注释掉其他尚未实现的渲染器注册
        // registerDriveRiderRenderers();
        // registerGaimRiderRenderers();
        // registerWizardRiderRenderers();
        // registerFourzeRiderRenderers();
        // registerOOORiderRenderers();
        // registerWRiderRenderers();
        // registerDecadeRiderRenderers();

        System.out.println("All Rider Effect Entity Renderers registered successfully");
    }

    /**
     * 注册Build骑士相关的特效实体渲染器
     */
    private static void registerBuildRiderRenderers() {
        EntityRendererRegistry.register(ModEntityTypes.BUILD_RIDER_EFFECT,
                (EntityRendererFactory.Context context) -> new BuildRiderRenderer(context));
    }

    private static void registerExaidSlashEffectRenderer() {
        EntityRendererRegistry.register(ModEntityTypes.EXAID_SLASH_EFFECT,
                (EntityRendererFactory.Context context) -> new ExAidSlashEffectRenderer(context));
    }

    // 暂时注释掉其他尚未实现的渲染器注册方法
    // 其他方法暂时被注释，避免编译错误
    // private static void registerDriveRiderRenderers() { ... }
    // private static void registerGaimRiderRenderers() { ... }
    // 其他方法类似处理
}