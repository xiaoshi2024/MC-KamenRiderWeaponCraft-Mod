package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods.foodeffects.HelheimEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tocraft.walkers.api.PlayerShape;

public class HelheimSpecialFood extends Item {

    public HelheimSpecialFood(int nutrition, boolean alwaysEat) {
        super(new Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(nutrition)
                        .saturationMod(0.3f)
                        .alwaysEat()
                        .build()));
    }

    /* 右键喂食异域者 */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack,
                                                  Player player,
                                                  LivingEntity target,
                                                  InteractionHand hand) {
        if (!player.level().isClientSide
                && target.getType() == ModEntityTypes.INVES_HEILEHIM.get()) {
            HelheimEffects.boostInves(target);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    /* 玩家食用逻辑 */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            // 40 % 概率变身
            if (player.getRandom().nextInt(100) < 40) {
                player.stopRiding();
                EntityType<?> invesType = ModEntityTypes.INVES_HEILEHIM.get();
                if (invesType != null) {
                    LivingEntity inves = (LivingEntity) invesType.create(level);
                    if (inves != null) {
                        inves.moveTo(player.getX(), player.getY(), player.getZ());
                        if (PlayerShape.updateShapes(player, inves)) {
                            /* 变身成功 → 给玩家（现在是异域者）加 Buff */
                            HelheimEffects.randomCombatBuff(player);
                        } else {
                            inves.discard();
                        }
                    }
                }
            } else {
                /* 未变身 → 直接给玩家加 Buff */
                HelheimEffects.randomCombatBuff(player);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}