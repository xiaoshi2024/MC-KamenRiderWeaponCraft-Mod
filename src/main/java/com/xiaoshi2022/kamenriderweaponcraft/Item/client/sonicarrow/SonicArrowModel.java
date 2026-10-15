package com.xiaoshi2022.kamenriderweaponcraft.Item.client.sonicarrow;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.SonicArrowItem;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 音速弓 GeckoLib 模型（按形态返回对应资源）
 * 资产命名沿用 1.20.1：
 * - DEFAULT/ORANGE: sonicarrow(.geo.json/.animation.json)，ORANGE 贴图 sonic_arrow_orange.png
 * - MELON: sonicarrow-melon
 * - LEMON/CHERRY/PEACH/DRAGON: sonic_arrow_<name>
 */
public class SonicArrowModel extends GeoModel<SonicArrowItem> {
    private final SonicArrowItem.Mode mode;

    public SonicArrowModel(SonicArrowItem.Mode mode) {
        this.mode = mode;
    }

    private String baseName() {
        return switch (mode) {
            case MELON -> "sonicarrow-melon";
            case LEMON -> "sonic_arrow_lemon";
            case CHERRY -> "sonic_arrow_cherry";
            case PEACH -> "sonic_arrow_peach";
            case DRAGON -> "sonic_arrow_dragon";
            default -> "sonicarrow";
        };
    }

    @Override
    public ResourceLocation getModelResource(SonicArrowItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID,
                "geo/item/" + baseName() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SonicArrowItem animatable) {
        String texture = mode == SonicArrowItem.Mode.ORANGE
                ? "sonic_arrow_orange"
                : baseName();
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID,
                "textures/item/" + texture + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(SonicArrowItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID,
                "animations/item/" + baseName() + ".animation.json");
    }
}
