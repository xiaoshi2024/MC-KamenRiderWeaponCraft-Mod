package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.helheim_crackBlockEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.dimension.ModDimensions;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.portal.ModTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;

public class helheim_crack extends Block implements EntityBlock {
    public static final IntegerProperty ANIMATION = IntegerProperty.create("animation", 0, 1);

    public static final DirectionProperty FACING = DirectionalBlock.FACING;


    public helheim_crack(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ANIMATION, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {

        return switch (state.getValue(FACING)) {
            default -> box(0, 0, 0, 16, 48, 16);
            case NORTH -> box(0, 0, 0, 16, 48, 16);
            case EAST -> box(0, 0, 0, 16, 48, 16);
            case WEST -> box(0, 0, 0, 16, 48, 16);
            case UP -> box(0, 0, 0, 16, 16, 48);
            case DOWN -> box(0, 0, -32, 16, 16, 16);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ANIMATION, FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.HELHEIM_CRACK_BLOCK_ENTITY.get().create(blockPos, blockState);
    }

    @Override
    public InteractionResult use(BlockState blockstate, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            // 触发动画状态改变
            int animationValue = 1; // 设置动画状态为 1
            BlockState newState = blockstate.setValue(ANIMATION, animationValue);
            world.setBlock(pos, newState, 3);

            // 触发方块状态同步
            world.getBlockEntity(pos).setChanged();

            // 播放音效（播放给所有玩家）
            playSound(world, pos);

            // 如果玩家是服务器玩家，执行传送逻辑
            if (entity instanceof ServerPlayer serverPlayer) {
                handleHelheimPortal(serverPlayer, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void handleHelheimPortal(ServerPlayer player, BlockPos pPos) {
        if (player.level() instanceof ServerLevel serverlevel) {
            MinecraftServer minecraftserver = serverlevel.getServer();
            ResourceKey<Level> resourcekey = player.level().dimension() == ModDimensions.HELHEIM_LEVEL_KEY ?
                    Level.OVERWORLD : ModDimensions.HELHEIM_LEVEL_KEY;

            ServerLevel portalDimension = minecraftserver.getLevel(resourcekey);
            if (portalDimension != null && !player.isPassenger()) {
                if(resourcekey == ModDimensions.HELHEIM_LEVEL_KEY) {
                    player.changeDimension(portalDimension, new ModTeleporter(pPos, true));
                } else {
                    player.changeDimension(portalDimension, new ModTeleporter(pPos, false));
                }
            }
        }
    }

    private void playSound(Level world, BlockPos pos) {
        // 播放音效给所有玩家
        world.playSound(null, pos, ModSounds.OPENDLOCK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (type == ModBlockEntities.HELHEIM_CRACK_BLOCK_ENTITY.get()) {
            return (lvl, pos, stt, be) -> helheim_crackBlockEntity.tick(lvl, pos, stt, (helheim_crackBlockEntity) be);
        }
        return null;
    }


    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        boolean shouldAnimate = false;

        // 1. 检测生物（需面向传送门）
        BlockPos frontPos = pos.relative(facing);
        AABB livingDetectionArea = new AABB(frontPos).inflate(1.0);

        for (LivingEntity livingEntity : level.getEntitiesOfClass(LivingEntity.class, livingDetectionArea)) {
            if (livingEntity.isPassenger() || livingEntity.isVehicle()) {
                continue;
            }

            if (isFacingPortal(livingEntity, pos, facing)) {
                handleEntityPortal(livingEntity, pos); // 传送生物
                livingEntity.hurtMarked = true;
                shouldAnimate = true;
                break;
            }
        }

        // 2. 检测掉落物（直接传送）
        AABB itemDetectionArea = new AABB(pos).inflate(0.5);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, itemDetectionArea)) {
            handleItemPortal(item, pos); // 使用专门处理物品的方法
            item.hurtMarked = true;
            shouldAnimate = true;
        }

        // 3. 处理动画
        if (shouldAnimate && state.getValue(ANIMATION) == 0) {
            level.setBlock(pos, state.setValue(ANIMATION, 1), 3);
            playSound(level, pos);
        } else if (!shouldAnimate && state.getValue(ANIMATION) != 0) {
            level.setBlock(pos, state.setValue(ANIMATION, 0), 3);
        }
    }

    // 专门处理物品传送的方法
    private void handleItemPortal(ItemEntity item, BlockPos pos) {
        if (item.level() instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            ResourceKey<Level> destinationKey = serverLevel.dimension() == ModDimensions.HELHEIM_LEVEL_KEY
                    ? Level.OVERWORLD
                    : ModDimensions.HELHEIM_LEVEL_KEY;

            ServerLevel destination = server.getLevel(destinationKey);
            if (destination != null) {
                // 创建新的物品实体到目标维度
                ItemEntity newItem = new ItemEntity(
                        destination,
                        pos.getX() + 0.5,
                        pos.getY(),
                        pos.getZ() + 0.5,
                        item.getItem()
                );
                destination.addFreshEntity(newItem);

                // 移除原物品
                item.discard();
            }
        }
    }

    // 保留原有的方向检测方法
    private boolean isFacingPortal(LivingEntity entity, BlockPos portalPos, Direction portalFacing) {
        Vec3 toPortal = new Vec3(
                portalFacing.getStepX(),
                portalFacing.getStepY(),
                portalFacing.getStepZ()
        ).normalize();
        return toPortal.dot(entity.getLookAngle()) > 0.1;
    }

    private void handleEntityPortal(LivingEntity entity, BlockPos pPos) {
        if (entity.level() instanceof ServerLevel serverlevel) {
            MinecraftServer minecraftserver = serverlevel.getServer();
            ResourceKey<Level> resourcekey = entity.level().dimension() == ModDimensions.HELHEIM_LEVEL_KEY ?
                    Level.OVERWORLD : ModDimensions.HELHEIM_LEVEL_KEY;

            ServerLevel portalDimension = minecraftserver.getLevel(resourcekey);
            if (portalDimension != null && !entity.isPassenger()) {
                if(resourcekey == ModDimensions.HELHEIM_LEVEL_KEY) {
                    entity.changeDimension(portalDimension, new ModTeleporter(pPos, true));
                } else {
                    entity.changeDimension(portalDimension, new ModTeleporter(pPos, false));
                }
            }
        }
    }
}