package com.xiaoshi2022.kamen_rider_weapon_craft.util;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        // ✅ 正确：引用原版标签（如果你想要使用原版标签）
        public static final TagKey<Block> VANILLA_LOGS = BlockTags.LOGS;
        public static final TagKey<Block> VANILLA_LEAVES = BlockTags.LEAVES;

        // ✅ 正确：创建自定义标签（使用你的模组命名空间）
        public static final TagKey<Block> PINE_LOGS = createForgeTag("pine_logs");
        public static final TagKey<Block> PINE_LEAVES = createForgeTag("pine_leaves");
        public static final TagKey<Block> LOGS_THAT_CAN_SUSTAIN_PINE_LEAVES = createForgeTag("logs_that_can_sustain_pine_leaves");

        // 创建自定义标签的辅助方法
        private static TagKey<Block> createForgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }

        // 或者创建使用你的模组ID的标签
        private static TagKey<Block> createModTag(String name) {
            return BlockTags.create(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, name));
        }
    }
}