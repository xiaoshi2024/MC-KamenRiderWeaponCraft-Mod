package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.needlekunai.NeedleKunaiRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.needlekunai.entity.ThrownNeedleKunai;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
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

public class NeedleKunai extends SwordItem implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation THROW = RawAnimation.begin().thenPlay("throw");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public NeedleKunai() {
        super(new Tier() {
            @Override public int getUses()               { return 300; }          // 耐久
            @Override public float getSpeed()            { return 3.5f; }         // 攻速
            @Override public float getAttackDamageBonus(){ return 15.0f; }        // 额外伤害
            @Override public int getLevel()              { return 3; }            // 等级
            @Override public int getEnchantmentValue()   { return 15; }           // 附魔值
            @Override public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.RIDER_FORGING_ALLOY_ORE.get()); } // 修复材料
        }, 3, -2.0f, new Item.Properties());

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private NeedleKunaiRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new NeedleKunaiRenderer();
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "idle_controller", 20, state -> {
                    state.setAnimation(IDLE);
                    return PlayState.CONTINUE;
                })
        );
        controllers.add(
                new AnimationController<>(this, "throw_controller", 20, state -> {
                    state.setAnimation(THROW);
                    return PlayState.CONTINUE;
                })
        );
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "throw_controller", "throw");
        }

        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            ThrownNeedleKunai thrownNeedleKunai = new ThrownNeedleKunai(level, player, stack);
            thrownNeedleKunai.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 1.0F);
            level.addFreshEntity(thrownNeedleKunai);

            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, (stack1) -> {
                    stack1.broadcastBreakEvent(hand);
                });
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            level.playSound(null, player, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}