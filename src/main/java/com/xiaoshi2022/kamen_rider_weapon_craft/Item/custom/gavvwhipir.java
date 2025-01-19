package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.gavvwhipir.gavvwhipirRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class gavvwhipir extends SwordItem implements GeoItem {
    // 定义一个简单的动画
    private static final RawAnimation EXTRUDING = RawAnimation.begin().thenPlay("extruding");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public gavvwhipir() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 350; // 武器的耐久度
            }

            @Override
            public float getSpeed() {
                return 3.5f; // 武器的攻击速度
            }

            @Override
            public float getAttackDamageBonus() {
                return 12.4f; // 武器的额外攻击伤害
            }

            @Override
            public int getLevel() {
                return 3; // 武器的等级
            }

            @Override
            public int getEnchantmentValue() {
                return 1; // 武器的附魔价值
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(); // 修复材料
            }
        }, 3, -2.2f, new Item.Properties());

        // 注册为服务器端处理的物品，启用动画数据同步和服务器端动画触发
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // 初始化客户端渲染器
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private gavvwhipirRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new gavvwhipirRenderer();
                }
                return this.renderer;
            }
        });
    }

    // 注册动画控制器
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "extruding", 20, state -> PlayState.CONTINUE)
                .triggerableAnim("extruding", EXTRUDING)
                // 标记动画可由服务器触发
                .setSoundKeyframeHandler(state -> {
                }));
    }
    // 处理物品使用方法，右键点击时触发动画
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "extruding", "extruding");
        }
        return super.use(level, player, hand);
    }

    // 获取动画实例缓存
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}