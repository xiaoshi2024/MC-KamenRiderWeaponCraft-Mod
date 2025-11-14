package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.KachidokiBataBlock;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.custom.KachidokiBataBlock;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class KachidokiBataBlockRenderer extends GeoBlockRenderer<KachidokiBataBlock.KachidokiBataBlockEntity> {
    
    public KachidokiBataBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new KachidokiBataBlockModel());
    }
    
    // 方块模型类
    public static class KachidokiBataBlockModel extends GeoModel<KachidokiBataBlock.KachidokiBataBlockEntity> {
        @Override
        public ResourceLocation getModelResource(KachidokiBataBlock.KachidokiBataBlockEntity animatable) {
            return new ResourceLocation("kamen_rider_weapon_craft", "geo/block/kachidoki_bata.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(KachidokiBataBlock.KachidokiBataBlockEntity animatable) {
            return new ResourceLocation("kamen_rider_weapon_craft", "textures/block/kachidoki_bata.png");
        }

        @Override
        public ResourceLocation getAnimationResource(KachidokiBataBlock.KachidokiBataBlockEntity animatable) {
            return new ResourceLocation("kamen_rider_weapon_craft", "animations/block/kachidoki_bata.animation.json");
        }
    }
}