package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SoundStopPacket {
    private final int playerId;
    private final ResourceLocation soundName;

    public SoundStopPacket(int playerId, ResourceLocation soundName) {
        this.playerId = playerId;
        this.soundName = soundName;
    }

    // 编码解码方法
    public static void encode(SoundStopPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.playerId);
        buffer.writeResourceLocation(msg.soundName);
    }

    public static SoundStopPacket decode(FriendlyByteBuf buffer) {
        int playerId = buffer.readInt();
        ResourceLocation soundName = buffer.readResourceLocation();
        return new SoundStopPacket(playerId, soundName);
    }

    public static void handle(SoundStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                ServerPlayer sender = ctx.get().getSender();
                
                // 服务器端逻辑：当数据包从客户端发送到服务器时执行
                if (sender != null) {
                    // 在服务器端执行停止音效命令作为备份
                    CommandSourceStack source = sender.createCommandSourceStack()
                            .withPermission(2)
                            .withSuppressedOutput();

                    // 使用@a选择器确保在多人服务器中向所有玩家发送停止音效命令
                    String command = String.format(
                            "/stopsound @a players %s",
                            msg.soundName.toString()
                    );
                    sender.server.getCommands().performPrefixedCommand(source, command);
                }
            } else {
                // 客户端逻辑：当数据包从服务器发送到客户端时执行
                // 使用DistExecutor确保客户端专用代码只在客户端执行
                net.minecraftforge.fml.DistExecutor.runWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                    // 客户端专用代码，只在客户端执行
                    try {
                        // 使用反射动态加载客户端类，避免直接引用导致服务器崩溃
                        Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                        Object mcInstance = minecraftClass.getMethod("getInstance").invoke(null);
                        Object level = minecraftClass.getMethod("level").invoke(mcInstance);
                        Object player = minecraftClass.getMethod("player").invoke(mcInstance);
                        
                        if (level == null || player == null) return;
                        
                        // 获取SoundManager并停止指定音效
                        Object soundManager = minecraftClass.getMethod("getSoundManager").invoke(mcInstance);
                        Class<?> soundManagerClass = Class.forName("net.minecraft.client.sounds.SoundManager");
                        
                        // 尝试调用stop方法停止所有声音
                        soundManagerClass.getMethod("stop").invoke(soundManager);
                    } catch (Exception e) {
                        // 静默失败，避免显示错误消息
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}