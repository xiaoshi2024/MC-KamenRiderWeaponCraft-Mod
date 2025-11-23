package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.Faiz;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Faiz空集符号渲染器
 */
public class FaizEmptySetRenderer extends GeoEntityRenderer<FaizEmptySetEntity> {
    
    public FaizEmptySetRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FaizEmptySetGeoModel());
        
        // 设置渲染器属性
        this.shadowRadius = 0.1f; // 阴影半径
        this.shadowStrength = 0.3f; // 阴影强度
        
        // 可以在这里设置其他渲染器参数，比如是否受光照影响
    }
    
    @Override
    public void render(FaizEmptySetEntity entity, float entityYaw, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight) {
        // 可以在渲染前进行一些特殊处理
        
        // 如果实体正在出现或消失，可以添加特殊效果
        if (entity.isAppearing() || entity.isDisappearing()) {
            // 增强发光效果的代码可以在这里添加
        }
        
        // 调用父类的渲染方法
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
    
    @Override
    protected float getDeathMaxRotation(FaizEmptySetEntity entityLivingBaseIn) {
        // 死亡旋转角度，对于特效实体保持为0
        return 0.0F;
    }
    
    @Override
    public ResourceLocation getTextureLocation(FaizEmptySetEntity entity) {
        // 直接使用Geo模型中定义的纹理
        return super.getTextureLocation(entity);
    }
}