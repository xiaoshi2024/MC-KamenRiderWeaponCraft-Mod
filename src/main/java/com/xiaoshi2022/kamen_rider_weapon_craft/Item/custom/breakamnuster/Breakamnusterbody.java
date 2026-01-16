package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.breakamnuster;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.breakamnuster.BreakamnusterbodyRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
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

public class Breakamnusterbody extends Item implements GeoItem {
    // 动画定义 - 使用通用的动画文件
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation USE_ANIM = RawAnimation.begin().thenPlay("use");
    private static final RawAnimation GO_ANIM = RawAnimation.begin().thenPlay("go");
    private static final RawAnimation UP_ANIM = RawAnimation.begin().thenPlay("up");
    
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Breakamnusterbody(Properties properties) {
        super(properties);
        
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // 动画控制器
    private PlayState predicate(AnimationState<Breakamnusterbody> event) {
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
            private BreakamnusterbodyRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new BreakamnusterbodyRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
}
