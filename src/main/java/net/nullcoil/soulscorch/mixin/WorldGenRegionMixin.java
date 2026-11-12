package net.nullcoil.soulscorch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.nullcoil.soulscorch.block.ModBlocks;
import net.nullcoil.soulscorch.util.ModTags;
import net.nullcoil.soulscorch.world.biome.SoulvoreFill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin {
    /**
     * Features and structures place blocks through WorldGenRegion. Refusing
     * every write inside Soulvore Caverns keeps neighboring biomes' features
     * (fortresses, bastions, glowstone, basalt, etc.) from cutting into it.
     */
    @Inject(method = "setBlock", at = @At("HEAD"), cancellable = true)
    private void soulscorch$protectSoulvore(CallbackInfoReturnable<Boolean> cir,
                                            @Local(argsOnly = true) BlockPos pos,
                                            @Local(argsOnly = true) BlockState state) {
        if (state.is(ModBlocks.SOUL_STONE)) return;

        WorldGenRegion self = (WorldGenRegion) (Object) this;
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        if (!self.hasChunk(chunkX, chunkZ)) return; // let vanilla handle out-of-region writes

        if (SoulvoreFill.isSoulvore(self.getChunk(chunkX, chunkZ), pos.getX(), pos.getY(), pos.getZ())) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Magma placed by features (magma ore, deltas, ...) in a soul biome becomes Soul Slag.
     */
    @ModifyVariable(method = "setBlock", at = @At("HEAD"), argsOnly = true)
    private BlockState soulscorch$soulSlag(BlockState state, @Local(argsOnly = true) BlockPos pos) {
        if (!state.is(Blocks.MAGMA_BLOCK)) return state;

        WorldGenRegion self = (WorldGenRegion) (Object) this;
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        if (!self.hasChunk(chunkX, chunkZ)) return state;

        boolean soul = self.getChunk(chunkX, chunkZ)
                .getNoiseBiome(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(pos.getZ()))
                .is(ModTags.Biomes.SOUL_BIOMES);
        return soul ? ModBlocks.SOUL_SLAG_BLOCK.defaultBlockState() : state;
    }
}
