package net.nullcoil.soulscorch.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.entity.vehicle.NetherBoat;
import net.nullcoil.soulscorch.entity.vehicle.NetherChestBoat;
import net.nullcoil.soulscorch.entity.vehicle.SeepingBoat;
import net.nullcoil.soulscorch.entity.vehicle.SeepingChestBoat;

import java.util.function.Supplier;

public class ModBoats {
    public record BoatEntry(EntityType<? extends AbstractBoat> entityType, BoatItem item) {}

    public static final BoatEntry SALLOW_BOAT = registerBoat("sallow", false, false);
    public static final BoatEntry SALLOW_CHEST_BOAT = registerBoat("sallow", true, false);
    public static final BoatEntry CRIMSON_BOAT = registerBoat("crimson", false, false);
    public static final BoatEntry CRIMSON_CHEST_BOAT = registerBoat("crimson", true, false);
    public static final BoatEntry WARPED_BOAT = registerBoat("warped", false, false);
    public static final BoatEntry WARPED_CHEST_BOAT = registerBoat("warped", true, false);
    public static final BoatEntry SEEPING_BOAT = registerBoat("seeping", false, true);
    public static final BoatEntry SEEPING_CHEST_BOAT = registerBoat("seeping", true, true);

    private static BoatEntry registerBoat(String wood, boolean chest, boolean seeping) {
        Identifier id = Soulscorch.id(wood + (chest ? "_chest_boat" : "_boat"));
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        ResourceKey<EntityType<?>> entityKey = ResourceKey.create(Registries.ENTITY_TYPE, id);

        Item[] drop = {Items.AIR};
        Supplier<Item> dropSupplier = () -> drop[0];

        EntityType<? extends AbstractBoat> entityType;
        if (chest) {
            entityType = Registry.register(BuiltInRegistries.ENTITY_TYPE, entityKey,
                    EntityType.Builder.<NetherChestBoat>of(
                                    seeping
                                            ? (type, level) -> new SeepingChestBoat(type, level, dropSupplier)
                                            : (type, level) -> new NetherChestBoat(type, level, dropSupplier),
                                    MobCategory.MISC)
                            .noLootTable()
                            .sized(1.375F, 0.5625F)
                            .eyeHeight(0.5625F)
                            .clientTrackingRange(10)
                            .build(entityKey));
        } else {
            entityType = Registry.register(BuiltInRegistries.ENTITY_TYPE, entityKey,
                    EntityType.Builder.<NetherBoat>of(
                                    seeping
                                            ? (type, level) -> new SeepingBoat(type, level, dropSupplier)
                                            : (type, level) -> new NetherBoat(type, level, dropSupplier),
                                    MobCategory.MISC)
                            .noLootTable()
                            .sized(1.375F, 0.5625F)
                            .eyeHeight(0.5625F)
                            .clientTrackingRange(10)
                            .build(entityKey));
        }

        BoatItem item = Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BoatItem(entityType, new Item.Properties().stacksTo(1).fireResistant().setId(itemKey)));
        drop[0] = item;
        DispenserBlock.registerBehavior(item, new BoatDispenseItemBehavior(entityType));
        return new BoatEntry(entityType, item);
    }

    public static void register() {
        Soulscorch.LOGGER.info("Registering boats for " + Soulscorch.MOD_ID);
    }
}