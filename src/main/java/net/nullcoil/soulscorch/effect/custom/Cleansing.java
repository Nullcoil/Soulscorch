package net.nullcoil.soulscorch.effect.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Removes every harmful effect from an entity, keeping the beneficial and neutral ones.
 *
 * <p>Asks for it are collected and carried out at the end of the server tick. Effects are
 * usually the ones asking (Asclepiosis, from inside its own tick), and removing effects while
 * the game is still going through an entity's effects would break that loop.
 */
public final class Cleansing {
    private Cleansing() {}

    private static final Set<LivingEntity> PENDING = Collections.newSetFromMap(new WeakHashMap<>());

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (PENDING.isEmpty()) return;
            List<LivingEntity> entities = new ArrayList<>(PENDING);
            PENDING.clear();
            for (LivingEntity entity : entities) {
                if (entity.isAlive() && !entity.isRemoved()) cleanse(entity);
            }
        });
    }

    /** Cleanse this entity at the end of the tick. Server side only. */
    public static void queue(LivingEntity entity) {
        if (!entity.level().isClientSide()) PENDING.add(entity);
    }

    /** Cleanse now. Only safe outside the entity's own effect ticking. */
    public static void cleanse(LivingEntity entity) {
        List<Holder<MobEffect>> harmful = new ArrayList<>();
        for (MobEffectInstance instance : entity.getActiveEffects()) {
            if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                harmful.add(instance.getEffect());
            }
        }
        for (Holder<MobEffect> effect : harmful) {
            entity.removeEffect(effect);
        }
    }
}
