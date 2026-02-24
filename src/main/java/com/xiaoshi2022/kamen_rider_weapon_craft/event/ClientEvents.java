package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.KachidokiBata.KachidokiBataRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.daidaimaru.ThrownDaidaimaruRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.musousaberd.musousaberdRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.needlekunai.ThrownNeedleKunaiRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.arrowx.LaserBeamEntityRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.KachidokiBataBlock.KachidokiBataBlockRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachine.RiderFusionMachineBlockRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.Time_traveler_studio_block.Time_traveler_studio_blockRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.helheim_crack.helheim_crackBlockRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.renderer.lockseedIronBarsEntityRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.entity.line.denliner;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.CloseMapPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.FruitConversionPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.client.LaserParticles;
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
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import static com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks.HELHEIMVINE;
import static com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding.CHANGE_KEY;
import static com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding.OPEN_LOCKSEED;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// 移除了类级别的@OnlyIn注解，改为使用内部类的@Mod.EventBusSubscriber注解来控制客户端执行
public class ClientEvents {
    // 这个内部类只在客户端执行，因为@Mod.EventBusSubscriber指定了value = Dist.CLIENT
    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID, value = Dist.CLIENT)
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
                    } else if (stack.getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.breakamnuster.BreakamnusterGun || 
                               stack.getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.breakamnuster.BreakamnusterSword) {
                        // 处理 Breakamnuster 武器切换
                        handleBreakamnusterWeaponSwitch(player, stack);
                    }
                }
            }
        }
        
        /**
         * 处理 Breakamnuster 武器切换逻辑
         */
        private static void handleBreakamnusterWeaponSwitch(LocalPlayer player, ItemStack oldStack) {
            // 创建新的物品堆栈
            ItemStack newStack;
            
            // 根据当前武器类型切换
            if (oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.breakamnuster.BreakamnusterGun) {
                // 从枪切换到剑
                newStack = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.BREAKAMNUSTER_SWORD.get());
            } else {
                // 从剑切换到枪
                newStack = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.BREAKAMNUSTER_GUN.get());
            }
            
            // 继承原武器的耐久度
            newStack.setDamageValue(oldStack.getDamageValue());
            
            // 替换主手物品
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, newStack);
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

    // 这个内部类只在客户端执行，因为@Mod.EventBusSubscriber指定了value = Dist.CLIENT
    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            // 粒子注册只在客户端执行，且内部类已经有@Mod.EventBusSubscriber(value = Dist.CLIENT)注解
            // 直接使用方法引用是安全的，因为这个方法只会在客户端被调用
            event.registerSpriteSet(ModParticles.AONICX_PARTICLE.get(), LaserParticles.Provider::new);
            event.registerSpriteSet(ModParticles.LEMON_PARTICLE.get(), LaserParticles.Provider::new);
            event.registerSpriteSet(ModParticles.MELON_PARTICLE.get(), LaserParticles.Provider::new);
            event.registerSpriteSet(ModParticles.CHERRY_PARTICLE.get(), LaserParticles.Provider::new);
            event.registerSpriteSet(ModParticles.PEACH_PARTICLE.get(), LaserParticles.Provider::new);
        }

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(CHANGE_KEY);
            event.register(OPEN_LOCKSEED);
        }

        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.HELHEIM_CRACK_BLOCK_ENTITY.get(), helheim_crackBlockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.TIME_TRAVELER_STUDIO_BLOCK_ENTITY.get(), Time_traveler_studio_blockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.RIDER_FUSION_MACHINE_BLOCK_ENTITY.get(), context -> new RiderFusionMachineBlockRenderer());
            event.registerBlockEntityRenderer(ModBlockEntities.KACHIDOKI_BATA_BLOCK_ENTITY.get(), KachidokiBataBlockRenderer::new);
        }

        @SubscribeEvent
        public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            EntityRenderers.register(ModEntityTypes.THROWN_DAIDAIMARU.get(), ThrownDaidaimaruRenderer::new);
            EntityRenderers.register(ModEntityTypes.THROWN_NEEDLE_KUNAI.get(), ThrownNeedleKunaiRenderer::new);
            EntityRenderers.register(ModEntityTypes.LASER_BEAM.get(), LaserBeamEntityRenderer::new);
            EntityRenderers.register(ModEntityTypes.EXAID_SLASH_EFFECT.get(), ExAidSlashEffectRenderer::new);
            // 注册Ghost伟人魂实体渲染器
            EntityRenderers.register(ModEntityTypes.GHOST_HEROIC_SOUL.get(), com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ghost.GhostHeroicSoulRenderer::new);
            // 注册Hassyars子弹实体渲染器
            EntityRenderers.register(ModEntityTypes.HASSYARS.get(), com.xiaoshi2022.kamen_rider_weapon_craft.entity.projectile.HassyarsRenderer::new);
            // 注册Photonic子弹实体渲染器
            EntityRenderers.register(ModEntityTypes.PHOTONIC.get(), com.xiaoshi2022.kamen_rider_weapon_craft.entity.projectile.PhotonicRenderer::new);
            // 移除了玩家分身NPC渲染器注册
            event.registerBlockEntityRenderer(ModBlockEntities.LOCKSEEDIRONBARS_ENTITY.get(), lockseedIronBarsEntityRenderer::new);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            CuriosRendererRegistry.register(ModItems.MUSOUSABERD.get(), musousaberdRenderer::new);
            CuriosRendererRegistry.register(ModItems.KACHIDOKI_BATA.get(), KachidokiBataRenderer::new);

            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(HELHEIMVINE.get(), RenderType.cutout());
            });
        }

        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.DENLINER.get(), denliner.createAttributes());
        }
    }
}

