package net.nullcoil.soulscorch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.nullcoil.soulscorch.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootTableProvider extends FabricBlockLootSubProvider {
    public ModBlockLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate() {
        dropSelf(ModBlocks.SOUL_BREWING_STAND);
        dropSelf(ModBlocks.IRON_BULB_BLOCK);
        dropSelf(ModBlocks.CERULEAN_FROGLIGHT);
        dropSelf(ModBlocks.SOUL_SLAG_BLOCK);
        dropSelf(ModBlocks.SEEPING_SALLOW_SAPLING);
        dropSelf(ModBlocks.SOUL_STONE);
        // GHOST_PEPPER_SHRUB intentionally left out
    }
}