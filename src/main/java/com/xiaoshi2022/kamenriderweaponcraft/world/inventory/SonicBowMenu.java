package com.xiaoshi2022.kamenriderweaponcraft.world.inventory;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.SonicArrowItem;
import com.xiaoshi2022.kamenriderweaponcraft.compat.KRCCompat;
import com.xiaoshi2022.kamenriderweaponcraft.register.MenuRegister;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 音速弓锁种装载菜单
 * - 单个锁种槽位 (30,28)，只接受本 mod 的 6 个锁种
 * - 装载目标 = 玩家主手/副手中的音速弓（构造时扫描，不读 buf）
 * - 锁种存于弓的 CustomData("Lockseed")，取下后形态回到 DEFAULT
 */
public class SonicBowMenu extends AbstractContainerMenu {
    private static final String LOCKSEED_KEY = "Lockseed";

    public final Player player;
    private final ItemStack bowStack;
    private final SimpleContainer lockseedContainer;
    private ItemStack lastSynced = null;

    /** 工厂用构造（buf 不携带数据，直接扫玩家手上弓） */
    public SonicBowMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        super(MenuRegister.SONIC_BOW.get(), id);
        this.player = inv.player;
        this.bowStack = findSonicBow(inv.player);

        this.lockseedContainer = new SimpleContainer(1);

        // 从弓的 CustomData 读回已装载的锁种
        if (!bowStack.isEmpty()) {
            CustomData data = bowStack.get(DataComponents.CUSTOM_DATA);
            if (data != null && data.copyTag().contains(LOCKSEED_KEY)) {
                this.lockseedContainer.setItem(0,
                        ItemStack.parseOptional(player.registryAccess(), data.copyTag().getCompound(LOCKSEED_KEY)));
            }
        }

        // 锁种槽（软联动：接受假面骑士工艺的锁种）
        this.addSlot(new Slot(this.lockseedContainer, 0, 30, 28) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return KRCCompat.isLockseed(stack.getItem());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // 玩家背包
        for (int si = 0; si < 3; ++si) {
            for (int sj = 0; sj < 9; ++sj) {
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
            }
        }
        for (int si = 0; si < 9; ++si) {
            this.addSlot(new Slot(inv, si, 8 + si * 18, 142));
        }

        this.lastSynced = this.lockseedContainer.getItem(0).copy();
    }

    private static ItemStack findSonicBow(Player player) {
        if (player.getMainHandItem().getItem() instanceof SonicArrowItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof SonicArrowItem) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // 手持弓操作，容器始终有效
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        syncModeToBow(); // GUI 打开期间实时同步形态
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        syncModeToBow(); // 关闭 GUI 时最终同步
    }

    /** 服务端：把锁种写入弓的 CustomData 并切换形态 */
    private void syncModeToBow() {
        if (player.level().isClientSide || bowStack.isEmpty()) return;

        ItemStack current = lockseedContainer.getItem(0);
        if (lastSynced != null && ItemStack.isSameItemSameComponents(current, lastSynced)) return;
        lastSynced = current.copy();

        // 保存锁种到弓
        CompoundTag tag = bowStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (current.isEmpty()) {
            tag.remove(LOCKSEED_KEY);
        } else {
            tag.put(LOCKSEED_KEY, current.saveOptional(player.registryAccess()));
        }
        bowStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // 切换形态（空 = DEFAULT；软联动：按 KRC 锁种映射形态）
        if (bowStack.getItem() instanceof SonicArrowItem bow) {
            SonicArrowItem.Mode mode = KRCCompat.getModeFromItem(current.getItem());
            if (mode == null) mode = SonicArrowItem.Mode.DEFAULT;
            bow.switchMode(bowStack, mode);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
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
}
