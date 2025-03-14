package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.entity.lockseedIronBarsEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.items.ItemStackHandler;

public class lockseedIronBarsEntityRenderer implements BlockEntityRenderer<lockseedIronBarsEntity> {
    public lockseedIronBarsEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(lockseedIronBarsEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverLay) {
        Direction direction = entity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        ItemStackHandler inventory = entity.getInventory();
        int posLong = (int) entity.getBlockPos().asLong();

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.5625, 0.5);
                float f = direction.toYRot();
                poseStack.mulPose(Axis.YP.rotationDegrees(f));
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                Vec2 itemOffset = entity.getItemOffset(i);
                poseStack.translate(itemOffset.x, itemOffset.y, 0);
                poseStack.scale(0.35f, 0.35f, 0.35f);
                if (entity.getLevel() != null) {
                    Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, LevelRenderer.getLightColor(entity.getLevel(),
                            entity.getBlockPos()), combinedOverLay, poseStack, buffer, entity.getLevel(), posLong + i);
                }
                poseStack.popPose();
            }
        }
    }
}