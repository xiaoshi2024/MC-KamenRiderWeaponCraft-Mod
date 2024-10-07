package com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.melon.MelonRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.example.registry.SoundRegistry;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class Melon extends Item implements GeoItem {
    private static final RawAnimation START = RawAnimation.begin().thenPlay("start");
    private static final RawAnimation COMBINE = RawAnimation.begin().thenPlay("combine");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public Melon(net.minecraft.world.item.Item.Properties properties) {
        super(properties);

        // Register our item as server-side handled.
        // This enables both animation data syncing and server-side animation triggering
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private MelonRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new MelonRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel)
            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "start", "start");

        return super.use(level, player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "start", 20, state -> PlayState.STOP)
                .triggerableAnim("start", START)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                    // Use helper method to avoid client-code in common class
                    Player player = ClientUtils.getClientPlayer();

                    if (player != null)
                        player.playSound(SoundRegistry.JACK_MUSIC.get(), 1, 1);
                }));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
