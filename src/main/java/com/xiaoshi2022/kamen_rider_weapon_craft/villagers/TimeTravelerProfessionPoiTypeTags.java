package com.xiaoshi2022.kamen_rider_weapon_craft.villagers;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TimeTravelerProfessionPoiTypeTags extends PoiTypeTagsProvider {
    public TimeTravelerProfessionPoiTypeTags(PackOutput p_256012_, CompletableFuture<HolderLookup.Provider> p_256617_, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_256012_, p_256617_, kamen_rider_weapon_craft.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider p_256621_) {
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE)
                .addOptional(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "traveler_studio_poi_type"));
    }
}
