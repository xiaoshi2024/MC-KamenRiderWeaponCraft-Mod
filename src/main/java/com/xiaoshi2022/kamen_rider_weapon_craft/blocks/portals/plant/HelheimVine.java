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
    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    public HelheimVine(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    //设置碰撞箱
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);  // 添加方向属性
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);  // 获取方向
        switch (facing) {
            case NORTH:
                return Block.box(0, 0, 0, 16, 16, 16);  // 定义形状
            case SOUTH:
                return Block.box(0, 0, 0, 16, 16, 16);  // 旋转形状
            case EAST:
                return Block.box(0, 0, 0, 16, 16, 16);
            case WEST:
                return Block.box(0, 0, 0, 16, 16, 16);
            default:
                return Shapes.block();
        }
    }

    //放置方向
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction.getAxis().isHorizontal() && neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite())) {
            // 如果相邻方块可以附着
            if (direction == Direction.EAST) {
                // 如果是东面，设置藤蔓的形状或偏移
                return state.setValue(FACING, Direction.WEST); // 假设藤蔓朝西生长
            } else {
                return state.setValue(FACING, direction.getOpposite());
            }
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
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
}