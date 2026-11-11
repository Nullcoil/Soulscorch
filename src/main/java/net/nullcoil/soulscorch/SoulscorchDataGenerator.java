package net.nullcoil.soulscorch;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.nullcoil.soulscorch.datagen.ModBlockLootTableProvider;
import net.nullcoil.soulscorch.datagen.ModBlockTagProvider;
import net.nullcoil.soulscorch.datagen.ModWorldGenProvider;
import net.nullcoil.soulscorch.world.ModConfiguredFeatures;
import net.nullcoil.soulscorch.world.ModPlacedFeatures;
import net.nullcoil.soulscorch.world.biome.ModBiomes;

public class SoulscorchDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(ModWorldGenProvider::new);
		pack.addProvider(ModBlockLootTableProvider::new);
		pack.addProvider(ModBlockTagProvider::new);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		registryBuilder.add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
		registryBuilder.add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap);
		registryBuilder.add(Registries.BIOME, ModBiomes::bootstrap);
	}
}