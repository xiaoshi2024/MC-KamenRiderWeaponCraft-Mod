package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.CloseMapPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.custom.AonicxParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding;
import com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK.weapon_map;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.arrowx.LaserBeamEntityRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.daidaimaru.ThrownDaidaimaruRenderer;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.Time_traveler_studio_block.Time_traveler_studio_blockRenderer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding.CHANGE_KEY;
import static com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding.OPEN_LOCKSEED;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.helheim_crack.helheim_crackBlockRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachine.RiderFusionMachineBlockRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.plant.HelheimVine.HelheimVineBlockRenderer;


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
            if (!(event.getEntity() instanceof Player)) return;

            Player player = (Player) event.getEntity();
            ItemStack heldItem = event.getItemStack();
            InteractionHand hand = event.getHand();

            // 检查玩家是否按下特定键（例如 X 键）
            if (!KeyBinding.CHANGE_KEY.isDown()) return;

            // 检查玩家是否拥有自定义buff
            if (!PlayerUtils.hasCustomBuff(player, "helmheim_power")) return;

            // 检查玩家是否持有哈密瓜
            if (heldItem.is(ItemTags.create(new ResourceLocation("forge", "fruits/cantaloupe")))) {
                replaceItem(player, hand, heldItem, ModItems.MELON.get());
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }

            // 检查玩家是否持有樱桃
            else if (heldItem.is(ItemTags.create(new ResourceLocation("forge", "fruits/cherry")))) {
                replaceItem(player, hand, heldItem, ModItems.CHERYY.get());
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }

            // 继续添加更多水果的逻辑
        }

        private static void replaceItem(Player player, InteractionHand hand, ItemStack heldItem, Item newItem) {
            // 检查玩家是否持有物品
            if (!heldItem.isEmpty()) {
                // 消耗手中的一个物品
                heldItem.shrink(1);

                // 如果消耗后物品数量为0，则清空手中的物品
                if (heldItem.getCount() <= 0) {
                    player.setItemInHand(hand, ItemStack.EMPTY);
                }
            }

            // 创建新物品的ItemStack
            ItemStack newItemStack = new ItemStack(newItem);

            // 将新物品添加到玩家的背包中
            if (!player.getInventory().add(newItemStack)) {
                // 如果背包已满，将新物品丢弃到玩家周围
                player.drop(newItemStack, false);
            }
        }
    }
    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID, value = Dist.CLIENT,bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents{
        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.AONICX_PARTICLE.get(), AonicxParticles::provider);
        }

       @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){
        event.register(CHANGE_KEY);
        event.register(OPEN_LOCKSEED);
        }

        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.HELHEIM_VINE_ENTITY.get(), HelheimVineBlockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.HELHEIM_CRACK_BLOCK_ENTITY.get(), helheim_crackBlockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.TIME_TRAVELER_STUDIO_BLOCK_ENTITY.get(), Time_traveler_studio_blockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.RIDER_FUSION_MACHINE_BLOCK_ENTITY.get(), context -> new RiderFusionMachineBlockRenderer());
    }
        @SubscribeEvent
        public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            EntityRenderers.register(ModEntityTypes.THROWN_DAIDAIMARU.get(), ThrownDaidaimaruRenderer::new);
            EntityRenderers.register(ModEntityTypes.LASER_BEAM.get(), LaserBeamEntityRenderer::new);
        }
    }
}

