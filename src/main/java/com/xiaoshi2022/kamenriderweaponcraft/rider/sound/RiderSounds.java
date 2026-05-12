package com.xiaoshi2022.kamenriderweaponcraft.rider.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RiderSounds {
    public static final SoundEvent RIDE_HEI_SABER = register("ride_hei_saber");
    public static final SoundEvent FINISH_TIME = register("finish_time");
    public static final SoundEvent ULTIMATE_TIME_BREAK = register("ultimate_time_break");
    public static final SoundEvent SCRAMBLE_TIME_BREAK = register("scramble_time_break");
    public static final SoundEvent DUAL_TIME_BREAK = register("dual_time_break");
    public static final SoundEvent HEY = register("hey");
    public static final SoundEvent HEY_SAY_RAPID = register("hey_say_rapid");

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
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath("kamenriderweaponcraft", name));
    }

    public static void playSound(Level level, Player player, SoundEvent sound) {
        if (level.isClientSide()) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            ((ServerLevel) level).playSound(null, player.getX(), player.getY(), player.getZ(),
                    sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void playDelayedSound(Level level, Player player, SoundEvent sound, int delayTicks) {
        if (level.isClientSide()) {
            level.getServer().execute(() -> {
                try {
                    Thread.sleep(delayTicks * 50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Minecraft.getInstance().execute(() -> {
                    playSound(level, player, sound);
                });
            });
        } else {
            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.getServer().execute(() -> {
                try {
                    Thread.sleep(delayTicks * 50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playSound(level, player, sound);
            });
        }
    }

    public static void playDelayedSoundSequence(Level level, Player player, List<DelayedSound> sounds) {
        if (level.isClientSide()) {
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
            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.getServer().execute(() -> {
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

    public static class DelayedSound {
        public final SoundEvent soundEvent;
        public final int delayTicks;

        public DelayedSound(SoundEvent soundEvent, int delayTicks) {
            this.soundEvent = soundEvent;
            this.delayTicks = delayTicks;
        }
    }

    public static void playSelectionSound(Level level, Player player, SoundEvent riderNameSound) {
        playSound(level, player, HEY);
        playDelayedSound(level, player, riderNameSound, 20);
    }

    public static void playAttackSound(Level level, Player player, SoundEvent riderNameSound) {
        playSound(level, player, riderNameSound);
    }

    public static void playKillSound(Level level, Player player, SoundEvent specialSound) {
        playSound(level, player, specialSound);
    }
}