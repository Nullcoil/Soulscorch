package net.nullcoil.soulscorch.entity.client.heedless;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

@Environment(EnvType.CLIENT)
public class HeedlessRenderState extends LivingEntityRenderState {
    public float cystGlow;
    public int warnVariant;
    public AnimationState warnAnimation = new AnimationState();
}
