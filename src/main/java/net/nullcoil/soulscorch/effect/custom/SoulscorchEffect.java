package net.nullcoil.soulscorch.effect.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.nullcoil.nidalib.attribute.Corruption;
import net.nullcoil.soulscorch.effect.ModEffects;
import net.nullcoil.soulscorch.item.ModItems;

public class SoulscorchEffect extends MobEffect {
    public SoulscorchEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void onMobHurt(ServerLevel serverLevel, LivingEntity livingEntity, int amplifier, DamageSource damageSource, float amount) {
        if (amount <= 0.0f || livingEntity.hasEffect(ModEffects.CAT_BUFF) || livingEntity.hasEffect(ModEffects.DOG_BUFF)) {
            super.onMobHurt(serverLevel, livingEntity, amplifier, damageSource, amount);
            return;
        }

        boolean totemFound = false;

        if (livingEntity instanceof Player player) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);

                if (stack.is(ModItems.SOULWARD_TOTEM)) {
                    EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;

                    stack.hurtAndBreak(5, player, slot);
                    totemFound = true;
                    break;
                }
            }
        }

        if (!totemFound) {
            Corruption.add(livingEntity, 1.0D);
        }

        super.onMobHurt(serverLevel, livingEntity, amplifier, damageSource, amount);
    }
}