package net.nullcoil.soulscorch.world.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.nullcoil.nidalib.worldgen.biome.SubBiomeRule;
import net.nullcoil.nidalib.worldgen.biome.SubBiomes;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.world.ModPlacedFeatures;

public class ModBiomes {
    /** Outer ring: craggy Soul Stone, Basalt Delta-style. Placed inside Soul Sand Valleys by Nidalib. */
    public static final ResourceKey<Biome> SOULVORE_CRAGS =
            ResourceKey.create(Registries.BIOME, Soulscorch.id("soulvore_crags"));

    /** Inner core: solid Soul Stone with carved caves. Placed inside the crags by Nidalib. */
    public static final ResourceKey<Biome> SOULVORE_CAVERNS =
            ResourceKey.create(Registries.BIOME, Soulscorch.id("soulvore_caverns"));

    private static final int SOULVORE_FOG = 0x352922;

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(SOULVORE_CRAGS, soulvoreCrags(placedFeatures, carvers));
        context.register(SOULVORE_CAVERNS, soulvoreCaverns(placedFeatures, carvers));
    }

    private static Biome soulvoreCrags(HolderGetter<PlacedFeature> placedFeatures,
                                       HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder generation =
                new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        // Mirrors vanilla Basalt Deltas: lava deltas and columns shape the surface.
        // Layering (Soul Stone, Blackstone, Netherrack) is done by SoulvoreFill.
        generation.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, ModPlacedFeatures.SOULVORE_DELTA);
        generation.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, ModPlacedFeatures.SMALL_SOUL_STONE_COLUMNS);
        generation.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, ModPlacedFeatures.LARGE_SOUL_STONE_COLUMNS);

        return baseBiome(new MobSpawnSettings.Builder(), generation);
    }

    private static Biome soulvoreCaverns(HolderGetter<PlacedFeature> placedFeatures,
                                         HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder generation =
                new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        // Carver, Soul Soil floors and Seeping Sallow trees come in the caverns step

        return baseBiome(new MobSpawnSettings.Builder(), generation);
    }

    private static Biome baseBiome(MobSpawnSettings.Builder spawns, BiomeGenerationSettings.Builder generation) {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(2.0F)
                .downfall(0.0F)
                // Fog color is an environment attribute now, not part of special effects
                .setAttribute(EnvironmentAttributes.FOG_COLOR, SOULVORE_FOG)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x3F76E4)
                        .build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    public static void register() {
        // Both are nested inside Soul Sand Valley, judged by how deep into the valley a spot is.
        // Rules nest, so the inner threshold must be lower than the outer one:
        //   ratio 0.0 - 0.4  -> Soulvore Caverns (center)
        //   ratio 0.4 - 0.7  -> Soulvore Crags (ring)
        //   ratio 0.7 - 1.0  -> Soul Sand Valley (outer edge, unchanged)
        SubBiomes.add(Biomes.SOUL_SAND_VALLEY, SOULVORE_CRAGS, SubBiomeRule.core(0.7));
        SubBiomes.add(SOULVORE_CRAGS, SOULVORE_CAVERNS, SubBiomeRule.core(0.4));
    }
}