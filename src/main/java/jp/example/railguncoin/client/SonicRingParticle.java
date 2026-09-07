package jp.example.railguncoin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class SonicRingParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private SonicRingParticle(
            ClientLevel level, double x, double y, double z,
            double directionX, double directionY, double directionZ, SpriteSet sprites
    ) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.lifetime = 10;
        this.quadSize = 0.65F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.rCol = 0.55F;
        this.gCol = 0.95F;
        this.bCol = 1.0F;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            quadSize = 0.65F + age * 0.12F;
            alpha = Math.max(0.0F, 1.0F - age / (float) lifetime);
            setSpriteFromAge(sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
            return new SonicRingParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
