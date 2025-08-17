package com.xiaoshi2022.kamen_rider_weapon_craft.tab;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.*;

public class ModTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, kamen_rider_weapon_craft.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TEST_TAB = TABS.register("kamen_rider_weapon_craft_tab", () -> CreativeModeTab.builder()
            .icon(() -> SONICARROW.get().getDefaultInstance())
            .title(Component.translatable("item group.kamen_rider_weapon_craft_tab"))
            .displayItems(((parameters, output) -> {
                output.accept(SONICARROW.get());
                output.accept(DAIDAIMARU.get());
                output.accept(HINAWA_DAIDAI_DJ_JU.get());
                output.accept(MUSOUHINAWADJ.get());
                output.accept(MUSOUSABERD.get());
                output.accept(GANGUNSABER.get());
                output.accept(RIDEBOOKER.get());
                output.accept(SATAN_SABRE.get());
                output.accept(ZANVAT_SWORD.get());
                output.accept(HEISEISWORD.get());
                output.accept(BANA_SPEAR.get());
                output.accept(DENKAMEN_SWORD.get());
                output.accept(GAVVWHIPIR.get());
                output.accept(DESTROY_FIFTY_SWORDS.get());
                output.accept(AUTHORIZE_BUSTER.get());
                output.accept(PROGRISE_HOPPER_BLADE.get());
            })).build());
    public static final RegistryObject<CreativeModeTab> T_TAB = TABS.register("kamen_rider_weapon_craft_prop_tab", () -> CreativeModeTab.builder()
            .icon(() -> MELON.get().getDefaultInstance())
            .title(Component.translatable("item group.kamen_rider_weapon_craft_prop_tab"))
            .displayItems(((parameters, output) -> {
                output.accept(WEAPON_MAP.get());
                output.accept(MELON.get());
                output.accept(CHERYY.get());
                output.accept(TIME_TRAVELER_STUDIO_BLOCK_ITEM.get());
                output.accept(RIDERFUSIONMACHINE_ITEM.get());
                output.accept(RIDER_CIRCUIT_BOARD.get());
                output.accept(RIDER_PASS.get());
                output.accept(RIDER_FORGING_ALLOY_ORE.get());
                output.accept(RIDER_BASIC_WEAPON.get());
                output.accept(RIDERFORGINGALLOYMINERAL.get());
            })).build());
    public static final RegistryObject<CreativeModeTab> Z_TAB = TABS.register("kamen_rider_weapon_craft_misc_tab", () -> CreativeModeTab.builder()
            .icon(() -> PINE_SAPLING_ITEM.get().getDefaultInstance())
            .title(Component.translatable("item group.kamen_rider_weapon_craft_misc_tab"))
            .displayItems(((parameters, output) -> {
                output.accept(PINE_PLANKS_ITEM.get());
                output.accept(PINE_LOG_ITEM.get());
                output.accept(PINE_LEAVES_ITEM.get());
                output.accept(PINE_WOOD_ITEM.get());
                output.accept(STRIPPED_PINE_LOG_ITEM.get());
                output.accept(STRIPPED_PINE_WOOD_ITEM.get());
//                output.accept(PINE_HANGING_SIGN_ITEM.get());
//                output.accept(PINE_WALL_HANGING_SIGN_ITEM.get());
                output.accept(PINE_SAPLING_ITEM.get());
                output.accept(HELHEIM_JELLY_BLOCK_ITEM.get());
                output.accept(HELHEIMFRUIT.get());
                output.accept(HELHEIM_JUICE_BOTTLE.get());
                output.accept(HELHEIM_ICE_CREAM.get());
                output.accept(HELHEIM_JELLY.get());
                output.accept(HELHEIM_CAKE_SLICE.get());
//                output.accept(HELHEIM_PULP.get());
                output.accept(HELHEIM_JUICE_BUCKET.get());
//                for (RegistryObject<Item> plantItem : HELHEIM_PLANT_ITEMS) {
//                    output.accept(new ItemStack(plantItem.get()));
//                }
                output.accept(HELHEIM_PLANT_4_ITEMS.get());
                output.accept(HELHEIM_PLANT_3_ITEMS.get());
                output.accept(HELHEIM_PLANT_2_ITEMS.get());
                output.accept(HELHEIM_PLANT_ITEMS.get());
                output.accept(HELHEIMVINE_ITEM.get());
                output.accept(LOCKSEEDIRONBARS_ITEM.get());
                output.accept(TIMESAND_ITEM.get());
                
            })).build());
}