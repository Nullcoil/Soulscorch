package net.nullcoil.soulscorch.entity.client.ghaze;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class GhazeRenderState extends LivingEntityRenderState {
    public boolean isAgitated = false;
    /** 0 calm, 1 agitated, eased between. Fades the calm glow out. */
    public float agitation;

    /** How far the tentacles have spun around the Ghaze, in radians. */
    public float spinAngle;
    /** Spin speed as the tentacles feel it (lags and overshoots the real spin). Sets their lean. */
    public float tentacleSwing;
    /** Per-Ghaze offset so each one's tentacles catch the wind differently. */
    public float windSeed;
    /** 0 = idle (wind and turns), 1 = ghast wiggle. */
    public float wiggle;
}
