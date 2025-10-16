package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.food.HelheimFruit.HelheimFruitRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.EffectInit;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import tocraft.walkers.api.PlayerShape;

import java.util.function.Consumer;

public class HelheimFruit extends Item implements GeoItem {

    public static final TagKey<Item> HELHEIM_FOOD_TAG =
            ItemTags.create(new ResourceLocation("kamen_rider_weapon_craft", "kamen_rider_helheim_food"));


    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public HelheimFruit(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
                .nutrition(3) // 饱腹值
                .saturationMod(0.3f) // 饱和度
                .alwaysEat() // 允许在饱腹状态下食用
                .build()));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            // 自定义渲染器
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new HelheimFruitRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "open", 2, state -> PlayState.STOP)
                .triggerableAnim("open", OPEN)
                .setSoundKeyframeHandler(state -> {
                }));
    }

    private PlayState predicate(AnimationState<HelheimFruit> event) {
        // 设置默认动画逻辑
        event.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    // 在玩家食用时播放动画
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel)
            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "open", "open");

        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // 确保只在服务器端执行逻辑
        if (!level.isClientSide && entity instanceof Player player) {
            // 40% 概率变形为异域者（INVES_HEILEHIM）
            if (player.getRandom().nextInt(100) < 40) { // 40% 概率
                player.stopRiding();
                ServerPlayer serverPlayer = (ServerPlayer) player;
                EntityType<?> invesHeilehimType = ModEntityTypes.INVES_HEILEHIM.get();
                if (invesHeilehimType != null) {
                    LivingEntity invesHeilehimEntity = (LivingEntity) invesHeilehimType.create(level);
                    if (invesHeilehimEntity != null) {
                        // 初始化实体并设置位置
                        invesHeilehimEntity.moveTo(player.getX(), player.getY(), player.getZ());
                        if (!invesHeilehimEntity.isAlive()) {
                            System.err.println("INVES_HEILEHIM initialization failed");
                            return stack;
                        }
                        // 调用 PlayerShape.updateShapes 方法进行变形 - 使用反射检查以避免缺少模组时崩溃
                        try {
                            // 动态加载类来检查walkers模组是否存在
                            Class<?> playerShapeClass = Class.forName("tocraft.walkers.api.PlayerShape");
                            // 使用反射调用updateShapes方法
                            java.lang.reflect.Method updateShapesMethod = playerShapeClass.getMethod("updateShapes", ServerPlayer.class, LivingEntity.class);
                            boolean result = (boolean) updateShapesMethod.invoke(null, serverPlayer, invesHeilehimEntity);
                            if (result) {
                                System.out.println("Player transformed to INVES_HEILEHIM successfully");
                            } else {
                                System.err.println("Player transformation failed");
                            }
                        } catch (Exception e) {
                            // 如果walkers模组不存在或发生其他错误，记录日志但不崩溃
                            System.out.println("Walkers mod not available, skipping transformation: " + e.getMessage());
                        }
                    }
                }
            }
            // 在finishUsingItem方法中修改赫尔海姆之力的赋予逻辑
            if (player.getRandom().nextInt(100) < 60) {
                // 随机给予1-3级效果
                int powerLevel = player.getRandom().nextInt(3); // 0=1级, 1=2级, 2=3级
                player.addEffect(new MobEffectInstance(
                        EffectInit.HELMHEIM_POWER.get(),
                        30 * 20, // 30秒
                        powerLevel, // 效果等级
                        false,
                        false
                ));

                // 如果给予的是2级或以上，发送消息
                if (powerLevel >= 1) {
                    player.sendSystemMessage(Component.translatable(
                            "message.kamen_rider_weapon_craft.helmheim_power.level_up",
                            powerLevel + 1 // 显示为2级/3级
                    ));
                }
            }
        }
        // 调用父类方法以确保物品的默认行为（例如消耗物品）
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}