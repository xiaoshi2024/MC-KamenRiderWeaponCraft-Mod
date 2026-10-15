package com.xiaoshi2022.kamenriderweaponcraft.Item.client.sonicarrow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.SonicArrowItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * 音速弓渲染器：按装载锁种切换对应形态模型/贴图
 * GeckoLib 4.x 的 renderByItem 通过 getGeoModel() 取模型，直接重写即可（1.20.1 的反射方案不再需要）
 *
 * 激光起点捕获（参照尸兄天罡匙的做法）：在 renderRecursively 渲染 arrow 骨骼时，
 * poseStack 此刻包含完整变换链（物品显示变换 × 全部父骨骼 × 本骨骼），
 * 直接变换原点即得箭尖视空间坐标，加相机坐标转世界坐标。
 * 注：本 GeckoLib 4.7 构建的 GeoBone 世界矩阵追踪不会被填充（prepMatrixForBone 不写矩阵），
 * 必须走渲染期 poseStack 捕获。
 */
public class SonicArrowRenderer extends GeoItemRenderer<SonicArrowItem> {
    /** 激光发射骨骼：弓上的箭 */
    public static final String MUZZLE_BONE = "arrow";

    private final GeoModel<SonicArrowItem>[] models;

    public SonicArrowRenderer() {
        super(new SonicArrowModel(SonicArrowItem.Mode.DEFAULT));
        @SuppressWarnings("unchecked")
        GeoModel<SonicArrowItem>[] array = new GeoModel[SonicArrowItem.Mode.values().length];
        this.models = array;
        for (SonicArrowItem.Mode mode : SonicArrowItem.Mode.values()) {
            this.models[mode.ordinal()] = new SonicArrowModel(mode);
        }
    }

    @Override
    public GeoModel<SonicArrowItem> getGeoModel() {
        ItemStack stack = this.currentItemStack;
        if (stack != null && stack.getItem() instanceof SonicArrowItem sonic) {
            return models[sonic.getCurrentMode(stack).ordinal()];
        }
        return models[SonicArrowItem.Mode.DEFAULT.ordinal()];
    }

    @Override
    public void renderRecursively(PoseStack poseStack, SonicArrowItem animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer vertexConsumer, boolean isReRender,
                                  float partialTick, int packedLight, int packedOverlay, int renderColor) {
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, vertexConsumer,
                isReRender, partialTick, packedLight, packedOverlay, renderColor);

        if (!MUZZLE_BONE.equals(bone.getName())) return;
        captureMuzzlePosition(poseStack);
    }

    /**
     * 渲染完 arrow 骨骼后捕获其世界坐标（仅本玩家手中的弓）
     * 此处 poseStack = 物品显示变换 × 全部父骨骼 × arrow 骨骼自身变换（含拉弓动画），视空间
     */
    private void captureMuzzlePosition(PoseStack poseStack) {
        ItemStack stack = this.currentItemStack;
        if (stack == null || !(stack.getItem() instanceof SonicArrowItem)) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        // 实例相等判断：只捕获本玩家手中的弓（渲染他人/GUI 中的弓时 stack 是不同实例）
        if (stack != player.getMainHandItem() && stack != player.getOffhandItem()) return;

        Vector3f viewPos = poseStack.last().pose().transformPosition(new Vector3f());
        Vec3 worldPos = new Vec3(viewPos.x, viewPos.y, viewPos.z)
                .add(mc.gameRenderer.getMainCamera().getPosition());
        SonicArrowMuzzleTracker.submit(worldPos);
    }
}
