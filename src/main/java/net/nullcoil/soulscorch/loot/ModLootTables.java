package net.nullcoil.soulscorch.loot;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
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
    public static void register() {
        Soulscorch.LOGGER.info("Registering Loot Tables for " + Soulscorch.MOD_ID);
        List<ResourceKey<LootTable>> bastions = List.of(
                BuiltInLootTables.BASTION_TREASURE,
                BuiltInLootTables.BASTION_OTHER,
                BuiltInLootTables.BASTION_BRIDGE
        );
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if(bastions.contains(key)) {
                builder.withPool(LootPool.lootPool().add(
                        NestedLootTable.lootTableReference(
                                ResourceKey.create(Registries.LOOT_TABLE, Soulscorch.id("inject/bastion_enchants"))
                        )
                ));
                builder.withPool(LootPool.lootPool().add(
                        NestedLootTable.lootTableReference(
                                ResourceKey.create(Registries.LOOT_TABLE, Soulscorch.id("inject/bastion_items"))
                        )
                ));
                builder.withPool(LootPool.lootPool().add(
                        NestedLootTable.lootTableReference(
                                ResourceKey.create(Registries.LOOT_TABLE, Soulscorch.id("inject/bastion_peasantries"))
                        )
                ));
            }
        });

        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if(key.equals(BuiltInLootTables.BASTION_HOGLIN_STABLE)) {
                builder.withPool(LootPool.lootPool().add(
                        NestedLootTable.lootTableReference(
                                ResourceKey.create(Registries.LOOT_TABLE, Soulscorch.id("inject/bastion_peasantries"))
                        )
                ));
            }
        });
    }
}
