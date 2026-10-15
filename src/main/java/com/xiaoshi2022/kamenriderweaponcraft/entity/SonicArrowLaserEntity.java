package com.xiaoshi2022.kamenriderweaponcraft.entity;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.SonicArrowItem;
import com.xiaoshi2022.kamenriderweaponcraft.event.SonicArrowBleedingHandler;
import com.xiaoshi2022.kamenriderweaponcraft.network.SonicArrowLaserPacket;
import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * 音速弓激光实体（移植自 1.20.1 LaserBeamEntity，渲染方式改为变形枪式雷电弧）
 * 实体本身不渲染，由 SonicArrowLaserPacket 在客户端生成雷电弧特效
 */
public class SonicArrowLaserEntity extends ThrowableItemProjectile {
    private static final int MAX_LIFETIME = 40;          // 激光束最大存活时间（tick）
    private static final int IGNITE_SECONDS = 5;         // 火矢附魔点燃时间（秒）
    private static final double RENDER_DISTANCE_SQ = 64.0 * 64.0;
    /** 蓄力加伤：每蓄力 1 秒伤害 +25%，上限翻倍（蓄满 4 秒） */
    private static final float CHARGE_DAMAGE_BONUS = 0.25F;
    private static final float CHARGE_DAMAGE_CAP = 2.0F;

    private final Vec3 startPos;
    private final double damage;
    private final float chargeTime;
    private final SonicArrowItem.Mode mode;
    private final int mainColor;
    private final int coreColor;
    private final ItemStack weaponStack;
    private boolean isCritical = false;
    private int lifeTicks = 0;

    /** 实体注册用构造函数 */
    public SonicArrowLaserEntity(EntityType<? extends SonicArrowLaserEntity> type, Level level) {
        super(type, level);
        this.startPos = Vec3.ZERO;
        this.damage = 9.0D;
        this.chargeTime = 0F;
        this.mode = SonicArrowItem.Mode.DEFAULT;
        this.mainColor = 0xFFFFFFFF;
        this.coreColor = 0xFFEAEAFF;
        this.weaponStack = ItemStack.EMPTY;
        this.setNoGravity(true);
    }

    /** 发射用构造函数 */
    public SonicArrowLaserEntity(Level level, LivingEntity shooter, double damage, float chargeTime,
                                 SonicArrowItem.Mode mode, ItemStack weaponStack, int mainColor, int coreColor) {
        super(EntityRegister.SONIC_ARROW_LASER.get(), shooter, level);
        // 起点取眼睛高度（弓的持握位置），客户端再由箭尖骨骼精确修正
        this.startPos = new Vec3(shooter.getX(), shooter.getEyeY(), shooter.getZ());
        this.damage = damage;
        this.chargeTime = chargeTime;
        this.mode = mode == null ? SonicArrowItem.Mode.DEFAULT : mode;
        this.weaponStack = weaponStack == null ? ItemStack.EMPTY : weaponStack;
        this.mainColor = mainColor;
        this.coreColor = coreColor;

        // 用 AIR 占位，避免默认把箭/武器渲染成旋转抛射物（特效完全由数据包驱动的雷电弧承担）
        this.setItem(new ItemStack(Items.AIR));
        this.setNoGravity(true);

        // 蓄力暴击几率（每蓄力1秒+10%）
        Random random = new Random();
        if (random.nextFloat() < chargeTime * 0.1F) {
            this.isCritical = true;
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AIR; // 不渲染任何物品
    }

    @Override
    public void tick() {
        super.tick();
        this.lifeTicks++;

        if (this.lifeTicks >= MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (level().isClientSide()) {
            // 电火花粒子
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        getX() + (random.nextDouble() - 0.5) * 0.4,
                        getY() + (random.nextDouble() - 0.5) * 0.4,
                        getZ() + (random.nextDouble() - 0.5) * 0.4,
                        (random.nextDouble() - 0.5) * 0.3,
                        (random.nextDouble() - 0.5) * 0.3,
                        (random.nextDouble() - 0.5) * 0.3);
            }
        } else if (this.lifeTicks % 3 == 0 && this.lifeTicks < 20) {
            // 飞行途中持续发送雷电弧渲染包（前 20 tick，每 3 tick 一次）
            if (level() instanceof ServerLevel serverLevel) {
                sendThunderPacket(serverLevel, this.position());
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        // 注意：这里【不能】调用 super.onHit(result)。
        // ThrowableItemProjectile.onHit 会分派到 onHitEntity，按其自身逻辑用 getItem()
        // 再结算一次伤害并 discard，导致目标受到双重伤害。
        if (level().isClientSide()) return;

        Vec3 hitPos = result.getLocation();

        // 命中时发送最后一道完整雷电弧
        if (level() instanceof ServerLevel serverLevel) {
            sendThunderPacket(serverLevel, hitPos);
        }

        if (result instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
            applyDamage(living);
        }

        this.discard();
    }

    private void applyDamage(LivingEntity target) {
        DamageSource damageSource;
        Entity owner = this.getOwner();
        if (owner instanceof Player player) {
            damageSource = this.damageSources().playerAttack(player);
        } else if (owner instanceof LivingEntity living) {
            damageSource = this.damageSources().mobAttack(living);
        } else {
            damageSource = this.damageSources().generic();
        }

        float damageAmount = (float) this.damage;

        // LEMON 形态：额外 1 点伤害（与近战一致）
        if (this.mode == SonicArrowItem.Mode.LEMON) {
            damageAmount += 1.0F;
        }

        // 蓄力加伤：每蓄力 1 秒 +25%，上限翻倍
        damageAmount *= Math.min(CHARGE_DAMAGE_CAP, 1.0F + this.chargeTime * CHARGE_DAMAGE_BONUS);

        if (this.isCritical) {
            damageAmount *= 1.5F; // 暴击增加50%伤害
        }

        // 火矢附魔：点燃 5 秒
        if (!this.weaponStack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(flameHolder(), this.weaponStack) > 0) {
            target.igniteForSeconds(IGNITE_SECONDS);
        }

        // 冲击附魔：击退
        int punchLevel = this.weaponStack.isEmpty() ? 0
                : EnchantmentHelper.getItemEnchantmentLevel(punchHolder(), this.weaponStack);
        if (punchLevel > 0 && owner != null) {
            float knockbackStrength = 0.5F * punchLevel;
            double dx = owner.getX() - target.getX();
            double dz = owner.getZ() - target.getZ();
            double length = Math.sqrt(dx * dx + dz * dz);
            if (length > 0) {
                dx /= length;
                dz /= length;
            }
            target.knockback(knockbackStrength, dx, dz);
        }

        target.hurt(damageSource, damageAmount);

        // 形态特效（与近战一致：击飞/流血/治疗/燃烧，LEMON 已在伤害段加成）
        applyModeEffect(target, owner);
    }

    /** 远程命中附带对应锁种形态的特效 */
    private void applyModeEffect(LivingEntity target, Entity owner) {
        switch (this.mode) {
            case ORANGE -> applyKnockback(target, owner, 1.0F);   // 击飞 1.0
            case MELON -> applyKnockback(target, owner, 1.2F);    // 强力击飞 1.2
            case CHERRY -> SonicArrowBleedingHandler.applyBleeding(target); // 流血 3 秒
            case PEACH -> {                                        // 治疗攻击者 2.0
                if (owner instanceof LivingEntity shooter) {
                    shooter.heal(2.0F);
                }
            }
            case DRAGON -> target.igniteForSeconds(10);            // 燃烧 10 秒
            default -> { }
        }
    }

    /** 从射击者方向击飞目标（原版 knockback 语义：传入击退来源点坐标） */
    private void applyKnockback(LivingEntity target, Entity owner, float strength) {
        double sourceX = owner != null ? owner.getX() : this.getX();
        double sourceZ = owner != null ? owner.getZ() : this.getZ();
        target.knockback(strength, sourceX, sourceZ);
    }

    /** 1.21.1 附魔常量是 ResourceKey，经注册表换取 Holder */
    private net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> flameHolder() {
        return this.level().registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FLAME);
    }

    private net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> punchHolder() {
        return this.level().registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PUNCH);
    }

    private void sendThunderPacket(ServerLevel serverLevel, Vec3 end) {
        SonicArrowLaserPacket payload = new SonicArrowLaserPacket(
                this.startPos.x, this.startPos.y, this.startPos.z,
                end.x, end.y, end.z, this.mainColor, this.coreColor);
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(end.x, end.y, end.z) < RENDER_DISTANCE_SQ) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}