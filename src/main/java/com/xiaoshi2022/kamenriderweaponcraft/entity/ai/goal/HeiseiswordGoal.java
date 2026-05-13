package com.xiaoshi2022.kamenriderweaponcraft.entity.ai.goal;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.HeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.HeiseiRiderEffectManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Heiseisword AI目标 - 允许任何生物使用平成剑
 */
public class HeiseiswordGoal extends Goal {
    
    private final LivingEntity entity;
    private final Random random;
    
    private int cooldown = 0;
    private int riderSelectionCooldown = 0;
    private int modeSwitchCooldown = 0;
    private int dodgeCooldown = 0;
    
    private static final int MIN_COOLDOWN = 40;
    private static final int MAX_COOLDOWN = 100;
    private static final int RIDER_SELECTION_INTERVAL = 200;
    private static final int MODE_SWITCH_INTERVAL = 300;
    private static final int DODGE_COOLDOWN = 100;
    private static final double DODGE_DISTANCE = 2.0;
    
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
        return mainHandItem.getItem() instanceof Heiseisword;
    }
    
    @Override
    public boolean canContinueToUse() {
        return canUse();
    }
    
    @Override
    public void start() {
        cooldown = 0;
        riderSelectionCooldown = 0;
        modeSwitchCooldown = 0;
        dodgeCooldown = 0;
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

        if (cooldown > 0) cooldown--;
        if (riderSelectionCooldown > 0) riderSelectionCooldown--;
        if (modeSwitchCooldown > 0) modeSwitchCooldown--;
        if (dodgeCooldown > 0) dodgeCooldown--;

        if (dodgeCooldown <= 0 && target != null && shouldDodge(target)) {
            performDodge(target);
            dodgeCooldown = DODGE_COOLDOWN;
        }

        if (riderSelectionCooldown <= 0) {
            selectRandomRider(mainHandItem, level);
            riderSelectionCooldown = RIDER_SELECTION_INTERVAL;
        }

        if (modeSwitchCooldown <= 0) {
            maybeSwitchMode(mainHandItem, level);
            modeSwitchCooldown = MODE_SWITCH_INTERVAL;
        }

        if (cooldown <= 0 && target != null && target.isAlive()) {
            performAction(mainHandItem, level, target);
            cooldown = random.nextInt(MAX_COOLDOWN - MIN_COOLDOWN + 1) + MIN_COOLDOWN;
        }

        if (target != null && entity instanceof Mob mob) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }
    
    private void selectRandomRider(ItemStack stack, Level level) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();
        if (riderOrder.isEmpty()) {
            return;
        }
        
        CompoundTag tag = getTag(stack);
        boolean isFixedRider = tag.getBoolean("fixedRider");
        
        if (isFixedRider) {
            if (Heiseisword.isFinishTimeModeStatic(stack)) {
                Heiseisword.setFinishTimeModeStatic(stack, false);
                Heiseisword.setScrambleRidersStatic(stack, new ArrayList<>());
                String fixedRiderName = tag.getString("fixedRiderName");
                Heiseisword.setSelectedRiderStatic(stack, fixedRiderName);
                putInt(stack, "currentRotationPosition", 0);
                removeTag(stack, "isXKeyUltimateReady");
            }
        } else {
            boolean newMode = !Heiseisword.isFinishTimeModeStatic(stack);
            
            if (newMode) {
                long currentTime = level.getGameTime();
                long lastEnterTime = getLong(stack, "lastFinishTimeEnter");
                
                if (currentTime - lastEnterTime > 100) {
                    Heiseisword.setFinishTimeModeStatic(stack, true);
                    putLong(stack, "lastFinishTimeEnter", currentTime);
                }
            } else {
                Heiseisword.setFinishTimeModeStatic(stack, false);
                Heiseisword.setScrambleRidersStatic(stack, new ArrayList<>());
                String randomRider = riderOrder.get(random.nextInt(riderOrder.size()));
                Heiseisword.setSelectedRiderStatic(stack, randomRider);
                putInt(stack, "currentRotationPosition", 0);
                removeTag(stack, "isXKeyUltimateReady");
            }
        }
    }
    
    private void maybeSwitchMode(ItemStack stack, Level level) {
        boolean isFinishTimeMode = Heiseisword.isFinishTimeModeStatic(stack);
        
        if (!isFinishTimeMode && random.nextFloat() < 0.1) {
            Heiseisword.setUltimateModeStatic(stack, true);
            HeiseiRiderEffectManager.playUltimateActivationSound(level, entity);
        }
    }
    
    private void performAction(ItemStack stack, Level level, LivingEntity target) {
        if (level.isClientSide) {
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
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(currentRider);
            
            if (effect != null) {
                effect.executeSpecialAttack(level, entity, direction);
                Heiseisword.setLastAttackTimeStatic(stack, level.getGameTime());
                
                selectedRiders.remove(0);
                Heiseisword.setScrambleRidersStatic(stack, selectedRiders);
                
                if (selectedRiders.isEmpty()) {
                    Heiseisword.setFinishTimeModeStatic(stack, false);
                    putInt(stack, "currentRotationPosition", 0);
                }
            }
        }
    }
    
    private void executeUltimateAttack(ItemStack stack, Level level, Vec3 direction) {
        List<String> riderOrder = HeiseiRiderEffectManager.getRiderOrder();
        
        for (String rider : riderOrder) {
            HeiseiRiderEffect effect = HeiseiRiderEffectManager.getRiderEffect(rider);
            if (effect != null) {
                effect.executeSpecialAttack(level, entity, direction);
            }
        }
        
        if (!level.isClientSide) {
            level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 4.0f, Level.ExplosionInteraction.MOB);
        }
        
        Heiseisword.setUltimateModeStatic(stack, false);
    }
    
    private boolean shouldDodge(LivingEntity target) {
        if (target.distanceTo(entity) < 3.0) {
            return random.nextFloat() < 0.3;
        }
        return false;
    }
    
    private void performDodge(LivingEntity target) {
        Vec3 awayDirection = entity.position().subtract(target.position()).normalize();
        Vec3 newPos = entity.position().add(awayDirection.scale(DODGE_DISTANCE));
        
        entity.setPos(newPos.x, newPos.y, newPos.z);
        
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }
    }
    
    // Helper methods for NBT operations using DataComponents
    private CompoundTag getTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }
    
    private void putLong(ItemStack stack, String key, long value) {
        CompoundTag tag = getTag(stack);
        tag.putLong(key, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    private long getLong(ItemStack stack, String key) {
        return getTag(stack).getLong(key);
    }
    
    private void putInt(ItemStack stack, String key, int value) {
        CompoundTag tag = getTag(stack);
        tag.putInt(key, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    private void removeTag(ItemStack stack, String key) {
        CompoundTag tag = getTag(stack);
        tag.remove(key);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}