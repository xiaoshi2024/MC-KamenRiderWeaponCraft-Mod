package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;

public class RiderforgingalloymineralBlock extends Block {
    public RiderforgingalloymineralBlock() {
        super(BlockBehaviour.Properties.of().sound(SoundType.COPPER).strength(4.5f, 10f));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }
}

