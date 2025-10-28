package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl.KabutoEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

/**
 * 甲斗王模式下减慢玩家自己发射的弹射物速度
 */
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KabutoProjectileHandler {
    
    // 减慢后的速度倍数 - 0.5表示速度变为原来的50%
    private static final double PROJECTILE_SLOW_FACTOR = 0.5;
    
    /**
     * 监听实体加入世界事件，用于检测玩家发射的弹射物
     */
    @SubscribeEvent
    public static void onProjectileSpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        
        // 检查是否为弹射物实体
        if (entity instanceof Projectile projectile) {
            // 获取弹射物的发射者
            Entity owner = projectile.getOwner();
            
            // 检查发射者是否为玩家，并且玩家处于甲斗王模式
            if (owner != null && owner.isAlive() && isPlayerInKabutoMode(owner)) {
                // 减慢弹射物速度
                slowProjectile(projectile);
            }
        }
    }
    
    /**
     * 检查实体是否为玩家且处于甲斗王模式
     */
    private static boolean isPlayerInKabutoMode(Entity entity) {
        // 这里复用KabutoEffect中的逻辑，检查高速移动+抗性效果
        // 由于KabutoEffect中的isPlayerInKabutoMode是private的，我们在这里重新实现
        return entity instanceof net.minecraft.world.entity.player.Player player &&
               player.hasEffect(MobEffects.MOVEMENT_SPEED) &&
               player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() >= 4 &&
               player.hasEffect(MobEffects.DAMAGE_RESISTANCE);
    }
    
    /**
     * 减慢弹射物速度
     */
    private static void slowProjectile(Projectile projectile) {
        // 获取当前速度向量
        Vec3 currentMotion = projectile.getDeltaMovement();
        
        // 将速度向量乘以减慢因子
        Vec3 slowedMotion = currentMotion.scale(PROJECTILE_SLOW_FACTOR);
        
        // 设置新的速度向量
        projectile.setDeltaMovement(slowedMotion);
        
        // 对于箭矢类弹射物，可能需要额外处理以确保减速效果正确应用
        if (projectile instanceof net.minecraft.world.entity.projectile.AbstractArrow) {
            // 确保减速效果不受其他因素影响
            projectile.setDeltaMovement(slowedMotion);
        }
    }
}