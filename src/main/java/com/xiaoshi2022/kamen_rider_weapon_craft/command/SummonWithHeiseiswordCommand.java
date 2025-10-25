package com.xiaoshi2022.kamen_rider_weapon_craft.command;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
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
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

// 导入通用AI控制器而非仅僵尸专用控制器
import com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.HeiseiswordController;

public class SummonWithHeiseiswordCommand {
    private static final SimpleCommandExceptionType ENTITY_TYPE_ERROR = new SimpleCommandExceptionType(
            Component.literal("指定的实体类型无效或无法创建！"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 保留原命令以保持兼容性
        dispatcher.register(Commands.literal("summonheiseiszombie")
                .requires(source -> source.hasPermission(2))
                .executes(context -> summonEntity(context, EntityType.ZOMBIE, context.getSource().getPlayerOrException(), 1))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> summonEntity(context, EntityType.ZOMBIE, EntityArgument.getPlayer(context, "player"), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 10))
                                .executes(context -> summonEntity(context, EntityType.ZOMBIE, EntityArgument.getPlayer(context, "player"), 
                                        IntegerArgumentType.getInteger(context, "count"))))));
        
        // 新的通用命令
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
                // 创建实体
                Entity entity = entityType.create(level);
                if (entity != null) {
                    // 生成随机位置（玩家周围4格范围内）
                    double offsetX = (player.getRandom().nextDouble() - 0.5) * 4.0;
                    double offsetZ = (player.getRandom().nextDouble() - 0.5) * 4.0;
                    entity.setPos(playerPos.x + offsetX, playerPos.y, playerPos.z + offsetZ);
                    
                    // 如果是生物实体，设置手持武器和AI
                    if (entity instanceof LivingEntity livingEntity) {
                        // 创建Heiseisword物品栈
                        ItemStack heiseiswordStack = new ItemStack(ModItems.HEISEISWORD.get());
                        
                        // 设置生物手持武器
                        livingEntity.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, heiseiswordStack);
                        
                        // 添加通用AI控制（支持所有生物类型）
                        HeiseiswordController.addHeiseiswordGoal(livingEntity);
                    }
                    
                    // 将实体添加到世界
                    level.addFreshEntity(entity);
                    summonedCount++;
                }
            } catch (Exception e) {
                // 如果创建特定类型的实体失败，记录错误但继续尝试创建其他实体
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