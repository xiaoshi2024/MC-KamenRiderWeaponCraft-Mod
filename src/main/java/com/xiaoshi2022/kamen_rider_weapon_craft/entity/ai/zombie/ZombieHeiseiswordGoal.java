package com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.zombie;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class ZombieHeiseiswordGoal extends Goal {
    private final Zombie zombie;
    private final Random random = new Random();
    private int cooldown = 0;
    private int riderSelectionCooldown = 0;
    private int modeSwitchCooldown = 0;
    private static final int MIN_COOLDOWN = 20; // 最小冷却时间（1秒）
    private static final int MAX_COOLDOWN = 100; // 最大冷却时间（5秒）
    private static final int RIDER_SELECTION_INTERVAL = 40; // 骑士选择间隔（2秒）
    private static final int MODE_SWITCH_INTERVAL = 100; // 模式切换间隔（5秒）
    private static final double ENERGY_RECOVERY_AMOUNT = 5.0; // 僵尸使用武器时的能量恢复量

    public ZombieHeiseiswordGoal(Zombie zombie) {
        this.zombie = zombie;
        // 移除TARGET标志，让僵尸能正常使用默认的目标追踪行为
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 只有当僵尸手持Heiseisword且有目标时才激活此AI
        ItemStack mainHandItem = zombie.getMainHandItem();
        return mainHandItem.getItem() instanceof Heiseisword && 
               zombie.getTarget() != null && 
               zombie.isAlive();
    }

    @Override
    public void start() {
        super.start();
        // 初始化时随机选择一个骑士
        initializeWeapon();
    }

    @Override
    public void tick() {
        super.tick();
        
        ItemStack mainHandItem = zombie.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof Heiseisword heiseisword)) {
            return;
        }

        Level level = zombie.level();
        LivingEntity target = zombie.getTarget();
        
        if (target == null || !target.isAlive()) {
            return;
        }

        // 更新冷却时间
        if (cooldown > 0) cooldown--;
        if (riderSelectionCooldown > 0) riderSelectionCooldown--;
        if (modeSwitchCooldown > 0) modeSwitchCooldown--;

        // 随机选择骑士
        if (riderSelectionCooldown <= 0) {
            selectRandomRider(heiseisword, mainHandItem, level);
            riderSelectionCooldown = RIDER_SELECTION_INTERVAL;
        }

        // 随机切换模式
        if (modeSwitchCooldown <= 0) {
            maybeSwitchMode(heiseisword, mainHandItem, level);
            modeSwitchCooldown = MODE_SWITCH_INTERVAL;
        }

        // 随机执行攻击或技能
        if (cooldown <= 0) {
            performAction(heiseisword, mainHandItem, level, target);
            cooldown = random.nextInt(MAX_COOLDOWN - MIN_COOLDOWN + 1) + MIN_COOLDOWN;
        }

        // 让僵尸看向目标
        zombie.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    // 初始化武器，选择第一个骑士
    private void initializeWeapon() {
        ItemStack mainHandItem = zombie.getMainHandItem();
        if (mainHandItem.getItem() instanceof Heiseisword) {
            List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();
            if (!riderOrder.isEmpty()) {
                // 设置第一个骑士（Build）
                Heiseisword.setSelectedRiderStatic(mainHandItem, riderOrder.get(0));
                mainHandItem.getOrCreateTag().putInt("currentRotationPosition", 0);
            }
        }
    }

    // 随机选择一个骑士
    private void selectRandomRider(Heiseisword heiseisword, ItemStack stack, Level level) {
        // 80%概率选择骑士
        if (random.nextDouble() < 0.8) {
            List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();
            if (!riderOrder.isEmpty()) {
                String randomRider = riderOrder.get(random.nextInt(riderOrder.size()));
                Heiseisword.setSelectedRiderStatic(stack, randomRider);
                
                // 更新旋转位置
                int rotationPosition = random.nextInt(4);
                stack.getOrCreateTag().putInt("currentRotationPosition", rotationPosition);
                
                // 播放选择音效
                if (!level.isClientSide) {
                    HeiseiRiderEffectManager.playSelectionSound(level, zombie, randomRider);
                }
            }
        }
    }

    // 随机切换模式
    private void maybeSwitchMode(Heiseisword heiseisword, ItemStack stack, Level level) {
        // 20%概率切换模式
        if (random.nextDouble() < 0.2) {
            boolean currentMode = Heiseisword.isFinishTimeModeStatic(stack);
            boolean newMode = !currentMode;
            
            // 检查是否可以切换到必杀模式（不在冷却中）
            if (newMode) {
                long lastEnterTime = stack.getOrCreateTag().getLong("lastFinishTimeEnter");
                long currentTime = level.getGameTime();
                int finishTimeCooldown = 300; // 与Heiseisword中的FINISH_TIME_COOLDOWN_TICKS保持一致
                
                if ((currentTime - lastEnterTime) >= finishTimeCooldown) {
                    // 可以进入必杀模式
                    Heiseisword.setFinishTimeModeStatic(stack, true);
                    stack.getOrCreateTag().putLong("lastFinishTimeEnter", currentTime);
                    
                    if (!level.isClientSide) {
                        HeiseiRiderEffectManager.playFinishTimeSound(level, zombie);
                    }
                    
                    // 清空之前的选择
                    Heiseisword.setScrambleRiders(stack, new java.util.ArrayList<>());
                }
            } else {
                // 退出必杀模式
                Heiseisword.setFinishTimeModeStatic(stack, false);
                Heiseisword.setScrambleRidersStatic(stack, new java.util.ArrayList<>());
                Heiseisword.setSelectedRiderStatic(stack, null);
                Heiseisword.setUltimateModeStatic(stack, false);
                stack.getOrCreateTag().putInt("currentRotationPosition", 0);
                stack.getOrCreateTag().remove("isXKeyUltimateReady");
            }
        }
    }

    // 执行动作（攻击或技能）
    private void performAction(Heiseisword heiseisword, ItemStack stack, Level level, LivingEntity target) {
        // 计算目标方向
        Vec3 direction = target.position().subtract(zombie.position()).normalize();
        
        // 模拟能量恢复
        // 注意：由于僵尸没有玩家特有的能量系统，我们需要特殊处理
        // 这里我们可以简化处理，假设僵尸总是有足够的能量使用技能
        
        boolean isFinishTimeMode = Heiseisword.isFinishTimeModeStatic(stack);
        
        if (isFinishTimeMode) {
            // 必杀模式下的行为
            performFinishTimeAction(heiseisword, stack, level, target, direction);
        } else {
            // 普通模式下的行为
            performNormalAction(heiseisword, stack, level, target, direction);
        }
    }

    // 执行普通模式下的动作
    private void performNormalAction(Heiseisword heiseisword, ItemStack stack, Level level, LivingEntity target, Vec3 direction) {
        String selectedRider = Heiseisword.getSelectedRiderStatic(stack);
        
        if (selectedRider != null && !selectedRider.isEmpty()) {
            // 获取骑士特效对象
            HeiseiRiderEffect riderEffect = HeiseiRiderEffectManager.getRiderEffect(selectedRider);
            
            // 70%概率执行普通攻击，30%概率执行远程攻击
            if (random.nextDouble() < 0.7) {
                // 近战攻击
                if (zombie.distanceTo(target) < 2.5) {
                    // 直接调用骑士的特殊攻击效果
                    riderEffect.executeSpecialAttack(level, zombie, direction);
                    
                    // 播放攻击音效
                    HeiseiRiderEffectManager.playAttackSound(level, zombie, selectedRider);
                    
                    // 更新攻击时间
                    stack.getOrCreateTag().putLong("lastAttackTime", level.getGameTime());
                }
            } else {
                // 远程攻击
                float chargeTime = 0.5F + random.nextFloat() * 0.5F; // 半满到满蓄力
                riderEffect.executeSpecialAttack(level, zombie, direction.scale(chargeTime * 2.0));
                
                // 播放攻击音效
                HeiseiRiderEffectManager.playAttackSound(level, zombie, selectedRider);
                
                // 更新攻击时间
                stack.getOrCreateTag().putLong("lastAttackTime", level.getGameTime());
            }
        }
    }

    // 执行必杀模式下的动作
    private void performFinishTimeAction(Heiseisword heiseisword, ItemStack stack, Level level, LivingEntity target, Vec3 direction) {
        // 30%概率选择更多骑士
        if (random.nextDouble() < 0.3) {
            selectMoreRiders(heiseisword, stack, level);
        }
        
        // 20%概率进入超必杀模式
        if (random.nextDouble() < 0.2 && !Heiseisword.isUltimateModeStatic(stack)) {
            Heiseisword.setUltimateModeStatic(stack, true);
            if (!level.isClientSide) {
                HeiseiRiderEffectManager.playUltimateActivationSound(level, zombie);
            }
        }
        
        // 执行攻击
        if (random.nextDouble() < 0.8) {
            List<String> selectedRiders = Heiseisword.getScrambleRidersStatic(stack);
            
            if (!selectedRiders.isEmpty()) {
                boolean isUltimateMode = Heiseisword.isUltimateModeStatic(stack);
                
                if (isUltimateMode) {
                    // 执行超必杀
                    executeUltimateAttack(heiseisword, stack, level, target, direction, selectedRiders);
                } else {
                    // 执行普通Scramble攻击
                    executeScrambleAttack(heiseisword, stack, level, target, direction, selectedRiders);
                }
                
                // 更新攻击时间
                stack.getOrCreateTag().putLong("lastAttackTime", level.getGameTime());
            }
        }
    }

    // 选择更多骑士
    private void selectMoreRiders(Heiseisword heiseisword, ItemStack stack, Level level) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();
        List<String> currentScrambleRiders = Heiseisword.getScrambleRidersStatic(stack);
        
        // 随机选择1-3个未被选择的骑士
        int count = 1 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            // 找到未被选择的骑士
            java.util.List<String> availableRiders = new java.util.ArrayList<>(riderOrder);
            availableRiders.removeAll(currentScrambleRiders);
            
            if (!availableRiders.isEmpty()) {
                String randomRider = availableRiders.get(random.nextInt(availableRiders.size()));
                currentScrambleRiders.add(randomRider);
                
                // 播放选择音效
                if (!level.isClientSide) {
                    HeiseiRiderEffectManager.playSelectionSound(level, zombie, randomRider);
                }
            }
        }
        
        Heiseisword.setScrambleRidersStatic(stack, currentScrambleRiders);
    }

    // 执行Scramble攻击
    private void executeScrambleAttack(Heiseisword heiseisword, ItemStack stack, Level level, LivingEntity target, Vec3 direction, List<String> riders) {
        if (!riders.isEmpty()) {
            // 播放Scramble攻击音效
            HeiseiRiderEffectManager.playScrambleTimeBreakSound(level, zombie, riders);
            
            // 对每个选中的骑士执行特殊攻击
            for (String rider : riders) {
                HeiseiRiderEffectManager.getRiderEffect(rider).executeSpecialAttack(level, zombie, direction);
            }
        }
    }

    // 执行超必杀攻击
    private void executeUltimateAttack(Heiseisword heiseisword, ItemStack stack, Level level, LivingEntity target, Vec3 direction, List<String> riders) {
        if (!riders.isEmpty()) {
            // 播放超必杀音效
            HeiseiRiderEffectManager.playUltimateTimeBreakSound(level, zombie, riders);
            
            // 对每个选中的骑士执行特殊攻击
            for (String rider : riders) {
                HeiseiRiderEffectManager.getRiderEffect(rider).executeSpecialAttack(level, zombie, direction.scale(2.0));
            }
            
            // 添加范围效果
            if (!level.isClientSide) {
                // 范围爆炸
                level.explode(zombie, zombie.getX(), zombie.getY(), zombie.getZ(), 4.0f, Level.ExplosionInteraction.MOB);
                
                // 击退效果
                target.setDeltaMovement(direction.scale(2.0));
            }
        }
        
        // 重置超必杀模式
        Heiseisword.setUltimateModeStatic(stack, false);
    }
    
    // 移除了冗余的HeiseiswordAccess内部类，现在直接使用Heiseisword类中的静态方法
}