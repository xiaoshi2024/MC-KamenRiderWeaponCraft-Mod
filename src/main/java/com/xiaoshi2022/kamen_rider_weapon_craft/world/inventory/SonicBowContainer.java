package com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModContainers;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SonicBowContainer extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
    public final static HashMap<String, Object> guistate = new HashMap<>();
    public final Player entity;
    public ItemStackHandler internal;
    private final Map<Integer, Slot> customSlots = new HashMap<>();

    // 1) 新增：记录最后一次成功放入的物品
    public ItemStack lastInput = ItemStack.EMPTY;


    public SonicBowContainer(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModContainers.SSONIC.get(), id);
        this.entity = inv.player;

        // 获取玩家左手上的物品
        ItemStack offhandStack = entity.getOffhandItem();
        if (offhandStack.getItem() == ModItems.SONICARROW.get()) { // 确保是特定物品
            CompoundTag tag = offhandStack.getOrCreateTag();
            this.internal = new ItemStackHandler(1); // 只有一个槽位
            if (tag.contains("Inventory")) {
                this.internal.deserializeNBT(tag.getCompound("Inventory")); // 反序列化 NBT
            }
        } else {
            this.internal = new ItemStackHandler(1); // 备用处理
        }

        // 添加输入槽位
        this.customSlots.put(0, this.addSlot(new SlotItemHandler(internal, 0, 30, 28) {
            @Override public int getMaxStackSize() { return 1; }

            @Override public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == ModItems.MELON.get()
                        || stack.getItem() == com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.LEMON_ENERGY.get();
            }

            @Override public void set(ItemStack stack) {
                super.set(stack);
                // 真正放进槽位时记录
                if (!stack.isEmpty()) lastInput = stack.copy();
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return 1; // 限制每次只能放入一个物品
            }
        }));

        // 添加玩家背包槽位
        for (int si = 0; si < 3; ++si) {
            for (int sj = 0; sj < 9; ++sj) {
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
            }
        }
        for (int si = 0; si < 9; ++si) {
            this.addSlot(new Slot(inv, si, 8 + si * 18, 142));
        }

        // 播放音效
        if (!entity.level().isClientSide) {
            ((ServerLevel) entity.level()).playSound(null, entity.blockPosition(), ModSounds.LOCK_SEED_PUT_IN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // 容器始终有效
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 1) {
                if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        ItemStack bow = player.getOffhandItem();
        if (bow.getItem() != ModItems.SONICARROW.get()) return;

        // 同步槽位数据（无论有没有物品）
        bow.getOrCreateTag().put("Inventory", internal.serializeNBT());

        // 槽位空 → 回到默认
        if (internal.getStackInSlot(0).isEmpty()) {
            ((sonicarrow) bow.getItem()).switchMode(bow, sonicarrow.Mode.DEFAULT);
        } else {
            // 槽位里还有锁种 → 根据锁种类型切形态
            sonicarrow.Mode newMode =
                    lastInput.getItem() == com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.LEMON_ENERGY.get()
                            ? sonicarrow.Mode.LEMON
                            : sonicarrow.Mode.MELON;
            ((sonicarrow) bow.getItem()).switchMode(bow, newMode);
        }
    }

    private sonicarrow.Mode decideMode() {
        if (lastInput.getItem() == com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.LEMON_ENERGY.get()) return sonicarrow.Mode.LEMON;
        if (lastInput.getItem() == ModItems.MELON.get()) return sonicarrow.Mode.MELON;
        return sonicarrow.Mode.DEFAULT;
    }

    // 同步槽位物品到 NBT 数据
    public void  syncInventoryToNBT(){
        ItemStack offhandStack = entity.getOffhandItem();
        if (offhandStack.getItem() == ModItems.SONICARROW.get()) {
            CompoundTag tag = offhandStack.getOrCreateTag();
            CompoundTag inventoryTag = this.internal.serializeNBT();
            tag.put("Inventory", inventoryTag); // 序列化 NBT
            offhandStack.setTag(tag);
        }
    }

    @Override
    public Map<Integer, Slot> get() {
        return customSlots;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        syncInventoryToNBT();
    }
}
