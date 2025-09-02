package com.xiaoshi2022.kamen_rider_weapon_craft.world.dimension;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DimensionSafeSpawnHandler {

    private static final ResourceLocation DESERT_OF_TIME = new ResourceLocation("kamen_rider_weapon_craft:the_desertof_time");

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        ResourceLocation dimension = event.getDimension().location();

        if (dimension.equals(DESERT_OF_TIME) && entity.level().isClientSide) {
            // 确保玩家生成在安全位置
            Level level = entity.level();
            BlockPos spawnPos = findSafeSpawnPosition(level, entity.blockPosition());

            if (!spawnPos.equals(entity.blockPosition())) {
                entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
            }
        }
    }

    private static BlockPos findSafeSpawnPosition(Level level, BlockPos startPos) {
        // 在Y=70层寻找安全的生成点（沙漠表面）
        int surfaceY = 70;
        BlockPos surfacePos = new BlockPos(startPos.getX(), surfaceY, startPos.getZ());

        // 检查当前位置是否安全
        if (isSafeSpawnPosition(level, surfacePos)) {
            return surfacePos;
        }

        // 如果当前位置不安全，在周围寻找安全位置
        for (int radius = 1; radius <= 10; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) == radius || Math.abs(z) == radius) {
                        BlockPos checkPos = surfacePos.offset(x, 0, z);
                        if (isSafeSpawnPosition(level, checkPos)) {
                            return checkPos;
                        }
                    }
                }
            }
        }

        // 如果找不到安全位置，强制在Y=65生成（高于地面）
        return new BlockPos(startPos.getX(), surfaceY + 1, startPos.getZ());
    }

    private static boolean isSafeSpawnPosition(Level level, BlockPos pos) {
        // 检查脚下方块是否坚固
        BlockState feetBlock = level.getBlockState(pos);
        BlockState headBlock = level.getBlockState(pos.above());
        BlockState groundBlock = level.getBlockState(pos.below());

        // 安全条件：脚下是固体，头部是空气，站立位置是空气
        return groundBlock.isSolid() &&
                headBlock.isAir() &&
                feetBlock.isAir();
    }
}