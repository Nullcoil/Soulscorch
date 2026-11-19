package net.nullcoil.soulscorch.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * The Soulless's alarm: when one turns hostile, every mob nearby that listens for it
 * (any {@link Listener}, like the Ghaze) hears who it turned on.
 */
public final class SoulAlarm {
    private SoulAlarm() {}

    /** How far the alarm carries, in blocks (the Soulless's follow range). */
    public static final double RADIUS = 35.0;

    /** A mob that reacts when a Soulless nearby raises the alarm. */
    public interface Listener {
        /**
         * @param source the mob that raised the alarm
         * @param target who it turned hostile toward
         */
        void onSoulAlarm(LivingEntity source, LivingEntity target);
    }

    /** Tells every listener within {@link #RADIUS} of {@code source} that it turned on {@code target}. */
    public static void raise(LivingEntity source, @Nullable LivingEntity target) {
        if (target == null || !(source.level() instanceof ServerLevel level)) return;

        AABB area = source.getBoundingBox().inflate(RADIUS);
        for (LivingEntity listener : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != source && entity.isAlive() && entity instanceof Listener)) {
            ((Listener) listener).onSoulAlarm(source, target);
        }
    }
}
