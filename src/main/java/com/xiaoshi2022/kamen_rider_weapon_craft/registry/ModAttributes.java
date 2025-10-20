//package com.xiaoshi2022.kamen_rider_weapon_craft.registry;
//
//import net.minecraft.core.registries.Registries;
//import net.minecraft.world.entity.ai.attributes.Attribute;
//import net.minecraft.world.entity.ai.attributes.AttributeModifier;
//import net.minecraft.world.entity.ai.attributes.RangedAttribute;
//import net.minecraftforge.registries.DeferredRegister;
//import net.minecraftforge.registries.RegistryObject;
//
//import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;
//
///**
// * 自定义实体属性注册类
// */
//public class ModAttributes {
//    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, MOD_ID);
//
//    // 自定义攻击力属性
//    public static final RegistryObject<Attribute> CUSTOM_ATTACK_DAMAGE = ATTRIBUTES.register("custom_attack_damage",
//            () -> new RangedAttribute("attribute.name.generic.custom_attack_damage", 0.0D, 0.0D, 1024.0D)
//                    .setSyncable(true));
//
//    // 自定义防御属性
//    public static final RegistryObject<Attribute> CUSTOM_ARMOR = ATTRIBUTES.register("custom_armor",
//            () -> new RangedAttribute("attribute.name.generic.custom_armor", 0.0D, 0.0D, 1024.0D)
//                    .setSyncable(true));
//
//    // 自定义韧性属性
//    public static final RegistryObject<Attribute> CUSTOM_ARMOR_TOUGHNESS = ATTRIBUTES.register("custom_armor_toughness",
//            () -> new RangedAttribute("attribute.name.generic.custom_armor_toughness", 0.0D, 0.0D, 1024.0D)
//                    .setSyncable(true));
//
//    // 自定义生命值属性
//    public static final RegistryObject<Attribute> CUSTOM_MAX_HEALTH = ATTRIBUTES.register("custom_max_health",
//            () -> new RangedAttribute("attribute.name.generic.custom_max_health", 20.0D, 1.0D, 1024.0D)
//                    .setSyncable(true));
//
//    // 自定义击退抗性属性
//    public static final RegistryObject<Attribute> CUSTOM_KNOCKBACK_RESISTANCE = ATTRIBUTES.register("custom_knockback_resistance",
//            () -> new RangedAttribute("attribute.name.generic.custom_knockback_resistance", 0.0D, 0.0D, 1.0D)
//                    .setSyncable(true));
//
//    // 注册方法
//    public static void register() {
//        // 这个方法会在主类中调用
//    }
//}