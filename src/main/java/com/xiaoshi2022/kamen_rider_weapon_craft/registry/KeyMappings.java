package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.progrise_hopper_blade;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.LockseedManager;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.ServerSound;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class KeyMappings {
    // Y 键定义
    public static final KeyMapping Y = new KeyMapping("key.cs.y", GLFW.GLFW_KEY_Y, "key.categories.ui") {
        private boolean isDownOld = false;

        @Override
        public void setDown(boolean isDown) {
            super.setDown(isDown);
            if (isDownOld != isDown && isDown) {
                kamen_rider_weapon_craft.PACKET_HANDLER.sendToServer(new LockseedManager(0, 0));
                LockseedManager.pressAction(Minecraft.getInstance().player, 0, 0);
            }
            isDownOld = isDown;
        }
    };

    // X 键定义
    public static final KeyMapping X = new KeyMapping("key.cs.x", GLFW.GLFW_KEY_X, "key.categories.ui") {
        private boolean isDownOld = false;

        @Override
        public void setDown(boolean isDown) {
            super.setDown(isDown);
            if (isDownOld != isDown && isDown) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    // 检查玩家左右手是否持有音速弓
                    if (player.getMainHandItem().getItem() instanceof sonicarrow ||
                        player.getOffhandItem().getItem() instanceof sonicarrow) {
                        // 发送网络包到服务端，播放 SONICARROW_BOOT_SOUND 音效
                        ServerSound.sendToServer(new ServerSound(ServerSound.SoundType.BOOT));
                    }
                    if (player.getMainHandItem().getItem() instanceof progrise_hopper_blade ||
                        player.getOffhandItem().getItem() instanceof progrise_hopper_blade) {
                        // 发送网络包到服务端，播放 PROGRISE_BOOT 音效
                        ServerSound.sendToServer(new ServerSound(ServerSound.SoundType.BOOT));
                    }
                }
            }
            isDownOld = isDown;
        }
    };

    // 注册按键
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(Y);
        event.register(X);
    }

    // 客户端 Tick 事件监听
    @Mod.EventBusSubscriber({Dist.CLIENT})
    public static class KeyEventListener {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (Minecraft.getInstance().screen == null) {
                Y.consumeClick();
                X.consumeClick();
            }
        }
    }
}