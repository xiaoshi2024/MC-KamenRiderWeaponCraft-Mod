package com.xiaoshi2022.kamenriderweaponcraft.Item.custom;

import com.xiaoshi2022.kamenriderweaponcraft.Item.client.sonicarrow.SonicArrowRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.entity.SonicArrowLaserEntity;
import com.xiaoshi2022.kamenriderweaponcraft.event.SonicArrowBleedingHandler;
import com.xiaoshi2022.kamenriderweaponcraft.rider.sound.RiderSounds;
import com.xiaoshi2022.kamenriderweaponcraft.world.inventory.SonicBowMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 音速弓（1.21.1 移植自 1.20.1 sonicarrow）
 * - 普通右键：拉弓蓄力，松开发射雷电弧激光（渲染颜色随装载锁种变化）
 * - 潜行+右键：打开锁种装载 GUI
 * - 7 种形态：DEFAULT/ORANGE/MELON/LEMON/CHERRY/PEACH/DRAGON（1.20.1 无 ORANGE，本版新增）
 */
public class SonicArrowItem extends SwordItem implements GeoItem {
    public static final String MODE_KEY = "Mode";

    // 属性修正 ID（1.21.1 用 ResourceLocation）
    private static final ResourceLocation DAMAGE_MOD_ID =
            ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "sonic_arrow_damage");
    private static final ResourceLocation SPEED_MOD_ID =
            ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "sonic_arrow_speed");

    private static final RawAnimation BOWBLADE = RawAnimation.begin().thenPlay("bowblade");
    private static final RawAnimation PULLBACK = RawAnimation.begin().thenPlay("pullback");
    private static final RawAnimation DRAW = RawAnimation.begin().thenPlay("draw");

    private final float meleeDamage;
    private final float attackSpeed;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SonicArrowItem() {
        this(28.0F, 8.0F, new Item.Properties().stacksTo(1).durability(1500));
    }

    public SonicArrowItem(float meleeDamage, float attackSpeed, Properties properties) {
        super(Tiers.GOLD, properties);
        this.meleeDamage = meleeDamage;
        this.attackSpeed = attackSpeed;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // ==================== 形态 ====================

    public enum Mode {
        DEFAULT, ORANGE, MELON, LEMON, CHERRY, PEACH, DRAGON
    }

    public record LaserColor(int main, int core) {}

    /** 激光颜色表（ARGB，ThunderMeshBuilder 从高位取 alpha） */
    public static LaserColor getLaserColor(Mode mode) {
        return switch (mode) {
            case ORANGE -> new LaserColor(0xFFFF8C00, 0xFFFFD700); // 橘
            case MELON  -> new LaserColor(0xFF32CD32, 0xFF99FF99); // 绿
            case LEMON  -> new LaserColor(0xFFFFFF00, 0xFFFFF9C4); // 黄
            case CHERRY -> new LaserColor(0xFFDC143C, 0xFFFF6B6B); // 绯红
            case PEACH  -> new LaserColor(0xFFFFB6C1, 0xFFFFE4E8); // 粉
            case DRAGON -> new LaserColor(0xFFFF2222, 0xFFFFAAAA); // 红
            default     -> new LaserColor(0xFFFFFFFF, 0xFFEAEAFF); // 白
        };
    }

    /** 当前形态（存于 CustomData 组件） */
    public Mode getCurrentMode(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            String name = customData.copyTag().getString(MODE_KEY);
            try {
                return Mode.valueOf(name);
            } catch (IllegalArgumentException e) {
                return Mode.DEFAULT;
            }
        }
        return Mode.DEFAULT;
    }

    /** 切换形态（GUI 装入锁种时调用） */
    public void switchMode(ItemStack stack, Mode mode) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putString(MODE_KEY, mode.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // ==================== 远程射击配置 ====================

    record ModeConfig(double damage, float shootSpeed, int burstCount, int coolDown) {}

    ModeConfig getShootConfig(Mode mode) {
        return switch (mode) {
            case ORANGE -> new ModeConfig(11.0, 2.0f, 1, 20);
            case MELON  -> new ModeConfig(12.0, 1.6f, 1, 30);
            case LEMON  -> new ModeConfig(10.0, 2.4f, 3, 10);
            case CHERRY -> new ModeConfig(11.0, 2.0f, 2, 20);
            case PEACH  -> new ModeConfig(13.0, 1.8f, 1, 25);
            case DRAGON -> new ModeConfig(15.0, 2.2f, 1, 40);
            default     -> new ModeConfig(9.0, 2.0f, 1, 20);
        };
    }

    // ==================== 近战配置 ====================

    record ModeConfigMelee(double damageBonus, float attackSpeedBonus, BiConsumer<LivingEntity, LivingEntity> onHitEffect) {}

    private ModeConfigMelee getMeleeConfig(Mode mode) {
        return switch (mode) {
            case ORANGE -> new ModeConfigMelee(8.0, 0.0F, SonicArrowItem::knockUpEffect);
            case MELON  -> new ModeConfigMelee(8.0, 0.0F, SonicArrowItem::melonEffect);
            case LEMON  -> new ModeConfigMelee(6.0, 1.0f, SonicArrowItem::lemonEffect);
            case CHERRY -> new ModeConfigMelee(7.0, 0.5f, (target, attacker) ->
                    SonicArrowBleedingHandler.applyBleeding(target));
            case PEACH  -> new ModeConfigMelee(9.0, 0.2f, (target, attacker) ->
                    attacker.heal(2.0F)); // 治疗攻击者
            case DRAGON -> new ModeConfigMelee(12.0, 0.0f, (target, attacker) ->
                    target.igniteForSeconds(10F)); // 燃烧 10 秒
            default     -> new ModeConfigMelee(0, 0, (target, attacker) -> {});
        };
    }

    /** ORANGE：小范围击飞 1.0 */
    private static void knockUpEffect(LivingEntity target, LivingEntity attacker) {
        target.knockback(1.0F,
                target.getX() - target.level().getRandom().nextDouble(),
                target.getZ() - target.level().getRandom().nextDouble());
    }

    /** MELON：小范围击飞 1.2 */
    private static void melonEffect(LivingEntity target, LivingEntity attacker) {
        target.knockback(1.2F,
                target.getX() - target.level().getRandom().nextDouble(),
                target.getZ() - target.level().getRandom().nextDouble());
    }

    /** LEMON：连斩，额外一次 1 点真实伤害 */
    private static void lemonEffect(LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            target.hurt(target.level().damageSources().playerAttack(player), 1.0F);
        } else {
            target.hurt(target.level().damageSources().generic(), 1.0F);
        }
    }

    // ==================== 动态属性 ====================

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ModeConfigMelee cfg = getMeleeConfig(getCurrentMode(stack));
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(DAMAGE_MOD_ID, this.meleeDamage + cfg.damageBonus(),
                                AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(SPEED_MOD_ID, this.attackSpeed + cfg.attackSpeedBonus(),
                                AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    // ==================== 交互 ====================

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 潜行 + 右键：打开锁种装载 GUI
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inventory, p) -> new SonicBowMenu(id, inventory, (RegistryFriendlyByteBuf) null),
                        Component.translatable("container.kamenriderweaponcraft.sonic_bow")));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // 普通右键：拉弓
        if (!level.isClientSide) {
            player.startUsingItem(hand);
            if (level instanceof ServerLevel serverLevel) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "draw", "draw");
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int ticksRemaining) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        Mode mode = getCurrentMode(stack);
        ModeConfig cfg = getShootConfig(mode);

        // 蓄力时间（秒）
        float chargeTime = (getUseDuration(stack, shooter) - ticksRemaining) / 20F;

        // 力量附魔加伤（1.21.1 附魔注册表返回 Holder）
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.POWER), stack);

        LaserColor color = getLaserColor(mode);
        for (int i = 0; i < cfg.burstCount(); i++) {
            // 连发微散布
            float yaw = shooter.getYRot() + (i - cfg.burstCount() / 2F) * 2.5F;
            float pitch = shooter.getXRot();
            Vec3 dir = Vec3.directionFromRotation(pitch, yaw);

            SonicArrowLaserEntity laser = new SonicArrowLaserEntity(
                    level, shooter,
                    cfg.damage() + powerLevel * 0.5,
                    chargeTime, mode, stack,
                    color.main(), color.core());
            laser.setPos(shooter.getX(), shooter.getEyeY(), shooter.getZ());
            laser.shoot(dir.x, dir.y, dir.z, cfg.shootSpeed(), 0.0F);
            level.addFreshEntity(laser);
        }

        // 音效（只播一次）
        serverLevel.playSound(null, shooter.blockPosition(),
                RiderSounds.SONICARROW_SHOOT, SoundSource.PLAYERS, 1F, 1F);

        // 耐久消耗
        stack.hurtAndBreak(cfg.burstCount(), shooter, EquipmentSlot.MAINHAND);

        // 冷却 + 拉回动画
        if (shooter instanceof Player player) {
            player.getCooldowns().addCooldown(this, cfg.coolDown());
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "pullback", "pullback");
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);

        if (!target.level().isClientSide && attacker instanceof Player) {
            getMeleeConfig(getCurrentMode(stack)).onHitEffect().accept(target, attacker);
        }
        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        // 10% 概率触发拔刀动画
        if (!player.level().isClientSide && player.level() instanceof ServerLevel serverLevel) {
            if (player.getRandom().nextInt(10) == 0) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "bowblade", "bowblade");
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    /** 允许附魔台附魔：弓类（火矢/冲击/力量/无限）与武器类（锋利/击退/火焰附加/掠夺） */
    @Override
    public boolean supportsEnchantment(ItemStack stack, net.minecraft.core.Holder<Enchantment> enchantment) {
        var supported = enchantment.value().getSupportedItems();
        return supported.contains(net.minecraft.core.registries.BuiltInRegistries.ITEM.wrapAsHolder(Items.BOW))
                || supported.contains(net.minecraft.core.registries.BuiltInRegistries.ITEM.wrapAsHolder(Items.DIAMOND_SWORD));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Mode mode = getCurrentMode(stack);
        tooltip.add(Component.translatable("item.kamenriderweaponcraft.sonicarrow.mode." + mode.name().toLowerCase())
                .withStyle(ChatFormatting.YELLOW));
    }

    // ==================== GeckoLib ====================

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private SonicArrowRenderer renderer;

            @Override
            public GeoItemRenderer<SonicArrowItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SonicArrowRenderer();
                }
                return this.renderer;
            }
        });
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
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
