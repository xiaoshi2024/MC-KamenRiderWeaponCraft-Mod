package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ghost;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/**
 * 伟人魂实体类
 * 用于渲染Ghost骑士的伟人魂技能模型
 */
public class GhostHeroicSoulEntity extends Projectile implements GeoEntity {
    // 动画常量定义
    private static final RawAnimation MOVE_ANIMATION = RawAnimation.begin().thenPlay("move");
    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("attack");

    // 动画实例缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 实体存活时间（刻）
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 100; // 约5秒

    // 攻击方向
    private Vec3 attackDirection;
    
    // 灵魂颜色
    private int soulColor;
    
    // 伤害值
    private float damage;
    
    // 是否为火属性
    private boolean isFireDamage;
    
    // 平衡相关属性
    private int attackCooldown = 0; // 攻击冷却
    private static final int DEFAULT_ATTACK_COOLDOWN = 10; // 默认攻击冷却为10tick（0.5秒）
    private int health = 20; // 生命值，20为10颗心
    private static final int MAX_HEALTH = 20; // 最大生命值
    private int postAttackLifetime = -1; // 攻击后的剩余生存时间，-1表示未开始计时
    private static final int DEFAULT_POST_ATTACK_LIFETIME = 40; // 攻击后默认生存2秒（40tick）

    // 存储owner的UUID
    private UUID ownerUUID = null;

    // 同步数据定义
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(GhostHeroicSoulEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ATTACKED = SynchedEntityData.defineId(GhostHeroicSoulEntity.class, EntityDataSerializers.BOOLEAN);

    // 构造函数
    public GhostHeroicSoulEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }
    
    // 移除属性相关的静态方法
    
    // 私有构造函数，用于生成新的特效实体，支持所有LivingEntity
    private GhostHeroicSoulEntity(Level level, LivingEntity owner, Vec3 direction, int color, float damage, boolean isFireDamage) {
        super(ModEntityTypes.GHOST_HEROIC_SOUL.get(), level);
        this.setOwner(owner);
        this.noPhysics = true;
        this.setPos(owner.getEyePosition().add(direction.scale(1.0)));
        this.attackDirection = direction;
        this.soulColor = color;
        // 降低伤害值以平衡游戏
        this.damage = damage * 0.75F; // 降低25%的伤害
        this.isFireDamage = isFireDamage;
        this.setOwnerUUID(owner.getUUID());
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        this.entityData.set(COLOR, color);
        this.entityData.set(ATTACKED, false);
        this.setSoulType("MUSASHI");
        this.health = MAX_HEALTH;
    }

    // 数据定义
    // 灵魂类型数据访问器
    private static final EntityDataAccessor<String> SOUL_TYPE = 
            SynchedEntityData.defineId(GhostHeroicSoulEntity.class, EntityDataSerializers.STRING);


    // 获取/设置同步属性的便捷方法
    public int getSoulColor() {
        return this.entityData.get(COLOR);
    }

    public boolean hasAttacked() {
        return this.entityData.get(ATTACKED);
    }

    public void setAttacked(boolean value) {
        this.entityData.set(ATTACKED, value);
    }
    
    public String getSoulType() {
        return this.entityData.get(SOUL_TYPE);
    }
    
    public void setSoulType(String type) {
        this.entityData.set(SOUL_TYPE, type);
    }

    // 获取owner UUID
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    // 设置owner UUID
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    // 生成实体的静态方法，支持所有LivingEntity
    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, int color, float damage, boolean isFireDamage, String soulType) {
        if (!level.isClientSide) {
            GhostHeroicSoulEntity soulEntity = new GhostHeroicSoulEntity(level, owner, direction, color, damage, isFireDamage);
            soulEntity.setSoulType(soulType);
            level.addFreshEntity(soulEntity);
        }
    }
    
    // 保持向后兼容性的重载方法
    public static void trySpawnEffect(Level level, Player player, Vec3 direction, int color, float damage, boolean isFireDamage, String soulType) {
        trySpawnEffect(level, (LivingEntity) player, direction, color, damage, isFireDamage, soulType);
    }

    @Override
    public void tick() {
        super.tick();
        lifetime++;

        // 更新追踪冷却
        if (trackingCooldown > 0) {
            trackingCooldown--;
        }
        
        // 更新攻击冷却
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        
        // 攻击后的生命周期管理
        if (hasAttacked() && postAttackLifetime == -1) {
            // 攻击后开始倒计时
            postAttackLifetime = DEFAULT_POST_ATTACK_LIFETIME;
        }
        
        // 减少攻击后的剩余生存时间
        if (postAttackLifetime > 0) {
            postAttackLifetime--;
        }

        // 追踪目标逻辑 - 这是关键，之前没有调用这个方法
        updateTrackingTarget();

        // 实体生命周期管理
        // 1. 如果生命值为0，实体消失
        if (health <= 0) {
            this.remove(RemovalReason.KILLED);
            return;
        }
        
        // 2. 如果已经攻击过并且倒计时结束，实体消失（无论是否击败目标）
        if (postAttackLifetime == 0) {
            this.remove(RemovalReason.KILLED);
            return;
        }
        
        // 3. 如果没有攻击过，且存活时间超过最大生命周期，实体消失
        if (!hasAttacked() && lifetime > MAX_LIFETIME) {
            this.remove(RemovalReason.KILLED);
            return;
        }

        // 移动实体 - 优先追踪目标移动
        if (trackingTarget != null && trackingTarget.isAlive()) {
            // 追踪目标移动
            Vec3 targetPos = trackingTarget.position().add(0, trackingTarget.getBbHeight() / 2, 0);
            Vec3 entityPos = this.position();
            
            // 计算朝向目标的方向向量并归一化
            Vec3 direction = targetPos.subtract(entityPos).normalize();
            
            // 应用平滑移动 - 稍微降低移动速度以平衡
            Vec3 moveVec = direction.scale(0.2);
            this.setDeltaMovement(moveVec);
            this.move(MoverType.SELF, this.getDeltaMovement());
            
            // 旋转实体朝向目标
            this.lookAt(targetPos);
        } else if (attackDirection != null) {
            // 如果没有追踪目标，使用攻击方向
            Vec3 moveVec = attackDirection.scale(0.2);
            this.setDeltaMovement(moveVec);
            this.move(MoverType.SELF, this.getDeltaMovement());
        } else {
            // 如果攻击方向为null，设置一个默认向上的方向
            this.setDeltaMovement(new Vec3(0, 0.05, 0));
            this.move(MoverType.SELF, this.getDeltaMovement());
        }

        // 生成粒子效果 - 优化性能，减少每tick的粒子数量
        if (this.level().isClientSide) {
            // 每2个tick生成一次粒子，减少性能消耗
            if (lifetime % 2 == 0) {
                double offsetX = (random.nextDouble() - 0.5) * 0.5;
                double offsetY = (random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (random.nextDouble() - 0.5) * 0.5;
                
                // 根据灵魂颜色生成不同颜色的粒子
                this.level().addParticle(ParticleTypes.ENTITY_EFFECT, 
                        this.getX() + offsetX, 
                        this.getY() + 0.5 + offsetY, 
                        this.getZ() + offsetZ, 
                        ((soulColor >> 16) & 0xFF) / 255.0, 
                        ((soulColor >> 8) & 0xFF) / 255.0, 
                        (soulColor & 0xFF) / 255.0);
            }
        } else {
            // 服务器端：检测并攻击目标 - 添加攻击冷却
            checkAndAttackEntities();
            
            // 主动让敌对生物将伟人魂视为攻击目标
            // 由于Projectile类不能直接被设置为Mob的目标，我们需要改变策略
            // 我们通过修改敌对生物的AI来模拟这个行为
            if (lifetime % 20 == 0) { // 每1秒执行一次，避免性能问题
                makeHostilesAttackThisEntity();
            }
        }
    }
    
    // 让实体朝向目标位置
    private void lookAt(Vec3 targetPos) {
        Vec3 entityPos = this.position();
        double dx = targetPos.x - entityPos.x;
        double dz = targetPos.z - entityPos.z;
        
        // 计算水平方向的角度
        double yaw = Math.atan2(dz, dx) * (180 / Math.PI) - 90.0;
        
        // 设置实体的旋转角度
        this.setYRot((float) yaw);
        this.setYBodyRot((float) yaw);
        this.setYHeadRot((float) yaw);
    }

    // 追踪目标相关字段
    private LivingEntity trackingTarget = null;
    private int trackingCooldown = 0;
    
    // 更新追踪目标
    private void updateTrackingTarget() {
        // 如果冷却中，不更新目标
        if (trackingCooldown > 0) {
            // 检查当前目标是否仍然有效
            if (trackingTarget != null && (!trackingTarget.isAlive() || 
                    trackingTarget.distanceToSqr(this) > 25.0)) {
                trackingTarget = null;
            }
            trackingCooldown--;
            return;
        }
        
        double searchRange = 10.0; // 扩大搜索范围到10格
        Player owner = (Player) getOwner();
        
        // 查找最近的敌对生物或敌对玩家
        LivingEntity nearestTarget = this.level().getEntitiesOfClass(LivingEntity.class, 
                this.getBoundingBox().inflate(searchRange),
                e -> e != owner && e.isAlive() && isHostileTarget(e, owner))
                .stream()
                .min(java.util.Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
        
        // 如果找到新目标，设置并重置冷却
        if (nearestTarget != null && nearestTarget != trackingTarget) {
            trackingTarget = nearestTarget;
            trackingCooldown = 20; // 20tick冷却（1秒）
        }
    }
    
    // 检测并攻击附近实体 - 添加攻击冷却以平衡
    private void checkAndAttackEntities() {
        // 如果攻击冷却中，不进行攻击
        if (attackCooldown > 0) {
            return;
        }
        
        double range = 2.0;
        Player owner = (Player) getOwner();
        
        // 优先攻击追踪目标
        if (trackingTarget != null && trackingTarget.isAlive() && this.distanceToSqr(trackingTarget) <= range * range) {
            // 造成伤害
            trackingTarget.hurt(this.level().damageSources().playerAttack(owner), damage);
            
            // 如果是火属性，设置目标着火
            if (isFireDamage) {
                trackingTarget.setSecondsOnFire(5);
            }
            
            // 设置攻击冷却
            attackCooldown = DEFAULT_ATTACK_COOLDOWN;
            
            // 设置为已攻击状态（用于动画）
            setAttacked(true);
            return;
        }
        
        // 如果没有追踪目标或目标太远，则攻击范围内的第一个实体
        this.level().getEntitiesOfClass(LivingEntity.class, 
                this.getBoundingBox().inflate(range),
                e -> e != owner && e.isAlive() && isHostileTarget(e, owner))
                .stream().findFirst().ifPresent(entity -> {
                    // 造成伤害
                    entity.hurt(this.level().damageSources().playerAttack(owner), damage);
                    
                    // 如果是火属性，设置目标着火
                    if (isFireDamage) {
                        entity.setSecondsOnFire(5);
                    }
                    
                    // 设置这个实体为新的追踪目标
                    trackingTarget = entity;
                    
                    // 设置攻击冷却
                    attackCooldown = DEFAULT_ATTACK_COOLDOWN;
                    
                    // 设置为已攻击状态（用于动画）
                    setAttacked(true);
                });
    }
    
    // 自定义护甲属性
    private double armor = 12.0D; // 护甲值
    private double armorToughness = 6.0D; // 护甲韧性
    private double knockbackResistance = 0.2D; // 击退抗性
    
    // 允许实体受到伤害
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 只有敌对实体的伤害才有效
        if (source.getEntity() != null && source.getEntity() instanceof LivingEntity) {
            // 计算护甲减免
            float damageAfterArmor = calculateDamageAfterArmor(amount);
            
            // 扣除生命值
            this.health -= damageAfterArmor;
            
            // 如果生命值低于等于0，实体消失
            if (this.health <= 0) {
                this.remove(RemovalReason.KILLED);
                return true;
            }
            
            // 显示受伤粒子效果
            if (this.level().isClientSide) {
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.CRIT, 
                            this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + this.random.nextDouble() * this.getBbHeight(),
                            this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            0, 0, 0);
                }
            }
            
            return true;
        }
        return super.hurt(source, amount);
    }
    
    // 计算护甲减免后的伤害
    private float calculateDamageAfterArmor(float damage) {
        // 护甲减免计算公式
        float reduction = (float)Math.min(0.8F, armor * 0.04F);
        float effectiveDamage = damage * (1.0F - reduction);
        
        return effectiveDamage;
    }
    
    // 确保实体可以被选择和攻击
    @Override
    public boolean isPickable() {
        return true;
    }
    
    // 确保实体可以被攻击
    @Override
    public boolean isAttackable() {
        return true;
    }
    
    // 确保实体可以被敌对生物视为目标
    @Override
    public boolean isAlive() {
        return super.isAlive();
    }
    
    // 确保敌对生物会攻击这个实体
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // 只有特定的伤害类型（如创造模式伤害）才免疫
        if (source.isCreativePlayer()) {
            return true;
        }
        return false;
    }
    
    // 使敌对生物能够攻击伟人魂的关键方法 - 确保实体是可交互的
    
    // 确保实体被正确注册为可攻击目标
    @Override
    public boolean isPushable() {
        return true;
    }
    
    // 添加吸引敌对生物的功能到tick方法中
    
    // 让敌对生物将伟人魂视为攻击目标
    private void makeHostilesAttackThisEntity() {
        double range = 15.0; // 扩大范围到15格
        final UUID thisUUID = this.getUUID();
        Player owner = (Player) this.getOwner();
        
        // 获取所有可能的敌对生物
        this.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, 
                this.getBoundingBox().inflate(range),
                e -> e.isAlive())
                .stream()
                .forEach(mob -> {
                    // 使用UUID比较，避免类型不兼容问题
                    if (thisUUID.equals(mob.getUUID())) {
                        return; // 跳过自己
                    }
                    
                    // 让敌对生物朝伟人魂的方向移动
                    if (mob.getNavigation() != null) {
                        // 使用更高的优先级移动到伟人魂
                        mob.getNavigation().moveTo(this.getX(), this.getY(), this.getZ(), 0.8);
                    }
                    
                    // 额外的AI行为：如果距离足够近，尝试模拟攻击
                    if (mob.distanceToSqr(this) < 4.0) {
                        // 使敌对生物看向伟人魂
                        mob.getLookControl().setLookAt(this.getX(), this.getY(), this.getZ());
                        
                        // 如果敌对生物有攻击冷却相关的方法，尝试重置它
                        try {
                            // 反射调用可能的方法，或使用其他方式触发攻击行为
                            // 这里我们简单地让敌对生物表现出攻击意图
                            mob.getNavigation().stop();
                            
                            // 模拟攻击延迟后再移动
                            mob.setNoActionTime(10);
                        } catch (Exception e) {
                            // 忽略任何异常，确保代码不会崩溃
                        }
                    }
                });
                
        // 特别处理Monster类的生物（大多数敌对生物）
        this.level().getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, 
                this.getBoundingBox().inflate(range),
                e -> e.isAlive())
                .stream()
                .forEach(monster -> {
                    if (thisUUID.equals(monster.getUUID())) {
                        return; // 跳过自己
                    }
                    
                    // 对Monster类型使用额外的吸引方法
                    if (monster.getNavigation() != null) {
                        // 使用较高的速度
                        monster.getNavigation().moveTo(this, 0.85);
                    }
                    
                    // 使怪物看向我们
                    monster.getLookControl().setLookAt(this);
                });
                
        // 确保伟人魂可以被攻击（关键属性设置）
        // 这些属性已经在其他方法中设置，但我们再次确认
    }
    
    // 判断目标是否为敌对目标
    private boolean isHostileTarget(LivingEntity target, LivingEntity owner) {
        if (owner == null) {
            return false;
        }
        
        // 1. 检查是否为标准敌对怪物类别
        if (target.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER) {
            return true;
        }
        
        // 2. 对于玩家类型owner的特殊处理
        if (owner instanceof Player player) {
            // 检查是否为敌对玩家
            if (target instanceof Player) {
                Player targetPlayer = (Player) target;
                
                // 不同队伍的玩家视为敌对
                if (player.getTeam() != null && targetPlayer.getTeam() != null) {
                    if (!player.getTeam().isAlliedTo(targetPlayer.getTeam())) {
                        return true;
                    }
                }
                
                // 在PvP服务器上，非同一队伍的玩家视为敌对
                return player.canHarmPlayer(targetPlayer);
            }
            
            // 3. 检查实体是否伤害过玩家
            if (target.getLastHurtMob() == player) {
                return true;
            }
            
            // 4. 特殊处理：对于Mob类型，检查是否将玩家作为目标
            if (target instanceof net.minecraft.world.entity.Mob) {
                net.minecraft.world.entity.Mob mob = (net.minecraft.world.entity.Mob) target;
                if (mob.getTarget() != null && mob.getTarget() instanceof Player) {
                    return true;
                }
            }
        }
        // 3. 对于非玩家生物，使用其原生的攻击判断
        else if (owner instanceof net.minecraft.world.entity.Mob mobOwner) {
            return mobOwner.canAttack(target) && target.isAlive();
        }
        
        // 4. 检查是否为实体类别的敌对生物
        String entityTypeName = target.getType().toString();
        if (entityTypeName.contains("hostile") || 
            entityTypeName.contains("enemy") || 
            entityTypeName.contains("boss") || 
            entityTypeName.contains("evil") || 
            entityTypeName.contains("demon") ||
            entityTypeName.contains("monster")) {
            return true;
        }
        
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        // 降低控制器的更新频率阈值，从5降到3，提高动画流畅度
        controllerRegistrar.add(new AnimationController<>(this, "controller", 3, this::predicate));
    }

    private PlayState predicate(AnimationState<GhostHeroicSoulEntity> state) {
        // 根据实体状态选择动画
        if (hasAttacked()) {
            state.setAnimation(ATTACK_ANIMATION);
        } else {
            state.setAnimation(MOVE_ANIMATION);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void onHitEntity(net.minecraft.world.phys.EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        // 攻击实体时的额外效果
        if (entity instanceof LivingEntity livingEntity && livingEntity != getOwner()) {
            setAttacked(true);
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(COLOR, 0xFFFFFF);
        this.entityData.define(ATTACKED, false);
        this.entityData.define(SOUL_TYPE, "MUSASHI");
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // 确保不攻击自己
        return super.canHitEntity(entity) && entity != this.getOwner();
    }
}