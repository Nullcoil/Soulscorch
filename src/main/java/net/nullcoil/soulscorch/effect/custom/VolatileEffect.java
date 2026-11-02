package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class VolatileEffect extends MobEffect {
    public VolatileEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectRemoved(MobEffectInstance instance, LivingEntity entity) {
        if (instance.getDuration() > 0 || instance.isInfiniteDuration()) return;
        if (!(entity.level() instanceof ServerLevel level) || entity.isRemoved()) return;

        float radius = 2f + instance.getAmplifier() + 1;
        level.explode(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                radius, Level.ExplosionInteraction.MOB
        );
    }
}
