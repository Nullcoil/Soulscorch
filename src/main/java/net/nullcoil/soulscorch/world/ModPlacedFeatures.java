package net.nullcoil.soulscorch.world;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.nullcoil.soulscorch.Soulscorch;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> GHOST_PEPPER_PATCH =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "ghost_pepper_patch"));

    public static final ResourceKey<PlacedFeature> SOULVORE_DELTA = createKey("soulvore_delta");
    public static final ResourceKey<PlacedFeature> SMALL_SOUL_STONE_COLUMNS = createKey("small_soul_stone_columns");
    public static final ResourceKey<PlacedFeature> LARGE_SOUL_STONE_COLUMNS = createKey("large_soul_stone_columns");
    public static final ResourceKey<PlacedFeature> SMALL_HANGING_SOUL_STONE_COLUMNS = createKey("small_hanging_soul_stone_columns");
    public static final ResourceKey<PlacedFeature> LARGE_HANGING_SOUL_STONE_COLUMNS = createKey("large_hanging_soul_stone_columns");

    private static ResourceKey<PlacedFeature> createKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Soulscorch.id(name));
    }

    @SuppressWarnings("deprecation") // CountOnEveryLayerPlacement is what vanilla's deltas still use
    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> ghostPeppers = configuredFeatures.getOrThrow(ModConfiguredFeatures.GHOST_PEPPER_SHRUB);
        PlacementUtils.register(context, GHOST_PEPPER_PATCH, ghostPeppers, new PlacementModifier[]{PlacementUtils.FULL_RANGE, BiomeFilter.biome(), CountPlacement.of(96), RandomOffsetPlacement.ofTriangle(7, 3), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)});

        // Soulvore Crags: same counts as vanilla Basalt Deltas
        PlacementUtils.register(context, SOULVORE_DELTA,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SOULVORE_DELTA),
                CountOnEveryLayerPlacement.of(40), BiomeFilter.biome());
        PlacementUtils.register(context, SMALL_SOUL_STONE_COLUMNS,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SMALL_SOUL_STONE_COLUMNS),
                CountOnEveryLayerPlacement.of(4), BiomeFilter.biome());
        PlacementUtils.register(context, LARGE_SOUL_STONE_COLUMNS,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.LARGE_SOUL_STONE_COLUMNS),
                CountOnEveryLayerPlacement.of(2), BiomeFilter.biome());
        // Hanging versions: placed on each floor layer, they climb to the ceiling above it
        PlacementUtils.register(context, SMALL_HANGING_SOUL_STONE_COLUMNS,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SMALL_HANGING_SOUL_STONE_COLUMNS),
                CountOnEveryLayerPlacement.of(4), BiomeFilter.biome());
        PlacementUtils.register(context, LARGE_HANGING_SOUL_STONE_COLUMNS,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.LARGE_HANGING_SOUL_STONE_COLUMNS),
                CountOnEveryLayerPlacement.of(2), BiomeFilter.biome());
    }
}
