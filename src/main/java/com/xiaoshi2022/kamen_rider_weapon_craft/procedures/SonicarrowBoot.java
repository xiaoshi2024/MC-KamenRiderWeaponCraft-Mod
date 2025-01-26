package com.xiaoshi2022.kamen_rider_weapon_craft.procedures;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SonicarrowBoot {
    static final int INTERVAL = 12 * 20; // Minecraft中1秒等于20个tick

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBinding.CHANGE_KEY.isDown()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            // 获取玩家手中的物品栈
            ItemStack stack = player.getMainHandItem();

            // 检查是否是Sonicarrow
            if (stack.getItem() instanceof sonicarrow) {
                // 获取玩家最后一次播放音效的时间
                long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");

                // 获取当前时间
                long currentTime = Minecraft.getInstance().level.getGameTime();

                // 检查是否已经过了间隔时间
                if (currentTime - lastPlayed >= INTERVAL) {
                    // 播放音效
                    player.playSound(ModSounds.SONICARROW_BOOT_SOUND.get(), 1.0F, 1.0F);
                    // 更新玩家最后一次播放音效的时间
                    player.getPersistentData().putLong("lastPlayedSound", currentTime);
                } else {
                    // 如果未达到间隔时间，可以在这里添加一些提示信息
                    player.displayClientMessage(Component.literal("冷却时间未结束，还需等待 " + (INTERVAL - (currentTime - lastPlayed)) / 20 + " 秒"), true);
                }
            }
        }
    }
}