package com.xiaoshi2022.kamen_rider_weapon_craft.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import tocraft.walkers.api.PlayerShape;

public class HelmheimPowerEffect extends BaseEffect {
    public HelmheimPowerEffect(MobEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 只在服务端执行，且当实体是玩家时
        if (!entity.level().isClientSide && entity instanceof ServerPlayer player) {
            // 使用反射检查walkers模组是否存在
            try {
                // 动态加载PlayerShape类
                Class<?> playerShapeClass = Class.forName("tocraft.walkers.api.PlayerShape");
                
                // 检查玩家是否处于变形状态且效果等级≥2
                java.lang.reflect.Method getCurrentShapeMethod = playerShapeClass.getMethod("getCurrentShape", ServerPlayer.class);
                Object currentShape = getCurrentShapeMethod.invoke(null, player);
                
                if (currentShape != null && amplifier >= 1) {
                    // 变回人类形态 - 使用updateShapes方法并传入null
                    java.lang.reflect.Method updateShapesMethod = playerShapeClass.getMethod("updateShapes", ServerPlayer.class, LivingEntity.class);
                    updateShapesMethod.invoke(null, player, (Object)null);

                    // 发送消息通知玩家
                    player.sendSystemMessage(Component.translatable(
                            "message.kamen_rider_weapon_craft.helmheim_power.revert"
                    ));
                }
            } catch (Exception e) {
                // 如果walkers模组不存在，忽略这个功能但不崩溃
                System.out.println("Walkers mod not available, skipping Helmheim power effect transformation logic");
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每10秒检查一次（减少性能开销）
        return duration % 200 == 0;
    }
}