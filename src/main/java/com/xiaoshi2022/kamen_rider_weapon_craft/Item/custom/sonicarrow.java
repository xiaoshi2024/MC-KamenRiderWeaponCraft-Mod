package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.sonicarrow.sonicarrowRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.entity.LaserBeamEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
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

public class sonicarrow extends SwordItem implements GeoItem {
    private final float meleeDamage;
    private final float attackSpeed;
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.randomUUID();
    private static final UUID ATTACK_SPEED_MODIFIER = UUID.randomUUID();
    private static final RawAnimation BOWBLADE = RawAnimation.begin().thenPlay("bowblade");
    private static final RawAnimation PULLBACK = RawAnimation.begin().thenPlay("pullback");
    private static final RawAnimation DRAW = RawAnimation.begin().thenPlay("draw");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 定义模式枚举
    public enum Mode {
        DEFAULT,
        MELON
    }

    // 当前模式
    private Mode currentMode = Mode.DEFAULT;

    public sonicarrow(float meleeDamage, float attackSpeed, Properties properties) {
        super(Tiers.NETHERITE, 3, -2.4F, properties);
        this.meleeDamage = meleeDamage;
        this.attackSpeed = attackSpeed;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public sonicarrow() {
        this(7.0F, -2.4F, new Properties().stacksTo(1).durability(201));
    }

    // 切换模式的方法
    public void switchMode(ItemStack stack, Mode mode) {
        CompoundTag tag = stack.getOrCreateTag();
        currentMode = mode;
        tag.putString("Mode", currentMode.name()); // 保存当前模式到 NBT
        stack.setTag(tag);
    }

    // 获取当前模式
    public Mode getCurrentMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("Mode")) {
            return Mode.valueOf(tag.getString("Mode"));
        }
        return Mode.DEFAULT; // 默认模式
    }

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

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(attacker.getUsedItemHand()));
        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide) {
            if (player.getRandom().nextInt(10) == 0) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) player.level()), "bowblade", "bowblade");
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.BOW || enchantment.category == EnchantmentCategory.WEAPON;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "draw", 20, state -> PlayState.STOP)
                .triggerableAnim("draw", DRAW));
        controllers.add(new AnimationController<>(this, "pullback", 20, state -> PlayState.CONTINUE)
                .triggerableAnim("pullback", PULLBACK));
        controllers.add(new AnimationController<>(this, "bowblade", 20, state -> PlayState.STOP)
                .triggerableAnim("bowblade", BOWBLADE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        if (world instanceof ServerLevel serverLevel)
            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "draw", "draw");

        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int ticksRemaining) {
        if (shooter instanceof Player player) {
            if (stack.getDamageValue() >= stack.getMaxDamage() - 1)
                return;

            // 增加冷却时间，这样你就不能快速开火了
            player.getCooldowns().addCooldown(this, 15);
            if (!level.isClientSide) {
                // 获取玩家的射箭角度
                float yaw = player.getYRot();
                float pitch = player.getXRot();

                // 激光束的飞行速度
                float laserVelocity = 4.8f;

                // 计算激光束的初始速度
                double xSpeed = -Mth.sin(yaw * (float) Math.PI / 180.0F) * Mth.cos(pitch * (float) Math.PI / 180.0F) * laserVelocity;
                double ySpeed = -Mth.sin(pitch * (float) Math.PI / 180.0F) * laserVelocity;
                double zSpeed = Mth.cos(yaw * (float) Math.PI / 180.0F) * Mth.cos(pitch * (float) Math.PI / 180.0F) * laserVelocity;

                // 创建激光束实体
                LaserBeamEntity laserBeam = new LaserBeamEntity(level, player, xSpeed, ySpeed, zSpeed);
                laserBeam.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
                laserBeam.setDeltaMovement(xSpeed, ySpeed, zSpeed);

                // 设置激光束的基础伤害为9点
                laserBeam.damage = 9.0D;

                // 将激光束实体添加到世界中
                level.addFreshEntity(laserBeam);
                //减少物品耐久
                stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(player.getUsedItemHand()));
                // 检查音速弓是否附魔了火焰附加
                if (stack.isEnchanted() && stack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
                    laserBeam.setSecondsOnFire(5);
                }
                // 播放动画
                triggerAnim(shooter, GeoItem.getOrAssignId(stack, (ServerLevel)shooter.level()), "pullback", "pullback");
            }
        }
    }


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