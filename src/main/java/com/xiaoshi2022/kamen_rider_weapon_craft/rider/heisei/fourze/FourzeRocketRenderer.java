package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.fourze;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Fourze火箭炮的渲染器类
 * 用于在游戏中渲染火箭炮实体
 */
public class FourzeRocketRenderer extends GeoEntityRenderer<FourzeRocketEntity> {
    private int currentTick = -1;

    public FourzeRocketRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FourzeRocketModel());
        // 设置阴影半径
        this.shadowRadius = 0.3f;
    }

    @Override
    protected float getDeathMaxRotation(FourzeRocketEntity entityLivingBaseIn) {
        // 爆炸时不需要死亡旋转
        return 0.0F;
    }

    @Override
    public void renderFinal(PoseStack poseStack, FourzeRocketEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer,
                            float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        if (this.currentTick < 0 || this.currentTick != animatable.tickCount) {
            this.currentTick = animatable.tickCount;

            // 在火箭喷射口位置生成火焰粒子
            this.model.getBone("befires").ifPresent(bone -> {
                RandomSource rand = animatable.level().getRandom();
                Vector3d bonePos = bone.getWorldPosition();

                // 从喷射口沿着实体Y轴向下喷出火焰粒子
                // 使用固定的向下方向，只在X和Z轴添加微小的随机偏移以增加效果
                for (int i = 0; i < 3; i++) {
                    // X和Z轴添加微小随机偏移，Y轴保持向下
                    double offsetX = (rand.nextDouble() - 0.5D) * 0.1D;
                    double offsetZ = (rand.nextDouble() - 0.5D) * 0.1D;
                    
                    // 确保粒子速度主要沿Y轴向下
                    double motionX = 0;
                    double motionY = -0.5D - rand.nextDouble() * 0.3D; // 强向下速度
                    double motionZ = 0;
                    
                    animatable.getCommandSenderWorld().addParticle(ParticleTypes.FLAME,
                            bonePos.x + offsetX,
                            bonePos.y - 0.1D,
                            bonePos.z + offsetZ,
                            motionX,
                            motionY,
                            motionZ);
                }
            });
        }

        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}