package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.Heiseisword.HeiseiswordRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.sound.RiderSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class Heiseisword extends SwordItem implements GeoItem {
    // 定义4个方位的旋转动画
    private static final RawAnimation ROTATE_POSITION_1 = RawAnimation.begin().thenPlay("rotate_pos1");
    private static final RawAnimation ROTATE_POSITION_2 = RawAnimation.begin().thenPlay("rotate_pos2");
    private static final RawAnimation ROTATE_POSITION_3 = RawAnimation.begin().thenPlay("rotate_pos3");
    private static final RawAnimation ROTATE_POSITION_4 = RawAnimation.begin().thenPlay("rotate_pos4");

    // 超必杀动画
    private static final RawAnimation ULTIMATE_TIME_BREAK_ANIM = RawAnimation.begin().thenPlay("ridertime");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // NBT键名常量
    private static final String TAG_SELECTED_RIDER = "selectedRider";
    private static final String TAG_SCRAMBLE_RIDERS = "scrambleRiders";
    private static final String TAG_IS_FINISH_TIME_MODE = "isFinishTimeMode";
    private static final String TAG_IS_ULTIMATE_MODE = "isUltimateMode";
    private static final String TAG_ROTATION_COUNT = "rotationCount";
    private static final String TAG_LAST_ROTATION_TIME = "lastRotationTime";
    private static final String TAG_CURRENT_ROTATION_POSITION = "currentRotationPosition";
    // 添加冷却时间相关常量和NBT键
    private static final String TAG_LAST_ATTACK_TIME = "lastAttackTime";
    private static final int ATTACK_COOLDOWN_TICKS = 10; // 攻击冷却时间（约0.5秒）
    private static final int ULTIMATE_ATTACK_COOLDOWN_TICKS = 40; // 超必杀冷却时间（约2秒）

    // 添加获取上次攻击时间的方法
    private long getLastAttackTime(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        return stack.getTag().getLong(TAG_LAST_ATTACK_TIME);
    }

    // 添加设置上次攻击时间的方法
    private void setLastAttackTime(ItemStack stack, long time) {
        stack.getOrCreateTag().putLong(TAG_LAST_ATTACK_TIME, time);
    }

    // 检查攻击是否冷却完毕
    private boolean isAttackOnCooldown(ItemStack stack, Level level) {
        long lastAttackTime = getLastAttackTime(stack);
        long currentTime = level.getGameTime();
        boolean isUltimate = isUltimateMode(stack);
        int cooldownTicks = isUltimate ? ULTIMATE_ATTACK_COOLDOWN_TICKS : ATTACK_COOLDOWN_TICKS;
        return (currentTime - lastAttackTime) < cooldownTicks;
    }

    // 状态管理的Getter和Setter方法 - 从ItemStack的NBT中读取和写入

    // 获取选中的骑士
    private String getSelectedRider(ItemStack stack) {
        if (!stack.hasTag()) return null;
        return stack.getTag().getString(TAG_SELECTED_RIDER);
    }

    // 设置选中的骑士
    private void setSelectedRider(ItemStack stack, String riderName) {
        stack.getOrCreateTag().putString(TAG_SELECTED_RIDER, riderName != null ? riderName : "");
    }

    // 获取Scramble选择的骑士列表
    private List<String> getScrambleRiders(ItemStack stack) {
        List<String> riders = new ArrayList<>();
        if (!stack.hasTag()) return riders;
        
        CompoundTag tag = stack.getTag();
        // 直接检查size是否存在，而不是检查主标签
        if (!tag.contains(TAG_SCRAMBLE_RIDERS + "_size")) return riders;
        
        int size = tag.getInt(TAG_SCRAMBLE_RIDERS + "_size");
        for (int i = 0; i < size; i++) {
            if (tag.contains(TAG_SCRAMBLE_RIDERS + "_" + i)) {
                riders.add(tag.getString(TAG_SCRAMBLE_RIDERS + "_" + i));
            }
        }
        return riders;
    }

    // 设置Scramble选择的骑士列表
    private void setScrambleRiders(ItemStack stack, List<String> riders) {
        CompoundTag tag = stack.getOrCreateTag();
        
        // 清除旧数据
        if (tag.contains(TAG_SCRAMBLE_RIDERS + "_size")) {
            int oldSize = tag.getInt(TAG_SCRAMBLE_RIDERS + "_size");
            for (int i = 0; i < oldSize; i++) {
                tag.remove(TAG_SCRAMBLE_RIDERS + "_" + i);
            }
        }
        
        // 保存新数据
        tag.putInt(TAG_SCRAMBLE_RIDERS + "_size", riders.size());
        for (int i = 0; i < riders.size(); i++) {
            tag.putString(TAG_SCRAMBLE_RIDERS + "_" + i, riders.get(i));
        }
    }

    // 检查是否在Finish Time模式
    private boolean isFinishTimeMode(ItemStack stack) {
        if (!stack.hasTag()) return false;
        return stack.getTag().getBoolean(TAG_IS_FINISH_TIME_MODE);
    }

    // 设置是否在Finish Time模式
    private void setFinishTimeMode(ItemStack stack, boolean mode) {
        stack.getOrCreateTag().putBoolean(TAG_IS_FINISH_TIME_MODE, mode);
    }

    // 检查是否在超必杀模式
    private boolean isUltimateMode(ItemStack stack) {
        if (!stack.hasTag()) return false;
        return stack.getTag().getBoolean(TAG_IS_ULTIMATE_MODE);
    }

    // 设置是否在超必杀模式
    private void setUltimateMode(ItemStack stack, boolean mode) {
        stack.getOrCreateTag().putBoolean(TAG_IS_ULTIMATE_MODE, mode);
    }

    // 获取旋转次数
    private int getRotationCount(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        return stack.getTag().getInt(TAG_ROTATION_COUNT);
    }

    // 设置旋转次数
    private void setRotationCount(ItemStack stack, int count) {
        stack.getOrCreateTag().putInt(TAG_ROTATION_COUNT, count);
    }

    // 获取上次旋转时间
    private long getLastRotationTime(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        return stack.getTag().getLong(TAG_LAST_ROTATION_TIME);
    }

    // 设置上次旋转时间
    private void setLastRotationTime(ItemStack stack, long time) {
        stack.getOrCreateTag().putLong(TAG_LAST_ROTATION_TIME, time);
    }

    // 获取当前旋转位置
    private int getCurrentRotationPosition(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        return stack.getTag().getInt(TAG_CURRENT_ROTATION_POSITION);
    }

    // 设置当前旋转位置
    private void setCurrentRotationPosition(ItemStack stack, int position) {
        stack.getOrCreateTag().putInt(TAG_CURRENT_ROTATION_POSITION, position);
    }

    public Heiseisword() {
        super(new Tier() {
            public int getUses() {
                return 1000;
            }

            public float getSpeed() {
                return 3f;
            }

            public float getAttackDamageBonus() {
                return 33f;
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
        }, 3, 2.4f, new Item.Properties());
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new HeiseiswordRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 20, state -> PlayState.STOP)
                .triggerableAnim("pos1", ROTATE_POSITION_1)
                .triggerableAnim("pos2", ROTATE_POSITION_2)
                .triggerableAnim("pos3", ROTATE_POSITION_3)
                .triggerableAnim("pos4", ROTATE_POSITION_4));

        controllers.add(new AnimationController<>(this, "ultimate", 20, state -> PlayState.STOP)
                .triggerableAnim("ultimate_time_break", ULTIMATE_TIME_BREAK_ANIM));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            // Shift+右键：切换到必杀时刻（模拟放入表盘）
            return toggleFinishTimeMode(level, player, stack);
        }

        if (isFinishTimeMode(stack)) {
            // 必杀时刻模式：处理Finish Time模式
            return handleFinishTimeMode(level, player, hand, stack);
        } else {
            // 普通模式：处理普通模式
            return handleNormalMode(level, player, hand, stack);
        }
    }

    // 切换到必杀时刻（模拟放入表盘）
    private InteractionResultHolder<ItemStack> toggleFinishTimeMode(Level level, Player player, ItemStack stack) {
        // 预留：检查副手是否有表盘
        // if (!hasRiderWatchInOffhand(player)) return InteractionResultHolder.pass(stack);

        boolean currentMode = isFinishTimeMode(stack);
        boolean newMode = !currentMode;
        setFinishTimeMode(stack, newMode);

        if (newMode) {
            // 进入Finish Time模式
            HeiseiRiderEffectManager.playFinishTimeSound(level, player);
            setScrambleRiders(stack, new ArrayList<>());
            setRotationCount(stack, 0);
            setSelectedRider(stack, null);
            setUltimateMode(stack, false);
            setCurrentRotationPosition(stack, 0);
        } else {
            // 退出Finish Time模式
            setSelectedRider(stack, null);
            setScrambleRiders(stack, new ArrayList<>());
            setCurrentRotationPosition(stack, 0);
            setUltimateMode(stack, false);
            setRotationCount(stack, 0);
        }
        return InteractionResultHolder.success(stack);
    }

    // 处理普通模式（未放入表盘）- 逆时针选择单个骑士
    private InteractionResultHolder<ItemStack> handleNormalMode(Level level, Player player, InteractionHand hand, ItemStack stack) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();

        String currentSelectedRider = getSelectedRider(stack);
        if (currentSelectedRider == null || currentSelectedRider.isEmpty()) {
            // 第一次使用，播放启动音效并选择Build
            HeiseiRiderEffectManager.playRiderTimeSound(level, player);
            setSelectedRider(stack, riderOrder.get(0)); // Build
            setCurrentRotationPosition(stack, 0);
        } else {
            // 逆时针选择下一个骑士
            int currentIndex = riderOrder.indexOf(currentSelectedRider);
            int nextIndex = (currentIndex + 1) % riderOrder.size();
            setSelectedRider(stack, riderOrder.get(nextIndex));

            // 更新旋转位置（4个方位）
            int currentPosition = getCurrentRotationPosition(stack);
            setCurrentRotationPosition(stack, (currentPosition + 1) % 4);
        }

        // 播放选择音效："Hey! △△!"
        HeiseiRiderEffectManager.playSelectionSound(level, player, getSelectedRider(stack));

        // 触发分段旋转动画
        triggerRotationAnimation(level, player, stack);

        return InteractionResultHolder.success(stack);
    }

    // 处理Finish Time模式（已放入表盘）- 顺时针选择多个骑士
    private InteractionResultHolder<ItemStack> handleFinishTimeMode(Level level, Player player, InteractionHand hand, ItemStack stack) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();

        // 检测X键按下（用于触发超必杀）
        if (KeyBinding.CHANGE_KEY.isDown() && !isUltimateMode(stack)) {
            setUltimateMode(stack, true);
            // 播放超必杀启动音效（嘿嘿待机音）和动画
            HeiseiRiderEffectManager.playUltimateActivationSound(level, player);
            triggerUltimateAnimation(level, player, stack);

            // 自动选择所有骑士用于超必杀
            setScrambleRiders(stack, new ArrayList<>(riderOrder));
            return InteractionResultHolder.success(stack);
        }

        // 顺时针选择骑士
        List<String> currentScrambleRiders = getScrambleRiders(stack);
        // 从ActionBar显示当前已选骑士信息
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.literal("当前已选骑士: " + currentScrambleRiders), true);
        }
        
        if (currentScrambleRiders.isEmpty()) {
            // 从Build开始顺时针选择
            String newRider = riderOrder.get(0); // Build
            List<String> updatedRiders = new ArrayList<>();
            updatedRiders.add(newRider);
            setScrambleRiders(stack, updatedRiders);
            HeiseiRiderEffectManager.playSelectionSound(level, player, newRider);
            setCurrentRotationPosition(stack, 0);
            // 从ActionBar显示选择信息
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal("选择了第一个骑士: " + newRider), true);
            }
        } else {
            // 顺时针选择下一个未被选择的骑士
            String lastRider = currentScrambleRiders.get(currentScrambleRiders.size() - 1);
            int currentIndex = riderOrder.indexOf(lastRider);
            
            // 防御性编程：如果lastRider不在riderOrder列表中，从0开始
            if (currentIndex == -1) {
                currentIndex = 0;
                // 从ActionBar显示警告信息
                if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.literal("警告: 当前骑士" + lastRider + "不在顺序列表中，从0开始查找"), true);
                }
            }

            // 查找下一个未被选择的骑士
            int nextIndex = (currentIndex + 1) % riderOrder.size();
            while (nextIndex != currentIndex) {
                String candidate = riderOrder.get(nextIndex);
                if (!currentScrambleRiders.contains(candidate)) {
                    // 找到了未被选择的骑士
                    List<String> updatedRiders = new ArrayList<>(currentScrambleRiders);
                    updatedRiders.add(candidate);
                    setScrambleRiders(stack, updatedRiders);
                    HeiseiRiderEffectManager.playSelectionSound(level, player, candidate);
                    setCurrentRotationPosition(stack, (getCurrentRotationPosition(stack) + 1) % 4);
                    // 从ActionBar显示选择信息
                    if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.displayClientMessage(Component.literal("选择了骑士: " + candidate), true);
                    }
                    break;
                }
                nextIndex = (nextIndex + 1) % riderOrder.size();
            }
        }

        return InteractionResultHolder.success(stack);
    }

    // 触发分段旋转动画
    private void triggerRotationAnimation(Level level, Player player, ItemStack stack) {
        if (level instanceof ServerLevel serverLevel) {
            int position = getCurrentRotationPosition(stack);
            String animationName = "pos" + (position + 1);
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "rotation", animationName);
        }
    }

    // 触发超必杀动画
    private void triggerUltimateAnimation(Level level, Player player, ItemStack stack) {
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "ultimate", "ultimate_time_break");
        }
    }

    // 执行普通攻击
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, net.minecraft.world.entity.Entity entity) {
        if (!player.level().isClientSide) {
            if (isFinishTimeMode(stack)) {
                // Finish Time模式下的攻击
                boolean result = handleFinishTimeAttack(player, stack);
                // 在Finish Time模式下，我们仍然需要检查实体是否被击败
                if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity && !livingEntity.isAlive()) {
                    // 实体被击败，根据当前模式播放相应的音效
                    if (isUltimateMode(stack)) {
                        // 超必杀模式，播放Ultimate Time Break音效
                        List<String> riders = getScrambleRiders(stack);
                        // 使用优化的批量播放方法，减少线程创建
                        List<RiderSounds.DelayedSound> sounds = new ArrayList<>();
                        
                        // 添加Hey音效（无延迟）
                        sounds.add(new RiderSounds.DelayedSound(RiderSounds.HEY, 0));
                        
                        // 然后按顺序添加所有选中骑士的名称音效
                        int delay = 20;
                        for (String riderName : riders) {
                            SoundEvent nameSound = HeiseiRiderEffectManager.getRiderNameSound(riderName);
                            if (nameSound != null) {
                                sounds.add(new RiderSounds.DelayedSound(nameSound, delay));
                                delay += 10;
                            }
                        }
                        // 添加Ultimate Time Break音效
                        sounds.add(new RiderSounds.DelayedSound(RiderSounds.ULTIMATE_TIME_BREAK, delay + 20));
                        
                        // 批量播放所有音效，显著减少资源消耗
                        RiderSounds.playDelayedSoundSequence(player.level(), player, sounds);
                    } else {
                        // 普通Scramble模式，播放Scramble Time Break音效
                        List<String> riders = getScrambleRiders(stack);
                        // 使用优化的批量播放方法，减少线程创建
                        List<RiderSounds.DelayedSound> sounds = new ArrayList<>();
                        
                        // 添加Hey音效（无延迟）
                        sounds.add(new RiderSounds.DelayedSound(RiderSounds.HEY, 0));
                        
                        // 然后按顺序添加所有选中骑士的名称音效
                        int delay = 20;
                        for (String riderName : riders) {
                            SoundEvent nameSound = HeiseiRiderEffectManager.getRiderNameSound(riderName);
                            if (nameSound != null) {
                                sounds.add(new RiderSounds.DelayedSound(nameSound, delay));
                                delay += 10;
                            }
                        }
                        // 添加Scramble Time Break音效
                        sounds.add(new RiderSounds.DelayedSound(RiderSounds.SCRAMBLE_TIME_BREAK, delay + 20));
                        
                        // 批量播放所有音效，显著减少资源消耗
                        RiderSounds.playDelayedSoundSequence(player.level(), player, sounds);
                    }
                }
                return result;
            } else {
                String rider = getSelectedRider(stack);
                if (rider != null && !rider.isEmpty()) {
                    // 普通模式下的攻击
                    boolean result = handleNormalAttack(player, stack);
                    // 检查实体是否被击败
                    if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity && !livingEntity.isAlive()) {
                        // 实体被击败，先播放骑士名称音效，然后播放Dual Time Break音效
                        // 使用直接方式播放，避免可能的延迟问题
                        HeiseiRiderEffectManager.playSelectionSound(player.level(), player, rider);
                        // 确保Dual Time Break音效正确播放
                        RiderSounds.playDelayedSound(player.level(), player, RiderSounds.DUAL_TIME_BREAK, 40);
                    }
                    return result;
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    // 处理普通模式攻击
    private boolean handleNormalAttack(Player player, ItemStack stack) {
        // 检查攻击冷却
        if (isAttackOnCooldown(stack, player.level())) {
            return false;
        }

        String rider = getSelectedRider(stack);
        HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
        if (effect != null) {
            // 不再在攻击时播放骑士名称音效和Dual Time Break音效
            // 这些音效现在只在击败实体后才会触发

            // 执行特殊攻击效果
            effect.executeSpecialAttack(player.level(), player, player.getLookAngle());

            // 更新上次攻击时间
            setLastAttackTime(stack, player.level().getGameTime());
            return true;
        }
        return false;
    }

    // 处理Finish Time模式攻击
    private boolean handleFinishTimeAttack(Player player, ItemStack stack) {
        // 检查攻击冷却
        if (isAttackOnCooldown(stack, player.level())) {
            return false;
        }

        List<String> riders = getScrambleRiders(stack);
        if (riders.isEmpty()) {
            // 如果没有选择骑士，在屏幕上显示提示信息
            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.translatable("message.kamen_rider_weapon_craft.no_riders_selected"));
            }
            return false;
        }

        if (isUltimateMode(stack)) {
            // 执行超必杀 "XX! 平成Riders! Ultimate Time Break!"
            executeUltimateTimeBreak(player.level(), player, stack);
        } else {
            // 执行普通Scramble必杀 "XX! OO! Scramble Time Break!"
            executeScrambleTimeBreak(player.level(), player, stack);
        }

        // 重置状态 - 只重置超必杀模式，但保持已选择的骑士列表，允许玩家继续进行连击
        // 用户需要通过按shift+右键明确退出finishTime模式
        setUltimateMode(stack, false);
        setRotationCount(stack, 0);
        // 更新上次攻击时间
        setLastAttackTime(stack, player.level().getGameTime());
        return true;
    }

    // 执行Scramble Time Break
    private void executeScrambleTimeBreak(Level level, Player player, ItemStack stack) {
        List<String> riders = getScrambleRiders(stack);
        int riderCount = riders.size();
        
        // 不再直接播放Scramble Time Break音效，音效将在击败实体后通过onLeftClickEntity方法触发

        // 移除特效数量限制，确保所有粒子效果都能显示
        for (int i = 0; i < riderCount; i++) {
            String rider = riders.get(i);
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                effect.executeSpecialAttack(level, player, player.getLookAngle());
            }
        }
    }

    // 执行Ultimate Time Break
    private void executeUltimateTimeBreak(Level level, Player player, ItemStack stack) {
        List<String> riders = getScrambleRiders(stack);
        
        // 不再直接播放Ultimate Time Break音效，音效将在击败实体后通过onLeftClickEntity方法触发

        // 移除特效数量限制，确保所有粒子效果都能显示
        for (int i = 0; i < riders.size(); i++) {
            String rider = riders.get(i);
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                // 超必杀模式下特效增强
                effect.executeSpecialAttack(level, player, player.getLookAngle());
            }
        }

        // 添加额外的全屏特效或范围伤害
        executeUltimateSpecialEffects(level, player);

        // 触发超必杀动画
        triggerUltimateAnimation(level, player, stack);
    }

    // 执行超必杀特殊效果
    private void executeUltimateSpecialEffects(Level level, Player player) {
        if (!level.isClientSide) {
            // 超必杀爆炸效果
            level.explode(player, player.getX(), player.getY(), player.getZ(),
                    8.0f, Level.ExplosionInteraction.MOB);

            // 获取玩家位置
            Vec3 playerPos = player.position();
            double range = 15.0;
            // 查找范围内的所有生物实体，排除玩家自己
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    player.getBoundingBox().inflate(range),
                    entity -> entity != player) // 过滤掉玩家自己
                .forEach(entity -> {
                    // 对每个敌人造成200点直接伤害（最终必杀伤害），足以有效击败基夫门徒
                        entity.hurt(level.damageSources().playerAttack(player), 200.0f);
                    
                    // 施加击退效果
                    entity.setDeltaMovement(
                            entity.getDeltaMovement().add(
                                    entity.position().subtract(playerPos).normalize().scale(2.0)
                            )
                    );
                });
        }
    }

    // ==================== 预留的表盘功能 ====================

    /**
     * 检查副手是否有表盘（预留方法）
     */
    private boolean hasRiderWatchInOffhand(Player player) {
        ItemStack offhandItem = player.getOffhandItem();
        // 预留：检查副手物品是否是表盘
        // return offhandItem.getItem() instanceof RiderWatchItem;
        return true; // 暂时返回true，等表盘开发完成后再实现具体逻辑
    }

    /**
     * 安装表盘（预留方法）
     */
    public void installRiderWatch(ItemStack watchStack) {
        // 注意：这个方法需要知道要修改哪个ItemStack的状态
        // 由于没有提供Heiseisword的ItemStack，此方法实际上无法正常工作
        // 正确的做法是通过toggleFinishTimeMode方法来切换模式
        // 预留：安装表盘后的逻辑
    }

    /**
     * 卸载表盘（预留方法）
     */
    public void uninstallRiderWatch() {
        // 注意：这个方法需要知道要修改哪个ItemStack的状态
        // 由于没有提供Heiseisword的ItemStack，此方法实际上无法正常工作
        // 正确的做法是通过toggleFinishTimeMode方法来切换模式
        // 预留：卸载表盘的逻辑
    }

    /**
     * 检查是否安装了表盘（预留方法）
     */
    public boolean hasRiderWatchInstalled() {
        // 注意：这个方法需要知道要检查哪个ItemStack的状态
        // 由于没有提供Heiseisword的ItemStack，此方法总是返回false
        // 正确的做法是使用ItemStack参数调用isFinishTimeMode方法
        return false;
    }

    // 获取当前选择的骑士
    public String getSelectedRider() {
        // 注意：这个方法需要知道要检查哪个ItemStack的状态
        // 由于没有提供Heiseisword的ItemStack，此方法总是返回null
        // 正确的做法是使用ItemStack参数调用getSelectedRider方法
        return null;
    }

    // 获取当前Scramble选择的骑士列表
    public List<String> getScrambleRiders() {
        // 注意：这个方法需要知道要检查哪个ItemStack的状态
        // 由于没有提供Heiseisword的ItemStack，此方法总是返回空列表
        // 正确的做法是使用ItemStack参数调用getScrambleRiders方法
        return new ArrayList<>();
    }

    // 获取当前旋转位置
    public int getCurrentRotationPosition() {
        // 注意：这个方法需要知道要检查哪个ItemStack的状态
        // 由于没有提供Heiseisword的ItemStack，此方法总是返回0
        // 正确的做法是使用ItemStack参数调用getCurrentRotationPosition方法
        return 0;
    }

    // 判断当前是否在Finish Time模式
    public boolean isInFinishTimeMode() {
        // 注意：这个方法需要知道要检查哪个ItemStack的状态
        // 由于没有提供Heiseisword的ItemStack，此方法总是返回false
        // 正确的做法是使用ItemStack参数调用isFinishTimeMode方法
        return false;
    }

    // 判断是否在超必杀模式
    public boolean isInUltimateMode() {
        // 注意：这个方法需要知道要检查哪个ItemStack的状态
        // 由于没有提供Heiseisword的ItemStack，此方法总是返回false
        // 正确的做法是使用ItemStack参数调用isUltimateMode方法
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}