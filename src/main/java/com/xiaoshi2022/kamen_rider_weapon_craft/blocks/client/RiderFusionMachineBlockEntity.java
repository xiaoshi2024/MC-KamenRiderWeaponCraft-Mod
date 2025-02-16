package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client;

import com.xiaoshi2022.kamen_rider_weapon_craft.network.SyncAnimationStatePacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.SyncGuiOpenStatePacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModRecipes;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.NetworkHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.SyncRecipeDataPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.RiderFusionMachineContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

public class RiderFusionMachineBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot < 4 && isCrafting) {

                checkRecipeMatch();
            }
            // 检查输出槽位（槽位4）是否被取出
            if (slot == 4 && isCraftingComplete && getStackInSlot(4).isEmpty()) {

                isCraftingComplete = false;
                shouldPlayEndAnimation = true;
                syncAnimationStateToClient(); // 同步动画状态到客户端
            }
        }
    };

    private void syncAnimationStateToClient() {
        if (level != null && !level.isClientSide) {
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)),
                    new SyncAnimationStatePacket(shouldPlayEndAnimation, worldPosition));
        }
    }

    public boolean isCrafting = false;
    public int craftingProgress = 0;
    private int fusionTime = 0;
    public int maxCraftingProgress = 0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public boolean isGuiOpen = false;          // GUI打开时触发
    private boolean isCraftingComplete = false; // 合成完成后保持rotate动画
    public boolean shouldPlayEndAnimation = false; // 输出槽清空时触发

    public RiderFusionMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RIDER_FUSION_MACHINE_BLOCK_ENTITY.get(), pos, state);

    }

    // ---------------------- GUI交互 ----------------------
    public void onGuiOpened() {

        isGuiOpen = true; // 标记需要播放GUI打开动画
        syncGuiOpenStateToClient(); // 同步GUI状态到客户端
    }

    private void syncGuiOpenStateToClient() {
        if (level != null && !level.isClientSide) {
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)),
                    new SyncGuiOpenStatePacket(isGuiOpen, worldPosition));
        }
    }

    // ---------------------- 合成逻辑 ----------------------
    public void startCrafting() {

        if (!isCrafting && canCraft()) {
            Optional<? extends Recipe<Container>> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.RIDER_FUSION_RECIPE.get(), getContainer(), level);

            if (recipe.isPresent() && recipe.get() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.recipe.RiderFusionRecipe fusionRecipe) {
                fusionTime = fusionRecipe.getFusionTime();
                maxCraftingProgress = fusionTime;
                isCrafting = true;
                craftingProgress = 0;
                setChanged();
                syncToClient();
            }
        }
    }

    public boolean canCraft() {
        if (level == null) {

            return false;
        }
        boolean canCraft = level.getRecipeManager().getRecipeFor(ModRecipes.RIDER_FUSION_RECIPE.get(), getContainer(), level).isPresent();

        return canCraft;
    }

    public Container getContainer() {
        SimpleContainer container = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            container.setItem(i, itemHandler.getStackInSlot(i));
        }
        return container;
    }

    // ---------------------- 物品槽获取 ----------------------
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    // ---------------------- 合成过程 ----------------------
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (isCrafting) {

            craftingProgress++;
            if (craftingProgress >= fusionTime) {

                completeCrafting();
                isCrafting = false;
                craftingProgress = 0;
            }
            setChanged();
            syncToClient();

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.players().forEach(player -> {
                    if (player.containerMenu instanceof RiderFusionMachineContainer container) {

                        container.setCraftingProgress(craftingProgress, maxCraftingProgress, isCrafting);
                    }
                });
            }
        }
    }

    public void completeCrafting() {

        if (level == null) {

            return;
        }

        Optional<? extends Recipe<Container>> recipe = level.getRecipeManager()
            .getRecipeFor(ModRecipes.RIDER_FUSION_RECIPE.get(), getContainer(), level);

        if (recipe.isPresent() && recipe.get() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.recipe.RiderFusionRecipe fusionRecipe) {
            ItemStack result = recipe.get().assemble(getContainer(), level.registryAccess());
            ItemStack currentOutput = itemHandler.getStackInSlot(4);


            if (currentOutput.isEmpty() ||
               (currentOutput.is(result.getItem()) &&
                currentOutput.getCount() + result.getCount() <= currentOutput.getMaxStackSize())) {

                // Deduct materials
                for (int i = 0; i < 4; i++) {
                    int extracted = itemHandler.extractItem(i, fusionRecipe.getRequiredCount(i), false).getCount();

                }

                // Attempt to insert
                ItemStack remaining = itemHandler.insertItem(4, result.copy(), false);
                if (!remaining.isEmpty()) {

                    isCrafting = false;
                    craftingProgress = 0;
                } else {
                    isCraftingComplete = true; // 标记合成完成，保持rotate动画
                }
            } else {

                isCrafting = false;
                craftingProgress = 0;
            }
        } else {

        }

        setChanged();
        syncToClient();
    }

    private void checkRecipeMatch() {

        if (!canCraft()) {

            isCrafting = false;
            craftingProgress = 0;
            setChanged();
            syncToClient();
        }
    }

    // ---------------------- 同步到客户端 ----------------------
    private void syncToClient() {
        if (level != null && !level.isClientSide) {

            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)),
                new SyncRecipeDataPacket(craftingProgress, maxCraftingProgress, isCrafting, worldPosition));
        }
    }

    public int getRedstoneSignal() {
        return isCrafting ? 3 : 0;
    }

    // ---------------------- 动画控制 ----------------------
    private PlayState predicate(AnimationState event) {
        // 优先级1: GUI打开动画
        if (isGuiOpen) {
            isGuiOpen = false; // 立即重置状态
            return event.setAndContinue(RawAnimation.begin().thenPlay("1").thenPlay("0"));
        }

        // 优先级2: 输出槽清空时的结束动画
        if (shouldPlayEndAnimation) {
            isCraftingComplete = false; // 立即重置状态
            shouldPlayEndAnimation = false; // 立即重置状态
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("end").thenPlay("0"));
        }

        // 优先级3: 合成完成后的持续旋转
        if (isCraftingComplete) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("rotate").thenPlayAndHold("end"));
        }

        // 优先级4: 合成中的旋转动画
        if (isCrafting) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("rotate"));
        }

        // 默认状态：空闲动画（由GeckoLib自动处理）
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("Inventory", itemHandler.serializeNBT());
        compound.putBoolean("isCrafting", isCrafting);
        compound.putInt("craftingProgress", craftingProgress);
        compound.putInt("fusionTime", fusionTime);
        compound.putInt("maxCraftingProgress", maxCraftingProgress);
        compound.putBoolean("isCraftingComplete", isCraftingComplete);
        compound.putBoolean("shouldPlayEndAnimation", shouldPlayEndAnimation);

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        itemHandler.deserializeNBT(compound.getCompound("Inventory"));
        isCrafting = compound.getBoolean("isCrafting");
        craftingProgress = compound.getInt("craftingProgress");
        fusionTime = compound.getInt("fusionTime");
        maxCraftingProgress = compound.getInt("maxCraftingProgress");
        isCraftingComplete = compound.getBoolean("isCraftingComplete");
        shouldPlayEndAnimation = compound.getBoolean("shouldPlayEndAnimation");

    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("isCrafting", isCrafting);
        tag.putInt("craftingProgress", craftingProgress);
        tag.putInt("maxCraftingProgress", maxCraftingProgress);
        tag.putBoolean("isCraftingComplete", isCraftingComplete);
        tag.putBoolean("shouldPlayEndAnimation", shouldPlayEndAnimation);

        return tag;
    }

    public void setCraftingProgress(int craftingProgress, int maxCraftingProgress, boolean isCrafting) {

        this.craftingProgress = craftingProgress;
        this.maxCraftingProgress = maxCraftingProgress;
        this.isCrafting = isCrafting;
        setChanged();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            isCrafting = tag.getBoolean("isCrafting");
            craftingProgress = tag.getInt("craftingProgress");
            maxCraftingProgress = tag.getInt("maxCraftingProgress");
            isCraftingComplete = tag.getBoolean("isCraftingComplete");
            shouldPlayEndAnimation = tag.getBoolean("shouldPlayEndAnimation");

        }
    }

    public int getCraftingProgress() {
        return craftingProgress;
    }

    public int getMaxCraftingProgress() {
        return maxCraftingProgress;
    }
}
