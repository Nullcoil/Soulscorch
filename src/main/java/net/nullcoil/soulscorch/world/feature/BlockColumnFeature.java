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
 * Vanilla's basalt column feature (Basalt Deltas), but the block is configurable instead
 * of hard-coded to basalt, and columns can hang from ceilings as well as rise from floors.
 *
 * <p>Everything is written in terms of "grow" (the direction columns extend in) and
 * "anchor" (the opposite: the surface they're attached to). With grow = UP this is
 * exactly vanilla's logic; with grow = DOWN it's the same thing upside down.
 */
public class BlockColumnFeature extends Feature<BlockColumnConfiguration> {
    private static final ImmutableSet<Block> CANNOT_PLACE_ON = ImmutableSet.of(
            Blocks.LAVA, Blocks.BEDROCK, Blocks.MAGMA_BLOCK, Blocks.SOUL_SAND,
            Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS,
            Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);

    /** How far up a hanging cluster looks for a ceiling from where it was placed. */
    private static final int CEILING_SEARCH = 32;

    public BlockColumnFeature(Codec<BlockColumnConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockColumnConfiguration> context) {
        int seaLevel = context.chunkGenerator().getSeaLevel();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockColumnConfiguration config = context.config();
        Direction grow = config.direction();

        // Placement puts us on a floor; hanging columns first climb to the ceiling above it
        BlockPos origin = grow == Direction.DOWN ? findCeiling(level, context.origin()) : context.origin();
        if (origin == null || !canPlaceAt(level, seaLevel, origin.mutable(), grow)) return false;

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
                placed |= placeColumn(level, seaLevel, pos, columnHeight, config.reach().sample(random), config.state(), grow);
            }
        }
        return placed;
    }

    /** The air block directly under the first ceiling above {@code start}, or null. */
    @Nullable
    private static BlockPos findCeiling(LevelAccessor level, BlockPos start) {
        BlockPos.MutableBlockPos pos = start.mutable();
        for (int i = 0; i < CEILING_SEARCH && pos.getY() < level.getMaxY(); i++) {
            if (!level.getBlockState(pos).isAir()) return null; // not in open air
            if (!level.getBlockState(pos.above()).isAir()) return pos.immutable();
            pos.move(Direction.UP);
        }
        return null;
    }

    private boolean placeColumn(LevelAccessor level, int seaLevel, BlockPos center, int height, int reach,
                                BlockState state, Direction grow) {
        boolean placed = false;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.getX() - reach, center.getY(), center.getZ() - reach,
                center.getX() + reach, center.getY(), center.getZ() + reach)) {
            int distance = pos.distManhattan(center);
            BlockPos.MutableBlockPos cursor = isAirOrLavaOcean(level, seaLevel, pos)
                    ? findSurface(level, seaLevel, pos.mutable(), distance, grow)
                    : findAir(level, pos.mutable(), distance, grow);

            if (cursor == null) continue;

            for (int remaining = height - distance / 2; remaining >= 0; remaining--) {
                if (isAirOrLavaOcean(level, seaLevel, cursor)) {
                    this.setBlock(level, cursor, state);
                    placed = true;
                } else if (!level.getBlockState(cursor).is(state.getBlock())) {
                    break;
                }
                cursor.move(grow);
            }
        }
        return placed;
    }

    /** Walks toward the anchor surface until there's a spot to attach a column. */
    @Nullable
    private static BlockPos.MutableBlockPos findSurface(LevelAccessor level, int seaLevel, BlockPos.MutableBlockPos pos,
                                                        int distance, Direction grow) {
        Direction anchor = grow.getOpposite();
        while (pos.getY() > level.getMinY() + 1 && pos.getY() < level.getMaxY() - 1 && distance > 0) {
            distance--;
            if (canPlaceAt(level, seaLevel, pos, grow)) return pos;
            pos.move(anchor);
        }
        return null;
    }

    /** Walks out of solid ground in the growth direction until reaching air. */
    @Nullable
    private static BlockPos.MutableBlockPos findAir(LevelAccessor level, BlockPos.MutableBlockPos pos, int distance,
                                                    Direction grow) {
        while (pos.getY() > level.getMinY() && pos.getY() <= level.getMaxY() && distance > 0) {
            distance--;
            BlockState current = level.getBlockState(pos);
            if (CANNOT_PLACE_ON.contains(current.getBlock())) return null;
            if (current.isAir()) return pos;
            pos.move(grow);
        }
        return null;
    }

    /** Open here, with something solid (and allowed) on the anchor side. */
    private static boolean canPlaceAt(LevelAccessor level, int seaLevel, BlockPos.MutableBlockPos pos, Direction grow) {
        if (!isAirOrLavaOcean(level, seaLevel, pos)) return false;
        Direction anchor = grow.getOpposite();
        BlockState attached = level.getBlockState(pos.move(anchor));
        pos.move(grow);
        return !attached.isAir() && !CANNOT_PLACE_ON.contains(attached.getBlock());
    }

    private static boolean isAirOrLavaOcean(LevelAccessor level, int seaLevel, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(Blocks.LAVA) && pos.getY() <= seaLevel;
    }
}
