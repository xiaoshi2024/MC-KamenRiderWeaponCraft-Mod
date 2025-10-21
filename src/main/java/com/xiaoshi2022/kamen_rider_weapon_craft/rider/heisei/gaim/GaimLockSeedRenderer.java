package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.gaim;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

/**
 * 铠武锁种渲染器类
 * 负责渲染锁种特效实体
 */
public class GaimLockSeedRenderer extends GeoEntityRenderer<GaimLockSeedEntity> {

    public GaimLockSeedRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GaimLockSeedModel());
        this.shadowRadius = 0.0F; // 特效不需要阴影
    }

    @Override
    protected void applyRotations(GaimLockSeedEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        // 不调用super.applyRotations，这样我们可以完全控制旋转
    }

    @Override
    public void render(GaimLockSeedEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 设置足够的光照值，确保特效清晰可见
        int lightValue = 15728880; // 最大光照值
        
        // 在渲染前应用旋转，确保特效朝向正确的方向
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        
        // 调整特效大小
        poseStack.scale(1.0F, 1.0F, 1.0F);
        
        // 调用父类的render方法进行实际渲染
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, lightValue);
        
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GaimLockSeedEntity entity) {
        // 根据锁种类型动态返回纹理位置
        String lockSeedType = entity.getLockSeedType().toLowerCase();
        return new ResourceLocation(MOD_ID, "textures/rider/gaim/" + lockSeedType + ".png");
    }
}