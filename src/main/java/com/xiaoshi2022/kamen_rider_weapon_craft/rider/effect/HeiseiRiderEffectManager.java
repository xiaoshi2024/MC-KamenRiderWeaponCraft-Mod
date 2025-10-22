package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.sound.RiderSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

import java.util.*;

public class HeiseiRiderEffectManager {
    private static final Map<String, HeiseiRiderEffect> RIDER_EFFECTS = new HashMap<>();
    private static final Map<String, SoundEvent> RIDER_NAME_SOUNDS = new HashMap<>();
    private static final List<String> RIDER_ORDER = new ArrayList<>();

    static {
        // 只注册名称音效，移除激活音效
        // 注意：由于实际的骑士效果实现类可能不存在，这里使用空实现或模拟
        // 在实际使用中需要替换为真正的实现
        registerRider("Build", null, null);
        registerRider("Ex-Aid", null, null);
        registerRider("Ghost", null, null);
        registerRider("Drive", null, null);
        registerRider("Gaim", null, null);
        registerRider("Wizard", null, null);
        registerRider("Fourze", null, null);
        registerRider("OOO", null, null);
        registerRider("W", null, null);
        registerRider("Decade", null, null);
        registerRider("Kiva", null, null);
        registerRider("Den-O", null, null);
        registerRider("Kabuto", null, null);
        registerRider("Hibiki", null, null);
        registerRider("Blade", null, null);
        registerRider("Faiz", null, null);
        registerRider("Ryuki", null, null);
        registerRider("Agito", null, null);
        registerRider("Kuuga", null, null);
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
    public static void playSelectionSound(World world, PlayerEntity player, String riderName) {
        SoundEvent nameSound = getRiderNameSound(riderName);
        if (nameSound != null) {
            // 避免空指针异常，仅在音效不为null时播放
            RiderSounds.playSelectionSound(world, player, nameSound);
        }
    }

    // 播放攻击音效："△△! Dual Time Break!"
    public static void playAttackSound(World world, PlayerEntity player, String riderName) {
        SoundEvent nameSound = getRiderNameSound(riderName);
        if (nameSound != null) {
            // 避免空指针异常，仅在音效不为null时播放
            RiderSounds.playAttackSound(world, player, nameSound);
        }
    }

    // 简化Scramble Time Break音效 - 使用优化的批量播放方法
    public static void playScrambleTimeBreakSound(World world, PlayerEntity player, List<String> selectedRiders) {
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
        RiderSounds.playDelayedSoundSequence(world, player, sounds);
    }

    // 播放超必杀的骑士名称部分音效
    public static void playUltimateTimeBreakNameSounds(World world, PlayerEntity player, List<String> selectedRiders) {
        if (selectedRiders.isEmpty()) return;

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
        RiderSounds.playDelayedSoundSequence(world, player, sounds);
    }

    // 简化Ultimate Time Break音效 - 使用优化的批量播放方法
    public static void playUltimateTimeBreakSound(World world, PlayerEntity player, List<String> selectedRiders) {
        // 现在这个方法只播放超必杀的骑士名称部分，最后报名音效会在击败实体后触发
        playUltimateTimeBreakNameSounds(world, player, selectedRiders);
    }

    // 播放完整的超必杀音效序列（在击败实体后触发）
    public static void playUltimateFinishSoundSequence(World world, PlayerEntity player, List<String> selectedRiders) {
        if (selectedRiders.isEmpty()) return;

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
        // 注意：需要确保RiderSounds.ULTIMATE_TIME_BREAK存在
        sounds.add(new RiderSounds.DelayedSound(RiderSounds.ULTIMATE_TIME_BREAK, delay + 20));

        // 批量播放所有音效，显著减少资源消耗
        RiderSounds.playDelayedSoundSequence(world, player, sounds);
    }

    // 新方法：在击败实体后播放特殊音效
    public static void playKillSound(World world, PlayerEntity player, SoundEvent specialSound) {
        if (specialSound != null) {
            RiderSounds.playKillSound(world, player, specialSound);
        }
    }

    public static void playRiderTimeSound(World world, PlayerEntity player) {
        // 确保RiderSounds.RIDE_HEI_SABER存在
        SoundEvent sound = RiderSounds.RIDE_HEI_SABER;
        if (sound != null) {
            RiderSounds.playSound(world, player, sound);
        }
    }

    public static void playFinishTimeSound(World world, PlayerEntity player) {
        // 确保RiderSounds.FINISH_TIME存在
        SoundEvent sound = RiderSounds.FINISH_TIME;
        if (sound != null) {
            RiderSounds.playSound(world, player, sound);
        }
    }

    public static void playUltimateActivationSound(World world, PlayerEntity player) {
        // 确保RiderSounds.HEY_SAY_RAPID存在
        SoundEvent sound = RiderSounds.HEY_SAY_RAPID;
        if (sound != null) {
            RiderSounds.playSound(world, player, sound);
        }
    }

    // 播放Scramble完成音效序列
    public static void playScrambleFinishSoundSequence(World world, PlayerEntity player, List<String> selectedRiders) {
        if (selectedRiders.isEmpty()) return;

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

        // 添加Scramble Time Break音效
        // 确保RiderSounds.SCRAMBLE_TIME_BREAK存在
        sounds.add(new RiderSounds.DelayedSound(RiderSounds.SCRAMBLE_TIME_BREAK, delay + 20));

        // 批量播放所有音效，显著减少资源消耗
        RiderSounds.playDelayedSoundSequence(world, player, sounds);
    }
}