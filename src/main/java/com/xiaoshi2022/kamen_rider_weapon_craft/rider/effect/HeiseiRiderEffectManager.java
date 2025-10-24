package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl.BuildEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.sound.RiderSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class HeiseiRiderEffectManager {
    private static final Map<String, HeiseiRiderEffect> RIDER_EFFECTS = new HashMap<>();
    private static final Map<String, SoundEvent> RIDER_NAME_SOUNDS = new HashMap<>();
    private static final List<String> RIDER_ORDER = new ArrayList<>();

    static {
        // 注册所有骑士及其音效，使用匿名内部类实现基础功能
        // 特别为Build注册自定义的BuildEffect类，其他骑士使用基础效果
        registerRider("Build", new BuildEffect(), RiderSounds.NAME_BUILD);
        registerRider("Ex-Aid", new BaseRiderEffect("Ex-Aid", 14.0f, 6.0f, 20.0), RiderSounds.NAME_EXAID);
        registerRider("Ghost", new BaseRiderEffect("Ghost", 16.0f, 4.0f, 20.0), RiderSounds.NAME_GHOST);
        registerRider("Drive", new BaseRiderEffect("Drive", 15.0f, 5.0f, 20.0), RiderSounds.NAME_DRIVE);
        registerRider("Gaim", new BaseRiderEffect("Gaim", 16.0f, 4.0f, 20.0), RiderSounds.NAME_GAIM);
        registerRider("Wizard", new BaseRiderEffect("Wizard", 14.0f, 6.0f, 20.0), RiderSounds.NAME_WIZARD);
        registerRider("Fourze", new BaseRiderEffect("Fourze", 17.0f, 3.0f, 20.0), RiderSounds.NAME_FOURZE);
        registerRider("OOO", new BaseRiderEffect("OOO", 16.0f, 5.0f, 20.0), RiderSounds.NAME_OOO);
        registerRider("W", new BaseRiderEffect("W", 15.0f, 5.0f, 20.0), RiderSounds.NAME_W);
        registerRider("Decade", new BaseRiderEffect("Decade", 18.0f, 4.0f, 20.0), RiderSounds.NAME_DECADE);
        registerRider("Kiva", new BaseRiderEffect("Kiva", 15.0f, 6.0f, 20.0), RiderSounds.NAME_KIVA);
        registerRider("Den-O", new BaseRiderEffect("Den-O", 16.0f, 4.0f, 20.0), RiderSounds.NAME_DEN_O);
        registerRider("Kabuto", new BaseRiderEffect("Kabuto", 17.0f, 3.0f, 20.0), RiderSounds.NAME_KABUTO);
        registerRider("Hibiki", new BaseRiderEffect("Hibiki", 14.0f, 7.0f, 20.0), RiderSounds.NAME_HIBIKI);
        registerRider("Blade", new BaseRiderEffect("Blade", 15.0f, 5.0f, 20.0), RiderSounds.NAME_BLADE);
        registerRider("Faiz", new BaseRiderEffect("Faiz", 16.0f, 4.0f, 20.0), RiderSounds.NAME_FAIZ);
        registerRider("Ryuki", new BaseRiderEffect("Ryuki", 15.0f, 6.0f, 20.0), RiderSounds.NAME_RYUKI);
        registerRider("Agito", new BaseRiderEffect("Agito", 17.0f, 4.0f, 20.0), RiderSounds.NAME_AGITO);
        registerRider("Kuuga", new BaseRiderEffect("Kuuga", 16.0f, 5.0f, 20.0), RiderSounds.NAME_KUUGA);
    }

    // 基础骑士效果实现类 - 继承AbstractHeiseiRiderEffect以获得默认实现
    private static class BaseRiderEffect extends AbstractHeiseiRiderEffect {
        private final String name;
        private final float damage;
        private final float range;
        private final double energyCost;

        public BaseRiderEffect(String name, float damage, float range, double energyCost) {
            this.name = name;
            this.damage = damage;
            this.range = range;
            this.energyCost = energyCost;
        }

        @Override
        public void executeSpecialAttack(World world, PlayerEntity player, Vec3d direction) {
            // 基础实现：对前方目标造成伤害
            double reach = 10.0;
            Vec3d start = player.getEyePos();
            Vec3d end = start.add(direction.multiply(reach));

            // 使用正确的射线检测API和实体获取方法
            HitResult hitResult = player.raycast(reach, 0.0f, false);
            if (hitResult instanceof EntityHitResult entityHitResult) {
                Entity entity = entityHitResult.getEntity();
                if (entity != player) {
                    // 暂时简化处理，只使用attack方法
                    player.attack(entity);
                }
            }
        }

        @Override
        public String getRiderName() {
            return name;
        }

        @Override
        public String getActivationSoundName() {
            return "activation_" + name.toLowerCase().replace("-", "_");
        }

        @Override
        public float getAttackDamage() {
            return damage;
        }

        @Override
        public float getEffectRange() {
            return range;
        }

        @Override
        public double getEnergyCost() {
            return energyCost;
        }
    }

    private static void registerRider(String name, HeiseiRiderEffect effect, SoundEvent nameSound) {
        RIDER_EFFECTS.put(name, effect);
        RIDER_NAME_SOUNDS.put(name, nameSound);
        RIDER_ORDER.add(name);
    }

    // 获取骑士特效
    public static HeiseiRiderEffect getRiderEffect(String name) {
        return RIDER_EFFECTS.get(name);
    }

    // 安全获取骑士能量消耗，确保即使没有实现也能正常工作
    public static double getRiderEnergyCost(String name) {
        HeiseiRiderEffect effect = getRiderEffect(name);
        if (effect != null) {
            try {
                return effect.getEnergyCost();
            } catch (AbstractMethodError e) {
                // 如果骑士效果没有实现getEnergyCost方法，返回默认值20
                return 20.0;
            }
        }
        return 20.0; // 默认能量消耗值
    }

    // 获取骑士名称音效
    public static SoundEvent getRiderNameSound(String name) {
        return RIDER_NAME_SOUNDS.get(name);
    }

    // 获取骑士顺序列表
    public static List<String> getRiderOrder() {
        return Collections.unmodifiableList(RIDER_ORDER);
    }

    // 播放选择音效："Hey! △△!"
    public static void playSelectionSound(World world, PlayerEntity player, String riderName) {
        SoundEvent nameSound = getRiderNameSound(riderName);
        if (nameSound != null) {
            RiderSounds.playSelectionSound(world, player, nameSound);
        }
    }

    // 播放攻击音效：只播放骑士名称，不再播放Dual Time Break
    public static void playAttackSound(World world, PlayerEntity player, String riderName) {
        SoundEvent nameSound = getRiderNameSound(riderName);
        if (nameSound != null) {
            // 只播放骑士名称，不再播放Dual Time Break
            RiderSounds.playSound(world, player, nameSound);
        }
    }

    // 简化Scramble Time Break音效 - 使用优化的批量播放方法
    public static void playScrambleTimeBreakSound(World world, PlayerEntity player, List<String> selectedRiders) {
        if (selectedRiders.isEmpty()) return;

        // 不再播放Scramble音效，音效将在EntityDeathEventListener中播放
        // RiderSounds.playSound(world, player, RiderSounds.SCRAMBLE_TIME_BREAK);

        // 延迟播放骑士名称音效
        for (int i = 0; i < selectedRiders.size(); i++) {
            final int delay = i * 10; // 每个骑士名称音效间隔10ticks
            final String riderName = selectedRiders.get(i);
 
            // 简化处理，直接在服务器上执行
            if (world instanceof ServerWorld) {
                world.getServer().execute(() -> {
                    SoundEvent nameSound = getRiderNameSound(riderName);
                    if (nameSound != null) {
                        RiderSounds.playSound(world, player, nameSound);
                    }
                });
            }
        }
    }

    // 播放最终攻击音效（无参数版本）- 用于超必杀模式
    // 注意：这个方法现在只在超必杀攻击激活时调用，真正的击败音效将在EntityDeathEventListener中播放
    public static void playUltimateTimeBreakSound(World world, PlayerEntity player) {
        // 保留方法但不播放音效，或者可以改为播放一个激活音效
        // RiderSounds.playSound(world, player, RiderSounds.ULTIMATE_TIME_BREAK);
    }

    // 播放最终攻击音效（带骑士列表版本）
    public static void playUltimateTimeBreakSound(World world, PlayerEntity player, List<String> selectedRiders) {
        // 先播放基础音效
        playUltimateTimeBreakSound(world, player);

        // 然后播放所有选中骑士的名称音效
        if (!selectedRiders.isEmpty()) {
            // 延迟播放骑士名称音效
            for (int i = 0; i < selectedRiders.size(); i++) {
                final int delay = i * 8 + 40; // 延迟40ticks后开始，每个间隔8ticks
                final String riderName = selectedRiders.get(i);

                // 简化处理，直接在服务器上执行
            if (world instanceof ServerWorld) {
                world.getServer().execute(() -> {
                    SoundEvent nameSound = getRiderNameSound(riderName);
                    if (nameSound != null) {
                        RiderSounds.playSound(world, player, nameSound);
                    }
                });
            }
            }
        }
    }

    // 获取所有已注册骑士的数量
    public static int getRiderCount() {
        return RIDER_ORDER.size();
    }

    // 根据索引获取骑士名称
    public static String getRiderAtIndex(int index) {
        if (index >= 0 && index < RIDER_ORDER.size()) {
            return RIDER_ORDER.get(index);
        }
        return null;
    }

    // 检查骑士名称是否有效
    public static boolean isValidRider(String riderName) {
        return RIDER_EFFECTS.containsKey(riderName);
    }

    // 播放完整的超必杀音效序列（在击败实体后触发）
    public static void playUltimateFinishSoundSequence(World world, PlayerEntity player, List<String> selectedRiders) {
        if (selectedRiders.isEmpty()) return;

        // 先播放所有选中骑士的名称音效
        for (int i = 0; i < selectedRiders.size(); i++) {
            final int delay = i * 8; // 每个间隔8ticks
            final String riderName = selectedRiders.get(i);

            // 简化处理，直接在服务器上执行
            if (world instanceof ServerWorld) {
                world.getServer().execute(() -> {
                    SoundEvent nameSound = getRiderNameSound(riderName);
                    if (nameSound != null) {
                        RiderSounds.playSound(world, player, nameSound);
                    }
                });
            }
        }

        // 延迟播放最终音效
        if (!selectedRiders.isEmpty()) {
            // 简化处理，直接播放最终音效
             if (world instanceof ServerWorld) {
                 world.getServer().execute(() -> {
                     RiderSounds.playSound(world, player, RiderSounds.ULTIMATE_TIME_BREAK);
                 });
             }
        }
    }

    // 播放Rider Time启动音效
    public static void playRiderTimeSound(World world, PlayerEntity player) {
        RiderSounds.playRiderTimeSound(world, player);
    }

    // 播放Finish Time音效
    public static void playFinishTimeSound(World world, PlayerEntity player) {
        RiderSounds.playFinishTimeSound(world, player);
    }

    // 播放超必杀激活音效
    public static void playUltimateActivationSound(World world, PlayerEntity player) {
        RiderSounds.playUltimateActivationSound(world, player);
    }

    // 新方法：在击败实体后播放特殊音效
    public static void playKillSound(World world, PlayerEntity player, SoundEvent specialSound) {
        if (specialSound != null) {
            RiderSounds.playSound(world, player, specialSound);
        }
    }

    // 播放Scramble完成音效序列
    public static void playScrambleFinishSoundSequence(World world, PlayerEntity player, List<String> selectedRiders) {
        if (selectedRiders.isEmpty()) return;

        // 播放Scramble音效
        RiderSounds.playSound(world, player, RiderSounds.SCRAMBLE_TIME_BREAK);

        // 延迟播放骑士名称音效
        for (int i = 0; i < selectedRiders.size(); i++) {
            final int delay = i * 10;
            final String riderName = selectedRiders.get(i);

            // 简化处理，直接在服务器上执行
            if (world instanceof ServerWorld) {
                world.getServer().execute(() -> {
                    SoundEvent nameSound = getRiderNameSound(riderName);
                    if (nameSound != null) {
                        RiderSounds.playSound(world, player, nameSound);
                    }
                });
            }
        }
    }
}