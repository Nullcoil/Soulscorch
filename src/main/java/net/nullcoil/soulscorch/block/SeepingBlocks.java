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
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.block.custom.*;
import net.nullcoil.soulscorch.particles.ModParticles;

import java.util.function.Function;

public class SeepingBlocks {
    public static final Block SEEPING_LEAVES = registerBlock("seeping_leaves",
            props -> new SeepingLeaves(0.02f, ModParticles.SEEPING_SALLOW_LEAVES, AmbientLeavesBlockSoundPlayer.noAmbientSound(), props), BlockBehaviour.Properties.of()
                    .strength(0.2f)
                    .randomTicks()
                    .sound(SoundType.LEAF_LITTER)
                    .noOcclusion()
                    .isValidSpawn(Blocks::ocelotOrParrot)
                    .isSuffocating(Blocks::never)
                    .pushReaction(PushReaction.POPPED)
                    .isRedstoneConductor(Blocks::never));

    public static final Block SEEPING_LOG = registerBlock("seeping_log",
            SeepingLog::new, BlockBehaviour.Properties.of()
                    .mapColor(Blocks.SOUL_SOIL.defaultMapColor())
                    .randomTicks()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block SEEPING_WOOD = registerBlock("seeping_wood",
            SeepingLog::new, BlockBehaviour.Properties.of()
                    .mapColor(Blocks.SOUL_SOIL.defaultMapColor())
                    .randomTicks()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block STRIPPED_SEEPING_LOG = registerBlock("stripped_seeping_log",
            SeepingLog::new, BlockBehaviour.Properties.of()
                    .mapColor(Blocks.SOUL_SOIL.defaultMapColor())
                    .randomTicks()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block STRIPPED_SEEPING_WOOD = registerBlock("stripped_seeping_wood",
            SeepingLog::new, BlockBehaviour.Properties.of()
                    .mapColor(Blocks.SOUL_SOIL.defaultMapColor())
                    .randomTicks()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block SEEPING_PLANKS = registerBlock("seeping_planks",
            SeepingPlanks::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .randomTicks()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block SEEPING_BUTTON = registerBlock("seeping_button",
            properties -> new SeepingButton(ModBlockSets.SEEPING, 30, properties), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .randomTicks()
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block SEEPING_DOOR = registerBlock("seeping_door",
            properties -> new SeepingDoor(ModBlockSets.SEEPING, properties), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .randomTicks()
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block SEEPING_FENCE = registerBlock("seeping_fence",
            SeepingFence::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .randomTicks()
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block SEEPING_FENCE_GATE = registerBlock("seeping_fence_gate",
            properties -> new SeepingFenceGate(ModWoodTypes.SEEPING, properties), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .randomTicks()
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block SEEPING_PRESSURE_PLATE = registerBlock("seeping_pressure_plate",
            properties -> new SeepingPressurePlate(ModBlockSets.SEEPING, properties), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .randomTicks()
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block SEEPING_SLAB = registerBlock("seeping_slab",
            SeepingSlab::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .randomTicks()
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block SEEPING_STAIRS = registerBlock("seeping_stairs",
            properties -> new SeepingStairs(SEEPING_PLANKS.defaultBlockState(), properties), BlockBehaviour.Properties.ofFullCopy(SEEPING_PLANKS)
                    .mapColor(MapColor.COLOR_BLACK)
                    .randomTicks()
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block SEEPING_TRAPDOOR = registerBlock("seeping_trapdoor",
            properties -> new SeepingTrapdoor(ModBlockSets.SEEPING, properties), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .randomTicks()
                    .strength(2f)
                    .sound(SoundType.WOOD));

    public static final Block SEEPING_WALL_SIGN = registerSignBlock(
            Soulscorch.id("seeping_wall_sign"),
            properties -> new SeepingWallSign(ModWoodTypes.SEEPING, properties),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).forceSolidOn()
                    .instrument(NoteBlockInstrument.BASS).noCollision().strength(1f));

    public static final Block SEEPING_SIGN = registerSignBlock(
            Soulscorch.id("seeping_sign"),
            properties -> new SeepingSign(ModWoodTypes.SEEPING, properties),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).forceSolidOn()
                    .instrument(NoteBlockInstrument.BASS).noCollision().strength(1f));

    public static final Block SEEPING_WALL_HANGING_SIGN = registerSignBlock(
            Soulscorch.id("seeping_wall_hanging_sign"),
            properties -> new SeepingWallHangingSign(ModWoodTypes.SEEPING, properties),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).forceSolidOn()
                    .instrument(NoteBlockInstrument.BASS).noCollision().strength(1f));

    public static final Block SEEPING_HANGING_SIGN = registerSignBlock(
            Soulscorch.id("seeping_hanging_sign"),
            properties -> new SeepingHangingSign(ModWoodTypes.SEEPING, properties),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).forceSolidOn()
                    .instrument(NoteBlockInstrument.BASS).noCollision().strength(1f));

    public static final Block SEEPING_SHELF = registerBlock("seeping_shelf",
            SeepingShelf::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .instrument(NoteBlockInstrument.BASS)
                    .sound(SoundType.SHELF)
                    .strength(2f,3f));

    public static final BlockFamily SEEPING_FAMILY = BlockFamilies.familyBuilder(SeepingBlocks.SEEPING_PLANKS)
            .button(SeepingBlocks.SEEPING_BUTTON)
            .door(SeepingBlocks.SEEPING_DOOR)
            .fence(SeepingBlocks.SEEPING_FENCE)
            .fenceGate(SeepingBlocks.SEEPING_FENCE_GATE)
            .pressurePlate(SeepingBlocks.SEEPING_PRESSURE_PLATE)
            .sign(SeepingBlocks.SEEPING_SIGN, SeepingBlocks.SEEPING_WALL_SIGN)
            .slab(SeepingBlocks.SEEPING_SLAB)
            .stairs(SeepingBlocks.SEEPING_STAIRS)
            .trapdoor(SeepingBlocks.SEEPING_TRAPDOOR)
            .recipeGroupPrefix("wooden")
            .recipeUnlockedBy("has_planks")
            .getFamily();

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

    public static void register() {
        BlockTransformerHelper.registerStripping(SEEPING_LOG, STRIPPED_SEEPING_LOG);
        BlockTransformerHelper.registerStripping(SEEPING_WOOD, STRIPPED_SEEPING_WOOD);
        BlockEntityTypes.SHELF.addValidBlock(SEEPING_SHELF);
        Soulscorch.LOGGER.info("Registering Seeping Blocks for " + Soulscorch.MOD_ID);
    }
}
