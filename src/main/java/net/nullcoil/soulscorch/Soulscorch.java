package net.nullcoil.soulscorch;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.Identifier;
import net.nullcoil.soulscorch.advancement.ModCriteriaTriggers;
import net.nullcoil.soulscorch.alchemy.ModAlchemy;
import net.nullcoil.soulscorch.attribute.ModAttributes;
import net.nullcoil.soulscorch.block.ModBlockEntities;
import net.nullcoil.soulscorch.block.ModBlocks;
import net.nullcoil.soulscorch.block.ModWoodTypes;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.enchantment.ModEnchantments;
import net.nullcoil.soulscorch.entity.ModEntities;
import net.nullcoil.soulscorch.event.ModEvents;
import net.nullcoil.soulscorch.fire.ModFire;
import net.nullcoil.soulscorch.item.ModItems;
import net.nullcoil.soulscorch.loot.ModLootTables;
import net.nullcoil.soulscorch.particles.ModParticles;
import net.nullcoil.soulscorch.sound.ModSounds;
import net.nullcoil.soulscorch.util.SampleBiomeCommand;
import net.nullcoil.soulscorch.world.gen.ModEntitySpawns;
import net.nullcoil.soulscorch.world.gen.ModWorldGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Soulscorch implements ModInitializer {
	public static final String MOD_ID = "soulscorch";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID); //test

	@Override
	public void onInitialize() {
		ModAttributes.register();
		ModWoodTypes.register();
		ModItems.register();
		ModBlocks.register();
		ModBlockEntities.register();
		ModEffects.register();
		ModEvents.register();
		ModFire.register();
		ModEntities.register();
		ModSounds.register();
		ModLootTables.register();
		ModEntitySpawns.register();
		ModAlchemy.register();
		ModEnchantments.register();
		ModParticles.register();
		ModCriteriaTriggers.register();
		ModWorldGen.generate();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				SampleBiomeCommand.register(dispatcher));

		LOGGER.info("Soulscorch initialized successfully");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}