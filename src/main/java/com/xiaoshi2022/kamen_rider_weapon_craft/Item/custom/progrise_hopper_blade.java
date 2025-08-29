package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.progrise_hopper_blade.progrise_hopper_bladeRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class progrise_hopper_blade extends SwordItem implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("Charge");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public progrise_hopper_blade() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 1200; // 武器的耐久度
            }

            @Override
            public float getSpeed() {
                return 4.5f; // 武器的攻击速度
            }

            @Override
            public float getAttackDamageBonus() {
                return 20.0f; // 武器的额外攻击伤害
            }

            @Override
            public int getLevel() {
                return 3; // 武器的等级
            }

            @Override
            public int getEnchantmentValue() {
                return 5; // 武器的附魔价值
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(); // 修复材料
            }
        }, 3, 2.0f, new Item.Properties());

        // 注册为服务器端处理的物品，启用动画数据同步和服务器端动画触发
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // 初始化客户端渲染器
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private progrise_hopper_bladeRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new progrise_hopper_bladeRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 20, event -> {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("idle"));
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<>(this, "Charge", 5, state -> PlayState.CONTINUE)
                .triggerableAnim("Charge", CHARGE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 设置间隔时间，19刻（Minecraft 20刻为现实1秒）
        final int INTERVAL = 1 * 19;

        // 设置冷却时间，5秒（100刻）
        final int COOLDOWN = 5 * 20;

        // 获取当前时间
        long currentTime = level.getGameTime();

        // 获取玩家最后一次播放音效的时间
        long lastPlayed = getLastPlayedSoundTime(stack);

        // 获取右键冷却时间
        long rightClickCooldown = getRightClickCooldown(player);

        // 检查是否在冷却时间内
        if (currentTime < rightClickCooldown) {
            return super.use(level, player, hand);
        }

        // 检查是否已经过了间隔时间
        if (currentTime - lastPlayed >= INTERVAL) {
            if (level instanceof ServerLevel serverLevel) {
                // 触发动画
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "Charge", "Charge");

                // 在服务器端播放 PROGRISE_HOPPER_BLADE_PRESS 音效
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.PROGRISE_HOPPER_BLADE_PRESS.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                // 更新玩家的持久化数据，记录当前时间
                setLastPlayedSoundTime(stack, currentTime);
            }
        }

        // 右键次数逻辑
        int clickCount = getRightClickCount(stack);
        clickCount++;

        // 检查是否达到5次
        if (clickCount >= 5) {
            if (level instanceof ServerLevel serverLevel) {
                // 播放 FINISH_RISE 音效
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FINISH_RISE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                // 触发 PRO_STANDBY_TONE 待机音
                playStandbyTone(serverLevel, player, currentTime);

                // 设置右键冷却时间
                setRightClickCooldown(player, currentTime + COOLDOWN);

                // 重置右键次数
                clickCount = 0;
            }
        }

        // 更新右键次数
        setRightClickCount(stack, clickCount);

        return super.use(level, player, hand);
    }

    // 辅助方法：播放待机音
    private void playStandbyTone(ServerLevel serverLevel, Player player, long startTime) {
        // 设置待机音的持续时间为5秒（100刻）
        final int STANDBY_DURATION = 5 * 20;

        // 播放待机音
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.PRO_STANDBY_TONE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // 设置待机音的开始时间和结束时间
        setStandbyStartTime(player, startTime);
        setStandbyEndTime(player, startTime + STANDBY_DURATION);
    }

    // 辅助方法：获取和设置音效播放时间
    private long getLastPlayedSoundTime(ItemStack stack) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        return stack.getTag().getLong("lastPlayedSound");
    }

    private void setLastPlayedSoundTime(ItemStack stack, long time) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putLong("lastPlayedSound", time);
    }

    // 辅助方法：获取和设置右键次数
    private int getRightClickCount(ItemStack stack) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        return stack.getTag().getInt("rightClickCount");
    }

    private void setRightClickCount(ItemStack stack, int count) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putInt("rightClickCount", count);
    }

    // 辅助方法：获取和设置待机音的开始时间
    private long getStandbyStartTime(Player player) {
        return player.getPersistentData().getLong("standbyStartTime");
    }

    private void setStandbyStartTime(Player player, long time) {
        player.getPersistentData().putLong("standbyStartTime", time);
    }

    // 辅助方法：获取和设置待机音的结束时间
    private long getStandbyEndTime(Player player) {
        return player.getPersistentData().getLong("standbyEndTime");
    }

    private void setStandbyEndTime(Player player, long time) {
        player.getPersistentData().putLong("standbyEndTime", time);
    }

    // 辅助方法：获取和设置右键冷却时间
    private long getRightClickCooldown(Player player) {
        return player.getPersistentData().getLong("rightClickCooldown");
    }

    private void setRightClickCooldown(Player player, long time) {
        player.getPersistentData().putLong("rightClickCooldown", time);
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}