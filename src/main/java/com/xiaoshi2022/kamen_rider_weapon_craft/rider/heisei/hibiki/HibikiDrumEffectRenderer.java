package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.hibiki;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 响鬼鼓特效渲染器
 */
public class HibikiDrumEffectRenderer extends GeoEntityRenderer<HibikiDrumEffectEntity> {
    
    public HibikiDrumEffectRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HibikiDrumEffectModel());
        
        // 设置渲染器属性
        this.shadowRadius = 0.5f; // 阴影半径
        this.shadowStrength = 0.5f; // 阴影强度
        
        // 可以在这里设置其他渲染器参数，比如是否受光照影响
        // 对于特效实体，通常可以禁用光照影响以保持亮度
    }
    
    @Override
    public void render(HibikiDrumEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight) {
        // 可以在渲染前进行一些特殊处理
        
        // 如果实体正在蓄力或爆炸，可以添加发光效果
        boolean enhanceGlow = entity.isCharging() || entity.hasExploded();
        
        if (enhanceGlow) {
            // 增强发光效果的代码
            // 这里可以通过修改poseStack或其他方式来实现特殊的视觉效果
        }
        
        // 调用父类的渲染方法
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        
        // 渲染后处理
    }
    
    @Override
    protected float getDeathMaxRotation(HibikiDrumEffectEntity entityLivingBaseIn) {
        // 死亡旋转角度，对于特效实体可以保持为0
        return 0.0F;
    }
    
    @Override
    public ResourceLocation getTextureLocation(HibikiDrumEffectEntity entity) {
        // 如果需要根据实体状态切换不同的纹理，可以在这里处理
        // 否则直接使用模型中的默认纹理
        return super.getTextureLocation(entity);
    }
    
    // 为了支持客户端渲染器获取实体状态，需要在HibikiDrumEffectEntity中添加对应的getter方法
    // 这些方法将在客户端使用，从同步的数据中获取状态
}