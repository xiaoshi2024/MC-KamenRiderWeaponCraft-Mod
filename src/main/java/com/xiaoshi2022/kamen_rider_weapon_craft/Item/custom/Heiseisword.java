package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.RiderEnergyHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.Heiseisword.HeiseiswordRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.HeiseiswordRiderSelectionPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.NetworkHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.sound.RiderSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.client.Minecraft;
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
    private static final String TAG_LAST_RIDER_SELECTION_TIME = "lastRiderSelectionTime"; // 上次选择骑士的时间
    private static final int ATTACK_COOLDOWN_TICKS = 10; // 攻击冷却时间（约0.5秒）
    private static final int ULTIMATE_ATTACK_COOLDOWN_TICKS = 40; // 超必杀冷却时间（约2秒）
    private static final int RIDER_SELECTION_COOLDOWN_TICKS = 15; // 骑士选择冷却时间（约0.75秒）

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
        // 移除SingletonGeoAnimatable注册，避免多人游戏中的状态共享问题
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

        // 右键仅用于远程攻击，不执行其他功能
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    // 获取上次选择骑士的时间
    private long getLastRiderSelectionTime(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        return stack.getTag().getLong(TAG_LAST_RIDER_SELECTION_TIME);
    }

    // 设置上次选择骑士的时间
    private void setLastRiderSelectionTime(ItemStack stack, long time) {
        stack.getOrCreateTag().putLong(TAG_LAST_RIDER_SELECTION_TIME, time);
    }

    // 检查骑士选择是否冷却完毕
    private boolean isRiderSelectionOnCooldown(ItemStack stack, Level level) {
        long lastSelectionTime = getLastRiderSelectionTime(stack);
        long currentTime = level.getGameTime();
        return (currentTime - lastSelectionTime) < RIDER_SELECTION_COOLDOWN_TICKS;
    }

    // 处理Y键选择骑士的逻辑 - 从武器类内部调用
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // 只有当物品在主手且实体是玩家时才处理
        if (isSelected && entity instanceof Player player) {
            // 检测Y键按下且不在冷却期
            if (KeyBinding.OPEN_LOCKSEED.isDown() && !isRiderSelectionOnCooldown(stack, level)) {
                // 客户端只负责发送网络包到服务端
                if (level.isClientSide) {
                    // 发送骑士选择请求到服务端
                    NetworkHandler.INSTANCE.sendToServer(new HeiseiswordRiderSelectionPacket());
                    
                    // 在客户端也设置冷却，避免快速连续发送多个包
                    setLastRiderSelectionTime(stack, level.getGameTime());
                }
                // 服务端处理逻辑会在handleRiderSelectionOnServer方法中进行
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }
    
    /**
     * 服务端处理骑士选择的静态方法
     * 由HeiseiswordRiderSelectionPacket调用
     */
    public static void handleRiderSelectionOnServer(ServerPlayer player, ItemStack stack) {
        if (stack.getItem() instanceof Heiseisword heiseisword) {
            // 检查冷却
            if (!heiseisword.isRiderSelectionOnCooldown(stack, player.level())) {
                // 实际处理骑士选择逻辑
                heiseisword.handleRiderSelectionInternal(player, stack);
                // 设置冷却时间
                heiseisword.setLastRiderSelectionTime(stack, player.level().getGameTime());
            }
        }
    }
    
    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return super.canContinueUsing(oldStack, newStack);
    }

    // 内部处理骑士选择逻辑（在服务端执行）
    private void handleRiderSelectionInternal(Player player, ItemStack stack) {
        if (isFinishTimeMode(stack)) {
            // 必杀时刻模式：处理Finish Time模式下的选择
            handleFinishTimeModeSelection(player, stack);
        } else {
            // 普通模式：处理普通模式下的选择
            handleNormalModeSelection(player, stack);
        }
    }

    // 处理普通模式下的Y键选择
    private void handleNormalModeSelection(Player player, ItemStack stack) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();

        String currentSelectedRider = getSelectedRider(stack);
        if (currentSelectedRider == null || currentSelectedRider.isEmpty()) {
            // 第一次使用，播放启动音效并选择Build
            HeiseiRiderEffectManager.playRiderTimeSound(player.level(), player);
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
        HeiseiRiderEffectManager.playSelectionSound(player.level(), player, getSelectedRider(stack));

        // 触发分段旋转动画（客户端和服务端都触发）
        triggerRotationAnimation(player.level(), player, stack);
    }

    // 处理Finish Time模式下的Y键选择
    private void handleFinishTimeModeSelection(Player player, ItemStack stack) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();

        // 检测X键按下（用于触发超必杀）
        if (KeyBinding.CHANGE_KEY.isDown() && !isUltimateMode(stack)) {
            setUltimateMode(stack, true);
            // 播放超必杀启动音效（嘿嘿待机音）和动画
            HeiseiRiderEffectManager.playUltimateActivationSound(player.level(), player);
            triggerUltimateAnimation(player.level(), player, stack);

            // 自动选择所有骑士用于超必杀
            setScrambleRiders(stack, new ArrayList<>(riderOrder));
            
            // 设置超必杀准备就绪标志，实际技能将在玩家发动攻击时执行
            stack.getOrCreateTag().putBoolean("isXKeyUltimateReady", true);
            return;
        }

        // 顺时针选择骑士并添加到列表
        List<String> currentScrambleRiders = getScrambleRiders(stack);
        
        if (currentScrambleRiders.isEmpty()) {
            // 从Build开始顺时针选择
            String newRider = riderOrder.get(0); // Build
            List<String> updatedRiders = new ArrayList<>();
            updatedRiders.add(newRider);
            setScrambleRiders(stack, updatedRiders);
            HeiseiRiderEffectManager.playSelectionSound(player.level(), player, newRider);
            setCurrentRotationPosition(stack, 0);
            // 从ActionBar显示选择信息
            if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal("选择了第一个骑士: " + newRider), true);
            }
        } else {
            // 顺时针选择下一个未被选择的骑士
            String lastRider = currentScrambleRiders.get(currentScrambleRiders.size() - 1);
            int currentIndex = riderOrder.indexOf(lastRider);
            
            // 防御性编程：如果lastRider不在riderOrder列表中，从0开始
            if (currentIndex == -1) {
                currentIndex = 0;
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
                    HeiseiRiderEffectManager.playSelectionSound(player.level(), player, candidate);
                    setCurrentRotationPosition(stack, (getCurrentRotationPosition(stack) + 1) % 4);
                    // 从ActionBar显示选择信息
                    if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.displayClientMessage(Component.literal("选择了骑士: " + candidate), true);
                    }
                    break;
                }
                nextIndex = (nextIndex + 1) % riderOrder.size();
            }
        }

        // 从ActionBar显示当前已选骑士信息
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.literal("当前已选骑士: " + getScrambleRiders(stack)), true);
        }
        
        // 在Finish Time模式下选择骑士时也触发旋转动画
        triggerRotationAnimation(player.level(), player, stack);
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
            // 保存当前选择的骑士到临时变量
            String savedRider = getSelectedRider(stack);
            setSelectedRider(stack, null);
            setUltimateMode(stack, false);
            setCurrentRotationPosition(stack, 0);
            // 恢复之前选择的骑士
            setSelectedRider(stack, savedRider);
        } else {
            // 退出Finish Time模式，清空选中的骑士，符合用户需求
            setScrambleRiders(stack, new ArrayList<>());
            setSelectedRider(stack, null); // 清空骑士选择
            setCurrentRotationPosition(stack, 0);
            setUltimateMode(stack, false);
            setRotationCount(stack, 0);
            // 清除超必杀准备就绪标志
            stack.getOrCreateTag().remove("isXKeyUltimateReady");
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
        int position = getCurrentRotationPosition(stack);
        String animationName = "pos" + (position + 1);
        
        // 确保在服务端触发动画并同步到所有客户端
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "rotation", animationName);
        }
        // 客户端不需要额外处理，因为GeckoLib会自动处理客户端动画
    }

    // 触发超必杀动画
    private void triggerUltimateAnimation(Level level, Player player, ItemStack stack) {
        // 确保在服务端触发动画并同步到所有客户端
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "ultimate", "ultimate_time_break");
        }
        // 客户端不需要额外处理，因为GeckoLib会自动处理客户端动画
    }

    // 执行普通攻击
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, net.minecraft.world.entity.Entity entity) {
        if (!player.level().isClientSide) {
            // 检查是否处于超必杀准备就绪状态（由X键触发）
            boolean isXKeyUltimateReady = stack.getOrCreateTag().getBoolean("isXKeyUltimateReady");
            
            if (isXKeyUltimateReady) {
                // 如果是X键超必杀准备就绪状态，执行X键超必杀攻击
                executeXKeyUltimateAttack(player.level(), player, stack);
                
                // 清除超必杀准备就绪标志
                stack.getOrCreateTag().remove("isXKeyUltimateReady");
                
                // 检查实体是否被击败
                if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity && !livingEntity.isAlive()) {
                    // 实体被击败，播放完整的超必杀音效序列
                    List<String> riders = getScrambleRiders(stack);
                    HeiseiRiderEffectManager.playUltimateFinishSoundSequence(player.level(), player, riders);
                }
                
                return true;
            }
            
            if (isFinishTimeMode(stack)) {
                // Finish Time模式下的攻击
                boolean result = handleFinishTimeAttack(player, stack);
                // 在Finish Time模式下，我们仍然需要检查实体是否被击败
                if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity && !livingEntity.isAlive()) {
                    // 实体被击败，根据当前模式播放相应的音效
                    if (isUltimateMode(stack)) {
                        // 超必杀模式，播放完整的超必杀音效序列（在击败实体后触发）
                        List<String> riders = getScrambleRiders(stack);
                        HeiseiRiderEffectManager.playUltimateFinishSoundSequence(player.level(), player, riders);
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
                        if ("Drive".equals(rider)) {
                            // 对Drive骑士进行特殊处理
                            RiderSounds.playSound(player.level(), player, RiderSounds.NAME_DRIVE);
                            // 确保Dual Time Break音效正确播放
                            RiderSounds.playDelayedSound(player.level(), player, RiderSounds.DUAL_TIME_BREAK, 40);
                        } else {
                            // 其他骑士使用常规处理
                            SoundEvent nameSound = HeiseiRiderEffectManager.getRiderNameSound(rider);
                            if (nameSound != null) {
                                RiderSounds.playSound(player.level(), player, nameSound);
                                // 确保Dual Time Break音效正确播放
                                RiderSounds.playDelayedSound(player.level(), player, RiderSounds.DUAL_TIME_BREAK, 40);
                            }
                        }
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
            // 检查骑士能量是否足够
            double energyCost = HeiseiRiderEffectManager.getRiderEnergyCost(rider);
            if (!RiderEnergyHandler.canUseRiderEnergy(player) || !RiderEnergyHandler.consumeRiderEnergy(player, energyCost)) {
                // 能量不足，显示屏幕中央提示
                if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                    // 在服务端，通过客户端消息传递到屏幕中央
                    serverPlayer.displayClientMessage(Component.literal("§c骑士能量不足，无法使用此技能！"), true);
                }
                return false;
            }
            
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
        
        // 移除攻击时播放的音效，改为在击败实体后播放
        // 音效将在onLeftClickEntity方法中，当实体被击败时触发
        
        // 计算总能量消耗（每个骑士的能量消耗之和）
        double totalEnergyCost = 0;
        for (String rider : riders) {
            totalEnergyCost += HeiseiRiderEffectManager.getRiderEnergyCost(rider);
        }
        
        // Scramble模式下能量消耗增加20%作为组合消耗
        totalEnergyCost *= 1.2;
        
        // 检查骑士能量是否足够
        if (!RiderEnergyHandler.canUseRiderEnergy(player) || !RiderEnergyHandler.consumeRiderEnergy(player, totalEnergyCost)) {
            // 能量不足，显示屏幕中央提示
            if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal("§c骑士能量不足，无法使用Scramble Time Break！"), true);
            }
            return;
        }

        // 移除特效数量限制，确保所有粒子效果都能显示
        for (int i = 0; i < riderCount; i++) {
            String rider = riders.get(i);
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                effect.executeSpecialAttack(level, player, player.getLookAngle());
            }
        }
    }

    // 执行X键触发的超必杀攻击（独立于叠加必杀的功能）
    private void executeXKeyUltimateAttack(Level level, Player player, ItemStack stack) {
        List<String> riders = getScrambleRiders(stack);
        
        // 计算总能量消耗（每个骑士的能量消耗之和）
        double totalEnergyCost = 0;
        for (String rider : riders) {
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                totalEnergyCost += effect.getEnergyCost();
            }
        }
        
        // 超必杀模式下能量消耗增加50%作为增强消耗
        totalEnergyCost *= 1.5;
        
        // 检查骑士能量是否足够
        if (!RiderEnergyHandler.canUseRiderEnergy(player) || !RiderEnergyHandler.consumeRiderEnergy(player, totalEnergyCost)) {
            // 能量不足，显示屏幕中央提示
            if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal("§c骑士能量不足，无法使用X键超必杀！"), true);
            }
            return;
        }
        
        // 移除特效数量限制，确保所有粒子效果都能显示
        for (int i = 0; i < riders.size(); i++) {
            String rider = riders.get(i);
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                // X键超必杀模式下特效增强
                effect.executeSpecialAttack(level, player, player.getLookAngle());
            }
        }

        // 添加额外的全屏特效或范围伤害
        executeUltimateSpecialEffects(level, player);
    }
    
    // 执行Ultimate Time Break（叠加必杀）
    private void executeUltimateTimeBreak(Level level, Player player, ItemStack stack) {
        List<String> riders = getScrambleRiders(stack);
        
        // 计算总能量消耗（每个骑士的能量消耗之和）
        double totalEnergyCost = 0;
        for (String rider : riders) {
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                totalEnergyCost += effect.getEnergyCost();
            }
        }
        
        // 超必杀模式下能量消耗增加50%作为增强消耗
        totalEnergyCost *= 1.5;
        
        // 检查骑士能量是否足够
        if (!RiderEnergyHandler.canUseRiderEnergy(player) || !RiderEnergyHandler.consumeRiderEnergy(player, totalEnergyCost)) {
            // 能量不足，显示屏幕中央提示
            if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal("§c骑士能量不足，无法使用Ultimate Time Break！"), true);
            }
            return;
        }
        
        // 移除特效数量限制，确保所有粒子效果都能显示
        for (int i = 0; i < riders.size(); i++) {
            String rider = riders.get(i);
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                // 叠加超必杀模式下特效增强
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

    // 远程攻击相关方法
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // 设置使用动画为弓的动画
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        // 设置最大使用时间
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int ticksRemaining) {
        if (!(shooter instanceof Player player) || level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        float chargeTime = (getUseDuration(stack) - ticksRemaining) / 20F;
        chargeTime = (chargeTime * chargeTime + chargeTime * 2.0F) / 3.0F;
        if (chargeTime > 1.0F) {
            chargeTime = 1.0F;
        }

        // 根据不同模式执行不同的远程攻击
        if (isFinishTimeMode(stack)) {
            if (isUltimateMode(stack)) {
                executeUltimateRangedAttack(player, stack, chargeTime);
            } else {
                executeScrambleRangedAttack(player, stack, chargeTime);
            }
        } else {
            executeNormalRangedAttack(player, stack, chargeTime);
        }

        // 触发远程攻击动画
        triggerRangedAttackAnimation(level, player, stack);

        // 消耗耐久
        stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(InteractionHand.MAIN_HAND));
    }

    // 触发远程攻击动画
    private void triggerRangedAttackAnimation(Level level, Player player, ItemStack stack) {
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "rotation", "pos1");
        }
    }

    // 执行普通模式远程攻击
    private void executeNormalRangedAttack(Player player, ItemStack stack, float chargeTime) {
        String rider = getSelectedRider(stack);
        if (rider != null && !rider.isEmpty()) {
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                // 远程攻击能量消耗根据充能时间调整
                double energyCost = HeiseiRiderEffectManager.getRiderEnergyCost(rider) * (0.5 + chargeTime * 0.5);
                
                // 检查骑士能量是否足够
                if (!RiderEnergyHandler.canUseRiderEnergy(player) || !RiderEnergyHandler.consumeRiderEnergy(player, energyCost)) {
                    // 能量不足，显示屏幕中央提示
                    if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.displayClientMessage(Component.literal("§c骑士能量不足，无法使用远程攻击！"), true);
                    }
                    return;
                }
                
                // 播放远程攻击音效
                SoundEvent nameSound = HeiseiRiderEffectManager.getRiderNameSound(rider);
                if (nameSound != null) {
                    RiderSounds.playAttackSound(player.level(), player, nameSound);
                }
                
                // 执行远程特殊攻击效果
                Vec3 lookAngle = player.getLookAngle().scale(chargeTime * 2.0);
                effect.executeSpecialAttack(player.level(), player, lookAngle);
                
                // 更新上次攻击时间
                setLastAttackTime(stack, player.level().getGameTime());
            }
        }
    }

    // 执行Scramble模式远程攻击
    private void executeScrambleRangedAttack(Player player, ItemStack stack, float chargeTime) {
        List<String> riders = getScrambleRiders(stack);
        if (!riders.isEmpty()) {
            // 计算总能量消耗（每个骑士的能量消耗之和）
            double totalEnergyCost = 0;
            for (String rider : riders) {
                HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
                if (effect != null) {
                    totalEnergyCost += effect.getEnergyCost();
                }
            }
            
            // Scramble模式下能量消耗增加20%作为组合消耗
            // 远程攻击能量消耗根据充能时间调整
            totalEnergyCost *= 1.2 * (0.5 + chargeTime * 0.5);
            
            // 检查骑士能量是否足够
            if (!RiderEnergyHandler.canUseRiderEnergy(player) || !RiderEnergyHandler.consumeRiderEnergy(player, totalEnergyCost)) {
                // 能量不足，显示屏幕中央提示
                if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.literal("§c骑士能量不足，无法使用Scramble远程攻击！"), true);
                }
                return;
            }
            
            // 播放远程攻击音效
            RiderSounds.playSound(player.level(), player, RiderSounds.SCRAMBLE_TIME_BREAK);
            
            // 对每个选中的骑士执行远程特殊攻击
            Vec3 lookAngle = player.getLookAngle().scale(chargeTime * 1.5);
            for (String rider : riders) {
                HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
                if (effect != null) {
                    effect.executeSpecialAttack(player.level(), player, lookAngle);
                }
            }
            
            // 更新上次攻击时间
            setLastAttackTime(stack, player.level().getGameTime());
        }
    }

    // 执行超必杀模式远程攻击
    private void executeUltimateRangedAttack(Player player, ItemStack stack, float chargeTime) {
        List<String> riders = getScrambleRiders(stack);
        if (!riders.isEmpty()) {
            // 计算总能量消耗（每个骑士的能量消耗之和）
            double totalEnergyCost = 0;
            for (String rider : riders) {
                HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
                if (effect != null) {
                    totalEnergyCost += effect.getEnergyCost();
                }
            }
            
            // 超必杀模式下能量消耗增加50%作为增强消耗
            // 远程攻击能量消耗根据充能时间调整
            totalEnergyCost *= 1.5 * (0.5 + chargeTime * 0.5);
            
            // 检查骑士能量是否足够
            if (!RiderEnergyHandler.canUseRiderEnergy(player) || !RiderEnergyHandler.consumeRiderEnergy(player, totalEnergyCost)) {
                // 能量不足，显示屏幕中央提示
                if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.literal("§c骑士能量不足，无法使用Ultimate远程攻击！"), true);
                }
                return;
            }
            
            // 播放远程超必杀音效
            RiderSounds.playSound(player.level(), player, RiderSounds.ULTIMATE_TIME_BREAK);
            
            // 对每个选中的骑士执行增强的远程特殊攻击
            Vec3 lookAngle = player.getLookAngle().scale(chargeTime * 3.0);
            for (String rider : riders) {
                HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
                if (effect != null) {
                    effect.executeSpecialAttack(player.level(), player, lookAngle);
                }
            }
            
            // 添加额外的全屏特效或范围伤害
            executeUltimateSpecialEffects(player.level(), player);
            
            // 更新上次攻击时间
            setLastAttackTime(stack, player.level().getGameTime());
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