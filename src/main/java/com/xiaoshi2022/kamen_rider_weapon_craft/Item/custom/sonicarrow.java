package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.sonicarrow.sonicarrowRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.entity.LaserBeamEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.ServerSound;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.procedures.PullSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft")
public class sonicarrow extends SwordItem implements GeoItem {
    private final float meleeDamage;
    private final float attackSpeed;
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.randomUUID();
    private static final UUID ATTACK_SPEED_MODIFIER = UUID.randomUUID();
    private static final RawAnimation BOWBLADE = RawAnimation.begin().thenPlay("bowblade");
    private static final RawAnimation PULLBACK = RawAnimation.begin().thenPlay("pullback");
    private static final RawAnimation DRAW = RawAnimation.begin().thenPlay("draw");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final String MODE_KEY = "Mode";

    public enum Mode {
        DEFAULT,
        MELON,
        LEMON,
        CHERRY,
        PEACH
    }

    public sonicarrow(float meleeDamage, float attackSpeed, Properties properties) {
        super((Tier) Tiers.GOLD, (int) meleeDamage, attackSpeed, properties);
        this.meleeDamage = meleeDamage;
        this.attackSpeed = attackSpeed;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public sonicarrow() {
    this(28.0F, 8.0F, new Properties().stacksTo(1).durability(1500));
    }

    public void switchMode(ItemStack stack, Mode mode) {
        stack.getOrCreateTag().putString("Mode", mode.name());

        Player player = (Player) stack.getEntityRepresentation();
        if (player != null) player.containerMenu.broadcastChanges();
    }

    public Mode getCurrentMode(ItemStack stack) {
        String name = stack.getOrCreateTag().getString(MODE_KEY);
        try {
            return Mode.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Mode.DEFAULT;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(MODE_KEY, Tag.TAG_STRING)) {
            tag.putString(MODE_KEY, Mode.DEFAULT.name());
        }
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
            Mode mode = getCurrentMode(stack);
            ModeConfigMelee cfg = getMeleeConfig(mode);

            // 基础值 + 锁种修正
            multimap.put(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier",
                            this.meleeDamage + cfg.damageBonus(), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED,
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier",
                            this.attackSpeed + cfg.attackSpeedBonus(), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(attacker.getUsedItemHand()));

        if (!target.level().isClientSide && attacker instanceof Player) {
            ModeConfigMelee cfg = getMeleeConfig(getCurrentMode(stack));
            cfg.onHitEffect().accept(target);
        }
        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide && player.level() instanceof ServerLevel serverLevel) {
            if (player.getRandom().nextInt(10) == 0) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "bowblade", "bowblade");
                serverLevel.playSound(null, player.blockPosition(), ModSounds.SLASH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
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
                .triggerableAnim("bowblade", BOWBLADE)
                .setSoundKeyframeHandler(state -> {
                    Player player = ClientUtils.getClientPlayer();
                    if (player != null) {
                        player.playSound(ModSounds.SLASH.get(), 1.0F, 1.0F);
                    }
                }));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.startUsingItem(hand);
            if (level instanceof ServerLevel serverLevel) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "draw", "draw");
                // 发送网络包，开始播放 pull_standby 音效
                ServerSound.sendToServer(new ServerSound(ServerSound.SoundType.START_STANDBY));
            }
        }
        return InteractionResultHolder.success(stack);
    }

    record ModeConfig(
            double damage,        // 单发伤害
            float  shootSpeed,    // 激光飞行速度
            int    burstCount,    // 连发数量
            int    coolDown,      // 射击冷却 tick
            ParticleOptions particle,
            SoundEvent shootSound
    ) {}

    ModeConfig getConfig(Mode mode){
        return switch(mode){
            case MELON -> new ModeConfig(
                    12.0, 1.6f, 1, 30,
                    ModParticles.MELON_PARTICLE.get(),
                    ModSounds.SONICARROW_SHOOT.get()
            );
            case LEMON -> new ModeConfig(
                    10.0, 2.4f, 3, 10,
                    ModParticles.LEMON_PARTICLE.get(),
                    ModSounds.SONICARROW_SHOOT.get()
            );
            case CHERRY -> new ModeConfig(
                    11.0, 2.0f, 2, 20,
                    ModParticles.CHERRY_PARTICLE.get(),
                    ModSounds.SONICARROW_SHOOT.get()
            );
            case PEACH -> new ModeConfig(
                    13.0, 1.8f, 1, 25,
                    ModParticles.PEACH_PARTICLE.get(),
                    ModSounds.SONICARROW_SHOOT.get()
            );
            default -> new ModeConfig(
                    9.0, 2.0f, 1, 20,
                    ModParticles.AONICX_PARTICLE.get(),
                    ModSounds.SONICARROW_SHOOT.get()
            );
        };
    }

    record ModeConfigMelee(
            double damageBonus,         // 额外攻击伤害
            float  attackSpeedBonus,    // 额外攻速
            Consumer<LivingEntity> onHitEffect // 命中特效
    ) {}

    private ModeConfigMelee getMeleeConfig(Mode mode){
        return switch(mode){
            case MELON  -> new ModeConfigMelee(
                    8.0, 0.0F,        // +2❤️
                    target -> {       // 小范围击飞
                        target.knockback(1.2F,
                                target.getX() - target.level().getRandom().nextDouble(),
                                target.getZ() - target.level().getRandom().nextDouble());
                    });
            case LEMON  -> new ModeConfigMelee(
                    6.0, 1.0f,       // +1❤️ +1攻速
                    target -> {       // 连斩：额外一次1点真实伤害
                        target.hurt(target.level().damageSources().playerAttack((Player)target), 1.0F);
                    });
            case CHERRY -> new ModeConfigMelee(
                    7.0, 0.5f,
                    target -> {
                        if (target.level() instanceof ServerLevel serverLevel) {
                            CompoundTag tag = target.getPersistentData();
                            if (!tag.contains("BleedingTimer")) {
                                tag.putInt("BleedingTimer", 60); // 3 秒
                            }
                            // 如果还没注册过，就注册一次
                            if (!tag.getBoolean("BleedingRegistered")) {
                                tag.putBoolean("BleedingRegistered", true);
                                net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new Object() {
                                    @SubscribeEvent
                                    public void onServerTick(TickEvent.ServerTickEvent event) {
                                        if (event.phase != TickEvent.Phase.END) return;

                                        if (target.isAlive() && target.getPersistentData().contains("BleedingTimer")) {
                                            int timer = target.getPersistentData().getInt("BleedingTimer");
                                            if (timer % 10 == 0) {
                                                target.hurt(target.level().damageSources().generic(), 0.5F);
                                            }
                                            target.getPersistentData().putInt("BleedingTimer", timer - 1);
                                            if (timer <= 1) {
                                                target.getPersistentData().remove("BleedingTimer");
                                                target.getPersistentData().remove("BleedingRegistered");
                                                net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(this);
                                            }
                                        } else {
                                            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(this);
                                        }
                                    }
                                });
                            }
                        }
                    });
            case PEACH -> new ModeConfigMelee(
                    9.0, 0.2f,       // +4.5❤️ +0.2攻速
                    target -> {       // 治疗效果：攻击时恢复生命值
                        LivingEntity attacker = target.getLastHurtByMob();
                        if (attacker != null && attacker.isAlive()) {
                            attacker.heal(2.0F);
                        }
                    });
            default     -> new ModeConfigMelee(0, 0, t -> {});
        };
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int ticksRemaining) {
        if (!(shooter instanceof Player player) || level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        Mode mode = getCurrentMode(stack);
        ModeConfig cfg = getConfig(mode);

        // 冷却
        player.getCooldowns().addCooldown(this, cfg.coolDown());

        // 计算充能时间（秒）
        float chargeTime = (getUseDuration(stack) - ticksRemaining) / 20F;

        // 连发
        for (int i = 0; i < cfg.burstCount(); i++) {
            // 微小散布
            float yaw   = player.getYRot() + (i - cfg.burstCount() / 2F) * 2.5F;
            float pitch = player.getXRot();

            // 方向向量
            Vec3 look = Vec3.directionFromRotation(pitch, yaw).scale(cfg.shootSpeed());

            LaserBeamEntity laser = new LaserBeamEntity(
                    level, player,
                    cfg.particle(),
                    cfg.damage(),
                    cfg.shootSound(),
                    chargeTime,
                    stack
            );
            laser.setPos(
                    player.getX(),
                    player.getEyeY(),
                    player.getZ()
            );
            laser.shoot(look.x, look.y, look.z);
            level.addFreshEntity(laser);

            // 附魔
            if (stack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0)
                laser.setSecondsOnFire(5);
            if (stack.getEnchantmentLevel(Enchantments.POWER_ARROWS) > 0)
                laser.damage += stack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
        }

        // 音效（只播一次）
        serverLevel.playSound(
                null,
                player.blockPosition(),
                cfg.shootSound(),
                SoundSource.PLAYERS,
                1F,
                1F
        );

        // 耐久
        stack.hurtAndBreak(cfg.burstCount(), player,
                e -> e.broadcastBreakEvent(player.getUsedItemHand()));

        // 动画
        triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "pullback", "pullback");
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
    public void appendHoverText(ItemStack stack, Level worldIn,
                                List<Component> tooltip, TooltipFlag flagIn) {
        Mode mode = getCurrentMode(stack);
        // 使用语言键，客户端会自动去 lang 文件找
        tooltip.add(Component.translatable("item.kamen_rider_weapon_craft.sonicarrow.mode." + mode.name().toLowerCase())
                .withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 每隔 2 秒检查一次标识符，播放 pull_standby 音效
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && ServerSound.isPlayingStandbySound()) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                if (isHoldingSonicArrow(player)) {
                    // 调用 PullSounds 类中的方法播放 pull_standby 音效
                    PullSounds.playPullStandbySound(player);
                }
            }
        }
    }


    private static boolean isHoldingSonicArrow(Player player) {
        return player.getMainHandItem().getItem() instanceof sonicarrow ||
               player.getOffhandItem().getItem() instanceof sonicarrow;
    }
}