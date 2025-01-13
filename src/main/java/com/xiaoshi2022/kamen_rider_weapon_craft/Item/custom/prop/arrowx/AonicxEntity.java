package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.prop.arrowx;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.items.AonicxItem;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AonicxEntity extends AbstractArrow implements GeoAnimatable {

    private static final RawAnimation ARROWCHANGE = RawAnimation.begin().thenPlay("arrowchange");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final DeferredRegister<EntityType<?>> ENTITY = DeferredRegister.create
            (ForgeRegistries.ENTITY_TYPES, kamen_rider_weapon_craft.MOD_ID);

    public static void init(IEventBus iEventBus) { ENTITY.register(iEventBus); }
   public static final RegistryObject<EntityType<AonicxEntity>> SONICX_ARROW = ENTITY.register("sonicx_arrow",
           () -> EntityType.Builder.<AonicxEntity>of(AonicxEntity::new, MobCategory.MISC)
                  .sized(0.5F, 0.5F)
                  .clientTrackingRange(4)
                   .updateInterval(20)
                  .build(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "sonicx_arrow").toString()));

    public final Item myArrowItem;
    private boolean canSpawnExtraArrows = true;
    private int lifeTicks = 60;

    public AonicxEntity(LivingEntity shooter, Level pLevel, Item myArrowItem) {
        super(SONICX_ARROW.get(), shooter, pLevel);
        this.myArrowItem = myArrowItem;
    }

    public AonicxEntity(EntityType<? extends AonicxEntity> pEntityType, Level level) {
        super(pEntityType, level);
        this.myArrowItem = AonicxItem.SONICX_ARROW.get();
    }
    public AonicxEntity(EntityType<? extends AonicxEntity> EntityType, Level level, boolean canSpawnExtraArrows) {
        super(EntityType,level);
        this.myArrowItem = AonicxItem.SONICX_ARROW.get();
        this.canSpawnExtraArrows = canSpawnExtraArrows;
    }

    @Override
    protected void onHitBlock(BlockHitResult p_36755_) {
        super.onHitBlock(p_36755_);
        Vec3 currentVelocity = this.getDeltaMovement();
        double speed = currentVelocity.length();
        if(!this.level().isClientSide && this.canSpawnExtraArrows){
            this.canSpawnExtraArrows = false;
            for (int i = 0; i<2; i++){
                AonicxEntity newArrow = new AonicxEntity(SONICX_ARROW.get(), this.level(), false);
                newArrow.setPos(this.getX(), this.getY(), this.getZ());

                Vec3 direction = currentVelocity.normalize().scale(speed);
                direction = direction.yRot((i-1)*10);
                newArrow.shoot(direction.x, direction.y, direction.z, (float)speed,0);
                this.level().addFreshEntity(newArrow);
            }
        }
        Vec3 normal = Vec3.atLowerCornerOf(p_36755_.getDirection().getNormal());
        Vec3 reflectedVelocity = currentVelocity.add(normal.scale(2*currentVelocity.dot(normal)));
        Vec3 newVelocity = reflectedVelocity.normalize().scale(speed);
        this.setDeltaMovement(newVelocity);
    }

    @Override
    public void shoot(double p_36775_, double p_36776_, double p_36777_, float p_36778_, float p_36779_) {
        super.shoot(p_36775_, p_36776_, p_36777_, p_36778_, p_36779_);
//        this.setNoGravity(false);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 velocity = this.getDeltaMovement();
        double fixedSpeed = 1.5;
        Vec3 normalizedVelocity = velocity.normalize().scale(fixedSpeed);
        this.setDeltaMovement(normalizedVelocity);

        if (this.lifeTicks > 0){
            this.lifeTicks--;
        }
        if (this.lifeTicks <= 0){
            this.discard();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, state -> state.setAndContinue
                (ARROWCHANGE)));
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    @Override
    public double getTick(Object o) {
        return 0;
    }
}
