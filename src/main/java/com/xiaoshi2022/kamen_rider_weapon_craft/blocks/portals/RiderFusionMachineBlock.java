package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachineBlockEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.procedures.RiderFusionMachineDangYouJiFangKuaiShiProcedure;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Collections;

public class RiderFusionMachineBlock extends BaseEntityBlock implements EntityBlock {
    public static final IntegerProperty ANIMATION = IntegerProperty.create("animation", 0, 4);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public RiderFusionMachineBlock() {
        super(BlockBehaviour.Properties.of()
                .instrument(NoteBlockInstrument.BASEDRUM)
                .sound(SoundType.METAL)
                .strength(5.3f, 10f)
                .lightLevel(s -> 2)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .pushReaction(PushReaction.PUSH_ONLY)
                .isRedstoneConductor((bs, br, bp) -> false));

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ANIMATION, 0));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1); // Start tick
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (level.getBlockEntity(pos) instanceof RiderFusionMachineBlockEntity be) {
            be.serverTick(level, pos, state);
            // 重新调度下一次tick
            level.scheduleTick(pos, this, 1);
        } else {
            // 处理方块实体为空的情况
            System.err.println("Block entity at position " + pos + " is not a RiderFusionMachineBlockEntity");
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true; // Allow random tick
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.RIDER_FUSION_MACHINE_BLOCK_ENTITY.get().create(blockPos, blockState);
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
        return new float[]{0.8f, 0f, 0.6f};
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 1;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> box(0, 0, 0, 16, 30.8, 16);
            case NORTH -> box(0, 0, 0, 16, 30.8, 16);
            case EAST -> box(0, 0, 0, 16, 30.8, 16);
            case WEST -> box(0, 0, 0, 16, 30.8, 16);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ANIMATION, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof RiderFusionMachineBlockEntity be) {
            int signal = be.getRedstoneSignal();
            return signal;
        }
        return 0;
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction direction, IPlantable plantable) {
        return true;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty()) {
            return dropsOriginal;
        }
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public InteractionResult use(BlockState blockstate, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult hit) {
        super.use(blockstate, world, pos, entity, hand, hit);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        double hitX = hit.getLocation().x;
        double hitY = hit.getLocation().y;
        double hitZ = hit.getLocation().z;
        Direction direction = hit.getDirection();

        RiderFusionMachineDangYouJiFangKuaiShiProcedure.execute(world, x, y, z, entity);
        return InteractionResult.SUCCESS;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, world, pos, eventID, eventParam);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(eventID, eventParam);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof RiderFusionMachineBlockEntity be) {
                // 直接访问 ItemStackHandler
                ItemStackHandler handler = (ItemStackHandler) be.getItemHandler();
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }
}