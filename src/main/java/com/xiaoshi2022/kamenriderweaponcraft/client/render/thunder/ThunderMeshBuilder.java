package com.xiaoshi2022.kamenriderweaponcraft.client.render.thunder;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * 雷电渲染构建器
 */
public class ThunderMeshBuilder {

    // 渲染类型（三角形面片）
    public static final RenderType THUNDER_RENDER_TYPE = RenderType.create(
            "krwc_thunder",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderType.COLOR_WRITE)
                    .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderType.NO_CULL)
                    .createCompositeState(false)
    );

    /**
     * 渲染雷电
     */
    public static void renderThunder(PoseStack poseStack, MultiBufferSource bufferSource,
                                     List<ThunderSegment> segments) {
        if (segments.isEmpty()) return;

        VertexConsumer vertexConsumer = bufferSource.getBuffer(THUNDER_RENDER_TYPE);
        Matrix4f matrix = poseStack.last().pose();

        for (ThunderSegment segment : segments) {
            renderSegment(vertexConsumer, matrix, segment);
        }
    }

    /**
     * 渲染单个线段
     */
    private static void renderSegment(VertexConsumer vertexConsumer, Matrix4f matrix,
                                      ThunderSegment segment) {
        Vec3 start = segment.startPos;
        Vec3 end = segment.endPos;

        int color = segment.color;
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float alpha = Math.max(0.15f, segment.alpha * (1 - segment.age / ThunderConfig.THUNDER_LIFETIME));
        a = (int) (a * alpha);

        Vec3 direction = end.subtract(start);
        Vec3 perp = getPerpendicularVector(direction);

        float width = segment.radius * 2.0f;

        Vector3f startVec = new Vector3f((float) start.x, (float) start.y, (float) start.z);
        Vector3f endVec = new Vector3f((float) end.x, (float) end.y, (float) end.z);
        Vector3f perpVec = new Vector3f((float) perp.x, (float) perp.y, (float) perp.z);

        // 四个角点
        Vector3f p1 = new Vector3f(startVec).sub(perpVec.mul(width / 2, new Vector3f()));
        Vector3f p2 = new Vector3f(startVec).add(perpVec.mul(width / 2, new Vector3f()));
        Vector3f p3 = new Vector3f(endVec).add(perpVec.mul(width / 2, new Vector3f()));
        Vector3f p4 = new Vector3f(endVec).sub(perpVec.mul(width / 2, new Vector3f()));

        // 应用矩阵变换
        Vector3f tp1 = new Vector3f(p1).mulPosition(matrix);
        Vector3f tp2 = new Vector3f(p2).mulPosition(matrix);
        Vector3f tp3 = new Vector3f(p3).mulPosition(matrix);
        Vector3f tp4 = new Vector3f(p4).mulPosition(matrix);

        // 三角形1: p1-p2-p3
        vertexConsumer.addVertex(tp1.x(), tp1.y(), tp1.z()).setColor(r, g, b, a);
        vertexConsumer.addVertex(tp2.x(), tp2.y(), tp2.z()).setColor(r, g, b, a);
        vertexConsumer.addVertex(tp3.x(), tp3.y(), tp3.z()).setColor(r, g, b, a);

        // 三角形2: p1-p3-p4
        vertexConsumer.addVertex(tp1.x(), tp1.y(), tp1.z()).setColor(r, g, b, a);
        vertexConsumer.addVertex(tp3.x(), tp3.y(), tp3.z()).setColor(r, g, b, a);
        vertexConsumer.addVertex(tp4.x(), tp4.y(), tp4.z()).setColor(r, g, b, a);
    }

    private static Vec3 getPerpendicularVector(Vec3 dir) {
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 perp = dir.cross(up);
        if (perp.length() < 0.1) {
            perp = dir.cross(new Vec3(1, 0, 0));
        }
        return perp.normalize();
    }
}
