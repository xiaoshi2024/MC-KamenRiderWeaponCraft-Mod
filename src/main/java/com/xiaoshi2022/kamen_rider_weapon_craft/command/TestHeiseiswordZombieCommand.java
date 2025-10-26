package com.xiaoshi2022.kamen_rider_weapon_craft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;

public class TestHeiseiswordZombieCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("testheiseiswordzombie")
                .requires(source -> source.hasPermission(2))
                .executes(TestHeiseiswordZombieCommand::spawnTestZombie)
        );
    }

    private static int spawnTestZombie(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // 创建僵尸
        Zombie zombie = new Zombie(source.getLevel());
        zombie.setPos(source.getPosition());

        // 装备平成剑
        ItemStack heiseisword = new ItemStack(ModItems.HEISEISWORD.get());
        zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, heiseisword);

        // 修改：直接在实体数据中设置标签
        zombie.addTag("HeiseiswordZombie");

        // 设置血量为300
        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(300.0F);
        zombie.setHealth(300.0F);

        // 生成僵尸
        if (!source.getLevel().isClientSide()) {
            source.getLevel().addFreshEntity(zombie);
            source.sendSuccess(() -> Component.literal("已生成测试用平成剑僵尸！请击杀并检查掉落。"), true);

            // 输出调试信息
            boolean hasTag = zombie.getTags().contains("HeiseiswordZombie");
            source.sendSuccess(() -> Component.literal("僵尸标签检查: " + hasTag), false);

            // 额外调试：输出完整NBT
            source.sendSuccess(() -> Component.literal("僵尸完整NBT: " + zombie.serializeNBT()), false);
        }

        return 1;
    }
}