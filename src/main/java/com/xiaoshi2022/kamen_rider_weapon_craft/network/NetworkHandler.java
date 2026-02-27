package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * NetworkHandler 负责处理所有的网络消息，包括配方数据的同步。
 */
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("kamen_rider_weapon_craft", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int packetId = 0;

        INSTANCE.registerMessage(
                packetId++,
                ServerSound.class,
                ServerSound::encode,
                ServerSound::decode,
                ServerSound::handle
        );

        INSTANCE.registerMessage(
                packetId++,
                SyncRecipeDataPacket.class,
                SyncRecipeDataPacket::encode,
                SyncRecipeDataPacket::decode,
                SyncRecipeDataPacket::handle
        );

        INSTANCE.registerMessage(
                packetId++,
                StartCraftingPacket.class,
                StartCraftingPacket::encode,
                StartCraftingPacket::decode,
                StartCraftingPacket::handle
        );


        INSTANCE.registerMessage(
                packetId++,
                SyncAnimationStatePacket.class,
                SyncAnimationStatePacket::encode,
                SyncAnimationStatePacket::decode,
                SyncAnimationStatePacket::handle
        );

        INSTANCE.registerMessage(
                packetId++,
                SyncGuiOpenStatePacket.class,
                SyncGuiOpenStatePacket::encode,
                SyncGuiOpenStatePacket::decode,
                SyncGuiOpenStatePacket::handle
        );
        INSTANCE.registerMessage(
                packetId++,
                DrawMusouSaberPacket.class,
                DrawMusouSaberPacket::encode,
                DrawMusouSaberPacket::decode,
                DrawMusouSaberPacket::handle
        );
        
        INSTANCE.registerMessage(
                packetId++,
                HeiseiswordRiderSelectionPacket.class,
                HeiseiswordRiderSelectionPacket::encode,
                HeiseiswordRiderSelectionPacket::decode,
                HeiseiswordRiderSelectionPacket::handle
        );
        
        INSTANCE.registerMessage(
                packetId++,
                HeiseiswordEnergySyncPacket.class,
                HeiseiswordEnergySyncPacket::encode,
                HeiseiswordEnergySyncPacket::decode,
                HeiseiswordEnergySyncPacket::handle
        );
        
        // 注册SoundStopPacket
        INSTANCE.registerMessage(
                packetId++,
                SoundStopPacket.class,
                SoundStopPacket::encode,
                SoundStopPacket::decode,
                SoundStopPacket::handle
        );

        // 注册KaizokuHassyarModeSwitchPacket
        INSTANCE.registerMessage(
                packetId++,
                KaizokuHassyarModeSwitchPacket.class,
                KaizokuHassyarModeSwitchPacket::encode,
                KaizokuHassyarModeSwitchPacket::decode,
                KaizokuHassyarModeSwitchPacket::handle
        );
        
        // 注册KaizokuHassyarSoundPacket
        INSTANCE.registerMessage(
                packetId++,
                KaizokuHassyarSoundPacket.class,
                KaizokuHassyarSoundPacket::encode,
                KaizokuHassyarSoundPacket::decode,
                KaizokuHassyarSoundPacket::handle
        );
    }
    
    // 发送到所有跟踪实体的客户端，包括实体本身
    public static <MSG> void sendToAllTracking(MSG message, Entity entity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    // 发送到服务器
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    // 发送到特定玩家
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    
    // 获取实例（兼容原有的PacketHandler调用方式）
    public static SimpleChannel getINSTANCE() {
        return INSTANCE;
    }
}