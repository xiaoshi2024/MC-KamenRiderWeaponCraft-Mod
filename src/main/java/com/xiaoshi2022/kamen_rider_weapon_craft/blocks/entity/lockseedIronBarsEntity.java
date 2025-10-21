package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.entity;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class lockseedIronBarsEntity extends BlockEntity {

    private ItemStackHandler inventory = new ItemStackHandler(8) { // 修改为 8
        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }
    };

    public lockseedIronBarsEntity(BlockPos Pos, BlockState State) {
        super(ModBlockEntities.LOCKSEEDIRONBARS_ENTITY.get(), Pos, State);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        } else {
            inventory.deserializeNBT(tag);
        }
    }

    private CompoundTag writeItems(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.put("Inventory", inventory.serializeNBT());
        return compoundTag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        this.writeItems(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.writeItems(new CompoundTag());
    }

    public boolean addItem(ItemStack itemStack) {
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                this.inventory.setStackInSlot(i, itemStack.split(1));
                setChanged();
                return true;
            }
        }
        return false;
    }

    public ItemStack removeItem() {
        for (int i = this.inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                this.inventory.setStackInSlot(i, ItemStack.EMPTY);
                setChanged();
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public void dropAllItems(Level level, BlockPos pos) { // 确保所有物品掉落
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    public Vec2 getItemOffset(int i) {
        float x = 0.5f;
        float y = 0.5f;
        Vec2[] offsets = new Vec2[]{
                new Vec2(x, y), new Vec2(x, -y),
                new Vec2(-x, y), new Vec2(-x, -y),
                new Vec2(x, y), new Vec2(x, -y),
                new Vec2(-x, y), new Vec2(-x, -y)
        };
        return offsets[i];
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}