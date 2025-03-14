package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

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

        // 注册 ServerSound
        INSTANCE.registerMessage(
                packetId++,
                ServerSound.class,
                ServerSound::encode,
                ServerSound::decode,
                ServerSound::handle
        );

        // 注册 SyncRecipeDataPacket
        INSTANCE.registerMessage(
                packetId++,
                SyncRecipeDataPacket.class,
                SyncRecipeDataPacket::encode,
                SyncRecipeDataPacket::decode,
                SyncRecipeDataPacket::handle
        );

        // 注册 StartCraftingPacket
        INSTANCE.registerMessage(
                packetId++,
                StartCraftingPacket.class,
                StartCraftingPacket::encode,
                StartCraftingPacket::decode,
                StartCraftingPacket::handle
        );

        // 注册 SyncAnimationStatePacket
        INSTANCE.registerMessage(
                packetId++,
                SyncAnimationStatePacket.class,
                SyncAnimationStatePacket::encode,
                SyncAnimationStatePacket::decode,
                SyncAnimationStatePacket::handle
        );

        // 注册 SyncGuiOpenStatePacket
        INSTANCE.registerMessage(
                packetId++,
                SyncGuiOpenStatePacket.class,
                SyncGuiOpenStatePacket::encode,
                SyncGuiOpenStatePacket::decode,
                SyncGuiOpenStatePacket::handle
        );
    }
}