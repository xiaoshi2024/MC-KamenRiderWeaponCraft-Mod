package com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.items;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.prop.arrowx.AonicxEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AonicxItem extends ArrowItem {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "kamen_rider_weapon_craft");
    public static void init(IEventBus iEventBus) { ITEMS.register(iEventBus); }
    public static final RegistryObject<Item> SONICX_ARROW = ITEMS.register("sonicx_arrow",
            () -> new AonicxItem(new Item.Properties(), 2.0f));


    public AonicxItem(Properties properties,float damage) {
        super(properties);
        this.damage = damage;
    }

    public float damage;

    @Override
    public AbstractArrow createArrow(Level plevel, ItemStack pStack, LivingEntity pShooter) {
        AonicxEntity arrow = new AonicxEntity(pShooter, plevel,AonicxItem.SONICX_ARROW.get());
        arrow.setBaseDamage(this.damage);
        return arrow;
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
        int enchant = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow);
        return enchant <= 0? false : this.getClass() == AonicxItem.class;
    }
}
