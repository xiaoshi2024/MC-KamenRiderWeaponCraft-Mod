package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.Time_traveler_studio_block;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.Time_traveler_studio_blockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class Time_traveler_studio_blockRenderer  extends GeoBlockRenderer<Time_traveler_studio_blockEntity> {
    public Time_traveler_studio_blockRenderer(BlockEntityRendererProvider.Context context) {
        super(new Time_traveler_studio_blockModel());
    }
}

