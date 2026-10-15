package com.xiaoshi2022.kamenriderweaponcraft.event;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 音速弓 CHERRY 形态流血处理
 * 每命中一次持续 3 秒，每 10 tick 造成 0.5 点伤害
 * 计时数据存于实体 persistentData（客户端不可见，无需同步）
 */
@EventBusSubscriber(modid = KamenRiderWeaponCraft.MODID)
public class SonicArrowBleedingHandler {
    private static final String BLEEDING_TICKS_KEY = "KrwcBleedingTicks";

    /** 给目标施加流血效果（重置计时为 3 秒） */
    public static void applyBleeding(LivingEntity target) {
        target.getPersistentData().putInt(BLEEDING_TICKS_KEY, 60);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof LivingEntity living) {
                    CompoundTag data = living.getPersistentData();
                    int ticks = data.getInt(BLEEDING_TICKS_KEY);
                    if (ticks > 0 && living.isAlive()) {
                        if (ticks % 10 == 0) {
                            living.hurt(living.damageSources().generic(), 0.5F);
                        }
                        data.putInt(BLEEDING_TICKS_KEY, ticks - 1);
                    }
                }
            }
        }
    }
}
