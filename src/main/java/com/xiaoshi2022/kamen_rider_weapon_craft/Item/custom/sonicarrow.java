package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.sonicarrow.sonicarrowRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.entity.LaserBeamEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.ServerSound;
import com.xiaoshi2022.kamen_rider_weapon_craft.particle.ModParticles;
import com.xiaoshi2022.kamen_rider_weapon_craft.procedures.PullSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
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
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft")
public class sonicarrow extends SwordItem implements GeoItem {
    private final float meleeDamage;
    private final float attackSpeed;
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.randomUUID();
    private static final UUID ATTACK_SPEED_MODIFIER = UUID.randomUUID();
    private static final RawAnimation BOWBLADE = RawAnimation.begin().thenPlay("bowblade");
    private static final RawAnimation PULLBACK = RawAnimation.begin().thenPlay("pullback");
    private static final RawAnimation DRAW = RawAnimation.begin().thenPlay("draw");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public enum Mode {
        DEFAULT,
        MELON
    }

    private Mode currentMode = Mode.DEFAULT;

    public sonicarrow(float meleeDamage, float attackSpeed, Properties properties) {
        super((Tier) Tiers.GOLD, (int) meleeDamage, attackSpeed, properties);
        this.meleeDamage = meleeDamage;
        this.attackSpeed = attackSpeed;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public sonicarrow() {
        this(9.0F, 4.4F, new Properties().stacksTo(1).durability(980));
    }

    public void switchMode(ItemStack stack, Mode mode) {
        CompoundTag tag = stack.getOrCreateTag();
        currentMode = mode;
        tag.putString("Mode", currentMode.name());
        stack.setTag(tag);
    }

    public Mode getCurrentMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("Mode")) {
            return Mode.valueOf(tag.getString("Mode"));
        }
        return Mode.DEFAULT;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private sonicarrowRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new sonicarrowRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();

        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.meleeDamage, AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", this.attackSpeed, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(attacker.getUsedItemHand()));
        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide) {
            if (player.getRandom().nextInt(10) == 0) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) player.level()), "bowblade", "bowblade");
                ((ServerLevel) player.level()).playSound(null, player.blockPosition(), ModSounds.SLASH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.BOW || enchantment.category == EnchantmentCategory.WEAPON;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "draw", 20, state -> PlayState.STOP)
                .triggerableAnim("draw", DRAW));
        controllers.add(new AnimationController<>(this, "pullback", 20, state -> PlayState.CONTINUE)
                .triggerableAnim("pullback", PULLBACK));
        controllers.add(new AnimationController<>(this, "bowblade", 20, state -> PlayState.STOP)
                .triggerableAnim("bowblade", BOWBLADE)
                .setSoundKeyframeHandler(state -> {
                    Player player = ClientUtils.getClientPlayer();
                    if (player != null) {
                        player.playSound(ModSounds.SLASH.get(), 1.0F, 1.0F);
                    }
                }));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.startUsingItem(hand);
            if (level instanceof ServerLevel serverLevel) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "draw", "draw");
                // 发送网络包，开始播放 pull_standby 音效
                ServerSound.sendToServer(new ServerSound(ServerSound.SoundType.START_STANDBY));
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int ticksRemaining) {
        if (shooter instanceof Player player) {
            // 发送网络包，停止播放 pull_standby 音效
            ServerSound.sendToServer(new ServerSound(ServerSound.SoundType.STOP_STANDBY));
            if (stack.getDamageValue() >= stack.getMaxDamage() - 1)
                return;

            player.getCooldowns().addCooldown(this, 30);
            if (!level.isClientSide) {
                float yaw = player.getYRot();
                float pitch = player.getXRot();
                float laserVelocity = 2f;

                double xSpeed = -Mth.sin(yaw * (float) Math.PI / 180.0F) * Mth.cos(pitch * (float) Math.PI / 180.0F) * laserVelocity;
                double ySpeed = -Mth.sin(pitch * (float) Math.PI / 180.0F) * laserVelocity;
                double zSpeed = Mth.cos(yaw * (float) Math.PI / 180.0F) * Mth.cos(pitch * (float) Math.PI / 180.0F) * laserVelocity;

                float chargeTime = (float) (this.getUseDuration(stack) - ticksRemaining) / 20.0F;

                LaserBeamEntity laserBeam = new LaserBeamEntity(level, player, ModParticles.AONICX_PARTICLE.get(), chargeTime, stack);
                laserBeam.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
                laserBeam.setDeltaMovement(xSpeed, ySpeed, zSpeed);
                laserBeam.damage = 9.0D;

                level.addFreshEntity(laserBeam);
                ((ServerLevel) player.level()).playSound(null, player.blockPosition(), ModSounds.SONICARROW_SHOOT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(player.getUsedItemHand()));

                if (stack.isEnchanted() && stack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
                    laserBeam.setSecondsOnFire(5);
                }
                if (stack.isEnchanted() && stack.getEnchantmentLevel(Enchantments.POWER_ARROWS) > 0) {
                    laserBeam.damage += stack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
                }

                triggerAnim(shooter, GeoItem.getOrAssignId(stack, (ServerLevel) shooter.level()), "pullback", "pullback");
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("item.sonicarrow.ammo",
                        stack.getMaxDamage() - stack.getDamageValue() - 1,
                        stack.getMaxDamage() - 1)
                .withStyle(ChatFormatting.ITALIC));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 每隔 2 秒检查一次标识符，播放 pull_standby 音效
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && ServerSound.isPlayingStandbySound()) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                if (isHoldingSonicArrow(player)) {
                    // 调用 PullSounds 类中的方法播放 pull_standby 音效
                    PullSounds.playPullStandbySound(player);
                }
            }
        }
    }


    private static boolean isHoldingSonicArrow(Player player) {
        return player.getMainHandItem().getItem() instanceof sonicarrow ||
               player.getOffhandItem().getItem() instanceof sonicarrow;
    }
}