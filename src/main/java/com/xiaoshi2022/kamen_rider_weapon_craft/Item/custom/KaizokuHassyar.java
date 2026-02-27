package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.server.entity.LaserBeamEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.entity.projectile.HassyarsEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.entity.projectile.PhotonicEntity;

import com.xiaoshi2022.kamen_rider_weapon_craft.network.KaizokuHassyarModeSwitchPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.KaizokuHassyarSoundPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.NetworkHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.procedures.PullSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft")
public class KaizokuHassyar extends SwordItem implements GeoItem {
    private final float meleeDamage;
    private final float attackSpeed;
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.randomUUID();
    private static final UUID ATTACK_SPEED_MODIFIER = UUID.randomUUID();
    private static final RawAnimation TRAIN_A = RawAnimation.begin().thenPlay("train_a");
    private static final RawAnimation TRAIN_B = RawAnimation.begin().thenPlay("train_b");
    private static final RawAnimation TRAIN_C = RawAnimation.begin().thenPlay("train_c");
    private static final RawAnimation TRAIN_D = RawAnimation.begin().thenPlay("train_d");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    // 移除静态Map，避免可能的注册表问题

    public static final String MODE_KEY = "Mode";

    public enum Mode {
        LOCAL_TRAIN,    // 各站电车
        EXPRESS_TRAIN,  // 急行电车
        RAPID_TRAIN,    // 快速电车
        PIRATE_TRAIN    // 海贼电车
    }

    public KaizokuHassyar(float meleeDamage, float attackSpeed, Properties properties) {
        super((Tier) Tiers.GOLD, (int) meleeDamage, attackSpeed, properties);
        this.meleeDamage = meleeDamage;
        this.attackSpeed = attackSpeed;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public KaizokuHassyar() {
        this(15.0F, 5.0F, new Properties().stacksTo(1).durability(1500));
    }

    public void switchMode(ItemStack stack, Mode mode, Player player) {
        stack.getOrCreateTag().putString("Mode", mode.name());

        if (player != null) {
            player.containerMenu.broadcastChanges();
            player.getInventory().setChanged();
        }
    }

    public Mode getCurrentMode(ItemStack stack) {
        String name = stack.getOrCreateTag().getString(MODE_KEY);
        try {
            return Mode.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Mode.LOCAL_TRAIN;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(MODE_KEY)) {
            tag.putString(MODE_KEY, Mode.LOCAL_TRAIN.name());
        }

        // 处理Y键切换技能
        if (isSelected && entity instanceof Player player) {
            if (level.isClientSide) {
                // 检查Y键是否按下
                if (KeyBinding.OPEN_LOCKSEED.isDown() && !isModeSwitchOnCooldown(stack, level)) {
                    Mode currentMode = getCurrentMode(stack);
                    Mode nextMode = Mode.values()[(currentMode.ordinal() + 1) % Mode.values().length];
                    // 发送网络包到服务器
                    NetworkHandler.INSTANCE.sendToServer(
                            new KaizokuHassyarModeSwitchPacket(nextMode)
                    );
                    setLastModeSwitchTime(stack, level.getGameTime());
                    

                }
            }
        }
    }

    private static final String TAG_LAST_MODE_SWITCH_TIME = "LastModeSwitchTime";
    private static final int MODE_SWITCH_COOLDOWN_TICKS = 15;

    private long getLastModeSwitchTime(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        return stack.getTag().getLong(TAG_LAST_MODE_SWITCH_TIME);
    }

    private void setLastModeSwitchTime(ItemStack stack, long time) {
        stack.getOrCreateTag().putLong(TAG_LAST_MODE_SWITCH_TIME, time);
    }

    private boolean isModeSwitchOnCooldown(ItemStack stack, Level level) {
        long lastSwitchTime = getLastModeSwitchTime(stack);
        long currentTime = level.getGameTime();
        return (currentTime - lastSwitchTime) < MODE_SWITCH_COOLDOWN_TICKS;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.kaizoku_hassyar.KaizokuHassyarRenderer();
                }
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
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.BOW || enchantment.category == EnchantmentCategory.WEAPON;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "train_a", 20, state -> PlayState.STOP)
                .triggerableAnim("train_a", TRAIN_A));
        controllers.add(new AnimationController<>(this, "train_b", 20, state -> PlayState.STOP)
                .triggerableAnim("train_b", TRAIN_B));
        controllers.add(new AnimationController<>(this, "train_c", 20, state -> PlayState.STOP)
                .triggerableAnim("train_c", TRAIN_C));
        controllers.add(new AnimationController<>(this, "train_d", 20, state -> PlayState.STOP)
                .triggerableAnim("train_d", TRAIN_D));
        controllers.add(new AnimationController<>(this, "shoot", 20, state -> PlayState.STOP)
                .triggerableAnim("shoot", SHOOT));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            // 开始蓄力，播放动画并止住
            player.startUsingItem(hand);

            // 立即播放对应的动画
            Mode mode = getCurrentMode(stack);
            ModeConfig cfg = getConfig(mode);
            if (level instanceof ServerLevel serverLevel) {
                // 播放动画
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), cfg.animationName(), cfg.animationName());
            }
        } else {
            // 客户端发送音效数据包到服务器
            Mode mode = getCurrentMode(stack);
            // 发送对应的火车音效数据包，让周围的人也能听到
            switch (mode) {
                case LOCAL_TRAIN:
                    NetworkHandler.sendToServer(new KaizokuHassyarSoundPacket(KaizokuHassyarSoundPacket.SoundType.TRAIN_A, player.getX(), player.getY(), player.getZ()));
                    break;
                case EXPRESS_TRAIN:
                    NetworkHandler.sendToServer(new KaizokuHassyarSoundPacket(KaizokuHassyarSoundPacket.SoundType.TRAIN_B, player.getX(), player.getY(), player.getZ()));
                    break;
                case RAPID_TRAIN:
                    NetworkHandler.sendToServer(new KaizokuHassyarSoundPacket(KaizokuHassyarSoundPacket.SoundType.TRAIN_C, player.getX(), player.getY(), player.getZ()));
                    break;
                case PIRATE_TRAIN:
                    NetworkHandler.sendToServer(new KaizokuHassyarSoundPacket(KaizokuHassyarSoundPacket.SoundType.TRAIN_D, player.getX(), player.getY(), player.getZ()));
                    break;
            }
        }
        return InteractionResultHolder.success(stack);
    }

    record ModeConfig(
            double damage,        // 单发伤害
            float  shootSpeed,    // 子弹飞行速度
            int    burstCount,    // 连发数量
            int    coolDown,      // 射击冷却 tick
            SoundEvent shootSound,
            String animationName
    ) {}

    //临时调用音速弓的弹药
    ModeConfig getConfig(Mode mode) {
        return switch(mode) {
            case LOCAL_TRAIN -> new ModeConfig(
                    25.0, 1.8f, 3, 20,
                    null, // 移除SONICARROW_SHOOT音效
                    "train_a"
            );
            case RAPID_TRAIN -> new ModeConfig(
                    30.0, 2.0f, 5, 15,
                    null, // 移除SONICARROW_SHOOT音效
                    "train_c"
            );
            case EXPRESS_TRAIN -> new ModeConfig(
                    35.0, 2.2f, 6, 10,
                    null, // 移除SONICARROW_SHOOT音效
                    "train_b"
            );
            case PIRATE_TRAIN -> new ModeConfig(
                    50.0, 2.5f, 1, 120, // 增加冷却时间到120 tick（6秒），作为大招
                    null, // 移除SONICARROW_SHOOT音效
                    "train_d"
            );
        };
    }

    record ModeConfigMelee(
            double damageBonus,         // 额外攻击伤害
            float  attackSpeedBonus,    // 额外攻速
            Consumer<LivingEntity> onHitEffect // 命中特效
    ) {}

    private ModeConfigMelee getMeleeConfig(Mode mode) {
        return switch(mode) {
            case LOCAL_TRAIN -> new ModeConfigMelee(
                    10.0, 1.0f,
                    target -> {
                        target.knockback(0.8F, target.getX() - target.level().getRandom().nextDouble(), target.getZ() - target.level().getRandom().nextDouble());
                    }
            );
            case RAPID_TRAIN -> new ModeConfigMelee(
                    15.0, 0.5f,
                    target -> {
                        target.setSecondsOnFire(3);
                    }
            );
            case EXPRESS_TRAIN -> new ModeConfigMelee(
                    20.0, 0.0f,
                    target -> {
                        target.hurt(target.level().damageSources().playerAttack((Player) target), 5.0F);
                    }
            );
            case PIRATE_TRAIN -> new ModeConfigMelee(
                    25.0, -0.5f,
                    target -> {
                        target.knockback(1.5F, target.getX() - target.level().getRandom().nextDouble(), target.getZ() - target.level().getRandom().nextDouble());
                        target.setSecondsOnFire(5);
                    }
            );
        };
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int ticksRemaining) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Mode mode = getCurrentMode(stack);
            ModeConfig cfg = getConfig(mode);

            float chargeTime = (getUseDuration(stack) - ticksRemaining) / 20F;

            // 触发发射动画
            triggerAnim(shooter, GeoItem.getOrAssignId(stack, serverLevel), "shoot", "shoot");

            // 发射远程攻击
            shootProjectile(level, shooter, stack, chargeTime);

            // 再次检测并停止train_A~b的音效，确保音效完全停止
            if (shooter instanceof Player player) {
                stopTrainSounds(player);
                player.getCooldowns().addCooldown(this, cfg.coolDown());
            }
        } else {
            // 客户端发送射击音效数据包到服务器
            if (shooter instanceof Player player) {
                // 发送射击音效数据包，让周围的人也能听到
                NetworkHandler.sendToServer(new KaizokuHassyarSoundPacket(KaizokuHassyarSoundPacket.SoundType.SHOOT, player.getX(), player.getY(), player.getZ()));
            }
        }
    }

    public void shootProjectile(Level level, LivingEntity shooter, ItemStack stack, float chargeTime) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        Mode mode = getCurrentMode(stack);
        ModeConfig cfg = getConfig(mode);
        
        // 停止TRAIN_A~D的音效
        if (shooter instanceof Player player) {
            stopTrainSounds(player);
        }
        
        // 查找附近的目标实体
        List<LivingEntity> nearbyEntities = findNearbyEntities(shooter, 10.0D);

        // 检查是否为海盗列车模式
        if (mode == Mode.PIRATE_TRAIN) {
            // 海盗列车模式：根据蓄力时长调整伤害
            float damageMultiplier = 1.0f + (chargeTime * 0.1f); // 每蓄力1秒，伤害增加10%，降低伤害倍数
            float finalDamage = (float) (cfg.damage() * damageMultiplier);
            
            // 海盗列车模式：使用单独的建模实体弹药 - HassyarsEntity
            Vec3 look = getDirectionWithTargetChance(shooter, nearbyEntities, 0.2F, 0.4F);
            
            // 使用HassyarsEntity作为海盗列车的单独建模实体弹药
            HassyarsEntity.spawnHassyars(level, (LivingEntity) shooter, look, finalDamage);
        } else {
            // 其他模式：根据不同模式设置不同的蓄力时间阈值
            float requiredChargeTime;
            switch (mode) {
                case LOCAL_TRAIN: // 各站电车
                    requiredChargeTime = 2.0f;
                    break;
                case RAPID_TRAIN: // 快速电车
                    requiredChargeTime = 4.0f;
                    break;
                case EXPRESS_TRAIN: // 急行电车
                    requiredChargeTime = 6.0f;
                    break;
                default:
                    requiredChargeTime = 2.0f;
            }
            
            // 根据蓄力时长和模式确定实际射出的弹药数量
            int actualBurstCount;
            if (chargeTime < requiredChargeTime) {
                // 蓄力不足时，根据模式设置不同的弹药数量
                switch (mode) {
                    case LOCAL_TRAIN: // 各站电车
                        actualBurstCount = 1;
                        break;
                    case RAPID_TRAIN: // 快速电车
                        actualBurstCount = 3;
                        break;
                    case EXPRESS_TRAIN: // 急行电车
                        actualBurstCount = 4;
                        break;
                    default:
                        actualBurstCount = 1;
                }
            } else {
                // 蓄力足够时，射出完整数量的弹药
                actualBurstCount = cfg.burstCount();
            }
            
            // 其他模式：使用PhotonicEntity作为普通弹药
            for (int i = 0; i < actualBurstCount; i++) {
                // 获取可能朝向目标实体的随机化射击方向
                Vec3 look = getDirectionWithTargetChance(shooter, nearbyEntities, 0.3F, 0.5F);

                // 使用PhotonicEntity作为普通弹药
                PhotonicEntity.spawnPhotonic(level, (LivingEntity) shooter, look, (float) cfg.damage());
            }
        }

        // 只有当shootSound不为null时才播放音效
        if (cfg.shootSound() != null) {
            serverLevel.playSound(
                    null,
                    shooter.blockPosition(),
                    cfg.shootSound(),
                    SoundSource.PLAYERS,
                    1F,
                    1F
            );
        }

        stack.hurtAndBreak(cfg.burstCount(), shooter,
                e -> e.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        
        // 射出弹药后停止TRAIN_A~D的音效
        if (shooter instanceof Player player) {
            stopTrainSounds(player);
        }
    }

    /**
     * 停止TRAIN_A~D的音效
     * @param player 玩家实体
     */
    private void stopTrainSounds(Player player) {
        if (!player.level().isClientSide) {
            // 停止TRAIN_A音效
            ResourceLocation trainASound = new ResourceLocation("kamen_rider_weapon_craft", "train_a");
            SoundStopPacket packetA = new SoundStopPacket(player.getId(), trainASound);
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    packetA
            );
            
            // 停止TRAIN_B音效
            ResourceLocation trainBSound = new ResourceLocation("kamen_rider_weapon_craft", "train_b");
            SoundStopPacket packetB = new SoundStopPacket(player.getId(), trainBSound);
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    packetB
            );
            
            // 停止TRAIN_C音效
            ResourceLocation trainCSound = new ResourceLocation("kamen_rider_weapon_craft", "train_c");
            SoundStopPacket packetC = new SoundStopPacket(player.getId(), trainCSound);
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    packetC
            );
            
            // 停止TRAIN_D音效
            ResourceLocation trainDSound = new ResourceLocation("kamen_rider_weapon_craft", "train_d");
            SoundStopPacket packetD = new SoundStopPacket(player.getId(), trainDSound);
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    packetD
            );
        }
    }

    /**
     * 查找射击者附近的目标实体
     * @param shooter 射击者实体
     * @param range 搜索范围
     * @return 附近目标实体列表
     */
    private List<LivingEntity> findNearbyEntities(LivingEntity shooter, double range) {
        List<LivingEntity> nearbyEntities = new ArrayList<>();
        
        // 搜索周围的实体
        for (Entity entity : shooter.level().getEntities(shooter, shooter.getBoundingBox().inflate(range))) {
            // 只考虑活着的实体，且不是射击者自己，且是可以攻击的目标
            if (entity instanceof LivingEntity livingEntity && 
                livingEntity.isAlive() && 
                livingEntity != shooter && 
                shooter.canAttack(livingEntity)) {
                nearbyEntities.add(livingEntity);
            }
        }
        
        return nearbyEntities;
    }

    /**
     * 获取可能朝向目标实体的射击方向
     * @param shooter 射击者实体
     * @param nearbyEntities 附近的目标实体列表
     * @param randomness 随机性程度（0.0-1.0）
     * @param targetChance 朝向目标的概率（0.0-1.0）
     * @return 射击方向向量
     */
    private Vec3 getDirectionWithTargetChance(LivingEntity shooter, List<LivingEntity> nearbyEntities, float randomness, float targetChance) {
        // 检查是否有附近的目标实体，并且随机决定是否朝向目标
        if (!nearbyEntities.isEmpty() && shooter.getRandom().nextFloat() < targetChance) {
            // 随机选择一个附近的目标实体
            LivingEntity targetEntity = nearbyEntities.get(shooter.getRandom().nextInt(nearbyEntities.size()));
            
            // 计算朝向目标实体的方向
            Vec3 targetDirection = targetEntity.position().subtract(shooter.position()).normalize();
            
            // 添加随机偏移
            double randomX = (shooter.getRandom().nextDouble() - 0.5) * 2 * randomness;
            double randomY = (shooter.getRandom().nextDouble() - 0.5) * 2 * randomness;
            double randomZ = (shooter.getRandom().nextDouble() - 0.5) * 2 * randomness;
            
            // 创建随机偏移向量
            Vec3 randomOffset = new Vec3(randomX, randomY, randomZ);
            
            // 合并目标方向和随机偏移，并归一化
            return targetDirection.add(randomOffset).normalize();
        } else {
            // 没有附近目标或随机决定不朝向目标，使用普通的随机化方向
            return getRandomizedDirection(shooter, randomness);
        }
    }

    /**
     * 获取随机化的射击方向
     * @param shooter 射击者实体
     * @param randomness 随机性程度（0.0-1.0）
     * @return 随机化的方向向量
     */
    private Vec3 getRandomizedDirection(LivingEntity shooter, float randomness) {
        // 基础方向：玩家的视线方向
        Vec3 baseDirection = shooter.getLookAngle();
        
        // 添加随机偏移
        double randomX = (shooter.getRandom().nextDouble() - 0.5) * 2 * randomness;
        double randomY = (shooter.getRandom().nextDouble() - 0.5) * 2 * randomness;
        double randomZ = (shooter.getRandom().nextDouble() - 0.5) * 2 * randomness;
        
        // 创建随机偏移向量
        Vec3 randomOffset = new Vec3(randomX, randomY, randomZ);
        
        // 合并基础方向和随机偏移，并归一化
        Vec3 finalDirection = baseDirection.add(randomOffset).normalize();
        
        return finalDirection;
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
        return 72000; // 恢复长按蓄力时间
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn,
                                List<Component> tooltip, TooltipFlag flagIn) {
        Mode mode = getCurrentMode(stack);
        tooltip.add(Component.translatable("item.kamen_rider_weapon_craft.kaizoku_hassyar.mode." + mode.name().toLowerCase())
                .withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }



    private static boolean isHoldingKaizokuHassyar(Player player) {
        return player.getMainHandItem().getItem() instanceof KaizokuHassyar ||
               player.getOffhandItem().getItem() instanceof KaizokuHassyar;
    }
    
    // 移除事件监听器，避免注册表问题
}