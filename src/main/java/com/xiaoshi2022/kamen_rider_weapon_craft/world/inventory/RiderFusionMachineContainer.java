package com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachineBlockEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModContainers;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

public class RiderFusionMachineContainer extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>>, Container {
    public final static HashMap<String, Object> guistate = new HashMap<>();
    public final Level world;
    public final Player entity;
    public int x, y, z;
    private ContainerLevelAccess access = ContainerLevelAccess.NULL;
    public IItemHandler internal;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private boolean bound = false;
    private Supplier<Boolean> boundItemMatcher = null;
    private Entity boundEntity = null;
    private BlockEntity boundBlockEntity = null;
    private boolean isProgressing = false; // 是否正在合成
    // 在类中添加数据追踪字段
    private final ContainerData data;
    public int craftingProgress;
    public int maxCraftingProgress = 60; // 与BlockEntity一致
    public void tick() {
        if (isProgressing) {
            craftingProgress++;
            if (craftingProgress >= maxCraftingProgress) {
                completeCrafting();
                isProgressing = false;
            }
        }
        broadcastChanges(); // 确保进度条状态同步到客户端
    }

    public void completeCrafting() {
        for (Recipe<Container> recipe : world.getRecipeManager().getAllRecipesFor(ModRecipes.RIDER_FUSION_RECIPE.get())) {
            Container container = new SimpleContainer(internal.getSlots());
            for (int i = 0; i < internal.getSlots(); i++) {
                container.setItem(i, internal.getStackInSlot(i));
            }
            if (recipe.matches(container, world)) {
                ItemStack result = recipe.assemble(container, world.registryAccess());
                if (!result.isEmpty()) {
                    internal.insertItem(3, result, false);
                    internal.extractItem(0, internal.getStackInSlot(0).getCount(), false);
                    internal.extractItem(1, internal.getStackInSlot(1).getCount(), false);
                    internal.extractItem(2, internal.getStackInSlot(2).getCount(), false);
                }
            }
        }
        craftingProgress = 0;
        broadcastChanges(); // 确保进度条状态同步到客户端
    }

    // 删除原有的 tryCraft 方法，改为向服务端发送数据包
    public void tryCraft() {
        if (this.world instanceof ServerLevel serverLevel) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(new BlockPos(x, y, z));
            if (blockEntity instanceof RiderFusionMachineBlockEntity be) {
                be.startCrafting(); // 调用方块实体的服务端方法
            }
        }
    }

    // 新增方法：从data中获取进度值
    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
        if (id == 0) craftingProgress = data;
        if (id == 1) maxCraftingProgress = data;
    }

    public RiderFusionMachineContainer(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModContainers.RIDER_FUSION_MACHINE.get(), id);
        this.entity = inv.player;
        this.world = inv.player.level();
        this.internal = new ItemStackHandler(4);
        // 添加数据同步（关键修复）
        this.data = new SimpleContainerData(2); // 同步2个值：进度和最大进度
        addDataSlots(data); // 注册数据槽
        BlockPos pos = null;
        if (extraData != null) {
            pos = extraData.readBlockPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            access = ContainerLevelAccess.create(world, pos);
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
        this.customSlots.put(0, this.addSlot(new SlotItemHandler(internal, 0, 35, 23) { // 槽位0: 基础控制电路
        }));

        this.customSlots.put(1, this.addSlot(new SlotItemHandler(internal, 1, 54, 43) { // 槽位1: 合金矿
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == ModItems.RIDER_FORGING_ALLOY_ORE.get(); // 替换为实际物品
            }
        }));

        this.customSlots.put(2, this.addSlot(new SlotItemHandler(internal, 2, 73, 23) { // 槽位2: 可选材料
        }));
        this.customSlots.put(3, this.addSlot(new SlotItemHandler(internal, 3, 153, 23) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player thePlayer, ItemStack stack) {
                // 当从输出槽位取出物品时，不执行任何操作
            }
        }));
        this.customSlots.put(4, this.addSlot(new SlotItemHandler(internal, 4, 153, 43) { // 槽位4: 能量输入槽
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == Items.COAL || stack.getItem() == Items.CHARCOAL; // 只允许放置煤炭或木炭
            }
        }));
        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 0 + 8 + sj * 18, 0 + 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(inv, si, 0 + 8 + si * 18, 0 + 142));
    }

    @Override
    public int getContainerSize() {
        return internal.getSlots(); // 返回容器的槽位数量
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < internal.getSlots(); i++) {
            if (!internal.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return internal.getStackInSlot(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return internal.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = internal.getStackInSlot(index);
        internal.extractItem(index, stack.getCount(), false);
        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        internal.insertItem(index, stack, false);
        if (index < 3 && !world.isClientSide) { // 只有在输入槽位中放置物品时才尝试合成
            ItemStack result = tryCraft();
            if (!result.isEmpty()) {
                // 合成成功，更新界面
                this.broadcastChanges();
            }
        }
    }

    @Override
    public void setChanged() {
        // 当容器内容发生变化时调用
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < internal.getSlots(); i++) {
            internal.extractItem(i, internal.getStackInSlot(i).getCount(), false);
        }
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
            if (index < 4) { // 输入槽位
                if (!this.moveItemStackTo(itemstack1, 4, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == 4) { // 能量槽位
                if (!this.moveItemStackTo(itemstack1, 5, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else { // 玩家背包
                if (!this.moveItemStackTo(itemstack1, 0, 4, false)) {
                    if (index < 4 + 27) {
                        if (!this.moveItemStackTo(itemstack1, 4 + 27, this.slots.size(), true)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(itemstack1, 4, 4 + 27, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
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

    @Override
    protected boolean moveItemStackTo(ItemStack p_38904_, int p_38905_, int p_38906_, boolean p_38907_) {
        boolean flag = false;
        int i = p_38905_;
        if (p_38907_) {
            i = p_38906_ - 1;
        }
        if (p_38904_.isStackable()) {
            while (!p_38904_.isEmpty()) {
                if (p_38907_) {
                    if (i < p_38905_) {
                        break;
                    }
                } else if (i >= p_38906_) {
                    break;
                }
                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (slot.mayPlace(itemstack) && !itemstack.isEmpty() && ItemStack.isSameItemSameTags(p_38904_, itemstack)) {
                    int j = itemstack.getCount() + p_38904_.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), p_38904_.getMaxStackSize());
                    if (j <= maxSize) {
                        p_38904_.setCount(0);
                        itemstack.setCount(j);
                        slot.set(itemstack);
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        p_38904_.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.set(itemstack);
                        flag = true;
                    }
                }
                if (p_38907_) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        if (!p_38904_.isEmpty()) {
            if (p_38907_) {
                i = p_38906_ - 1;
            } else {
                i = p_38905_;
            }
            while (true) {
                if (p_38907_) {
                    if (i < p_38905_) {
                        break;
                    }
                } else if (i >= p_38906_) {
                    break;
                }
                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(p_38904_)) {
                    if (p_38904_.getCount() > slot1.getMaxStackSize()) {
                        slot1.setByPlayer(p_38904_.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.setByPlayer(p_38904_.split(p_38904_.getCount()));
                    }
                    slot1.setChanged();
                    flag = true;
                    break;
                }
                if (p_38907_) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        return flag;
    }

    @Override
    public void removed(Player playerIn) {
        // 处理容器被移除时的逻辑
        super.removed(playerIn);
        if (!bound && playerIn instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
                for (int j = 0; j < internal.getSlots(); ++j) {
                    playerIn.drop(internal.extractItem(j, internal.getStackInSlot(j).getCount(), false), false);
                }
            } else {
                for (int i = 0; i < internal.getSlots(); ++i) {
                    playerIn.getInventory().placeItemBackInInventory(internal.extractItem(i, internal.getStackInSlot(i).getCount(), false));
                }
            }
        }
    }

    public Map<Integer, Slot> get() {
        return customSlots;
    }

    public int getCraftingProgress() {
        return craftingProgress;
    }

    public int getMaxCraftingProgress() {
        return maxCraftingProgress;
    }
}