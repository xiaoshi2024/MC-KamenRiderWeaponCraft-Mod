package com.xiaoshi2022.kamen_rider_weapon_craft.items.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.component.ModComponents;
import com.xiaoshi2022.kamen_rider_weapon_craft.items.client.Heiseisword.HeiseiswordRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Heiseisword extends Item implements GeoItem {
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
            public @Nullable GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new HeiseiswordRenderer();

                return this.renderer;
            }
        });
    }
    // ========== 数据组件管理方法 ==========

    // 添加当前旋转位置组件
    private int getCurrentRotationPosition(ItemStack stack) {
        Integer value = stack.get(ModComponents.CURRENT_ROTATION_POSITION);
        return value != null ? value : 0;
    }

    private void setCurrentRotationPosition(ItemStack stack, int position) {
        stack.set(ModComponents.CURRENT_ROTATION_POSITION, position);
    }

    private String getSelectedRider(ItemStack stack) {
        return stack.get(ModComponents.SELECTED_RIDER);
    }

    private void setSelectedRider(ItemStack stack, String riderName) {
        if (riderName != null && !riderName.isEmpty()) {
            stack.set(ModComponents.SELECTED_RIDER, riderName);
        } else {
            stack.remove(ModComponents.SELECTED_RIDER);
        }
    }

    private List<String> getScrambleRiders(ItemStack stack) {
        List<String> riders = stack.get(ModComponents.SCRAMBLE_RIDERS);
        return riders != null ? new ArrayList<>(riders) : new ArrayList<>();
    }

    private void setScrambleRiders(ItemStack stack, List<String> riders) {
        stack.set(ModComponents.SCRAMBLE_RIDERS, new ArrayList<>(riders));
    }

    private boolean isFinishTimeMode(ItemStack stack) {
        Boolean value = stack.get(ModComponents.IS_FINISH_TIME_MODE);
        return value != null ? value : false;
    }

    private void setFinishTimeMode(ItemStack stack, boolean mode) {
        stack.set(ModComponents.IS_FINISH_TIME_MODE, mode);
    }

    private boolean isUltimateMode(ItemStack stack) {
        Boolean value = stack.get(ModComponents.IS_ULTIMATE_MODE);
        return value != null ? value : false;
    }

    private void setUltimateMode(ItemStack stack, boolean mode) {
        stack.set(ModComponents.IS_ULTIMATE_MODE, mode);
    }

    private long getLastFinishTimeEnter(ItemStack stack) {
        Long value = stack.get(ModComponents.LAST_FINISH_TIME_ENTER);
        return value != null ? value : 0L;
    }

    private void setLastFinishTimeEnter(ItemStack stack, long time) {
        stack.set(ModComponents.LAST_FINISH_TIME_ENTER, time);
    }

    private long getLastAttackTime(ItemStack stack) {
        Long value = stack.get(ModComponents.LAST_ATTACK_TIME);
        return value != null ? value : 0L;
    }

    private void setLastAttackTime(ItemStack stack, long time) {
        stack.set(ModComponents.LAST_ATTACK_TIME, time);
    }

    private long getLastRiderSelectionTime(ItemStack stack) {
        Long value = stack.get(ModComponents.LAST_RIDER_SELECTION_TIME);
        return value != null ? value : 0L;
    }

    private void setLastRiderSelectionTime(ItemStack stack, long time) {
        stack.set(ModComponents.LAST_RIDER_SELECTION_TIME, time);
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
            // 保存当前选择的骑士
            String savedRider = getSelectedRider(stack);
            setSelectedRider(stack, null);
            setCurrentRotationPosition(stack, 0);
            // 恢复之前选择的骑士
            setSelectedRider(stack, savedRider);

            // 播放音效
            if (player instanceof ServerPlayerEntity) {
                RiderSounds.playFinishTimeSound(world, player);
            }
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

    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;

        // 检查是否在主手
        boolean isSelected = player.getMainHandStack() == stack;
        if (isSelected) {
            handleRiderSelection(player, stack, world);
        }
    }

    private void handleRiderSelection(PlayerEntity player, ItemStack stack, World world) {
        // 检查冷却
        if (world.getTime() - getLastRiderSelectionTime(stack) < RIDER_SELECTION_COOLDOWN_TICKS) {
            return;
        }

        // 这里需要实现按键检测 - 在Fabric中通常使用输入回调
        // 暂时模拟按键检测，实际使用时需要连接到输入系统
        boolean selectionKeyPressed = checkRiderSelectionKeyPressed(player);

        if (selectionKeyPressed) {
            if (isFinishTimeMode(stack)) {
                handleFinishTimeModeSelection(player, stack, world);
            } else {
                handleNormalModeSelection(player, stack, world);
            }
            setLastRiderSelectionTime(stack, world.getTime());
        }
    }

    // 模拟按键检测 - 需要连接到实际的输入系统
    private boolean checkRiderSelectionKeyPressed(PlayerEntity player) {
        // 这里需要连接到Fabric的输入系统
        // 暂时返回false，实际使用时需要根据按键状态返回
        return false;
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
        RiderSounds.playSelectionSound(world, player, getSelectedRider(stack));

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

        // 检测超必杀触发键（需要连接到输入系统）
        boolean ultimateKeyPressed = checkUltimateKeyPressed(player);
        if (ultimateKeyPressed && !isUltimateMode(stack)) {
            setUltimateMode(stack, true);
            // 播放超必杀启动音效
            RiderSounds.playUltimateActivationSound(world, player);
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
            RiderSounds.playSelectionSound(world, player, newRider);
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
                    RiderSounds.playSelectionSound(world, player, candidate);
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

    // 模拟超必杀键检测 - 需要连接到实际的输入系统
    private boolean checkUltimateKeyPressed(PlayerEntity player) {
        // 这里需要连接到Fabric的输入系统
        // 暂时返回false，实际使用时需要根据按键状态返回
        return false;
    }

    // ========== 动画触发方法 ==========

    private void triggerRotationAnimation(World world, PlayerEntity player, ItemStack stack) {
        int position = getCurrentRotationPosition(stack);
        String animationName = "pos" + (position + 1);

        if (world instanceof ServerWorld serverWorld) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverWorld), "rotation", animationName);
        }
    }

    private void triggerUltimateAnimation(World world, PlayerEntity player, ItemStack stack) {
        if (world instanceof ServerWorld serverWorld) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverWorld), "ultimate", "ultimate_time_break");
        }
    }

    private void triggerRangedAttackAnimation(World world, PlayerEntity player, ItemStack stack) {
        if (world instanceof ServerWorld serverWorld) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverWorld), "rotation", "pos1");
        }
    }

    // ========== 攻击实现 ==========

    private void executeNormalRangedAttack(PlayerEntity player, ItemStack stack, float chargeTime) {
        if (chargeTime < 0.1F) return;

        String rider = getSelectedRider(stack);
        if (rider != null && !rider.isEmpty()) {
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

                setLastAttackTime(stack, player.getWorld().getTime());
            }
        }
    }

    private void executeScrambleRangedAttack(PlayerEntity player, ItemStack stack, float chargeTime) {
        if (chargeTime < 0.1F) return;

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
                effect.executeSpecialAttack(player.getWorld(), player, lookAngle);
            }
        }

        // 播放Scramble攻击音效
        RiderSounds.playSound(player.getWorld(), player, RiderSounds.SCRAMBLE_TIME_BREAK);

        setLastAttackTime(stack, player.getWorld().getTime());
    }

    private void executeUltimateRangedAttack(PlayerEntity player, ItemStack stack, float chargeTime) {
        if (chargeTime < 0.1F) return;

        List<String> riders = getScrambleRiders(stack);
        if (riders.isEmpty()) return;

        // 对每个选中的骑士执行增强的远程特殊攻击
        Vec3d lookAngle = player.getRotationVector().multiply(chargeTime * 3.0);
        for (String rider : riders) {
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                effect.executeSpecialAttack(player.getWorld(), player, lookAngle);
            }
        }

        // 播放超必杀音效
        RiderSounds.playSound(player.getWorld(), player, RiderSounds.ULTIMATE_TIME_BREAK);

        // 添加额外的特效
        executeUltimateSpecialEffects(player.getWorld(), player);

        setLastAttackTime(stack, player.getWorld().getTime());

        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("终极必杀发动!"), true);
            triggerUltimateAnimation(player.getWorld(), player, stack);
        }
    }

    // 执行超必杀特殊效果
    private void executeUltimateSpecialEffects(World world, PlayerEntity player) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) return;

        // 这里可以实现爆炸效果、范围伤害等
        // 示例：对周围敌人造成伤害
        world.getOtherEntities(player, player.getBoundingBox().expand(15.0),
                        entity -> entity instanceof LivingEntity && entity != player)
                .forEach(entity -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        DamageSource damageSource = player.getDamageSources().playerAttack(player);
                        float damageAmount = 200.0f;
                        livingEntity.damage(serverWorld, damageSource, damageAmount);
                    }
                });
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