package net.nullcoil.soulscorch.world.carver;

import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.nullcoil.nidalib.worldgen.biome.SubBiomes;
import net.nullcoil.soulscorch.world.biome.ModBiomes;
import org.jetbrains.annotations.Nullable;

/**
 * How big Soulvore caves are at a spot. Carvers don't get the climate sampler, so the chunk
 * generator mixin hands it over for the duration of the carver step on that thread.
 */
public final class SoulvoreCarving {
    private SoulvoreCarving() {}

    /** Cave size multiplier at the very center of the Caverns (1 = vanilla at the edge). */
    public static final float CENTER_SCALE = 2.5F;

    /** Height the depth is measured at; the Nether's climate is the same at every height. */
    private static final int SAMPLE_Y = 64;

    private record Context(BiomeSource biomes, Climate.Sampler sampler) {}

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    public static void begin(BiomeSource biomes, Climate.Sampler sampler) {
        CURRENT.set(new Context(biomes, sampler));
    }

    public static void end() {
        CURRENT.remove();
    }

    /**
     * Cave size multiplier for caves starting in this chunk: 1 at the Caverns' edge, rising
     * smoothly to {@link #CENTER_SCALE} at the center. Depth comes from Nidalib's edge ratio,
     * which runs 0 (center) to {@link ModBiomes#CAVERNS_CORE} (the Caverns' edge).
     */
    public static float scaleAt(ChunkPos chunk) {
        @Nullable Context context = CURRENT.get();
        if (context == null) return 1F;

        double ratio = SubBiomes.edgeRatio(context.biomes(), context.sampler(),
                QuartPos.fromBlock(chunk.getMiddleBlockX()), QuartPos.fromBlock(SAMPLE_Y),
                QuartPos.fromBlock(chunk.getMiddleBlockZ()));
        if (Double.isNaN(ratio)) return 1F;

        float depth = Mth.clamp(1F - (float) (ratio / ModBiomes.CAVERNS_CORE), 0F, 1F);
        float eased = depth * depth * (3F - 2F * depth); // smoothstep: gentle near the edge
        return Mth.lerp(eased, 1F, CENTER_SCALE);
    }
}
