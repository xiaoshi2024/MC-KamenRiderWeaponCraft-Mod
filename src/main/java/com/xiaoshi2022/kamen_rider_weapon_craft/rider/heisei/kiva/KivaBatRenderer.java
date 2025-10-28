package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kiva;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Kamen Rider Kiva 蝙蝠渲染器类
 * 负责渲染蝙蝠群中的单个蝙蝠实体
 */
public class KivaBatRenderer extends GeoEntityRenderer<KivaBatEntity> {
    
    public KivaBatRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KivaBatModel());
        
        // 设置蝙蝠的阴影半径，较小的阴影使蝙蝠看起来更自然
        this.shadowRadius = 0.2f;
        
        // 可以在这里设置其他渲染参数，如缩放、发光等
        // 例如：如果需要蝙蝠发光效果，可以启用发光
        // this.setRenderLayer(animatable -> 
        //     GeoRenderType.eyes(new ResourceLocation(MOD_ID, "textures/rider/kiva/kiva_bat_eyes.png"))
        // );
    }
    
    // 可以重写其他渲染方法来自定义渲染行为
    // 例如：设置特殊的发光效果、环境光遮蔽等
}