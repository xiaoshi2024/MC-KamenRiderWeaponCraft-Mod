// rider/sound/RiderSounds.java
package com.xiaoshi2022.kamen_rider_weapon_craft.rider.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import java.util.*;

public class RiderSounds {
    private static final Map<String, Lazy<SoundEvent>> SOUND_REGISTRY = new HashMap<>();

    // 固定音效
    public static final SoundEvent RIDE_HEI_SABER = register("ride_hei_saber");
    public static final SoundEvent FINISH_TIME = register("finish_time");
    public static final SoundEvent ULTIMATE_TIME_BREAK = register("ultimate_time_break");
    public static final SoundEvent SCRAMBLE_TIME_BREAK = register("scramble_time_break");
    public static final SoundEvent DUAL_TIME_BREAK = register("dual_time_break");
    public static final SoundEvent HEY = register("hey");
    public static final SoundEvent HEY_SAY_RAPID = register("hey_say_rapid"); // 快速Hey Say音效

    // 各个骑士的名称音效
    public static final SoundEvent NAME_BUILD = register("name_build");
    public static final SoundEvent NAME_EXAID = register("name_exaid");
    public static final SoundEvent NAME_GHOST = register("name_ghost");
    public static final SoundEvent NAME_DRIVE = register("name_drive");
    public static final SoundEvent NAME_GAIM = register("name_gaim");
    public static final SoundEvent NAME_WIZARD = register("name_wizard");
    public static final SoundEvent NAME_FOURZE = register("name_fourze");
    public static final SoundEvent NAME_OOO = register("name_ooo");
    public static final SoundEvent NAME_W = register("name_w");
    public static final SoundEvent NAME_DECADE = register("name_decade");
    public static final SoundEvent NAME_KIVA = register("name_kiva");
    public static final SoundEvent NAME_DEN_O = register("name_den_o");
    public static final SoundEvent NAME_KABUTO = register("name_kabuto");
    public static final SoundEvent NAME_HIBIKI = register("name_hibiki");
    public static final SoundEvent NAME_BLADE = register("name_blade");
    public static final SoundEvent NAME_FAIZ = register("name_faiz");
    public static final SoundEvent NAME_RYUKI = register("name_ryuki");
    public static final SoundEvent NAME_AGITO = register("name_agito");
    public static final SoundEvent NAME_KUUGA = register("name_kuuga");

    private static SoundEvent register(String name) {
        Lazy<SoundEvent> soundEvent = Lazy.of(() ->
                SoundEvent.createVariableRangeEvent(
                        new net.minecraft.resources.ResourceLocation(
                                "kamen_rider_weapon_craft",
                                name
                        )
                )
        );
        SOUND_REGISTRY.put(name, soundEvent);
        return soundEvent.get();
    }

    public static void playSound(Level level, Player player, SoundEvent sound) {
        if (level.isClientSide()) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            // 在服务器端也播放音效
            ((ServerLevel) level).playSound(null, player.getX(), player.getY(), player.getZ(),
                    sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    // 优化版延迟播放音效 - 使用单一的声音序列替代多个独立线程
    public static void playDelayedSound(Level level, Player player, SoundEvent sound, int delayTicks) {
        if (level.isClientSide()) {
            // 客户端延迟播放
            // 使用单个定时器处理多个音效，避免创建过多定时器
            Timer timer = new Timer(true); // 使用守护线程，避免阻止程序退出
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Minecraft.getInstance().execute(() -> {
                        playSound(level, player, sound);
                    });
                }
            }, delayTicks * 50L);
        } else {
            // 服务器端延迟播放 - 使用适合1.20.1版本的正确API
            ServerLevel serverLevel = (ServerLevel) level;
            int finalDelay = delayTicks;
            // 在主线程中执行任务
            serverLevel.getServer().execute(() -> {
                // 直接使用计时器进行延迟
                new Timer(true).schedule(new TimerTask() {
                    @Override
                    public void run() {
                        playSound(level, player, sound);
                    }
                }, finalDelay * 50L);
            });
        }
    }
    
    // 新增方法：批量播放延迟音效序列 - 优化多音效播放性能
    public static void playDelayedSoundSequence(Level level, Player player, List<DelayedSound> sounds) {
        if (level.isClientSide()) {
            // 客户端使用单个定时器处理所有音效
            Timer timer = new Timer(true);
            for (DelayedSound delayedSound : sounds) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Minecraft.getInstance().execute(() -> {
                            playSound(level, player, delayedSound.soundEvent);
                        });
                    }
                }, delayedSound.delayTicks * 50L);
            }
        } else {
            // 服务器端使用适合1.20.1版本的正确API
            ServerLevel serverLevel = (ServerLevel) level;
            // 在主线程中执行任务
            serverLevel.getServer().execute(() -> {
                // 创建单个定时器处理所有音效
                Timer timer = new Timer(true);
                for (DelayedSound delayedSound : sounds) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            playSound(level, player, delayedSound.soundEvent);
                        }
                    }, delayedSound.delayTicks * 50L);
                }
            });
        }
    }
    
    // 延迟音效数据类
    public static class DelayedSound {
        public final SoundEvent soundEvent;
        public final int delayTicks;
        
        public DelayedSound(SoundEvent soundEvent, int delayTicks) {
            this.soundEvent = soundEvent;
            this.delayTicks = delayTicks;
        }
    }

    // 播放选择音效："Hey!" + 骑士名称
    public static void playSelectionSound(Level level, Player player, SoundEvent riderNameSound) {
        playSound(level, player, HEY);
        playDelayedSound(level, player, riderNameSound, 20);
    }

    // 播放攻击音效：骑士名称 + "Dual Time Break!"
    public static void playAttackSound(Level level, Player player, SoundEvent riderNameSound) {
        playSound(level, player, riderNameSound);
        // Dual Time Break音效现在只在击败实体后才会触发
        // 这里我们保持原有的骑士名称音效播放，但不播放Dual Time Break音效
        // 实际的Dual Time Break音效会在实体被击败时通过新的方法触发
    }
    
    // 新方法：在击败实体后播放特殊音效
    public static void playKillSound(Level level, Player player, SoundEvent specialSound) {
        playSound(level, player, specialSound);
    }
}