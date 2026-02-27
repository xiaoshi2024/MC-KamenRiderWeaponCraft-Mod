package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.breakamnuster;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.breakamnuster.BreakamnusterSwordRenderer;
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

public class BreakamnusterSword extends SwordItem implements GeoItem {
    // 动画定义 - 使用通用的动画文件
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation USE_ANIM = RawAnimation.begin().thenPlay("use");
    private static final RawAnimation GO_ANIM = RawAnimation.begin().thenPlay("go");
    private static final RawAnimation UP_ANIM = RawAnimation.begin().thenPlay("up");
    
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 基础伤害值
    private static float baseDamage = 18.0f;
    
    // 公开API - 设置基础伤害
    public static void setBaseDamage(float damage) {
        baseDamage = damage;
    }
    
    // 公开API - 获取当前基础伤害
    public static float getBaseDamage() {
        return baseDamage;
    }

    public BreakamnusterSword(Properties properties) {
        super(new Tier() {
            @Override
            public int getUses() {
                return 1500; // 武器耐久度
            }

            @Override
            public float getSpeed() {
                return 1.8f; // 攻击速度 - 剑的攻击速度较快
            }

            @Override
            public float getAttackDamageBonus() {
                return baseDamage; // 额外攻击伤害 - 使用基础伤害值
            }

            @Override
            public int getLevel() {
                return 5; // 武器等级
            }

            @Override
            public int getEnchantmentValue() {
                return 15; // 附魔值
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(); // 修理材料
            }
        }, 3, -2.4f, properties);
        
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // 动画控制器
    private PlayState predicate(AnimationState<BreakamnusterSword> event) {
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
            private BreakamnusterSwordRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new BreakamnusterSwordRenderer();
                }
                return this.renderer;
            }
        });
    }

    // 远程攻击相关方法
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR; // 使用矛的动画
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // 最大使用时间
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        /* -------- Shift + 右键：切换到枪模式 -------- */
        if (player.isShiftKeyDown() && !player.isUsingItem()) {
            if (!level.isClientSide) {
                // 创建新的枪形态武器
                ItemStack gunStack = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.BREAKAMNUSTER_GUN.get(), 1);
                
                // 复制耐久度等重要属性
                if (stack.isDamageableItem()) {
                    gunStack.setDamageValue(stack.getDamageValue());
                }
                
                // 复制NBT数据
                if (stack.hasTag()) {
                    net.minecraft.nbt.CompoundTag tag = stack.getTag().copy();
                    gunStack.setTag(tag);
                }
                
                // 替换物品
                player.setItemInHand(hand, gunStack);
                
                // 播放切换音效
                level.playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.PISTON_EXTEND, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.1F);
                level.playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.IRON_DOOR_CLOSE, net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 0.8F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        
        // 正常使用：剑攻击
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
}
