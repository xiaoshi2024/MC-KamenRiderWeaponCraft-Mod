package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.breakamnuster;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.breakamnuster.BreakamnusterGunRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class BreakamnusterGun extends SwordItem implements GeoItem {
    // 动画定义
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation USE_ANIM = RawAnimation.begin().thenPlay("use");
    private static final RawAnimation GO_ANIM = RawAnimation.begin().thenPlay("go");
    private static final RawAnimation UP_ANIM = RawAnimation.begin().thenPlay("up");
    
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BreakamnusterGun(Properties properties) {
        super(new Tier() {
            @Override
            public int getUses() {
                return 1000; // 武器耐久度
            }

            @Override
            public float getSpeed() {
                return 1.0f; // 攻击速度
            }

            @Override
            public float getAttackDamageBonus() {
                return 8.0f; // 额外攻击伤害
            }

            @Override
            public int getLevel() {
                return 5; // 武器等级
            }

            @Override
            public int getEnchantmentValue() {
                return 10; // 附魔值
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(); // 修理材料
            }
        }, 4, -2.4f, properties);
        
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // 动画控制器
    private PlayState predicate(AnimationState<BreakamnusterGun> event) {
        // 循环播放idle动画
        event.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 客户端渲染器注册
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            private BreakamnusterGunRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new BreakamnusterGunRenderer();
                }
                return this.renderer;
            }
        });
    }

    // 远程攻击相关方法
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW; // 使用弓的动画
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // 最大使用时间
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
}
