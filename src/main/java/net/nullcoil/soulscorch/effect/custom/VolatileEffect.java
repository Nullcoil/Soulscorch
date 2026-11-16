package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.nullcoil.soulscorch.Soulscorch;

public class VolatileEffect extends MobEffect {
    private static final ThreadLocal<LivingEntity> WATCHED = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> TOTEM_POPPED = ThreadLocal.withInitial(() -> false);

    public VolatileEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectRemoved(MobEffectInstance instance, LivingEntity entity) {
        if (instance.getDuration() > 0 || instance.isInfiniteDuration()) return;
        if (!(entity.level() instanceof ServerLevel level) || entity.isRemoved()) return;

        float radius = 2f + instance.getAmplifier() + 1;

        WATCHED.set(entity);
        TOTEM_POPPED.set(false);
        try {
            level.explode(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    radius, Level.ExplosionInteraction.MOB
            );
        } finally {
            WATCHED.remove();
        }
        boolean usedTotem = TOTEM_POPPED.get();
        TOTEM_POPPED.remove();

        if (!usedTotem && entity.isAlive() && entity instanceof ServerPlayer player) {
            AdvancementHolder advancement = level.getServer().getAdvancements().get(Soulscorch.id("time_bomb"));
            if (advancement != null) {
                for (String criterion : advancement.value().criteria().keySet()) {
                    player.getAdvancements().award(advancement, criterion);
                }
            }
        }
    }

    public static void noteTotem(LivingEntity entity) {
        if (WATCHED.get() == entity) TOTEM_POPPED.set(true);
    }
}