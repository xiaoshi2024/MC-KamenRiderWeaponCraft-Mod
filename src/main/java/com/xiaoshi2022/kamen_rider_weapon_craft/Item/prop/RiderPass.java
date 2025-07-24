package com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RiderPass extends Item {
    public RiderPass(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // 确保玩家是服务器端的玩家
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // 创建目标维度的资源键
            ResourceKey<Level> destinationType = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "the_desertof_time"));

            // 检查玩家是否已经在目标维度
            if (serverPlayer.level().dimension() == destinationType) {
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }

            // 获取目标维度的 ServerLevel 实例
            ServerLevel nextLevel = serverPlayer.server.getLevel(destinationType);
            if (nextLevel != null) {
                // 发送游戏事件包
                serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0));

                // 传送玩家到目标维度
                serverPlayer.teleportTo(nextLevel, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());

                // 发送玩家能力包
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(serverPlayer.getAbilities()));

                // 发送玩家效果包
                for (var effectInstance : serverPlayer.getActiveEffects()) {
                    serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(serverPlayer.getId(), effectInstance));
                }

                // 发送维度事件包
                serverPlayer.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
            } else {
                // 目标维度不存在，输出错误信息
                System.out.println("Target dimension does not exist!");
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}