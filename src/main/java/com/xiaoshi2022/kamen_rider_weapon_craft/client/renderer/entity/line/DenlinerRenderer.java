package com.xiaoshi2022.kamen_rider_weapon_craft.client.renderer.entity.line;

import com.xiaoshi2022.kamen_rider_weapon_craft.entity.line.denliner;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DenlinerRenderer extends GeoEntityRenderer<denliner> {
    public DenlinerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DenlinerModel());
        this.shadowRadius = 1.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(denliner instance) {
        return DenlinerModel.LOCATION;
    }

    // Denliner 模型类
    private static class DenlinerModel extends GeoModel<denliner> {
        private static final ResourceLocation MODEL = new ResourceLocation("kamen_rider_weapon_craft", "geo/entity/denliner.geo.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation("kamen_rider_weapon_craft", "textures/entity/denliner.png");
        private static final ResourceLocation ANIMATION = new ResourceLocation("kamen_rider_weapon_craft", "animations/entity/denliner.animation.json");
        public static final ResourceLocation LOCATION = TEXTURE;

        @Override
        public ResourceLocation getModelResource(denliner object) {
            return MODEL;
        }

        @Override
        public ResourceLocation getTextureResource(denliner object) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(denliner object) {
            return ANIMATION;
        }
    }
}