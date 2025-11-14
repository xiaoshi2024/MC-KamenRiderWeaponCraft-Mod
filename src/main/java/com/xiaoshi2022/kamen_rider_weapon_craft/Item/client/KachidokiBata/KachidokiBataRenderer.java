package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.KachidokiBata;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.KachidokiBata;
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
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class KachidokiBataRenderer extends GeoItemRenderer<KachidokiBata> implements ICurioRenderer {
    public KachidokiBataRenderer() {
        super(new KachidokiBataModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"kachidoki_bata")));
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int i, float v, float v1, float v2, float v3, float v4, float v5) {
        poseStack.pushPose();
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            HumanoidModel<LivingEntity> model = (HumanoidModel<LivingEntity>) humanoidModel;

            // 将模型移动到身体位置（背部位置）
            model.body.translateAndRotate(poseStack);

            // 微调位置：背部中央略靠上，旗帜杆位置
            poseStack.translate(0.0F, 0.2F, 0.2F); // X右+  Y下+  Z前+
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0F)); // 旗帜朝向正后方
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(0F)); // 旗帜垂直悬挂
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-60F));

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
                    ItemDisplayContext.FIXED, // 使用FIXED显示模式
                    false,
                    poseStack,
                    multiBufferSource,
                    i
            );
        }
        poseStack.popPose();
    }
}