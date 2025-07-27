package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.food;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HelheimJellyBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16); // 略矮一格

    public HelheimJellyBlock(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
                                        BlockPos pos, CollisionContext ctx) {
        return SHAPE; // 允许站在上面
    }

    @Override
    public void entityInside(BlockState state, net.minecraft.world.level.Level level,
                             BlockPos pos, Entity entity) {
        // 减速 + 轻微弹跳
        entity.makeStuckInBlock(state, new Vec3(0.9, 1.5, 0.9));
    }
}