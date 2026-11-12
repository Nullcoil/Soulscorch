package net.nullcoil.soulscorch.world.carver;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;

import java.util.function.Function;

/**
 * Vanilla's cave carver, with tunnels and rooms that grow the deeper into the Soulvore
 * Caverns they start. Each carve samples the configured radius multipliers once and
 * scales them by {@link SoulvoreCarving#scaleAt}, then carves exactly like vanilla.
 */
public class SoulvoreCaveCarver extends CaveWorldCarver {
    /** Floor flatness, as configured in ModCarvers (vanilla caves use -0.7). */
    public static final float FLOOR_LEVEL = -0.7F;

    public SoulvoreCaveCarver(Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeGetter, RandomSource random, Aquifer aquifer,
                         ChunkPos sourceChunkPos, CarvingMask mask) {
        float scale = SoulvoreCarving.scaleAt(sourceChunkPos);
        if (scale <= 1.001F) {
            return super.carve(context, config, chunk, biomeGetter, random, aquifer, sourceChunkPos, mask);
        }

        CaveCarverConfiguration scaled = new CaveCarverConfiguration(
                config.probability, config.y, config.yScale, config.lavaLevel, config.replaceable,
                ConstantFloat.of(config.horizontalRadiusMultiplier.sample(random) * scale),
                ConstantFloat.of(config.verticalRadiusMultiplier.sample(random) * scale),
                ConstantFloat.of(FLOOR_LEVEL));
        return super.carve(context, scaled, chunk, biomeGetter, random, aquifer, sourceChunkPos, mask);
    }
}
