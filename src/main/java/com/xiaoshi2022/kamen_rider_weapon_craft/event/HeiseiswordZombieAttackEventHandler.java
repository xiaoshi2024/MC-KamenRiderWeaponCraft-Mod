package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.zombie.ZombieHeiseiswordController;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class HeiseiswordZombieAttackEventHandler {
    
    // 成就ID
    private static final String CORRUPTED_TIME_SPACE_ACHIEVEMENT = "corrupted_time_space";
    
    // NBT键，用于跟踪玩家是否已经触发过成就
    private static final String TAG_HAS_TRIGGERED_CORRUPTED_ACHIEVEMENT = "hasTriggeredCorruptedTimeSpaceAchievement";
    
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        // 检查被攻击的是否是玩家
        if (event.getEntity() instanceof Player player) {
            // 检查攻击来源是否是实体，并且是僵尸
            DamageSource source = event.getSource();
            if (source.getEntity() instanceof Zombie zombie) {
                // 检查僵尸是否手持平成剑
                ItemStack mainHandItem = zombie.getMainHandItem();
                if (mainHandItem.getItem() instanceof Heiseisword) {
                    // 处理服务器端逻辑
                    if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                        // 检查玩家是否已经触发过这个成就
                        CompoundTag playerData = serverPlayer.getPersistentData();
                        if (!playerData.getBoolean(TAG_HAS_TRIGGERED_CORRUPTED_ACHIEVEMENT)) {
                            // 触发成就
                        triggerCorruptedTimeSpaceAchievement(serverPlayer);
                        
                        // 标记玩家已触发成就，防止重复触发
                        playerData.putBoolean(TAG_HAS_TRIGGERED_CORRUPTED_ACHIEVEMENT, true);
                        // 移除DCD僵尸的生成，保留成就触发
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 触发"腐败的时空"成就
     */
    private static void triggerCorruptedTimeSpaceAchievement(ServerPlayer player) {
        try {
            // 获取成就实例
            Advancement advancement = player.getServer().getAdvancements().getAdvancement(
                    new ResourceLocation(MOD_ID, CORRUPTED_TIME_SPACE_ACHIEVEMENT));
            
            // 检查成就是否存在并触发
            if (advancement != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) {
                    player.getAdvancements().award(advancement, "trigger");
                }
            }
        } catch (Exception e) {
            // 防止成就触发失败影响游戏
            e.printStackTrace();
        }
    }
    
    /**
     * 生成Decade(DCD)形态的武器僵尸
     */
    private static void spawnDecadeZombie(ServerPlayer player) {
        try {
            ServerLevel level = player.serverLevel();
            
            // 创建僵尸
            Zombie decadeZombie = new Zombie(level);
            
            // 设置僵尸位置在玩家附近
            double offsetX = (Math.random() - 0.5) * 8; // -4 到 4 的随机偏移
            double offsetZ = (Math.random() - 0.5) * 8; // -4 到 4 的随机偏移
            double spawnX = player.getX() + offsetX;
            double spawnY = player.getY() + 1;
            double spawnZ = player.getZ() + offsetZ;
            
            decadeZombie.setPos(spawnX, spawnY, spawnZ);
            
            // 装备平成剑并设置为Decade形态
            ItemStack heiseisword = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.HEISEISWORD.get());
            
            // 设置选中的骑士为Decade
            Heiseisword.setSelectedRiderStatic(heiseisword, "Decade");
            
            // 设置固定骑士标签
            heiseisword.getOrCreateTag().putBoolean("fixedRider", true);
            heiseisword.getOrCreateTag().putString("fixedRiderName", "Decade");
            
            // 添加特殊的DCD标记
            heiseisword.getOrCreateTag().putBoolean("isCorruptedDecade", true);
            
            decadeZombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, heiseisword);
            
            // 添加标签标识
            decadeZombie.addTag("HeiseiswordZombie");
            decadeZombie.addTag("DecadeZombie");
            decadeZombie.addTag("CorruptedTimeSpaceZombie");
            
            // 设置更高的血量和攻击力
            decadeZombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(400.0);
            decadeZombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(8.0);
            decadeZombie.setHealth(400.0F);
            
            // 添加自定义名称
            decadeZombie.setCustomName(net.minecraft.network.chat.Component.literal("腐败时空的Decade僵尸"));
            decadeZombie.setCustomNameVisible(true);
            
            // 生成僵尸
            level.addFreshEntity(decadeZombie);
            
            // 添加平成剑AI目标
            ZombieHeiseiswordController.addHeiseiswordGoal(decadeZombie);
            
            // 向玩家发送消息
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("时空出现异常！腐败时空的Decade僵尸现身了！"));
            
        } catch (Exception e) {
            // 防止僵尸生成失败影响游戏
            e.printStackTrace();
        }
    }
}