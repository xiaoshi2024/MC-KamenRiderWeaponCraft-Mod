// rider/sound/RiderSounds.java
package com.xiaoshi2022.kamen_rider_weapon_craft.rider.sound;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.*;

public class RiderSounds {
    private static final Map<String, SoundEvent> SOUND_REGISTRY = new HashMap<>();

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
        Identifier id = Identifier.of("kamen_rider_weapon_craft", name);
        SoundEvent soundEvent = SoundEvent.of(id);
        SOUND_REGISTRY.put(name, soundEvent);
        return soundEvent;
    }

    // 注册所有音效到游戏
    public static void registerSounds() {
        for (Map.Entry<String, SoundEvent> entry : SOUND_REGISTRY.entrySet()) {
            Identifier id = Identifier.of("kamen_rider_weapon_craft", entry.getKey());
            // 检查是否已经注册，避免重复注册导致的IllegalStateException
            if (!Registries.SOUND_EVENT.containsId(id)) {
                Registry.register(Registries.SOUND_EVENT, id, entry.getValue());
            }
        }
    }

    public static void playSound(World world, PlayerEntity player, SoundEvent sound) {
        if (world.isClient()) {
            world.playSound(player, player.getX(), player.getY(), player.getZ(),
                    sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
        } else {
            // 在服务器端也播放音效
            ((ServerWorld) world).playSound(null, player.getX(), player.getY(), player.getZ(),
                    sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    // 优化版延迟播放音效 - 使用Fabric的调度系统
    public static void playDelayedSound(World world, PlayerEntity player, SoundEvent sound, int delayTicks) {
        if (world.isClient()) {
            // 客户端延迟播放 - 直接使用Timer
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    playSound(world, player, sound);
                }
            }, delayTicks * 50L);
        } else {
            // 服务器端延迟播放 - 使用服务器调度器
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.getServer().execute(() -> {
                Timer timer = new Timer(true);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        playSound(world, player, sound);
                    }
                }, delayTicks * 50L);
            });
        }
    }

    // 新增方法：批量播放延迟音效序列 - 优化多音效播放性能
    public static void playDelayedSoundSequence(World world, PlayerEntity player, List<DelayedSound> sounds) {
        if (world.isClient()) {
            // 客户端使用单个定时器处理所有音效
            Timer timer = new Timer(true);
            for (DelayedSound delayedSound : sounds) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        playSound(world, player, delayedSound.soundEvent);
                    }
                }, delayedSound.delayTicks * 50L);
            }
        } else {
            // 服务器端使用服务器调度器
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.getServer().execute(() -> {
                Timer timer = new Timer(true);
                for (DelayedSound delayedSound : sounds) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            playSound(world, player, delayedSound.soundEvent);
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
    public static void playSelectionSound(World world, PlayerEntity player, SoundEvent riderNameSound) {
        playSound(world, player, HEY);
        playDelayedSound(world, player, riderNameSound, 20);
    }

    // 播放攻击音效：骑士名称 + "Dual Time Break!"
    public static void playAttackSound(World world, PlayerEntity player, SoundEvent riderNameSound) {
        playSound(world, player, riderNameSound);
    }

    // 新方法：在击败实体后播放特殊音效
    public static void playKillSound(World world, PlayerEntity player, SoundEvent specialSound) {
        playSound(world, player, specialSound);
    }

    // 播放骑士时间音效
    public static void playRiderTimeSound(World world, PlayerEntity player) {
        playSound(world, player, RIDE_HEI_SABER);
    }

    // 播放必杀时刻音效
    public static void playFinishTimeSound(World world, PlayerEntity player) {
        playSound(world, player, FINISH_TIME);
    }

    // 播放超必杀激活音效
    public static void playUltimateActivationSound(World world, PlayerEntity player) {
        // 播放嘿嘿待机音或其他超必杀启动音效
        playSound(world, player, HEY_SAY_RAPID);
    }

    // 播放选择音效（通过骑士名称字符串）
    public static void playSelectionSound(World world, PlayerEntity player, String riderName) {
        SoundEvent nameSound = getRiderNameSound(riderName);
        if (nameSound != null) {
            playSelectionSound(world, player, nameSound);
        }
    }

    // 根据骑士名称获取对应的音效
    public static SoundEvent getRiderNameSound(String riderName) {
        return switch (riderName) {
            case "Build" -> NAME_BUILD;
            case "Ex-Aid" -> NAME_EXAID;
            case "Ghost" -> NAME_GHOST;
            case "Drive" -> NAME_DRIVE;
            case "Gaim" -> NAME_GAIM;
            case "Wizard" -> NAME_WIZARD;
            case "Fourze" -> NAME_FOURZE;
            case "OOO" -> NAME_OOO;
            case "W" -> NAME_W;
            case "Decade" -> NAME_DECADE;
            case "Kiva" -> NAME_KIVA;
            case "Den-O" -> NAME_DEN_O;
            case "Kabuto" -> NAME_KABUTO;
            case "Hibiki" -> NAME_HIBIKI;
            case "Blade" -> NAME_BLADE;
            case "Faiz" -> NAME_FAIZ;
            case "Ryuki" -> NAME_RYUKI;
            case "Agito" -> NAME_AGITO;
            case "Kuuga" -> NAME_KUUGA;
            default -> null;
        };
    }

    // 播放超必杀完成音效序列
    public static void playUltimateFinishSoundSequence(World world, PlayerEntity player, List<String> riders) {
        List<DelayedSound> sounds = new ArrayList<>();

        // 添加Hey音效（无延迟）
        sounds.add(new DelayedSound(HEY, 0));

        // 然后按顺序添加所有选中骑士的名称音效
        int delay = 20;
        for (String riderName : riders) {
            SoundEvent nameSound = getRiderNameSound(riderName);
            if (nameSound != null) {
                sounds.add(new DelayedSound(nameSound, delay));
                delay += 10;
            }
        }

        // 添加Ultimate Time Break音效
        sounds.add(new DelayedSound(ULTIMATE_TIME_BREAK, delay + 20));

        // 批量播放所有音效
        playDelayedSoundSequence(world, player, sounds);
    }

    // 播放Scramble完成音效序列
    public static void playScrambleFinishSoundSequence(World world, PlayerEntity player, List<String> riders) {
        List<DelayedSound> sounds = new ArrayList<>();

        // 添加Hey音效（无延迟）
        sounds.add(new DelayedSound(HEY, 0));

        // 然后按顺序添加所有选中骑士的名称音效
        int delay = 20;
        for (String riderName : riders) {
            SoundEvent nameSound = getRiderNameSound(riderName);
            if (nameSound != null) {
                sounds.add(new DelayedSound(nameSound, delay));
                delay += 10;
            }
        }

        // 添加Scramble Time Break音效
        sounds.add(new DelayedSound(SCRAMBLE_TIME_BREAK, delay + 20));

        // 批量播放所有音效
        playDelayedSoundSequence(world, player, sounds);
    }
}