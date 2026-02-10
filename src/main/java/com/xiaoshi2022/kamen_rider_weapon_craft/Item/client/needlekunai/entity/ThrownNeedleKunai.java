package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.needlekunai.entity;

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

public class ThrownNeedleKunai extends AbstractArrow implements ItemSupplier {
    private static final Logger LOGGER = LogManager.getLogger();
    private int damageValue;
    private CompoundTag enchantmentTag;
    private static final float DAMAGE_AMOUNT = 10.0F;
    private static final float BOUNCE_FACTOR = -0.1F;

    public ThrownNeedleKunai(EntityType<? extends ThrownNeedleKunai> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownNeedleKunai(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntityTypes.THROWN_NEEDLE_KUNAI.get(), shooter, level);
        this.pickup = Pickup.ALLOWED;

        this.damageValue = stack.getDamageValue();

        this.enchantmentTag = new CompoundTag();
        ListTag enchantmentList = new ListTag();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level1 = entry.getValue();
            CompoundTag enchantmentCompound = new CompoundTag();
            enchantmentCompound.putString("id", BuiltInRegistries.ENCHANTMENT.getKey(enchantment).toString());
            enchantmentCompound.putInt("lvl", level1);
            enchantmentList.add(enchantmentCompound);
        }
        this.enchantmentTag.put("Enchantments", enchantmentList);
    }

    @Override
    protected ItemStack getPickupItem() {
        ItemStack stack = new ItemStack(ModItems.NEEDLE_KUNAI.get());
        stack.setDamageValue(this.damageValue + 1);

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
            float damage = DAMAGE_AMOUNT;
            if (livingEntity instanceof LivingEntity) {
                damage += EnchantmentHelper.getDamageBonus(this.getItem(), livingEntity.getMobType());
            }

            Entity owner = this.getOwner();
            DamageSource source = this.damageSources().trident(this, owner != null ? owner : this);
            livingEntity.hurt(source, damage);

            this.setDeltaMovement(this.getDeltaMovement().multiply(BOUNCE_FACTOR, BOUNCE_FACTOR, BOUNCE_FACTOR));
        }
        super.onHitEntity(result);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.NEEDLE_KUNAI.get());
    }
}