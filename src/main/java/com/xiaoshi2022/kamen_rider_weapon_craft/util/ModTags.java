package com.xiaoshi2022.kamen_rider_weapon_craft.util;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        // 添加自定义树干到 minecraft:logs 标签
        public static final TagKey<Block> LOGS = BlockTags.create(new ResourceLocation("minecraft", "logs"));
        public static final TagKey<Block> LOGS_THAT_CAN_SUSTAIN_LEAVES = BlockTags.create(new ResourceLocation("minecraft", "logs_that_sustain_leaves"));

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, name));
        }
    }
}