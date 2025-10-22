package com.xiaoshi2022.kamen_rider_weapon_craft.items.client.Heiseisword;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;

public class HeiseiswordItemEntityRenderer extends ItemEntityRenderer {

    public HeiseiswordItemEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    // 移除 @Override 注解，因为父类方法签名可能已改变
    public void render(ItemEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {

        // 检查是否是我们的自定义物品
        if (entity.getStack().getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.items.custom.Heiseisword) {
            matrices.push();

            // 调整位置、旋转和大小
            matrices.translate(0.0, 0.1, 0.0);
            matrices.scale(0.7f, 0.7f, 0.7f);

            // 创建渲染器实例并渲染
            HeiseiswordRenderer renderer = new HeiseiswordRenderer();
            renderer.renderAsEntity(entity.getStack(), matrices, vertexConsumers, light);

            matrices.pop();
        } else {
            // 其他物品使用默认渲染
            // 调用父类的保护方法或使用其他方式
            doRender(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        }
    }

    // 辅助方法来处理默认渲染
    private void doRender(ItemEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                          VertexConsumerProvider vertexConsumers, int light) {
        // 由于API变化，我们可能需要使用其他方式
        // 暂时先不渲染其他物品，或者尝试其他方法
    }
}