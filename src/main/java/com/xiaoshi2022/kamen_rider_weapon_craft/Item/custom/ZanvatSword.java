package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.ZanvatSword.ZanvatSwordRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
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

public class ZanvatSword extends SwordItem implements GeoItem {
    private static final RawAnimation BLOOD = RawAnimation.begin().thenPlayAndHold("blood");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ZanvatSword() {
        super(new Tier() {
            public int getUses() {
                return 900; // 武器的耐久度
            }

            public float getSpeed() {
                return 1f; // 武器的攻击速度
            }

            public float getAttackDamageBonus() {
                return 38f; // 武器的额外攻击伤害
            }

            public int getLevel() {
                return 5;
            }

            public int getEnchantmentValue() {
                return 3;
            }

            public Ingredient getRepairIngredient() {
                return Ingredient.of();
            }
        }, 4, 2.4f, new Item.Properties());
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
                    this.renderer = new ZanvatSwordRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "blood", 20, state -> PlayState.STOP)
                .triggerableAnim("blood", BLOOD)
                .setSoundKeyframeHandler(state -> {
                }));
    }

    // 重写 use 方法，处理武器使用时的动画触发
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel)
            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "blood", "blood");

        return super.use(level, player, hand);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
