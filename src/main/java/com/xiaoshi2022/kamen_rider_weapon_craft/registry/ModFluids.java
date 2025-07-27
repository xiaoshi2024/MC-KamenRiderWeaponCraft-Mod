package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.Fluid.HelheimJuiceFluid;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;


public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, MOD_ID);

    /* 注册器：流体类型（原先缺失）*/
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MOD_ID);


    /* ========== 赫尔海姆果汁 ========== */
    private static final ResourceLocation HELHEIM_JUICE_STILL =
            new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "block/fluid/helheim_juice_still");
    private static final ResourceLocation HELHEIM_JUICE_FLOW =
            new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "block/fluid/helheim_juice_flow");

    public static final RegistryObject<FluidType> HELHEIM_JUICE_TYPE =
            FLUID_TYPES.register("helheim_juice",
                    () -> new FluidType(FluidType.Properties.create()
                            .density(1200).temperature(20).viscosity(1200)
                            .descriptionId("fluid." + kamen_rider_weapon_craft.MOD_ID + ".helheim_juice")) {
                        @Override
                        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                            consumer.accept(new IClientFluidTypeExtensions() {
                                @Override
                                public ResourceLocation getStillTexture() {
                                    return HELHEIM_JUICE_STILL;
                                }

                                @Override
                                public ResourceLocation getFlowingTexture() {
                                    return HELHEIM_JUICE_FLOW;
                                }
                            });
                        }
                    });

    public static final RegistryObject<FlowingFluid> HELHEIM_JUICE_STILL_FLUID =
            FLUIDS.register("helheim_juice",
                    () -> new HelheimJuiceFluid.Source(helheimJuiceProps()));

    public static final RegistryObject<FlowingFluid> HELHEIM_JUICE_FLOWING_FLUID =
            FLUIDS.register("helheim_juice_flowing",
                    () -> new HelheimJuiceFluid.Flowing(helheimJuiceProps()));

    private static ForgeFlowingFluid.Properties helheimJuiceProps() {
        return new ForgeFlowingFluid.Properties(
                HELHEIM_JUICE_TYPE,
                HELHEIM_JUICE_STILL_FLUID,
                HELHEIM_JUICE_FLOWING_FLUID)
                .block(ModBlocks.HELHEIM_JUICE_BLOCK)
                .bucket(ModItems.HELHEIM_JUICE_BUCKET)
                .slopeFindDistance(4)        // 最大流动距离
                .tickRate(10);               // 更新间隔（越大越慢）
    }
}