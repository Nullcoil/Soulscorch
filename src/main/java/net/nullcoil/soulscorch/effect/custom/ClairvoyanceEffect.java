package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Clairvoyance: mobs near you glow through walls, and only you see it (the outline is
 * drawn on your client; nothing is put on the mobs). Glowstone widens the radius.
 */
public class ClairvoyanceEffect extends MobEffect {
    /** Radius at level I, in blocks; each level above adds the same again. */
    public static final double BASE_RADIUS = 16.0;

    public ClairvoyanceEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /** How far you can sense mobs at this amplifier: 16 blocks at I, 32 at II. */
    public static double radius(int amplifier) {
        return BASE_RADIUS * (amplifier + 1);
    }
}
