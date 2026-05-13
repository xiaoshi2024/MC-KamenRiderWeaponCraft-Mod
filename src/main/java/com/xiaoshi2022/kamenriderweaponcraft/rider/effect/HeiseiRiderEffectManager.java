package com.xiaoshi2022.kamenriderweaponcraft.rider.effect;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl.*;
import com.xiaoshi2022.kamenriderweaponcraft.rider.sound.RiderSounds;
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

    public static HeiseiRiderEffect getRiderEffect(String name) {
        return RIDER_EFFECTS.get(name);
    }

    public static double getRiderEnergyCost(String name) {
        HeiseiRiderEffect effect = getRiderEffect(name);
        if (effect != null) {
            try {
                return effect.getEnergyCost();
            } catch (AbstractMethodError e) {
                return 20.0;
            }
        }
        return 20.0;
    }

    public static SoundEvent getRiderNameSound(String name) {
        return RIDER_NAME_SOUNDS.get(name);
    }

    public static List<String> getRiderOrder() {
        return Collections.unmodifiableList(RIDER_ORDER);
    }

    public static void playSelectionSound(Level level, LivingEntity shooter, String riderName) {
        SoundEvent nameSound = getRiderNameSound(riderName);
        if (nameSound != null) {
            if (shooter instanceof Player player) {
                // 玩家：播放完整的选人音效（Hey! + 骑士名称）
                RiderSounds.playSelectionSound(level, player, nameSound);
            } else {
                // 非玩家实体：直接播放骑士名称音效，不需要延迟和阻塞
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                        nameSound, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
    }

    public static void playAttackSound(Level level, LivingEntity shooter, String riderName) {
        SoundEvent nameSound = getRiderNameSound(riderName);
        if (nameSound != null) {
            if (shooter instanceof Player player) {
                RiderSounds.playAttackSound(level, player, nameSound);
            } else {
                // 非玩家实体：直接播放攻击音效
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                        nameSound, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
    }

    // 简化 playScrambleTimeBreakSound
    public static void playScrambleTimeBreakSound(Level level, LivingEntity shooter, List<String> selectedRiders) {
        if (selectedRiders.isEmpty()) return;

        if (shooter instanceof Player player) {
            List<RiderSounds.DelayedSound> sounds = new ArrayList<>();
            int delay = 0;
            for (String rider : selectedRiders) {
                SoundEvent nameSound = getRiderNameSound(rider);
                if (nameSound != null) {
                    sounds.add(new RiderSounds.DelayedSound(nameSound, delay));
                    delay += 10;
                }
            }
            RiderSounds.playDelayedSoundSequence(level, player, sounds);
        } else {
            // 非玩家实体：只播放第一个骑士的音效
            if (!selectedRiders.isEmpty()) {
                SoundEvent firstSound = getRiderNameSound(selectedRiders.get(0));
                if (firstSound != null) {
                    level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                            firstSound, SoundSource.HOSTILE, 1.0F, 1.0F);
                }
            }
        }
    }

    // 简化 playUltimateTimeBreakNameSounds
    public static void playUltimateTimeBreakNameSounds(Level level, LivingEntity shooter, List<String> selectedRiders) {
        if (selectedRiders.isEmpty()) return;

        if (shooter instanceof Player player) {
            List<RiderSounds.DelayedSound> sounds = new ArrayList<>();
            int delay = 40;
            for (String rider : selectedRiders) {
                SoundEvent nameSound = getRiderNameSound(rider);
                if (nameSound != null) {
                    sounds.add(new RiderSounds.DelayedSound(nameSound, delay));
                    delay += 8;
                }
            }
            RiderSounds.playDelayedSoundSequence(level, player, sounds);
        } else {
            // 非玩家实体：直接播放音效，不需要延迟序列
            if (!selectedRiders.isEmpty()) {
                SoundEvent firstSound = getRiderNameSound(selectedRiders.get(0));
                if (firstSound != null) {
                    level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                            firstSound, SoundSource.HOSTILE, 1.0F, 1.0F);
                }
            }
        }
    }

    public static void playUltimateTimeBreakSound(Level level, LivingEntity shooter, List<String> selectedRiders) {
        playUltimateTimeBreakNameSounds(level, shooter, selectedRiders);
    }

    public static void playUltimateFinishSoundSequence(Level level, LivingEntity shooter, List<String> selectedRiders) {
        List<RiderSounds.DelayedSound> sounds = new ArrayList<>();
        int delay = 0;

        for (String rider : selectedRiders) {
            SoundEvent nameSound = getRiderNameSound(rider);
            if (nameSound != null) {
                sounds.add(new RiderSounds.DelayedSound(nameSound, delay));
                delay += 8;
            }
        }

        sounds.add(new RiderSounds.DelayedSound(RiderSounds.ULTIMATE_TIME_BREAK, delay + 20));

        if (shooter instanceof Player player) {
            RiderSounds.playDelayedSoundSequence(level, player, sounds);
        } else {
            if (!sounds.isEmpty()) {
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    sounds.get(0).soundEvent, SoundSource.HOSTILE, 1.0F, 1.0F);
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    RiderSounds.ULTIMATE_TIME_BREAK, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
    }

    public static void playKillSound(Level level, LivingEntity shooter, SoundEvent specialSound) {
        if (shooter instanceof Player player) {
            RiderSounds.playKillSound(level, player, specialSound);
        } else {
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                specialSound, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    public static void playRiderTimeSound(Level level, LivingEntity shooter) {
        if (shooter instanceof Player player) {
            RiderSounds.playSound(level, player, RiderSounds.RIDE_HEI_SABER);
        } else {
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                RiderSounds.RIDE_HEI_SABER, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    public static void playFinishTimeSound(Level level, LivingEntity shooter) {
        if (shooter instanceof Player player) {
            RiderSounds.playSound(level, player, RiderSounds.FINISH_TIME);
        } else {
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                RiderSounds.FINISH_TIME, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    public static void playUltimateActivationSound(Level level, LivingEntity shooter) {
        if (shooter instanceof Player player) {
            RiderSounds.playSound(level, player, RiderSounds.HEY_SAY_RAPID);
        } else {
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                RiderSounds.HEY_SAY_RAPID, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }
}