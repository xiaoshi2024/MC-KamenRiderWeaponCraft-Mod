package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ExAidSlashEffectRenderer <R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<ExAidSlashEffectEntity, R> {

    public ExAidSlashEffectRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new ExAidSlashEffectModel());
        this.shadowRadius = 0.0F; // 特效不需要阴影
    }
}