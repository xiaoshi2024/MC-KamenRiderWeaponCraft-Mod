package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.ModTires;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.*;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.HelheimFruit;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods.HelheimBowlFood;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods.HelheimJuiceBottle;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.helheimfoods.HelheimSpecialFood;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.RiderPass;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.custom.Melon;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.custom.cheryy;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.display.rider_fusion_machine_item;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.display.time_traveler_studio_item;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK.weapon_map;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.SonicBowContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

import static com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks.HELHEIM_JELLY_BLOCK;
import static net.minecraft.world.phys.shapes.Shapes.block;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, kamen_rider_weapon_craft.MOD_ID);

    public static final RegistryObject<satan_sabre> SATAN_SABRE = ITEMS.register("satan_sabre",
            () -> new satan_sabre());
    public static final RegistryObject<denkamen_sword> DENKAMEN_SWORD = ITEMS.register("denkamen_sword",
            () -> new denkamen_sword());
    public static final RegistryObject<authorize_buster> AUTHORIZE_BUSTER = ITEMS.register("authorize_buster",
            () -> new authorize_buster());
    public static final RegistryObject<sonicarrow> SONICARROW = ITEMS.register("sonicarrow",
            () -> new sonicarrow());
    public static final RegistryObject<progrise_hopper_blade> PROGRISE_HOPPER_BLADE = ITEMS.register("progrise_hopper_blade",
            () -> new progrise_hopper_blade());
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
    public static final RegistryObject<HinawaDaidai_DJ_Ju> HINAWA_DAIDAI_DJ_JU = ITEMS.register("hinawadai_dai_dj_ju",
            () -> new HinawaDaidai_DJ_Ju(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<musouhinawadj> MUSOUHINAWADJ = ITEMS.register("musouhinawadj",
            () -> new musouhinawadj(ModTires.DAIMARU, 15, 2.7F, new Item.Properties()));

    //方块物品
    public static final RegistryObject<Item> TIMESAND_ITEM = ITEMS.register("timesand_item",
            () -> new BlockItem((Block) ModBlocks.TIMESAND.get(), new Item.Properties()));

    public static final RegistryObject<Item> LOCKSEEDIRONBARS_ITEM = ITEMS.register("lockseed_iron_bars_item",
            () -> new BlockItem(ModBlocks.LOCKSEEDIRONBARS.get(), new Item.Properties()));

    // 存储注册的物品的 RegistryObject 列表

    //食物
    public static final RegistryObject<HelheimFruit> HELHEIMFRUIT = ITEMS.register("helheimfruit",
            ()-> new HelheimFruit(new Item.Properties()));
    public static final RegistryObject<HelheimJuiceBottle> HELHEIM_JUICE_BOTTLE = ITEMS.register("helheim_juice_bottle",
            HelheimJuiceBottle::new);
    public static final RegistryObject<HelheimBowlFood> HELHEIM_ICE_CREAM = ITEMS.register("helheim_ice_cream",
            ()-> new HelheimBowlFood(2, false));
    public static final RegistryObject<HelheimBowlFood> HELHEIM_JELLY= ITEMS.register("helheim_jelly",
            ()-> new HelheimBowlFood(2, false));
    public static final RegistryObject<HelheimSpecialFood> HELHEIM_CAKE_SLICE = ITEMS.register("helheim_cake_slice",
            ()-> new HelheimSpecialFood(4, false));
//    public static final RegistryObject<Item> HELHEIM_PULP= ITEMS.register("helheim_pulp",
//            ()-> new Item(new Item.Properties()));

    public static final RegistryObject<BlockItem> HELHEIM_PLANT_ITEMS = ITEMS.register("helheim_plant",
            () -> new BlockItem(ModBlocks.HELHEIM_PLANT.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> HELHEIM_PLANT_2_ITEMS = ITEMS.register("helheim_plant_2",
            () -> new BlockItem(ModBlocks.HELHEIM_PLANT.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> HELHEIM_PLANT_3_ITEMS = ITEMS.register("helheim_plant_3",
            () -> new BlockItem(ModBlocks.HELHEIM_PLANT.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> HELHEIM_PLANT_4_ITEMS = ITEMS.register("helheim_plant_4",
            () -> new BlockItem(ModBlocks.HELHEIM_PLANT.get(), new Item.Properties()));

//    static {
//        // 遍历 ModBlocks 中的植物方块列表
//        for (RegistryObject<net.minecraft.world.level.block.Block> plantBlock : ModBlocks.HELHEIM_PLANTS) {
//            // 为每个方块创建对应的物品并注册
//            RegistryObject<Item> plantItem = ITEMS.register(plantBlock.getId().getPath(), () -> {
//                Item.Properties properties = new Item.Properties();
//                return new BlockItem(plantBlock.get(), properties);
//            });
//            HELHEIM_PLANT_ITEMS.add(plantItem);
//        }
//    }

    public static final RegistryObject<Item> HELHEIM_CRACK_ITEM = ITEMS.register("helheim_crack",
            () -> new BlockItem(ModBlocks.HELHEIM_CRACK_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> HELHEIMVINE_ITEM = ITEMS.register("helheimvine",
            () -> new BlockItem(ModBlocks.HELHEIMVINE.get(), new Item.Properties()));
    public static final RegistryObject<Item> TIME_TRAVELER_STUDIO_BLOCK_ITEM = ITEMS.register("time_traveler_studio_item",
            () -> new time_traveler_studio_item(ModBlocks.TIME_TRAVELER_STUDIO_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> RIDERFUSIONMACHINE_ITEM = ITEMS.register("rider_fusion_machine_item",
            () -> new rider_fusion_machine_item(ModBlocks.RIDER_FUSION_MACHINE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> RIDERFORGINGALLOYMINERAL = block(ModBlocks.RIDERFORGINGALLOYMINERAL);

    //果汁液体桶【赫尔果实】
    public static final RegistryObject<Item> HELHEIM_JUICE_BUCKET =
            ITEMS.register("helheim_juice_bucket",
                    () -> new BucketItem(ModFluids.HELHEIM_JUICE_STILL_FLUID.get(),
                            new Item.Properties().stacksTo(1)));

    private static RegistryObject<Item> block(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }



    // 武器联动道具
    public static final RegistryObject<Melon> MELON = ITEMS.register("melon",
            () -> new Melon(new Item.Properties()) {
                @Override
                public void onCraftedBy(ItemStack stack, Level world, Player player) {
                    stack.getOrCreateTag().putInt("is_lockseed", 1);
                }
            }
    );
    public static final RegistryObject<cheryy> CHERYY = ITEMS.register("cheryy",
            () -> new cheryy(new Item.Properties()) {
                @Override
                public void onCraftedBy(ItemStack stack, Level world, Player player) {
                    stack.getOrCreateTag().putInt("is_lockseed", 1);
                }
            }
    );

    public static final RegistryObject<Item> RIDER_BASIC_WEAPON = ITEMS.register("rider_basic_weapon",
            () -> new Item(new Item.Properties()));

    //说明书
    public static final RegistryObject<weapon_map> WEAPON_MAP = ITEMS.register("weapon_map",
            () -> new weapon_map(new Item.Properties()));
    //电路板
    public static final RegistryObject<Item> RIDER_FORGING_ALLOY_ORE = ITEMS.register("rider_forging_alloy_ore",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RIDER_CIRCUIT_BOARD = ITEMS.register("rider_circuit_board",
            () -> new Item(new Item.Properties()));

    //工具
    public static final RegistryObject<Item> RIDER_PASS = ITEMS.register("rider_pass",
            () -> new RiderPass(new Item.Properties()));

    //植物的杂七杂八
    public static final RegistryObject<BlockItem> PINE_PLANKS_ITEM = ITEMS.register(
"pine_planks", () -> new BlockItem(ModBlocks.PINE_PLANKS.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> PINE_LOG_ITEM = ITEMS.register(
"pine_log", () -> new BlockItem(ModBlocks.PINE_LOG.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> PINE_LEAVES_ITEM = ITEMS.register(
"pine_leaves", () -> new BlockItem(ModBlocks.PINE_LEAVES.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> PINE_WOOD_ITEM = ITEMS.register(
"pine_wood", () -> new BlockItem(ModBlocks.PINE_WOOD.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> STRIPPED_PINE_LOG_ITEM = ITEMS.register(
"stripped_pine_log", () -> new BlockItem(ModBlocks.STRIPPED_PINE_LOG.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> STRIPPED_PINE_WOOD_ITEM = ITEMS.register(
"stripped_pine_wood", () -> new BlockItem(ModBlocks.STRIPPED_PINE_WOOD.get(), new Item.Properties()));
//    public static final RegistryObject<BlockItem> PINE_HANGING_SIGN_ITEM = ITEMS.register(
//"pine_hanging_sign", () -> new BlockItem(ModBlocks.PINE_HANGING_SIGN.get(), new Item.Properties()));
//        public static final RegistryObject<BlockItem> PINE_WALL_HANGING_SIGN_ITEM = ITEMS.register(
//"pine_wall_hanging_sign", () -> new BlockItem(ModBlocks.PINE_WALL_HANGING_SIGN.get(), new Item.Properties()));

    //树苗
    public static final RegistryObject<BlockItem> PINE_SAPLING_ITEM = ITEMS.register(
"pine_sapling", () -> new BlockItem(ModBlocks.PINE_SAPLING.get(), new Item.Properties()));

    public static final RegistryObject<Item> HELHEIM_JELLY_BLOCK_ITEM =
            ITEMS.register("helheim_jelly_block",
                    () -> new BlockItem(HELHEIM_JELLY_BLOCK.get(),
                            new Item.Properties()));
}