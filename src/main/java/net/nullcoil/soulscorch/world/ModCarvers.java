package net.nullcoil.soulscorch.world;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.block.ModBlocks;

public class ModCarvers {
    public static final ResourceKey<ConfiguredWorldCarver<?>> SOULVORE_CAVE =
            ResourceKey.create(Registries.CONFIGURED_CARVER, Soulscorch.id("soulvore_cave"));

    public static void bootstrap(BootstrapContext<ConfiguredWorldCarver<?>> context) {
        // Vanilla's Overworld cave carver (tunnels and rooms), allowed to cut only Soul Stone.
        // Vanilla's own carvers can't cut Soul Stone, so this is the only thing that can carve
        // the Soulvore Caverns, and its tunnels can break out into the crags as entrances.
        context.register(SOULVORE_CAVE, WorldCarver.CAVE.configured(new CaveCarverConfiguration(
                0.35F,                                                   // chance per chunk (Overworld caves: 0.15)
                UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.belowTop(8)),
                UniformFloat.of(0.1F, 0.9F),                             // vertical squash, as vanilla
                VerticalAnchor.aboveBottom(8),                           // lava below this
                HolderSet.direct(ModBlocks.SOUL_STONE.builtInRegistryHolder()),
                UniformFloat.of(0.9F, 1.6F),                             // wider than vanilla: bigger rooms
                UniformFloat.of(0.8F, 1.3F),                             // taller than vanilla
                ConstantFloat.of(-0.7F)                                  // flat-ish floors, as vanilla
        )));
    }
}
