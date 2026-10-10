package net.nullcoil.soulscorch.entity.ai.heedless;

import net.minecraft.util.Mth;

public class HeedlessProximity {
    public static final float FADE_START = 8f;
    public static final float FADE_FULL = 5f;
    public static final float DETONATE = 3f;

    private HeedlessProximity() {}

    public static float glow(float distance) {
        if (distance >= FADE_START) return 0f;
        if (distance <= FADE_FULL) return 1f;
        float t = (FADE_START - distance) / (FADE_START - FADE_FULL);
        return Mth.square(t) * (3f - 2f * t);
    }
}
