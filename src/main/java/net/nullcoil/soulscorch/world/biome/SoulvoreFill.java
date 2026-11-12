package net.nullcoil.soulscorch.world.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.nullcoil.soulscorch.block.ModBlocks;

/**
 * Terrain passes for the Soulvore biomes, run around the carver step:
 * <ol>
 *   <li>{@link #fillCaverns} before carvers: Soulvore Caverns becomes solid Soul Stone (with a
 *       rounded, noisy border), so its own cave carver (the only carver allowed to cut Soul
 *       Stone) shapes it from scratch.</li>
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

    // Border shape: instead of filling whole 4x4x4 biome cells, the Caverns' membership is
    // smoothly interpolated between cells and then roughened with noise, so the border with
    // the Crags is rounded and craggy rather than a wall of big squares.
    private static final float BORDER_ROUGHNESS = 0.35f; // noise strength, 0 to under 0.5 (0 = smooth rounded border)
    private static final double BORDER_SCALE = 0.11;     // noise frequency (higher = smaller bumps)
    private static final ImprovedNoise BORDER_NOISE = new ImprovedNoise(RandomSource.create(0x5017_C0BEL));

    /**
     * Before carvers: Soulvore Caverns becomes solid Soul Stone (everything but bedrock),
     * with a rounded, noisy border.
     */
    public static void fillCaverns(ChunkAccess chunk, int minY, int maxY, BiomeSource biomes, Climate.Sampler sampler) {
        BlockState soulStone = ModBlocks.SOUL_STONE.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();
        int bottom = Math.max(minY, chunk.getMinY());
        int top = Math.min(maxY, chunk.getMinY() + chunk.getHeight());
        if (top <= bottom) return;

        // Caverns membership of every biome cell this chunk can touch, one cell of margin around
        int q0x = QuartPos.fromBlock(startX) - 1, q0z = QuartPos.fromBlock(startZ) - 1;
        int q0y = QuartPos.fromBlock(bottom) - 1;
        int cellsY = QuartPos.fromBlock(top - 1) - q0y + 2;
        float[][][] member = new float[6][6][cellsY];

        // This chunk's own cells first (cheap). Caverns are always ringed by Crags, so a chunk
        // with neither can't be touched by the border, and skips the costlier neighbor lookups.
        boolean anyCaverns = false, anySoulvore = false;
        for (int cx = 1; cx <= 4; cx++) {
            for (int cz = 1; cz <= 4; cz++) {
                for (int cy = 0; cy < cellsY; cy++) {
                    Holder<Biome> biome = chunk.getNoiseBiome(q0x + cx, q0y + cy, q0z + cz);
                    if (biome.is(ModBiomes.SOULVORE_CAVERNS)) {
                        member[cx][cz][cy] = 1f;
                        anyCaverns = anySoulvore = true;
                    } else if (biome.is(ModBiomes.SOULVORE_CRAGS)) {
                        anySoulvore = true;
                    }
                }
            }
        }
        if (!anySoulvore) return;

        // The one-cell margin from neighboring chunks, straight from the biome source
        for (int cx = 0; cx < 6; cx++) {
            for (int cz = 0; cz < 6; cz++) {
                if (cx >= 1 && cx <= 4 && cz >= 1 && cz <= 4) continue;
                for (int cy = 0; cy < cellsY; cy++) {
                    if (biomes.getNoiseBiome(q0x + cx, q0y + cy, q0z + cz, sampler).is(ModBiomes.SOULVORE_CAVERNS)) {
                        member[cx][cz][cy] = 1f;
                        anyCaverns = true;
                    }
                }
            }
        }
        if (!anyCaverns) return;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = startX + dx, z = startZ + dz;
                for (int y = bottom; y < top; y++) {
                    if (!insideCaverns(member, q0x, q0y, q0z, x, y, z)) continue;

                    pos.set(x, y, z);
                    BlockState current = chunk.getBlockState(pos);
                    if (current.is(Blocks.BEDROCK) || current == soulStone) continue;
                    chunk.setBlockState(pos, soulStone);
                }
            }
        }
    }

    /** Trilinear interpolation of cell membership, plus noise, against 0.5. */
    private static boolean insideCaverns(float[][][] member, int q0x, int q0y, int q0z, int x, int y, int z) {
        // Cell centers sit at 4q + 1.5 in block coordinates
        float tx = (x - 1.5f) / 4f - q0x, ty = (y - 1.5f) / 4f - q0y, tz = (z - 1.5f) / 4f - q0z;
        int ix = Mth.floor(tx), iy = Mth.floor(ty), iz = Mth.floor(tz);
        float fx = tx - ix, fy = ty - iy, fz = tz - iz;

        float c000 = cell(member, ix, iy, iz), c100 = cell(member, ix + 1, iy, iz);
        float c010 = cell(member, ix, iy + 1, iz), c110 = cell(member, ix + 1, iy + 1, iz);
        float c001 = cell(member, ix, iy, iz + 1), c101 = cell(member, ix + 1, iy, iz + 1);
        float c011 = cell(member, ix, iy + 1, iz + 1), c111 = cell(member, ix + 1, iy + 1, iz + 1);

        // Fast path: all eight neighbors agree, and the noise is too weak to flip it
        float sum = c000 + c100 + c010 + c110 + c001 + c101 + c011 + c111;
        if (sum == 8f) return true;
        if (sum == 0f) return false;

        float m = (float) Mth.lerp3(fx, fy, fz, c000, c100, c010, c110, c001, c101, c011, c111);
        float n = (float) BORDER_NOISE.noise(x * BORDER_SCALE, y * BORDER_SCALE, z * BORDER_SCALE);
        return m + n * BORDER_ROUGHNESS > 0.5f;
    }

    private static float cell(float[][][] member, int cx, int cy, int cz) {
        cx = Mth.clamp(cx, 0, member.length - 1);
        cz = Mth.clamp(cz, 0, member[0].length - 1);
        cy = Mth.clamp(cy, 0, member[0][0].length - 1);
        return member[cx][cz][cy];
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
        boolean[][] scanned = new boolean[16][16];
        boolean any = false;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                if (!columnHasSoulvore(chunk, startX + dx, startZ + dz, bottom, top)) continue;
                any = true;
                scanned[dx][dz] = true;
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
                if (!scanned[dx][dz]) continue;
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
                        } else if (depthFromBelow[i] < CAVERN_SKIN || nearSideOpening(solid, scanned, dx, dz, i)) {
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
    private static boolean nearSideOpening(boolean[][][] solid, boolean[][] scanned, int dx, int dz, int i) {
        for (int d = 1; d <= CAVERN_SKIN; d++) {
            if (open(solid, scanned, dx + d, dz, i) || open(solid, scanned, dx - d, dz, i)
                    || open(solid, scanned, dx, dz + d, i) || open(solid, scanned, dx, dz - d, i)) return true;
        }
        return false;
    }

    private static boolean open(boolean[][][] solid, boolean[][] scanned, int dx, int dz, int i) {
        // Other chunks, and columns outside the Soulvore biomes, weren't scanned: treat as solid
        if (dx < 0 || dx >= 16 || dz < 0 || dz >= 16 || !scanned[dx][dz]) return false;
        return !solid[dx][dz][i];
    }

    private static boolean columnHasSoulvore(ChunkAccess chunk, int x, int z, int bottom, int top) {
        for (int y = bottom; y < top; y += 4) {
            Holder<Biome> biome = biomeAt(chunk, x, y, z);
            if (biome.is(ModBiomes.SOULVORE_CAVERNS) || biome.is(ModBiomes.SOULVORE_CRAGS)) return true;
        }
        return false;
    }
}
