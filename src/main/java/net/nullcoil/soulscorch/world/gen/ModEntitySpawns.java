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
import net.nullcoil.soulscorch.entity.ai.BlaztEntity;
import net.nullcoil.soulscorch.entity.ai.RestlessEntity;
import net.nullcoil.soulscorch.entity.ai.SoullessEntity;
import net.nullcoil.soulscorch.entity.ai.jellyfish.JellyfishEntity;
import net.nullcoil.soulscorch.util.ModTags;
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
                MobCategory.MONSTER, ModEntities.SOULLESS, 200, 4, 16);

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CAVERNS),
                MobCategory.MONSTER, ModEntities.HEEDLESS, 50, 1, 1);

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
                MobCategory.MONSTER, ModEntities.RESTLESS, 20, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CAVERNS),
                MobCategory.MONSTER, ModEntities.SOULLESS, 40, 1, 2);

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
                MobCategory.MONSTER, ModEntities.HYTODOM, 1, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CRAGS),
                MobCategory.MONSTER, ModEntities.HYTODOM, 5, 2, 5);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ModBiomes.SOULVORE_CAVERNS),
                MobCategory.MONSTER, ModEntities.HYTODOM, 10, 3, 8);


        // =========================================
        // 2. Global Spawn Rules (How they spawn)
        // =========================================

        SpawnPlacements.register(ModEntities.BLAZT, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlaztEntity::checkMobSpawnRules);

        SpawnPlacements.register(ModEntities.SOULLESS, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING, SoullessEntity::checkMobSpawnRules);

        SpawnPlacements.register(ModEntities.RESTLESS, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING, RestlessEntity::checkMobSpawnRules);

        SpawnPlacements.register(ModEntities.HYTODOM, SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, JellyfishEntity::checkJellyfishSpawnRules);
    }
}