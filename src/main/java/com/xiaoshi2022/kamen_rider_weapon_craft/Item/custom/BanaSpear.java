package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.banaspear.BanaSpearRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class BanaSpear extends SwordItem implements GeoItem {

    /* =========================  动画与缓存  ========================= */
    private static final RawAnimation EXTEND = RawAnimation.begin().thenPlay("extend");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /* =========================  构造方法  ========================= */
    public BanaSpear() {
        super(new Tier() {
            @Override public int getUses()               { return 400; }          // 耐久
            @Override public float getSpeed()            { return 3.0f; }         // 攻速
            @Override public float getAttackDamageBonus(){ return 10.0f; }        // 额外伤害
            @Override public int getLevel()              { return 3; }            // 等级
            @Override public int getEnchantmentValue()   { return 15; }           // 附魔值
            @Override public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.RIDER_FORGING_ALLOY_ORE.get()); } // 举例：用竹子修
        }, 3, -2.4f, new Item.Properties());

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    /* =========================  客户端渲染  ========================= */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BanaSpearRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new BanaSpearRenderer();
                return renderer;
            }
        });
    }

    /* =========================  动画控制器  ========================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "extend_controller", 20, state -> PlayState.CONTINUE)
                        .triggerableAnim("extend", EXTEND)
        );
    }

    /* =========================  右键逻辑  ========================= */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        final int COOLDOWN = 4 * 20; // 4 秒
        long last = player.getPersistentData().getLong("bana_spear_last_used");
        long now  = level.getGameTime();

        if (now - last >= COOLDOWN) {
            if (level instanceof ServerLevel server) {
                // 触发 Geckolib 动画
                triggerAnim(player,
                        GeoItem.getOrAssignId(player.getItemInHand(hand), server),
                        "extend_controller",
                        "extend");
            }
        } else {
            int remain = (int) ((COOLDOWN - (now - last)) / 20);
            player.displayClientMessage(
                    Component.literal("香蕉矛枪冷却中，剩余 " + remain + " 秒"), true);
        }
        return super.use(level, player, hand);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}