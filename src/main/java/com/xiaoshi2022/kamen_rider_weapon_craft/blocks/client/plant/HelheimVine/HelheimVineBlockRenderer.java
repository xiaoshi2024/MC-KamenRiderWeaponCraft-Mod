package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.plant.HelheimVine;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.plant.HelheimVineBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class HelheimVineBlockRenderer extends GeoBlockRenderer<HelheimVineBlockEntity> {
    public HelheimVineBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new HelheimVineBlockModel());
    }
}