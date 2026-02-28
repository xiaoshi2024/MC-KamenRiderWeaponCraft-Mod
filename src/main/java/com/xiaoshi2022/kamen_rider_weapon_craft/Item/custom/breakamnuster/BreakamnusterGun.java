package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.breakamnuster;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.breakamnuster.BreakamnusterGunRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BreakamnusterGun extends SwordItem implements GeoItem {
    // 动画定义
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation USE_ANIM = RawAnimation.begin().thenPlay("use");
    
    // 冷却时间设置（单位：tick，20tick=1秒）
    private static final int COOLDOWN_TICKS = 20; // 1秒冷却

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack currentStack; // 用于存储当前物品堆栈
    private boolean isShooting = false; // 标记是否正在射击
    
    // 联动接口 - 伤害修改器系统
    public static class DamageModifier {
        private final Predicate<Player> condition; // 触发条件
        private final float multiplier; // 伤害倍率
        private final String name; // 修饰符名称
        
        public DamageModifier(String name, Predicate<Player> condition, float multiplier) {
            this.name = name;
            this.condition = condition;
            this.multiplier = multiplier;
        }
        
        public boolean test(Player player) {
            return condition.test(player);
        }
        
        public float getMultiplier() {
            return multiplier;
        }
        
        public String getName() {
            return name;
        }
    }
    
    // 联动接口 - 击中效果系统
    public static class HitEffect {
        private final Predicate<LivingEntity> targetCondition; // 目标条件
        private final BiConsumer<LivingEntity, Float> effectAction; // 效果动作
        private final String name; // 效果名称
        
        public HitEffect(String name, Predicate<LivingEntity> targetCondition, BiConsumer<LivingEntity, Float> effectAction) {
            this.name = name;
            this.targetCondition = targetCondition;
            this.effectAction = effectAction;
        }
        
        public boolean test(LivingEntity target) {
            return targetCondition.test(target);
        }
        
        public void apply(LivingEntity target, float chargeRatio) {
            effectAction.accept(target, chargeRatio);
        }
        
        public String getName() {
            return name;
        }
    }
    
    // 注册列表
    private static final List<DamageModifier> damageModifiers = new ArrayList<>();
    private static final List<HitEffect> hitEffects = new ArrayList<>();
    
    // 基础伤害值
    private static float baseDamage = 12.0f;
    
    // 公开API - 添加伤害修改器
    public static void registerDamageModifier(DamageModifier modifier) {
        damageModifiers.add(modifier);
    }
    
    // 公开API - 移除伤害修改器
    public static void unregisterDamageModifier(String modifierName) {
        damageModifiers.removeIf(modifier -> modifier.getName().equals(modifierName));
    }
    
    // 公开API - 添加击中效果
    public static void registerHitEffect(HitEffect effect) {
        hitEffects.add(effect);
    }
    
    // 公开API - 移除击中效果
    public static void unregisterHitEffect(String effectName) {
        hitEffects.removeIf(effect -> effect.getName().equals(effectName));
    }
    
    // 公开API - 设置基础伤害
    public static void setBaseDamage(float damage) {
        baseDamage = damage;
    }
    
    // 公开API - 获取当前基础伤害
    public static float getBaseDamage() {
        return baseDamage;
    }
    
    // 计算最终伤害（包含所有修改器）
    public float calculateFinalDamage(Player player, float chargeRatio) {
        float finalDamage = baseDamage * (1.0f + chargeRatio); // 基础伤害 * 蓄力加成
        
        // 应用所有匹配的伤害修改器
        for (DamageModifier modifier : damageModifiers) {
            if (modifier.test(player)) {
                finalDamage *= modifier.getMultiplier();
            }
        }
        
        return finalDamage;
    }
    
    // 应用击中效果
    public void applyHitEffects(LivingEntity target, float chargeRatio) {
        applyHitEffectsStatic(target, chargeRatio);
    }
    
    // 静态版本的击中效果应用方法 - 供弹丸实体调用
    public static void applyHitEffectsStatic(LivingEntity target, float chargeRatio) {
        for (HitEffect effect : hitEffects) {
            if (effect.test(target)) {
                effect.apply(target, chargeRatio);
            }
        }
    }
    
    // 默认击中效果：缓慢
    public static final HitEffect DEFAULT_SLOWNESS_EFFECT = new HitEffect(
        "default_slowness",
        (target) -> true, // 对所有目标生效
        (target, chargeRatio) -> {
            // 根据蓄力时间增加缓慢效果的强度和持续时间
            int duration = 20 + (int)(chargeRatio * 40); // 1-3秒
            int amplifier = (int)(chargeRatio * 2); // 0-2级
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
        }
    );
    
    // 静态初始化：注册默认效果
    static {
        registerHitEffect(DEFAULT_SLOWNESS_EFFECT);
    }

    public BreakamnusterGun(Properties properties) {
        super(new Tier() {
            @Override
            public int getUses() {
                return 1500; // 武器耐久度
            }

            @Override
            public float getSpeed() {
                return 0.8f; // 攻击速度 - 枪的攻击速度较慢
            }

            @Override
            public float getAttackDamageBonus() {
                return baseDamage; // 额外攻击伤害 - 使用联动接口的基础伤害值
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
        }, 4, -2.4f, properties);

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // 动画控制器
    private PlayState predicate(AnimationState<BreakamnusterGun> event) {
        // 检查是否正在射击
        if (isShooting) {
            event.getController().setAnimation(USE_ANIM);
            // 动画播放完成后重置状态
            if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
                isShooting = false;
            }
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "controller", 0, this::predicate)
                .triggerableAnim("use_trigger", USE_ANIM));
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
            private BreakamnusterGunRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new BreakamnusterGunRenderer();
                }
                return this.renderer;
            }
        });
    }

    // 远程攻击相关方法
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW; // 使用弓的动画
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // 最大使用时间
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        /* -------- Shift + 右键：切换到剑模式 -------- */
        if (player.isShiftKeyDown() && !player.isUsingItem()) {
            if (!level.isClientSide) {
                // 创建新的剑形态武器
                ItemStack swordStack = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.BREAKAMNUSTER_SWORD.get(), 1);
                
                // 复制耐久度等重要属性
                if (stack.isDamageableItem()) {
                    swordStack.setDamageValue(stack.getDamageValue());
                }
                
                // 复制NBT数据
                if (stack.hasTag()) {
                    net.minecraft.nbt.CompoundTag tag = stack.getTag().copy();
                    swordStack.setTag(tag);
                }
                
                // 替换物品
                player.setItemInHand(hand, swordStack);
                
                // 播放切换音效
                level.playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.PISTON_CONTRACT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.9F);
                level.playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN, net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.2F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        
        // 正常使用：蓄力射击
        currentStack = stack; // 设置当前堆栈用于动画判断
        player.startUsingItem(hand);

        // 触发使用动画
        if (!level.isClientSide) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "use_trigger");
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int ticksRemaining) {
        if (!(shooter instanceof Player player)) return;

        // 计算蓄力时间（秒）
        int chargeTime = getUseDuration(stack) - ticksRemaining;
        float chargeRatio = Math.min(1.0f, chargeTime / 20.0f); // 最大蓄力20ticks=1秒

        // 蓄力越久，射程越远
        int range = 5 + (int) (chargeRatio * 45); // 5-50格射程

        // 设置射击状态以触发动画
        this.isShooting = true;

        // 发射弹丸
        if (!level.isClientSide) {
            // 这里需要先创建弹射物实体类型，可以注册一个简单的ThrowableItemProjectile
            // 由于实体注册比较复杂，这里先使用射线方式，稍后添加弹丸实体

            shootProjectileRay(level, player, range, chargeRatio);
        }

        // 消耗耐久度
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(shooter.getUsedItemHand()));

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        // 重置使用状态
        shooter.stopUsingItem();
    }

    /**
     * 发射弹丸射线（临时方案，可以使用自定义实体替换）
     */
    private void shootProjectileRay(Level level, Player player, int range, float chargeRatio) {
        ServerLevel serverLevel = (ServerLevel) level;

        // 获取玩家视线方向
        Vec3 look = player.getLookAngle();
        Vec3 startPos = player.getEyePosition(1.0f);

        // 射线粗细参数（值越大射线越粗）
        float rayThickness = 0.3f;
        
        // 发射视觉效果（弹丸轨迹）
        for (int i = 0; i < range; i += 2) {
            Vec3 particlePos = startPos.add(look.scale(i));

            // 添加弹丸粒子效果 - 多个粒子形成粗射线
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    particlePos.x, particlePos.y, particlePos.z,
                    3, rayThickness, rayThickness, rayThickness, 0);
            serverLevel.sendParticles(ParticleTypes.GLOW,
                    particlePos.x, particlePos.y, particlePos.z,
                    5, rayThickness, rayThickness, rayThickness, 0);

            // 根据蓄力添加额外效果
            if (chargeRatio > 0.5f) {
                serverLevel.sendParticles(ParticleTypes.FIREWORK,
                        particlePos.x, particlePos.y, particlePos.z,
                        2, rayThickness * 0.8f, rayThickness * 0.8f, rayThickness * 0.8f, 0);
            }
        }

        // 沿视线方向检测实体和方块
        for (int i = 1; i <= range; i++) {
            Vec3 checkPos = startPos.add(look.scale(i));
            BlockPos blockPos = BlockPos.containing(checkPos.x, checkPos.y, checkPos.z);
            BlockState blockState = level.getBlockState(blockPos);

            // 检测实体
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(checkPos.x - rayThickness, checkPos.y - rayThickness, checkPos.z - rayThickness, 
                    checkPos.x + rayThickness, checkPos.y + rayThickness, checkPos.z + rayThickness))) {
                if (entity != player) {
                    // 计算伤害
                    float finalDamage = calculateFinalDamage(player, chargeRatio);
                    // 造成伤害
                    entity.hurt(level.damageSources().playerAttack(player), finalDamage);
                    // 应用击中效果
                    applyHitEffects(entity, chargeRatio);
                    // 在命中点添加特效
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            checkPos.x, checkPos.y, checkPos.z,
                            10, 0.2, 0.2, 0.2, 0.1);
                    return; // 只伤害第一个命中的实体
                }
            }

            // 如果检测到可擦除的方块
            if (isErasableBlock(blockState.getBlock())) {
                // 擦除方块
                eraseBlock(serverLevel, blockPos, chargeRatio);
                break; // 只擦除第一个命中的方块
            }

            // 如果检测到固体方块（不可擦除），停止检测
            if (blockState.isSolidRender(level, blockPos)) {
                // 在命中点添加特效
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        checkPos.x, checkPos.y, checkPos.z,
                        10, 0.2, 0.2, 0.2, 0.1);
                break;
            }
        }
    }

    /**
     * 静态方法：擦除方块并添加特效
     */
    public static void eraseBlock(ServerLevel level, BlockPos blockPos, float chargeRatio) {
        // 擦除方块
        level.destroyBlock(blockPos, false);

        // 添加粒子效果（使用烟雾和云粒子模拟擦除效果）
        int particleCount = 5 + (int)(chargeRatio * 10); // 根据蓄力增加粒子数量

        for (int j = 0; j < particleCount; j++) {
            double dx = blockPos.getX() + level.random.nextDouble();
            double dy = blockPos.getY() + level.random.nextDouble();
            double dz = blockPos.getZ() + level.random.nextDouble();

            level.sendParticles(ParticleTypes.SMOKE, dx, dy, dz, 1, 0, 0, 0, 0.1);
            level.sendParticles(ParticleTypes.CLOUD, dx, dy, dz, 1, 0, 0, 0, 0.1);

            // 根据蓄力添加额外特效
            if (chargeRatio > 0.7f) {
                level.sendParticles(ParticleTypes.FLASH,
                        dx, dy, dz, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * 判断方块是否可以被擦除
     */
    private boolean isErasableBlock(Block block) {
        // 获取方块的注册表名称
        String blockName = block.getName().toString();

        // 可擦除的方块类型：拉杆、按钮、压力板、红石相关等
        return blockName.contains("lever") ||      // 拉杆
                blockName.contains("button") ||     // 按钮
                blockName.contains("pressure_plate") || // 压力板
                blockName.contains("redstone") ||   // 红石相关
                blockName.contains("tripwire") ||   // 绊线
                blockName.contains("hopper") ||     // 漏斗
                blockName.contains("dispenser") ||  // 发射器
                blockName.contains("dropper") ||    // 投掷器
                blockName.contains("piston") ||     // 活塞
                blockName.contains("lectern") ||    // 讲台
                blockName.contains("bell") ||       // 钟
                blockName.contains("command_block") || // 命令方块
                blockName.contains("tnt") ||        // TNT
                blockName.contains("trapped_chest"); // 陷阱箱
    }

    /**
     * 触发射击动画
     */
    public void triggerShootAnimation() {
        this.isShooting = true;
    }
}