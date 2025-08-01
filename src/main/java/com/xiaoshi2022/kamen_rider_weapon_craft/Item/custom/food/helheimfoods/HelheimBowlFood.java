package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods.foodeffects.HelheimEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import tocraft.walkers.api.PlayerShape;

import java.util.Objects;


public class HelheimBowlFood extends BowlFoodItem {
    // 静态标签常量
    public static final TagKey<Item> HELHEIM_FOOD_TAG =
            ItemTags.create(new ResourceLocation("kamen_rider_weapon_craft", "kamen_rider_helheim_food"));

    public HelheimBowlFood(int nutrition, boolean alwaysEat) {
        super(new Properties()
                .stacksTo(16)
                .food(new FoodProperties.Builder()
                        .nutrition(nutrition)
                        .saturationMod(0.3f)
                        .alwaysEat()
                        .build()));
    }

    // 关键修改：手持时自动激活
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide &&
                entity instanceof Player player &&
                (isSelected || player.getOffhandItem() == stack)) {
            // 自动标记为吸引物品
            stack.getOrCreateTag().putBoolean("HelheimAttract", true);
        }
    }

    // 简化右键交互（直接喂食）
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide &&
                target.getType() == ModEntityTypes.INVES_HEILEHIM.get()) {
            HelheimEffects.boostInves(target);
            stack.shrink(1);
            return InteractionResult.CONSUME;
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    /* 玩家食用逻辑 */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // 先处理饱食度效果（调用Item类的实现）
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            super.finishUsingItem(stack, level, entity);
        } else {
            // 非创造模式下应用食物效果并消耗物品
            entity.eat(level, stack);
        }

        // 处理变身和buff逻辑
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            // 40% 概率变身
            if (player.getRandom().nextInt(100) < 40) {
                player.stopRiding();
                EntityType<?> invesType = ModEntityTypes.INVES_HEILEHIM.get();
                if (invesType != null) {
                    LivingEntity inves = (LivingEntity) invesType.create(level);
                    if (inves != null) {
                        inves.moveTo(player.getX(), player.getY(), player.getZ());
                        if (PlayerShape.updateShapes(player, inves)) {
                            HelheimEffects.randomCombatBuff(player);
                        } else {
                            inves.discard();
                        }
                    }
                }
            } else {
                HelheimEffects.randomCombatBuff(player);
            }
        }

        // 处理碗的返回逻辑
        if (stack.getCount() > 1) {
            ItemStack newStack = stack.copy();
            newStack.shrink(1);

            // 给玩家添加空碗
            if (entity instanceof Player player) {
                if (!player.getInventory().add(new ItemStack(Items.BOWL))) {
                    player.drop(new ItemStack(Items.BOWL), false);
                }
            }
            return newStack;
        }

        return new ItemStack(Items.BOWL);
    }
}