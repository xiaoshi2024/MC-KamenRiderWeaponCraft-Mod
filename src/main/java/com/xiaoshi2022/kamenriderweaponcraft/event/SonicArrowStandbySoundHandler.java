package com.xiaoshi2022.kamenriderweaponcraft.event;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.SonicArrowItem;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.rider.sound.RiderSounds;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 音速弓蓄力待机音（移植自 1.20.1 PullSounds + ServerSound 标志位方案，简化为服务端直接实现）
 * 蓄力第 4 秒开始每 1 秒播放一次 pull_standby，松手/中断自动停止（无需标志位与网络包）
 */
@EventBusSubscriber(modid = KamenRiderWeaponCraft.MODID)
public class SonicArrowStandbySoundHandler {
    /** 蓄力到音效首次响起的延迟（tick）：第 4 秒 = 80 tick */
    private static final int START_DELAY_TICKS = 80;
    /** 循环间隔：每 1 秒响一次 */
    private static final int LOOP_INTERVAL_TICKS = 20;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof SonicArrowItem)) {
                continue;
            }
            // 已蓄力 tick 数（用 remaining 换算，避免 getTicksUsingItem 的语义歧义）
            int elapsed = player.getUseItem().getUseDuration(player) - player.getUseItemRemainingTicks();
            if (elapsed < START_DELAY_TICKS) {
                continue;
            }
            // 第 4 秒起每秒触发一次（elapsed 每 tick +1，模 20 恰好命中一次）
            if ((elapsed - START_DELAY_TICKS) % LOOP_INTERVAL_TICKS == 0) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        RiderSounds.PULL_STANDBY, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}
