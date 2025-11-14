package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.KachidokiBataBlock;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.KachidokiBataBlockItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KachidokiBataBlockItemModel extends GeoModel<KachidokiBataBlockItem> {
    @Override
    public ResourceLocation getModelResource(KachidokiBataBlockItem animatable) {
        return  new ResourceLocation("kamen_rider_weapon_craft", "geo/block/kachidoki_bata.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KachidokiBataBlockItem animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/block/kachidoki_bata.png");
    }


    @Override
    public ResourceLocation getAnimationResource(KachidokiBataBlockItem animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/block/kachidoki_bata.animation.json");
    }
}
