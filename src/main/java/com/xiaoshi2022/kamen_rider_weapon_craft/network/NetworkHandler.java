package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.items.custom.Heiseisword;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import static com.xiaoshi2022.kamen_rider_weapon_craft.Kamen_Rider_Weapon_Craft.LOGGER;
import static com.xiaoshi2022.kamen_rider_weapon_craft.Kamen_Rider_Weapon_Craft.MOD_ID;

/**
 * 网络处理类，负责注册和管理数据包的发送和接收
 * 适配Fabric 1.21.8网络API
 */
public class NetworkHandler {

    // 定义按键类型常量
    public static final String RIDER_SELECTION = "rider_selection";
    public static final String ULTIMATE_MODE = "ultimate_mode";

    // 定义Payload记录
    public record KeyPressPayload(String keyType, boolean isPressed) implements CustomPayload {
        public static final CustomPayload.Id<KeyPressPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "key_press"));

        public static final PacketCodec<PacketByteBuf, KeyPressPayload> CODEC = PacketCodec.of(
                KeyPressPayload::write,
                KeyPressPayload::read
        );

        // 写入方法
        public void write(PacketByteBuf buf) {
            buf.writeString(keyType);
            buf.writeBoolean(isPressed);
        }

        // 读取方法
        public static KeyPressPayload read(PacketByteBuf buf) {
            return new KeyPressPayload(buf.readString(), buf.readBoolean());
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /**
     * 注册所有网络处理器
     */
    public static void register() {
        // 1. 首先在PayloadTypeRegistry中注册payload类型
        PayloadTypeRegistry.playC2S().register(KeyPressPayload.ID, KeyPressPayload.CODEC);

        // 2. 然后注册服务端接收处理器
        ServerPlayNetworking.registerGlobalReceiver(KeyPressPayload.ID,
                (payload, context) -> {
                    // 直接使用payload对象，不再从缓冲区读取
                    // 在主线程上处理数据包
                    context.server().execute(() -> {
                        handleKeyPressOnServer(context.player(), payload.keyType(), payload.isPressed());
                    });
                }
        );
    }

    /**
     * 从客户端发送按键按下数据包到服务端
     */
    @Environment(EnvType.CLIENT)
    public static void sendKeyPressPacket(String keyType, boolean isPressed) {
        // 创建Payload对象并发送
        KeyPressPayload payload = new KeyPressPayload(keyType, isPressed);
        ClientPlayNetworking.send(payload);
    }

    /**
     * 在服务端处理按键按下事件
     */
    private static void handleKeyPressOnServer(ServerPlayerEntity player, String keyType, boolean isPressed) {
        // 安全检查
        if (player == null || keyType == null) {
            LOGGER.warn("Received invalid key press packet");
            return;
        }

        // 确保只在按键按下时处理（避免重复处理）
        if (isPressed) {
            // 根据按键类型调用相应的处理方法
            switch (keyType) {
                case RIDER_SELECTION:
                    Heiseisword.handleRiderSelectionKeyPress(player);
                    break;
                case ULTIMATE_MODE:
                    Heiseisword.handleUltimateKeyPress(player);
                    break;
                default:
                    LOGGER.warn("Unknown key type: {}", keyType);
                    break;
            }
        }

        LOGGER.info("Player {} pressed key: {}, state: {}",
                player.getName().getString(), keyType, isPressed);
    }
}