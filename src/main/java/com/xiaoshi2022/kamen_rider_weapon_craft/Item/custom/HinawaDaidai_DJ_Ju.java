package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.HinawaDaidai_DJ_Ju.HinawaDaidai_DJ_JuRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.entity.LaserBeamEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.ServerSound;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.procedures.PullSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft")
public class HinawaDaidai_DJ_Ju extends Item implements GeoItem {
    private static final float SHOOT_POWER = 1.5f;
    private static final float INACCURACY = 1.0f;
    private static final int SHOOT_COOLDOWN = 20;

    public HinawaDaidai_DJ_Ju(Properties p_41383_) {
        super(p_41383_);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    private void shoot(Level level, Player player) {
        if (level.isClientSide) return;

        ItemStack bow = player.getMainHandItem();
        if (!(bow.getItem() instanceof sonicarrow sa)) return;

        sonicarrow.Mode mode = sa.getCurrentMode(bow);
        sonicarrow.ModeConfig cfg = sa.getConfig(mode);

        float charge = 0.0F;               // 如果需要蓄力可改为实际值
        LaserBeamEntity laser = new LaserBeamEntity(
                level,
                player,
                cfg.particle(),            // 对应形态粒子
                cfg.damage(),              // 对应形态伤害
                cfg.shootSound(),          // 对应形态音效
                charge,
                bow
        );
        laser.shoot(player.getXRot(), player.getYRot(), 0.0F, 1.5F, 0.0F);
        level.addFreshEntity(laser);
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);




    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HinawaDaidai_DJ_JuRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new HinawaDaidai_DJ_JuRenderer();
                }
                return this.renderer;
            }
        });
    }

    private static final RawAnimation DISC = RawAnimation.begin().thenPlay("disc");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "disc", 20, state -> PlayState.CONTINUE)
                .triggerableAnim("disc", DISC)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private static boolean isUsing = false;

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.startUsingItem(hand);
            isUsing = true;
            if (level instanceof ServerLevel serverLevel) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "disc", "disc");
                ServerSound.sendToServer(new ServerSound(ServerSound.SoundType.START_STANDBY));
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int ticksRemaining) {
        if (shooter instanceof Player player) {
            isUsing = false;
            if (level instanceof ServerLevel serverLevel) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "disc", "disc");
            }
            ServerSound.sendToServer(new ServerSound(ServerSound.SoundType.STOP_STANDBY));

            if (!level.isClientSide && player.getServer() != null) {
                float yaw = player.getYRot();
                float pitch = player.getXRot();
                float velocity = 2f;

                double xSpeed = -Mth.sin(yaw * (float) Math.PI / 180.0F) * Mth.cos(pitch * (float) Math.PI / 180.0F) * velocity;
                double ySpeed = -Mth.sin(pitch * (float) Math.PI / 180.0F) * velocity;
                double zSpeed = Mth.cos(yaw * (float) Math.PI / 180.0F) * Mth.cos(pitch * (float) Math.PI / 180.0F) * velocity;

                float chargeTime = (float) (this.getUseDuration(stack) - ticksRemaining) / 20.0F;

                LaserBeamEntity laserBeam = new LaserBeamEntity(level, player, ModParticles.AONICX_PARTICLE.get(), chargeTime, stack);
                laserBeam.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
                laserBeam.setDeltaMovement(xSpeed, ySpeed, zSpeed);
                laserBeam.damage = 14.0D;

                level.addFreshEntity(laserBeam);


                stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && ServerSound.isPlayingStandbySound()) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                if (isHoldingDJ(player) && isUsing) {
                    triggerAnim(player, GeoItem.getOrAssignId(player.getMainHandItem(), (ServerLevel) player.level()), "disc", "disc");
                    PullSounds.playPullStandbyDJSound(player);
                }
            }
        }
    }

    private static boolean isHoldingDJ(Player player) {
        return player.getMainHandItem().getItem() instanceof HinawaDaidai_DJ_Ju ||
                player.getOffhandItem().getItem() instanceof HinawaDaidai_DJ_Ju;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }
}
