package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.plant;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.animatable.GeoBlockEntity;

public class HelheimVineBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final InstancedAnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);

    public HelheimVineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HELHEIM_VINE_ENTITY.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<HelheimVineBlockEntity> event) {
        // 设置动画逻辑
        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public InstancedAnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}