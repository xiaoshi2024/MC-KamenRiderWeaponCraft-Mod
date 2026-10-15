package com.xiaoshi2022.kamenriderweaponcraft.network;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.Item.client.sonicarrow.SonicArrowMuzzleTracker;
import com.xiaoshi2022.kamenriderweaponcraft.client.render.thunder.LightningArcBuilder;
import com.xiaoshi2022.kamenriderweaponcraft.client.render.thunder.ThunderConfig;
import com.xiaoshi2022.kamenriderweaponcraft.client.render.thunder.ThunderRenderManager;
import com.xiaoshi2022.kamenriderweaponcraft.client.render.thunder.ThunderSegment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 音速弓激光雷电弧渲染包（服务端→客户端）
 * 携带起点/终点与颜色（按装载的锁种变化）
 */
public record SonicArrowLaserPacket(
        double startX, double startY, double startZ,
        double endX, double endY, double endZ,
        int mainColor, int coreColor
) implements CustomPacketPayload {

    public static final Type<SonicArrowLaserPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "sonic_arrow_laser"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SonicArrowLaserPacket> STREAM_CODEC =
            StreamCodec.ofMember(SonicArrowLaserPacket::write, SonicArrowLaserPacket::read);

    private static SonicArrowLaserPacket read(RegistryFriendlyByteBuf buf) {
        return new SonicArrowLaserPacket(
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readInt(), buf.readInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeDouble(startX);
        buf.writeDouble(startY);
        buf.writeDouble(startZ);
        buf.writeDouble(endX);
        buf.writeDouble(endY);
        buf.writeDouble(endZ);
        buf.writeInt(mainColor);
        buf.writeInt(coreColor);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SonicArrowLaserPacket payload, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                Vec3 start = new Vec3(payload.startX, payload.startY, payload.startZ);
                // 本玩家的射击：起点改用弓箭尖骨骼的最新位置（激光从骨骼射出而非胸口）
                Vec3 muzzle = SonicArrowMuzzleTracker.getFresh();
                if (muzzle != null && muzzle.distanceToSqr(start) < 6 * 6) {
                    start = muzzle;
                }
                renderThunder(start,
                        new Vec3(payload.endX, payload.endY, payload.endZ),
                        payload.mainColor, payload.coreColor);
            });
        }
    }

    private static void renderThunder(Vec3 start, Vec3 end, int mainColor, int coreColor) {
        int seed = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        List<Vec3[]> arcs = LightningArcBuilder.generateStraightLightning(start, end, seed);
        List<ThunderSegment> segments = new ArrayList<>();

        for (Vec3[] arc : arcs) {
            segments.add(new ThunderSegment(arc[0], arc[1],
                    ThunderConfig.THUNDER_BASE_WIDTH, mainColor, 0.9f, 0));
            segments.add(new ThunderSegment(arc[0], arc[1],
                    ThunderConfig.THUNDER_CORE_WIDTH, coreColor, 1.0f, 0));
        }

        ThunderRenderManager.getInstance().addThunder(segments, ThunderConfig.THUNDER_LIFETIME);
    }
}
