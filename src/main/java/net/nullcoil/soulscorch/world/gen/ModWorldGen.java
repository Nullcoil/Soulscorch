package net.nullcoil.soulscorch.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.world.ModPlacedFeatures;
import net.nullcoil.soulscorch.world.biome.ModBiomes;

public class ModWorldGen {
    /**
     * Seeping Sallow trees on the Soulvore Caverns' Soul Soil floors, only where there's
     * room for a whole tree (big caves). Hand-written JSON, like the tree itself:
     * data/soulscorch/worldgen/placed_feature/soulvore_seeping_sallow.json
     */
    public static final ResourceKey<PlacedFeature> SOULVORE_SEEPING_SALLOW =
            ResourceKey.create(Registries.PLACED_FEATURE, Soulscorch.id("soulvore_seeping_sallow"));

    public static void generate() {
        Soulscorch.LOGGER.info("Registering Ghost Pepper worldgen feature...");
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                ModPlacedFeatures.GHOST_PEPPER_PATCH
        );
        Soulscorch.LOGGER.info("Ghost Pepper feature registered.");

        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CAVERNS),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                SOULVORE_SEEPING_SALLOW
        );
    }
}