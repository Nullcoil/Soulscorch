package net.nullcoil.soulscorch.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.block.ModBlocks;
import net.nullcoil.soulscorch.block.custom.GhostPepperShrubBlock;
import net.nullcoil.soulscorch.world.feature.BlockColumnConfiguration;
import net.nullcoil.soulscorch.world.feature.ModFeatures;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> GHOST_PEPPER_SHRUB =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "ghost_pepper_shrub"));

    // Soulvore Crags (Basalt Delta-style, in Soul Stone)
    public static final ResourceKey<ConfiguredFeature<?, ?>> SOULVORE_DELTA = createKey("soulvore_delta");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SMALL_SOUL_STONE_COLUMNS = createKey("small_soul_stone_columns");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LARGE_SOUL_STONE_COLUMNS = createKey("large_soul_stone_columns");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        BlockState soulStone = ModBlocks.SOUL_STONE.defaultBlockState();

        // Lava pools rimmed with Soul Slag (vanilla deltas use magma for the rim)
        FeatureUtils.register(context, SOULVORE_DELTA, Feature.DELTA_FEATURE,
                new DeltaFeatureConfiguration(Blocks.LAVA.defaultBlockState(),
                        ModBlocks.SOUL_SLAG_BLOCK.defaultBlockState(),
                        UniformInt.of(3, 7), UniformInt.of(0, 2)));

        // Same sizes as vanilla's small and large basalt columns
        FeatureUtils.register(context, SMALL_SOUL_STONE_COLUMNS, ModFeatures.BLOCK_COLUMNS,
                new BlockColumnConfiguration(soulStone, ConstantInt.of(1), UniformInt.of(1, 4)));
        FeatureUtils.register(context, LARGE_SOUL_STONE_COLUMNS, ModFeatures.BLOCK_COLUMNS,
                new BlockColumnConfiguration(soulStone, UniformInt.of(2, 3), UniformInt.of(5, 10)));


        // Register exactly like CRIMSON_ROOTS in NetherFeatures
        FeatureUtils.register(context, GHOST_PEPPER_SHRUB,
                Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(
                        BlockStateProvider.simple(ModBlocks.GHOST_PEPPER_SHRUB.defaultBlockState().setValue(GhostPepperShrubBlock.AGE, GhostPepperShrubBlock.MAX_AGE))
                ));
    }

    // Optional helper if you want to keep registerKey style
    private static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE,
                Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, name));
    }
}