package net.nullcoil.soulscorch.loot;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.nullcoil.soulscorch.Soulscorch;

import java.util.List;

public class ModLootTables {
    public static final ResourceKey<LootTable> BASTION_ENCHANTS = inject("bastion_enchants");
    public static final ResourceKey<LootTable> BASTION_ITEMS = inject("bastion_items");
    public static final ResourceKey<LootTable> BASTION_PEASANTRIES = inject("bastion_peasantries");

    private static ResourceKey<LootTable> inject(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Soulscorch.id("inject/" + path));
    }

    private static Holder<LootTable> holder(HolderLookup.Provider registries, ResourceKey<LootTable> key) {
        return registries.lookupOrThrow(Registries.LOOT_TABLE).getOrThrow(key);
    }

    public static void register() {
        Soulscorch.LOGGER.info("Registering Loot Tables for " + Soulscorch.MOD_ID);

        List<ResourceKey<LootTable>> bastions = List.of(
                BuiltInLootTables.BASTION_TREASURE,
                BuiltInLootTables.BASTION_OTHER,
                BuiltInLootTables.BASTION_BRIDGE
        );

        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (bastions.contains(key)) {
                builder.withPool(LootPool.lootPool().add(
                        NestedLootTable.lootTableReference(holder(registries, BASTION_ENCHANTS))));
                builder.withPool(LootPool.lootPool().add(
                        NestedLootTable.lootTableReference(holder(registries, BASTION_ITEMS))));
                builder.withPool(LootPool.lootPool().add(
                        NestedLootTable.lootTableReference(holder(registries, BASTION_PEASANTRIES))));
            }

            if (key.equals(BuiltInLootTables.BASTION_HOGLIN_STABLE)) {
                builder.withPool(LootPool.lootPool().add(
                        NestedLootTable.lootTableReference(holder(registries, BASTION_PEASANTRIES))));
            }
        });
    }
}