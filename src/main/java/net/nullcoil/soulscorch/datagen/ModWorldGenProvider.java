package net.nullcoil.soulscorch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;

import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends FabricDynamicRegistryProvider {
    public ModWorldGenProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        // We use the provider to look up the registry, then add all to entries
        // This avoids the 'RegistryLookup' vs 'Reference' type variable mismatch
        registries.lookup(Registries.FEATURE).ifPresent(entries::addAll);
        registries.lookup(Registries.PLACED_FEATURE).ifPresent(entries::addAll);
    }

    @Override
    public String getName() {
        return "Soulscorch World Gen";
    }
}