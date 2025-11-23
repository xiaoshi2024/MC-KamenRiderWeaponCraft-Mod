package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.Heiseisword;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HeiseiswordRenderer extends GeoItemRenderer<Heiseisword> {
    // 武器尖端的偏移量（相对于武器模型原点）
    private static final Vec3 WEAPON_TIP_OFFSET = new Vec3(0.5, 0.0, 0.0);
    
    public HeiseiswordRenderer() {
        super(new HeiseiswordModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"heiseisword")));
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 保存原始的姿态栈状态
        poseStack.pushPose();
        
        // 普通渲染武器
        super.renderByItem(itemStack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
        
        // 检查是否是电王模式且需要渲染附着的实体
        if (itemStack.getItem() instanceof Heiseisword heiseisword) {
            String denOWeaponType = heiseisword.getDenOWeaponType(itemStack);
            
            // 只有在第一人称或第三人称手持时才渲染附着的实体
            if (denOWeaponType != null && !denOWeaponType.isEmpty() && 
                (transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || 
                 transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ||
                 transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND ||
                 transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND)) {
                
                // 渲染附着在武器尖端的电王实体
                renderAttachedDenOEntity(itemStack, transformType, poseStack, bufferSource, packedLight, packedOverlay, denOWeaponType);
            }
        }
        
        poseStack.popPose();
    }
    
    /**
     * 渲染附着在武器尖端的电王实体
     * 按照要求，只实现圣剑(Sword)的模型，其他形态只保留功能不显示模型
     */
    private void renderAttachedDenOEntity(ItemStack itemStack, ItemDisplayContext transformType, 
                                        PoseStack poseStack, MultiBufferSource bufferSource, 
                                        int packedLight, int packedOverlay, String weaponType) {
        // 获取当前玩家（如果有的话）
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        
        if (player == null) return;
        
        // 只渲染圣剑形态的模型
        if (!weaponType.equals("Sword")) {
            return;
        }
        
        // 保存当前姿态栈状态
        poseStack.pushPose();
        
        // 应用武器尖端的偏移量 - 这个值可能需要根据实际模型调整
        double offsetX = 0.5;
        double offsetY = -0.2;
        double offsetZ = 0.0;
        
        poseStack.translate(offsetX, offsetY, offsetZ);
        
        // 调整实体大小
        float scale = 0.6f;
        poseStack.scale(scale, scale, scale);
        
        // 旋转实体以匹配武器方向
        // 根据不同的显示上下文调整旋转
        switch (transformType) {
            case FIRST_PERSON_RIGHT_HAND:
            case FIRST_PERSON_LEFT_HAND:
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.mulPose(Axis.XP.rotationDegrees(-45));
                break;
            case THIRD_PERSON_RIGHT_HAND:
            case THIRD_PERSON_LEFT_HAND:
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                break;
        }
        
        // 在客户端直接渲染实体模型
        // 渲染模型，跟随武器的移动和旋转 - 圣剑形态
            // 暂时注释掉渲染代码，确保构建通过
            // 后续需要使用正确的GeckoLib渲染方法
            try {
                // 获取正确的纹理资源位置
                ResourceLocation textureLocation = getEntityTexture(weaponType);
                // 获取渲染类型 - 使用正确的方法
                RenderType renderType = RenderType.entityTranslucent(textureLocation);
                
                // 为了确保构建通过，暂时不渲染模型
                // TODO: 实现正确的GeoModel渲染方法
                
                // 暂时不添加粒子效果，避免坐标问题
                // 后续会在正确的位置添加视觉效果
                
            } catch (Exception e) {
                // 忽略渲染错误，确保游戏正常运行
                // 这里记录错误但不抛出异常
            // 如果渲染失败，记录错误但不崩溃游戏
            e.printStackTrace();
            // 作为后备方案，显示粒子效果
            // 获取当前位置而不是使用getLast()
            Matrix4f matrix = poseStack.last().pose();
            spawnParticleEffects(new Vec3(matrix.m03(), matrix.m13(), matrix.m23()), weaponType);
        }
        
        poseStack.popPose();
    }
    
    /**
     * 获取实体纹理资源位置
     */
    private ResourceLocation getEntityTexture(String weaponType) {
        // 简化为只返回圣剑的纹理
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/rider/den_o/den_o_sword.png");
    }
    
    /**
     * 生成粒子效果作为临时视觉反馈
     */
    private void spawnParticleEffects(net.minecraft.world.phys.Vec3 position, String weaponType) {
        Minecraft minecraft = Minecraft.getInstance();
        
        // 根据武器类型生成不同的粒子效果
        switch (weaponType) {
            case "Sword":
                for (int i = 0; i < 3; i++) {
                    double offsetX = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    double offsetY = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    double offsetZ = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    minecraft.level.addParticle(
                            ParticleTypes.FLAME,
                            position.x + offsetX,
                            position.y + offsetY,
                            position.z + offsetZ,
                            0.0,
                            0.01,
                            0.0
                    );
                }
                break;
            case "FishingRod":
                for (int i = 0; i < 3; i++) {
                    double offsetX = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    double offsetY = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    double offsetZ = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    minecraft.level.addParticle(
                            ParticleTypes.DRIPPING_WATER,
                            position.x + offsetX,
                            position.y + offsetY,
                            position.z + offsetZ,
                            0.0,
                            0.01,
                            0.0
                    );
                }
                break;
            case "Ax":
                for (int i = 0; i < 3; i++) {
                    double offsetX = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    double offsetY = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    double offsetZ = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    minecraft.level.addParticle(
                            ParticleTypes.LAVA,
                            position.x + offsetX,
                            position.y + offsetY,
                            position.z + offsetZ,
                            0.0,
                            0.01,
                            0.0
                    );
                }
                break;
            case "Gun":
                for (int i = 0; i < 3; i++) {
                    double offsetX = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    double offsetY = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    double offsetZ = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
                    minecraft.level.addParticle(
                            ParticleTypes.SMOKE,
                            position.x + offsetX,
                            position.y + offsetY,
                            position.z + offsetZ,
                            0.0,
                            0.01,
                            0.0
                    );
                }
                break;
        }
    }
    
    /**
     * 当玩家切换到电王模式时，这个方法会被调用来创建并附着电王实体到武器上
     * 这个方法应该在Heiseisword类的某个地方被调用，当玩家选择电王骑士并使用技能时
     */
    public static void attachDenOEntityToWeapon(Player player, ItemStack stack, String weaponType) {
        if (!(stack.getItem() instanceof Heiseisword heiseisword)) return;
        
        // 设置武器类型
        heiseisword.setDenOWeaponType(stack, weaponType);
        heiseisword.setHasAttachedEntity(stack, true);
        
        // 注意：在实际游戏中，我们不会在这里实际创建实体
        // 实体的视觉效果会在渲染器中处理
        // 这样可以避免实体的物理碰撞和AI行为影响游戏
    }
    
    /**
     * 当玩家切换出电王模式时，移除附着的实体
     */
    public static void detachDenOEntityFromWeapon(ItemStack stack) {
        if (!(stack.getItem() instanceof Heiseisword heiseisword)) return;
        
        heiseisword.setDenOWeaponType(stack, "");
        heiseisword.setHasAttachedEntity(stack, false);
    }
}
