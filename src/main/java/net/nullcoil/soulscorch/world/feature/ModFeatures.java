package net.nullcoil.soulscorch.world.feature;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.nullcoil.soulscorch.Soulscorch;

/** Feature types (the code behind configured features). */
public class ModFeatures {
    public static final Feature<BlockColumnConfiguration> BLOCK_COLUMNS = Registry.register(
            BuiltInRegistries.FEATURE,
            Soulscorch.id("block_columns"),
            new BlockColumnFeature(BlockColumnConfiguration.CODEC));

    public static void register() {
        Soulscorch.LOGGER.info("Registering Mod Features for " + Soulscorch.MOD_ID);
    }
}
