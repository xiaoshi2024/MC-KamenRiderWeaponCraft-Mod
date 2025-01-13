package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.sonicarrow;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.combineds.client.combineds.sonicarrow_melon.sonicarrowMelonModel;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class sonicarrowRenderer extends GeoItemRenderer<sonicarrow> {
    private final sonicarrowModel defaultModel;
    private final sonicarrowMelonModel melonModel;

    public sonicarrowRenderer() {
        super(new sonicarrowModel()); // 默认模型
        this.defaultModel = new sonicarrowModel();
        this.melonModel = new sonicarrowMelonModel();
    }

    @Override
    public GeoModel<sonicarrow> getGeoModel() {
        // 获取当前 ItemStack
        ItemStack stack = getCurrentItemStack();

        if (stack.getItem() instanceof sonicarrow) {
            sonicarrow weapon = (sonicarrow) stack.getItem();
            sonicarrow.Mode mode = weapon.getCurrentMode(stack);

            // 根据模式返回不同的模型
            if (mode == sonicarrow.Mode.MELON) {
                return melonModel; // 返回蜜瓜模式模型
            } else {
                return defaultModel; // 返回默认模式模型
            }
        }

        return super.getGeoModel(); // 默认返回父类的模型
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // 调用父类的渲染逻辑
        super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
    }
}