package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.sonicarrow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.combineds.client.combineds.sonicarrow_melon.sonicarrowCherryModel;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.combineds.client.combineds.sonicarrow_melon.sonicarrowLemonModel;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.combineds.client.combineds.sonicarrow_melon.sonicarrowMelonModel;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.combineds.client.combineds.sonicarrow_melon.sonicarrowPeachModel;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class sonicarrowRenderer extends GeoItemRenderer<sonicarrow> {
    private final GeoModel<sonicarrow> defaultModel;
    private final GeoModel<sonicarrow> melonModel;
    private final GeoModel<sonicarrow> lemonModel;
    private final GeoModel<sonicarrow> cherryModel;
    private final GeoModel<sonicarrow> peachModel;

    public sonicarrowRenderer() {
        super(new sonicarrowModel());
        this.defaultModel = new sonicarrowModel();
        this.melonModel = new sonicarrowMelonModel();
        this.lemonModel = new sonicarrowLemonModel();
        this.cherryModel = new sonicarrowCherryModel();
        this.peachModel = new sonicarrowPeachModel();
    }

    @Override
    public void renderByItem(ItemStack stack,
                             ItemDisplayContext transformType,
                             PoseStack poseStack,
                             MultiBufferSource buffer,
                             int packedLight,
                             int packedOverlay) {

        // 获取当前模式
        sonicarrow.Mode mode = ((sonicarrow)stack.getItem()).getCurrentMode(stack);

        // 根据模式选择模型
        GeoModel<sonicarrow> currentModel = switch(mode) {
            case MELON -> melonModel;
            case LEMON -> lemonModel;
            case CHERRY -> cherryModel;
            case PEACH -> peachModel;
            default -> defaultModel;
        };

        // 使用反射临时修改模型字段
        try {
            java.lang.reflect.Field modelField = GeoItemRenderer.class.getDeclaredField("model");
            modelField.setAccessible(true);
            modelField.set(this, currentModel);

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