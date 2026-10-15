package com.xiaoshi2022.kamenriderweaponcraft.client.render.thunder;

import net.minecraft.world.phys.Vec3;

/**
 * 雷电线段数据结构
 */
public class ThunderSegment {
    public Vec3 startPos;
    public Vec3 endPos;
    public float radius;
    public int color;
    public float alpha;
    public float age;

    public ThunderSegment(Vec3 startPos, Vec3 endPos, float radius, int color, float alpha, float age) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.radius = radius;
        this.color = color;
        this.alpha = alpha;
        this.age = age;
    }
}
