package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods.foodeffects.HelheimEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class HelheimJuiceBottle extends Item {
    public HelheimJuiceBottle() {
        super(new Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(1).saturationMod(0.2f).alwaysEat().build())
                .craftRemainder(Items.GLASS_BOTTLE).stacksTo(16));
    }

    @NotNull
    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        /* 1. 父类处理：减少数量并返回剩余物（玻璃瓶） */
        ItemStack result = super.finishUsingItem(stack, level, entity);

        /* 2. 创造模式不消耗主物品，也不给剩余物 */
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return result;
        }

        /* 3. 普通玩家：把玻璃瓶塞进背包或掉地上 */
        if (entity instanceof Player player) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }

            // 口渴兼容 & 40% 变身
            tryAddThirst(player);
            if (player.getRandom().nextInt(100) < 40) {
                HelheimEffects.tryTransformToInves(player);
            }
        }

        return result;
    }

    /* ===================== 私有工具方法 ===================== */
    private static void tryAddThirst(Player player) {
        // 1) Tough As Nails
        try {
            Class<?> thirstHelper = Class.forName("toughasnails.thirst.ThirstHelper");
            Method addStats = thirstHelper.getMethod("addStats", ServerPlayer.class, int.class);
            addStats.invoke(null, (ServerPlayer) player, 2);
        } catch (Exception ignore) {}

        // 2) Thirst Was Taken
        try {
            Class<?> thirstCapability = Class.forName("net.mcreator.thirstmod.capability.ThirstCapability");
            Method addHydration = thirstCapability.getMethod("addHydration", Player.class, int.class);
            addHydration.invoke(null, player, 2);
        } catch (Exception ignore) {}
    }

}