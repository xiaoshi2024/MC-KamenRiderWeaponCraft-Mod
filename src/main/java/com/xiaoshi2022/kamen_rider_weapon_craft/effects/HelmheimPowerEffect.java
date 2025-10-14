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
            // 检查玩家是否处于变形状态且效果等级≥2
            if (PlayerShape.getCurrentShape(player) != null && amplifier >= 1) {
                // 变回人类形态 - 使用updateShapes方法并传入null
                PlayerShape.updateShapes(player, null);

                // 发送消息通知玩家
                player.sendSystemMessage(Component.translatable(
                        "message.kamen_rider_weapon_craft.helmheim_power.revert"
                ));
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每10秒检查一次（减少性能开销）
        return duration % 200 == 0;
    }
}