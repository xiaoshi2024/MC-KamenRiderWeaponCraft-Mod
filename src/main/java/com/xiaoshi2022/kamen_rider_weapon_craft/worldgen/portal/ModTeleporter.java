package com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class ModTeleporter implements ITeleporter {
    private final BlockPos targetPos;
    private final boolean toHelheim;

    public ModTeleporter(BlockPos pos, boolean toHelheim) {
        this.targetPos = pos;
        this.toHelheim = toHelheim;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);

        BlockPos destinationPos = findSafePosition(destinationWorld, targetPos);

        entity.teleportTo(
                destinationPos.getX() + 0.5,
                destinationPos.getY(),
                destinationPos.getZ() + 0.5
        );

        return entity;
    }

    private BlockPos findSafePosition(ServerLevel world, BlockPos startPos) {
        if (toHelheim) {
            return findSafePositionFromY(world, startPos, 60);
        }
        return findSafePositionFromY(world, startPos, startPos.getY());
    }

    private BlockPos findSafePositionFromY(ServerLevel world, BlockPos pos, int startY) {
        startY = Math.min(Math.max(startY, world.getMinBuildHeight() + 1), world.getMaxBuildHeight() - 2);

        // 向上搜索
        for (int y = startY; y < world.getMaxBuildHeight() - 2; y++) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (isSafePosition(world, checkPos)) {
                return checkPos;
            }
        }

        // 向下搜索
        for (int y = startY; y > world.getMinBuildHeight() + 1; y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (isSafePosition(world, checkPos)) {
                return checkPos;
            }
        }

        return new BlockPos(pos.getX(), startY, pos.getZ());
    }

    private boolean isSafePosition(ServerLevel world, BlockPos pos) {
        BlockState floor = world.getBlockState(pos.below());
        BlockState feet = world.getBlockState(pos);
        BlockState head = world.getBlockState(pos.above());

        return !floor.isAir() &&
                (feet.isAir() || feet.canBeReplaced()) &&
                (head.isAir() || head.canBeReplaced());
    }
}