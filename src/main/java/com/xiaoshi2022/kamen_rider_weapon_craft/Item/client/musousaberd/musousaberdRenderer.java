package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.musousaberd;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.musousaberd;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class musousaberdRenderer extends GeoItemRenderer<musousaberd> implements ICurioRenderer {
    public musousaberdRenderer() {
        super(new musousabersModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"musou_saber")));
//        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int i, float v, float v1, float v2, float v3, float v4, float v5) {
        poseStack.pushPose();
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            HumanoidModel<LivingEntity> model = (HumanoidModel<LivingEntity>) humanoidModel;

            // 将模型移动到身体位置（胸甲位置）
            model.body.translateAndRotate(poseStack);

            // 微调位置：腰间左侧
            poseStack.translate(0.3F, 0.8F, 0.2F); // X右+  Y下+  Z前+
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90F)); // 刀柄朝前
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(15F)); // 微倾斜
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90F)); // 斜

            // 缩放
            poseStack.scale(0.8F, 0.8F, 0.8F);

            // 渲染物品
            ItemInHandRenderer renderer = new ItemInHandRenderer(
                    Minecraft.getInstance(),
                    Minecraft.getInstance().getEntityRenderDispatcher(),
                    Minecraft.getInstance().getItemRenderer()
            );
            renderer.renderItem(
                    slotContext.entity(),
                    itemStack,
                    ItemDisplayContext.FIXED, // 使用FIXED或THIRD_PERSON_RIGHT_HAND
                    false,
                    poseStack,
                    multiBufferSource,
                    i
            );
        }
        poseStack.popPose();
    }
}

