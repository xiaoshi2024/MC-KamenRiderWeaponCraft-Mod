package com.xiaoshi2022.kamen_rider_weapon_craft.items.custom;


import com.mojang.datafixers.util.Pair;
import net.minecraft.text.Text;

import com.xiaoshi2022.kamen_rider_weapon_craft.component.ridermodComponents;
import com.xiaoshi2022.kamen_rider_weapon_craft.items.client.Heiseisword.HeiseiswordRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.key.KeyBindings;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy.HeiseiswordEnergyManager;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.sound.RiderSounds;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Heiseisword extends Item implements GeoItem {
    // 日志记录器
    private static final Logger LOGGER = LoggerFactory.getLogger(Heiseisword.class);
    // 动画定义
    private static final RawAnimation ROTATE_POSITION_1 = RawAnimation.begin().thenPlay("rotate_pos1");
    private static final RawAnimation ROTATE_POSITION_2 = RawAnimation.begin().thenPlay("rotate_pos2");
    private static final RawAnimation ROTATE_POSITION_3 = RawAnimation.begin().thenPlay("rotate_pos3");
    private static final RawAnimation ROTATE_POSITION_4 = RawAnimation.begin().thenPlay("rotate_pos4");
    private static final RawAnimation ULTIMATE_TIME_BREAK_ANIM = RawAnimation.begin().thenPlay("ridertime");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 冷却时间常量
    private static final int ATTACK_COOLDOWN_TICKS = 10;
    private static final int ULTIMATE_ATTACK_COOLDOWN_TICKS = 40;
    private static final int RIDER_SELECTION_COOLDOWN_TICKS = 15;
    private static final int FINISH_TIME_COOLDOWN_TICKS = 300;
    
    // 能量消耗常量
    private static final double NORMAL_ATTACK_ENERGY_COST = 10.0;
    private static final double SCRAMBLE_ATTACK_ENERGY_COST = 25.0;
    private static final double ULTIMATE_ATTACK_ENERGY_COST = 50.0;

    // 武器属性常量
    private static final float ATTACK_DAMAGE = 33.0f;
    private static final float ATTACK_SPEED = 2.4f;

    // 属性修饰符ID
    private static final Identifier ATTACK_DAMAGE_MODIFIER_ID = Identifier.of("kamen_rider_weapon_craft", "attack_damage");
    private static final Identifier ATTACK_SPEED_MODIFIER_ID = Identifier.of("kamen_rider_weapon_craft", "attack_speed");

    // 属性修饰符
    private static final EntityAttributeModifier ATTACK_DAMAGE_MODIFIER = new EntityAttributeModifier(
            ATTACK_DAMAGE_MODIFIER_ID, ATTACK_DAMAGE, EntityAttributeModifier.Operation.ADD_VALUE);

    private static final EntityAttributeModifier ATTACK_SPEED_MODIFIER = new EntityAttributeModifier(
            ATTACK_SPEED_MODIFIER_ID, ATTACK_SPEED, EntityAttributeModifier.Operation.ADD_VALUE);

    public Heiseisword(Settings settings) {
        // 重要：直接传递原始的settings给父类构造函数
        // 不要在这里修改settings，确保物品能正确设置ID
        super(settings);

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // 创建武器属性修饰符
    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(EntityAttributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER, AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER, AttributeModifierSlot.MAINHAND)
                .build();
    }

    // Utilise our own render hook to define our custom renderer
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private HeiseiswordRenderer renderer;

            @Override
            @Nullable
            public GeoItemRenderer<Heiseisword> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new HeiseiswordRenderer();

                return this.renderer;
            }
        });
    }
    // ========== 数据组件管理方法 ==========

    // 添加当前旋转位置组件
    private int getCurrentRotationPosition(ItemStack stack) {
        Integer value = stack.get(ridermodComponents.CURRENT_ROTATION_POSITION);
        return value != null ? value : 0;
    }

    private void setCurrentRotationPosition(ItemStack stack, int position) {
        stack.set(ridermodComponents.CURRENT_ROTATION_POSITION, position);
    }

    private String getSelectedRider(ItemStack stack) {
        return stack.get(ridermodComponents.SELECTED_RIDER);
    }

    private void setSelectedRider(ItemStack stack, String riderName) {
        if (riderName != null && !riderName.isEmpty()) {
            stack.set(ridermodComponents.SELECTED_RIDER, riderName);
        } else {
            stack.remove(ridermodComponents.SELECTED_RIDER);
        }
    }

    private List<String> getScrambleRiders(ItemStack stack) {
        List<String> riders = stack.get(ridermodComponents.SCRAMBLE_RIDERS);
        return riders != null ? new ArrayList<>(riders) : new ArrayList<>();
    }

    private void setScrambleRiders(ItemStack stack, List<String> riders) {
        stack.set(ridermodComponents.SCRAMBLE_RIDERS, new ArrayList<>(riders));
    }

    private boolean isFinishTimeMode(ItemStack stack) {
        Boolean value = stack.get(ridermodComponents.IS_FINISH_TIME_MODE);
        return value != null ? value : false;
    }

    private void setFinishTimeMode(ItemStack stack, boolean mode) {
        stack.set(ridermodComponents.IS_FINISH_TIME_MODE, mode);
    }

    private boolean isUltimateMode(ItemStack stack) {
        Boolean value = stack.get(ridermodComponents.IS_ULTIMATE_MODE);
        return value != null ? value : false;
    }

    private void setUltimateMode(ItemStack stack, boolean mode) {
        // 在Fabric中，设置组件后不需要手动调用markDirty，set方法会自动处理
        stack.set(ridermodComponents.IS_ULTIMATE_MODE, mode);
        // ItemStack本身没有markDirty()方法，组件更改会自动同步
    }

    private long getLastFinishTimeEnter(ItemStack stack) {
        Long value = stack.get(ridermodComponents.LAST_FINISH_TIME_ENTER);
        return value != null ? value : 0L;
    }

    private void setLastFinishTimeEnter(ItemStack stack, long time) {
        stack.set(ridermodComponents.LAST_FINISH_TIME_ENTER, time);
    }

    private long getLastAttackTime(ItemStack stack) {
        Long value = stack.get(ridermodComponents.LAST_ATTACK_TIME);
        return value != null ? value : 0L;
    }

    private void setLastAttackTime(ItemStack stack, long time) {
        stack.set(ridermodComponents.LAST_ATTACK_TIME, time);
    }

    private long getLastRiderSelectionTime(ItemStack stack) {
        Long value = stack.get(ridermodComponents.LAST_RIDER_SELECTION_TIME);
        return value != null ? value : 0L;
    }

    private void setLastRiderSelectionTime(ItemStack stack, long time) {
        stack.set(ridermodComponents.LAST_RIDER_SELECTION_TIME, time);
    }

    // ========== GeckoLib 动画相关 ==========

    // GeckoLib 5 动画控制器注册
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("rotation",20, state -> PlayState.STOP)
                .triggerableAnim("pos1", ROTATE_POSITION_1)
                .triggerableAnim("pos2", ROTATE_POSITION_2)
                .triggerableAnim("pos3", ROTATE_POSITION_3)
                .triggerableAnim("pos4", ROTATE_POSITION_4));

        controllers.add(new AnimationController<>("ultimate", 20, state -> PlayState.STOP)
                .triggerableAnim("ultimate_time_break", ULTIMATE_TIME_BREAK_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ========== 物品行为方法 ==========

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (player.isSneaking()) {
            return toggleFinishTimeMode(world, player, stack);
        }

        player.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player) || world.isClient()) return false;

        // 检查攻击冷却
        if (isAttackOnCooldown(stack, world)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("攻击冷却中..."), true);
            }
            return false;
        }

        float chargeTime = (getMaxUseTime(stack, user) - remainingUseTicks) / 20F;
        chargeTime = Math.min((chargeTime * chargeTime + chargeTime * 2.0F) / 3.0F, 1.0F);

        if (isFinishTimeMode(stack)) {
            if (isUltimateMode(stack)) {
                executeUltimateRangedAttack(player, stack, chargeTime);
            } else {
                executeScrambleRangedAttack(player, stack, chargeTime);
            }
        } else {
            executeNormalRangedAttack(player, stack, chargeTime);
        }

        triggerRangedAttackAnimation(world, player, stack);
        stack.damage(1, player, EquipmentSlot.MAINHAND);
        return false;
    }

    // ========== 核心功能方法 ==========

    private ActionResult toggleFinishTimeMode(World world, PlayerEntity player, ItemStack stack) {
        boolean currentMode = isFinishTimeMode(stack);
        boolean newMode = !currentMode;

        if (newMode && isFinishTimeOnCooldown(stack, world)) {
            long remainingTicks = FINISH_TIME_COOLDOWN_TICKS - (world.getTime() - getLastFinishTimeEnter(stack));
            int remainingSeconds = (int) remainingTicks / 20;
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("必杀模式冷却中，剩余 " + remainingSeconds + " 秒"), true);
            }
            return ActionResult.PASS;
        }

        setFinishTimeMode(stack, newMode);

        if (newMode) {
            setLastFinishTimeEnter(stack, world.getTime());
            setScrambleRiders(stack, new ArrayList<>());
            setUltimateMode(stack, false);
            setCurrentRotationPosition(stack, 0);

            // 移除音效播放，所有音效将在击败实体时由EntityDeathEventListener统一播放
        } else {
            setScrambleRiders(stack, new ArrayList<>());
            setSelectedRider(stack, null);
            setUltimateMode(stack, false);
            setCurrentRotationPosition(stack, 0);
        }
        return ActionResult.SUCCESS;
    }

    private boolean isFinishTimeOnCooldown(ItemStack stack, World world) {
        long lastEnterTime = getLastFinishTimeEnter(stack);
        return (world.getTime() - lastEnterTime) < FINISH_TIME_COOLDOWN_TICKS;
    }

    // ========== 完整的骑士选择逻辑 ==========

    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, int slotForDisplay) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;

        // 检查是否在主手
        boolean isSelected = player.getMainHandStack() == stack;
        if (isSelected) {
            handleRiderSelection(player, stack, world);
        }
    }

    private void handleRiderSelection(PlayerEntity player, ItemStack stack, World world) {
        // 只在客户端运行，服务端的按键处理由静态方法handleRiderSelectionKeyPress处理
        if (world.isClient) {
            // 检查冷却
            if (world.getTime() - getLastRiderSelectionTime(stack) < RIDER_SELECTION_COOLDOWN_TICKS) {
                return;
            }

            // 检查骑士选择键是否被按下
            boolean selectionKeyPressed = checkRiderSelectionKeyPressed(player);

            if (selectionKeyPressed) {
                // 客户端只做动画预览，实际效果由服务端处理
                // 按键事件会通过数据包发送到服务端
            }
        }
    }

    // 检查骑士选择键是否被按下
    private boolean checkRiderSelectionKeyPressed(PlayerEntity player) {
        // 客户端：直接检测按键状态
        if (player.getWorld().isClient) {
            try {
                // 使用反射避免直接引用客户端类
                Object keyBinding = KeyBindings.getRiderSelectionKey();
                if (keyBinding != null) {
                    return (boolean) keyBinding.getClass().getMethod("isPressed").invoke(keyBinding);
                }
            } catch (Exception e) {
                // 避免在某些环境下客户端特有类加载失败
            }
        }
        // 服务器端：现在通过数据包同步，这个方法主要在客户端使用
        // 服务端的按键处理由handleRiderSelectionKeyPress静态方法处理
        return false;
    }
    
    /**
     * 静态方法：处理从服务端接收到的骑士选择按键事件
     */
    public static void handleRiderSelectionKeyPress(ServerPlayerEntity player) {
        ItemStack mainHandStack = player.getMainHandStack();
        if (mainHandStack.getItem() instanceof Heiseisword heiseisword) {
            // 检查冷却
            if (player.getWorld().getTime() - heiseisword.getLastRiderSelectionTime(mainHandStack) < RIDER_SELECTION_COOLDOWN_TICKS) {
                return;
            }
            
            if (heiseisword.isFinishTimeMode(mainHandStack)) {
                heiseisword.handleFinishTimeModeSelection(player, mainHandStack, player.getWorld());
            } else {
                heiseisword.handleNormalModeSelection(player, mainHandStack, player.getWorld());
            }
            heiseisword.setLastRiderSelectionTime(mainHandStack, player.getWorld().getTime());
        }
    }

    // 处理普通模式下的骑士选择
    private void handleNormalModeSelection(PlayerEntity player, ItemStack stack, World world) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();

        String currentSelectedRider = getSelectedRider(stack);
        if (currentSelectedRider == null || currentSelectedRider.isEmpty()) {
            // 第一次使用，播放启动音效并选择第一个骑士
            RiderSounds.playRiderTimeSound(world, player);
            setSelectedRider(stack, riderOrder.get(0));
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

        // 播放选择音效
        String selectedRider = getSelectedRider(stack);
        SoundEvent nameSound = HeiseiRiderEffectManager.getRiderNameSound(selectedRider);
        if (nameSound != null) {
            RiderSounds.playSelectionSound(world, player, nameSound);
        }

        // 触发分段旋转动画
        triggerRotationAnimation(world, player, stack);

        // 显示选择信息
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("已选择骑士: " + getSelectedRider(stack)), true);
        }
    }

    // 处理Finish Time模式下的骑士选择
    private void handleFinishTimeModeSelection(PlayerEntity player, ItemStack stack, World world) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();

        // 检测超必杀触发键
        boolean ultimateKeyPressed = checkUltimateKeyPressed(player);
        if (ultimateKeyPressed && !isUltimateMode(stack)) {
            setUltimateMode(stack, true);
            // 播放快速Hey Say音效
            RiderSounds.playRapidSelectionSound(world, player);
            triggerUltimateAnimation(world, player, stack);

            // 自动选择所有骑士用于超必杀
            setScrambleRiders(stack, new ArrayList<>(riderOrder));

            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("超必杀模式激活! 已选择所有骑士"), true);
            }
            return;
        }

        // 顺时针选择骑士并添加到列表
        List<String> currentScrambleRiders = getScrambleRiders(stack);

        if (currentScrambleRiders.isEmpty()) {
            // 从第一个骑士开始顺时针选择
            String newRider = riderOrder.get(0);
            List<String> updatedRiders = new ArrayList<>();
            updatedRiders.add(newRider);
            setScrambleRiders(stack, updatedRiders);
            SoundEvent nameSound = HeiseiRiderEffectManager.getRiderNameSound(newRider);
            if (nameSound != null) {
                RiderSounds.playSelectionSound(world, player, nameSound);
            }
            setCurrentRotationPosition(stack, 0);

            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("选择了第一个骑士: " + newRider), true);
            }
        } else {
            // 顺时针选择下一个未被选择的骑士
            String lastRider = currentScrambleRiders.get(currentScrambleRiders.size() - 1);
            int currentIndex = riderOrder.indexOf(lastRider);

            // 防御性编程
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
                    SoundEvent nameSound = HeiseiRiderEffectManager.getRiderNameSound(candidate);
                    if (nameSound != null) {
                        RiderSounds.playSelectionSound(world, player, nameSound);
                    }
                    setCurrentRotationPosition(stack, (getCurrentRotationPosition(stack) + 1) % 4);

                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.sendMessage(Text.literal("选择了骑士: " + candidate), true);
                    }
                    break;
                }
                nextIndex = (nextIndex + 1) % riderOrder.size();
            }
        }

        // 显示当前已选骑士信息
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("当前已选骑士: " + getScrambleRiders(stack)), true);
        }

        // 触发旋转动画
        triggerRotationAnimation(world, player, stack);
    }

    // 检查超必杀键是否被按下
    private boolean checkUltimateKeyPressed(PlayerEntity player) {
        // 客户端：直接检测按键状态
        if (player.getWorld().isClient) {
            try {
                // 使用反射避免直接引用客户端类
                Object keyBinding = KeyBindings.getUltimateModeKey();
                if (keyBinding != null) {
                    return (boolean) keyBinding.getClass().getMethod("isPressed").invoke(keyBinding);
                }
            } catch (Exception e) {
                // 避免在某些环境下客户端特有类加载失败
            }
        }
        // 服务器端：现在通过数据包同步，这个方法主要在客户端使用
        // 服务端的按键处理由handleUltimateKeyPress静态方法处理
        return false;
    }
    
    /**
     * 静态方法：处理从服务端接收到的终极模式按键事件
     * 修改为确保所有玩家在多人游戏中都能触发超必杀
     */
    public static void handleUltimateKeyPress(ServerPlayerEntity player) {
        ItemStack mainHandStack = player.getMainHandStack();
        if (mainHandStack.getItem() instanceof Heiseisword heiseisword) {
            // 详细日志，帮助调试多人游戏问题
            LOGGER.info("Player {} attempting to activate ultimate mode. FinishTimeMode: {}, UltimateMode: {}",
                    player.getName().getString(),
                    heiseisword.isFinishTimeMode(mainHandStack),
                    heiseisword.isUltimateMode(mainHandStack));

            if (heiseisword.isFinishTimeMode(mainHandStack)) {
                // 检查是否在Finish Time模式下且未激活超必杀
                if (!heiseisword.isUltimateMode(mainHandStack)) {
                    // 确保所有玩家都能激活超必杀模式
                    heiseisword.setUltimateMode(mainHandStack, true);

                    // 自动选择所有骑士用于超必杀
                    List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();
                    heiseisword.setScrambleRiders(mainHandStack, new java.util.ArrayList<>(riderOrder));

                    // 关键修复：在设置所有状态后再同步
                    syncItemStackToAllClients(player, mainHandStack);

                    // 确保动画在多人游戏中正确触发
                    heiseisword.triggerUltimateAnimation(player.getWorld(), player, mainHandStack);

                    // 播放快速Hey Say音效
                    RiderSounds.playRapidSelectionSound(player.getWorld(), player);

                    // 发送消息给玩家
                    player.sendMessage(Text.literal("超必杀模式激活! 已选择所有骑士"), true);

                    // 添加详细日志
                    LOGGER.info("Player {} successfully activated ultimate mode. Synced to all clients.",
                            player.getName().getString());

                } else {
                    // 如果已经是超必杀模式，发送提示
                    player.sendMessage(Text.literal("已经处于超必杀模式中!"), true);
                }
            } else {
                // 如果不在Finish Time模式，提示玩家
                player.sendMessage(Text.literal("需要先进入Finish Time模式!"), true);
            }
        } else {
            LOGGER.warn("Player {} attempted ultimate mode without Heiseisword in main hand",
                    player.getName().getString());
        }
    }

    /**
     * 关键修复：同步物品堆栈状态到所有客户端
     */
    private static void syncItemStackToAllClients(ServerPlayerEntity player, ItemStack stack) {
        if (player == null || stack.isEmpty()) return;
        
        try {
            // 方法1: 同步整个物品栏（基础同步）
            player.playerScreenHandler.syncState();
            player.getInventory().markDirty();
            
            // 方法2: 发送装备更新包（更精确）
            ServerWorld world = (ServerWorld) player.getWorld();
            List<Pair<EquipmentSlot, ItemStack>> equipmentUpdates = new ArrayList<>();
            equipmentUpdates.add(new Pair<>(EquipmentSlot.MAINHAND, stack));
            
            EntityEquipmentUpdateS2CPacket packet = new EntityEquipmentUpdateS2CPacket(player.getId(), equipmentUpdates);
            world.getChunkManager().sendToNearbyPlayers(player, packet);
            
            // 确保玩家自己也能看到更新
            player.networkHandler.sendPacket(packet);
            
            LOGGER.debug("Successfully synced item stack state for player: {}", player.getName().getString());
        } catch (Exception e) {
            LOGGER.error("Failed to sync item stack for player {}: {}", 
                    player.getName().getString(), e.getMessage());
        }
    }

    // ========== 动画触发方法 ==========

    private void triggerRotationAnimation(World world, PlayerEntity player, ItemStack stack) {
        int position = getCurrentRotationPosition(stack);
        String animationName = "pos" + (position + 1);

        if (world instanceof ServerWorld serverWorld) {
            // 修复多人游戏动画同步问题，正确传递动画触发器
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverWorld), "rotation", animationName);
        }
    }

    private void triggerUltimateAnimation(World world, PlayerEntity player, ItemStack stack) {
        if (world instanceof ServerWorld serverWorld) {
            // 修复多人游戏超必杀动画同步问题，确保所有玩家都能触发
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverWorld), "ultimate", "ultimate_time_break");
        }
    }

    private void triggerRangedAttackAnimation(World world, PlayerEntity player, ItemStack stack) {
        if (world instanceof ServerWorld serverWorld) {
            // 修复多人游戏远程攻击动画同步问题
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverWorld), "rotation", "pos1");
        }
    }

    // ========== 攻击实现 ==========

    private void executeNormalRangedAttack(PlayerEntity player, ItemStack stack, float chargeTime) {
        if (chargeTime < 0.1F) return;

        // 检查能量是否足够
        if (!HeiseiswordEnergyManager.canUseEnergy(player, NORMAL_ATTACK_ENERGY_COST)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("能量不足! 当前能量: " + HeiseiswordEnergyManager.getCurrentEnergy(player)), true);
            }
            return;
        }

        String rider = getSelectedRider(stack);
        if (rider == null || rider.isEmpty()) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("请先选择骑士!"), true);
            }
            return;
        }

        HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
        if (effect != null) {
            // 执行远程特殊攻击效果
            Vec3d lookAngle = player.getRotationVector().multiply(chargeTime * 2.0);
            effect.executeSpecialAttack(player.getWorld(), player, lookAngle);

            // 播放远程攻击音效
            SoundEvent nameSound = HeiseiRiderEffectManager.getRiderNameSound(rider);
            if (nameSound != null) {
                RiderSounds.playAttackSound(player.getWorld(), player, nameSound);
            }

            // 消耗能量
            HeiseiswordEnergyManager.consumeEnergy(player, NORMAL_ATTACK_ENERGY_COST);
            
            setLastAttackTime(stack, player.getWorld().getTime());
            
            // 显示攻击信息
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("发动 " + rider + " 攻击! 剩余能量: " + HeiseiswordEnergyManager.getCurrentEnergy(player)), true);
            }
        }
    }

    private void executeScrambleRangedAttack(PlayerEntity player, ItemStack stack, float chargeTime) {
        if (chargeTime < 0.1F) return;

        // 检查能量是否足够
        if (!HeiseiswordEnergyManager.canUseEnergy(player, SCRAMBLE_ATTACK_ENERGY_COST)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("能量不足! 当前能量: " + HeiseiswordEnergyManager.getCurrentEnergy(player)), true);
            }
            return;
        }

        List<String> scrambleRiders = getScrambleRiders(stack);
        if (scrambleRiders.isEmpty()) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("需要先选择骑士!"), true);
            }
            return;
        }

        // 对每个选中的骑士执行远程特殊攻击
        Vec3d lookAngle = player.getRotationVector().multiply(chargeTime * 1.5);
        for (String rider : scrambleRiders) {
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                // 增强版攻击：基于chargeTime增加伤害
                float damageMultiplier = 1.0f + (chargeTime * 0.5f);
                // 这里可以通过修改lookAngle来调整攻击强度
                effect.executeSpecialAttack(player.getWorld(), player, lookAngle);
            }
        }

        // 移除音效播放，所有音效将在击败实体时由EntityDeathEventListener统一播放

        // 消耗能量
        HeiseiswordEnergyManager.consumeEnergy(player, SCRAMBLE_ATTACK_ENERGY_COST);
        
        setLastAttackTime(stack, player.getWorld().getTime());
        
        // 移除重置必杀模式，让玩家可以持续使用Scramble模式
        // setFinishTimeMode(stack, false);
        
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("Scramble Time Break! 剩余能量: " + HeiseiswordEnergyManager.getCurrentEnergy(player)), true);
        }
    }

    private void executeUltimateRangedAttack(PlayerEntity player, ItemStack stack, float chargeTime) {
        if (chargeTime < 0.1F) return;

        // 确保在服务器端执行，并且为所有玩家提供相同的功能
        if (player instanceof ServerPlayerEntity serverPlayer) {
            // 检查能量是否足够
            if (!HeiseiswordEnergyManager.canUseEnergy(player, ULTIMATE_ATTACK_ENERGY_COST)) {
                serverPlayer.sendMessage(Text.literal("能量不足! 当前能量: " + HeiseiswordEnergyManager.getCurrentEnergy(player)), true);
                return;
            }

            List<String> riders = getScrambleRiders(stack);
            if (riders.isEmpty()) {
                serverPlayer.sendMessage(Text.literal("未选择骑士!"), true);
                return;
            }

            // 对每个选中的骑士执行超增强的远程特殊攻击
            Vec3d lookAngle = player.getRotationVector().multiply(chargeTime * 3.0);
            for (String rider : riders) {
                HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
                if (effect != null) {
                    // 超必杀版攻击：基于chargeTime大幅增加伤害
                    float damageMultiplier = 2.0f + (chargeTime * 1.0f);
                    // 执行特殊攻击，确保所有玩家都能触发
                    effect.executeSpecialAttack(player.getWorld(), player, lookAngle);
                }
            }

            // 添加额外的特效
            executeUltimateSpecialEffects(player.getWorld(), player);

            // 消耗能量
            HeiseiswordEnergyManager.consumeEnergy(player, ULTIMATE_ATTACK_ENERGY_COST);
            
            setLastAttackTime(stack, player.getWorld().getTime());
            
            // 攻击后重置所有模式
            setFinishTimeMode(stack, false);
            setUltimateMode(stack, false);

            // 发送消息和触发动画
            serverPlayer.sendMessage(Text.literal("ULTIMATE TIME BREAK! 剩余能量: " + HeiseiswordEnergyManager.getCurrentEnergy(player)), true);
            triggerUltimateAnimation(player.getWorld(), player, stack);
        }
    }

    // 执行超必杀特殊效果
    private void executeUltimateSpecialEffects(World world, PlayerEntity player) {
        // 确保在服务器端执行，并且为所有玩家提供相同的功能
        if (world.isClient() || !(world instanceof ServerWorld serverWorld) || !(player instanceof ServerPlayerEntity)) {
            return;
        }

        // 实现爆炸效果和范围伤害
        // 1. 创建视觉效果（可以在实际实现中添加粒子效果）
        
        // 2. 对周围敌人造成范围伤害
        double radius = 20.0;
        float baseDamage = 150.0f;
        
        // 确保为所有玩家计算范围伤害，不受房主限制
        world.getOtherEntities(player, player.getBoundingBox().expand(radius),
                        entity -> entity instanceof LivingEntity && entity != player)
                .forEach(entity -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        // 计算距离，距离越近伤害越高
                        double distance = entity.squaredDistanceTo(player);
                        double damageMultiplier = Math.max(0.5, 1.0 - (distance / (radius * radius)));
                        float finalDamage = (float)(baseDamage * damageMultiplier);
                        
                        // 使用正确的伤害来源，确保所有玩家都能正确应用伤害
                        DamageSource damageSource = player.getDamageSources().playerAttack(player);
                        livingEntity.damage(serverWorld, damageSource, finalDamage);
                        
                        // 添加击退效果
                        Vec3d direction = entity.getPos().subtract(player.getPos()).normalize();
                        livingEntity.addVelocity(direction.x * 1.5, 0.5, direction.z * 1.5);
                    }
                });
        
        // 3. 播放额外的爆炸音效（如果有）
        // RiderSounds.playSound(world, player, EXPLOSION_SOUND);
    }

    // ========== 攻击冷却检查 ==========

    private boolean isAttackOnCooldown(ItemStack stack, World world) {
        long lastAttackTime = getLastAttackTime(stack);
        long currentTime = world.getTime();
        boolean isUltimate = isUltimateMode(stack);
        int cooldownTicks = isUltimate ? ULTIMATE_ATTACK_COOLDOWN_TICKS : ATTACK_COOLDOWN_TICKS;
        return (currentTime - lastAttackTime) < cooldownTicks;
    }
}