package com.xiaoshi2022.kamen_rider_weapon_craft.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Photonic子弹的渲染器类
 * 用于在游戏中渲染Photonic子弹实体
 */
public class PhotonicRenderer extends GeoEntityRenderer<PhotonicEntity> {
    private int currentTick = -1;

    public PhotonicRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PhotonicModel());
        // 设置阴影半径
        this.shadowRadius = 0.3f;
    }

    @Override
    protected float getDeathMaxRotation(PhotonicEntity entityLivingBaseIn) {
        // 爆炸时不需要死亡旋转
        return 0.0F;
    }

    @Override
    protected void applyRotations(PhotonicEntity entityLiving, PoseStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
        
        // 检查是否为地面光弹
        if (entityLiving.isGroundBased) {
            // 地面光弹缩放为3倍
            matrixStackIn.scale(3.0f, 3.0f, 3.0f);
        } else {
            // 普通光弹缩放为1.5倍
            matrixStackIn.scale(1.5f, 1.5f, 1.5f);
        }
    }

    @Override
    public void renderFinal(PoseStack poseStack, PhotonicEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer,
                            float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        if (this.currentTick < 0 || this.currentTick != animatable.tickCount) {
            this.currentTick = animatable.tickCount;

            // 在子弹位置生成粒子
            RandomSource rand = animatable.level().getRandom();
            Vector3d entityPos = new Vector3d(animatable.getX(), animatable.getY(), animatable.getZ());

            // 生成电气火花粒子
            for (int i = 0; i < 3; i++) {
                // X和Z轴添加微小随机偏移
                double offsetX = (rand.nextDouble() - 0.5D) * 0.1D;
                double offsetZ = (rand.nextDouble() - 0.5D) * 0.1D;
                
                animatable.getCommandSenderWorld().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        entityPos.x + offsetX,
                        entityPos.y,
                        entityPos.z + offsetZ,
                        0,
                        0,
                        0);
            }
        }

        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}