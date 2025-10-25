// rider/effect/HeiseiRiderEffectManager.java
package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl.*;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.sound.RiderSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public class HeiseiRiderEffectManager {
    private static final Map<String, HeiseiRiderEffect> RIDER_EFFECTS = new HashMap<>();
    private static final Map<String, SoundEvent> RIDER_NAME_SOUNDS = new HashMap<>();
    private static final List<String> RIDER_ORDER = new ArrayList<>();

    static {
        // 只注册名称音效，移除激活音效
        registerRider("Build", new BuildEffect(), RiderSounds.NAME_BUILD);
        registerRider("Ex-Aid", new ExAidEffect(), RiderSounds.NAME_EXAID);
        registerRider("Ghost", new GhostEffect(), RiderSounds.NAME_GHOST);
        registerRider("Drive", new DriveEffect(), RiderSounds.NAME_DRIVE);
        registerRider("Gaim", new GaimEffect(), RiderSounds.NAME_GAIM);
        registerRider("Wizard", new WizardEffect(), RiderSounds.NAME_WIZARD);
        registerRider("Fourze", new FourzeEffect(), RiderSounds.NAME_FOURZE);
        registerRider("OOO", new OOOEffect(), RiderSounds.NAME_OOO);
        registerRider("W", new WEffect(), RiderSounds.NAME_W);
        registerRider("Decade", new DecadeEffect(), RiderSounds.NAME_DECADE);
        registerRider("Kiva", new KivaEffect(), RiderSounds.NAME_KIVA);
        registerRider("Den-O", new DenOEffect(), RiderSounds.NAME_DEN_O);
        registerRider("Kabuto", new KabutoEffect(), RiderSounds.NAME_KABUTO);
        registerRider("Hibiki", new HibikiEffect(), RiderSounds.NAME_HIBIKI);
        registerRider("Blade", new BladeEffect(), RiderSounds.NAME_BLADE);
        registerRider("Faiz", new FaizEffect(), RiderSounds.NAME_FAIZ);
        registerRider("Ryuki", new RyukiEffect(), RiderSounds.NAME_RYUKI);
        registerRider("Agito", new AgitoEffect(), RiderSounds.NAME_AGITO);
        registerRider("Kuuga", new KuugaEffect(), RiderSounds.NAME_KUUGA);
    }

    private static void registerRider(String name, HeiseiRiderEffect effect, SoundEvent nameSound) {
        RIDER_EFFECTS.put(name, effect);
        RIDER_NAME_SOUNDS.put(name, nameSound);
        RIDER_ORDER.add(name);
    }

    // 获取骑士特效
    public static HeiseiRiderEffect getRiderEffect(String name) {
        return RIDER_EFFECTS.get(name);
    }
    
    // 安全获取骑士能量消耗，确保即使没有实现也能正常工作
    public static double getRiderEnergyCost(String name) {
        HeiseiRiderEffect effect = getRiderEffect(name);
        if (effect != null) {
            try {
                return effect.getEnergyCost();
            } catch (AbstractMethodError e) {
                // 如果骑士效果没有实现getEnergyCost方法，返回默认值20
                return 20.0;
            }
        }
        return 20.0; // 默认能量消耗值
    }

    // 获取骑士名称音效
    public static SoundEvent getRiderNameSound(String name) {
        return RIDER_NAME_SOUNDS.get(name);
    }

    // 获取骑士顺序列表
    public static List<String> getRiderOrder() {
        return Collections.unmodifiableList(RIDER_ORDER);
    }

    // 播放选择音效："Hey! △△!"
    public static void playSelectionSound(Level level, LivingEntity shooter, String riderName) {
        SoundEvent nameSound = getRiderNameSound(riderName);
        if (nameSound != null) {
            // 支持玩家和非玩家实体
            if (shooter instanceof Player player) {
                RiderSounds.playSelectionSound(level, player, nameSound);
            } else {
                // 为非玩家实体播放音效，使用HOSTILE音源
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                    nameSound, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
    }

    // 播放攻击音效："△△! Dual Time Break!"
    public static void playAttackSound(Level level, LivingEntity shooter, String riderName) {
        SoundEvent nameSound = getRiderNameSound(riderName);
        if (nameSound != null) {
            // 支持玩家和非玩家实体
            if (shooter instanceof Player player) {
                RiderSounds.playAttackSound(level, player, nameSound);
            } else {
                // 为非玩家实体播放音效，使用HOSTILE音源
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                    nameSound, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
    }

    // 简化Scramble Time Break音效 - 使用优化的批量播放方法
    public static void playScrambleTimeBreakSound(Level level, LivingEntity shooter, List<String> selectedRiders) {
        if (selectedRiders.isEmpty()) return;

        // 使用优化的批量播放方法，减少线程创建
        List<RiderSounds.DelayedSound> sounds = new ArrayList<>();
        
        // 播放所有选中骑士的名称音效作为"OO"部分
        int delay = 0;
        for (String rider : selectedRiders) {
            SoundEvent nameSound = getRiderNameSound(rider);
            if (nameSound != null) {
                sounds.add(new RiderSounds.DelayedSound(nameSound, delay));
                delay += 10;
            }
        }
        
        // 批量播放所有音效，显著减少资源消耗
        if (shooter instanceof Player player) {
            RiderSounds.playDelayedSoundSequence(level, player, sounds);
        } else {
            // 简化版：直接播放第一个音效，为非玩家实体使用HOSTILE音源
            if (!sounds.isEmpty()) {
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                    sounds.get(0).soundEvent, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
        
        // Scramble Time Break音效现在只在击败实体后才会触发
        // 实际的Scramble Time Break音效会在实体被击败时通过新的方法触发
    }

    // 播放超必杀的骑士名称部分音效
    public static void playUltimateTimeBreakNameSounds(Level level, LivingEntity shooter, List<String> selectedRiders) {
        // 不需要再播放Hey Say音效，因为已经在playUltimateActivationSound中播放过了

        // 使用优化的批量播放方法，减少线程创建
        List<RiderSounds.DelayedSound> sounds = new ArrayList<>();
        
        int delay = 40;

        // 播放所有选中骑士的名称音效作为"XX"部分
        for (String rider : selectedRiders) {
            SoundEvent nameSound = getRiderNameSound(rider);
            if (nameSound != null) {
                sounds.add(new RiderSounds.DelayedSound(nameSound, delay));
                delay += 8;
            }
        }
        
        // 批量播放所有音效，显著减少资源消耗
        if (shooter instanceof Player player) {
            RiderSounds.playDelayedSoundSequence(level, player, sounds);
        } else {
            // 简化版：直接播放第一个音效，为非玩家实体使用HOSTILE音源
            if (!sounds.isEmpty()) {
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                    sounds.get(0).soundEvent, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
    }
    
    // 简化Ultimate Time Break音效 - 使用优化的批量播放方法
    public static void playUltimateTimeBreakSound(Level level, LivingEntity shooter, List<String> selectedRiders) {
        // 现在这个方法只播放超必杀的骑士名称部分，最后报名音效会在击败实体后触发
        playUltimateTimeBreakNameSounds(level, shooter, selectedRiders);
    }
    
    // 播放完整的超必杀音效序列（在击败实体后触发）
    public static void playUltimateFinishSoundSequence(Level level, LivingEntity shooter, List<String> selectedRiders) {
        // 使用优化的批量播放方法，减少线程创建
        List<RiderSounds.DelayedSound> sounds = new ArrayList<>();
        
        int delay = 0;

        // 播放所有选中骑士的名称音效
        for (String rider : selectedRiders) {
            SoundEvent nameSound = getRiderNameSound(rider);
            if (nameSound != null) {
                sounds.add(new RiderSounds.DelayedSound(nameSound, delay));
                delay += 8;
            }
        }
        
        // 添加Ultimate Time Break最后报名音效
        sounds.add(new RiderSounds.DelayedSound(RiderSounds.ULTIMATE_TIME_BREAK, delay + 20));
        
        // 批量播放所有音效，显著减少资源消耗
        if (shooter instanceof Player player) {
            RiderSounds.playDelayedSoundSequence(level, player, sounds);
        } else {
            // 简化版：直接播放第一个音效和终极音效，使用HOSTILE音源
            if (!sounds.isEmpty()) {
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                    sounds.get(0).soundEvent, SoundSource.HOSTILE, 1.0F, 1.0F);
                // 直接播放终极音效
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                    RiderSounds.ULTIMATE_TIME_BREAK, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
    }
    
    // 新方法：在击败实体后播放特殊音效
    public static void playKillSound(Level level, LivingEntity shooter, SoundEvent specialSound) {
        if (shooter instanceof Player player) {
            RiderSounds.playKillSound(level, player, specialSound);
        } else {
            // 为非玩家实体播放音效，使用HOSTILE音源
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                specialSound, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    public static void playRiderTimeSound(Level level, LivingEntity shooter) {
        if (shooter instanceof Player player) {
            RiderSounds.playSound(level, player, RiderSounds.RIDE_HEI_SABER);
        } else {
            // 为非玩家实体播放音效，使用HOSTILE音源
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                RiderSounds.RIDE_HEI_SABER, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    public static void playFinishTimeSound(Level level, LivingEntity shooter) {
        if (shooter instanceof Player player) {
            RiderSounds.playSound(level, player, RiderSounds.FINISH_TIME);
        } else {
            // 为非玩家实体播放音效，使用HOSTILE音源
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                RiderSounds.FINISH_TIME, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }


    public static void playUltimateActivationSound(Level level, LivingEntity shooter) {
        if (shooter instanceof Player player) {
            RiderSounds.playSound(level, player, RiderSounds.HEY_SAY_RAPID);
        } else {
            // 为非玩家实体播放音效，使用HOSTILE音源
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                RiderSounds.HEY_SAY_RAPID, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }
}