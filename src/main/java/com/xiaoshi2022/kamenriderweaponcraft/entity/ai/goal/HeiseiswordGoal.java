package com.xiaoshi2022.kamenriderweaponcraft.entity.ai.goal;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.HeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.HeiseiRiderEffectManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeiseiswordGoal extends Goal {
    private static final Logger LOGGER = LoggerFactory.getLogger(KamenRiderWeaponCraft.MODID + "/HeiseiswordGoal");

    private final LivingEntity entity;
    private final Random random;
    private int cooldown = 0;
    private int riderSwitchCooldown = 0;
    private int modeSwitchCooldown = 0;
    private int chargedAttackCooldown = 0;
    private int dodgeCooldown = 0;

    // 骑士切换间隔（3-6秒）
    private static final int RIDER_SWITCH_INTERVAL_MIN = 60;
    private static final int RIDER_SWITCH_INTERVAL_MAX = 120;

    // 模式切换间隔（10-20秒）
    private static final int MODE_SWITCH_INTERVAL_MIN = 200;
    private static final int MODE_SWITCH_INTERVAL_MAX = 400;

    // 蓄力攻击冷却（5-10秒）
    private static final int CHARGED_ATTACK_COOLDOWN_MIN = 100;
    private static final int CHARGED_ATTACK_COOLDOWN_MAX = 200;

    private static final int MIN_COOLDOWN = 20;
    private static final int MAX_COOLDOWN = 60;
    private static final int DODGE_COOLDOWN = 80;
    private static final double DODGE_SPEED = 0.5;

    // 调试开关
    private static final boolean DEBUG = false;  // 生产环境设为 false

    public HeiseiswordGoal(LivingEntity entity) {
        this.entity = entity;
        this.random = new Random(entity.getRandom().nextLong());
    }

    @Override
    public boolean canUse() {
        if (entity.isDeadOrDying()) {
            return false;
        }
        ItemStack mainHandItem = entity.getMainHandItem();
        return !mainHandItem.isEmpty() && mainHandItem.getItem() instanceof Heiseisword;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() && !entity.isDeadOrDying();
    }

    @Override
    public void start() {
        cooldown = 0;
        riderSwitchCooldown = 0;
        modeSwitchCooldown = 0;
        chargedAttackCooldown = 0;
        dodgeCooldown = 0;

        ItemStack mainHandItem = entity.getMainHandItem();
        if (mainHandItem.getItem() instanceof Heiseisword) {
            List<String> riders = HeiseiRiderEffectManager.getRiderOrder();
            if (!riders.isEmpty()) {
                String newRider = riders.get(random.nextInt(riders.size()));
                Heiseisword.setSelectedRiderStatic(mainHandItem, newRider);

                if (DEBUG) {
                    LOGGER.debug("[{}] 初始化骑士: {}", entity.getName().getString(), newRider);
                }
            }

            int modeRoll = random.nextInt(100);
            if (modeRoll < 5) {
                Heiseisword.setUltimateModeStatic(mainHandItem, true);
                Heiseisword.setFinishTimeModeStatic(mainHandItem, true);
                List<String> allRiders = new ArrayList<>(HeiseiRiderEffectManager.getRiderOrder());
                Heiseisword.setScrambleRidersStatic(mainHandItem, allRiders);
                if (DEBUG) {
                    LOGGER.debug("[{}] 初始化模式: 超必杀", entity.getName().getString());
                }
            } else if (modeRoll < 25) {
                Heiseisword.setFinishTimeModeStatic(mainHandItem, true);
                Heiseisword.setUltimateModeStatic(mainHandItem, false);
                if (DEBUG) {
                    LOGGER.debug("[{}] 初始化模式: 必杀", entity.getName().getString());
                }
            } else {
                Heiseisword.setFinishTimeModeStatic(mainHandItem, false);
                Heiseisword.setUltimateModeStatic(mainHandItem, false);
                if (DEBUG) {
                    LOGGER.debug("[{}] 初始化模式: 普通", entity.getName().getString());
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        ItemStack mainHandItem = entity.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof Heiseisword)) {
            return;
        }

        Level level = entity.level();
        LivingEntity target = null;

        if (entity instanceof Mob mob) {
            target = mob.getTarget();
        }

        // 更新冷却
        if (cooldown > 0) cooldown--;
        if (riderSwitchCooldown > 0) riderSwitchCooldown--;
        if (modeSwitchCooldown > 0) modeSwitchCooldown--;
        if (chargedAttackCooldown > 0) chargedAttackCooldown--;
        if (dodgeCooldown > 0) dodgeCooldown--;

        // 闪避
        if (dodgeCooldown <= 0 && target != null && shouldDodge(target)) {
            performDodge(target);
            dodgeCooldown = DODGE_COOLDOWN;
        }

        // 周期性切换骑士（即使没有目标也切换）
        if (riderSwitchCooldown <= 0) {
            switchToRandomRider(mainHandItem, level);
            riderSwitchCooldown = random.nextInt(RIDER_SWITCH_INTERVAL_MAX - RIDER_SWITCH_INTERVAL_MIN + 1)
                    + RIDER_SWITCH_INTERVAL_MIN;
        }

        // 周期性切换操作模式
        if (modeSwitchCooldown <= 0) {
            switchRandomMode(mainHandItem, level);
            modeSwitchCooldown = random.nextInt(MODE_SWITCH_INTERVAL_MAX - MODE_SWITCH_INTERVAL_MIN + 1)
                    + MODE_SWITCH_INTERVAL_MIN;
        }

        // 攻击
        if (cooldown <= 0 && target != null && target.isAlive()) {
            boolean useChargedAttack = chargedAttackCooldown <= 0 && random.nextFloat() < 0.3;

            if (useChargedAttack) {
                performChargedAttack(mainHandItem, level, target);
                chargedAttackCooldown = random.nextInt(CHARGED_ATTACK_COOLDOWN_MAX - CHARGED_ATTACK_COOLDOWN_MIN + 1)
                        + CHARGED_ATTACK_COOLDOWN_MIN;
            } else {
                performAction(mainHandItem, level, target);
            }

            cooldown = random.nextInt(MAX_COOLDOWN - MIN_COOLDOWN + 1) + MIN_COOLDOWN;
        }

        // 面向目标
        if (target != null && entity instanceof Mob mob) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    private boolean shouldDodge(LivingEntity target) {
        return target.distanceTo(entity) < 3.0 && random.nextFloat() < 0.3;
    }

    private void performDodge(LivingEntity target) {
        Vec3 dodgeDirection = entity.position().subtract(target.position()).normalize();
        entity.setDeltaMovement(dodgeDirection.scale(DODGE_SPEED));
    }

    private void switchToRandomRider(ItemStack stack, Level level) {
        List<String> riders = HeiseiRiderEffectManager.getRiderOrder();
        if (riders.size() <= 1) return;

        String currentRider = Heiseisword.getSelectedRiderStatic(stack);
        String newRider;

        int attempts = 0;
        do {
            newRider = riders.get(random.nextInt(riders.size()));
            attempts++;
            if (attempts > 10) break;
        } while (newRider.equals(currentRider) && riders.size() > 1);

        if (!newRider.equals(currentRider)) {
            Heiseisword.setSelectedRiderStatic(stack, newRider);

            if (!level.isClientSide && random.nextFloat() < 0.3f) {
                HeiseiRiderEffectManager.playSelectionSound(level, entity, newRider);
            }

            if (DEBUG) {
                LOGGER.debug("[{}] 切换骑士: {} -> {}", entity.getName().getString(), currentRider, newRider);
            }
        }
    }

    private void switchRandomMode(ItemStack stack, Level level) {
        if (level.isClientSide) return;

        int modeChoice = random.nextInt(100);
        String currentMode = getCurrentModeString(stack);

        if (modeChoice < 10) {
            Heiseisword.setFinishTimeModeStatic(stack, true);
            Heiseisword.setUltimateModeStatic(stack, true);
            List<String> allRiders = new ArrayList<>(HeiseiRiderEffectManager.getRiderOrder());
            Heiseisword.setScrambleRidersStatic(stack, allRiders);

            if (!currentMode.equals("超必杀")) {
                HeiseiRiderEffectManager.playUltimateActivationSound(level, entity);
                if (DEBUG) {
                    LOGGER.debug("[{}] 模式切换: {} -> 超必杀", entity.getName().getString(), currentMode);
                }
            }

        } else if (modeChoice < 35) {
            Heiseisword.setFinishTimeModeStatic(stack, true);
            Heiseisword.setUltimateModeStatic(stack, false);
            Heiseisword.setScrambleRidersStatic(stack, new ArrayList<>());

            if (!currentMode.equals("必杀")) {
                HeiseiRiderEffectManager.playFinishTimeSound(level, entity);
                if (DEBUG) {
                    LOGGER.debug("[{}] 模式切换: {} -> 必杀", entity.getName().getString(), currentMode);
                }
            }

        } else {
            Heiseisword.setFinishTimeModeStatic(stack, false);
            Heiseisword.setUltimateModeStatic(stack, false);
            Heiseisword.setScrambleRidersStatic(stack, new ArrayList<>());

            if (DEBUG && !currentMode.equals("普通")) {
                LOGGER.debug("[{}] 模式切换: {} -> 普通", entity.getName().getString(), currentMode);
            }
        }
    }

    private String getCurrentModeString(ItemStack stack) {
        if (Heiseisword.isUltimateModeStatic(stack)) return "超必杀";
        if (Heiseisword.isFinishTimeModeStatic(stack)) return "必杀";
        return "普通";
    }

    private void performChargedAttack(ItemStack stack, Level level, LivingEntity target) {
        if (level.isClientSide) return;

        Vec3 direction = target.position().subtract(entity.position()).normalize();

        boolean isFinishTime = Heiseisword.isFinishTimeModeStatic(stack);
        boolean isUltimate = Heiseisword.isUltimateModeStatic(stack);

        String currentRider = Heiseisword.getSelectedRiderStatic(stack);

        if (DEBUG) {
            LOGGER.debug("[{}] 蓄力攻击 - 骑士:{}, 必杀:{}, 超必杀:{}",
                    entity.getName().getString(), currentRider, isFinishTime, isUltimate);
        }

        if (isUltimate) {
            List<String> allRiders = HeiseiRiderEffectManager.getRiderOrder();
            for (String rider : allRiders) {
                HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
                if (effect != null) {
                    effect.executeSpecialAttack(level, entity, direction);
                }
            }
            HeiseiRiderEffectManager.playUltimateTimeBreakSound(level, entity, allRiders);

        } else if (isFinishTime) {
            List<String> selectedRiders = Heiseisword.getScrambleRidersStatic(stack);
            if (selectedRiders.isEmpty()) {
                String currentRider2 = Heiseisword.getSelectedRiderStatic(stack);
                if (currentRider2 != null && !currentRider2.isEmpty()) {
                    selectedRiders.add(currentRider2);
                    Heiseisword.setScrambleRidersStatic(stack, selectedRiders);
                }
            }

            for (String rider : selectedRiders) {
                HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
                if (effect != null) {
                    effect.executeSpecialAttack(level, entity, direction);
                }
            }
            HeiseiRiderEffectManager.playScrambleTimeBreakSound(level, entity, selectedRiders);
            Heiseisword.setFinishTimeModeStatic(stack, false);
            Heiseisword.setScrambleRidersStatic(stack, new ArrayList<>());

        } else {
            String rider = Heiseisword.getSelectedRiderStatic(stack);
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                effect.executeSpecialAttack(level, entity, direction);
                HeiseiRiderEffectManager.playAttackSound(level, entity, rider);
            }
        }

        Heiseisword.setLastAttackTimeStatic(stack, level.getGameTime());
    }

    private void performAction(ItemStack stack, Level level, LivingEntity target) {
        if (entity.isDeadOrDying()) {
            return;
        }

        Vec3 direction = target.position().subtract(entity.position()).normalize();

        if (Heiseisword.isUltimateModeStatic(stack)) {
            executeUltimateAttack(stack, level, direction);
        } else if (Heiseisword.isFinishTimeModeStatic(stack)) {
            executeFinishTimeAttack(stack, level, direction);
        } else {
            executeNormalAttack(stack, level, direction);
        }
    }

    private void executeNormalAttack(ItemStack stack, Level level, Vec3 direction) {
        String rider = Heiseisword.getSelectedRiderStatic(stack);

        if (!level.isClientSide) {
            HeiseiRiderEffectManager.playAttackSound(level, entity, rider);
        }

        HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);

        if (effect != null) {
            effect.executeSpecialAttack(level, entity, direction);
            Heiseisword.setLastAttackTimeStatic(stack, level.getGameTime());
        }
    }

    private void executeFinishTimeAttack(ItemStack stack, Level level, Vec3 direction) {
        List<String> selectedRiders = Heiseisword.getScrambleRidersStatic(stack);

        if (selectedRiders.isEmpty()) {
            List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();
            if (!riderOrder.isEmpty()) {
                String firstRider = riderOrder.get(0);
                Heiseisword.setSelectedRiderStatic(stack, firstRider);
                selectedRiders.add(firstRider);
            }
        }

        if (!selectedRiders.isEmpty()) {
            String currentRider = selectedRiders.get(0);

            if (!level.isClientSide) {
                HeiseiRiderEffectManager.playAttackSound(level, entity, currentRider);
            }

            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(currentRider);

            if (effect != null) {
                effect.executeSpecialAttack(level, entity, direction);
                Heiseisword.setLastAttackTimeStatic(stack, level.getGameTime());

                selectedRiders.remove(0);
                Heiseisword.setScrambleRidersStatic(stack, selectedRiders);

                if (selectedRiders.isEmpty()) {
                    Heiseisword.setFinishTimeModeStatic(stack, false);
                }
            }
        }
    }

    private void executeUltimateAttack(ItemStack stack, Level level, Vec3 direction) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();

        for (String rider : riderOrder) {
            if (!level.isClientSide) {
                HeiseiRiderEffectManager.playAttackSound(level, entity, rider);
            }

            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                effect.executeSpecialAttack(level, entity, direction);
            }
        }
        Heiseisword.setUltimateModeStatic(stack, false);
        Heiseisword.setFinishTimeModeStatic(stack, false);
    }
}