package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.sonicarrow.sonicarrowRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.prop.arrowx.AonicxEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.items.AonicxItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
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
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Example {@link GeoItem} in the form of a "shootable" pistol.
 * @see sonicarrowRenderer
 */
public class sonicarrow extends SwordItem implements GeoItem {

    // 定义近战攻击的属性
    private final float meleeDamage;
    private final float attackSpeed;

    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.randomUUID();
    private static final UUID ATTACK_SPEED_MODIFIER = UUID.randomUUID();
    private static final RawAnimation BOWBLADE = RawAnimation.begin().thenPlay("bowblade");
    private static final RawAnimation PULLBACK = RawAnimation.begin().thenPlay("pullback");
    private static final RawAnimation DRAW = RawAnimation.begin().thenPlay("draw");
private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public sonicarrow(float meleeDamage, float attackSpeed, Properties properties) {
        super(Tiers.NETHERITE, 3, -2.4F, properties);
        //将我们的项目注册为服务器端处理的。
        //这将启用动画数据同步和服务器端动画触发
//        super(new Properties().stacksTo(1).durability(201));
        this.meleeDamage = meleeDamage;
        this.attackSpeed = attackSpeed;
        //将我们的项目注册为服务器端处理的。
        //这将启用动画数据同步和服务器端动画触发
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }


    //定义弓的近战和攻击速度
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();

        if (slot == EquipmentSlot.MAINHAND) {
            // 设置攻击点和攻击速度
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.meleeDamage, AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", this.attackSpeed, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }

    //为已设置攻击点添加减少耐久方法
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(attacker.getUsedItemHand()));
        return true;
    }

    //设置当玩家攻击到实体后有几率播放动画bowblade
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide) {
            if (player.getRandom().nextInt(10) == 0) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) player.level()), "bowblade", "bowblade");
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    //使音速弓能在生存模式下可以附魔上剑和弓的附魔效果并能成功使用
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.BOW || enchantment.category == EnchantmentCategory.WEAPON;
    }

    //利用现有的forge钩子来定义我们的自定义渲染器(我们在createRenderer中创建的)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private sonicarrowRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new sonicarrowRenderer();

                return this.renderer;
            }
        });
    }





    // 注册我们的动画控制器
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "draw", 20, state -> PlayState.STOP)
                .triggerableAnim("draw", DRAW));
        controllers.add(new AnimationController<>(this, "pullback", 20, state -> PlayState.CONTINUE)
                .triggerableAnim("pullback", PULLBACK));
        controllers.add(new AnimationController<>(this, "bowblade", 20, state -> PlayState.STOP)
                .triggerableAnim("bowblade", BOWBLADE));
        // 我们已经将“射击”动画标记为可从服务器触发
    }

    // 单击后开始“使用”该项目
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        if (world instanceof ServerLevel serverLevel)
            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "draw", "draw");


        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
    // 释放鼠标按钮时发射箭头并播放动画
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int ticksRemaining) {
        if (shooter instanceof Player player) {
            if (stack.getDamageValue() >= stack.getMaxDamage() - 1)
                return;

            // 增加冷却时间，这样你就不能快速开火了
            player.getCooldowns().addCooldown(this, 15);

            if (!level.isClientSide) {
                AonicxEntity arrow = new AonicxEntity(shooter, level, AonicxItem.SONICX_ARROW.get());
                arrow.tickCount = 35;

                // 检查音速弓是否附魔了火焰附加

                if (stack.isEnchanted() && stack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
                    arrow.setSecondsOnFire(5);
                }

                // 修改箭矢的飞行力度
                float arrowVelocity = 4.8f;
                float pull = 1.9f;
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), arrowVelocity, pull, 1);
                arrow.setBaseDamage(2.5);
                arrow.isNoGravity();
                arrow.setCritArrow(true);

                // 减少物品耐久
                stack.hurtAndBreak(1, shooter, p -> p.broadcastBreakEvent(shooter.getUsedItemHand()));

                level.addFreshEntity(arrow);
                triggerAnim(shooter, GeoItem.getOrAssignId(stack, (ServerLevel)shooter.level()), "pullback", "pullback");

                // 设置箭矢的旋转角度
                arrow.setYRot(shooter.getYRot());
                arrow.setXRot(shooter.getXRot());
            }
        }

    }


    // 使用普通动画来“拉回”手枪，同时给它充能
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    // 让我们给工具提示添加一些弹药文本
    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("item.sonicarrow.ammo",
                        stack.getMaxDamage() - stack.getDamageValue() - 1,
                        stack.getMaxDamage() - 1)
                .withStyle(ChatFormatting.ITALIC));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}