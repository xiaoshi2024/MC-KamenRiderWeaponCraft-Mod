// package com.xiaoshi2022.kamenriderweaponcraft.integration.example;

// import com.xiaoshi2022.kamenriderweaponcraft.rider.core.CoreSlotManager;
// import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.ExternalRiderEffectProvider;
// import net.minecraft.core.particles.ParticleTypes;
// import net.minecraft.resources.ResourceLocation;
// import net.minecraft.sounds.SoundEvents;
// import net.minecraft.sounds.SoundSource;
// import net.minecraft.world.effect.MobEffectInstance;
// import net.minecraft.world.effect.MobEffects;
// import net.minecraft.world.entity.LivingEntity;
// import net.minecraft.world.entity.player.Player;
// import net.minecraft.world.level.Level;
// import net.minecraft.world.phys.Vec3;

// import javax.annotation.Nullable;
// import java.util.function.Supplier;

// public class CoreCraftExternalCoreProvider implements ExternalRiderEffectProvider {

//     public static final String CORE_ID = "dream_zzz";
//     public static final String MOD_ID = "corecraft";

//     @Override
//     public String getExternalRiderId() {
//         return CORE_ID;
//     }

//     @Override
//     public String getExternalRiderName() {
//         return "ZZZ Dream Core";
//     }

//     @Override
//     public float getAttackDamage() {
//         return 48.0f;
//     }

//     @Override
//     public float getEffectRange() {
//         return 7.0f;
//     }

//     @Override
//     public double getEnergyCost() {
//         return 28.0;
//     }

//     @Override
//     public String getActivationSoundName() {
//         return "Dream Slash!";
//     }

//     @Override
//     public void executeSkill(Level level, LivingEntity shooter, Vec3 direction) {
//         if (level.isClientSide) {
//             spawnDreamParticles(level, shooter, direction);
//             return;
//         }

//         Vec3 normalizedDirection = direction.normalize();
//         double range = getEffectRange();
//         Vec3 start = shooter.getEyePosition(1.0f);
//         Vec3 end = start.add(normalizedDirection.scale(range));

//         net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(2.5, 2.5, 2.5);

//         level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
//             if (entity == shooter) return false;
//             Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
//             double dotProduct = toEntity.dot(normalizedDirection);
//             return dotProduct > 0.6;
//         }).forEach(target -> {
//             if (shooter instanceof Player player) {
//                 target.hurt(level.damageSources().playerAttack(player), getAttackDamage());
//             } else {
//                 target.hurt(level.damageSources().mobAttack(shooter), getAttackDamage());
//             }

//             target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
//             target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
//             target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));

//             spawnHitParticles(level, target);
//         });

//         if (shooter instanceof Player player) {
//             player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0));
//             player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 2));
//             player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
//         }

//         level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
//                 SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 1.5F);

//         spawnDreamWave(level, shooter, normalizedDirection);
//     }

//     private void spawnDreamParticles(Level level, LivingEntity shooter, Vec3 direction) {
//         for (int i = 0; i < 40; i++) {
//             double offsetX = (level.random.nextGaussian() * 2.0);
//             double offsetY = level.random.nextGaussian() * 2.0 + 1.0;
//             double offsetZ = (level.random.nextGaussian() * 2.0);

//             Vec3 particlePos = shooter.position().add(offsetX, offsetY, offsetZ);
//             Vec3 particleMotion = direction.scale(0.4).add(
//                     level.random.nextGaussian() * 0.15,
//                     level.random.nextGaussian() * 0.15,
//                     level.random.nextGaussian() * 0.15
//             );

//             level.addParticle(ParticleTypes.WARPED_SPORE, particlePos.x, particlePos.y, particlePos.z,
//                     particleMotion.x, particleMotion.y, particleMotion.z);
//             level.addParticle(ParticleTypes.SOUL, particlePos.x, particlePos.y, particlePos.z,
//                     particleMotion.x * 0.3, particleMotion.y * 0.3, particleMotion.z * 0.3);
//         }
//     }

//     private void spawnHitParticles(Level level, LivingEntity target) {
//         for (int i = 0; i < 20; i++) {
//             double offsetX = (level.random.nextGaussian() * 0.6);
//             double offsetY = level.random.nextGaussian() * 0.6 + 0.5;
//             double offsetZ = (level.random.nextGaussian() * 0.6);

//             Vec3 particlePos = target.position().add(offsetX, offsetY, offsetZ);

//             level.addParticle(ParticleTypes.SOUL, particlePos.x, particlePos.y, particlePos.z,
//                     level.random.nextGaussian() * 0.3, level.random.nextGaussian() * 0.3, level.random.nextGaussian() * 0.3);
//             level.addParticle(ParticleTypes.END_ROD, particlePos.x, particlePos.y, particlePos.z,
//                     level.random.nextGaussian() * 0.15, 0.15, level.random.nextGaussian() * 0.15);
//         }
//     }

//     private void spawnDreamWave(Level level, LivingEntity shooter, Vec3 direction) {
//         Vec3 start = shooter.getEyePosition(1.0f);

//         for (int i = 0; i < 30; i++) {
//             double progress = (double) i / 30.0;
//             double distance = progress * getEffectRange();

//             Vec3 wavePos = start.add(direction.scale(distance));
//             wavePos = wavePos.add(
//                     (level.random.nextGaussian() * 1.0),
//                     (level.random.nextGaussian() * 1.0),
//                     (level.random.nextGaussian() * 1.0)
//             );

//             level.addParticle(ParticleTypes.WARPED_SPORE, wavePos.x, wavePos.y, wavePos.z,
//                     0.0, 0.08, 0.0);
//             if (i % 3 == 0) {
//                 level.addParticle(ParticleTypes.SOUL, wavePos.x, wavePos.y, wavePos.z,
//                         0.0, 0.05, 0.0);
//             }
//         }
//     }

//     @Nullable
//     @Override
//     public ResourceLocation getExternalModelLocation() {
//         return new ResourceLocation("corecraft", "geo/item/cores/zzz_dream_core.geo.json");
//     }

//     @Nullable
//     @Override
//     public Supplier<ResourceLocation> getExternalAnimController() {
//         return () -> new ResourceLocation("corecraft", "controller/cores/zzz_dream_core.json");
//     }

//     @Override
//     public boolean isExternal() {
//         return true;
//     }

//     @Override
//     public boolean supportsScrambleMode() {
//         return true;
//     }

//     @Override
//     public int getScrambleModeMaxLayers() {
//         return 4;
//     }

//     public static void register() {
//         CoreSlotManager.registerExternalCore(
//                 CORE_ID,
//                 "ZZZ Dream Core",
//                 MOD_ID,
//                 new ResourceLocation("corecraft", "geo/item/cores/zzz_dream_core.geo.json"),
//                 () -> new ResourceLocation("corecraft", "controller/cores/zzz_dream_core.json"),
//                 new CoreCraftExternalCoreProvider(),
//                 500
//         );
//     }
// }