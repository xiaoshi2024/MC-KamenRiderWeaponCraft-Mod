package com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModContainers;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

public class SonicBowContainer extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
    public final static HashMap<String, Object> guistate = new HashMap<>();
    public final Level world;
    public final Player entity;
    public int x, y, z;
    private ContainerLevelAccess access = ContainerLevelAccess.NULL;
    private IItemHandler internal;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private boolean bound = false;
    private Supplier<Boolean> boundItemMatcher = null;
    private Entity boundEntity = null;
    private BlockEntity boundBlockEntity = null;
    private int progress = 0;

    public SonicBowContainer(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModContainers.SONIC_BOW_CONTAINER.get(), id);
        this.entity = inv.player;
        this.world = inv.player.level();
        this.internal = new ItemStackHandler(1);
        BlockPos pos = null;
        if (extraData != null && extraData.readableBytes() >= 12) { // 12 bytes for BlockPos (3 int values)
            pos = extraData.readBlockPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            access = ContainerLevelAccess.create(world, pos);
        } else {
            // Handle the case where there is not enough data
            // This could involve logging an error, throwing an exception, or providing a default value
        }
        if (pos != null) {
            if (extraData.readableBytes() == 1) { // bound to item
                byte hand = extraData.readByte();
                ItemStack itemstack = hand == 0 ? this.entity.getMainHandItem() : this.entity.getOffhandItem();
                this.boundItemMatcher = () -> itemstack == (hand == 0 ? this.entity.getMainHandItem() : this.entity.getOffhandItem());
                itemstack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(capability -> {
                    this.internal = capability;
                    this.bound = true;
                });
            } else if (extraData.readableBytes() > 1) { // bound to entity
                extraData.readByte(); // drop padding
                boundEntity = world.getEntity(extraData.readVarInt());
                if (boundEntity != null)
                    boundEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(capability -> {
                        this.internal = capability;
                        this.bound = true;
                    });
            } else { // might be bound to block
                boundBlockEntity = this.world.getBlockEntity(pos);
                if (boundBlockEntity != null)
                    boundBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(capability -> {
                        this.internal = capability;
                        this.bound = true;
                    });
            }
        }
        this.customSlots.put(0, this.addSlot(new SlotItemHandler(internal, 0, 30, 28) {
            private final int slot = 0;

            @Override
            public boolean mayPlace(ItemStack stack) {
                return ModItems.MELON.get().asItem() == stack.getItem();
            }
        }));
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 9; ++j)
                this.addSlot(new Slot(inv, j + (i + 1) * 9, 0 + 8 + j * 18, 0 + 84 + i * 18));
        for (int i = 0; i < 9; ++i)
            this.addSlot(new Slot(inv, i, 0 + 8 + i * 18, 0 + 142));
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.bound) {
            if (this.boundItemMatcher != null)
                return this.boundItemMatcher.get();
            else if (this.boundBlockEntity != null)
                return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
            else if (this.boundEntity != null)
                return this.boundEntity.isAlive();
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, itemstack1);
        }
        return itemstack;
    }

//
//    @Override
//    protected boolean moveItemStackTo(ItemStack p_38904_, int p_38905_, int p_38906_, boolean p_38907_) {
//        boolean flag = false;
//        int i = p_38905_;
//        if (p_38907_) {
//            i = p_38906_ - 1;
//        }
//        if (p_38904_.isStackable()) {
//            while (!p_38904_.isEmpty()) {
//                if (p_38907_) {
//                    if (i < p_38905_) {
//                        break;
//                    }
//                } else if (i >= p_38906_) {
//                    break;
//                }
//                Slot slot = this.slots.get(i);
//                ItemStack itemstack = slot.getItem();
//                if (slot.mayPlace(itemstack) && !itemstack.isEmpty() && ItemStack.isSameItemSameTags(p_38904_, itemstack)) {
//                    int j = itemstack.getCount() + p_38904_.getCount();
//                    int maxSize = Math.min(slot.getMaxStackSize(), p_38904_.getMaxStackSize());
//                    if (j <= maxSize) {
//                        p_38904_.setCount(0);
//                        itemstack.setCount(j);
//                        slot.set(itemstack);
//                        flag = true;
//                    } else if (itemstack.getCount() < maxSize) {
//                        p_38904_.shrink(maxSize - itemstack.getCount());
//                        itemstack.setCount(maxSize);
//                        slot.set(itemstack);
//                        flag = true;
//                    }
//                }
//                if (p_38907_) {
//                    --i;
//                } else {
//                    ++i;
//                }
//            }
//        }
//        if (!p_38904_.isEmpty()) {
//            if (p_38907_) {
//                i = p_38906_ - 1;
//            } else {
//                i = p_38905_;
//            }
//            while (true) {
//                if (p_38907_) {
//                    if (i < p_38905_) {
//                        break;
//                    }
//                } else if (i >= p_38906_) {
//                    break;
//                }
//                Slot slot1 = this.slots.get(i);
//                ItemStack itemstack1 = slot1.getItem();
//                if (itemstack1.isEmpty() && slot1.mayPlace(p_38904_)) {
//                    if (p_38904_.getCount() > slot1.getMaxStackSize()) {
//                        slot1.setByPlayer(p_38904_.split(slot1.getMaxStackSize()));
//                    } else {
//                        slot1.setByPlayer(p_38904_.split(p_38904_.getCount()));
//                    }
//                    slot1.setChanged();
//                    flag = true;
//                    break;
//                }
//                if (p_38907_) {
//                    --i;
//                } else {
//                    ++i;
//                }
//            }
//        }
//        return flag;
//    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (!bound && playerIn instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
                for (int j = 0; j < internal.getSlots(); ++j) {
                    if (j == 0)
                        continue;
                    playerIn.drop(internal.extractItem(j, internal.getStackInSlot(j).getCount(), false), false);
                }
            } else {
                for (int i = 0; i < internal.getSlots(); ++i) {
                    if (i == 0)
                        continue;
                    playerIn.getInventory().placeItemBackInInventory(internal.extractItem(i, internal.getStackInSlot(i).getCount(), false));
                }
            }
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        // 允许在进度条满时取走物品
        return this.progress >= 100;
    }

    public void updateProgress(int newProgress) {
        this.progress = newProgress;
        if (this.progress >= 100) {
            this.progress = 0;
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player != null) {
                Level world = player.level();
                world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);

                ItemStack sonicArrow = player.getOffhandItem();
                if (!sonicArrow.isEmpty() && sonicArrow.getItem() == ModItems.SONICARROW.get()) {
                    CompoundTag tag = sonicArrow.getOrCreateTag();
                    tag.putInt("Charge", 100);
                    sonicArrow.setTag(tag);
                    world.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_DIAMOND, SoundSource.PLAYERS, 1.0F, 1.0F);
                    sonicArrow.enchant(Enchantments.SHARPNESS, 1);
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
                }
            }
        }
    }

    public int getProgress() {
        return this.progress;
    }

    public Map<Integer, Slot> get() {
        return customSlots;
    }

    public Slot getCustomSlot() {
        return customSlots.get(0);
    }
}