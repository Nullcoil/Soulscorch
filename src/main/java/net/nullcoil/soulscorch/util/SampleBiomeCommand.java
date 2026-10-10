package net.nullcoil.soulscorch.util;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

public class SampleBiomeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("samplebiome")
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    ServerLevel level = source.getLevel();
                    BlockPos pos = BlockPos.containing(source.getPosition());

                    if (!(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator)) {
                        source.sendFailure(Component.translatable(TranslationKeys.SAMPLE_BIOME_FAILURE));
                        return 0;
                    }

                    RandomState randomState = level.getChunkSource().randomState();
                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();

                    Climate.Sampler climate = randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED);
                    Climate.TargetPoint sample = climate.sample(
                            QuartPos.fromBlock(x),
                            QuartPos.fromBlock(y),
                            QuartPos.fromBlock(z)
                    );

                    double temperature     = Climate.unquantizeCoord(sample.temperature());
                    double humidity        = Climate.unquantizeCoord(sample.humidity());
                    double continentalness = Climate.unquantizeCoord(sample.continentalness());
                    double erosion         = Climate.unquantizeCoord(sample.erosion());
                    double depth           = Climate.unquantizeCoord(sample.depth());
                    double weirdness       = Climate.unquantizeCoord(sample.weirdness());

                    Identifier biomeKey = level.getBiome(pos)
                            .unwrapKey()
                            .map(ResourceKey::identifier)
                            .orElse(Identifier.withDefaultNamespace("unknown"));

                    source.sendSuccess(() -> Component.translatable(
                            TranslationKeys.SAMPLE_BIOME_RESULT,
                            x, y, z,
                            biomeKey.toString(),
                            String.format("%.4f", temperature),
                            String.format("%.4f", humidity),
                            String.format("%.4f", continentalness),
                            String.format("%.4f", erosion),
                            String.format("%.4f", depth),
                            String.format("%.4f", weirdness)
                    ), false);
                    return 1;
                })
        );
    }
}