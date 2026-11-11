package net.nullcoil.soulscorch.item.custom;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.nullcoil.nidalib.attribute.ModAttributes;
import org.jspecify.annotations.NonNull;

public class GhostPepperItem extends BlockItem {

    public GhostPepperItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NonNull ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide() && entity instanceof Player player) {

            player.hurt(player.damageSources().onFire(), 1f);
            AttributeInstance corruptionAttr = player.getAttribute(ModAttributes.CORRUPTION);

            if (corruptionAttr != null) {
                double currentCorruption = corruptionAttr.getBaseValue();

                if (currentCorruption > 0d) {
                    RandomSource random = player.getRandom();
                    double cleanseAmount = random.nextInt(3) + 1;

                    double newCorruption = Math.max(0d, currentCorruption - cleanseAmount);
                    corruptionAttr.setBaseValue(newCorruption);
                }
            }
        }
        return result;
    }
}