package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.KachidokiBata.KachidokiBataRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
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
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.function.Consumer;

public class KachidokiBata extends SwordItem implements GeoItem, ICurioItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("charge");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public KachidokiBata() {
        super(new Tier() {
            public int getUses() {
                return 1500; // 武器的耐久度
            }

            public float getSpeed() {
                return 1f; // 武器的攻击速度
            }

            public float getAttackDamageBonus() {
                return 35f; // 武器的额外攻击伤害
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
        }, 4, 2.4f, new Properties());
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private KachidokiBataRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new KachidokiBataRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Idle动画控制器 - 持续播放
        controllers.add(new AnimationController<>(this, "idle", 20, event -> {
            event.getController().setAnimation(IDLE);
            return PlayState.CONTINUE;
        }));
        
        // Charge动画控制器 - 设置为可触发模式
        controllers.add(new AnimationController<>(this, "charge", 0, state -> {
            // 默认返回STOP，但可以通过triggerAnim触发
            return PlayState.STOP;
        }).triggerableAnim("charge", CHARGE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // 实现ICurioItem接口的canEquip方法，允许在背饰槽中装备
        return true;
    }
    
    // 确保武器功能正常，添加右键使用逻辑
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // 保持原有的动画触发功能
        if (level instanceof ServerLevel serverLevel)
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "charge", "charge");
            
        // 添加使用物品的逻辑
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
}