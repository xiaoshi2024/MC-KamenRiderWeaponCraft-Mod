package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.den_o;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
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
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DenOTrainEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    // 保留原构造函数以保持兼容性
    public DenOTrainEntity(EntityType<? extends DenOTrainEntity> type, Level level) {
        super(type, level);
        this.weaponType = "Sword"; // 默认值
    }

    // 存储武器类型
    private final String weaponType;
    // 是否连接到武器
    private boolean attachedToWeapon = false;
    // 连接的玩家
    private Player attachedPlayer = null;
    private Vec3 direction;
    private float damage;
    private float speed = 1.5f;
    private int maxLifeTicks = 100;
    private boolean isContinuous = false;
    private LivingEntity owner;

    public DenOTrainEntity(Level level, LivingEntity shooter, Vec3 direction, float damage, String weaponType) {
        super(ModEntityTypes.DEN_O_TRAIN.get(), level);
        this.owner = shooter;
        this.direction = direction;
        this.damage = damage;
        this.weaponType = weaponType;
    }

    /**
     * 静态方法，用于生成电王列车实体
     *
     * @return
     */
    public static DenOTrainEntity spawn(ServerLevel level, LivingEntity shooter, Vec3 direction, float damage, String weaponType) {
        // 对于剑形态，检查并移除射击者现有的剑形态发射物
        if ("Sword".equals(weaponType)) {
            // 查找并移除射击者的现有剑形态发射物（包括玩家和非玩家实体）
            for (DenOTrainEntity existingEntity : level.getEntitiesOfClass(DenOTrainEntity.class, 
                    shooter.getBoundingBox().inflate(30.0),
                    e -> e.getOwner() == shooter && "Sword".equals(e.getWeaponType()) && !e.isAttachedToWeapon())) {
                existingEntity.remove(RemovalReason.DISCARDED);
            }
        }
        
        // 创建电王列车实体
        DenOTrainEntity entity = new DenOTrainEntity(level, shooter, direction, damage, weaponType);
        
        // 如果射击者是玩家，并且是电王模式，则设置实体为附着状态
        if (shooter instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof Heiseisword) {
                Heiseisword heiseisword = (Heiseisword) mainHand.getItem();
                String denOWeaponType = heiseisword.getDenOWeaponType(mainHand);
                
                // 如果武器处于电王模式且武器类型匹配，则实体附着到武器
                if (denOWeaponType != null && denOWeaponType.equals(weaponType)) {
                    entity.attachToWeapon(player);
                    return entity;
                }
            }
        }
        
        // 设置实体位置为射击者的眼睛位置加上一点偏移
        Vec3 startPos = shooter.getEyePosition(1.0f).add(direction.normalize().scale(0.5));
        entity.setPos(startPos.x, startPos.y, startPos.z);
        
        // 添加实体到世界
        level.addFreshEntity(entity);
        return entity;
    }

    /**
     * 附着实体到玩家武器
     */
    public void attachToWeapon(Player player) {
        this.attachedToWeapon = true;
        this.attachedPlayer = player;
        this.damage = 0; // 附着状态下不造成伤害
        this.speed = 0; // 附着状态下不移动
        
        // 设置为无敌状态
        this.setInvulnerable(true);
        
        // 设置为客户端渲染优先，不影响服务器逻辑
        if (!level().isClientSide) {
            this.setSharedFlag(6, true); // 设置为隐形
        }
    }
    
    /**
     * 从武器上分离实体
     */
    public void detachFromWeapon() {
        this.attachedToWeapon = false;
        this.attachedPlayer = null;
        this.setInvulnerable(false);
        if (!level().isClientSide) {
            this.setSharedFlag(6, false); // 设置为可见
        }
        
        // 分离后立即移除实体
        this.discard();
    }

    /**
     * 攻击目标
     */
    private void attackTarget(LivingEntity target) {
        // 根据武器类型造成不同的效果
        switch (weaponType) {
            case "Sword":
                // 剑形态：快速连击，提高伤害并添加发光效果
                target.hurt(this.level().damageSources().thrown(this, owner), damage * 1.2f);
                target.hurt(this.level().damageSources().thrown(this, owner), damage * 1.2f);
                
                // 给目标添加发光效果（持续5秒）
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                
                // 剑形态碰撞实体时播放动画
                if (owner instanceof Player player && !level().isClientSide) {
                    ItemStack mainHand = player.getMainHandItem();
                    if (mainHand.getItem() instanceof Heiseisword heiseisword) {
                        // 触发剑形态动画 - 对应Heiseisword.java中的SWORD_FORM_ANIM
                        heiseisword.triggerAnimationForPlayer(player, mainHand);
                    }
                }
                break;
            case "FishingRod":
                // 鱼竿形态：拉回敌人并可以捕获水生物
                target.hurt(this.level().damageSources().thrown(this, owner), damage * 0.5f);
                
                // 如果目标是水生物（如鱼、鱿鱼等），则将其捕获并放置在最近的容器中
                if (target instanceof WaterAnimal) {
                    // 只在服务器端执行
                    if (!level().isClientSide && owner instanceof Player player) {
                        // 尝试捕获水生物
                        ItemStack fishItem = getFishItem(target);
                        if (!fishItem.isEmpty()) {
                            // 尝试将捕获的鱼添加到玩家物品栏
                            if (!player.getInventory().add(fishItem)) {
                                // 如果物品栏已满，将鱼丢到地上
                                player.drop(fishItem, false);
                            }
                            // 移除被捕获的实体
                            target.remove(RemovalReason.DISCARDED);
                        }
                    }
                } else {
                    // 对于非水生物，执行拉回操作
                    Vec3 pullDir = owner.position().subtract(target.position()).normalize();
                    target.setDeltaMovement(target.getDeltaMovement().add(pullDir.scale(1.5)));
                }
                
                // 钓鱼形态碰撞实体时播放动画
                if (owner instanceof Player player && !level().isClientSide) {
                    ItemStack mainHand = player.getMainHandItem();
                    if (mainHand.getItem() instanceof Heiseisword heiseisword) {
                        // 触发钓鱼竿形态动画 - 对应Heiseisword.java中的FISHING_ROD_FORM_ANIM
                        heiseisword.triggerAnimationForPlayer(player, mainHand);
                    }
                }
                break;
            case "Ax":
                // 斧形态：高伤害
                target.hurt(this.level().damageSources().thrown(this, owner), damage * 1.2f);
                target.setSecondsOnFire(3);
                break;
            case "Gun":
                // 枪形态：远程攻击
                target.hurt(this.level().damageSources().thrown(this, owner), damage * 0.4f);
                break;
        }
    }
    
    /**
     * 获取武器类型
     */
    public String getWeaponType() {
        return weaponType;
    }
    
    /**
     * 获取所有者
     */
    public LivingEntity getOwner() {
        return owner;
    }
    
    /**
     * 根据水生物实体返回对应的物品
     */
    private ItemStack getFishItem(LivingEntity entity) {
        // 简化版本，直接返回鱼或相应物品
        return new ItemStack(Items.COD); // 默认返回鳕鱼
    }

    @Override
    public void tick() {
        super.tick();
        
        // 如果实体附着在武器上
        if (attachedToWeapon) {
            handleAttachedToWeaponTick();
            return;
        }
        
        // 普通模式下的移动 - 添加null检查避免空指针异常
        if (direction != null) {
            this.setDeltaMovement(direction.scale(speed));
            this.setPos(this.position().add(this.getDeltaMovement()));
        } else {
            // 如果direction为null，设置为默认的向上方向
            this.setDeltaMovement(new Vec3(0, 0.1, 0));
            this.setPos(this.position().add(this.getDeltaMovement()));
        }
        
        // 检查是否应该移除实体
        if (maxLifeTicks-- <= 0 || this.isInWaterOrRain() || this.isInLava()) {
            this.remove(RemovalReason.DISCARDED);
        }
        
        // 处理攻击逻辑
        if (level().isClientSide) return;
        
        // 查找范围内的实体进行攻击
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, 
                this.getBoundingBox().inflate(1.5),
                e -> e != owner && e.isAlive())) {
            
            // 根据武器类型造成不同的效果
            attackTarget(entity);
            
            // 攻击后移除实体（除非是持续型武器）
            if (!isContinuous) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }
    
    /**
     * 处理附着在武器上时的tick逻辑
     */
    private void handleAttachedToWeaponTick() {
        // 检查玩家是否仍然持有武器
        if (attachedPlayer == null || attachedPlayer.isRemoved()) {
            detachFromWeapon();
            return;
        }
        
        // 检查玩家是否切换了武器
        ItemStack mainHand = attachedPlayer.getMainHandItem();
        if (!(mainHand.getItem() instanceof Heiseisword) || 
            !((Heiseisword)mainHand.getItem()).hasAttachedEntity(mainHand)) {
            detachFromWeapon();
            return;
        }
        
        // 客户端处理：更新位置以匹配武器尖端
        if (level().isClientSide) {
            // 这里的逻辑将由渲染器处理，而不是实际移动实体
            // 因为实体在服务器端是隐形的，只有客户端渲染它附着在武器上
        }
    }
    
    /**
     * 检查是否附着在武器上
     */
    public boolean isAttachedToWeapon() {
        return attachedToWeapon;
    }
    
    /**
     * 获取附着的玩家
     */
    public Player getAttachedPlayer() {
        return attachedPlayer;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    protected void defineSynchedData() {
        // 不需要同步数据
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // 不需要读取数据
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 动画控制器将在子类中实现
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }


}