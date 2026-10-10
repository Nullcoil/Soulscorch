package net.nullcoil.soulscorch.world;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.CherryTrunkPlacer;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.block.ModBlocks;
import net.nullcoil.soulscorch.block.SeepingBlocks;
import net.nullcoil.soulscorch.block.custom.GhostPepperShrubBlock;

public class ModConfiguredFeatures {

    public static final ResourceKey<Feature> GHOST_PEPPER_SHRUB =
            ResourceKey.create(Registries.FEATURE, Soulscorch.id("ghost_pepper_shrub"));
    public static final ResourceKey<Feature> SEEPING_SALLOW =
            ResourceKey.create(Registries.FEATURE, Soulscorch.id("seeping_sallow"));

    public static void bootstrap(BootstrapContext<Feature> context) {
        context.register(
                GHOST_PEPPER_SHRUB,
                new SimpleBlockFeature(BlockStateProvider.of(
                        ModBlocks.GHOST_PEPPER_SHRUB.defaultBlockState()
                                .setValue(GhostPepperShrubBlock.AGE, GhostPepperShrubBlock.MAX_AGE)
                ))
        );

        context.register(
                SEEPING_SALLOW,
                new TreeFeature.Builder(
                        BlockStateProvider.of(SeepingBlocks.SEEPING_LOG.defaultBlockState()),
                        new CherryTrunkPlacer(
                                7, 1, 0,
                                new WeightedListInt(WeightedList.of(
                                        new Weighted<>(ConstantInt.of(1), 1),
                                        new Weighted<>(ConstantInt.of(2), 1),
                                        new Weighted<>(ConstantInt.of(3), 1)
                                )),
                                UniformInt.of(2, 4),
                                UniformInt.of(-4, -3),
                                UniformInt.of(-1, 0)
                        ),
                        BlockStateProvider.of(
                                SeepingBlocks.SEEPING_LEAVES.defaultBlockState()
                                        .setValue(LeavesBlock.DISTANCE, 7)
                                        .setValue(LeavesBlock.PERSISTENT, false)
                                        .setValue(BlockStateProperties.WATERLOGGED, false)
                        ),
                        new CherryFoliagePlacer(
                                ConstantInt.of(4),
                                ConstantInt.of(0),
                                ConstantInt.of(5),
                                0.25F,
                                0.25F,
                                0.16666667F,
                                0.33333334F
                        ),
                        new TwoLayersFeatureSize(1, 0, 2),
                        Holder.direct(BlockStateProvider.of(Blocks.SOUL_SOIL))
                ).ignoreVines().build()
        );
    }
}