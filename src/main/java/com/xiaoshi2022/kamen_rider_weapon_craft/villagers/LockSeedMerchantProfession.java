package com.xiaoshi2022.kamen_rider_weapon_craft.villagers;

import com.google.common.collect.ImmutableSet;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LockSeedMerchantProfession {
    /* ----------------  POI  ---------------- */
    public static final DeferredRegister<PoiType> POI_TYPE =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, kamen_rider_weapon_craft.MOD_ID);

    public static final RegistryObject<PoiType> LOCKSEED_MARKET_POI_TYPE =
            POI_TYPE.register("lockseed_market_poi_type", () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.LOCKSEEDIRONBARS.get()
                            .getStateDefinition()
                            .getPossibleStates()),
                    1,   // maxTickets
                    1    // validRange
            ));

    /* -------------- Profession ------------- */
    public static final DeferredRegister<VillagerProfession> PROFESSION =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, kamen_rider_weapon_craft.MOD_ID);

    public static final RegistryObject<VillagerProfession> LOCKSEED_MERCHANT =
            PROFESSION.register("lockseed_merchant",
                    () -> new VillagerProfession(
                            "lockseed_merchant",
                            holder -> holder.get() == LOCKSEED_MARKET_POI_TYPE.get(),
                            holder -> holder.get() == LOCKSEED_MARKET_POI_TYPE.get(),
                            ImmutableSet.of(ModItems.CHERYY.get()),   // 可持有物品
                            ImmutableSet.of(Block.byItem(ModItems.CHERYY.get())),
                            SoundEvents.VILLAGER_WORK_CARTOGRAPHER          // 工作音效
                    ));
}