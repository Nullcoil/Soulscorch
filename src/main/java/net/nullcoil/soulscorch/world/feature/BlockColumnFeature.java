package net.nullcoil.soulscorch.world.feature;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.jetbrains.annotations.Nullable;

/**
 * Vanilla's basalt column feature (Basalt Deltas), but the block is configurable
 * instead of hard-coded to basalt.
 */
public class BlockColumnFeature extends Feature<BlockColumnConfiguration> {
    private static final ImmutableSet<Block> CANNOT_PLACE_ON = ImmutableSet.of(
            Blocks.LAVA, Blocks.BEDROCK, Blocks.MAGMA_BLOCK, Blocks.SOUL_SAND,
            Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS,
            Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);

    public BlockColumnFeature(Codec<BlockColumnConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockColumnConfiguration> context) {
        int seaLevel = context.chunkGenerator().getSeaLevel();
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockColumnConfiguration config = context.config();

        if (!canPlaceAt(level, seaLevel, origin.mutable())) return false;

        int height = config.height().sample(random);
        boolean dense = random.nextFloat() < 0.9F;
        int spreadRadius = Math.min(height, dense ? 5 : 8);
        int attempts = dense ? 50 : 15;
        boolean placed = false;

        for (BlockPos pos : BlockPos.randomBetweenClosed(random, attempts,
                origin.getX() - spreadRadius, origin.getY(), origin.getZ() - spreadRadius,
                origin.getX() + spreadRadius, origin.getY(), origin.getZ() + spreadRadius)) {
            int columnHeight = height - pos.distManhattan(origin);
            if (columnHeight >= 0) {
                placed |= placeColumn(level, seaLevel, pos, columnHeight, config.reach().sample(random), config.state());
            }
        }
        return placed;
    }

    private boolean placeColumn(LevelAccessor level, int seaLevel, BlockPos center, int height, int reach, BlockState state) {
        boolean placed = false;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.getX() - reach, center.getY(), center.getZ() - reach,
                center.getX() + reach, center.getY(), center.getZ() + reach)) {
            int distance = pos.distManhattan(center);
            BlockPos.MutableBlockPos cursor = isAirOrLavaOcean(level, seaLevel, pos)
                    ? findSurface(level, seaLevel, pos.mutable(), distance)
                    : findAir(level, pos.mutable(), distance);

            if (cursor == null) continue;

            for (int remaining = height - distance / 2; remaining >= 0; remaining--) {
                if (isAirOrLavaOcean(level, seaLevel, cursor)) {
                    this.setBlock(level, cursor, state);
                    placed = true;
                } else if (!level.getBlockState(cursor).is(state.getBlock())) {
                    break;
                }
                cursor.move(Direction.UP);
            }
        }
        return placed;
    }

    @Nullable
    private static BlockPos.MutableBlockPos findSurface(LevelAccessor level, int seaLevel, BlockPos.MutableBlockPos pos, int distance) {
        while (pos.getY() > level.getMinY() + 1 && distance > 0) {
            distance--;
            if (canPlaceAt(level, seaLevel, pos)) return pos;
            pos.move(Direction.DOWN);
        }
        return null;
    }

    @Nullable
    private static BlockPos.MutableBlockPos findAir(LevelAccessor level, BlockPos.MutableBlockPos pos, int distance) {
        while (pos.getY() <= level.getMaxY() && distance > 0) {
            distance--;
            BlockState current = level.getBlockState(pos);
            if (CANNOT_PLACE_ON.contains(current.getBlock())) return null;
            if (current.isAir()) return pos;
            pos.move(Direction.UP);
        }
        return null;
    }

    private static boolean canPlaceAt(LevelAccessor level, int seaLevel, BlockPos.MutableBlockPos pos) {
        if (!isAirOrLavaOcean(level, seaLevel, pos)) return false;
        BlockState below = level.getBlockState(pos.move(Direction.DOWN));
        pos.move(Direction.UP);
        return !below.isAir() && !CANNOT_PLACE_ON.contains(below.getBlock());
    }

    private static boolean isAirOrLavaOcean(LevelAccessor level, int seaLevel, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(Blocks.LAVA) && pos.getY() <= seaLevel;
    }
}
