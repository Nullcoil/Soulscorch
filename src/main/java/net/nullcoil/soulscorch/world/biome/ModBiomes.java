package net.nullcoil.soulscorch.world.biome;

import net.fabricmc.fabric.api.biome.v1.NetherBiomes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.nullcoil.soulscorch.Soulscorch;

public class ModBiomes {
    public static final ResourceKey<Biome> SOULVORE_CAVERNS =
            ResourceKey.create(Registries.BIOME, Soulscorch.id("soulvore_caverns"));

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(SOULVORE_CAVERNS, soulvoreCaverns(placedFeatures, carvers));
    }

    private static Biome soulvoreCaverns(HolderGetter<PlacedFeature> placedFeatures,
                                         HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();

        BiomeGenerationSettings.Builder generation =
                new BiomeGenerationSettings.Builder(placedFeatures, carvers);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(2.0F)
                .downfall(0.0F)
                // Fog color is an environment attribute now, not part of special effects
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 0x352922)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x3F76E4)
                        .build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    public static void register() {
        NetherBiomes.addNetherBiome(SOULVORE_CAVERNS,
                Climate.parameters(-.3f, -.6f, 0f, 0f, 0f, 0f, 0f));
    }
}