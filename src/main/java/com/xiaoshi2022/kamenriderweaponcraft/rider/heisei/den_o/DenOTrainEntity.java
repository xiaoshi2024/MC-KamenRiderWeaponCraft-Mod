package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.den_o;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DenOTrainEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DenOTrainEntity(EntityType<? extends DenOTrainEntity> type, Level level) {
        super(type, level);
        this.weaponType = "Sword";
    }

    private final String weaponType;
    private boolean attachedToWeapon = false;
    private Player attachedPlayer = null;
    private Vec3 direction;
    private float damage;
    private float speed = 1.5f;
    private int maxLifeTicks = 100;
    private boolean isContinuous = false;
    private LivingEntity owner;

    public DenOTrainEntity(Level level, LivingEntity shooter, Vec3 direction, float damage, String weaponType) {
        super(EntityRegister.DEN_O_TRAIN.get(), level);
        this.owner = shooter;
        this.direction = direction;
        this.damage = damage;
        this.weaponType = weaponType;
    }

    public static DenOTrainEntity spawn(ServerLevel level, LivingEntity shooter, Vec3 direction, float damage, String weaponType) {
        if ("Sword".equals(weaponType)) {
            for (DenOTrainEntity existingEntity : level.getEntitiesOfClass(DenOTrainEntity.class,
                    shooter.getBoundingBox().inflate(30.0),
                    e -> e.getOwner() == shooter && "Sword".equals(e.getWeaponType()) && !e.isAttachedToWeapon())) {
                existingEntity.discard();
            }
        }

        DenOTrainEntity entity = new DenOTrainEntity(level, shooter, direction, damage, weaponType);

        if (shooter instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof Heiseisword) {
                Heiseisword heiseisword = (Heiseisword) mainHand.getItem();
                String denOWeaponType = heiseisword.getDenOWeaponType(mainHand);

                if (denOWeaponType != null && denOWeaponType.equals(weaponType)) {
                    entity.attachToWeapon(player);
                    return entity;
                }
            }
        }

        Vec3 startPos = shooter.getEyePosition(1.0f).add(direction.normalize().scale(0.5));
        entity.setPos(startPos.x, startPos.y, startPos.z);

        level.addFreshEntity(entity);
        return entity;
    }

    public void attachToWeapon(Player player) {
        this.attachedToWeapon = true;
        this.attachedPlayer = player;
        this.damage = 0;
        this.speed = 0;

        this.setInvulnerable(true);

        if (!level().isClientSide) {
            this.setSharedFlag(6, true);
        }
    }

    public void detachFromWeapon() {
        this.attachedToWeapon = false;
        this.attachedPlayer = null;
        this.setInvulnerable(false);
        if (!level().isClientSide) {
            this.setSharedFlag(6, false);
        }

        this.discard();
    }

    private void attackTarget(LivingEntity target) {
        switch (weaponType) {
            case "Sword":
                target.hurt(this.level().damageSources().thrown(this, owner), damage * 1.2f);
                target.hurt(this.level().damageSources().thrown(this, owner), damage * 1.2f);

                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));

                if (owner instanceof Player player && !level().isClientSide) {
                    ItemStack mainHand = player.getMainHandItem();
                    if (mainHand.getItem() instanceof Heiseisword heiseisword) {
                        heiseisword.triggerAnimationForPlayer(player, mainHand);
                    }
                }
                break;
            case "FishingRod":
                target.hurt(this.level().damageSources().thrown(this, owner), damage * 0.5f);

                if (target instanceof WaterAnimal) {
                    if (!level().isClientSide && owner instanceof Player player) {
                        ItemStack fishItem = getFishItem(target);
                        if (!fishItem.isEmpty()) {
                            if (!player.getInventory().add(fishItem)) {
                                player.drop(fishItem, false);
                            }
                            target.discard();
                        }
                    }
                } else {
                    Vec3 pullDir = owner.position().subtract(target.position()).normalize();
                    target.setDeltaMovement(target.getDeltaMovement().add(pullDir.scale(1.5)));
                }

                if (owner instanceof Player player && !level().isClientSide) {
                    ItemStack mainHand = player.getMainHandItem();
                    if (mainHand.getItem() instanceof Heiseisword heiseisword) {
                        heiseisword.triggerAnimationForPlayer(player, mainHand);
                    }
                }
                break;
            case "Ax":
                target.hurt(this.level().damageSources().thrown(this, owner), damage * 1.2f);
                target.setRemainingFireTicks(60);
                break;
            case "Gun":
                target.hurt(this.level().damageSources().thrown(this, owner), damage * 0.4f);
                break;
        }
    }

    public String getWeaponType() {
        return weaponType;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    private ItemStack getFishItem(LivingEntity entity) {
        return new ItemStack(Items.COD);
    }

    @Override
    public void tick() {
        super.tick();

        if (attachedToWeapon) {
            handleAttachedToWeaponTick();
            return;
        }

        if (direction != null) {
            this.setDeltaMovement(direction.scale(speed));
            this.setPos(this.position().add(this.getDeltaMovement()));
        } else {
            this.setDeltaMovement(new Vec3(0, 0.1, 0));
            this.setPos(this.position().add(this.getDeltaMovement()));
        }

        if (maxLifeTicks-- <= 0 || this.isInWaterOrRain() || this.isInLava()) {
            this.discard();
        }

        if (level().isClientSide) return;

        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(1.5),
                e -> e != owner && e.isAlive())) {

            attackTarget(entity);

            if (!isContinuous) {
                this.discard();
            }
        }
    }

    private void handleAttachedToWeaponTick() {
        if (attachedPlayer == null || attachedPlayer.isRemoved()) {
            detachFromWeapon();
            return;
        }

        ItemStack mainHand = attachedPlayer.getMainHandItem();
        if (!(mainHand.getItem() instanceof Heiseisword) ||
                !((Heiseisword)mainHand.getItem()).hasAttachedEntity(mainHand)) {
            detachFromWeapon();
            return;
        }

        if (level().isClientSide) {
        }
    }

    public boolean isAttachedToWeapon() {
        return attachedToWeapon;
    }

    public Player getAttachedPlayer() {
        return attachedPlayer;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // 不定义任何数据
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}