package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.daidaimaru.entity;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;

public class ThrownDaidaimaru extends AbstractArrow implements ItemSupplier {
    private static final Logger LOGGER = LogManager.getLogger();
    private int damageValue;
    private CompoundTag enchantmentTag;
    private static final float DAMAGE_AMOUNT = 5.0F; // 造成的伤害值，可根据需要调整
    private static final float BOUNCE_FACTOR = -0.1F; // 反弹系数，可根据需要调整

    public ThrownDaidaimaru(EntityType<? extends ThrownDaidaimaru> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownDaidaimaru(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntityTypes.THROWN_DAIDAIMARU.get(), shooter, level);
        this.pickup = Pickup.ALLOWED; // 设置拾取规则为允许玩家捡起

        // 保存原始物品的耐久度
        this.damageValue = stack.getDamageValue();

        // 保存附魔信息
        this.enchantmentTag = new CompoundTag();
        ListTag enchantmentList = new ListTag();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int player = entry.getValue();
            CompoundTag enchantmentCompound = new CompoundTag();
            enchantmentCompound.putString("id", BuiltInRegistries.ENCHANTMENT.getKey(enchantment).toString());
            enchantmentCompound.putInt("lvl", player);
            enchantmentList.add(enchantmentCompound);
        }
        this.enchantmentTag.put("Enchantments", enchantmentList);
    }

    @Override
    protected ItemStack getPickupItem() {
        ItemStack stack = new ItemStack(ModItems.DAIDAIMARU.get());
        stack.setDamageValue(this.damageValue + 1);

        // 恢复附魔信息
        if (this.enchantmentTag != null && this.enchantmentTag.contains("Enchantments")) {
            ListTag enchantmentList = this.enchantmentTag.getList("Enchantments", 10);
            for (int i = 0; i < enchantmentList.size(); i++) {
                CompoundTag enchantmentCompound = enchantmentList.getCompound(i);
                String enchantmentId = enchantmentCompound.getString("id");
                int level = enchantmentCompound.getInt("lvl");
                Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.get(new ResourceLocation(enchantmentId));
                if (enchantment != null) {
                    EnchantmentHelper.setEnchantments(Collections.singletonMap(enchantment, level), stack);
                }
            }
        }
        LOGGER.info("Returning item with damage value: " + this.damageValue + ", Enchantments restored");
        return stack;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Damage", this.damageValue);
        if (this.enchantmentTag != null) {
            tag.put("Enchantments", this.enchantmentTag.getList("Enchantments", 10));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Damage")) {
            this.damageValue = tag.getInt("Damage");
        }
        if (tag.contains("Enchantments")) {
            this.enchantmentTag = tag.getCompound("Enchantments");
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (target instanceof LivingEntity livingEntity) {
            // 造成伤害
            float damage = DAMAGE_AMOUNT;
            if (livingEntity instanceof LivingEntity) {
                damage += EnchantmentHelper.getDamageBonus(this.getItem(), livingEntity.getMobType());
            }

            Entity owner = this.getOwner();
            DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);

            if (livingEntity.hurt(damageSource, damage)) {
                if (livingEntity.getType() == EntityType.ENDERMAN) {
                    return;
                }

                if (owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingEntity, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, livingEntity);
                }

                this.doPostHurtEffects(livingEntity);
            }

            // 反弹逻辑
            Vec3 motion = this.getDeltaMovement();
            Vec3 newMotion = new Vec3(motion.x * BOUNCE_FACTOR, motion.y * BOUNCE_FACTOR, motion.z * BOUNCE_FACTOR);
            this.setDeltaMovement(newMotion);

            // 启用重力
            this.setNoGravity(false);
        }
    }

    @Override
    protected EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        return super.findHitEntity(start, end);
    }


    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // 调整实体状态，使其落在地上
        Vec3 motion = this.getDeltaMovement();
        Vec3 newMotion = new Vec3(motion.x * BOUNCE_FACTOR, motion.y * BOUNCE_FACTOR, motion.z * BOUNCE_FACTOR);
        this.setDeltaMovement(newMotion);
        this.setNoGravity(false);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.DAIDAIMARU.get());
    }
}