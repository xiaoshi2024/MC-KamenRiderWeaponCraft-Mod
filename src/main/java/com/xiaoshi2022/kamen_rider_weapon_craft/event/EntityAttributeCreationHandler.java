//package com.xiaoshi2022.kamen_rider_weapon_craft.event;
//
//import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
//import net.minecraft.world.entity.ai.attributes.Attributes;
//import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//
//import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;
//
///**
// * 实体属性创建处理器
// * 注意：只适用于继承自LivingEntity的实体
// */
//@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
//public class EntityAttributeCreationHandler {
//
//    @SubscribeEvent
//    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
//        // 注意：由于GhostHeroicSoulEntity继承自Projectile，GaimLockSeedEntity继承自Entity
//        // 而不是LivingEntity，所以它们不能使用Minecraft的属性系统
//        // 这些实体的生命值和伤害值将在各自的类中直接管理
//    }
//
//    /**
//     * 辅助方法，用于创建通用的实体属性构建器
//     */
//    private static AttributeSupplier.Builder createBaseAttributes() {
//        return AttributeSupplier.builder()
//                .add(Attributes.MAX_HEALTH, 20.0D)
//                .add(Attributes.MOVEMENT_SPEED, 0.3D);
//    }
//}