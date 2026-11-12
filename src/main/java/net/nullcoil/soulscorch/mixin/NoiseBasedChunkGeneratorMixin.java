package net.nullcoil.soulscorch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.nullcoil.soulscorch.world.biome.SoulvoreFill;
import net.nullcoil.soulscorch.world.carver.SoulvoreCarving;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {
    @Shadow @Final private Holder<NoiseGeneratorSettings> settings;

    /**
     * Before carvers: Soulvore Caverns becomes solid Soul Stone, so its own carver
     * (the only one allowed to cut Soul Stone) carves it from scratch.
     * Limited to the dimension's generation height so the Nether's
     * open space above the bedrock roof stays empty.
     */
    @Inject(method = "applyCarvers", at = @At("HEAD"))
    private void soulscorch$fillCaverns(CallbackInfo ci, @Local(argsOnly = true) ChunkAccess chunk,
                                        @Local(argsOnly = true) RandomState randomState) {
        NoiseSettings noise = this.settings.value().noiseSettings();
        // The biome source lets the fill see biomes in neighboring chunks, for a seamless border
        ChunkGenerator generator = (ChunkGenerator) (Object) this;
        SoulvoreFill.fillCaverns(chunk, noise.minY(), noise.minY() + noise.height(),
                generator.getBiomeSource(), randomState.sampler());
        // Lets the Soulvore cave carver measure how deep into the Caverns it is
        SoulvoreCarving.begin(generator.getBiomeSource(), randomState.sampler());
    }

    /** After carvers: rock layers for both Soulvore biomes, on the carved terrain. */
    @Inject(method = "applyCarvers", at = @At("TAIL"))
    private void soulscorch$layerSoulvore(CallbackInfo ci, @Local(argsOnly = true) ChunkAccess chunk) {
        SoulvoreCarving.end();
        NoiseSettings noise = this.settings.value().noiseSettings();
        SoulvoreFill.layer(chunk, noise.minY(), noise.minY() + noise.height());
    }
}