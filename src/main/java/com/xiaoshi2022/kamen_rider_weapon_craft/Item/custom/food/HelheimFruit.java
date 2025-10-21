package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food;

// 移除直接导入boss模组类，改用反射安全检查
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
            // 检查是否有walkers模组，先尝试进化为人类的逻辑
            try {
                Class<?> playerShapeClass = Class.forName("tocraft.walkers.api.PlayerShape");
                ServerPlayer serverPlayer = (ServerPlayer) player;
                
                // 检查玩家是否已经变形
                java.lang.reflect.Method getCurrentShapeMethod = playerShapeClass.getMethod("getCurrentShape", Player.class);
                LivingEntity currentShape = (LivingEntity) getCurrentShapeMethod.invoke(null, player);
                
                // 如果玩家已经变形，检查是否可以进化为人类
                if (currentShape != null) {
                    System.out.println("[HelheimFruit] Player is transformed, checking Helmheim Power level");
                    
                    // 检查赫尔海姆之力等级
                    int helmheimPowerLevel = -1;
                    for (MobEffectInstance effect : player.getActiveEffects()) {
                        if (effect.getEffect() == EffectInit.HELMHEIM_POWER.get()) {
                            helmheimPowerLevel = effect.getAmplifier();
                            System.out.println("[HelheimFruit] Detected Helmheim Power level: " + (helmheimPowerLevel + 1));
                            break;
                        }
                    }
                    
                    // 如果赫尔海姆之力等级大于1（即2级或3级），进化为人类
                    if (helmheimPowerLevel >= 1) {
                        System.out.println("[HelheimFruit] High enough power level, evolving to human");
                        
                        // 根据PlayerShape源码，使用updateShapes方法传入null来清除变形
                        java.lang.reflect.Method updateShapesMethod = playerShapeClass.getMethod("updateShapes", ServerPlayer.class, LivingEntity.class);
                        boolean result = (boolean) updateShapesMethod.invoke(null, serverPlayer, null);
                        
                        if (result) {
                            System.out.println("[HelheimFruit] Successfully evolved to human form");
                            // 发送进化消息
                            player.sendSystemMessage(Component.translatable("message.kamen_rider_weapon_craft.helmheim_power.evolved_to_human"));
                            
                            // 给予玩家强化的赫尔海姆之力效果作为奖励
                            player.addEffect(new MobEffectInstance(
                                    EffectInit.HELMHEIM_POWER.get(),
                                    120 * 20, // 120秒
                                    helmheimPowerLevel, // 保持原等级
                                    false,
                                    false
                            ));
                        } else {
                            System.out.println("[HelheimFruit] Failed to evolve to human form");
                        }
                        
                        // 无论是否进化成功，都继续下面的逻辑
                    }
                }
            } catch (ClassNotFoundException e) {
                System.out.println("[HelheimFruit] Walkers mod not available, skipping human evolution check");
            } catch (Exception e) {
                System.out.println("[HelheimFruit] Error during human evolution check: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 40% 概率变形为异域者（INVES_HEILEHIM）
            if (player.getRandom().nextInt(100) < 40) { // 40% 概率
                player.stopRiding();
                ServerPlayer serverPlayer = (ServerPlayer) player;
                
                // 使用反射安全地检查boss模组和创建实体
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
                                // 使用updateShapes方法进行变形
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
                } catch (ClassNotFoundException e) {
                    // 如果boss模组不存在，忽略这个功能但不崩溃
                    System.out.println("Boss mod not available, skipping INVES_HEILEHIM transformation");
                } catch (Exception e) {
                    // 记录其他错误的详细信息
                    System.out.println("Error during INVES_HEILEHIM transformation: " + e.getMessage());
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