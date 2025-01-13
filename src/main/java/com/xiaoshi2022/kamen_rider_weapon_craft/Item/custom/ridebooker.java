package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.ridebooker.ridebookerRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
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

import java.util.List;
import java.util.function.Consumer;

public class ridebooker extends SwordItem implements GeoItem {
    private static final RawAnimation TAKE = RawAnimation.begin().thenPlay("take");
    private static final RawAnimation SABRE = RawAnimation.begin().thenPlay("sabre");
    private static final RawAnimation CEASE = RawAnimation.begin().thenPlay("cease");
    private static final RawAnimation SABRER = RawAnimation.begin().thenPlay("sabrer");
    private static final RawAnimation SABRERX = RawAnimation.begin().thenPlay("sabrerx");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ridebooker(Tier p_43269_, int p_43270_, float p_43271_, Properties p_43272_) {
        super(p_43269_, p_43270_, p_43271_, p_43272_);

        // Register our item as server-side handled.
        // This enables both animation data syncing and server-side animation triggering
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ridebookerRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new ridebookerRenderer();

                return this.renderer;
            }
        });
    }

    // Let's add our animation controller
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "sabre", 20, state -> PlayState.STOP)
                .triggerableAnim("sabre", SABRE)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                }));
        controllers.add(new AnimationController<>(this, "take", 20, state -> PlayState.STOP)
                .triggerableAnim("take", TAKE)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                }));
        controllers.add(new AnimationController<>(this, "cease", 20, state -> PlayState.STOP)
                .triggerableAnim("cease", CEASE)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                }));
        controllers.add(new AnimationController<>(this, "sabrer", 20, state -> PlayState.STOP)
                .triggerableAnim("sabrer", SABRER)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                }));
        controllers.add(new AnimationController<>(this, "sabrerx", 20, state -> PlayState.STOP)
                .triggerableAnim("sabrerx", SABRERX)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                }));
    }

    // Let's handle our use method so that we activate the animation when right-clicking while holding the box
    @Override
    public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            CompoundTag tag = player.getItemInHand(hand).getTag();
            tag.putFloat("close", tag.getFloat("close") == 1 ? 0 : 1);
            if (level instanceof ServerLevel serverLevel) {
                triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "sabre", "sabre");//首播放
            if (level instanceof ServerLevel) {
                    triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "sabrerx", "sabrerx");//同步首播放,次播放（又和首播放同时播放）
                }
            }
        }
            return super.use(level, player, hand);
        }
    //添加"close“标签
    @Override
    public void inventoryTick(ItemStack itemStack,Level level, Entity entity, int i,boolean b){
        if (! itemStack.hasTag()){
            itemStack.setTag(new CompoundTag());
        }
        if(itemStack.getTag().isEmpty()){
            itemStack.getTag().putFloat("close",0);
        }
        super.inventoryTick(itemStack,level,entity,i,b);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public int getEnchantmentValue() {
        return 12;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, world, list, flag);
        list.add(Component.literal("ridebooker decade"));
    }
}
