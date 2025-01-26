package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.CloseMapPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.custom.AonicxParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding;
import com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK.weapon_map;
import net.minecraft.client.Minecraft;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.arrowx.LaserBeamEntityRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.daidaimaru.ThrownDaidaimaruRenderer;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding.CHANGE_KEY;
import static com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding.OPEN_LOCKSEED;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.helheim_crack.helheim_crackBlockRenderer;


public class ClientEvents {
    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID,value = Dist.CLIENT)
    public static class ClientForgeEvents {
        static final int INTERVAL = 12 * 20; // Minecraft中1秒等于20个tick

        private static final int SOUND_INTERVAL = 20; // 1秒 = 20 ticks
        private static long lastPlayedTime = 0;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                long currentTime = level.getGameTime();
                if (currentTime - lastPlayedTime >= SOUND_INTERVAL) {
                    lastPlayedTime = currentTime;

                    Player player = Minecraft.getInstance().player;
                    if (player != null && player.isUsingItem() && player.getUseItem().getItem() instanceof sonicarrow) {
                        // 在客户端播放音效
                        player.playSound(ModSounds.PULL_STANDBY.get(), 1.0F, 1.0F);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (KeyBinding.CHANGE_KEY.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                ItemStack stack = null;
                if (player != null) {
                    stack = player.getMainHandItem();
                    if (stack.getItem() instanceof weapon_map) {
                        kamen_rider_weapon_craft.PACKET_HANDLER.sendToServer(new CloseMapPacket());
                    }
                }
                if (stack.getItem() instanceof sonicarrow) {
                    // 获取玩家最后一次播放音效的时间
                    long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");

                    // 获取当前时间
                    long currentTime = mc.level.getGameTime();

                    // 检查是否已经过了间隔时间
                    if (currentTime - lastPlayed >= INTERVAL) {
                        // 服务器播放音效
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
    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID, value = Dist.CLIENT,bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents{
        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.AONICX_PARTICLE.get(), AonicxParticles::provider);
        }

       @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){
        event.register(CHANGE_KEY);
        event.register(OPEN_LOCKSEED);
        }

        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.HELHEIM_CRACK_BLOCK_ENTITY.get(), helheim_crackBlockRenderer::new);
        }
        @SubscribeEvent
        public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            EntityRenderers.register(ModEntityTypes.THROWN_DAIDAIMARU.get(), ThrownDaidaimaruRenderer::new);
            EntityRenderers.register(ModEntityTypes.LASER_BEAM.get(), LaserBeamEntityRenderer::new);
        }
    }
}

