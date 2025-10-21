package com.xiaoshi2022.kamen_rider_weapon_craft.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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
                // 使用Player参数类型而不是ServerPlayer
                java.lang.reflect.Method getCurrentShapeMethod = playerShapeClass.getMethod("getCurrentShape", Player.class);
                Object currentShape = getCurrentShapeMethod.invoke(null, player);
                
                if (currentShape != null && amplifier >= 1) {
                    System.out.println("[HelmheimPower] Player is transformed and has high enough power level, reverting to human");
                    
                    // 变回人类形态 - 使用updateShapes方法并传入null
                    java.lang.reflect.Method updateShapesMethod = playerShapeClass.getMethod("updateShapes", ServerPlayer.class, LivingEntity.class);
                    boolean result = (boolean) updateShapesMethod.invoke(null, player, (Object)null);
                    
                    if (result) {
                        System.out.println("[HelmheimPower] Successfully reverted to human form");
                        // 发送消息通知玩家
                        player.sendSystemMessage(Component.translatable(
                                "message.kamen_rider_weapon_craft.helmheim_power.revert"
                        ));
                    } else {
                        System.out.println("[HelmheimPower] Failed to revert to human form");
                    }
                }
            } catch (ClassNotFoundException e) {
                // 如果walkers模组不存在，忽略这个功能但不崩溃
                System.out.println("Walkers mod not available, skipping Helmheim power effect transformation logic");
            } catch (Exception e) {
                // 记录其他错误的详细信息
                System.out.println("Error during Helmheim power effect transformation logic: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每5秒检查一次（增加检查频率以提高响应性）
        return duration % 100 == 0;
    }
}