package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
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

    // 优化tick机制，使用更可靠的方式确保方块持续更新效果
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        // 创建一个以方块为中心，半径为4格的AABB区域
        AABB area = new AABB(
                pos.getX() - 4, pos.getY() - 4, pos.getZ() - 4,
                pos.getX() + 5, pos.getY() + 5, pos.getZ() + 5
        );

        // 获取区域内的所有玩家
        for (Player player : level.getEntitiesOfClass(Player.class, area)) {
            // 为玩家添加速度和力量效果（鼓舞效果）
            // 检查速度效果：如果玩家没有该效果，或者效果剩余时间少于10秒（200tick），则添加
            if (player.getEffect(MobEffects.MOVEMENT_SPEED) == null || 
                player.getEffect(MobEffects.MOVEMENT_SPEED).getDuration() < 200) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        220, // 持续时间11秒
                        0,  // 等级1
                        false, false, true
                ));
            }

            // 检查力量效果：如果玩家没有该效果，或者效果剩余时间少于10秒（200tick），则添加
            if (player.getEffect(MobEffects.DAMAGE_BOOST) == null || 
                player.getEffect(MobEffects.DAMAGE_BOOST).getDuration() < 200) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST,
                        220,
                        0,
                        false, false, true
                ));
            }
        }

        // 无论如何都重新调度下一次tick，确保效果持续生效
        level.scheduleTick(pos, this, 20); // 每20tick(1秒)执行一次
    }

    // 当方块被移除时的清理
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        // 清理任何可能的任务调度
        if (!newState.is(this)) {
            // 方块被移除，确保不再调度tick
        }
    }
    
    // 当方块被放置时启动tick循环（这是关键修复）
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        
        // 只在服务器端调度tick（客户端不需要）
        if (!level.isClientSide) {
            // 延迟1tick开始第一个tick，确保方块完全放置
            level.scheduleTick(pos, this, 1);
        }
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
    
    // 移除不必要的randomTick机制，我们使用onPlace和scheduleTick来确保稳定的tick循环
    // 这样更加可控，不会受到随机tick的影响
    
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