package net.nullcoil.soulscorch.util;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;

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
                    NoiseRouter router = randomState.router();

                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();
                    DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(x, y, z);

                    double temperature     = router.temperature().compute(point);
                    double humidity        = router.vegetation().compute(point);
                    double continentalness = router.continents().compute(point);
                    double erosion         = router.erosion().compute(point);
                    double depth           = router.depth().compute(point);
                    double weirdness       = router.ridges().compute(point);

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