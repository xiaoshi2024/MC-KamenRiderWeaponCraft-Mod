package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.entity.lockseedIronBarsEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class lockseedIronBars extends BaseEntityBlock {
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    // 定义一个 VoxelShape 字段来存储最终的形状
    private static final VoxelShape SHAPE = Shapes.or(
            // 底部框架
            Block.box(0, 0, 0, 16, 16, 2),
            Block.box(0, 0, 14, 16, 16, 16),

            // 顶部框架
            Block.box(0, 14, 0, 16, 16, 2),
            Block.box(0, 14, 14, 16, 16, 16),

            // 中间竖条
            Block.box(0, 0, 2, 2, 16, 14),
            Block.box(14, 0, 2, 16, 16, 14),

            // 中间横条
            Block.box(2, 0, 0, 14, 2, 16),
            Block.box(2, 14, 0, 14, 16, 16)
    );

    public lockseedIronBars(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState p_49728_, BlockGetter p_49729_, BlockPos p_49730_, CollisionContext p_49731_) {
        return SHAPE;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // 自定义掉落逻辑
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty()) {
            return dropsOriginal;
        }
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof lockseedIronBarsEntity) {
            lockseedIronBarsEntity entity = (lockseedIronBarsEntity) blockEntity;
            if (!level.isClientSide) {
                if (player.isShiftKeyDown()) {
                    // Shift + 右键移除最后一个物品
                    ItemStack removedItem = entity.removeItem();
                    if (!removedItem.isEmpty()) {
                        player.addItem(removedItem); // 将移除的物品返回给玩家
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    // 普通右键添加物品
                    if (entity.addItem(player.getMainHandItem())) {
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.CONSUME;
    }


    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof lockseedIronBarsEntity) {
            lockseedIronBarsEntity entity = (lockseedIronBarsEntity) blockEntity;
            entity.dropAllItems(level, pos); // 破坏方块时掉落所有物品
        }
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new lockseedIronBarsEntity(pos, state);
    }
}