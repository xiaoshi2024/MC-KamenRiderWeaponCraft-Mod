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
public class time_traveler_professionTradesEvent { // ${mod_class} 占位符：你的MOD主类名
    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        // 检查职业是否为自定义职业
        if (event.getType() == TimeTravelerProfession.TIME_TRAVELER_PROFESSION.get()) { // ${profession_name} 占位符：新的职业名称
            event.getTrades().get(1).add(new BasicItemListing(
                    new ItemStack(Items.DIAMOND,2), //6颗钻石换
                    new ItemStack(ModItems.RIDER_CIRCUIT_BOARD.get(),1), // 出骑士电路板
            6, // 交易次数
                    10, // 经验值
                    0.05f // 价格乘数
            ));
            // 第 2 级交易
            event.getTrades().get(2).add(new BasicItemListing(
                    new ItemStack(ModItems.RIDER_FORGING_ALLOY_ORE.get(), 6), // 6 个 rider_forging_alloy_ore
                    new ItemStack(Items.DIAMOND, 3), // 3 颗钻石
                    9, // 交易次数
                    14, // 经验值
                    0.1f // 价格乘数
            ));
            // 第 3 级交易
            event.getTrades().get(3).add(new BasicItemListing(
                    new ItemStack(ModItems.RIDERFORGINGALLOYMINERAL.get(), 12), // 12 个 骑士矿
                    new ItemStack(Items.DIAMOND, 2), // 2 颗钻石
                    12, // 交易次数
                    18, // 经验值
                    0.15f // 价格乘数
            ));
        }
    }
}