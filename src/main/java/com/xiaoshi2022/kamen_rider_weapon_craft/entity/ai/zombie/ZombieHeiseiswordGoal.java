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
import net.minecraft.world.phys.AABB;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class ZombieHeiseiswordGoal extends Goal {
    private final Zombie zombie;
    private final Random random = new Random();
    private int cooldown = 0;
    private int riderSelectionCooldown = 0;
    private int modeSwitchCooldown = 0;
    private int dodgeCooldown = 0;
    private static final int MIN_COOLDOWN = 5; // 最小冷却时间（0.25秒）- 大幅降低以提高敏捷度
    private static final int MAX_COOLDOWN = 30; // 最大冷却时间（1.5秒）- 大幅降低以提高敏捷度
    private static final int RIDER_SELECTION_INTERVAL = 10; // 骑士选择间隔（0.5秒）- 大幅降低以提高敏捷度
    private static final int MODE_SWITCH_INTERVAL = 80; // 模式切换间隔（4秒）
    private static final int DODGE_COOLDOWN = 20; // 闪避冷却时间（1秒）
    private static final double DODGE_SUCCESS_RATE = 0.8; // 80%的闪避成功率
    private static final double AVOIDANCE_DISTANCE = 4.0; // 僵尸躲避距离
    private static final int AVOIDANCE_EFFECT_DURATION = 60; // 躲避加速效果持续时间
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
    public boolean canContinueToUse() {
        // 与canUse()条件相同，确保AI持续运行
        return canUse();
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
        if (dodgeCooldown > 0) dodgeCooldown--;
        
        // 检查是否需要闪避目标的攻击
        if (dodgeCooldown <= 0 && shouldDodge()) {
            performDodge();
            dodgeCooldown = DODGE_COOLDOWN;
        }

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

    // 初始化武器，根据是否有固定骑士设置选择相应的骑士
    private void initializeWeapon() {
        ItemStack mainHandItem = zombie.getMainHandItem();
        if (mainHandItem.getItem() instanceof Heiseisword) {
            // 检查是否设置了固定骑士
            boolean isFixedRider = mainHandItem.hasTag() && mainHandItem.getTag().getBoolean("fixedRider");
            String fixedRiderName = isFixedRider ? mainHandItem.getTag().getString("fixedRiderName") : null;
            
            if (isFixedRider && fixedRiderName != null && !fixedRiderName.isEmpty()) {
                // 固定骑士模式：使用指定的骑士
                Heiseisword.setSelectedRiderStatic(mainHandItem, fixedRiderName);
            } else {
                // 普通模式：选择第一个骑士（Build）
                List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();
                if (!riderOrder.isEmpty()) {
                    Heiseisword.setSelectedRiderStatic(mainHandItem, riderOrder.get(0));
                }
            }
            
            // 设置旋转位置
            mainHandItem.getOrCreateTag().putInt("currentRotationPosition", 0);
        }
    }

    // 随机选择一个骑士或保持固定骑士
    private void selectRandomRider(Heiseisword heiseisword, ItemStack stack, Level level) {
        // 检查是否设置了固定骑士
        boolean isFixedRider = stack.hasTag() && stack.getTag().getBoolean("fixedRider");
        String fixedRiderName = isFixedRider ? stack.getTag().getString("fixedRiderName") : null;
        
        if (isFixedRider && fixedRiderName != null && !fixedRiderName.isEmpty()) {
            // 固定骑士模式：确保使用指定的骑士
            String currentRider = Heiseisword.getSelectedRiderStatic(stack);
            if (currentRider == null || !currentRider.equals(fixedRiderName)) {
                Heiseisword.setSelectedRiderStatic(stack, fixedRiderName);
                
                // 更新旋转位置
                int rotationPosition = random.nextInt(4);
                stack.getOrCreateTag().putInt("currentRotationPosition", rotationPosition);
                
                // 播放选择音效
                if (!level.isClientSide) {
                    HeiseiRiderEffectManager.playSelectionSound(level, zombie, fixedRiderName);
                }
            }
        } else {
            // 普通模式：随机选择骑士（80%概率）
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
    }

    // 随机切换模式
    private void maybeSwitchMode(Heiseisword heiseisword, ItemStack stack, Level level) {
        // 检查是否设置了固定骑士
        boolean isFixedRider = stack.hasTag() && stack.getTag().getBoolean("fixedRider");
        
        // 固定骑士模式下不切换到必杀模式（因为会清除当前骑士选择）
        if (isFixedRider) {
            // 如果当前处于必杀模式，退出它
            if (Heiseisword.isFinishTimeModeStatic(stack)) {
                Heiseisword.setFinishTimeModeStatic(stack, false);
                Heiseisword.setScrambleRidersStatic(stack, new java.util.ArrayList<>());
                
                // 重新设置固定骑士
                String fixedRiderName = stack.getTag().getString("fixedRiderName");
                Heiseisword.setSelectedRiderStatic(stack, fixedRiderName);
                
                Heiseisword.setUltimateModeStatic(stack, false);
                stack.getOrCreateTag().putInt("currentRotationPosition", 0);
                stack.getOrCreateTag().remove("isXKeyUltimateReady");
            }
            return; // 固定骑士模式下不进行其他模式切换
        }
        
        // 普通模式：20%概率切换模式
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
        // 70%概率执行普通攻击，30%概率执行远程攻击
        if (random.nextDouble() < 0.7) {
            // 近战攻击
            if (zombie.distanceTo(target) < 2.5) {
                performNormalAttack(heiseisword, stack, level, target, direction);
            }
        } else {
            // 远程攻击
            performRangedAttack(heiseisword, stack, level, target, direction);
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

    // 号召附近的僵尸规避即将释放的特效
    private void callZombiesToAvoidEffects(Level level, Vec3 direction) {
        if (level.isClientSide()) return;
        
        // 查找更大范围内的其他僵尸
        double leaderRange = 20.0; // 扩大僵尸首领影响范围
        Vec3 pos = zombie.position();
        AABB searchArea = new AABB(pos.x(), pos.y(), pos.z(), pos.x(), pos.y(), pos.z()).inflate(leaderRange);
        List<Zombie> nearbyZombies = level.getEntitiesOfClass(Zombie.class, searchArea);
        
        for (Zombie nearbyZombie : nearbyZombies) {
            // 跳过自己和已经是僵尸首领的僵尸
            if (nearbyZombie == zombie || nearbyZombie.getPersistentData().contains("IsZombieLeader")) {
                continue;
            }
            
            // 计算规避方向 - 与攻击方向相反，并根据僵尸位置做个性化调整
            Vec3 avoidanceDirection = calculateSmartAvoidanceDirection(nearbyZombie, direction);
            
            // 让僵尸朝规避方向移动以避开特效
            if (nearbyZombie.getNavigation() != null) {
                // 根据距离动态调整躲避距离
                double distance = nearbyZombie.distanceTo(zombie);
                double adjustedDistance = Math.min(AVOIDANCE_DISTANCE, distance * 0.5);
                
                double targetX = nearbyZombie.getX() + avoidanceDirection.x * adjustedDistance;
                double targetY = nearbyZombie.getY();
                double targetZ = nearbyZombie.getZ() + avoidanceDirection.z * adjustedDistance;
                
                // 确保目标坐标在有效范围内
                int blockX = (int)Math.floor(targetX);
                int blockY = (int)Math.floor(targetY);
                int blockZ = (int)Math.floor(targetZ);
                if (level.getBlockState(new net.minecraft.core.BlockPos(blockX, blockY, blockZ)).isValidSpawn(level, null, null)) {
                    // 设置更高的移动速度，让僵尸更快地规避
                    nearbyZombie.getNavigation().moveTo(targetX, targetY, targetZ, 1.5);
                    
                    // 添加更强的加速效果，持续更长时间
                    nearbyZombie.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, AVOIDANCE_EFFECT_DURATION, 2, 
                        false, false, true));
                    
                    // 添加防御提升效果，减少可能受到的伤害
                    nearbyZombie.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, AVOIDANCE_EFFECT_DURATION, 1, 
                        false, false, true));
                }
            }
        }
    }
    
    // 计算智能规避方向，考虑僵尸的当前位置和攻击方向
    private Vec3 calculateSmartAvoidanceDirection(Zombie nearbyZombie, Vec3 attackDirection) {
        // 基础规避方向：与攻击方向相反
        Vec3 basicAvoidance = new Vec3(-attackDirection.x, 0, -attackDirection.z).normalize();
        
        // 计算从僵尸首领到附近僵尸的向量
        Vec3 toNearbyZombie = nearbyZombie.position().subtract(zombie.position()).normalize();
        
        // 结合两个向量，创建一个避开攻击方向且与附近僵尸位置相关的规避路径
        // 这使得僵尸会选择最佳的躲避路线，而不仅仅是朝攻击相反方向移动
        Vec3 smartAvoidance = basicAvoidance.add(toNearbyZombie.scale(0.5)).normalize();
        
        return smartAvoidance;
    }
    
    // 检查是否应该闪避
    private boolean shouldDodge() {
        LivingEntity target = zombie.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        
        // 如果目标正在攻击且距离较近，概率性闪避
        double distance = zombie.distanceTo(target);
        if (distance < 4.0) {
            // 目标越近，闪避概率越高
            double dodgeChance = DODGE_SUCCESS_RATE * (1.0 - (distance / 4.0));
            return random.nextDouble() < dodgeChance;
        }
        
        return false;
    }
    
    // 执行闪避动作
    private void performDodge() {
        LivingEntity target = zombie.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        
        // 计算闪避方向 - 与目标方向垂直或稍微偏移
        Vec3 toTarget = target.position().subtract(zombie.position()).normalize();
        
        // 随机选择向左或向右闪避
        boolean dodgeLeft = random.nextBoolean();
        
        // 计算垂直于目标方向的闪避向量
        Vec3 dodgeDirection;
        if (dodgeLeft) {
            // 向左闪避 (逆时针90度)
            dodgeDirection = new Vec3(-toTarget.z, 0, toTarget.x).normalize();
        } else {
            // 向右闪避 (顺时针90度)
            dodgeDirection = new Vec3(toTarget.z, 0, -toTarget.x).normalize();
        }
        
        // 添加一些随机性
        dodgeDirection = dodgeDirection.add(new Vec3(
            (random.nextDouble() - 0.5) * 0.4,
            0,
            (random.nextDouble() - 0.5) * 0.4
        )).normalize();
        
        // 执行闪避移动
        if (zombie.getNavigation() != null) {
            // 计算闪避目标位置
            double targetX = zombie.getX() + dodgeDirection.x * 2.5;
            double targetY = zombie.getY();
            double targetZ = zombie.getZ() + dodgeDirection.z * 2.5;
            
            // 设置高速度进行闪避
            zombie.getNavigation().moveTo(targetX, targetY, targetZ, 2.0);
            
            // 为闪避添加短暂的冲刺效果
            zombie.setDeltaMovement(dodgeDirection.scale(0.6));
            
            // 添加临时无敌效果（短暂的伤害减免）
            zombie.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 10, 3,
                false, false, true));
        }
    }

    // 执行普通攻击动作
    private void performNormalAttack(Heiseisword heiseisword, ItemStack stack, Level level, LivingEntity target, Vec3 direction) {
        String selectedRider = Heiseisword.getSelectedRiderStatic(stack);
        
        if (selectedRider != null && !selectedRider.isEmpty()) {
            // 号召附近僵尸规避
            callZombiesToAvoidEffects(level, direction);
            
            // 执行骑士特效
            HeiseiRiderEffectManager.getRiderEffect(selectedRider).executeSpecialAttack(level, zombie, direction);
            
            // 播放攻击音效
            HeiseiRiderEffectManager.playAttackSound(level, zombie, selectedRider);
            
            // 更新攻击时间
            stack.getOrCreateTag().putLong("lastAttackTime", level.getGameTime());
        }
    }

    // 执行远程攻击动作
    private void performRangedAttack(Heiseisword heiseisword, ItemStack stack, Level level, LivingEntity target, Vec3 direction) {
        String selectedRider = Heiseisword.getSelectedRiderStatic(stack);
        
        if (selectedRider != null && !selectedRider.isEmpty()) {
            // 号召附近僵尸规避
            callZombiesToAvoidEffects(level, direction);
            
            float chargeTime = 0.5F + random.nextFloat() * 0.5F; // 半满到满蓄力
            HeiseiRiderEffectManager.getRiderEffect(selectedRider).executeSpecialAttack(level, zombie, direction.scale(chargeTime * 2.0));
            
            // 播放攻击音效
            HeiseiRiderEffectManager.playAttackSound(level, zombie, selectedRider);
            
            // 更新攻击时间
            stack.getOrCreateTag().putLong("lastAttackTime", level.getGameTime());
        }
    }

    // 执行Scramble攻击
    private void executeScrambleAttack(Heiseisword heiseisword, ItemStack stack, Level level, LivingEntity target, Vec3 direction, List<String> riders) {
        if (!riders.isEmpty()) {
            // 号召附近僵尸规避大范围特效
            callZombiesToAvoidEffects(level, direction);
            
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
            // 号召附近僵尸规避超必杀特效
            callZombiesToAvoidEffects(level, direction);
            
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