// 在 mixins 包中创建 LivingEntityMixin.java
package com.xiaoshi2022.kamen_rider_weapon_craft.mixin;

import com.xiaoshi2022.kamen_rider_weapon_craft.event.EntityDeathEventListener;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onEntityDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        EntityDeathEventListener.onEntityDeath(entity, damageSource);
    }

    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void onEntityDamaged(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        EntityDeathEventListener.recordDamageSource(entity, source);
    }
}