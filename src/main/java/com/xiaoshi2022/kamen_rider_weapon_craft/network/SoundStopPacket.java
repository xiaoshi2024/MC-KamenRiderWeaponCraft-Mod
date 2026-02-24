package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

public class SoundStopPacket {
    private final int playerId;
    private final ResourceLocation soundName;

    public SoundStopPacket(int playerId, ResourceLocation soundName) {
        this.playerId = playerId;
        this.soundName = soundName;
    }

    // 编码方法
    public static void encode(SoundStopPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.playerId);
        buffer.writeResourceLocation(msg.soundName);
    }

    // 解码方法
    public static SoundStopPacket decode(FriendlyByteBuf buffer) {
        int playerId = buffer.readInt();
        ResourceLocation soundName = buffer.readResourceLocation();
        return new SoundStopPacket(playerId, soundName);
    }

    // 服务器端处理器
    public static void handle(SoundStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                ServerPlayer sender = ctx.get().getSender();

                if (sender != null) {
                    // 关键：使用传入的msg，不要重新创建
                    NetworkHandler.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender),
                            msg  // 直接使用传入的msg
                    );
                }
            } else {
                // 客户端处理器
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null || mc.player == null) return;

                SoundManager soundManager = mc.getSoundManager();
                stopSoundByName(soundManager, msg.soundName);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // 声音停止方法
    private static void stopSoundByName(SoundManager manager, ResourceLocation soundName) {
        try {
            // 直接停止所有音源类别下的该音效
            for (SoundSource source : SoundSource.values()) {
                try {
                    manager.stop(soundName, source);
                } catch (Exception e) {
                    // 忽略错误，继续尝试其他音源
                }
            }
        } catch (Exception e) {
            // 静默失败，避免显示错误消息
        }
    }
}