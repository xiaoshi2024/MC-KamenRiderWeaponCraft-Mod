package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KachidokiBataBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    
    // 定义旗帜的碰撞箱形状
    private static final VoxelShape POLE_SHAPE = Block.box(6, 0, 6, 10, 16, 10);
    private static final VoxelShape BANNER_SHAPE_NORTH = Block.box(0, 0, 4, 16, 16, 6);
    private static final VoxelShape BANNER_SHAPE_SOUTH = Block.box(0, 0, 10, 16, 16, 12);
    private static final VoxelShape BANNER_SHAPE_EAST = Block.box(10, 0, 0, 12, 16, 16);
    private static final VoxelShape BANNER_SHAPE_WEST = Block.box(4, 0, 0, 6, 16, 16);
    
    private static final VoxelShape SHAPE_NORTH = Shapes.or(POLE_SHAPE, BANNER_SHAPE_NORTH);
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(POLE_SHAPE, BANNER_SHAPE_SOUTH);
    private static final VoxelShape SHAPE_EAST = Shapes.or(POLE_SHAPE, BANNER_SHAPE_EAST);
    private static final VoxelShape SHAPE_WEST = Shapes.or(POLE_SHAPE, BANNER_SHAPE_WEST);
    
    public KachidokiBataBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        switch (direction) {
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            default:
                return SHAPE_NORTH;
        }
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 确保方块只能放置在地面上
        BlockPos belowPos = context.getClickedPos().below();
        Level level = context.getLevel();
        if (!level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP)) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    

    
    // 方块实体相关方法
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KachidokiBataBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 对于GeoBlockEntity，客户端不需要额外的ticker，它会自动处理渲染更新
        return null;
    }
    
    // 方块实体类
    public static class KachidokiBataBlockEntity extends BlockEntity implements GeoBlockEntity {
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
        private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
        
        public KachidokiBataBlockEntity(BlockPos pos, BlockState state) {
            super(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities.KACHIDOKI_BATA_BLOCK_ENTITY.get(), pos, state);
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(new AnimationController<>(this, "controller", 20, state -> {
                return state.setAndContinue(IDLE_ANIM);
            }));
        }
        
        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }
        
        @Override
        public double getTick(Object blockEntity) {
            return level.isClientSide() ? 0 : 0;
        }
    }
}