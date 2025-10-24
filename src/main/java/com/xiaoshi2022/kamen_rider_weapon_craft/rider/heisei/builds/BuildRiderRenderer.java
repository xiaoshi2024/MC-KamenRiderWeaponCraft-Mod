package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.builds;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * Kamen Rider Build 实体渲染器 - GeckoLib 5 版本 for Fabric 1.21.8
 */
public class BuildRiderRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<BuildRiderEntity, R> {
    public BuildRiderRenderer(EntityRendererFactory.Context context) {
        super(context, new BuildRiderModel());
    }
}