//package com.xiaoshi2022.kamen_rider_weapon_craft.event;
//
//import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
//import net.minecraft.ChatFormatting;
//import net.minecraft.network.chat.Component;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.event.entity.player.ItemTooltipEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.registries.RegistryObject;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
///**
// * 为模组中所有注册的武器添加统一提示的事件处理器
// */
//@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft", value = Dist.CLIENT)
//public class WeaponTooltipHandler {
//
//    // 存储所有模组武器的集合
//    private static final Set<Item> MOD_WEAPONS = new HashSet<>();
//
//    // 静态初始化块，将ModItems中注册的所有武器添加到集合中
//    static {
//        MOD_WEAPONS.add(ModItems.HEISEISWORD.get());
//        MOD_WEAPONS.add(ModItems.BANA_SPEAR.get());
//        MOD_WEAPONS.add(ModItems.ZANVAT_SWORD.get());
//        MOD_WEAPONS.add(ModItems.SATAN_SABRE.get());
//        MOD_WEAPONS.add(ModItems.DENKAMEN_SWORD.get());
//        MOD_WEAPONS.add(ModItems.AUTHORIZE_BUSTER.get());
//        MOD_WEAPONS.add(ModItems.SONICARROW.get());
//        MOD_WEAPONS.add(ModItems.PROGRISE_HOPPER_BLADE.get());
//        MOD_WEAPONS.add(ModItems.DAIDAIMARU.get());
//        MOD_WEAPONS.add(ModItems.MUSOUSABERD.get());
//        MOD_WEAPONS.add(ModItems.GANGUNSABER.get());
//        MOD_WEAPONS.add(ModItems.RIDEBOOKER.get());
//        MOD_WEAPONS.add(ModItems.GAVVWHIPIR.get());
//        MOD_WEAPONS.add(ModItems.DESTROY_FIFTY_SWORDS.get());
//        MOD_WEAPONS.add(ModItems.HINAWA_DAIDAI_DJ_JU.get());
//        MOD_WEAPONS.add(ModItems.MUSOUHINAWADJ.get());
//    }
//
//    @SubscribeEvent
//    public static void onItemTooltip(ItemTooltipEvent event) {
//        ItemStack stack = event.getItemStack();
//        Item item = stack.getItem();
//
//        // 检查物品是否是模组中的武器
//        if (MOD_WEAPONS.contains(item)) {
//            List<Component> tooltips = event.getToolTip();
//
//            // 添加金色斜体的"「假面骑士武器」"提示
//            tooltips.add(Component.literal("「假面骑士武器」").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
//        }
//    }
//}