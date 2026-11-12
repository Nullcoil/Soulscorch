package net.nullcoil.soulscorch.world.carver;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.nullcoil.soulscorch.Soulscorch;

/** Carver types (the code behind configured carvers). */
public class ModWorldCarvers {
    public static final WorldCarver<CaveCarverConfiguration> SOULVORE_CAVE = Registry.register(
            BuiltInRegistries.CARVER,
            Soulscorch.id("soulvore_cave"),
            new SoulvoreCaveCarver(CaveCarverConfiguration.CODEC));

    public static void register() {
        Soulscorch.LOGGER.info("Registering Mod Carvers for " + Soulscorch.MOD_ID);
    }
}
