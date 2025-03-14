package com.xiaoshi2022.kamen_rider_weapon_craft.procedures;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.HinawaDaidai_DJ_Ju;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PullSoundsClient {
    private static final int SOUND_INTERVAL = 20; // 1秒 = 20 ticks
    private static long lastPlayedTime = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        long currentTime = level.getGameTime();
        if (currentTime - lastPlayedTime >= SOUND_INTERVAL) {
            lastPlayedTime = currentTime;

            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            ItemStack useItem = player.getUseItem();
            if (player.isUsingItem() && useItem != null) {
                if (useItem.getItem() instanceof sonicarrow && ModSounds.PULL_STANDBY.get() != null) {
                    level.playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.PULL_STANDBY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                if (useItem.getItem() instanceof HinawaDaidai_DJ_Ju && ModSounds.DJ_DISC.get() != null) {
                    level.playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.DJ_DISC.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }
}