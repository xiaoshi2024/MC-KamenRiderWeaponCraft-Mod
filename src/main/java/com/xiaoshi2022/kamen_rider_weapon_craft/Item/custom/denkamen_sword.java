package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.denkamen_sword.denkamen_swordRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

// 注册事件监听器
@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft")

public class denkamen_sword extends SwordItem implements GeoItem {
    private static final RawAnimation FROTATION = RawAnimation.begin().thenPlay("frotation");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public denkamen_sword() {
        super(new Tier() {
            public int getUses() {
                return 1500; // 武器的耐久度
            }

            public float getSpeed() {
                return -1.0f; // 武器的攻击速度
            }

            public float getAttackDamageBonus() {
                return 30f; // 武器的额外攻击伤害
            }

            public int getLevel() {
                return 4;
            }

            public int getEnchantmentValue() {
                return 8;
            }

            public Ingredient getRepairIngredient() {
                return Ingredient.of(); // 修复材料
            }
        }, 3, -2.4f, new Item.Properties());
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new denkamen_swordRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, state -> {
            // 默认播放空闲状态或根据使用状态播放格挡动画
            if (isBlocking(state.getAnimatable().getDefaultInstance())) {
                return state.setAndContinue(FROTATION);
            }
            return PlayState.STOP;
        })
                .triggerableAnim("frotation", FROTATION)
                .setSoundKeyframeHandler(state -> {
                }));
    }

    // 辅助方法：检查是否正在格挡
    private boolean isBlocking(ItemStack itemStack) {
        // 这里需要根据你的逻辑判断是否正在格挡
        // 简单实现：检查物品是否正在被使用
        return itemStack != null && itemStack.getUseDuration() > 0;
    }

    // 重写 use 方法，处理右键格挡
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 在服务器端触发格挡动画
            triggerAnim(player, GeoItem.getOrAssignId(itemstack, (ServerLevel) level), "controller", "frotation");

            // 设置使用持续时间，让玩家保持格挡姿势
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }

        return InteractionResultHolder.success(itemstack);
    }

    // 重写 getUseDuration 方法，定义格挡的持续时间
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // 一个很大的值，使得玩家可以持续格挡
    }

    // 重写这个方法可以让物品在使用时显示为格挡姿势
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK; // 使用原版的格挡动画姿势
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 检查物品是否是电假面剑
    public static boolean isDenkamenSword(ItemStack stack) {
        return stack.getItem() instanceof denkamen_sword;
    }

    // 监听伤害事件，实现格挡减伤功能
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        Entity attacker = source.getDirectEntity();

        // 检查目标是否是玩家并且正在使用电假面剑格挡
        if (target instanceof Player player && player.isUsingItem()) {
            ItemStack stack = player.getItemInHand(player.getUsedItemHand());
            if (isDenkamenSword(stack) && player.getUseItemRemainingTicks() > 0) {
                // 如果伤害来源是近战攻击（有直接实体攻击者）
                if (attacker instanceof LivingEntity && (source.getMsgId().equals("player") || source.getMsgId().equals("mob"))) {
                    // 完全减免近战伤害
                    event.setAmount(0.0F);
                    
                    // 播放格挡音效
                    target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 0.8F + target.level().random.nextFloat() * 0.4F);
                    
                    // 消耗武器耐久度
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            }
        }
    }
}