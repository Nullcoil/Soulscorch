package net.nullcoil.soulscorch.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * A drip that hangs, falls and splashes, like vanilla's. Used for Seeping wood's glowing soul
 * fire blue drips ({@link #SEEPING}) and the Viscous Violet Cort's pale yellow goop ({@link #CORT}).
 */
@Environment(EnvType.CLIENT)
public class SeepingDripParticle extends SingleQuadParticle {
    /** How a kind of drip looks. */
    public record Style(int rgb, boolean glowing) {}

    /** Soul fire blue, glowing. */
    public static final Style SEEPING = new Style(0x2ED5DA, true);
    /** Pale yellow goop, lit like the world around it. */
    public static final Style CORT = new Style(0xE8DC8A, false);

    protected final boolean isGlowing;

    protected SeepingDripParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, Style style) {
        super(level, x, y, z, sprite);
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.06F;
        this.isGlowing = style.glowing();
        this.setColor(((style.rgb() >> 16) & 0xFF) / 255f, ((style.rgb() >> 8) & 0xFF) / 255f, (style.rgb() & 0xFF) / 255f);
    }

    @Override
    public Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Override
    public int getLightCoords(float f) {
        return this.isGlowing ? 240 : super.getLightCoords(f);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }
        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.98F;
        this.yd *= 0.98F;
        this.zd *= 0.98F;
    }

    @Environment(EnvType.CLIENT)
    static class HangParticle extends SeepingDripParticle {
        private final ParticleOptions fallingParticle;

        HangParticle(ClientLevel level, double x, double y, double z,
                     ParticleOptions fallingParticle, TextureAtlasSprite sprite, Style style) {
            super(level, x, y, z, sprite, style);
            this.fallingParticle = fallingParticle;
            this.gravity *= 0.02F;
            this.lifetime = 100;
        }

        @Override
        public void tick() {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            if (this.lifetime-- <= 0) {
                this.remove();
                this.level.addParticle(this.fallingParticle, this.x, this.y, this.z, this.xd, this.yd, this.zd);
                return;
            }
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.02;
            this.yd *= 0.02;
            this.zd *= 0.02;
        }
    }

    @Environment(EnvType.CLIENT)
    static class FallParticle extends SeepingDripParticle {
        private final ParticleOptions landParticle;

        FallParticle(ClientLevel level, double x, double y, double z,
                     ParticleOptions landParticle, TextureAtlasSprite sprite, Style style) {
            super(level, x, y, z, sprite, style);
            this.landParticle = landParticle;
            this.lifetime = (int)(64.0F / (this.random.nextFloat() * 0.8F + 0.2F));
        }

        @Override
        public void tick() {
            super.tick();
            if (this.onGround) {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0, 0, 0);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    static class LandParticle extends SeepingDripParticle {
        LandParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, Style style) {
            super(level, x, y, z, sprite, style);
            this.lifetime = (int)(28.0F / (this.random.nextFloat() * 0.8F + 0.2F));
        }
    }

    @Environment(EnvType.CLIENT)
    public static class HangProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final ParticleOptions fallingParticle;
        private final Style style;
        /** Seeping wood's drip. */
        public HangProvider(SpriteSet sprites) { this(sprites, ModParticles.SEEPING_DRIP_FALL, SEEPING); }
        public HangProvider(SpriteSet sprites, ParticleOptions fallingParticle, Style style) {
            this.sprites = sprites;
            this.fallingParticle = fallingParticle;
            this.style = style;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz, RandomSource rng) {
            return new HangParticle(level, x, y, z,
                    this.fallingParticle, this.sprites.get(rng), this.style);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class FallProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final ParticleOptions landParticle;
        private final Style style;
        /** Seeping wood's drip. */
        public FallProvider(SpriteSet sprites) { this(sprites, ModParticles.SEEPING_DRIP_LAND, SEEPING); }
        public FallProvider(SpriteSet sprites, ParticleOptions landParticle, Style style) {
            this.sprites = sprites;
            this.landParticle = landParticle;
            this.style = style;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz, RandomSource rng) {
            return new FallParticle(level, x, y, z,
                    this.landParticle, this.sprites.get(rng), this.style);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class LandProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Style style;
        /** Seeping wood's drip. */
        public LandProvider(SpriteSet sprites) { this(sprites, SEEPING); }
        public LandProvider(SpriteSet sprites, Style style) {
            this.sprites = sprites;
            this.style = style;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz, RandomSource rng) {
            return new LandParticle(level, x, y, z, this.sprites.get(rng), this.style);
        }
    }
}