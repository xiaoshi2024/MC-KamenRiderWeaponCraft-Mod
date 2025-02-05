package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals.RiderFusionMachineBlock;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModRecipes;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.RiderFusionMachineContainer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.GeoBlockEntity;

import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

import java.util.stream.IntStream;

public class RiderFusionMachineBlockEntity extends RandomizableContainerBlockEntity implements GeoBlockEntity, WorldlyContainer {
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) { // 总共有4个槽位，3个输入槽位，1个输出槽位
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int craftingProgress = 0; // 合成进度
    private int maxCraftingProgress = 60; // 最大合成进度（3秒）
    private boolean isProgressing = false;

    private RiderFusionMachineContainer container;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(9, ItemStack.EMPTY);
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());

    public RiderFusionMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RIDER_FUSION_MACHINE_BLOCK_ENTITY.get(), pos, state);
    }

    public RiderFusionMachineContainer getContainer() {
        return container;
    }


    private PlayState predicate(AnimationState event) {
        String animationprocedure = ("" + this.getBlockState().getValue(RiderFusionMachineBlock.ANIMATION));
        if (animationprocedure.equals("0")) {
            return event.setAndContinue(RawAnimation.begin().thenLoop(animationprocedure));
        }
        return PlayState.STOP;
    }

    String prevAnim = "0";

    private PlayState procedurePredicate(AnimationState event) {
        String animationprocedure = ("" + this.getBlockState().getValue(RiderFusionMachineBlock.ANIMATION));
        if (!animationprocedure.equals("0") && event.getController().getAnimationState() == AnimationController.State.STOPPED || (!animationprocedure.equals(prevAnim) && !animationprocedure.equals("0"))) {
            if (!animationprocedure.equals(prevAnim))
                event.getController().forceAnimationReset();
            event.getController().setAnimation(RawAnimation.begin().thenPlay(animationprocedure));
            if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
                if (this.getBlockState().getBlock().getStateDefinition().getProperty("animation") instanceof IntegerProperty _integerProp)
                    level.setBlock(this.getBlockPos(), this.getBlockState().setValue(_integerProp, 0), 3);
                event.getController().forceAnimationReset();
            }
        } else if (animationprocedure.equals("0")) {
            prevAnim = "0";
            return PlayState.STOP;
        }
        prevAnim = animationprocedure;
        return PlayState.CONTINUE;
    }

    // 假设这里有一个方法用于处理输出骑士电路板的逻辑
    public void outputRiderCircuitBoard() {
        ItemStack circuitBoardStack = new ItemStack(ModItems.RIDER_CIRCUIT_BOARD.get());
        if (itemHandler.getStackInSlot(3).isEmpty()) { // 假设槽位3是输出槽位
            itemHandler.insertItem(3, circuitBoardStack, false);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<RiderFusionMachineBlockEntity>(this, "controller", 0, this::predicate));
        data.add(new AnimationController<RiderFusionMachineBlockEntity>(this, "procedurecontroller", 0, this::procedurePredicate));
    }

    // 添加服务端启动合成的方法
    public void startCrafting() {
        if (!isProgressing && hasValidRecipe()) {
            isProgressing = true;
            craftingProgress = 0;
            setChanged(); // 标记数据变化
        }
    }

    // 修改 serverTick 方法
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (isProgressing && hasEnergy() && hasValidRecipe()) {
            craftingProgress++;
            setChanged(); // 同步数据
            if (craftingProgress >= maxCraftingProgress) {
                completeCrafting();
                isProgressing = false;
                craftingProgress = 0;
            }
        } else {
            isProgressing = false;
            craftingProgress = 0;
        }
    }

    // 在方块实体中绑定容器
    public void setContainer(RiderFusionMachineContainer container) {
        this.container = container;
        // 初始化进度同步（关键修复）
        container.craftingProgress = this.craftingProgress;
        container.maxCraftingProgress = this.maxCraftingProgress;
    }

    public void completeCrafting() {
        for (Recipe<Container> recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.RIDER_FUSION_RECIPE.get())) {
            Container container = new SimpleContainer(itemHandler.getSlots());
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                container.setItem(i, itemHandler.getStackInSlot(i));
            }
            if (recipe.matches(container, level)) {
                ItemStack result = recipe.assemble(container, level.registryAccess());
                if (!result.isEmpty()) {
                    itemHandler.insertItem(3, result, false);
                    itemHandler.extractItem(0, itemHandler.getStackInSlot(0).getCount(), false);
                    itemHandler.extractItem(1, itemHandler.getStackInSlot(1).getCount(), false);
                    itemHandler.extractItem(2, itemHandler.getStackInSlot(2).getCount(), false);
                }
            }
        }
        craftingProgress = 0;
        setChanged();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public int getCraftingProgress() {
        return craftingProgress;
    }

    public void setCraftingProgress(int progress) {
        this.craftingProgress = progress;
        setChanged();
    }

    public int getMaxCraftingProgress() {
        return maxCraftingProgress;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (!this.tryLoadLootTable(compound))
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound, this.stacks);
        if (compound.get("energyStorage") instanceof IntTag intTag)
            energyStorage.deserializeNBT(intTag);
        if (compound.get("fluidTank") instanceof CompoundTag compoundTag)
            fluidTank.readFromNBT(compoundTag);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (!this.trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, this.stacks);
        }
        compound.put("energyStorage", energyStorage.serializeNBT());
        compound.put("fluidTank", fluidTank.writeToNBT(new CompoundTag()));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }

    @Override
    public Component getDefaultName() {
        return Component.literal("rider_fusion_machine");
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (index == 3) { // 输出槽位
            // 清空输入槽位
            for (int i = 0; i < 3; i++) {
                itemHandler.extractItem(i, itemHandler.getStackInSlot(i).getCount(), false);
            }
        }
        return true;
    }

    private final EnergyStorage energyStorage = new EnergyStorage(400000, 200, 200, 0) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int retval = super.receiveEnergy(maxReceive, simulate);
            if (!simulate) {
                setChanged();
                level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 2);
            }
            return retval;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int retval = super.extractEnergy(maxExtract, simulate);
            if (!simulate) {
                setChanged();
                level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 2);
            }
            return retval;
        }
    };
    private final FluidTank fluidTank = new FluidTank(8000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
            level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 2);
        }
    };

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == ForgeCapabilities.ITEM_HANDLER)
            return handlers[facing.ordinal()].cast();
        if (!this.remove && capability == ForgeCapabilities.ENERGY)
            return LazyOptional.of(() -> energyStorage).cast();
        if (!this.remove && capability == ForgeCapabilities.FLUID_HANDLER)
            return LazyOptional.of(() -> fluidTank).cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
    }
}