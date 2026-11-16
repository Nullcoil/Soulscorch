package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.InstantaneousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.nullcoil.nidalib.attribute.Corruption;
import net.nullcoil.nidalib.attribute.ModAttributes;
import net.nullcoil.soulscorch.Soulscorch;
import org.jspecify.annotations.Nullable;

public class SoulRenderEffect extends InstantaneousMobEffect {
    public SoulRenderEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void applyInstantaneousEffect(final ServerLevel level, final @Nullable Entity source, final @Nullable Entity owner, final LivingEntity livingEntity, final int amplifier, final double scale) {
        float corruptionBefore = (float) Corruption.get(livingEntity);

        if (corruptionBefore > 0.0F) {
            float maxHealth = livingEntity.getMaxHealth();

            // Their effective max HP before the cleanse, and how full it is
            float effectiveMaxBefore = Math.max(1.0F, maxHealth - corruptionBefore); // avoid divide by zero
            float ratio = livingEntity.getHealth() / effectiveMaxBefore;

            Corruption.remove(livingEntity, (amplifier + 1) * 10.0D);

            float effectiveMaxAfter = maxHealth - (float) Corruption.get(livingEntity);
            livingEntity.setHealth(effectiveMaxAfter * ratio);
        }

        super.applyInstantaneousEffect(level, source, owner, livingEntity, amplifier, scale);
    }
}