package net.nullcoil.soulscorch.mixin;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidStateModelSet.class)
public class FluidStateModelSetMixin {

    @Shadow
    @Final
    @Mutable
    private static FluidModel.Unbaked LAVA_MODEL;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void soulscorch$lavaTranslucent(CallbackInfo ci) {
        LAVA_MODEL = new FluidModel.Unbaked(
                LAVA_MODEL.stillMaterial().withForceTranslucent(true),
                LAVA_MODEL.flowingMaterial().withForceTranslucent(true),
                LAVA_MODEL.overlayMaterial(),
                LAVA_MODEL.tintSource()
        );
    }
}