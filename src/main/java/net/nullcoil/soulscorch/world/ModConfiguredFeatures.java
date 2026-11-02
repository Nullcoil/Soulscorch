package net.nullcoil.soulscorch.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.block.ModBlocks;
import net.nullcoil.soulscorch.block.custom.GhostPepperShrubBlock;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> GHOST_PEPPER_SHRUB =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    Identifier.fromNamespaceAndPath(Soulscorch.MOD_ID, "ghost_pepper_shrub"));

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
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