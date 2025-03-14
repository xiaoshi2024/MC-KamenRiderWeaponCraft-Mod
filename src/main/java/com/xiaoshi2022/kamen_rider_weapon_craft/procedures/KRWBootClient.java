package com.xiaoshi2022.kamen_rider_weapon_craft.procedures;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.HinawaDaidai_DJ_Ju;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.progrise_hopper_blade;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class KRWBootClient {
    static final int INTERVAL = 12 * 20; // Minecraft中1秒等于20个tick

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBinding.CHANGE_KEY.isDown()) { // 检查是否按下了自定义按键
            // 获取玩家
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            // 获取玩家手中的物品栈
            ItemStack stack = player.getMainHandItem();

            // 获取玩家最后一次播放音效的时间
            long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");

            // 获取当前时间
            long currentTime = Minecraft.getInstance().level.getGameTime();

            // 检查是否已经过了间隔时间
            if (currentTime - lastPlayed >= INTERVAL) {
                // 根据物品类型播放不同的音效
                if (stack.getItem() instanceof sonicarrow) {
                    player.playSound(ModSounds.SONICARROW_BOOT_SOUND.get(), 1.0F, 1.0F);
                } else if (stack.getItem() instanceof progrise_hopper_blade) {
                    player.playSound(ModSounds.PROGRISE_HOPPER_BLADE_BOOT.get(), 1.0F, 1.0F);
                }else if (stack.getItem() instanceof HinawaDaidai_DJ_Ju) {
                    player.playSound(ModSounds.DJ_BOOT_TONE.get(), 1.0F, 1.0F);
                } else {
                    return;
                }

                // 更新玩家最后一次播放音效的时间
                player.getPersistentData().putLong("lastPlayedSound", currentTime);
            } else {
                // 如果未达到间隔时间，提示玩家
                player.displayClientMessage(Component.literal("冷却时间未结束，还需等待 " + (INTERVAL - (currentTime - lastPlayed)) / 20 + " 秒"), true);
            }
        }
    }
}