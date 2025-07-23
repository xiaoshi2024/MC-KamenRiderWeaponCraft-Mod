package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class WitherSpawnHandler {
    @SubscribeEvent
    public static void onWitherSpawn(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof WitherBoss)) return;
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        // 检查周围5格范围内是否有其他WitherBoss
        if (!level.getEntitiesOfClass(WitherBoss.class, event.getEntity().getBoundingBox().inflate(5)).isEmpty()) return;

        // 获取WitherBoss的中心位置
        BlockPos centerPos = event.getEntity().blockPosition();
        Random random = new Random();

        // 在5x5x5范围内随机选择一个位置生成TIMESAND
        boolean hasGenerated = false;

        // 随机选择一个位置
        int randomX = random.nextInt(5) - 2;
        int randomY = random.nextInt(5) - 2;
        int randomZ = random.nextInt(5) - 2;

        BlockPos targetPos = centerPos.offset(randomX, randomY, randomZ);

        // 检查目标位置的下方是否为固体方块（如地面）
        if (level.getBlockState(targetPos.below()).isSolidRender(level, targetPos.below())) {
            level.setBlock(targetPos, ModBlocks.TIMESAND.get().defaultBlockState(), 3);
            hasGenerated = true;
        }

        // 如果没有生成任何一个时间沙子，尝试在中心位置生成
        if (!hasGenerated) {
            BlockPos centerTargetPos = centerPos.offset(0, 0, 0);
            if (level.getBlockState(centerTargetPos.below()).isSolidRender(level, centerTargetPos.below())) {
                level.setBlock(centerTargetPos, ModBlocks.TIMESAND.get().defaultBlockState(), 3);
            }
        }
    }
}