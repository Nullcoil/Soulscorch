package net.nullcoil.soulscorch.world.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.nullcoil.soulscorch.block.ModBlocks;

public final class SoulvoreFill {
    private SoulvoreFill() {}

    // Crags layering, counted from the nearest open face (floor or ceiling):
    // depth 0 is the exposed surface.
    private static final int SOUL_STONE_DEPTH = 4;  // surface + Soul Stone layer beneath it
    private static final int BLACKSTONE_DEPTH = 4;  // then this many Blackstone, then Netherrack

    private static Holder<Biome> biomeAt(ChunkAccess chunk, int x, int y, int z) {
        return chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z));
    }

    /** True if the 4x4x4 biome cell containing this block is Soulvore Caverns. */
    public static boolean isSoulvore(ChunkAccess chunk, int x, int y, int z) {
        return biomeAt(chunk, x, y, z).is(ModBiomes.SOULVORE_CAVERNS);
    }

    /**
     * Rewrites the Soulvore biomes' terrain in this chunk, between minY (inclusive)
     * and maxY (exclusive):
     * <ul>
     *   <li>Caverns: every non-bedrock block becomes Soul Stone, including air and lava.</li>
     *   <li>Crags: solid blocks are layered by depth below the nearest open face:
     *       Soul Stone, then Blackstone, then Netherrack. Air and lava stay, so the
     *       Nether's terrain shape and lava seas are kept.</li>
     * </ul>
     */
    public static void fill(ChunkAccess chunk, int minY, int maxY) {
        BlockState soulStone = ModBlocks.SOUL_STONE.defaultBlockState();
        BlockState blackstone = Blocks.BLACKSTONE.defaultBlockState();
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();
        int bottom = Math.max(minY, chunk.getMinY());
        int top = Math.min(maxY, chunk.getMinY() + chunk.getHeight());
        int height = top - bottom;
        if (height <= 0) return;

        boolean[] solid = new boolean[height];
        int[] depthFromAbove = new int[height];
        int[] depthFromBelow = new int[height];

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = startX + dx;
                int z = startZ + dz;

                // Skip columns with no Soulvore biome at all (most of the Nether)
                if (!columnHasSoulvore(chunk, x, z, bottom, top)) continue;

                // Read the column once: what's solid (bedrock counts, so the roof isn't "surface")
                for (int i = 0; i < height; i++) {
                    BlockState state = chunk.getBlockState(pos.set(x, bottom + i, z));
                    solid[i] = !state.isAir() && state.getFluidState().isEmpty();
                }

                // Distance to the nearest open block above and below
                int run = 0;
                for (int i = height - 1; i >= 0; i--) {
                    run = solid[i] ? run + 1 : 0;
                    depthFromAbove[i] = run - 1;
                }
                run = 0;
                for (int i = 0; i < height; i++) {
                    run = solid[i] ? run + 1 : 0;
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

                    boolean caverns = biome.is(ModBiomes.SOULVORE_CAVERNS);
                    if (!caverns && !biome.is(ModBiomes.SOULVORE_CRAGS)) continue;

                    pos.set(x, y, z);
                    BlockState current = chunk.getBlockState(pos);
                    if (current.is(Blocks.BEDROCK)) continue;

                    BlockState target;
                    if (caverns) {
                        target = soulStone;
                    } else {
                        if (!solid[i]) continue; // crags keep their air and lava
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

    private static boolean columnHasSoulvore(ChunkAccess chunk, int x, int z, int bottom, int top) {
        for (int y = bottom; y < top; y += 4) {
            Holder<Biome> biome = biomeAt(chunk, x, y, z);
            if (biome.is(ModBiomes.SOULVORE_CAVERNS) || biome.is(ModBiomes.SOULVORE_CRAGS)) return true;
        }
        return false;
    }
}