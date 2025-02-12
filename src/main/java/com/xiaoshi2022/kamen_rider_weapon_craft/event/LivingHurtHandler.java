package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft")
public class LivingHurtHandler {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();

        if (source.getEntity() instanceof Player player) {
            long currentTime = player.level().getGameTime();
            long standbyStartTime = getStandbyStartTime(player);
            long standbyEndTime = getStandbyEndTime(player);

            if (currentTime >= standbyStartTime && currentTime <= standbyEndTime) {
                if (player.level() instanceof ServerLevel serverLevel) {
                    // 播放 PROGRISING_STRASH 音效
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.PROGRISING_STRASH.get(), SoundSource.PLAYERS, 1.8F, 1.0F);

                    // 增加6点暴击伤害
                    event.setAmount(event.getAmount() + 6.0F);

                    // 停止 PRO_STANDBY_TONE 待机音
                    stopStandbyTone(serverLevel, player);
                }
            }
        }
    }

    // 辅助方法：停止待机音
    private static void stopStandbyTone(ServerLevel serverLevel, Player player) {
        MinecraftServer server = serverLevel.getServer();
        if (server != null) {
            // 创建命令源
            CommandSourceStack commandSourceStack = player.createCommandSourceStack().withSuppressedOutput();

            // 执行 /stopsound 命令
            server.getCommands().performPrefixedCommand(commandSourceStack, "/stopsound @s player kamen_rider_weapon_craft:pro_standby_tone");
        }
    }

    // 辅助方法：获取和设置待机音的开始时间
    private static long getStandbyStartTime(Player player) {
        return player.getPersistentData().getLong("standbyStartTime");
    }

    private static void setStandbyStartTime(Player player, long time) {
        player.getPersistentData().putLong("standbyStartTime", time);
    }

    // 辅助方法：获取和设置待机音的结束时间
    private static long getStandbyEndTime(Player player) {
        return player.getPersistentData().getLong("standbyEndTime");
    }

    private static void setStandbyEndTime(Player player, long time) {
        player.getPersistentData().putLong("standbyEndTime", time);
    }
}