package com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK;

import com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK.weapon_mapx.weapon_mapRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.function.Consumer;

// 假设这里的 GeoItem 是自定义接口，包含了相关的动画方法
public class weapon_map extends Item implements GeoItem {
    // 定义动画实例缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    // 定义一些动画常量
    private static final RawAnimation OPEN_ANIMATION = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation CLOSE_ANIMATION = RawAnimation.begin().thenPlay("close");

    public weapon_map(Properties properties) {
        super(new Item.Properties());
        // 注册为可同步动画的对象
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // 初始化客户端，设置自定义渲染器
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private weapon_mapRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new weapon_mapRenderer();
                }
                return this.renderer;
            }
        });
    }

    // 当玩家使用物品时的处理逻辑
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);


        // 确保物品有 NBT 标签
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        // 标记物品已打开
        stack.getTag().putBoolean("opened", true);


        // 如果是服务器世界实例，触发动画
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "open", "open");
        }


        // 检查是否为服务器端，因为一些 GUI 操作需要在服务器端触发
        if (!level.isClientSide()) {
            PatchouliAPI.get().openBookGUI((ServerPlayer) player, new ResourceLocation("kamen_rider_weapon_craft:weapon_map"));
        }


        return InteractionResultHolder.success(stack);
    }


    // 注册动画控制器
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "open", 20, state -> PlayState.CONTINUE)
                .triggerableAnim("open", OPEN_ANIMATION)
                // 标记动画可由服务器触发
                .setSoundKeyframeHandler(state -> {
                }));
        controllers.add(new AnimationController<>(this, "close", 20, state -> PlayState.CONTINUE)
                .triggerableAnim("close", CLOSE_ANIMATION)
                // 标记动画可由服务器触发
                .setSoundKeyframeHandler(state -> {
                }));
    }

    // 获取动画实例缓存
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}


