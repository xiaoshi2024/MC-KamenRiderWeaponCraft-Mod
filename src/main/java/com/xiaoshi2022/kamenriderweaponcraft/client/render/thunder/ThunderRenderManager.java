package com.xiaoshi2022.kamenriderweaponcraft.client.render.thunder;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 雷电渲染管理器（单例）：管理所有待渲染的雷电效果
 * 移植自 rick_mod 变形枪的雷电系统
 */
public class ThunderRenderManager {
    private static ThunderRenderManager instance;
    private final List<ThunderEffect> effects = new ArrayList<>();

    private ThunderRenderManager() {}

    public static ThunderRenderManager getInstance() {
        if (instance == null) {
            instance = new ThunderRenderManager();
        }
        return instance;
    }

    public void addThunder(List<ThunderSegment> segments, int lifetime) {
        effects.add(new ThunderEffect(segments, lifetime));
    }

    public void tick() {
        Iterator<ThunderEffect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            ThunderEffect effect = iterator.next();
            effect.tick();

            for (ThunderSegment segment : effect.segments) {
                segment.age += 0.5f;
                float progress = Math.min(1.0f, segment.age / effect.maxLifetime);
                segment.alpha = Math.max(0.1f, 1.0f - progress);
            }

            if (effect.isExpired()) {
                iterator.remove();
            }
        }
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        if (effects.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (ThunderEffect effect : effects) {
            ThunderMeshBuilder.renderThunder(poseStack, bufferSource, effect.segments);
        }

        poseStack.popPose();
    }

    public void clear() {
        effects.clear();
    }

    private static class ThunderEffect {
        List<ThunderSegment> segments;
        int lifetime;
        int maxLifetime;

        ThunderEffect(List<ThunderSegment> segments, int lifetime) {
            this.segments = segments;
            this.maxLifetime = lifetime;
            this.lifetime = lifetime;
        }

        void tick() {
            lifetime--;
        }

        boolean isExpired() {
            return lifetime <= 0;
        }
    }
}
