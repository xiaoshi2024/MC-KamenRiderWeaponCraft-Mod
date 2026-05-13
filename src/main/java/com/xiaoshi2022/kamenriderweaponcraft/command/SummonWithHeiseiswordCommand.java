package com.xiaoshi2022.kamenriderweaponcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.xiaoshi2022.kamenriderweaponcraft.entity.ai.HeiseiswordController;
import com.xiaoshi2022.kamenriderweaponcraft.register.ItemRegister;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class SummonWithHeiseiswordCommand {
    private static final SimpleCommandExceptionType ENTITY_TYPE_ERROR = new SimpleCommandExceptionType(
            Component.literal("指定的实体类型无效或无法创建！"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("summonwithheiseisword")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("entity_type", ResourceLocationArgument.id())
                        .executes(context -> {
                            ResourceLocation entityTypeLoc = ResourceLocationArgument.getId(context, "entity_type");
                            EntityType<?> entityType = EntityType.byString(entityTypeLoc.toString())
                                    .orElseThrow(() -> ENTITY_TYPE_ERROR.create());
                            
                            return summonEntity(context, entityType, context.getSource().getPlayerOrException(), 1);
                        })
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ResourceLocation entityTypeLoc = ResourceLocationArgument.getId(context, "entity_type");
                                    EntityType<?> entityType = EntityType.byString(entityTypeLoc.toString())
                                            .orElseThrow(() -> ENTITY_TYPE_ERROR.create());
                                    
                                    return summonEntity(context, entityType, 
                                            EntityArgument.getPlayer(context, "player"), 1);
                                })
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 10))
                                        .executes(context -> {
                                            ResourceLocation entityTypeLoc = ResourceLocationArgument.getId(context, "entity_type");
                                            EntityType<?> entityType = EntityType.byString(entityTypeLoc.toString())
                                                    .orElseThrow(() -> ENTITY_TYPE_ERROR.create());
                                            
                                            return summonEntity(context, entityType, 
                                                    EntityArgument.getPlayer(context, "player"), 
                                                    IntegerArgumentType.getInteger(context, "count"));
                                        })))));
    }

    private static int summonEntity(CommandContext<CommandSourceStack> context, EntityType<?> entityType, ServerPlayer player, int count) throws CommandSyntaxException {
        ServerLevel level = player.serverLevel();
        Vec3 playerPos = player.position();
        
        int summonedCount = 0;
        for (int i = 0; i < count; i++) {
            try {
                Entity entity = entityType.create(level);
                if (entity != null) {
                    double offsetX = (player.getRandom().nextDouble() - 0.5) * 4.0;
                    double offsetZ = (player.getRandom().nextDouble() - 0.5) * 4.0;
                    entity.setPos(playerPos.x + offsetX, playerPos.y, playerPos.z + offsetZ);
                    
                    if (entity instanceof LivingEntity livingEntity) {
                        ItemStack heiseiswordStack = new ItemStack(ItemRegister.HEISEISWORD.get());
                        livingEntity.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, heiseiswordStack);
                        
                        HeiseiswordController.addHeiseiswordGoal(livingEntity);
                    }
                    
                    level.addFreshEntity(entity);
                    summonedCount++;
                }
            } catch (Exception e) {
                context.getSource().sendFailure(Component.literal("创建实体时出错: " + e.getMessage()));
            }
        }
        
        if (summonedCount > 0) {
            int finalSummonedCount = summonedCount;
            context.getSource().sendSuccess(() -> Component.literal("已成功召唤" + finalSummonedCount + "个持有Heiseisword的" +
                    entityType.getDescription().getString() + "！"), true);
        } else {
            throw new SimpleCommandExceptionType(Component.literal("无法召唤指定类型的实体！")).create();
        }
        
        return summonedCount;
    }
}