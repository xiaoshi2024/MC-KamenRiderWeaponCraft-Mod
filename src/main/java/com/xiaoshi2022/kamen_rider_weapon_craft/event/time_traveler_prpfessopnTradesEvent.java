 // ${mod_package} 占位符：你的MOD包名
package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.villagers.TimeTravelerProfession;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.common.BasicItemListing;

import net.minecraft.world.item.ItemStack;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class time_traveler_prpfessopnTradesEvent { // ${mod_class} 占位符：你的MOD主类名
    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        // 检查职业是否为自定义职业
        if (event.getType() == TimeTravelerProfession.TIME_TRAVELER_PROFESSION.get()) { // ${profession_name} 占位符：新的职业名称
            event.getTrades().get(1).add(new BasicItemListing(
                    new ItemStack(Items.DIAMOND,6), //6颗钻石换
                    new ItemStack(ModItems.WEAPON_MAP.get(),1), //加上1本说明图鉴
            new ItemStack(ModItems.RIDEBOOKER.get(),1), // 出骑士卡盒剑
            1, // 交易次数
                    10, // 经验值
                    0.05f // 价格乘数
            ));
        }
    }
}