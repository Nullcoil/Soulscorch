package net.nullcoil.soulscorch.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.block.ModBlocks;
import net.nullcoil.soulscorch.block.SallowBlocks;
import net.nullcoil.soulscorch.block.SeepingBlocks;
import net.nullcoil.soulscorch.entity.ModBoats;
import net.nullcoil.soulscorch.entity.ModEntities;
import net.nullcoil.soulscorch.item.custom.CandiedGhostPepperItem;
import net.nullcoil.soulscorch.item.custom.GhostPepperItem;
import net.nullcoil.soulscorch.item.custom.SoulwardTotemItem;
import net.nullcoil.soulscorch.item.foods.ModConsumables;
import net.nullcoil.soulscorch.item.foods.ModFoods;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ModItems {
    public static final Item BLAZT_POWDER;
    public static final Item BLAZT_ROD;
    public static final Item SOUL_CREAM;
    public static final Item SOUL_CHARGE;
    public static final Item SOUL_SHARD;
    public static final Item SOULWARD_TOTEM;
    public static final Item SOULLESS_SPAWN_EGG;
    public static final Item BLAZT_SPAWN_EGG;
    public static final Item RESTLESS_SPAWN_EGG;
    public static final Item HYTODOM_SPAWN_EGG;
    public static final Item SOULCAT_SPAWN_EGG;
    public static final Item SOULWOLF_SPAWN_EGG;
    public static final Item HEEDLESS_SPAWN_EGG;
    public static final Item GHOST_PEPPER;
    public static final Item CANDIED_GHOST_PEPPER;
    public static final Item SEEPING_SIGN;
    public static final Item SEEPING_HANGING_SIGN;
    public static final Item SALLOW_SIGN;
    public static final Item SALLOW_HANGING_SIGN;
    public static final Item AVALCOMB;
    public static final Item VOLATILE_EXTRACT;

    static {
        BLAZT_POWDER = registerItem("blazt_powder", Item::new, new Item.Properties());
        BLAZT_ROD = registerItem("blazt_rod", Item::new, new Item.Properties());
        SOUL_CREAM = registerItem("soul_cream", Item::new, new Item.Properties());
        SOUL_CHARGE = registerItem("soul_charge", FireChargeItem::new, new Item.Properties());
        SOUL_SHARD = registerItem("soul_shard", Item::new, new Item.Properties());
        SOULWARD_TOTEM = registerItem("soulward_totem", SoulwardTotemItem::new, new Item.Properties().stacksTo(1));
        SOULLESS_SPAWN_EGG = registerItem("soulless_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SOULLESS));
        BLAZT_SPAWN_EGG = registerItem("blazt_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.BLAZT));
        RESTLESS_SPAWN_EGG = registerItem("restless_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.RESTLESS));
        HYTODOM_SPAWN_EGG = registerItem("hytodom_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.HYTODOM));
        SOULCAT_SPAWN_EGG = registerItem("soulcat_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SOULBORNE_CAT));
        SOULWOLF_SPAWN_EGG = registerItem("soulwolf_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SOULBORNE_WOLF));
        HEEDLESS_SPAWN_EGG = registerItem("heedless_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.HEEDLESS));
        GHOST_PEPPER = registerItem("ghost_pepper", ModBlocks.GHOST_PEPPER_SHRUB,
                GhostPepperItem::new, new Item.Properties().food(ModFoods.GHOST_PEPPER));
        CANDIED_GHOST_PEPPER = registerItem("candied_ghost_pepper",
                CandiedGhostPepperItem::new, new Item.Properties().food(ModFoods.CANDIED_GHOST_PEPPER));
        SEEPING_SIGN = registerItem("seeping_sign",
                properties -> new StandingAndWallBlockItem(SeepingBlocks.SEEPING_SIGN, SeepingBlocks.SEEPING_WALL_SIGN, Direction.DOWN, properties),
                new Item.Properties().stacksTo(16));
        SEEPING_HANGING_SIGN = registerItem("seeping_hanging_sign",
                properties -> new HangingSignItem(SeepingBlocks.SEEPING_HANGING_SIGN, SeepingBlocks.SEEPING_WALL_HANGING_SIGN, properties),
                new Item.Properties().stacksTo(16));
        SALLOW_SIGN = registerItem("sallow_sign",
                properties -> new StandingAndWallBlockItem(SallowBlocks.SALLOW_SIGN, SallowBlocks.SALLOW_WALL_SIGN, Direction.DOWN, properties),
                new Item.Properties().stacksTo(16));
        SALLOW_HANGING_SIGN = registerItem("sallow_hanging_sign",
                properties -> new HangingSignItem(SallowBlocks.SALLOW_HANGING_SIGN, SallowBlocks.SALLOW_WALL_HANGING_SIGN, properties),
                new Item.Properties().stacksTo(16));
        AVALCOMB = registerItem("avalcomb", Item::new, new Item.Properties());

        VOLATILE_EXTRACT = registerItem("volatile_extract", Item::new, new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).food(
                ModFoods.VOLATILE_EXTRACT, ModConsumables.VOLATILE_EXTRACT).usingConvertsTo(Items.GLASS_BOTTLE).stacksTo(16));
    }

    // 2. The completed helper method
    private static Item registerItem(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        // Build the Identifier
        Identifier id = Soulscorch.id(name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        Item item = factory.apply(properties.setId(itemKey));
        return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

    private static Item registerItem(String name, Block block, BiFunction<Block, Item.Properties, Item> factory, Item.Properties properties) {
        Identifier id = Soulscorch.id(name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        // Applies both the block and the modified properties to the constructor
        Item item = factory.apply(block, properties.setId(itemKey));
        return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

    public static void addToCreativeMenu() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.insertAfter(Items.BLAZE_POWDER, BLAZT_POWDER);
            entries.insertAfter(Items.BLAZE_ROD, BLAZT_ROD);
            entries.insertAfter(Items.MAGMA_CREAM, SOUL_CREAM);
            entries.insertAfter(Items.FIRE_CHARGE, SOUL_CHARGE);
            entries.insertAfter(Items.BREEZE_ROD, SOUL_SHARD);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.insertAfter(Items.BAMBOO_RAFT,
                    ModBoats.CRIMSON_BOAT.item(),
                    ModBoats.CRIMSON_CHEST_BOAT.item(),
                    ModBoats.WARPED_BOAT.item(),
                    ModBoats.WARPED_CHEST_BOAT.item(),
                    ModBoats.SEEPING_BOAT.item(),
                    ModBoats.SEEPING_CHEST_BOAT.item(),
                    ModBoats.SALLOW_BOAT.item(),
                    ModBoats.SALLOW_CHEST_BOAT.item());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.insertAfter(Items.TOTEM_OF_UNDYING, SOULWARD_TOTEM);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.insertAfter(Items.BREWING_STAND, ModBlocks.SOUL_BREWING_STAND);
            entries.insertBefore(Items.COPPER_BULB.weathering().unaffected(), ModBlocks.IRON_BULB_BLOCK);
            entries.insertAfter(Items.PEARLESCENT_FROGLIGHT, ModBlocks.CERULEAN_FROGLIGHT);
            entries.insertAfter(Items.WARPED_HANGING_SIGN,
                    SEEPING_SIGN,
                    SEEPING_HANGING_SIGN,
                    SALLOW_SIGN,
                    SALLOW_HANGING_SIGN);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(entries -> {
            entries.insertBefore(Items.COPPER_BULB.waxed().unaffected(), ModBlocks.IRON_BULB_BLOCK);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            entries.insertAfter(Items.PEARLESCENT_FROGLIGHT, ModBlocks.CERULEAN_FROGLIGHT);
            entries.insertAfter(Items.MAGMA_BLOCK, ModBlocks.SOUL_SLAG_BLOCK);
            entries.insertAfter(Items.FLOWERING_AZALEA_LEAVES, SeepingBlocks.SEEPING_LEAVES);
            entries.insertAfter(Items.PALE_OAK_SAPLING, ModBlocks.SEEPING_SALLOW_SAPLING);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            entries.accept(SOULLESS_SPAWN_EGG);
            entries.accept(HEEDLESS_SPAWN_EGG);
            entries.accept(BLAZT_SPAWN_EGG);
            entries.accept(RESTLESS_SPAWN_EGG);
            entries.accept(HYTODOM_SPAWN_EGG);
            entries.accept(SOULCAT_SPAWN_EGG);
            entries.accept(SOULWOLF_SPAWN_EGG);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
            entries.insertAfter(Items.BEETROOT, GHOST_PEPPER, CANDIED_GHOST_PEPPER);
            entries.insertAfter(Items.HONEY_BOTTLE, VOLATILE_EXTRACT);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            entries.insertAfter(Items.WARPED_BUTTON,
                    SeepingBlocks.SEEPING_LOG,
                    SeepingBlocks.SEEPING_WOOD,
                    SeepingBlocks.SEEPING_PLANKS,
                    SeepingBlocks.STRIPPED_SEEPING_LOG,
                    SeepingBlocks.STRIPPED_SEEPING_WOOD,
                    SeepingBlocks.SEEPING_BUTTON,
                    SeepingBlocks.SEEPING_DOOR,
                    SeepingBlocks.SEEPING_FENCE,
                    SeepingBlocks.SEEPING_FENCE_GATE,
                    SeepingBlocks.SEEPING_PRESSURE_PLATE,
                    SeepingBlocks.SEEPING_SLAB,
                    SeepingBlocks.SEEPING_STAIRS,
                    SeepingBlocks.SEEPING_TRAPDOOR,
                    SallowBlocks.SALLOW_LOG,
                    SallowBlocks.SALLOW_WOOD,
                    SallowBlocks.SALLOW_PLANKS,
                    SallowBlocks.STRIPPED_SALLOW_LOG,
                    SallowBlocks.STRIPPED_SALLOW_WOOD,
                    SallowBlocks.SALLOW_BUTTON,
                    SallowBlocks.SALLOW_DOOR,
                    SallowBlocks.SALLOW_FENCE,
                    SallowBlocks.SALLOW_FENCE_GATE,
                    SallowBlocks.SALLOW_PRESSURE_PLATE,
                    SallowBlocks.SALLOW_SLAB,
                    SallowBlocks.SALLOW_STAIRS,
                    SallowBlocks.SALLOW_TRAPDOOR);
            entries.insertAfter(Items.HEAVY_WEIGHTED_PRESSURE_PLATE, ModBlocks.IRON_BULB_BLOCK);
        });
    }

    public static void register() {
        addToCreativeMenu();
        Soulscorch.LOGGER.info("Registering Mod Items for " + Soulscorch.MOD_ID);
    }
}