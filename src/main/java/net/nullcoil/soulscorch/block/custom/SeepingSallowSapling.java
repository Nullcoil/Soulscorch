package net.nullcoil.soulscorch.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.Optional;
import net.nullcoil.soulscorch.sound.ModSounds;

public class SeepingSallowSapling extends SaplingBlock {

    private static final ResourceKey<Feature> SEEPING_SALLOW_TREE =
            ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath("soulscorch", "seeping_sallow"));

    private static final TreeGrower SEEPING_SALLOW = new TreeGrower(
            "seeping_sallow",
            WeightedList.of(SEEPING_SALLOW_TREE),
            WeightedList.of(),
            WeightedList.of(),
            SEEPING_SALLOW_TREE
    );

    public SeepingSallowSapling(Properties properties) {
        super(SEEPING_SALLOW, properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SOUL_SOIL) || state.is(BlockTags.DIRT);
    }
}