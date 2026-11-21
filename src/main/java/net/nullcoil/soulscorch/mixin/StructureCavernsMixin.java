package net.nullcoil.soulscorch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.nullcoil.soulscorch.world.biome.SoulvoreFill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Structures that would reach into Soulvore Caverns aren't placed at all.
 *
 * <p>A structure's layout (all its pieces) is chosen before any terrain exists, from the
 * biome at its starting point only, so a fortress starting in Nether Wastes can stretch right
 * across the Caverns. Its blocks come much later, and WorldGenRegionMixin refuses the ones in
 * the Caverns, which left structures cut off (and stray bits like hanging chains). Instead, a
 * layout whose outline touches the Caverns is thrown out here, the same as one that failed
 * vanilla's own checks. Structures that fit (in the Crags, say) are unaffected.
 */
@Mixin(Structure.class)
public abstract class StructureCavernsMixin {
    @ModifyReturnValue(method = "generate", at = @At("RETURN"))
    private StructureStart soulscorch$avoidCaverns(StructureStart start,
                                                   @Local(argsOnly = true) BiomeSource biomes,
                                                   @Local(argsOnly = true) RandomState randomState) {
        if (!start.isValid()) return start;
        return SoulvoreFill.overlapsCaverns(start.getBoundingBox(), biomes, randomState.sampler())
                ? StructureStart.INVALID_START
                : start;
    }
}
