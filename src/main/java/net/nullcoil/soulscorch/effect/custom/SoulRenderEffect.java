package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.InstantaneousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.nullcoil.soulscorch.Soulscorch;
import net.nullcoil.soulscorch.attribute.ModAttributes;
import org.jspecify.annotations.Nullable;

public class SoulRenderEffect extends InstantaneousMobEffect {
    public SoulRenderEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void applyInstantaneousEffect(final ServerLevel level, final @Nullable Entity source, final @Nullable Entity owner, final LivingEntity livingEntity, final int amplifier, final double scale) {
        Soulscorch.LOGGER.info("SoulRenderEffect.applyInstantenousEffect called! amplifier={}, entity={}", amplifier, livingEntity.getName().getString());
        AttributeInstance corruptionAttr = livingEntity.getAttribute(ModAttributes.CORRUPTION);

        if (corruptionAttr != null) {
            float currentCorruption = (float) corruptionAttr.getBaseValue();

            if (currentCorruption > 0.0F) {
                float currentHealth = livingEntity.getHealth();
                float trueMaxHealth = livingEntity.getMaxHealth();

                // Amplifier 0 (Tier I) = 10 cleanse. Amplifier 1 (Tier II) = 20 cleanse.
                float cleanseAmount = (amplifier + 1) * 10.0F;
                float newCorruption = Math.max(0.0F, currentCorruption - cleanseAmount);

                // Their "Max HP" prior to the cleanse
                float effectiveMaxHealthBefore = trueMaxHealth - currentCorruption;
                if (effectiveMaxHealthBefore <= 0.0F) {
                    effectiveMaxHealthBefore = 1.0F; // Prevent divide by zero
                }

                // Calculate the ratio: CurrHP / EffectiveMaxHP
                float ratio = currentHealth / effectiveMaxHealthBefore;

                // 1. Apply the partial or full cleanse
                corruptionAttr.setBaseValue((double) newCorruption);

                // 2. The Ratio Heal based on their NEW effective max health
                float effectiveMaxHealthAfter = trueMaxHealth - newCorruption;
                livingEntity.setHealth(Mth.ceil(effectiveMaxHealthAfter * ratio));
            }
        }

        super.applyInstantaneousEffect(level, source, owner, livingEntity, amplifier, scale);
    }
}