package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.function.Function;

public class helheim_crack extends HorizontalDirectionalBlock implements EntityBlock {
    public static final IntegerProperty ANIMATION = IntegerProperty.create("animation", 0, 1);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<AttachFace> FACE = FaceAttachedHorizontalDirectionalBlock.FACE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public helheim_crack(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.WALL)
                .setValue(WATERLOGGED, false)
                .setValue(ANIMATION, 0));
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ANIMATION, FACING, WATERLOGGED, FACE);
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
                // 判断玩家当前所在的维度
                if (world.dimension() == Level.OVERWORLD) {
                    // 玩家在主世界，传送至自定义维度
                    ResourceKey<Level> customDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("kamen_rider_weapon_craft", "helheim"));
                    Level targetLevel = world.getServer().getLevel(customDimension);
                    if (targetLevel != null) {
                        serverPlayer.changeDimension((ServerLevel) targetLevel, new ITeleporter() {
                            @Override
                            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                                // 可以在这里设置玩家在自定义维度的传送位置
                                BlockPos targetPos = new BlockPos(0, 64, 0); // 示例位置
                                return repositionEntity.apply(true);
                            }
                        });
                    } else {
                        entity.sendSystemMessage(Component.literal("自定义维度加载失败！"));
                        return InteractionResult.FAIL;
                    }
                } else {
                    // 玩家在自定义维度，传送回主世界
                    Level overworld = world.getServer().getLevel(Level.OVERWORLD);
                    if (overworld != null) {
                        serverPlayer.changeDimension((ServerLevel) overworld, new ITeleporter() {
                            @Override
                            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                                // 可以在这里设置玩家在主世界的传送位置
                                BlockPos targetPos = new BlockPos(0, 64, 0); // 示例位置
                                return repositionEntity.apply(true);
                            }
                        });
                    } else {
                        entity.sendSystemMessage(Component.literal("主世界加载失败！"));
                        return InteractionResult.FAIL;
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void playSound(Level world, BlockPos pos) {
        // 播放音效给所有玩家
        world.playSound(null, pos, ModSounds.OPENDLOCK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}