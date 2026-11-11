package net.nullcoil.soulscorch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.nullcoil.soulscorch.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public ModBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // These use requiresCorrectToolForDrops(), so they only drop
        // when mined with a tool from a matching mineable tag.
        builder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(key(ModBlocks.SOUL_BREWING_STAND))
                .add(key(ModBlocks.IRON_BULB_BLOCK))
                .add(key(ModBlocks.SOUL_SLAG_BLOCK))
                .add(key(ModBlocks.SOUL_STONE));

        // Iron bulb needs at least a stone pickaxe, like vanilla's iron-tier blocks
        builder(BlockTags.NEEDS_STONE_TOOL)
                .add(key(ModBlocks.IRON_BULB_BLOCK));
    }

    // 26.2 tag builders take ids, not Block instances
    private static ResourceKey<Block> key(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
    }
}