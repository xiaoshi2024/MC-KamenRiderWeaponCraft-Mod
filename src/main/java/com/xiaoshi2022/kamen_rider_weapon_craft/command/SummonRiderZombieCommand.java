package com.xiaoshi2022.kamen_rider_weapon_craft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.zombie.ZombieHeiseiswordController;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SummonRiderZombieCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 获取所有可用的骑士名称
        List<String> riderNames = HeiseiRiderEffectManager.getRiderOrder();
        
        dispatcher.register(
            Commands.literal("summonriderzombie")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("riderName", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        // 提供骑士名称的自动补全
                        for (String name : riderNames) {
                            builder.suggest(name.toLowerCase());
                        }
                        return builder.buildFuture();
                    })
                    .executes(SummonRiderZombieCommand::spawnRiderZombie)
                )
                .executes(SummonRiderZombieCommand::showUsage) // 没有提供参数时显示用法
        );
    }

    private static int spawnRiderZombie(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String riderNameArg = StringArgumentType.getString(context, "riderName");
        
        // 规范化骑士名称（首字母大写，其余小写）
        String normalizedRiderName = normalizeRiderName(riderNameArg);
        
        // 检查骑士名称是否有效
        List<String> validRiderNames = HeiseiRiderEffectManager.getRiderOrder();
        if (!validRiderNames.contains(normalizedRiderName)) {
            source.sendFailure(Component.literal("无效的骑士名称: " + riderNameArg));
            source.sendFailure(Component.literal("可用的骑士名称: " + String.join(", ", validRiderNames)));
            return 0;
        }
        
        try {
            // 创建僵尸
            Zombie zombie = new Zombie(source.getLevel());
            zombie.setPos(source.getPosition());
            
            // 装备平成剑
            ItemStack heiseisword = new ItemStack(ModItems.HEISEISWORD.get());
            
            // 设置选中的骑士
            Heiseisword.setSelectedRiderStatic(heiseisword, normalizedRiderName);
            
            // 设置固定骑士标签，以便AI识别这是一个固定骑士的僵尸
            heiseisword.getOrCreateTag().putBoolean("fixedRider", true);
            heiseisword.getOrCreateTag().putString("fixedRiderName", normalizedRiderName);
            
            zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, heiseisword);
            
            // 添加标签标识这是一个平成剑僵尸
            zombie.addTag("HeiseiswordZombie");
            // 添加特定骑士的标签，方便识别
            zombie.addTag("RiderZombie_" + normalizedRiderName);
            
            // 设置血量为300
            zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(300.0F);
            zombie.setHealth(300.0F);
            
            // 生成僵尸
            if (!source.getLevel().isClientSide()) {
                source.getLevel().addFreshEntity(zombie);
                
                // 添加平成剑AI目标
                ZombieHeiseiswordController.addHeiseiswordGoal(zombie);
                
                source.sendSuccess(() -> Component.literal("已生成" + normalizedRiderName + "骑士僵尸！"), true);
                source.sendSuccess(() -> Component.literal("此僵尸将只使用" + normalizedRiderName + "的技能。"), false);
            }
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("生成骑士僵尸时出错: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int showUsage(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<String> riderNames = HeiseiRiderEffectManager.getRiderOrder();
        
        source.sendFailure(Component.literal("用法: /summonriderzombie <riderName>"));
        source.sendFailure(Component.literal("可用的骑士名称: " + String.join(", ", riderNames)));
        
        return 0;
    }
    
    // 规范化骑士名称（正确处理带连字符的名称）
    private static String normalizeRiderName(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // 特殊处理 "OOO" 这种全部大写的名称
        if (input.equalsIgnoreCase("ooo")) {
            return "OOO";
        }
        
        // 特殊处理 "Den-O" 带连字符的名称
        if (input.equalsIgnoreCase("den-o")) {
            return "Den-O";
        }
        
        // 特殊处理 "Ex-Aid" 带连字符的名称
        if (input.equalsIgnoreCase("ex-aid")) {
            return "Ex-Aid";
        }
        
        // 正常处理：首字母大写，其余小写
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}