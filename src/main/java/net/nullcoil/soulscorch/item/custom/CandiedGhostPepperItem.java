package net.nullcoil.soulscorch.item.custom;

import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nullcoil.nidalib.attribute.ModAttributes;
import net.nullcoil.soulscorch.effect.ModEffects;
import org.jspecify.annotations.NonNull;

public class CandiedGhostPepperItem extends Item {
    public CandiedGhostPepperItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide() && entity instanceof Player player) {

            // 1. The Cure: Cleanse Poison and Soulscorch
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(ModEffects.SOULSCORCH);

            // 2. The Cleanse: Reduce Corruption by 1.0 to 3.0 (half heart to 1.5 hearts)
            AttributeInstance corruptionAttr = player.getAttribute(ModAttributes.CORRUPTION);

            if (corruptionAttr != null) {
                double currentCorruption = corruptionAttr.getBaseValue();

                if (currentCorruption > 0.0D) {
                    RandomSource random = player.getRandom();
                    double cleanseAmount = random.nextInt(3) + 1;

                    // Floor it at 0 so they can't have negative corruption!
                    double newCorruption = Math.max(0.0D, currentCorruption - cleanseAmount);
                    corruptionAttr.setBaseValue(newCorruption);
                }
            }
        }

        return result;
    }
}