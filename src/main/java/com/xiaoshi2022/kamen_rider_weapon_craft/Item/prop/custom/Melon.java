package com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.melon.MelonRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Random;
import java.util.function.Consumer;

public class Melon extends Item implements GeoItem {
    private static final RawAnimation START = RawAnimation.begin().thenPlay("start");
    private static final RawAnimation COMBINE = RawAnimation.begin().thenPlay("combine");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public Melon(net.minecraft.world.item.Item.Properties properties) {
        super(properties);

        // Register our item as server-side handled.
        // This enables both animation data syncing and server-side animation triggering
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private MelonRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new MelonRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putBoolean("lockseed", true);

        // 设置间隔时间，12秒
        final int INTERVAL = 12 * 20; // Minecraft中1秒等于20个tick

        // 获取玩家最后一次播放音效的时间
        long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");

        // 获取当前时间
        long currentTime = level.getGameTime();

        // 检查是否已经过了间隔时间
        if (currentTime - lastPlayed >= INTERVAL) {
            if (level instanceof ServerLevel serverLevel) {
                // 触发动画
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "start", "start");

                // 检查玩家是否已经生成了5个方块
                int generatedBlocks = player.getPersistentData().getInt("generatedBlocks");

                // 尝试生成一个赫尔海姆方块
                if (generateHelheimCrack(level, player)) {
                    generatedBlocks++; // 成功生成一个方块，计数加1
                }

                // 更新生成方块的数量
                player.getPersistentData().putInt("generatedBlocks", generatedBlocks);

                // 如果生成的方块总数达到5个，触发爆炸
                if (generatedBlocks >= 5) {
                    createPlayerOnlyExplosion(level, player);
                    player.getPersistentData().putInt("generatedBlocks", 0); // 重置计数
                }

                // 更新玩家最后一次播放音效的时间
                player.getPersistentData().putLong("lastPlayedSound", currentTime);

                // 物品不再消失
                // stack.shrink(1); // 注释掉这行代码
            }
        } else {
            // 如果未达到间隔时间，提示玩家
            player.displayClientMessage(Component.literal("冷却时间未结束，还需等待 " + (INTERVAL - (currentTime - lastPlayed)) / 20 + " 秒"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private boolean generateHelheimCrack(Level level, Player player) {
        Random random = new Random();
        BlockPos playerPos = player.blockPosition();
        int range = 4; // 生成范围为 4 格

        // 35% 的几率生成方块
        if (random.nextDouble() < 0.35) {
            // 随机生成一个位置
            BlockPos randomPos = new BlockPos(
                    playerPos.getX() + random.nextInt(range * 2) - range,
                    playerPos.getY() + random.nextInt(range * 2) - range,
                    playerPos.getZ() + random.nextInt(range * 2) - range
            );

            // 检查生成位置是否为空，如果是，则放置方块
            if (level.isEmptyBlock(randomPos)) {
                BlockState state = ModBlocks.HELHEIM_CRACK_BLOCK.get().defaultBlockState();
                level.setBlockAndUpdate(randomPos, state);

                // 播放声音
                playSound(level, player, randomPos);
                return true; // 成功生成一个方块
            }
        }
        return false; // 未生成方块
    }

    private void createPlayerOnlyExplosion(Level level, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            // 在玩家脚下生成一个爆炸，只对玩家造成伤害
            player.hurt(player.damageSources().generic(), 3.0F); // 对玩家造成3点伤害

            // 检查玩家是否处于残血状态（生命值小于等于1点）
            if (player.getHealth() <= 1.0F) {
                // 如果玩家死亡，在xx你被什么杀害了改为自定义
                String deathMessage = player.getName().getString() + "被自己贪玩的小手背叛了";
                player.sendSystemMessage(Component.literal(deathMessage));
            }
        }
    }

    private void playSound(Level level, Player player, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, pos, ModSounds.MELONENERGY.get(), SoundSource.PLAYERS, 1, 1);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "start", 20, state -> PlayState.STOP)
                .triggerableAnim("start", START)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                    // Use helper method to avoid client-code in common class
                    Player player = ClientUtils.getClientPlayer();

                    if (player != null)
                        player.playSound(ModSounds.MELONENERGY.get(), 1, 1);
                }));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
