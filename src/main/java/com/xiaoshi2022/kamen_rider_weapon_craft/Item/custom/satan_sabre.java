//撒旦军刀，Cjiangqiu添加的的隐藏物品，仅仅作为彩蛋,伤害逻辑上采用了最终之剑的代码，3D模型没来得及完成就只好用贴图自动生成了
package com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.SatanSabre.SatanSabreRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class satan_sabre extends SwordItem implements GeoItem {
	private static final RawAnimation APPEAR = RawAnimation.begin().thenPlay("appear");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public satan_sabre() {
		super(new Tier() {
			public int getUses() {
				return 1500;
			}

			public float getSpeed() {
				return 15f;
			}

			public float getAttackDamageBonus() {
				return 35f;
			}

			public int getLevel() {
				return 4;
			}

			public int getEnchantmentValue() {
				return 8;
			}

			public Ingredient getRepairIngredient() {
				return Ingredient.of();
			}
		}, 3, -2.4f, new Item.Properties());
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			private SatanSabreRenderer renderer;

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new SatanSabreRenderer();

				return this.renderer;
			}
		});
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "appear", 20, state -> PlayState.STOP)
				.triggerableAnim("appear", APPEAR)
				// We've marked the "box_open" animation as being triggerable from the server
				.setSoundKeyframeHandler(state -> {
				}));
	}
	// Let's handle our use method so that we activate the animation when right-clicking while holding the box
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel serverLevel)
			triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "appear", "appear");

		return super.use(level, player, hand);
	}
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}
}
