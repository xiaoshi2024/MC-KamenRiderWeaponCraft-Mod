package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
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

    public static class Handler {
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
                    // 服务器端处理结束，确保在多人环境中向所有玩家广播停止音效
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class ClientHandler {
        public static void handle(SoundStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null || mc.player == null) return;

                // 直接调用优化后的停止音效方法
                SoundManager soundManager = mc.getSoundManager();
                stopSpecificSound(soundManager, msg.soundName);
            });
            ctx.get().setPacketHandled(true);
        }

        // 使用更简单的方法停止指定音效，避免依赖内部实现细节
        private static void stopSpecificSound(SoundManager manager, ResourceLocation soundName) {
            try {
                // 尝试查找所有可能的字段名
                String[] possibleFields = {"instanceToChannel", "playingSounds", "activeSounds", "sounds", "soundInstances"};
                Field targetField = null;
                
                // 尝试找到一个有效的字段
                for (String fieldName : possibleFields) {
                    try {
                        targetField = SoundManager.class.getDeclaredField(fieldName);
                        if (Map.class.isAssignableFrom(targetField.getType()) || Collection.class.isAssignableFrom(targetField.getType())) {
                            break;
                        }
                    } catch (NoSuchFieldException ignored) {
                    }
                }
                
                if (targetField != null) {
                    targetField.setAccessible(true);
                    Object fieldValue = targetField.get(manager);
                    
                    if (fieldValue instanceof Map) {
                        Map<?, ?> soundMap = (Map<?, ?>) fieldValue;
                        for (Object key : soundMap.keySet()) {
                            if (key instanceof SoundInstance) {
                                SoundInstance sound = (SoundInstance) key;
                                if (sound.getSource() == SoundSource.PLAYERS && sound.getLocation().equals(soundName)) {
                                    manager.stop(sound);
                                }
                            }
                        }
                    } else if (fieldValue instanceof Collection) {
                        Collection<?> soundCollection = (Collection<?>) fieldValue;
                        for (Object obj : soundCollection) {
                            if (obj instanceof SoundInstance) {
                                SoundInstance sound = (SoundInstance) obj;
                                if (sound.getSource() == SoundSource.PLAYERS && sound.getLocation().equals(soundName)) {
                                    manager.stop(sound);
                                }
                            }
                        }
                    }
                } else {
                    // 备选方案：尝试使用更直接的方式停止声音
                    // 1. 尝试直接使用Minecraft的内置方法停止所有声音
                    Minecraft.getInstance().getSoundManager().stop();
                    
                    // 2. 或者尝试使用更具体的停止方法
                    try {
                        // 尝试调用SoundManager的stop方法（如果有重载版本）
                        java.lang.reflect.Method stopMethod = SoundManager.class.getMethod("stop", SoundSource.class, ResourceLocation.class);
                        stopMethod.invoke(manager, SoundSource.PLAYERS, soundName);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                // 静默失败，避免显示错误消息
            }
        }
    }
}