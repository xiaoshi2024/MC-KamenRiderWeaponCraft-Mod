package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class KaizokuHassyarSoundPacket {

    public enum SoundType {
        TRAIN_A,
        TRAIN_B,
        TRAIN_C,
        TRAIN_D,
        SHOOT
    }

    private final SoundType soundType;
    private final double x;
    private final double y;
    private final double z;

    public KaizokuHassyarSoundPacket(SoundType soundType, double x, double y, double z) {
        this.soundType = soundType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(soundType);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }

    public static KaizokuHassyarSoundPacket decode(FriendlyByteBuf buffer) {
        return new KaizokuHassyarSoundPacket(
                buffer.readEnum(SoundType.class),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
    }

    public static void handle(KaizokuHassyarSoundPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getSender();
            if (player != null) {
                // 获取对应的音效
                SoundEvent soundEvent = switch (message.soundType) {
                    case TRAIN_A -> ModSounds.TRAIN_A.get();
                    case TRAIN_B -> ModSounds.TRAIN_B.get();
                    case TRAIN_C -> ModSounds.TRAIN_C.get();
                    case TRAIN_D -> ModSounds.TRAIN_D.get();
                    case SHOOT -> ModSounds.SHOOTKR.get();
                };
                
                // 播放音效并广播给周围的玩家
                player.level().playSound(null, message.x, message.y, message.z, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
                
                // 广播给周围的玩家
                for (ServerPlayer otherPlayer : player.serverLevel().getPlayers(p -> p.distanceToSqr(message.x, message.y, message.z) <= 16 * 16)) {
                    if (otherPlayer != player) {
                        otherPlayer.level().playSound(null, message.x, message.y, message.z, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sendToServer(KaizokuHassyarSoundPacket message) {
        NetworkHandler.sendToServer(message);
    }
}
