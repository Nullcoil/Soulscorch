package net.nullcoil.soulscorch.world;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
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
import net.nullcoil.soulscorch.block.ModBlocks;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> GHOST_PEPPER_PATCH =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "ghost_pepper_patch"));
    /** Ghost Peppers on the Soulvore Caverns' Soul Soil floors; added to the biome in ModWorldGen. */
    public static final ResourceKey<PlacedFeature> SOULVORE_GHOST_PEPPER_PATCH = createKey("soulvore_ghost_pepper_patch");

    public static final ResourceKey<PlacedFeature> SOULVORE_DELTA = createKey("soulvore_delta");
    public static final ResourceKey<PlacedFeature> SMALL_SOUL_STONE_COLUMNS = createKey("small_soul_stone_columns");
    public static final ResourceKey<PlacedFeature> LARGE_SOUL_STONE_COLUMNS = createKey("large_soul_stone_columns");
    public static final ResourceKey<PlacedFeature> SMALL_HANGING_SOUL_STONE_COLUMNS = createKey("small_hanging_soul_stone_columns");
    public static final ResourceKey<PlacedFeature> LARGE_HANGING_SOUL_STONE_COLUMNS = createKey("large_hanging_soul_stone_columns");

    /** Hanging from the Soulvore Caverns' Soul Stone ceilings; added to the biome in ModWorldGen. */
    public static final ResourceKey<PlacedFeature> VISCOUS_VIOLET_CORT = createKey("viscous_violet_cort");

    private static ResourceKey<PlacedFeature> createKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Soulscorch.id(name));
    }

    // Soulvore Crags column density, per chunk per layer. Floor and ceiling use the same
    // counts so they come out even on average. (Vanilla Basalt Deltas: 4 small, 2 large.)
    private static final int SMALL_COLUMNS = 8;
    private static final int LARGE_COLUMNS = 4;

    // Ghost Pepper tries per patch. The Soul Sand Valley gets one patch per chunk; the Soulvore
    // Caverns get 1 or 2 at random, so 1.5x as many patches, each the same size.
    private static final int GHOST_PEPPER_TRIES = 96;
    private static final int SOULVORE_GHOST_PEPPER_PATCHES_MIN = 1;
    private static final int SOULVORE_GHOST_PEPPER_PATCHES_MAX = 2;

    // Viscous Violet Cort: clusters per chunk per cave layer, Corts per cluster, and how far
    // a cluster spreads sideways from its center
    private static final int CORT_CLUSTERS = 3;
    private static final int CORT_CLUSTER_MIN = 1;
    private static final int CORT_CLUSTER_MAX = 5;
    private static final int CORT_SPREAD = 3;
    // How far up from a floor to look for the ceiling (taller caves get none)
    private static final int CORT_CEILING_SEARCH = 32;

    @SuppressWarnings("deprecation") // CountOnEveryLayerPlacement is what vanilla's deltas still use
    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> ghostPeppers = configuredFeatures.getOrThrow(ModConfiguredFeatures.GHOST_PEPPER_SHRUB);
        PlacementUtils.register(context, GHOST_PEPPER_PATCH, ghostPeppers, new PlacementModifier[]{PlacementUtils.FULL_RANGE, BiomeFilter.biome(), CountPlacement.of(GHOST_PEPPER_TRIES), RandomOffsetPlacement.ofTriangle(7, 3), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)});
        // Same patches in the Soulvore Caverns, 1.5x as often (1 or 2 per chunk, each at its own
        // height). They only take on Soul Soil, which in the Caverns is the cave floors.
        PlacementUtils.register(context, SOULVORE_GHOST_PEPPER_PATCH, ghostPeppers, new PlacementModifier[]{CountPlacement.of(UniformInt.of(SOULVORE_GHOST_PEPPER_PATCHES_MIN, SOULVORE_GHOST_PEPPER_PATCHES_MAX)), PlacementUtils.FULL_RANGE, BiomeFilter.biome(), CountPlacement.of(GHOST_PEPPER_TRIES), RandomOffsetPlacement.ofTriangle(7, 3), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)});

        // Soulvore Crags: same counts as vanilla Basalt Deltas
        PlacementUtils.register(context, SOULVORE_DELTA,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SOULVORE_DELTA),
                CountOnEveryLayerPlacement.of(40), BiomeFilter.biome());
        PlacementUtils.register(context, SMALL_SOUL_STONE_COLUMNS,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SMALL_SOUL_STONE_COLUMNS),
                CountOnEveryLayerPlacement.of(SMALL_COLUMNS), BiomeFilter.biome());
        PlacementUtils.register(context, LARGE_SOUL_STONE_COLUMNS,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.LARGE_SOUL_STONE_COLUMNS),
                CountOnEveryLayerPlacement.of(LARGE_COLUMNS), BiomeFilter.biome());
        // Hanging versions: placed on each floor layer, they climb to the ceiling above it
        PlacementUtils.register(context, SMALL_HANGING_SOUL_STONE_COLUMNS,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SMALL_HANGING_SOUL_STONE_COLUMNS),
                CountOnEveryLayerPlacement.of(SMALL_COLUMNS), BiomeFilter.biome());
        PlacementUtils.register(context, LARGE_HANGING_SOUL_STONE_COLUMNS,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.LARGE_HANGING_SOUL_STONE_COLUMNS),
                CountOnEveryLayerPlacement.of(LARGE_COLUMNS), BiomeFilter.biome());

        // Viscous Violet Cort, in clusters on the Caverns' ceilings. From a spot on each cave
        // floor, climb through air to the first block that has Soul Stone above it (where a
        // Cort can hang). Spread copies around that spot, then lift each one to the ceiling
        // just above it; copies that land in rock or under open air are dropped.
        BlockPredicate canHang = BlockPredicate.wouldSurvive(ModBlocks.VISCOUS_VIOLET_CORT.defaultBlockState(), Vec3i.ZERO);
        PlacementUtils.register(context, VISCOUS_VIOLET_CORT,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.VISCOUS_VIOLET_CORT),
                CountOnEveryLayerPlacement.of(CORT_CLUSTERS),
                EnvironmentScanPlacement.scanningFor(Direction.UP, canHang, BlockPredicate.ONLY_IN_AIR_PREDICATE, CORT_CEILING_SEARCH),
                CountPlacement.of(UniformInt.of(CORT_CLUSTER_MIN, CORT_CLUSTER_MAX)),
                RandomOffsetPlacement.of(UniformInt.of(-CORT_SPREAD, CORT_SPREAD), UniformInt.of(-2, 1)),
                EnvironmentScanPlacement.scanningFor(Direction.UP, canHang, BlockPredicate.ONLY_IN_AIR_PREDICATE, 3),
                BiomeFilter.biome());
    }
}
