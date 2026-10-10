package net.nullcoil.soulscorch.world;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.nullcoil.soulscorch.Soulscorch;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> GHOST_PEPPER_PATCH =
            ResourceKey.create(Registries.PLACED_FEATURE, Soulscorch.id("ghost_pepper_patch"));

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        Holder<Feature> ghostPeppers = context.lookup(Registries.FEATURE)
                .getOrThrow(ModConfiguredFeatures.GHOST_PEPPER_SHRUB);

        PlacementUtils.register(context, GHOST_PEPPER_PATCH, ghostPeppers,
                PlacementUtils.FULL_RANGE,
                BiomeFilter.biome(),
                CountPlacement.of(96),
                OffsetPlacement.of(
                        UniformInt.of(-7, 7),
                        UniformInt.of(-3, 3)
                ),
                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)
        );
    }
}