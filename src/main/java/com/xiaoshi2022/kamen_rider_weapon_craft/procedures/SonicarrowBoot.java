package com.xiaoshi2022.kamen_rider_weapon_craft.procedures;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SonicarrowBoot {
    static final int INTERVAL = 12 * 20; // Minecraft中1秒等于20个tick

    public static void playSonicarrowBootSound(ServerPlayer player) {
        Level level = player.level();
        long currentTime = level.getGameTime();

        // 获取玩家最后一次播放音效的时间
        long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");

        // 检查是否已经过了间隔时间
        if (currentTime - lastPlayed >= INTERVAL) {
            // 播放音效
            player.playSound(ModSounds.SONICARROW_BOOT_SOUND.get(),  1.0F, 1.0F);
            // 在客户端播放音效
            if (player.level().isClientSide) {
                player.playSound(ModSounds.SONICARROW_BOOT_SOUND.get(), 1.0F, 1.0F);
            }
            // 更新玩家最后一次播放音效的时间
            player.getPersistentData().putLong("lastPlayedSound", currentTime);
        } else {
            // 如果未达到间隔时间，可以在这里添加一些提示信息
            player.displayClientMessage(Component.literal("冷却时间未结束，还需等待 " + (INTERVAL - (currentTime - lastPlayed)) / 20 + " 秒"), true);
        }
    }
}