package net.nullcoil.soulscorch.world.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.nullcoil.soulscorch.block.ModBlocks;

public final class SoulvoreFill {
    private SoulvoreFill() {}

    /** True if the 4x4x4 biome cell containing this block is Soulvore Caverns. */
    public static boolean isSoulvore(ChunkAccess chunk, int x, int y, int z) {
        return chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z))
                .is(ModBiomes.SOULVORE_CAVERNS);
    }

    /**
     * Turns every non-bedrock block of Soulvore Caverns in this chunk into Soul Stone,
     * between minY (inclusive) and maxY (exclusive). Air, lava and carved-out
     * space all get filled.
     */
    public static void fill(ChunkAccess chunk, int minY, int maxY) {
        BlockState soulStone = ModBlocks.SOUL_STONE.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();
        int bottom = Math.max(minY, chunk.getMinY());
        int top = Math.min(maxY, chunk.getMinY() + chunk.getHeight());

        // Walk the chunk one biome cell (4x4x4) at a time,
        // so the biome lookup happens once per cell instead of once per block.
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
}