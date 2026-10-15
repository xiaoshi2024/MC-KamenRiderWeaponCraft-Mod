package com.xiaoshi2022.kamenriderweaponcraft.Item.client.sonicarrow;

import net.minecraft.world.phys.Vec3;

/**
 * 音速弓枪口（箭尖骨骼）位置追踪器（参照尸兄 TianGangBeamState 的思路）
 * SonicArrowRenderer 每帧渲染本玩家手持音速弓时，提交 arrow 骨骼的世界坐标；
 * 雷电弧包处理时若提交位置仍新鲜，则用骨骼位置替代包起点，
 * 使激光从弓的箭尖骨骼射出而非玩家胸口
 */
public final class SonicArrowMuzzleTracker {
    /** 提交位置的有效期（毫秒），覆盖松弓后雷电弧包到达的窗口 */
    private static final long FRESH_MS = 300;

    private static Vec3 origin;
    private static long timestamp;

    private SonicArrowMuzzleTracker() {}

    /** 渲染线程提交当前箭尖骨骼的世界坐标 */
    public static void submit(Vec3 pos) {
        origin = pos;
        timestamp = System.currentTimeMillis();
    }

    /** 取新鲜（300ms 内）的骨骼位置；过期返回 null */
    public static Vec3 getFresh() {
        if (origin == null) return null;
        if (System.currentTimeMillis() - timestamp > FRESH_MS) return null;
        return origin;
    }
}
