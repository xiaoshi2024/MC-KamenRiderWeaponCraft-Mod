package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.ModTires;
//import com.xiaoshi2022.kamen_rider_weapon_craft.Item.combineds.client.combined.sonicarrow_melon;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.*;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.custom.Melon;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.satan_sabre;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK.weapon_map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, kamen_rider_weapon_craft.MOD_ID);

    public static final RegistryObject<satan_sabre> SATAN_SABRE = ITEMS.register("satan_sabre",
            () -> new satan_sabre());
    public static final RegistryObject<sonicarrow> SONICARROW = ITEMS.register("sonicarrow",
            () -> new sonicarrow(10.2f, 1.5f, new Item.Properties()));
    public static final RegistryObject<daidaimaru> DAIDAIMARU = ITEMS.register("daidaimaru",
            () -> new daidaimaru(ModTires.DAIMARU, 3, 2.5F, new Item.Properties()));
    public static final RegistryObject<musousaberd> MUSOUSABERD = ITEMS.register("musousaberd",
            () -> new musousaberd(ModTires.DAIMARU, 3, 2.5F, new Item.Properties()));
    public static final RegistryObject<gangunsaber> GANGUNSABER = ITEMS.register("gangunsaber",
            () -> new gangunsaber(ModTires.DAIMARU, 3, 1.9F, new Item.Properties()));
    public static final RegistryObject<ridebooker> RIDEBOOKER = ITEMS.register("ridebooker",
            () -> new ridebooker(ModTires.DAIMARU, 4, 1.9F, new Item.Properties()));
    public static final RegistryObject<gavvwhipir> GAVVWHIPIR = ITEMS.register("gavvwhipir",
            () -> new gavvwhipir());
    public static final RegistryObject<destroy_fifty_swords> DESTROY_FIFTY_SWORDS = ITEMS.register("destroy_fifty_swords",
            () -> new destroy_fifty_swords());

    //方块物品
    public static final RegistryObject<Item> HELHEIM_CRACK_ITEM = ITEMS.register("helheim_crack",
            () -> new BlockItem(ModBlocks.HELHEIM_CRACK_BLOCK.get(), new Item.Properties()));

    // 武器联动道具
    public static final RegistryObject<Melon> MELON = ITEMS.register("melon",
            () -> new Melon(new Item.Properties()));

    //说明书
    public static final RegistryObject<weapon_map> WEAPON_MAP = ITEMS.register("weapon_map",
            () -> new weapon_map(new Item.Properties()));
}