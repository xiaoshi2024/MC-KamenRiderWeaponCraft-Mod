package com.xiaoshi2022.kamen_rider_weapon_craft.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LaserParticles {

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;
        public Provider(SpriteSet sprite) { this.sprite = sprite; }

        @Override
        public Particle createParticle(SimpleParticleType type,
                                       ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new LaserParticle(level, x, y, z, vx, vy, vz, sprite);
        }
    }

    public static final class LaserParticle extends TextureSheetParticle {
        private final SpriteSet sprites;

        public LaserParticle(ClientLevel level, double x, double y, double z,
                             double vx, double vy, double vz, SpriteSet sprites) {
            super(level, x, y, z);
            this.sprites = sprites;
            setSpriteFromAge(sprites);
            lifetime = 7;
            gravity = 0;
            quadSize = 0.25F;
            xd = vx;
            yd = vy;
            zd = vz;
        }

        @Override public void tick() {
            super.tick();
            setSpriteFromAge(sprites);
        }

        @Override public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }
}