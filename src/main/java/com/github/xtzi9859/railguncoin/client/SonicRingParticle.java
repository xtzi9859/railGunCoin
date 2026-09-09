package com.github.xtzi9859.railguncoin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public final class SonicRingParticle extends HugeExplosionParticle {
    private final Quaternionf orientation;
    private final Quaternionf reverseOrientation;

    private SonicRingParticle(
            ClientLevel level, double x, double y, double z,
            double directionX, double directionY, double directionZ,
            SpriteSet sprites
    ) {
        super(level, x, y, z, 1.0D, sprites);
        Vector3f direction = new Vector3f((float)directionX, (float)directionY, (float)directionZ);
        if (direction.lengthSquared() < 0.0001F) {
            direction.set(0.0F, 0.0F, 1.0F);
        } else {
            direction.normalize();
        }
        this.orientation = new Quaternionf().rotationTo(0.0F, 0.0F, 1.0F, direction.x, direction.y, direction.z);
        this.reverseOrientation = new Quaternionf(orientation).rotateY((float)Math.PI);
        this.lifetime = 16;
        this.quadSize = 2.25F;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        setSpriteFromAge(sprites);
    }

    @Override
    public void render(@Nonnull VertexConsumer buffer, @Nonnull Camera camera, float partialTick) {
        renderRotatedQuad(buffer, camera, orientation, partialTick);
        renderRotatedQuad(buffer, camera, reverseOrientation, partialTick);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                @Nonnull SimpleParticleType type, @Nonnull ClientLevel level,
                double x, double y, double z, double xd, double yd, double zd
        ) {
            return new SonicRingParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
