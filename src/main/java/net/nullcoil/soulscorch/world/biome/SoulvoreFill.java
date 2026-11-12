package net.nullcoil.soulscorch.world.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.nullcoil.soulscorch.block.ModBlocks;

/**
 * Terrain passes for the Soulvore biomes, run around the carver step:
 * <ol>
 *   <li>{@link #fillCaverns} before carvers: Soulvore Caverns becomes solid Soul Stone, so its
 *       own cave carver (the only carver allowed to cut Soul Stone) shapes it from scratch.</li>
 *   <li>{@link #layer} after carvers: both biomes get their rock layers, based on the final,
 *       carved terrain.</li>
 * </ol>
 */
public final class SoulvoreFill {
    private SoulvoreFill() {}

    // Layer depths, counted from the nearest open face.
    private static final int SOUL_STONE_DEPTH = 4;  // Crags: surface + Soul Stone layer
    private static final int BLACKSTONE_DEPTH = 4;  // then Blackstone, then Netherrack
    private static final int CAVERN_SKIN = 2;       // Caverns: walls and ceilings stay Soul Stone this deep

    private static Holder<Biome> biomeAt(ChunkAccess chunk, int x, int y, int z) {
        return chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z));
    }

    /** True if the 4x4x4 biome cell containing this block is Soulvore Caverns. */
    public static boolean isSoulvore(ChunkAccess chunk, int x, int y, int z) {
        return biomeAt(chunk, x, y, z).is(ModBiomes.SOULVORE_CAVERNS);
    }

    /** Before carvers: every non-bedrock block of Soulvore Caverns becomes Soul Stone. */
    public static void fillCaverns(ChunkAccess chunk, int minY, int maxY) {
        BlockState soulStone = ModBlocks.SOUL_STONE.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();
        int bottom = Math.max(minY, chunk.getMinY());
        int top = Math.min(maxY, chunk.getMinY() + chunk.getHeight());

        // One biome lookup per 4x4x4 cell
        for (int cellX = 0; cellX < 16; cellX += 4) {
            for (int cellZ = 0; cellZ < 16; cellZ += 4) {
                for (int cellY = bottom; cellY < top; cellY += 4) {
                    if (!isSoulvore(chunk, startX + cellX, cellY, startZ + cellZ)) continue;

                    for (int dx = 0; dx < 4; dx++) {
                        for (int dz = 0; dz < 4; dz++) {
                            for (int y = cellY; y < Math.min(cellY + 4, top); y++) {
                                pos.set(startX + cellX + dx, y, startZ + cellZ + dz);
                                BlockState current = chunk.getBlockState(pos);
                                if (current.is(Blocks.BEDROCK) || current == soulStone) continue;
                                chunk.setBlockState(pos, soulStone);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * After carvers: rock layers, measured on the carved terrain.
     * <ul>
     *   <li>Crags: Soul Stone at every open face, then Blackstone, then Netherrack.</li>
     *   <li>Caverns: floors get Soul Soil, then Soul Stone, Blackstone and Netherrack going down.
     *       Walls and ceilings stay Soul Stone.</li>
     * </ul>
     * Air, lava and bedrock are never touched.
     */
    public static void layer(ChunkAccess chunk, int minY, int maxY) {
        BlockState soulStone = ModBlocks.SOUL_STONE.defaultBlockState();
        BlockState soulSoil = Blocks.SOUL_SOIL.defaultBlockState();
        BlockState blackstone = Blocks.BLACKSTONE.defaultBlockState();
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();
        int bottom = Math.max(minY, chunk.getMinY());
        int top = Math.min(maxY, chunk.getMinY() + chunk.getHeight());
        int height = top - bottom;
        if (height <= 0) return;

        // Solid map of the whole chunk, so walls can check their sideways neighbors
        boolean[][][] solid = new boolean[16][16][height];
        boolean any = false;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                if (!columnHasSoulvore(chunk, startX + dx, startZ + dz, bottom, top)) continue;
                any = true;
                for (int i = 0; i < height; i++) {
                    BlockState state = chunk.getBlockState(pos.set(startX + dx, bottom + i, startZ + dz));
                    // Bedrock counts as solid, so the blocks under the roof aren't "surface"
                    solid[dx][dz][i] = !state.isAir() && state.getFluidState().isEmpty();
                }
            }
        }
        if (!any) return;

        int[] depthFromAbove = new int[height];
        int[] depthFromBelow = new int[height];

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = startX + dx;
                int z = startZ + dz;
                if (!columnHasSoulvore(chunk, x, z, bottom, top)) continue;
                boolean[] column = solid[dx][dz];

                // Distance to the nearest open block above and below
                int run = 0;
                for (int i = height - 1; i >= 0; i--) {
                    run = column[i] ? run + 1 : 0;
                    depthFromAbove[i] = run - 1;
                }
                run = 0;
                for (int i = 0; i < height; i++) {
                    run = column[i] ? run + 1 : 0;
                    depthFromBelow[i] = run - 1;
                }

                // Small per-column wobble so the layer boundaries aren't perfectly flat
                int wobble = Math.floorMod(x * 734287 ^ z * 912931, 3) - 1; // -1, 0 or 1
                int soulStoneEnd = SOUL_STONE_DEPTH + wobble;
                int blackstoneEnd = soulStoneEnd + BLACKSTONE_DEPTH;

                Holder<Biome> biome = null;
                for (int i = 0; i < height; i++) {
                    int y = bottom + i;
                    if (biome == null || (y & 3) == 0) biome = biomeAt(chunk, x, y, z);
                    if (!column[i]) continue;

                    boolean caverns = biome.is(ModBiomes.SOULVORE_CAVERNS);
                    if (!caverns && !biome.is(ModBiomes.SOULVORE_CRAGS)) continue;

                    pos.set(x, y, z);
                    BlockState current = chunk.getBlockState(pos);
                    if (current.is(Blocks.BEDROCK)) continue;

                    BlockState target;
                    if (caverns) {
                        int below = depthFromAbove[i]; // how far under the floor above us
                        if (below == 0) {
                            target = soulSoil; // the floor itself
                        } else if (depthFromBelow[i] < CAVERN_SKIN || nearSideOpening(solid, dx, dz, i)) {
                            target = soulStone; // ceilings and walls
                        } else if (below < soulStoneEnd) {
                            target = soulStone;
                        } else if (below < blackstoneEnd) {
                            target = blackstone;
                        } else {
                            target = netherrack;
                        }
                    } else {
                        int depth = Math.min(depthFromAbove[i], depthFromBelow[i]);
                        if (depth < soulStoneEnd) target = soulStone;
                        else if (depth < blackstoneEnd) target = blackstone;
                        else target = netherrack;
                    }

                    if (current != target) chunk.setBlockState(pos, target);
                }
            }
        }
    }

    /** Whether there's an opening within {@link #CAVERN_SKIN} blocks sideways (same chunk only). */
    private static boolean nearSideOpening(boolean[][][] solid, int dx, int dz, int i) {
        for (int d = 1; d <= CAVERN_SKIN; d++) {
            if (open(solid, dx + d, dz, i) || open(solid, dx - d, dz, i)
                    || open(solid, dx, dz + d, i) || open(solid, dx, dz - d, i)) return true;
        }
        return false;
    }

    private static boolean open(boolean[][][] solid, int dx, int dz, int i) {
        if (dx < 0 || dx >= 16 || dz < 0 || dz >= 16) return false; // other chunk: unknown, treat as solid
        boolean[] column = solid[dx][dz];
        // Columns outside the Soulvore biomes weren't scanned; their arrays are all false,
        // so only trust columns that were (they contain at least one solid block)
        return column.length > i && !column[i] && hasAnySolid(column);
    }

    private static boolean hasAnySolid(boolean[] column) {
        for (boolean b : column) if (b) return true;
        return false;
    }

    private static boolean columnHasSoulvore(ChunkAccess chunk, int x, int z, int bottom, int top) {
        for (int y = bottom; y < top; y += 4) {
            Holder<Biome> biome = biomeAt(chunk, x, y, z);
            if (biome.is(ModBiomes.SOULVORE_CAVERNS) || biome.is(ModBiomes.SOULVORE_CRAGS)) return true;
        }
        return false;
    }
}