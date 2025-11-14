package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.KachidokiBata;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;

@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft")
public class KachidokiBataEventHandler {
    
    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);
        
        // 检查是否是胜哄旗武器物品
        if (stack.getItem() instanceof KachidokiBata) {
            // 检查玩家是否在看方块表面
            HitResult hitResult = player.pick(5.0D, 0.0F, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos pos = blockHitResult.getBlockPos();
                Direction face = blockHitResult.getDirection();
                
                // 创建放置上下文
                BlockPlaceContext context = new BlockPlaceContext(player, hand, stack, blockHitResult);
                
                // 检查目标位置是否可以放置方块
                if (canPlaceBanner(level, pos, face)) {
                    BlockPos placePos = pos.relative(face);
                    
                    // 尝试放置方块
                    if (placeKachidokiBata(level, placePos, player, blockHitResult)) {
                        // 如果放置成功，完全消耗手中物品
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        
                        // 取消原有的交互，避免武器功能触发
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                    }
                }
            }
        }
    }
    
    /**
     * 检查是否可以在指定位置放置旗帜
     */
    private static boolean canPlaceBanner(Level level, BlockPos pos, Direction face) {
        BlockPos placePos = pos.relative(face);
        
        // 检查放置位置是否有方块
        if (!level.isEmptyBlock(placePos)) {
            return false;
        }
        
        // 检查放置位置下方是否有支撑
        BlockPos belowPos = placePos.below();
        if (!level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 放置胜哄旗方块
     */
    private static boolean placeKachidokiBata(Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        try {
            // 从ModBlocks注册表获取胜哄旗方块
            Block bannerBlock = ModBlocks.KACHIDOKI_BATA_BLOCK.get();
            
            // 设置方块状态（方向等）
            Direction facing = player.getDirection().getOpposite();
            net.minecraft.world.level.block.state.BlockState state = bannerBlock.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, facing);
            
            // 放置方块
            if (level.setBlock(pos, state, 3)) {
                // 播放放置音效
                level.levelEvent(player, 2001, pos, Block.getId(state));
                return true;
            }
        } catch (Exception e) {
            // 如果获取方块失败，使用默认方式放置
            BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, 
                    new ItemStack(ModBlocks.KACHIDOKI_BATA_BLOCK.get().asItem()), hitResult);
            
            if (context.canPlace()) {
                net.minecraft.world.level.block.state.BlockState state = ModBlocks.KACHIDOKI_BATA_BLOCK.get().getStateForPlacement(context);
                if (state != null && level.setBlock(pos, state, 3)) {
                    level.levelEvent(player, 2001, pos, Block.getId(state));
                    return true;
                }
            }
        }
        
        return false;
    }
}