package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.kaizoku_hassyar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.KaizokuHassyar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class KaizokuHassyarRenderer extends GeoItemRenderer<KaizokuHassyar> {
    private final GeoModel<KaizokuHassyar> defaultModel;

    public KaizokuHassyarRenderer() {
        super(new KaizokuHassyarModel());
        this.defaultModel = new KaizokuHassyarModel();
    }

    @Override
    public void renderByItem(ItemStack stack,
                             ItemDisplayContext transformType,
                             PoseStack poseStack,
                             MultiBufferSource buffer,
                             int packedLight,
                             int packedOverlay) {

        // 使用默认模型渲染
        try {
            java.lang.reflect.Field modelField = GeoItemRenderer.class.getDeclaredField("model");
            modelField.setAccessible(true);
            modelField.set(this, defaultModel);

            // 调用父类渲染
            super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);

            // 恢复默认模型
            modelField.set(this, defaultModel);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果反射失败，使用默认模型渲染
            super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
        }
    }
}