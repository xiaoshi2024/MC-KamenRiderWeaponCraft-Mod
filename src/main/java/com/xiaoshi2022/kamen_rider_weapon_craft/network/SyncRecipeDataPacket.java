package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

public class SyncRecipeDataPacket {
    private final int craftingProgress;
    private final int maxCraftingProgress;
    private final boolean isCrafting;
    private final BlockPos pos;

    public SyncRecipeDataPacket(int craftingProgress, int maxCraftingProgress, boolean isCrafting, BlockPos pos) {
        this.craftingProgress = craftingProgress;
        this.maxCraftingProgress = maxCraftingProgress;
        this.isCrafting = isCrafting;
        this.pos = pos;

    }

    public SyncRecipeDataPacket(FriendlyByteBuf buffer) {
        this.craftingProgress = buffer.readInt();
        this.maxCraftingProgress = buffer.readInt();
        this.isCrafting = buffer.readBoolean();
        this.pos = buffer.readBlockPos();

    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(craftingProgress);
        buffer.writeInt(maxCraftingProgress);
        buffer.writeBoolean(isCrafting);
        buffer.writeBlockPos(pos);

    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {


            // Ensure it is handled on the client
            if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Level level = Minecraft.getInstance().level;
                if (level == null) {

                    return;
                }

                // Get the block entity
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity == null) {

                    return;
                }

                if (blockEntity instanceof RiderFusionMachineBlockEntity fusionMachine) {

                    // Safely update the data
                    fusionMachine.setCraftingProgress(craftingProgress, maxCraftingProgress, isCrafting);
                } else {
                }
            } else {
            }
        });

        context.get().setPacketHandled(true);
    }

    @Override
    public String toString() {
        return String.format(
            "SyncRecipeDataPacket{Progress=%d/%d, Status=%b, Position=%s}",
            craftingProgress, maxCraftingProgress, isCrafting, pos
        );
    }
}
