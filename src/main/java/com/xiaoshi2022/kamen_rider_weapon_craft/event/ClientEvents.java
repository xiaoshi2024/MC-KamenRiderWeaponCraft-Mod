package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.daidaimaru.ThrownDaidaimaruRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.musousaberd.musousaberdRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.arrowx.LaserBeamEntityRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachine.RiderFusionMachineBlockRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.Time_traveler_studio_block.Time_traveler_studio_blockRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.helheim_crack.helheim_crackBlockRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.renderer.lockseedIronBarsEntityRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.CloseMapPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.FruitConversionPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.custom.LaserParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid.ExAidSlashEffectRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.FruitConversionRegistry;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.PlayerUtils;
import com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK.weapon_map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import static com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks.HELHEIMVINE;
import static com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding.CHANGE_KEY;
import static com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding.OPEN_LOCKSEED;


public class ClientEvents {
    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID,value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (KeyBinding.CHANGE_KEY.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                ItemStack stack = null;
                if (player != null) {
                    stack = player.getMainHandItem();
                    if (stack.getItem() instanceof weapon_map) {
                        kamen_rider_weapon_craft.PACKET_HANDLER.sendToServer(new CloseMapPacket());
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
            Player player = event.getEntity();

            // 1. 检查按键状态
            if (!KeyBinding.CHANGE_KEY.isDown()) {
                return;
            }

            // 2. 检查Helheim能量状态
            if (!PlayerUtils.hasCustomBuff(player, "helmheim_power")) {
                return;
            }

            // 3. 获取并验证物品
            ItemStack heldItem = event.getItemStack();
            if (heldItem.isEmpty() || !FruitConversionRegistry.isConvertibleFruit(heldItem)) {
                return;
            }

            // 4. 发送转换请求
            kamen_rider_weapon_craft.PACKET_HANDLER.sendToServer(
                    new FruitConversionPacket(event.getHand())
            );

            // 5. 取消事件防止其他操作
            if (FruitConversionRegistry.isConvertibleFruit(heldItem)) {
                event.setCanceled(true);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID, value = Dist.CLIENT,bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents{
        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.AONICX_PARTICLE.get(), LaserParticles.Provider::new);
            event.registerSpriteSet(ModParticles.LEMON_PARTICLE.get(),  LaserParticles.Provider::new);
            event.registerSpriteSet(ModParticles.MELON_PARTICLE.get(),   LaserParticles.Provider::new);
            event.registerSpriteSet(ModParticles.CHERRY_PARTICLE.get(),  LaserParticles.Provider::new);
            event.registerSpriteSet(ModParticles.PEACH_PARTICLE.get(),  LaserParticles.Provider::new);
        }

       @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){
        event.register(CHANGE_KEY);
        event.register(OPEN_LOCKSEED);
        }

        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.HELHEIM_CRACK_BLOCK_ENTITY.get(), helheim_crackBlockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.TIME_TRAVELER_STUDIO_BLOCK_ENTITY.get(), Time_traveler_studio_blockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.RIDER_FUSION_MACHINE_BLOCK_ENTITY.get(), context -> new RiderFusionMachineBlockRenderer());
    }
        @SubscribeEvent
        public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            EntityRenderers.register(ModEntityTypes.THROWN_DAIDAIMARU.get(), ThrownDaidaimaruRenderer::new);
            EntityRenderers.register(ModEntityTypes.LASER_BEAM.get(), LaserBeamEntityRenderer::new);
            EntityRenderers.register(ModEntityTypes.EXAID_SLASH_EFFECT.get(), ExAidSlashEffectRenderer::new);
            // 注册Ghost伟人魂实体渲染器
            EntityRenderers.register(ModEntityTypes.GHOST_HEROIC_SOUL.get(), com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ghost.GhostHeroicSoulRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.LOCKSEEDIRONBARS_ENTITY.get(), lockseedIronBarsEntityRenderer::new);
        }
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            CuriosRendererRegistry.register(ModItems.MUSOUSABERD.get(), musousaberdRenderer::new);

            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(HELHEIMVINE.get(), RenderType.cutout());
            });
        }
    }
}

