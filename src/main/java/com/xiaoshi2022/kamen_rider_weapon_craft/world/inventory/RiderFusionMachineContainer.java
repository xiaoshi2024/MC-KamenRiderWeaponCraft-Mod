package com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachineBlockEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class RiderFusionMachineContainer extends AbstractContainerMenu {
    private final IItemHandler internal;
    private final Level world;
    private final BlockPos pos;
    private final RiderFusionMachineBlockEntity blockEntity;

    public RiderFusionMachineContainer(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModContainers.RIDER_FUSION_MACHINE.get(), id);
        this.world = inv.player.level();
        this.pos = extraData.readBlockPos();

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof RiderFusionMachineBlockEntity) {
            this.blockEntity = (RiderFusionMachineBlockEntity) be;
            this.internal = blockEntity.getItemHandler();

            this.blockEntity.onGuiOpened();
        } else {
            throw new IllegalStateException("Block entity type mismatch");
        }

        addSlot(new SlotItemHandler(internal, 0, 35, 23) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });
        addSlot(new SlotItemHandler(internal, 1, 54, 43) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });
        addSlot(new SlotItemHandler(internal, 2, 73, 23) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });
        addSlot(new SlotItemHandler(internal, 3, 153, 43) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        addSlot(new SlotItemHandler(internal, 4, 153, 23) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                int index = column + row * 9 + 9;
                int x = 8 + column * 18;
                int y = 84 + row * 18;
                addSlot(new Slot(inv, index, x, y));
            }
        }

        for (int slot = 0; slot < 9; ++slot) {
            int x = 8 + slot * 18;
            addSlot(new Slot(inv, slot, x, 142));
        }
    }

    public int getCraftingProgress() {
        return blockEntity.getCraftingProgress();
    }

    public int getMaxCraftingProgress() {
        return blockEntity.getMaxCraftingProgress();
    }

    public void setCraftingProgress(int craftingProgress, int maxCraftingProgress, boolean isCrafting) {
        blockEntity.setCraftingProgress(craftingProgress, maxCraftingProgress, isCrafting);
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 4) {
                if (!this.moveItemStackTo(itemstack1, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == 4) {
                if (!this.moveItemStackTo(itemstack1, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean success = false;
                for (int i = 0; i < 4; i++) {
                    Slot inputSlot = this.slots.get(i);
                    if (inputSlot.mayPlace(itemstack1)) {
                        if (this.moveItemStackTo(itemstack1, i, i + 1, false)) {
                            success = true;
                            break;
                        }
                    }
                }
                if (!success) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }
}