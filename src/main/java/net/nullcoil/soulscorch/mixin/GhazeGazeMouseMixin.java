package net.nullcoil.soulscorch.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.nullcoil.soulscorch.entity.client.ghaze.GhazeGaze;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies the Ghaze's camera pull every frame, right after the mouse turns the camera. */
@Mixin(MouseHandler.class)
public abstract class GhazeGazeMouseMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "handleAccumulatedMovement", at = @At("TAIL"))
    private void soulscorch$ghazeGaze(CallbackInfo ci) {
        GhazeGaze.tick(this.minecraft);
    }
}
