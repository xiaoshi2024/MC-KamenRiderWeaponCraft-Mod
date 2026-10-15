package com.xiaoshi2022.kamenriderweaponcraft.mixin;

import com.xiaoshi2022.kamenriderweaponcraft.compat.KRCRecipeInjector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * 配方管理器注入：在解析配方 JSON 前改写数据包内容，
 * 实现与假面骑士工艺（kamenridercraft）的配方软联动（未安装该 mod 时不做任何事）
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD")
    )
    private void krwc$injectKRCRecipes(Map<ResourceLocation, JsonElement> recipes,
                                       ResourceManager resourceManager,
                                       ProfilerFiller profiler,
                                       CallbackInfo ci) {
        KRCRecipeInjector.inject(recipes);
    }
}
