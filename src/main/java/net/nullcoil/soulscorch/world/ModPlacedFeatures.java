package net.nullcoil.soulscorch.world;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.nullcoil.soulscorch.Soulscorch;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> GHOST_PEPPER_PATCH =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    Soulscorch.id("ghost_pepper_patch"));

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> ghostPeppers = configuredFeatures.getOrThrow(ModConfiguredFeatures.GHOST_PEPPER_SHRUB);
        PlacementUtils.register(context, GHOST_PEPPER_PATCH, ghostPeppers, new PlacementModifier[]{PlacementUtils.FULL_RANGE, BiomeFilter.biome(), CountPlacement.of(96), RandomOffsetPlacement.ofTriangle(7, 3), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)});
    }
}