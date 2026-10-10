package net.nullcoil.soulscorch.block;

import net.fabricmc.fabric.api.item.v1.BlockTransformerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.nullcoil.soulscorch.Soulscorch;

import java.util.function.Function;

public class SallowBlocks {
    public static final Block SALLOW_LOG;
    public static final Block SALLOW_WOOD;
    public static final Block STRIPPED_SALLOW_LOG;
    public static final Block STRIPPED_SALLOW_WOOD;
    public static final Block SALLOW_PLANKS;
    public static final Block SALLOW_BUTTON;
    public static final Block SALLOW_DOOR;
    public static final Block SALLOW_FENCE;
    public static final Block SALLOW_FENCE_GATE;
    public static final Block SALLOW_PRESSURE_PLATE;
    public static final Block SALLOW_SLAB;
    public static final Block SALLOW_STAIRS;
    public static final Block SALLOW_TRAPDOOR;
    public static final Block SALLOW_WALL_SIGN;
    public static final Block SALLOW_SIGN;
    public static final Block SALLOW_WALL_HANGING_SIGN;
    public static final Block SALLOW_HANGING_SIGN;
    public static final Block SALLOW_SHELF;
    public static final BlockFamily SALLOW_FAMILY;

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        return registerBlock(name,factory,properties, true);
    }

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties, boolean buildItem) {
        Identifier id = Soulscorch.id(name);

        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        Block block = factory.apply(properties.setId(blockKey));
        if(buildItem) {
            Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));
        }

        // Register and return the Block
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    public static void register() {
        BlockEntityTypes.SHELF.addValidBlock(SALLOW_SHELF);
        BlockTransformerHelper.registerStripping(SALLOW_LOG, STRIPPED_SALLOW_LOG);
        BlockTransformerHelper.registerStripping(SALLOW_WOOD, STRIPPED_SALLOW_WOOD);
        Soulscorch.LOGGER.info("Registering Sallow Blocks for " + Soulscorch.MOD_ID);
    }

    public static <T extends SignBlock> T registerSignBlock(
            Identifier id,
            Function<BlockBehaviour.Properties, T> factory,
            BlockBehaviour.Properties settings) {

        // Correct way to create the key (ResourceKey, not RegistryKey)
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);

        // Apply the key to the properties — this is the important part
        BlockBehaviour.Properties settingsWithKey = settings.setId(blockKey);

        // Create the block instance using the factory
        T block = factory.apply(settingsWithKey);

        // Register the block
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        // Add the block to the correct BlockEntityType so signs render and function properly
        if (block instanceof StandingSignBlock || block instanceof WallSignBlock) {
            BlockEntityTypes.SIGN.addValidBlock(block);        // This is the correct method name in 26.1
        } else if (block instanceof CeilingHangingSignBlock || block instanceof WallHangingSignBlock) {
            BlockEntityTypes.HANGING_SIGN.addValidBlock(block);
        }

        return block;
    }

    static {
        SALLOW_LOG = registerBlock("sallow_log",
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_LOG));
        SALLOW_WOOD = registerBlock("sallow_wood",
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_WOOD));
        STRIPPED_SALLOW_LOG = registerBlock("stripped_sallow_log",
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.STRIPPED_SEEPING_LOG));
        STRIPPED_SALLOW_WOOD = registerBlock("stripped_sallow_wood",
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.STRIPPED_SEEPING_WOOD));
        SALLOW_PLANKS = registerBlock("sallow_planks",
                Block::new, BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_PLANKS));
        SALLOW_BUTTON = registerBlock("sallow_button",
                p -> new ButtonBlock(ModBlockSets.SALLOW, 30, p), BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_BUTTON));
        SALLOW_DOOR = registerBlock("sallow_door",
                p -> new DoorBlock(ModBlockSets.SALLOW, p), BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_DOOR));
        SALLOW_FENCE = registerBlock("sallow_fence",
                FenceBlock::new, BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_FENCE));
        SALLOW_FENCE_GATE = registerBlock("sallow_fence_gate",
                p -> new FenceGateBlock(ModWoodTypes.SALLOW, p), BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_FENCE_GATE));
        SALLOW_PRESSURE_PLATE = registerBlock("sallow_pressure_plate",
                p -> new PressurePlateBlock(ModBlockSets.SALLOW, p), BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_PRESSURE_PLATE));
        SALLOW_SLAB = registerBlock("sallow_slab",
                SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_SLAB));
        SALLOW_STAIRS = registerBlock("sallow_stairs",
                p -> new StairBlock(SALLOW_PLANKS.defaultBlockState(), p), BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_SLAB));
        SALLOW_TRAPDOOR = registerBlock("sallow_trapdoor",
                p -> new TrapDoorBlock(ModBlockSets.SALLOW, p), BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_TRAPDOOR));

        SALLOW_WALL_SIGN = registerSignBlock(
                Soulscorch.id("sallow_wall_sign"),
                p -> new WallSignBlock(ModWoodTypes.SALLOW, p),
                BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_WALL_SIGN)
        );
        SALLOW_SIGN = registerSignBlock(
                Soulscorch.id("sallow_sign"),
                p -> new StandingSignBlock(ModWoodTypes.SALLOW, p),
                BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_SIGN)
        );
        SALLOW_WALL_HANGING_SIGN = registerSignBlock(
                Soulscorch.id("sallow_wall_hanging_sign"),
                p -> new WallHangingSignBlock(ModWoodTypes.SALLOW, p),
                BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_WALL_HANGING_SIGN)
        );
        SALLOW_HANGING_SIGN = registerSignBlock(
                Soulscorch.id("sallow_hanging_sign"),
                p -> new CeilingHangingSignBlock(ModWoodTypes.SALLOW, p),
                BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_HANGING_SIGN)
        );
        SALLOW_SHELF = registerBlock("sallow_shelf",
                ShelfBlock::new, BlockBehaviour.Properties.ofFullCopy(SeepingBlocks.SEEPING_SHELF));
        SALLOW_FAMILY = BlockFamilies.familyBuilder(SallowBlocks.SALLOW_PLANKS)
                .button(SallowBlocks.SALLOW_BUTTON)
                .door(SallowBlocks.SALLOW_DOOR)
                .fence(SallowBlocks.SALLOW_FENCE)
                .fenceGate(SallowBlocks.SALLOW_FENCE_GATE)
                .pressurePlate(SallowBlocks.SALLOW_PRESSURE_PLATE)
                .sign(SallowBlocks.SALLOW_SIGN, SallowBlocks.SALLOW_WALL_SIGN)
                .slab(SallowBlocks.SALLOW_SLAB)
                .stairs(SallowBlocks.SALLOW_STAIRS)
                .trapdoor(SallowBlocks.SALLOW_TRAPDOOR)
                .recipeGroupPrefix("wooden")
                .recipeUnlockedBy("has_planks")
                .getFamily();
    }
}
