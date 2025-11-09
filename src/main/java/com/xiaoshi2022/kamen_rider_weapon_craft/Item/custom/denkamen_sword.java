package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.denkamen_sword.denkamen_swordRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.entity.line.denliner;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
    
    // 记录玩家是否正在使用必杀
    private static final String RIDING_DENLINER_TAG = "riding_denliner";
    // 记录玩家必杀冷却时间的标签
    private static final String FINISHER_COOLDOWN_TAG = "finisher_cooldown";
    // 必杀冷却时间（30秒 = 600刻）
    private static final int FINISHER_COOLDOWN_TICKS = 600;
    // 待机音开始时间标签
    private static final String STANDBY_START_TIME_TAG = "standby_start_time";
    // 待机音结束时间标签
    private static final String STANDBY_END_TIME_TAG = "standby_end_time";
    // 待机音持续时间（5秒 = 100刻）
    private static final int STANDBY_DURATION_TICKS = 100;

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

    // 重写 use 方法，处理右键触发必杀
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查玩家是否已经在使用必杀（骑乘在电车上）
        if (isPlayerRidingDenliner(player)) {
            return InteractionResultHolder.pass(itemstack);
        }
        
        // 检查必杀是否在冷却中
        if (isFinisherOnCooldown(player)) {
            // 添加冷却提示
            if (!level.isClientSide) {
                long cooldownStartTime = player.getPersistentData().getLong(FINISHER_COOLDOWN_TAG);
                long remainingTicks = FINISHER_COOLDOWN_TICKS - (player.level().getGameTime() - cooldownStartTime);
                int remainingSeconds = (int) (remainingTicks / 20);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c必杀技冷却中！剩余 " + remainingSeconds + " 秒"));
            }
            return InteractionResultHolder.pass(itemstack);
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // 触发电车必杀特效
            denliner train = denliner.create(serverLevel, player);
            
            // 标记玩家正在使用必杀
            markPlayerAsRidingDenliner(player);
            
            // 播放必杀待机音
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.DEN_O_LINES.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // 设置待机音的开始时间和结束时间
            long currentTime = serverLevel.getGameTime();
            setStandbyStartTime(player, currentTime);
            setStandbyEndTime(player, currentTime + STANDBY_DURATION_TICKS);
            
            return InteractionResultHolder.consume(itemstack);
        }

        return InteractionResultHolder.success(itemstack);
    }
    
    // 重写 onEntitySwing 方法，用于触发格挡动画
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        // 只有玩家可以使用这个功能
        if (!(entity instanceof Player player)) {
            return super.onEntitySwing(stack, entity);
        }
        
        Level level = player.level();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // 在服务器端触发格挡动画
            triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "frotation");
        }
        
        return super.onEntitySwing(stack, entity);
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

    // 重写 hurtEnemy 方法，在攻击敌人时停止音效并移除电车
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 检查攻击者是否是玩家
        if (attacker instanceof Player player) {
            // 先停止待机音，无论玩家是否在待机状态
            clearPlayerStandby(player);
            
            // 如果玩家正在骑乘电车（使用必杀中）
            if (isPlayerRidingDenliner(player)) {
                // 先清除玩家的标记，避免循环引用问题
                clearPlayerRidingDenliner(player);
                
                // 检查玩家是否正在骑乘 denliner
                for (Entity passenger : player.getPassengers()) {
                    if (passenger instanceof denliner train) {
                        // 调用电车的 onEnemyAttacked 方法，停止音效并移除电车
                        train.onEnemyAttacked();
                        break;
                    }
                }
                
                // 检查玩家是否在 denliner 上
                if (player.getVehicle() instanceof denliner train) {
                    train.onEnemyAttacked();
                }
                
                // 设置必杀冷却时间
                setFinisherCooldown(player);
                
                // 给予玩家时间抗性4效果，持续10秒（200刻）
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 3)); // 时间抗性=缓降效果，等级4
                
                // 添加玩家突击效果 - 给玩家一个向前的推动力
                double lookAngle = Math.toRadians(player.getYRot());
                double forwardMotion = 1.5D; // 突击力度
                player.push(-Math.sin(lookAngle) * forwardMotion, 0.2D, Math.cos(lookAngle) * forwardMotion);
                
                // 增加伤害作为必杀攻击的特效
                stack.hurtAndBreak(2, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                
                // 在敌人位置创建爆炸效果
                if (!target.level().isClientSide && target.level() instanceof ServerLevel serverLevel) {
                    // 创建没有破坏方块的爆炸，伤害半径为3.0
                    // 将玩家设为爆炸源，并设置为不会伤害自身
                    serverLevel.explode(
                        player, // 爆炸源设为玩家
                        target.getX(), // 爆炸中心X坐标
                        target.getY(), // 爆炸中心Y坐标
                        target.getZ(), // 爆炸中心Z坐标
                        3.0F, // 爆炸强度
                        false, // 是否会破坏方块
                        Level.ExplosionInteraction.NONE // 交互类型设为NONE，避免伤害玩家
                    );
                    
                    // 确保敌人受到足够的伤害 - 必杀伤害翻倍，参考假面骑士电王最终形态必杀
                    target.hurt(serverLevel.damageSources().explosion(null), 100.0F);
                    
                    // 实现玩家面前9格内的敌人受到突击伤害
                    // 获取玩家的朝向角度
                    double playerLookAngle = Math.toRadians(player.getYRot());
                    double xDir = -Math.sin(playerLookAngle);
                    double zDir = Math.cos(playerLookAngle);
                    
                    // 遍历玩家面前9格内的所有敌人实体
                    for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, 
                            player.getBoundingBox().expandTowards(xDir * 9, 2, zDir * 9))) {
                        // 确保不是玩家自己，且是敌对实体
                        if (entity != player && entity != target) {
                            // 计算实体与玩家视线方向的夹角，只攻击前方的敌人
                            double dx = entity.getX() - player.getX();
                            double dz = entity.getZ() - player.getZ();
                            double distance = Math.sqrt(dx * dx + dz * dz);
                            
                            // 检查是否在玩家前方9格内且角度在60度范围内
                            if (distance <= 9.0 && (dx * xDir + dz * zDir) / distance > 0.5) { // 0.5对应约60度
                                // 对前方敌人造成伤害
                                entity.hurt(serverLevel.damageSources().explosion(null), 75.0F);
                                
                                // 给敌人一个击退效果
                                entity.push(xDir * 1.0, 0.3, zDir * 1.0);
                            }
                        }
                    }
                }
            } else {
                // 普通攻击的耐久消耗
                stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(attacker.getUsedItemHand()));
            }
        }
        
        return true;
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
    
    // 辅助方法：标记玩家正在骑乘电车
    private void markPlayerAsRidingDenliner(Player player) {
        player.getPersistentData().putBoolean(RIDING_DENLINER_TAG, true);
    }
    
    // 辅助方法：清除玩家骑乘电车的标记
    private void clearPlayerRidingDenliner(Player player) {
        player.getPersistentData().remove(RIDING_DENLINER_TAG);
    }
    
    // 辅助方法：检查玩家是否正在骑乘电车
    private boolean isPlayerRidingDenliner(Player player) {
        return player.getPersistentData().getBoolean(RIDING_DENLINER_TAG);
    }
    
    // 设置必杀冷却时间
    private void setFinisherCooldown(Player player) {
        player.getPersistentData().putLong(FINISHER_COOLDOWN_TAG, player.level().getGameTime());
    }
    
    // 检查必杀是否在冷却中
    private boolean isFinisherOnCooldown(Player player) {
        if (!player.getPersistentData().contains(FINISHER_COOLDOWN_TAG)) {
            return false;
        }
        long cooldownStartTime = player.getPersistentData().getLong(FINISHER_COOLDOWN_TAG);
        return player.level().getGameTime() - cooldownStartTime < FINISHER_COOLDOWN_TICKS;
    }
    
    // 辅助方法：获取和设置待机音的开始时间
    private long getStandbyStartTime(Player player) {
        return player.getPersistentData().getLong(STANDBY_START_TIME_TAG);
    }
    
    private void setStandbyStartTime(Player player, long time) {
        player.getPersistentData().putLong(STANDBY_START_TIME_TAG, time);
    }
    
    // 辅助方法：获取和设置待机音的结束时间
    private long getStandbyEndTime(Player player) {
        return player.getPersistentData().getLong(STANDBY_END_TIME_TAG);
    }
    
    private void setStandbyEndTime(Player player, long time) {
        player.getPersistentData().putLong(STANDBY_END_TIME_TAG, time);
    }
    
    // 辅助方法：检查玩家是否正在播放待机音
    private boolean isPlayerInStandby(Player player) {
        if (!player.getPersistentData().contains(STANDBY_START_TIME_TAG) || 
            !player.getPersistentData().contains(STANDBY_END_TIME_TAG)) {
            return false;
        }
        long currentTime = player.level().getGameTime();
        long endTime = getStandbyEndTime(player);
        return currentTime < endTime;
    }
    
    // 辅助方法：清除玩家的待机音状态
    private void clearPlayerStandby(Player player) {
        // 直接清除待机状态标记即可，不使用stopsound命令避免显示系统消息
        // 声音会自然播放完毕，或者通过游戏机制自动处理
        player.getPersistentData().remove(STANDBY_START_TIME_TAG);
        player.getPersistentData().remove(STANDBY_END_TIME_TAG);
    }
}