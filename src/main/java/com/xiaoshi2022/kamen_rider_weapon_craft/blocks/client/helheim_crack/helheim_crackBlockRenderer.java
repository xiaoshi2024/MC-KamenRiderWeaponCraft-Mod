package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.helheim_crack;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.helheim_crackBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class helheim_crackBlockRenderer  extends GeoBlockRenderer<helheim_crackBlockEntity> {
    public helheim_crackBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new helheim_crackModel());
    }
}

