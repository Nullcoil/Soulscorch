package net.nullcoil.soulscorch.item.custom;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nullcoil.nidalib.attribute.Corruption;
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
            // Cleanse 1 to 3 Corruption (never below 0)
            Corruption.remove(player, player.getRandom().nextInt(3) + 1);
        }

        return result;
    }
}