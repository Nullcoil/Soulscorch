package net.nullcoil.soulscorch.item.foods;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.nullcoil.soulscorch.effect.ModEffects;

public class ModConsumables {
    public static final Consumable VOLATILE_EXTRACT;

    static {
        VOLATILE_EXTRACT = Consumables.defaultDrink().onConsume(
                new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModEffects.VOLATILE,
                        200, 0, false, true, true))).build();
    }
}
