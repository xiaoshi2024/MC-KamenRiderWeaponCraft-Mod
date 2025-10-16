package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods.foodeffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tocraft.walkers.api.PlayerShape;

import java.util.Random;

public final class HelheimEffects {
    private static final Random RNG = new Random();

    /** 40 % 概率把玩家变成 INVES_HEILEHIM */
    public static void tryTransformToInves(Player player) {
        if (!player.level().isClientSide && player.getRandom().nextInt(100) < 40) {
            player.stopRiding();
            if (player instanceof ServerPlayer sp) {
                EntityType<?> invesType = null;
            try {
                Class<?> modEntityTypesClass = Class.forName("com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes");
                Object field = modEntityTypesClass.getDeclaredField("INVES_HEILEHIM").get(null);
                java.lang.reflect.Method getMethod = field.getClass().getMethod("get");
                invesType = (EntityType<?>) getMethod.invoke(field);
            } catch (Exception e) {
                System.out.println("Failed to access ModEntityTypes.INVES_HEILEHIM: " + e.getMessage());
            }
                if (invesType != null) {
                    LivingEntity inves = (LivingEntity) invesType.create(sp.level());
                    if (inves != null) {
                        inves.moveTo(sp.getX(), sp.getY(), sp.getZ());
                        if (!inves.isAlive()) {
                            System.err.println("INVES_HEILEHIM init failed");
                            return;
                        }
                        // 使用反射检查walkers模组是否存在
                        try {
                            Class<?> playerShapeClass = Class.forName("tocraft.walkers.api.PlayerShape");
                            java.lang.reflect.Method updateShapesMethod = playerShapeClass.getMethod("updateShapes", ServerPlayer.class, LivingEntity.class);
                            boolean result = (boolean) updateShapesMethod.invoke(null, sp, inves);
                            
                            if (result) {
                                // 成功
                            } else {
                                inves.discard();
                            }
                        } catch (Exception e) {
                            // 如果walkers模组不存在，跳过变身
                            System.out.println("Walkers mod not available, skipping transformation in HelheimEffects");
                            inves.discard();
                        }
                    }
                }
            }
        }
    }

    /** 给异域者增加战斗 Buff */
    public static void boostInves(LivingEntity inves) {
        inves.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1, false, true));
        inves.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0, false, true));
        inves.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0, false, true));

        ((ServerLevel) inves.level()).sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                inves.getX(), inves.getY() + 1, inves.getZ(),
                8, 0.5, 0.5, 0.5, 0);
    }

    /** 给异域者形态的玩家随机战斗 Buff */
    public static void randomCombatBuff(Player player) {
        MobEffect[] effects = { MobEffects.DAMAGE_BOOST, MobEffects.DAMAGE_RESISTANCE, MobEffects.MOVEMENT_SPEED };
        MobEffect chosen = effects[RNG.nextInt(effects.length)];
        int amplifier = RNG.nextInt(2); // 0 或 1
        player.addEffect(new MobEffectInstance(chosen, 300, amplifier, false, true));
    }
}