package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals.plant;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class HelheimVine extends VineBlock implements EntityBlock {
    // 定义方向属性
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    public HelheimVine(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 获取玩家放置方块时的位置
        BlockPos pos = context.getClickedPos();
        // 获取玩家的朝向
        Direction playerFacing = context.getHorizontalDirection();

        // 先尝试以玩家朝向为基础进行附着检查
        if (canAttachTo(context, playerFacing)) {
            return this.defaultBlockState().setValue(FACING, playerFacing.getOpposite());
        }

        // 如果玩家朝向不满足条件，检查其他水平方向
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (canAttachTo(context, dir)) {
                return this.defaultBlockState().setValue(FACING, dir.getOpposite());
            }
        }

        // 如果所有水平方向都不满足条件，检查侧方（左面和右面）
        Direction left = playerFacing.getCounterClockWise();
        Direction right = playerFacing.getClockWise();

        if (canAttachTo(context, left)) {
            return this.defaultBlockState().setValue(FACING, left.getOpposite());
        }

        if (canAttachTo(context, right)) {
            return this.defaultBlockState().setValue(FACING, right.getOpposite());
        }

        // 如果都不满足，默认设置藤蔓朝向北面
        return this.defaultBlockState().setValue(FACING, Direction.NORTH);
    }

    private boolean canAttachTo(BlockPlaceContext context, Direction facing) {
        BlockPos pos = context.getClickedPos().relative(facing);
        BlockState state = context.getLevel().getBlockState(pos);
        // 检查方块是否是树叶
        if (state.hasProperty(LeavesBlock.DISTANCE)) { // 检查方块是否有distance属性，这是树叶特有的
            return true;
        }
        return state.isFaceSturdy(context.getLevel(), pos, facing.getOpposite());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // 对于藤蔓，我们通常不需要完整的碰撞箱，因为它们是透明的
        // 但我们需要确保藤蔓能够正确地与玩家和其他实体交互
        // 这里我们返回一个非常薄的碰撞箱，以模拟藤蔓的占据空间
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // 视觉形状用于确定方块的渲染形状
        // 对于藤蔓，我们可以根据其附着的方向返回不同的视觉形状
        Direction facing = state.getValue(FACING);
        return switch (facing) {
            default -> Shapes.empty();
        };
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction.getAxis() == Direction.Axis.Y) {
            return state;
        } else {
            Direction facing = state.getValue(FACING);
            if (!canAttachTo(level, currentPos, facing)) {
                BlockState checkState = level.getBlockState(currentPos.relative(facing));
                // 检查方块是否为树叶
                if (checkState.getBlock() instanceof LeavesBlock) {
                    // 如果方块是树叶，允许附着
                    return state;
                } else {
                    // 如果方块不是树叶且不能附着，返回空气方块
                    return Blocks.AIR.defaultBlockState();
                }
            }
            // 如果可以附着，保持当前状态
            return state;
        }
    }

    private boolean canAttachTo(LevelAccessor level, BlockPos pos, Direction facing) {
        BlockPos checkPos = pos.relative(facing);
        BlockState checkState = level.getBlockState(checkPos);
        // 检查方块是否为树叶或足够坚固
        return checkState.getBlock() instanceof LeavesBlock || checkState.isFaceSturdy(level, checkPos, facing.getOpposite());
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return SoundType.VINE;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 20; // 藤蔓的火势蔓延速度通常为30
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        // 检查玩家是否持有剪刀来加速挖掘进度
        ItemStack itemStack = player.getMainHandItem();
        if (!itemStack.isEmpty() && itemStack.is(Items.SHEARS)) {
            // 如果玩家持有剪刀，则返回一个较小的值以增加挖掘速度
            return 1.0F;
        } else {
            // 如果玩家没有持有剪刀，则返回一个较大的值以减慢挖掘速度
            return 0.1F;
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        // 检查玩家是否持有剪刀
        if (!itemStack.isEmpty() && itemStack.is(Items.SHEARS)) {
            // 掉落藤蔓本身
            spawnAfterBreak(level, pos, state);
        }
        super.playerDestroy(level, player, pos, state, blockEntity, itemStack);
    }

    private void spawnAfterBreak(Level level, BlockPos pos, BlockState state) {
        // 获取方块的物品形式
        Item item = state.getBlock().asItem();
        if (item != Items.AIR) {
            // 掉落藤蔓本身
            Block.popResource(level, pos, new ItemStack(item, 1));
        }
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        // 继承原版藤曼的摧毁硬度
        level.levelEvent(2001, pos, Block.getId(state));
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Direction facing = state.getValue(FACING);
        Direction newFacing;
        switch (mirror) {
            case LEFT_RIGHT:
                switch (facing) {
                    case NORTH:
                        newFacing = Direction.SOUTH;
                        break;
                    case SOUTH:
                        newFacing = Direction.NORTH;
                        break;
                    case EAST:
                        newFacing = Direction.WEST;
                        break;
                    case WEST:
                        newFacing = Direction.EAST;
                        break;
                    default:
                        throw new IllegalArgumentException("Unable to mirror block with facing " + facing);
                }
                break;
            case FRONT_BACK:
                switch (facing) {
                    case NORTH:
                        newFacing = Direction.SOUTH;
                        break;
                    case SOUTH:
                        newFacing = Direction.NORTH;
                        break;
                    case EAST:
                        newFacing = Direction.WEST;
                        break;
                    case WEST:
                        newFacing = Direction.EAST;
                        break;
                    default:
                        throw new IllegalArgumentException("Unable to mirror block with facing " + facing);
                }
                break;
            default:
                return super.mirror(state, mirror);
        }
        return state.setValue(FACING, newFacing);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // 返回藤蔓对应的方块实体
        return ModBlockEntities.HELHEIM_VINE_ENTITY.get().create(pos, state);
    }
}