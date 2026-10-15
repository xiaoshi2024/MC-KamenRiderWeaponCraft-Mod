package com.xiaoshi2022.kamenriderweaponcraft;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.client.render.thunder.ThunderRenderManager;
import com.xiaoshi2022.kamenriderweaponcraft.client.screen.SonicBowScreen;
import com.xiaoshi2022.kamenriderweaponcraft.entity.client.SonicArrowLaserRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import com.xiaoshi2022.kamenriderweaponcraft.register.MenuRegister;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = KamenRiderWeaponCraft.MODID, value = Dist.CLIENT)
public class KamenRiderWeaponCraftClient {
    public static KeyMapping nextRiderKey;
    public static KeyMapping prevRiderKey;

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {

    }

    // ==================== 音速弓客户端 ====================

    /** 注册音速弓界面与激光实体渲染器 */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegister.SONIC_ARROW_LASER.get(), SonicArrowLaserRenderer::new);
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegister.SONIC_BOW.get(), SonicBowScreen::new);
    }

    /** 雷电弧特效每帧更新 */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ThunderRenderManager.getInstance().tick();
    }

    /** 雷电弧特效渲染（AFTER_PARTICLES 阶段） */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            Minecraft mc = Minecraft.getInstance();
            PoseStack poseStack = new PoseStack();
            MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

            ThunderRenderManager.getInstance().render(poseStack, buffer);
        }
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // 骑士选择键（原 Y 键功能）
        nextRiderKey = new KeyMapping(
                "key.kamenriderweaponcraft.select_rider",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,  // 改回 Y 键
                "key.categories.gameplay"
        );

        // 超必杀修饰键（原 X 键功能）
        prevRiderKey = new KeyMapping(
                "key.kamenriderweaponcraft.ultimate_mode",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_X,  // X 键作为超必杀修饰键
                "key.categories.gameplay"
        );
        event.register(nextRiderKey);
        event.register(prevRiderKey);
    }

    @EventBusSubscriber(modid = KamenRiderWeaponCraft.MODID, value = Dist.CLIENT)
    public static class KeyInputHandler {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof Heiseisword heiseisword)) return;

            if (nextRiderKey.consumeClick()) {
                heiseisword.handleClientRiderSelection(true);
            } else if (prevRiderKey.consumeClick()) {
                heiseisword.handleClientRiderSelection(false);
            }
        }
    }
}