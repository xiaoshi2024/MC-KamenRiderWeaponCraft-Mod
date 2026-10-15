package com.xiaoshi2022.kamenriderweaponcraft.client.render.thunder;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 电弧构建器 - 生成直线或轻微弯曲的电光
 */
public class LightningArcBuilder {
    private static final Random RANDOM = new Random();

    /**
     * 生成直线电光（从起点到终点，带轻微分支效果）
     */
    public static List<Vec3[]> generateStraightLightning(Vec3 start, Vec3 end, int seed) {
        List<Vec3[]> segments = new ArrayList<>();
        RANDOM.setSeed(seed);

        Vec3 direction = end.subtract(start);
        double totalLength = direction.length();
        if (totalLength < 0.1) {
            segments.add(new Vec3[]{start, end});
            return segments;
        }

        Vec3 dir = direction.normalize();

        // 分段数量（每0.5格一段）
        int segmentCount = Math.max(1, (int) (totalLength / 0.5));

        // 生成主线段
        for (int i = 0; i < segmentCount; i++) {
            double t1 = (double) i / segmentCount;
            double t2 = (double) (i + 1) / segmentCount;

            Vec3 p1 = start.add(dir.scale(t1 * totalLength));
            Vec3 p2 = start.add(dir.scale(t2 * totalLength));

            // 轻微随机偏移（让电光有自然感，但不是弯曲）
            if (i > 0 && i < segmentCount - 1) {
                double offset = RANDOM.nextDouble() * 0.1;
                Vec3 perp = getRandomPerpendicular(dir);
                p1 = p1.add(perp.scale(offset));
            }

            segments.add(new Vec3[]{p1, p2});
        }

        // 添加分支（从中间分出小分支）
        int branchCount = Math.min(3, segmentCount / 3);
        for (int b = 0; b < branchCount; b++) {
            int idx = segmentCount / 2 + (b - branchCount / 2) * 2;
            if (idx > 0 && idx < segmentCount) {
                double t = (double) idx / segmentCount;
                Vec3 midPos = start.add(dir.scale(t * totalLength));

                double angle = RANDOM.nextDouble() * Math.PI * 2;
                double branchLen = 0.3 + RANDOM.nextDouble() * 0.5;
                Vec3 branchDir = new Vec3(
                        Math.cos(angle),
                        Math.sin(angle),
                        0
                ).normalize();
                Vec3 branchEnd = midPos.add(branchDir.scale(branchLen));

                segments.add(new Vec3[]{midPos, branchEnd});
            }
        }

        return segments;
    }

    /**
     * 获取随机垂直向量
     */
    private static Vec3 getRandomPerpendicular(Vec3 dir) {
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 perp = dir.cross(up);
        if (perp.length() < 0.1) {
            perp = dir.cross(new Vec3(1, 0, 0));
        }
        return perp.normalize();
    }
}
