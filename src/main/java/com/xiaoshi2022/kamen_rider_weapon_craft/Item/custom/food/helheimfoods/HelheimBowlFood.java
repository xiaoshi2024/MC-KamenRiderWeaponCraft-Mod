package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods;
// 移除直接导入boss模组类，改用反射安全检查
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods.foodeffects.HelheimEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
        if (!player.level().isClientSide) {
            // 使用反射安全地检查boss模组中的实体类型
            try {
                // 动态加载ModEntityTypes类
                Class<?> modEntityTypesClass = Class.forName("com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes");
                // 获取INVES_HEILEHIM静态字段
                java.lang.reflect.Field invesHeilehimField = modEntityTypesClass.getDeclaredField("INVES_HEILEHIM");
                invesHeilehimField.setAccessible(true);
                // 获取RegistryObject对象
                Object registryObject = invesHeilehimField.get(null);
                // 调用get()方法获取EntityType
                java.lang.reflect.Method getMethod = registryObject.getClass().getMethod("get");
                EntityType<?> invesHeilehimType = (EntityType<?>) getMethod.invoke(registryObject);
                
                // 检查目标实体类型
                if (invesHeilehimType != null && target.getType() == invesHeilehimType) {
                    HelheimEffects.boostInves(target);
                    stack.shrink(1);
                    return InteractionResult.CONSUME;
                }
            } catch (ClassNotFoundException e) {
                // 如果boss模组不存在，忽略这个功能但不崩溃
                System.out.println("Boss mod not available, skipping INVES_HEILEHIM interaction");
            } catch (Exception e) {
                // 记录其他错误的详细信息
                System.out.println("Error during INVES_HEILEHIM interaction: " + e.getMessage());
            }
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
                    LivingEntity inves = (LivingEntity) invesType.create(level);
                    if (inves != null) {
                        inves.moveTo(player.getX(), player.getY(), player.getZ());
                        // 使用反射检查walkers模组是否存在
                    try {
                        Class<?> playerShapeClass = Class.forName("tocraft.walkers.api.PlayerShape");
                        java.lang.reflect.Method updateShapesMethod = playerShapeClass.getMethod("updateShapes", ServerPlayer.class, LivingEntity.class);
                        boolean result = (boolean) updateShapesMethod.invoke(null, player, inves);
                        
                        if (result) {
                            HelheimEffects.randomCombatBuff(player);
                        } else {
                            inves.discard();
                        }
                    } catch (Exception e) {
                        // 如果walkers模组不存在，跳过变身但仍然给予buff
                        System.out.println("Walkers mod not available, skipping transformation but applying buffs");
                        HelheimEffects.randomCombatBuff(player);
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