package net.nullcoil.soulscorch.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.ModEntities;
import net.nullcoil.soulscorch.entity.ai.Blazt;
import net.nullcoil.soulscorch.entity.ai.ghaze.Ghaze;
import net.nullcoil.soulscorch.entity.ai.Restless;
import net.nullcoil.soulscorch.entity.ai.soulless.SoullessEntity;
import net.nullcoil.soulscorch.entity.ai.heedless.Heedless;
import net.nullcoil.soulscorch.entity.ai.jellyfish.AbstractJellyfish;
import net.nullcoil.soulscorch.world.biome.ModBiomes;

public class ModEntitySpawns {
    public static void register() {
        Soulscorch.LOGGER.info("Adding Mod Entity Spawns for " + Soulscorch.MOD_ID);

        // =========================================
        // 1. Biome Spawn Rates (Where they spawn)
        // =========================================

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
                MobCategory.MONSTER, ModEntities.BLAZT, 8, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CRAGS),
                MobCategory.MONSTER, ModEntities.BLAZT, 20, 1, 1);

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
                MobCategory.MONSTER, ModEntities.SOULLESS, 100, 2, 8);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CAVERNS),
                MobCategory.MONSTER, ModEntities.SOULLESS, 120, 4, 16);

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CAVERNS),
                MobCategory.MONSTER, ModEntities.HEEDLESS, 40, 1, 1);

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
                MobCategory.MONSTER, ModEntities.RESTLESS, 20, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CAVERNS),
                MobCategory.MONSTER, ModEntities.RESTLESS, 60, 1, 2);

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
                MobCategory.MONSTER, ModEntities.HYTODOM, 1, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CRAGS),
                MobCategory.MONSTER, ModEntities.HYTODOM, 5, 2, 5);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CAVERNS),
                MobCategory.MONSTER, ModEntities.HYTODOM, 120, 3, 8);

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CAVERNS),
                MobCategory.MONSTER, ModEntities.GHAZE, 60, 1 ,1);


        // =========================================
        // 2. Global Spawn Rules (How they spawn)
        // =========================================

        SpawnPlacements.register(ModEntities.BLAZT, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Blazt::checkMobSpawnRules);

        SpawnPlacements.register(ModEntities.SOULLESS, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING, SoullessEntity::checkMobSpawnRules);

        SpawnPlacements.register(ModEntities.HEEDLESS, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING, Heedless::checkMobSpawnRules);

        SpawnPlacements.register(ModEntities.RESTLESS, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING, Restless::checkMobSpawnRules);

        SpawnPlacements.register(ModEntities.HYTODOM, SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, AbstractJellyfish::checkJellyfishSpawnRules);

        SpawnPlacements.register(ModEntities.GHAZE, SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Ghaze::checkMobSpawnRules);
    }
}