package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.nullcoil.nidalib.attribute.ModAttributes;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.item.ModItems;

public class SoulscorchEffect extends MobEffect {
    public SoulscorchEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void onMobHurt(ServerLevel serverLevel, LivingEntity livingEntity, int amplifier, DamageSource damageSource, float amount) {
        // 1. Check immunities and ensure there is actual damage
        if (amount <= 0.0f || livingEntity.hasEffect(ModEffects.CAT_BUFF) || livingEntity.hasEffect(ModEffects.DOG_BUFF)) {
            super.onMobHurt(serverLevel, livingEntity, amplifier, damageSource, amount);
            return;
        }

        boolean totemFound = false;

        // 2. Check for the Soulward Totem (Players only)
        if (livingEntity instanceof Player player) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);

                if (stack.is(ModItems.SOULWARD_TOTEM)) {
                    EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;

                    // Damage the totem to absorb the corruption!
                    stack.hurtAndBreak(5, player, slot);
                    totemFound = true;
                    break;
                }
            }
        }

        // 3. Apply the Corruption if they weren't protected by a Totem
        if (!totemFound) {
            AttributeInstance corruptionAttr = livingEntity.getAttribute(ModAttributes.CORRUPTION);

            if (corruptionAttr != null) {
                double newCorruption = corruptionAttr.getBaseValue() + 1.0D; // Add half a heart of corruption
                corruptionAttr.setBaseValue(newCorruption);

                float maxHealth = livingEntity.getMaxHealth();

                // 4. Death check: Has the corruption consumed their whole soul?
                if (newCorruption >= maxHealth) {
                    livingEntity.hurtServer(serverLevel, serverLevel.damageSources().genericKill(), Float.MAX_VALUE);
                } else {
                    // 5. Clamp their current health down if it exceeds their new safe limit
                    float safeHealth = maxHealth - (float) newCorruption;
                    if (livingEntity.getHealth() > safeHealth) {
                        livingEntity.setHealth(Math.max(0.0f, safeHealth));
                    }
                }
            }
        }

        super.onMobHurt(serverLevel, livingEntity, amplifier, damageSource, amount);
    }
}