package com.xiaoshi2022.kamenriderweaponcraft.Item.custom;

import com.xiaoshi2022.kamenriderweaponcraft.Item.client.Heiseisword.HeiseiswordRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.network.HeiseiswordRiderSelectionPacket;
import com.xiaoshi2022.kamenriderweaponcraft.network.NetworkHandler;
import com.xiaoshi2022.kamenriderweaponcraft.rider.core.CoreSlotManager;
import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.HeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.HeiseiRiderEffectManager;
import com.xiaoshi2022.kamenriderweaponcraft.rider.energy.HeiseiswordEnergyManager;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.den_o.DenOTrainEntity;
import com.xiaoshi2022.kamenriderweaponcraft.rider.sound.RiderSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Heiseisword extends SwordItem implements GeoItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(Heiseisword.class);

    public static boolean isHeiseisword(ItemStack stack) {
        return stack.getItem() instanceof Heiseisword;
    }

    public static boolean isHeiseisword(net.minecraft.world.entity.player.Player player) {
        if (player == null) return false;
        return isHeiseisword(player.getMainHandItem()) || isHeiseisword(player.getOffhandItem());
    }

    public static ItemStack getHeiseiswordFromPlayer(net.minecraft.world.entity.player.Player player) {
        if (isHeiseisword(player.getMainHandItem())) {
            return player.getMainHandItem();
        }
        if (isHeiseisword(player.getOffhandItem())) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    // 定义4个方位的旋转动画
    private static final RawAnimation ROTATE_POSITION_1 = RawAnimation.begin().thenPlay("rotate_pos1");
    private static final RawAnimation ROTATE_POSITION_2 = RawAnimation.begin().thenPlay("rotate_pos2");
    private static final RawAnimation ROTATE_POSITION_3 = RawAnimation.begin().thenPlay("rotate_pos3");
    private static final RawAnimation ROTATE_POSITION_4 = RawAnimation.begin().thenPlay("rotate_pos4");

    // 超必杀动画
    private static final RawAnimation ULTIMATE_TIME_BREAK_ANIM = RawAnimation.begin().thenPlay("ridertime");

    // 电王武器形态动画
    private static final RawAnimation SWORD_FORM_ANIM = RawAnimation.begin().thenLoop("animation.den_o.sword.idle");
    private static final RawAnimation FISHING_ROD_FORM_ANIM = RawAnimation.begin().thenLoop("animation.den_o.fishing_rod.idle");
    private static final RawAnimation AX_FORM_ANIM = RawAnimation.begin().thenLoop("animation.den_o.ax.idle");
    private static final RawAnimation GUN_FORM_ANIM = RawAnimation.begin().thenLoop("animation.den_o.gun.idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // NBT键名常量
    private static final String TAG_SELECTED_RIDER = "selectedRider";
    private static final String TAG_SCRAMBLE_RIDERS = "scrambleRiders";
    private static final String TAG_IS_FINISH_TIME_MODE = "isFinishTimeMode";
    private static final String TAG_IS_ULTIMATE_MODE = "isUltimateMode";
    private static final String TAG_ROTATION_COUNT = "rotationCount";
    private static final String TAG_LAST_ROTATION_TIME = "lastRotationTime";
    private static final String TAG_CURRENT_ROTATION_POSITION = "currentRotationPosition";
    private static final String TAG_LAST_ATTACK_TIME = "lastAttackTime";
    private static final String TAG_LAST_RIDER_SELECTION_TIME = "lastRiderSelectionTime";
    private static final String TAG_LAST_FINISH_TIME_ENTER = "lastFinishTimeEnter";
    private static final String TAG_DEN_O_WEAPON_TYPE = "den_o_weapon_type";
    private static final String TAG_HAS_ATTACHED_ENTITY = "has_attached_entity";
    private static final String TAG_SWORD_PROJECTILE_READY = "sword_projectile_ready";

    private static final int ATTACK_COOLDOWN_TICKS = 10;
    private static final int ULTIMATE_ATTACK_COOLDOWN_TICKS = 40;
    private static final int RIDER_SELECTION_COOLDOWN_TICKS = 15;
    private static final int FINISH_TIME_COOLDOWN_TICKS = 300;

    // 自定义 Tier - 统一使用这个
    public static final Tier HEISEI_SWORD_TIER = new Tier() {
        @Override
        public int getUses() { return 1000; }
        @Override
        public float getSpeed() { return 3f; }
        @Override
        public float getAttackDamageBonus() { return 33f; }
        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        }
        @Override
        public int getEnchantmentValue() { return 3; }
        @Override
        public Ingredient getRepairIngredient() { return Ingredient.of(); }
    };

    // 无参构造函数
    public Heiseisword() {
        super(HEISEI_SWORD_TIER, new Item.Properties()
                .stacksTo(1)
                .attributes(SwordItem.createAttributes(HEISEI_SWORD_TIER, 3, 2.4f))
                .setNoRepair());
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // 添加带参数的构造函数供 ItemRegister 调用
    public Heiseisword(Tier tier, Item.Properties properties) {
        super(tier, properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private HeiseiswordRenderer renderer;
            @Override
            public GeoItemRenderer<Heiseisword> getGeoItemRenderer() {
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

        controllers.add(new AnimationController<>(this, "den_o_sword", 20, state -> PlayState.STOP)
                .triggerableAnim("sword_idle", SWORD_FORM_ANIM));
        controllers.add(new AnimationController<>(this, "den_o_fishing_rod", 20, state -> PlayState.STOP)
                .triggerableAnim("fishing_rod_idle", FISHING_ROD_FORM_ANIM));
        controllers.add(new AnimationController<>(this, "den_o_ax", 20, state -> PlayState.STOP)
                .triggerableAnim("ax_idle", AX_FORM_ANIM));
        controllers.add(new AnimationController<>(this, "den_o_gun", 20, state -> PlayState.STOP)
                .triggerableAnim("gun_idle", GUN_FORM_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ==================== NBT 辅助方法 ====================

    private CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag();
    }

    private void setTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private long getLong(ItemStack stack, String key) {
        return getOrCreateTag(stack).getLong(key);
    }

    private void putLong(ItemStack stack, String key, long value) {
        CompoundTag tag = getOrCreateTag(stack);
        tag.putLong(key, value);
        setTag(stack, tag);
    }

    private int getInt(ItemStack stack, String key) {
        return getOrCreateTag(stack).getInt(key);
    }

    private void putInt(ItemStack stack, String key, int value) {
        CompoundTag tag = getOrCreateTag(stack);
        tag.putInt(key, value);
        setTag(stack, tag);
    }

    private boolean getBoolean(ItemStack stack, String key) {
        return getOrCreateTag(stack).getBoolean(key);
    }

    private void putBoolean(ItemStack stack, String key, boolean value) {
        CompoundTag tag = getOrCreateTag(stack);
        tag.putBoolean(key, value);
        setTag(stack, tag);
    }

    private String getString(ItemStack stack, String key) {
        return getOrCreateTag(stack).getString(key);
    }

    private void putString(ItemStack stack, String key, String value) {
        CompoundTag tag = getOrCreateTag(stack);
        tag.putString(key, value);
        setTag(stack, tag);
    }

    // ==================== 状态管理方法 ====================

    private String getSelectedRider(ItemStack stack) {
        return getString(stack, TAG_SELECTED_RIDER);
    }

    private void setSelectedRider(ItemStack stack, String riderName) {
        putString(stack, TAG_SELECTED_RIDER, riderName != null ? riderName : "");
    }

    private List<String> getScrambleRiders(ItemStack stack) {
        List<String> riders = new ArrayList<>();
        CompoundTag tag = getOrCreateTag(stack);
        if (!tag.contains(TAG_SCRAMBLE_RIDERS + "_size")) return riders;
        int size = tag.getInt(TAG_SCRAMBLE_RIDERS + "_size");
        for (int i = 0; i < size; i++) {
            riders.add(tag.getString(TAG_SCRAMBLE_RIDERS + "_" + i));
        }
        return riders;
    }

    private void setScrambleRiders(ItemStack stack, List<String> riders) {
        CompoundTag tag = getOrCreateTag(stack);
        if (tag.contains(TAG_SCRAMBLE_RIDERS + "_size")) {
            int oldSize = tag.getInt(TAG_SCRAMBLE_RIDERS + "_size");
            for (int i = 0; i < oldSize; i++) {
                tag.remove(TAG_SCRAMBLE_RIDERS + "_" + i);
            }
        }
        tag.putInt(TAG_SCRAMBLE_RIDERS + "_size", riders.size());
        for (int i = 0; i < riders.size(); i++) {
            tag.putString(TAG_SCRAMBLE_RIDERS + "_" + i, riders.get(i));
        }
        setTag(stack, tag);
    }

    private boolean isFinishTimeMode(ItemStack stack) {
        return getBoolean(stack, TAG_IS_FINISH_TIME_MODE);
    }

    private void setFinishTimeMode(ItemStack stack, boolean mode) {
        putBoolean(stack, TAG_IS_FINISH_TIME_MODE, mode);
    }

    private boolean isUltimateMode(ItemStack stack) {
        return getBoolean(stack, TAG_IS_ULTIMATE_MODE);
    }

    private void setUltimateMode(ItemStack stack, boolean mode) {
        putBoolean(stack, TAG_IS_ULTIMATE_MODE, mode);
    }

    private int getCurrentRotationPosition(ItemStack stack) {
        return getInt(stack, TAG_CURRENT_ROTATION_POSITION);
    }

    private void setCurrentRotationPosition(ItemStack stack, int position) {
        putInt(stack, TAG_CURRENT_ROTATION_POSITION, position);
    }

    private long getLastAttackTime(ItemStack stack) {
        return getLong(stack, TAG_LAST_ATTACK_TIME);
    }

    private void setLastAttackTime(ItemStack stack, long time) {
        putLong(stack, TAG_LAST_ATTACK_TIME, time);
    }

    private long getLastRiderSelectionTime(ItemStack stack) {
        return getLong(stack, TAG_LAST_RIDER_SELECTION_TIME);
    }

    private void setLastRiderSelectionTime(ItemStack stack, long time) {
        putLong(stack, TAG_LAST_RIDER_SELECTION_TIME, time);
    }

    private long getLastFinishTimeEnter(ItemStack stack) {
        return getLong(stack, TAG_LAST_FINISH_TIME_ENTER);
    }

    private void setLastFinishTimeEnter(ItemStack stack, long time) {
        putLong(stack, TAG_LAST_FINISH_TIME_ENTER, time);
    }

    private boolean isAttackOnCooldown(ItemStack stack, Level level) {
        long lastAttackTime = getLastAttackTime(stack);
        long currentTime = level.getGameTime();
        int cooldownTicks = isUltimateMode(stack) ? ULTIMATE_ATTACK_COOLDOWN_TICKS : ATTACK_COOLDOWN_TICKS;
        return (currentTime - lastAttackTime) < cooldownTicks;
    }

    private boolean isRiderSelectionOnCooldown(ItemStack stack, Level level) {
        long lastSelectionTime = getLastRiderSelectionTime(stack);
        long currentTime = level.getGameTime();
        return (currentTime - lastSelectionTime) < RIDER_SELECTION_COOLDOWN_TICKS;
    }

    private boolean isFinishTimeOnCooldown(ItemStack stack, Level level) {
        long lastEnterTime = getLastFinishTimeEnter(stack);
        long currentTime = level.getGameTime();
        return (currentTime - lastEnterTime) < FINISH_TIME_COOLDOWN_TICKS;
    }

    // 电王模式方法
    public String getDenOWeaponType(ItemStack stack) {
        return getString(stack, TAG_DEN_O_WEAPON_TYPE);
    }

    public void setDenOWeaponType(ItemStack stack, String weaponType) {
        putString(stack, TAG_DEN_O_WEAPON_TYPE, weaponType);
    }

    public boolean hasAttachedEntity(ItemStack stack) {
        return getBoolean(stack, TAG_HAS_ATTACHED_ENTITY);
    }

    public void setHasAttachedEntity(ItemStack stack, boolean hasEntity) {
        putBoolean(stack, TAG_HAS_ATTACHED_ENTITY, hasEntity);
    }

    private boolean isSwordProjectileReady(ItemStack stack) {
        return getBoolean(stack, TAG_SWORD_PROJECTILE_READY);
    }

    private void setSwordProjectileReady(ItemStack stack, boolean ready) {
        putBoolean(stack, TAG_SWORD_PROJECTILE_READY, ready);
    }

    public boolean isInDenOMode(ItemStack stack) {
        String type = getDenOWeaponType(stack);
        return type != null && !type.isEmpty();
    }

    public void cycleDenOWeaponForm(ItemStack stack) {
        String currentType = getDenOWeaponType(stack);
        String[] forms = {"Sword", "FishingRod", "Ax", "Gun"};
        int currentIndex = 0;
        for (int i = 0; i < forms.length; i++) {
            if (forms[i].equals(currentType)) {
                currentIndex = i;
                break;
            }
        }
        String nextType = forms[(currentIndex + 1) % forms.length];
        setDenOWeaponType(stack, nextType);
        setHasAttachedEntity(stack, true);
    }

    public void resetDenOMode(ItemStack stack) {
        setDenOWeaponType(stack, "");
        setHasAttachedEntity(stack, false);
    }

    // ==================== 外部表盘系统方法 ====================

    public boolean hasExternalCoreAttached(ItemStack stack) {
        return CoreSlotManager.hasAttachedCore(stack);
    }

    @Nullable
    public String getAttachedCoreId(ItemStack stack) {
        return CoreSlotManager.getAttachedCoreId(stack);
    }

    public void attachExternalCore(ItemStack stack, String coreId) {
        CoreSlotManager.attachCore(stack, coreId);
    }

    public void detachExternalCore(ItemStack stack) {
        CoreSlotManager.detachCore(stack);
    }

    @Nullable
    public ResourceLocation getCurrentModelLocation(ItemStack stack) {
        if (hasExternalCoreAttached(stack)) {
            return CoreSlotManager.getAttachedCoreModel(stack);
        }
        String selectedRider = getSelectedRider(stack);
        if (selectedRider != null) {
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(selectedRider);
            if (effect != null) {
                return effect.getExternalModelLocation();
            }
        }
        return null;
    }

    @Nullable
    public Supplier<ResourceLocation> getCurrentAnimController(ItemStack stack) {
        if (hasExternalCoreAttached(stack)) {
            return CoreSlotManager.getAttachedCoreAnimController(stack);
        }
        String selectedRider = getSelectedRider(stack);
        if (selectedRider != null) {
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(selectedRider);
            if (effect != null) {
                return effect.getExternalAnimController();
            }
        }
        return null;
    }

    @Nullable
    public HeiseiRiderEffect getCurrentRiderEffect(ItemStack stack) {
        if (hasExternalCoreAttached(stack)) {
            return CoreSlotManager.getAttachedCoreEffect(stack);
        }
        String selectedRider = getSelectedRider(stack);
        if (selectedRider != null) {
            return HeiseiRiderEffectManager.getRiderEffect(selectedRider);
        }
        return null;
    }

    public int getTotalDurability(ItemStack stack) {
        int baseUses = HEISEI_SWORD_TIER.getUses();
        int coreBonus = CoreSlotManager.getTotalDurabilityBonus(stack);
        return baseUses + coreBonus;
    }

    // ==================== 动画触发方法 ====================

    private void triggerRotationAnimation(Level level, Player player, ItemStack stack) {
        int position = getCurrentRotationPosition(stack);
        String animationName = "pos" + (position + 1);
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "rotation", animationName);
        }
    }

    private void triggerUltimateAnimation(Level level, Player player, ItemStack stack) {
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "ultimate", "ultimate_time_break");
        }
    }

    // ==================== 网络方法 ====================

    public void handleClientRiderSelection(boolean isXKeyDown) {
        if (Minecraft.getInstance().player != null) {
//            KamenRiderWeaponCraft.LOGGER.debug("发送骑士选择请求, isXKeyDown: {}", isXKeyDown);
            NetworkHandler.sendToServer(new HeiseiswordRiderSelectionPacket(isXKeyDown));
        }
    }

    // ==================== 核心游戏逻辑 ====================

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);


        // ========== 修复：先处理 Shift+右键（必杀时刻模式切换） ==========
        if (player.isShiftKeyDown()) {
            return toggleFinishTimeMode(level, player, stack);
        }

        // 外部表盘：开始蓄力，技能释放由 RiderWatchCraft 处理
        if (CoreSlotManager.hasAttachedCore(stack)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        String selectedRider = getSelectedRider(stack);
// 在 use 方法中，将 "DenO" 改为 "Den-O"
        if (selectedRider != null && selectedRider.equals("Den-O")) {  // 改为 Den-O
            String weaponType = getDenOWeaponType(stack);

            if ("Sword".equals(weaponType)) {
                if (isSwordProjectileReady(stack)) {
                    if (!level.isClientSide) {
                        spawnDenOTrainEntity(level, player, stack);
                        setSwordProjectileReady(stack, false);
                    }
                    level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
                    return InteractionResultHolder.success(stack);
                } else {
                    setSwordProjectileReady(stack, true);
                    if (!level.isClientSide && player instanceof ServerPlayer sp) {
                        sp.displayClientMessage(Component.literal("剑已准备就绪，再次右键发射"), true);
                    }
                    if (level.isClientSide) {
                        for (int i = 0; i < 8; i++) {
                            level.addParticle(ParticleTypes.FLAME,
                                    player.getX() + (level.random.nextDouble() - 0.5) * 0.8,
                                    player.getY() + 1.2 + (level.random.nextDouble() - 0.5) * 0.5,
                                    player.getZ() + (level.random.nextDouble() - 0.5) * 0.8,
                                    0.0, 0.02, 0.0);
                        }
                    }
                    return InteractionResultHolder.success(stack);
                }
            } else {
                cycleDenOWeaponForm(stack);
                if (!level.isClientSide && player instanceof ServerPlayer sp) {
                    sp.displayClientMessage(Component.literal("切换到电王武器形态: " + getDenOWeaponType(stack)), true);
                }
                return InteractionResultHolder.success(stack);
            }
        }

        // 关键：开始使用物品，这样才能触发蓄力
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    private InteractionResultHolder<ItemStack> toggleFinishTimeMode(Level level, Player player, ItemStack stack) {
        boolean currentMode = isFinishTimeMode(stack);
        boolean newMode = !currentMode;

        if (newMode && isFinishTimeOnCooldown(stack, level)) {
            long remainingTicks = FINISH_TIME_COOLDOWN_TICKS - (level.getGameTime() - getLastFinishTimeEnter(stack));
            int remainingSeconds = (int) remainingTicks / 20;
            if (!level.isClientSide && player instanceof ServerPlayer sp) {
                sp.displayClientMessage(Component.literal("必杀模式冷却中，剩余 " + remainingSeconds + " 秒"), true);
            }
            return InteractionResultHolder.pass(stack);
        }

        setFinishTimeMode(stack, newMode);

        if (newMode) {
            setLastFinishTimeEnter(stack, level.getGameTime());
            HeiseiRiderEffectManager.playFinishTimeSound(level, player);
            setScrambleRiders(stack, new ArrayList<>());
            setUltimateMode(stack, false);
            setCurrentRotationPosition(stack, 0);
        } else {
            setScrambleRiders(stack, new ArrayList<>());
            setSelectedRider(stack, null);
            setCurrentRotationPosition(stack, 0);
            setUltimateMode(stack, false);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // 按键检测已经在 KeyInputHandler 中处理
        // 这里不需要再做检测
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    public static void handleRiderSelectionOnServer(ServerPlayer player, ItemStack stack, boolean isXKeyDown) {
//        KamenRiderWeaponCraft.LOGGER.info("服务端收到骑士选择请求, 玩家: {}, isXKeyDown: {}", player.getName().getString(), isXKeyDown);

        if (stack.getItem() instanceof Heiseisword heiseisword && player.level() instanceof ServerLevel level) {
            if (!heiseisword.isRiderSelectionOnCooldown(stack, level)) {
                heiseisword.handleRiderSelectionInternal(player, stack, isXKeyDown);
                heiseisword.setLastRiderSelectionTime(stack, level.getGameTime());
            } else {
//                KamenRiderWeaponCraft.LOGGER.debug("骑士选择冷却中");
            }
        }
    }

    private void handleRiderSelectionInternal(Player player, ItemStack stack, boolean isXKeyDown) {
        if (isFinishTimeMode(stack)) {
            handleFinishTimeModeSelection(player, stack, isXKeyDown);
        } else {
            handleNormalModeSelection(player, stack);
        }
    }

    private void handleNormalModeSelection(Player player, ItemStack stack) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();
        String currentSelectedRider = getSelectedRider(stack);
        String newRider;

//        KamenRiderWeaponCraft.LOGGER.info("当前选择的骑士: {}", currentSelectedRider);

        if (currentSelectedRider == null || currentSelectedRider.isEmpty()) {
            HeiseiRiderEffectManager.playRiderTimeSound(player.level(), player);
            newRider = riderOrder.get(0);
            setSelectedRider(stack, newRider);
            setCurrentRotationPosition(stack, 0);
            KamenRiderWeaponCraft.LOGGER.info("首次选择骑士: {}", newRider);
        } else {
            int currentIndex = riderOrder.indexOf(currentSelectedRider);
            int nextIndex = (currentIndex + 1) % riderOrder.size();
            newRider = riderOrder.get(nextIndex);
            setSelectedRider(stack, newRider);
            setCurrentRotationPosition(stack, (getCurrentRotationPosition(stack) + 1) % 4);
//            KamenRiderWeaponCraft.LOGGER.info("切换骑士: {} -> {}", currentSelectedRider, newRider);
        }

        HeiseiRiderEffectManager.playSelectionSound(player.level(), player, getSelectedRider(stack));
        triggerRotationAnimation(player.level(), player, stack);

        String displayName = getRiderDisplayName(newRider);
        player.displayClientMessage(Component.literal("⚔ 选择骑士: " + displayName).withStyle(net.minecraft.ChatFormatting.GOLD), true);
    }

    private void handleFinishTimeModeSelection(Player player, ItemStack stack, boolean isXKeyDown) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();

        // X键触发超必杀
        if (isXKeyDown && !isUltimateMode(stack)) {
            setUltimateMode(stack, true);
            HeiseiRiderEffectManager.playUltimateActivationSound(player.level(), player);
            triggerUltimateAnimation(player.level(), player, stack);
            setScrambleRiders(stack, new ArrayList<>(riderOrder));

            // 显示超必杀激活消息
            player.displayClientMessage(
                    Component.literal("✨ 超必杀模式激活！ ✨").withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD),
                    true
            );
            return;
        }

        List<String> currentScrambleRiders = getScrambleRiders(stack);
        int totalRiders = riderOrder.size();

        if (currentScrambleRiders.isEmpty()) {
            // 第一次选择骑士
            String newRider = riderOrder.get(0);
            List<String> updatedRiders = new ArrayList<>();
            updatedRiders.add(newRider);
            setScrambleRiders(stack, updatedRiders);
            HeiseiRiderEffectManager.playSelectionSound(player.level(), player, newRider);
            setCurrentRotationPosition(stack, 0);

            // 显示选择消息
            String displayName = getRiderDisplayName(newRider);
            player.displayClientMessage(
                    Component.literal("⚡ 选择骑士: " + displayName + " (1/" + totalRiders + ")").withStyle(net.minecraft.ChatFormatting.GOLD),
                    true
            );
        } else {
            // 继续添加骑士
            String lastRider = currentScrambleRiders.get(currentScrambleRiders.size() - 1);
            int currentIndex = riderOrder.indexOf(lastRider);
            if (currentIndex == -1) currentIndex = 0;

            int nextIndex = (currentIndex + 1) % riderOrder.size();
            boolean found = false;

            while (nextIndex != currentIndex) {
                String candidate = riderOrder.get(nextIndex);
                if (!currentScrambleRiders.contains(candidate)) {
                    List<String> updatedRiders = new ArrayList<>(currentScrambleRiders);
                    updatedRiders.add(candidate);
                    setScrambleRiders(stack, updatedRiders);
                    HeiseiRiderEffectManager.playSelectionSound(player.level(), player, candidate);
                    setCurrentRotationPosition(stack, (getCurrentRotationPosition(stack) + 1) % 4);

                    // 显示添加骑士消息
                    String displayName = getRiderDisplayName(candidate);
                    int currentCount = updatedRiders.size();
                    player.displayClientMessage(
                            Component.literal("➕ 添加骑士: " + displayName + " (" + currentCount + "/" + totalRiders + ")").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE),
                            true
                    );
                    found = true;
                    break;
                }
                nextIndex = (nextIndex + 1) % riderOrder.size();
            }

            if (!found) {
                // 如果所有骑士都已选择，显示提示
                player.displayClientMessage(
                        Component.literal("✅ 已选择所有骑士！按攻击键释放!").withStyle(net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.BOLD),
                        true
                );
            }
        }

        // 触发旋转动画
        triggerRotationAnimation(player.level(), player, stack);

        // 显示当前已选骑士组合列表
        List<String> currentRiders = getScrambleRiders(stack);
        if (!currentRiders.isEmpty()) {
            String riderList = currentRiders.stream()
                    .map(this::getRiderDisplayName)
                    .collect(java.util.stream.Collectors.joining(" → "));
            player.displayClientMessage(
                    Component.literal("📀 组合: " + riderList).withStyle(net.minecraft.ChatFormatting.AQUA),
                    true
            );
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int ticksRemaining) {
        if (!(shooter instanceof Player player) || level.isClientSide) return;


        // ========== 修改：必杀时刻模式下，让 RiderWatchCraft 处理叠加 ==========
        if (CoreSlotManager.hasAttachedCore(stack)) {
            // 如果是必杀时刻模式，让 RiderWatchCraft 处理叠加技能
            // 不是必杀时刻模式，直接返回（只有外部表盘）
            boolean isFinishTimeMode = isFinishTimeMode(stack);
            if (!isFinishTimeMode) {
                return;  // 普通模式：只有外部表盘，跳过原版
            }
            // 必杀时刻模式：继续执行原版必杀，让 RiderWatchCraft 也执行外部表盘技能
            // 这样就会叠加
        }

        // 检查是否是电王剑形态且准备就绪
        String selectedRider = getSelectedRider(stack);
        String weaponType = getDenOWeaponType(stack);
        if (selectedRider != null && selectedRider.equals("Den-O") && "Sword".equals(weaponType) && isSwordProjectileReady(stack)) {
            spawnDenOTrainEntity(level, player, stack);
            setSwordProjectileReady(stack, false);

            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
            return;
        }

        // ========== 关键修复：正确计算蓄力时间 ==========
        // 获取最大使用时间（需要传入 LivingEntity）
        int maxUseDuration = this.getUseDuration(stack, shooter);
        // 计算已使用的刻数
        int ticksUsed = maxUseDuration - ticksRemaining;
        // 转换为秒
        float chargeTime = ticksUsed / 20.0F;
        // 二次曲线，让蓄力收益递减
        chargeTime = (chargeTime * chargeTime + chargeTime * 2.0F) / 3.0F;
        // 限制最大为1.0
        if (chargeTime > 1.0F) {
            chargeTime = 1.0F;
        }

        // 添加调试输出（可选，用于测试）
//        if (chargeTime > 0.1F) {
//            KamenRiderWeaponCraft.LOGGER.debug("蓄力时间: {}%, ticksUsed: {}", (int)(chargeTime * 100), ticksUsed);
//        }
        // ========== 蓄力计算结束 ==========

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
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // 使用弓的动画，支持蓄力
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        // 最大使用时间 72000 刻（60分钟）
        return 72000;
    }

    // 触发远程攻击动画
    private void triggerRangedAttackAnimation(Level level, Player player, ItemStack stack) {
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "rotation", "pos1");
        }
    }

    // 执行普通模式远程攻击
    private void executeNormalRangedAttack(Player player, ItemStack stack, float chargeTime) {
        HeiseiRiderEffect effect = getCurrentRiderEffect(stack);

        if (effect != null) {
            String riderId = hasExternalCoreAttached(stack) ?
                    getAttachedCoreId(stack) : getSelectedRider(stack);

            double energyCost = effect.getEnergyCost() * (0.8 + chargeTime * 0.7);
            energyCost = Math.min(energyCost, 40.0);

            if (!HeiseiswordEnergyManager.consumeEnergy(player, energyCost)) {
                return;
            }

            net.minecraft.sounds.SoundEvent nameSound = HeiseiRiderEffectManager.getRiderNameSound(riderId);
            if (nameSound != null) {
                RiderSounds.playAttackSound(player.level(), player, nameSound);
            }

            Vec3 lookAngle = player.getLookAngle().scale(chargeTime * 2.0);
            effect.executeSpecialAttack(player.level(), player, lookAngle);

            setLastAttackTime(stack, player.level().getGameTime());
        }
    }

    // 执行Scramble模式远程攻击
    private void executeScrambleRangedAttack(Player player, ItemStack stack, float chargeTime) {
        List<String> riders = getScrambleRiders(stack);
        if (!riders.isEmpty()) {
            // 计算总能量消耗（每个骑士的能量消耗之和）
            double totalEnergyCost = 0;
            for (String rider : riders) {
                totalEnergyCost += HeiseiRiderEffectManager.getRiderEnergyCost(rider);
            }

            // 远程攻击能量消耗根据充能时间调整
            totalEnergyCost *= (0.8 + chargeTime * 0.7);
            totalEnergyCost = Math.min(totalEnergyCost, 100.0);

            // 使用自定义的武器能量系统
            if (!HeiseiswordEnergyManager.consumeEnergy(player, totalEnergyCost)) {
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
                totalEnergyCost += HeiseiRiderEffectManager.getRiderEnergyCost(rider);
            }

            // 远程攻击能量消耗根据充能时间调整
            totalEnergyCost *= (0.9 + chargeTime * 0.8);
            totalEnergyCost = Math.min(totalEnergyCost, 100.0);

            // 使用自定义的武器能量系统
            if (!HeiseiswordEnergyManager.consumeEnergy(player, totalEnergyCost)) {
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

    private String getRiderDisplayName(String rider) {
        return switch (rider) {
            case "Build" -> "Build";
            case "Ex-Aid" -> "Ex-Aid";
            case "Ghost" -> "Ghost";
            case "Drive" -> "Drive";
            case "Gaim" -> "Gaim";
            case "Wizard" -> "Wizard";
            case "Fourze" -> "Fourze";
            case "OOO" -> "OOO";
            case "W" -> "Double";
            case "Decade" -> "Decade";
            case "Kiva" -> "Kiva";
            case "Den-O" -> "Den-O";
            case "Kabuto" -> "Kabuto";
            case "Hibiki" -> "Hibiki";
            case "Blade" -> "Blade";
            case "Faiz" -> "Faiz";
            case "Ryuki" -> "Ryuki";
            case "Agito" -> "Agito";
            case "Kuuga" -> "Kuuga";
            default -> rider;
        };
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        // 客户端粒子反馈
        if (player.level().isClientSide) {
            // 添加简单的攻击粒子效果，确保有视觉反馈
            for (int i = 0; i < 5; i++) {
                player.level().addParticle(
                        ParticleTypes.SWEEP_ATTACK,
                        entity.getX() + (player.level().random.nextDouble() - 0.5) * 1.5,
                        entity.getY() + player.level().random.nextDouble() * entity.getBoundingBox().getYsize(),
                        entity.getZ() + (player.level().random.nextDouble() - 0.5) * 1.5,
                        0, 0, 0
                );
            }
            // 客户端不需要处理服务端逻辑，直接返回
            return super.onLeftClickEntity(stack, player, entity);
        }

        // ========== 服务端逻辑 ==========
        float damageDealt = 0.0f;
        if (entity instanceof LivingEntity livingEntity) {
            damageDealt = livingEntity.getHealth();
        }

        if (isAttackOnCooldown(stack, player.level())) {
            return false;
        }

        boolean isXKeyUltimateReady = getBoolean(stack, "isXKeyUltimateReady");
        boolean handled = false;

        if (isXKeyUltimateReady) {
            executeXKeyUltimateAttack(player.level(), player, stack);
            putBoolean(stack, "isXKeyUltimateReady", false);
            handled = true;
        } else if (isFinishTimeMode(stack)) {
            handled = handleFinishTimeAttack(player, stack);
        } else {
            String rider = getSelectedRider(stack);
            if (rider != null && !rider.isEmpty()) {
                handled = handleNormalAttack(player, stack);
            }
        }

        if (handled) {
            setLastAttackTime(stack, player.level().getGameTime());
        }

        // 攻击完成后检查实体是否被击败
        if (entity instanceof LivingEntity livingEntity) {
            float actualDamage = damageDealt - livingEntity.getHealth();
            if (actualDamage > 0) {
                // 恢复能量
                HeiseiswordEnergyManager.recoverEnergyByDamage(player, actualDamage);
            }

            if (!livingEntity.isAlive() && handled) {
                // 击败实体，播放骑士名称音效
                if (isFinishTimeMode(stack)) {
                    List<String> riders = getScrambleRiders(stack);
                    HeiseiRiderEffectManager.playUltimateFinishSoundSequence(player.level(), player, riders);
                } else {
                    String rider = getSelectedRider(stack);
                    if (rider != null) {
                        net.minecraft.sounds.SoundEvent nameSound = HeiseiRiderEffectManager.getRiderNameSound(rider);
                        if (nameSound != null) {
                            RiderSounds.playSound(player.level(), player, nameSound);
                            RiderSounds.playDelayedSound(player.level(), player, RiderSounds.DUAL_TIME_BREAK, 40);
                        }
                    }
                }
            }
        }

        // 如果已处理，返回 true；否则执行默认攻击
        return handled ? true : super.onLeftClickEntity(stack, player, entity);
    }

    // 修改为返回 boolean
    private boolean handleNormalAttack(Player player, ItemStack stack) {
        if (isAttackOnCooldown(stack, player.level())) {
            return false;
        }

        String rider = getSelectedRider(stack);
        HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
        
        // 防御性检查：如果骑士名称无效或找不到效果，尝试使用第一个骑士
        if (effect == null) {
            List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();
            if (!riderOrder.isEmpty()) {
                rider = riderOrder.get(0);
                effect = HeiseiRiderEffectManager.getRiderEffect(rider);
                // 更新选中的骑士
                setSelectedRider(stack, rider);
            }
        }
        
        if (effect != null) {
            double energyCost = HeiseiRiderEffectManager.getRiderEnergyCost(rider) * 2.0;
            if (!HeiseiswordEnergyManager.consumeEnergy(player, energyCost)) {
                return false;  // 能量不足，允许默认攻击
            }

            // 执行特效
            effect.executeSpecialAttack(player.level(), player, player.getLookAngle());
            setLastAttackTime(stack, player.level().getGameTime());
            return true;  // 已处理，阻止默认攻击
        }
        return false;  // 没有特效，允许默认攻击
    }

    private boolean handleFinishTimeAttack(Player player, ItemStack stack) {
        if (isAttackOnCooldown(stack, player.level())) {
            return false;
        }

        List<String> riders = getScrambleRiders(stack);
        if (riders.isEmpty()) {
            return false;
        }

        double totalEnergyCost = 0;
        for (String rider : riders) {
            totalEnergyCost += HeiseiRiderEffectManager.getRiderEnergyCost(rider);
        }
        totalEnergyCost = Math.min(totalEnergyCost * 1.5, 100.0);

        if (!HeiseiswordEnergyManager.consumeEnergy(player, totalEnergyCost)) {
            return false;  // 能量不足，允许默认攻击
        }

        for (String rider : riders) {
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                effect.executeSpecialAttack(player.level(), player, player.getLookAngle());
            }
        }
        setLastAttackTime(stack, player.level().getGameTime());
        return true;
    }

    private void executeXKeyUltimateAttack(Level level, Player player, ItemStack stack) {
        List<String> riders = getScrambleRiders(stack);

        double totalEnergyCost = 0;
        for (String rider : riders) {
            totalEnergyCost += HeiseiRiderEffectManager.getRiderEnergyCost(rider);
        }
        totalEnergyCost = Math.min(totalEnergyCost * 1.8, 100.0);

        if (!HeiseiswordEnergyManager.consumeEnergy(player, totalEnergyCost)) return;  // 改为 player

        for (String rider : riders) {
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                effect.executeSpecialAttack(level, player, player.getLookAngle());
            }
        }
        executeUltimateSpecialEffects(level, player);
    }


    private void executeUltimateSpecialEffects(Level level, Player player) {
        if (!level.isClientSide) {
            level.explode(player, player.getX(), player.getY(), player.getZ(), 8.0f, Level.ExplosionInteraction.MOB);
            Vec3 playerPos = player.position();
            double range = 15.0;
            level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range),
                    entity -> entity != player).forEach(entity -> {
                entity.hurt(level.damageSources().playerAttack(player), 200.0f);
                entity.setDeltaMovement(entity.getDeltaMovement().add(
                        entity.position().subtract(playerPos).normalize().scale(2.0)));
            });
        }
    }

    /**
     * 为玩家触发剑形态动画，供 DenOTrainEntity 调用
     */
    public void triggerAnimationForPlayer(Player player, ItemStack stack) {
        if (player.level() instanceof ServerLevel serverLevel) {
            // 触发剑形态攻击动画
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "rotation", "pos1");
        }
    }

    private void spawnDenOTrainEntity(Level level, Player player, ItemStack stack) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        // 取消注释这一行
        DenOTrainEntity.spawn(serverLevel, player,
                player.getLookAngle().normalize().scale(1.5),
                16.0f, getDenOWeaponType(stack));

        LOGGER.debug("Spawning Den-O train entity - weapon type: {}", getDenOWeaponType(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        String selectedRider = getSelectedRider(stack);
        if (selectedRider != null && !selectedRider.isEmpty()) {
            // 骑士名称显示
            String displayName = switch (selectedRider) {
                case "Decade" -> "DCD";
                case "W" -> "Double";
                case "Fourze" -> "Fourze";
                case "Faiz" -> "Faiz";
                case "Kabuto" -> "Kabuto";
                case "Hibiki" -> "Hibiki";
                case "DenO" -> "电王";
                case "Agito" -> "Agito";
                case "Kuuga" -> "Kuuga";
                case "Blade" -> "Blade";
                case "Kiva" -> "Kiva";
                case "Ryuki" -> "Ryuki";
                case "Wizard" -> "Wizard";
                case "Ghost" -> "Ghost";
                case "Ex-Aid" -> "Ex-Aid";
                case "Build" -> "Build";
                case "Zi-O" -> "时王";
                default -> selectedRider;
            };
            tooltip.add(Component.literal("已选择: " + displayName).withStyle(net.minecraft.ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.literal("未选择骑士").withStyle(net.minecraft.ChatFormatting.GRAY));
        }

        if (isInDenOMode(stack)) {
            String weaponType = getDenOWeaponType(stack);
            String displayWeaponType = switch (weaponType) {
                case "Sword" -> "剑形态";
                case "FishingRod" -> "竿形态";
                case "Ax" -> "斧形态";
                case "Gun" -> "枪形态";
                default -> weaponType;
            };
            tooltip.add(Component.literal("电王模式: " + displayWeaponType).withStyle(net.minecraft.ChatFormatting.BLUE));
        }

        if (isFinishTimeMode(stack)) {
            tooltip.add(Component.literal("必杀时刻模式").withStyle(net.minecraft.ChatFormatting.RED));
            List<String> scrambleRiders = getScrambleRiders(stack);
            if (!scrambleRiders.isEmpty()) {
                tooltip.add(Component.literal("已选择 " + scrambleRiders.size() + " 位骑士").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            }
            if (isUltimateMode(stack)) {
                tooltip.add(Component.literal("超必杀准备就绪！").withStyle(net.minecraft.ChatFormatting.DARK_RED).withStyle(net.minecraft.ChatFormatting.BOLD));
            }
        }

        // 能量不在这里显示，因为 HUD 已经显示了能量条
    }

    // 静态方法供AI类调用
    public static String getSelectedRiderStatic(ItemStack stack) {
        CompoundTag tag = getStaticOrCreateTag(stack);
        return tag.getString(TAG_SELECTED_RIDER);
    }

    public static void setSelectedRiderStatic(ItemStack stack, String riderName) {
        CompoundTag tag = getStaticOrCreateTag(stack);
        tag.putString(TAG_SELECTED_RIDER, riderName != null ? riderName : "");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static CompoundTag getStaticOrCreateTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag();
    }
    
    private static void setStaticTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    public static void setLastAttackTimeStatic(ItemStack stack, long time) {
        CompoundTag tag = getStaticOrCreateTag(stack);
        tag.putLong(TAG_LAST_ATTACK_TIME, time);
        setStaticTag(stack, tag);
    }
    
    public static boolean isUltimateModeStatic(ItemStack stack) {
        CompoundTag tag = getStaticOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_ULTIMATE_MODE);
    }
    
    public static void setUltimateModeStatic(ItemStack stack, boolean mode) {
        CompoundTag tag = getStaticOrCreateTag(stack);
        tag.putBoolean(TAG_IS_ULTIMATE_MODE, mode);
        setStaticTag(stack, tag);
    }
    
    public static boolean isFinishTimeModeStatic(ItemStack stack) {
        CompoundTag tag = getStaticOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_FINISH_TIME_MODE);
    }
    
    public static void setFinishTimeModeStatic(ItemStack stack, boolean mode) {
        CompoundTag tag = getStaticOrCreateTag(stack);
        tag.putBoolean(TAG_IS_FINISH_TIME_MODE, mode);
        setStaticTag(stack, tag);
    }
    
    public static List<String> getScrambleRidersStatic(ItemStack stack) {
        List<String> riders = new ArrayList<>();
        CompoundTag tag = getStaticOrCreateTag(stack);
        if (!tag.contains(TAG_SCRAMBLE_RIDERS + "_size")) return riders;
        int size = tag.getInt(TAG_SCRAMBLE_RIDERS + "_size");
        for (int i = 0; i < size; i++) {
            riders.add(tag.getString(TAG_SCRAMBLE_RIDERS + "_" + i));
        }
        return riders;
    }
    
    public static void setScrambleRidersStatic(ItemStack stack, List<String> riders) {
        CompoundTag tag = getStaticOrCreateTag(stack);
        if (tag.contains(TAG_SCRAMBLE_RIDERS + "_size")) {
            int oldSize = tag.getInt(TAG_SCRAMBLE_RIDERS + "_size");
            for (int i = 0; i < oldSize; i++) {
                tag.remove(TAG_SCRAMBLE_RIDERS + "_" + i);
            }
        }
        tag.putInt(TAG_SCRAMBLE_RIDERS + "_size", riders.size());
        for (int i = 0; i < riders.size(); i++) {
            tag.putString(TAG_SCRAMBLE_RIDERS + "_" + i, riders.get(i));
        }
        setStaticTag(stack, tag);
    }
}