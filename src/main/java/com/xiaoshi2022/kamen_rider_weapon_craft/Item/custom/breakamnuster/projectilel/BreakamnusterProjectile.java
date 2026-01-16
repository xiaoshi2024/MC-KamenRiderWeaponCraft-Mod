package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.breakamnuster.projectilel;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.breakamnuster.BreakamnusterGun;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class BreakamnusterProjectile extends ThrowableItemProjectile {
    private static final float PROJECTILE_SPEED = 2.5f; // 弹丸速度
    private final int range; // 最大射程
    private final float chargeRatio; // 蓄力比例

    public BreakamnusterProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
        this.range = 50;
        this.chargeRatio = 1.0f;
    }

    public BreakamnusterProjectile(EntityType<? extends ThrowableItemProjectile> type, LivingEntity shooter, Level level, int range, float chargeRatio) {
        super(type, shooter, level);
        this.range = range;
        this.chargeRatio = chargeRatio;
        // 设置弹丸速度和方向
        Vec3 look = shooter.getLookAngle();
        this.shoot(look.x, look.y, look.z, PROJECTILE_SPEED, 0.0f);
    }

    @Override
    protected Item getDefaultItem() {
        // 返回一个物品作为弹丸的显示（可以自定义）
        return null;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        // 命中实体时的效果
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity livingEntity) {
            // 应用BreakamnusterGun的击中效果
            // 静态方法调用，无需创建实例
            BreakamnusterGun.applyHitEffectsStatic(livingEntity, this.chargeRatio);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // 命中方块时的效果
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            // 传递chargeRatio参数
            BreakamnusterGun.eraseBlock(serverLevel, result.getBlockPos(), this.chargeRatio);
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        // 添加粒子效果（弹丸轨迹）
        if (this.level().isClientSide) {
            for (int i = 0; i < 2; i++) {
                double dx = this.getX() + (this.random.nextDouble() - 0.5) * 0.1;
                double dy = this.getY() + (this.random.nextDouble() - 0.5) * 0.1;
                double dz = this.getZ() + (this.random.nextDouble() - 0.5) * 0.1;
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        dx, dy, dz,
                        0, 0, 0);
                this.level().addParticle(ParticleTypes.GLOW,
                        dx, dy, dz,
                        0, 0, 0);

                // 根据蓄力添加额外粒子
                if (this.chargeRatio > 0.5f) {
                    this.level().addParticle(ParticleTypes.FIREWORK,
                            dx, dy, dz,
                            0, 0, 0);
                }
            }
        }

        // 检查是否超出射程
        if (this.tickCount > this.range * 5) { // 根据速度估算ticks
            if (!this.level().isClientSide) {
                this.discard();
            }
        }
    }
}