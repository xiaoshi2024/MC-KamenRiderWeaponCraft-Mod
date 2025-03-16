package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class SyncRecipeDataPacket {
    private static final Logger LOGGER = LogManager.getLogger();
    private final int craftingProgress;
    private final int maxCraftingProgress;
    private final boolean isCrafting;
    private final BlockPos pos;

    public SyncRecipeDataPacket(int craftingProgress, int maxCraftingProgress, boolean isCrafting, BlockPos pos) {
        this.craftingProgress = craftingProgress;
        this.maxCraftingProgress = maxCraftingProgress;
        this.isCrafting = isCrafting;
        this.pos = pos;
        LOGGER.debug("[Network] Created SyncRecipeDataPacket for position: {}, craftingProgress: {}, maxCraftingProgress: {}, isCrafting: {}",
                pos, craftingProgress, maxCraftingProgress, isCrafting);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(craftingProgress);
        buf.writeInt(maxCraftingProgress);
        buf.writeBoolean(isCrafting);
        buf.writeBlockPos(pos);
        LOGGER.debug("[Network] Wrote SyncRecipeDataPacket to buffer for position: {}, craftingProgress: {}, maxCraftingProgress: {}, isCrafting: {}",
                pos, craftingProgress, maxCraftingProgress, isCrafting);
    }

    public static SyncRecipeDataPacket decode(FriendlyByteBuf buf) {
        int craftingProgress = buf.readInt();
        int maxCraftingProgress = buf.readInt();
        boolean isCrafting = buf.readBoolean();
        BlockPos pos = buf.readBlockPos();
        LOGGER.debug("[Network] Read SyncRecipeDataPacket from buffer for position: {}, craftingProgress: {}, maxCraftingProgress: {}, isCrafting: {}",
                pos, craftingProgress, maxCraftingProgress, isCrafting);
        return new SyncRecipeDataPacket(craftingProgress, maxCraftingProgress, isCrafting, pos);
    }

    public static void handle(SyncRecipeDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            NetworkEvent.Context context = ctx.get();
            if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                handleClient(packet);
            } else {
                LOGGER.warn("Received SyncRecipeDataPacket in wrong direction: {}", context.getDirection());
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleClient(SyncRecipeDataPacket packet) {
        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        if (client == null) return;

        Level level = client.level;
        if (level == null || !level.isLoaded(packet.pos)) {
            LOGGER.debug("Received recipe data for unloaded position: {}", packet.pos);
            return;
        }

        client.execute(() -> {
            try {
                BlockEntity be = level.getBlockEntity(packet.pos);
                if (be instanceof RiderFusionMachineBlockEntity fusionMachine) {
                    // 验证数据合理性
                    if (packet.craftingProgress < 0 || packet.craftingProgress > packet.maxCraftingProgress) {
                        LOGGER.error("Invalid crafting progress values for {}: progress={}, max={}", packet.pos,
                                packet.craftingProgress, packet.maxCraftingProgress);
                        return;
                    }
                    fusionMachine.handleRecipeSync(
                            packet.craftingProgress,
                            packet.maxCraftingProgress,
                            packet.isCrafting
                    );
                } else {
                    LOGGER.error("Block entity at position {} is not a RiderFusionMachineBlockEntity", packet.pos);
                }
            } catch (Exception e) {
                LOGGER.error("Error syncing recipe data at {}: {}", packet.pos, e.toString());
            }
        });
    }
}