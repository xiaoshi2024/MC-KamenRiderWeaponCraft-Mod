package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

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

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(craftingProgress);
        buf.writeInt(maxCraftingProgress);
        buf.writeBoolean(isCrafting);
        buf.writeBlockPos(pos);
    }

    public static SyncRecipeDataPacket decode(FriendlyByteBuf buf) {
        int craftingProgress = buf.readInt();
        int maxCraftingProgress = buf.readInt();
        boolean isCrafting = buf.readBoolean();
        BlockPos pos = buf.readBlockPos();
        return new SyncRecipeDataPacket(craftingProgress, maxCraftingProgress, isCrafting, pos);
    }

    public static void handle(SyncRecipeDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            NetworkEvent.Context context = ctx.get();
            if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                // 客户端处理逻辑
                BlockPos pos = packet.pos;
                int craftingProgress = packet.craftingProgress;
                int maxCraftingProgress = packet.maxCraftingProgress;
                boolean isCrafting = packet.isCrafting;
                // 更新客户端的合成进度
            }
        });
        ctx.get().setPacketHandled(true);
    }
}