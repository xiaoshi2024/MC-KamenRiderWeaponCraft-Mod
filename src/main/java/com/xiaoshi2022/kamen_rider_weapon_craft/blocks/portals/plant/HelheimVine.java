package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals.plant;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HelheimVine extends VineBlock implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // 定义方块的碰撞箱
    private static final VoxelShape SHAPE_N = Block.box(0, 1.7763568394002505e-15, 14, 15.999999999999995, 16, 15.99999999999999);
    private static final VoxelShape SHAPE_S = Block.box(5.329070518200751e-15, 1.7763568394002505e-15, 1.0658141036401503e-14, 16, 16, 2);
    private static final VoxelShape SHAPE_E = Block.box(1.0658141036401503e-14, 1.7763568394002505e-15, 0, 2, 16, 15.999999999999995);
    private static final VoxelShape SHAPE_W = Block.box(14, 1.7763568394002505e-15, 5.329070518200751e-15, 15.99999999999999, 16, 16);
    private static final VoxelShape SHAPE_UP = Block.box(0, 1.0658141036401503e-14, 1.7763568394002505e-15, 15.999999999999995, 2, 16);
    private static final VoxelShape SHAPE_DOWN = Block.box(0, 14, 0, 15.999999999999995, 15.99999999999999, 15.999999999999998);
    public HelheimVine(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // 随机数生成器
        Random random = new Random();
        // 假设掉落几率为 30%
        if (random.nextDouble() < 0.3) {
            return Collections.singletonList(new ItemStack(ModItems.HELHEIMFRUIT.get()));
        } else {
            return Collections.emptyList(); // 不掉落任何物品
        }
    }

    // 设置碰撞箱
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);  // 添加方向属性
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);  // 获取藤蔓的实际方向
        switch (facing) {
            case NORTH:
              return SHAPE_N;
            case SOUTH:
              return SHAPE_S;
            case EAST:
              return SHAPE_E;
            case WEST:
              return SHAPE_W;
            case UP:
              return SHAPE_UP;
            case DOWN:
              return SHAPE_DOWN;
            default:
                return Shapes.block();  // 默认情况下返回完整方块形状
        }
    }


    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 随机选择一个方向进行攀爬尝试
        Direction direction = Direction.getRandom(random);
        BlockPos targetPos = pos.relative(direction);

        // 检查目标位置是否为空
        if (level.isEmptyBlock(targetPos)) {
            // 检查是否有合适的支撑点
            if (canSupportAtFace(level, targetPos, direction.getOpposite())) {
                // 设置藤蔓生长到目标位置
                BlockState newState = this.defaultBlockState().setValue(FACING, direction);
                level.setBlockAndUpdate(targetPos, newState);
            }
        }
    }

    private boolean canSupportAtFace(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos supportPos = pos.relative(direction);
        BlockState supportState = level.getBlockState(supportPos);
        return supportState.isFaceSturdy(level, supportPos, direction.getOpposite());
    }

    // 放置方向
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        // 检查是否可以附着在点击的面上
        if (clickedFace.getAxis().isHorizontal() || clickedFace == Direction.UP || clickedFace == Direction.DOWN) {
            BlockPos neighborPos = pos.relative(clickedFace.getOpposite());
            BlockState neighborState = level.getBlockState(neighborPos);

            // 检查目标方块是否可以附着
            if (neighborState.isFaceSturdy(level, neighborPos, clickedFace) || neighborState.getBlock() instanceof LeavesBlock) {
                // 水平面：藤蔓方向为点击面的方向
                // 垂直面：藤蔓方向为点击面的方向
                return this.defaultBlockState().setValue(FACING, clickedFace);
            }
        }

        // 如果没有找到可以附着的方块，藤蔓消失
        return Blocks.AIR.defaultBlockState();
    }


    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);

        // 如果藤蔓的附着方向发生变化
        if (facing.getOpposite() == direction) {
            if (!neighborState.isFaceSturdy(level, neighborPos, facing)) {
                // 尝试找到新的附着方向
                for (Direction possibleDirection : Direction.values()) {
                    if (possibleDirection != facing) {
                        BlockPos testPos = currentPos.relative(possibleDirection.getOpposite());
                        if (level.getBlockState(testPos).isFaceSturdy(level, testPos, possibleDirection)) {
                            return state.setValue(FACING, possibleDirection);
                        }
                    }
                }
                // 如果找不到合适的附着方向，藤蔓消失
                return Blocks.AIR.defaultBlockState();
            }
        }

        // 如果当前方向仍然可以附着，保持原方向
        return state;
    }


    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        // 根据旋转方向调整藤蔓的方向
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        // 根据镜像方向调整藤蔓的方向
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // 返回藤蔓的碰撞箱
        return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_60550_) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getVisualShape(BlockState p_60479_, BlockGetter p_60480_, BlockPos p_60481_, CollisionContext p_60482_) {
        return super.getVisualShape(p_60479_, p_60480_, p_60481_, p_60482_);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        // 返回藤蔓对应的方块实体
        return ModBlockEntities.HELHEIM_VINE_ENTITY.get().create(p_153215_, p_153216_);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return SoundType.VINE;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 20; // 藤蔓的火势蔓延速度
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        // 检查玩家是否持有剪刀来加速挖掘进度
        ItemStack itemStack = player.getMainHandItem();
        if (!itemStack.isEmpty() && itemStack.is(Items.SHEARS)) {
            return 1.0F; // 使用剪刀时挖掘速度更快
        } else {
            return 0.1F; // 没有剪刀时挖掘速度较慢
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        // 检查玩家是否持有剪刀
        if (!itemStack.isEmpty() && itemStack.is(Items.SHEARS)) {
            // 掉落藤蔓本身
            Block.popResource(level, pos, new ItemStack(this.asItem(), 1));
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
}