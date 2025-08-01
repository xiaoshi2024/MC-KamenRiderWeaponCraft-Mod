package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals.plant;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static net.minecraft.world.level.block.HugeMushroomBlock.DOWN;

public class HelheimVine extends VineBlock {

    public HelheimVine(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(UP, false)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(DOWN, true)); // 默认向下生长
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, NORTH, EAST, SOUTH, WEST, DOWN);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        BlockState aboveState = context.getLevel().getBlockState(context.getClickedPos().above());
        return aboveState.getBlock() instanceof HelheimVine || super.canBeReplaced(state, context);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.random.nextInt(4) == 0) {
            this.growDown(level, pos, state, random);
        }
    }

    private void growDown(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        BlockPos belowPos = pos.below();
        if (level.isEmptyBlock(belowPos)) {
            int maxLength = 5;
            int length = 0;

            // 检查已有藤蔓长度
            for (int i = 1; i < maxLength; i++) {
                if (level.getBlockState(pos.above(i)).getBlock() instanceof HelheimVine) {
                    length = i;
                } else {
                    break;
                }
            }

            if (length < maxLength) {
                BlockState newState = this.defaultBlockState();
                level.setBlockAndUpdate(belowPos, newState);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        Random random = new Random();
        if (random.nextDouble() < 0.3) {
            return Collections.singletonList(new ItemStack(ModItems.HELHEIMFRUIT.get()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        ItemStack itemStack = player.getMainHandItem();
        if (!itemStack.isEmpty() && itemStack.is(Items.SHEARS)) {
            return 1.0F;
        } else {
            return 0.1F;
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.is(Items.SHEARS)) {
            Block.popResource(level, pos, new ItemStack(this.asItem(), 1));
        }
        super.playerDestroy(level, player, pos, state, blockEntity, itemStack);
    }
}