package com.github.xtzi9859.railguncoin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public final class SonicRingParticle extends HugeExplosionParticle {
    private SonicRingParticle(
            ClientLevel level, double x, double y, double z,
            SpriteSet sprites
    ) {
        super(level, x, y, z, 1.0D, sprites);
        this.lifetime = 16;
        this.quadSize = 1.5F;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        setSpriteFromAge(sprites);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type, ClientLevel level,
                double x, double y, double z, double xd, double yd, double zd
        ) {
            return new SonicRingParticle(level, x, y, z, sprites);
        }
    }
}
